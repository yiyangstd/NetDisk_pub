package com.CloudDisk;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;
import java.util.Iterator;
import java.util.HashSet;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

/**
 * 监视本地文件夹的变化，
 */
public class FileMonitor implements Runnable {

	File localDir;// 本地文件夹
	//CloudDisk fileInfoDisk;
	FileControl filecontrol;
    NetworkAgent networkAgent;

	public FileMonitor(File localDir, FileControl filecontrol, NetworkAgent networkAgent){
		this.localDir = localDir;
		this.filecontrol = filecontrol;
		this.networkAgent = networkAgent;
		//CloudFile.fileSet = new HashSet<CloudFile>();
	}
	
	/**
	 * 获取本地和异地的文件列表信息，进行对比，产生对应上传下载策略
	 */
	public void scan() {
		scanDir(this.localDir);
		Set<CloudFile> localSet = null;//本地文件列表信息
		Set<CloudFile> remoteSet = null;//异地文件列表信息
		File localFileInfo = new File(ConsoleControl.fileRoot + "\\" + "FileInfo");
		//File localFileInfo = new File("D:\\CloudFileTest\\FileInfo");
		Gson gson = new Gson();
		
		
		//下载服务器端文件信息列表
		try{
			if(filecontrol.do_download(ConsoleControl.fileRoot + "\\" + "remoteFileInfo", "remoteFileInfo")){
		    	try{
		    		//将服务器端文件列表信息由json转换成Set
		    		File fremote = new File(ConsoleControl.fileRoot + "\\" + "remoteFileInfo");
					FileReader fr1 = new FileReader(fremote);
					remoteSet = gson.fromJson(fr1,new TypeToken<Set<CloudFile>>(){}.getType());//将json转换成java对象
					fr1.close();
					fremote.delete();
				}catch(IOException a){
					a.printStackTrace();
				}
		    }
			else{
				//服务器无文件列表信息
				
				
			}
		}catch(Exception e){}
		
		File remoteFileInfo = new File(ConsoleControl.fileRoot + "\\" + "remoteFileInfo");
		try{
			if(remoteFileInfo.exists()){
				remoteFileInfo.delete();
			}
		}catch(Exception e){}
		
		
		//检测本地文件信息列表存在
		if(!localFileInfo.canExecute()){//本地文件信息不存在，即建立新的文件
			try{
				localFileInfo.delete();
				localFileInfo.createNewFile();
				String sets =  "attrib +H \"" + localFileInfo.getAbsolutePath() + "\""; 
				Runtime.getRuntime().exec(sets); 
			}catch(IOException e){
				e.printStackTrace();
			}finally{
				//scanDir(localDir);//扫描文件夹
			}
			fileSetCompare(remoteSet);
		}else{
			
			//本地文件信息列表存在则读取FileInfo文件获得localSet
			try{
				FileReader fr = new FileReader(localFileInfo);
				localSet = gson.fromJson(fr,new TypeToken<Set<CloudFile>>(){}.getType());//将json转换成java对象
				fr.close();
			}catch(IOException a){
				a.printStackTrace();
			}
			//比较本地文件信息和扫描的文件信息
			//本地新建文件：无影响
			//本地修改文件：若修改了文件名以及文件内容，则当本地文件被删除后又新建了一个文件
			//本地删除文件：要与服务器新建文件区别
			for(Iterator<CloudFile> localIt = localSet.iterator();localIt.hasNext();){
				CloudFile localFile = localIt.next();
				boolean flag = false;
				for(Iterator<CloudFile> newIt = CloudFile.fileSet.iterator();newIt.hasNext();){
					CloudFile newFile = newIt.next();
					if(localFile.filename.equals(newFile.filename))
						flag = true;
				}
				if(flag == false){//表示locaFile已经在本地被删除
					localFile.setFlag(true);//将此文件标识为被删除
					CloudFile.fileSet.add(localFile);
					
				}
			}
			
			fileSetCompare(remoteSet);
		}
		

		//将新的文件信息转换为json写入文件FileInfo	
		Gson gson1 = new Gson();
		//System.out.println(1);
		String fileInf = gson1.toJson(CloudFile.fileSet);
		//System.out.println(2);
		try{
			File f1 = new File(ConsoleControl.fileRoot + "\\" + "FileInfo");
			f1.delete();
			f1.createNewFile();
			FileWriter f = new FileWriter(f1);
			f.write(fileInf);
			String sets =  "attrib +H \"" + f1.getAbsolutePath() + "\""; 
			Runtime.getRuntime().exec(sets); 
			
			//FileWriter f = new FileWriter(localFileInfo);
			//FileWriter f = new FileWriter(f1);
			//f.write(fileInf);
			f.close();
		}catch(IOException ea){
			ea.printStackTrace();
		}
		
		//将新的文件信息上传
		NetworkTask newFileInfo = new NetworkTask(localFileInfo, "remoteFileInfo");
		networkAgent.uploadQueue.add(newFileInfo);
		
		
		System.out.println("Monitor done.");
		System.out.println("Wait ...");
	}

	/**
	 * 扫描本地文件夹，将本地文件信息存入CloudFile.fileSet中
	 * @param dir
	 */
	
	public static void scanDir(File dir){
		File[] files = dir.listFiles();
		for(File f1: files){
			if(!f1.isHidden() && (!f1.getName().equals("remoteFileInfo"))){
				if(!f1.isDirectory())
					CloudFile.fileSet.add(new CloudFile(f1));
				else{
					scanDir(f1);
				}
			}
		}
	}
	
	/**
	 * 本地文件列表和服务器文件列表进行对比，确定处理策略
	 * 问题：本地删除文件和服务器新建文件需下载解决；
	 * 问题：异地删除了文件和本地需上传的冲突解决
	 * @param remoteSet
	 */
	public  void fileSetCompare(Set<CloudFile> remoteSet){
		if(remoteSet == null){
			//上传所有文件
			for(Iterator<CloudFile> localIt = CloudFile.fileSet.iterator();localIt.hasNext();){
				CloudFile localNext = localIt.next();
				System.out.println("（0）上传文件：" + localNext.filename.getName());
				try{
					//localNext.setNetworkAgent(this.networkAgent);
					localNext.upload(this.networkAgent);
				}catch(Exception e){}
			}
		}
		else{
			//对比本地文件列表和新的文件列表，得到文件变动信息
			boolean comFlag;
			Set<CloudFile> localRemoveSet = new HashSet<CloudFile>(); 
			Set<CloudFile> remoteRemoveSet = new HashSet<CloudFile>();
			Set<CloudFile> localAddSet = new HashSet<CloudFile>();
			for(Iterator<CloudFile> remoteIt = remoteSet.iterator();remoteIt.hasNext();){//遍历异地文件信息列表查找到与本地列表不同的表项
				CloudFile remoteNext = remoteIt.next();
				comFlag = false;//标记该文件是否在本地被找到
				for(Iterator<CloudFile> localIt = CloudFile.fileSet.iterator();localIt.hasNext();){
					CloudFile localNext = localIt.next();
					if(remoteNext.filename.getName().equals(localNext.filename.getName())){
						comFlag = true;
						if(localNext.flag && (!remoteNext.flag)){
							//本地文件已经删除，则删除服务器相应文件
							System.out.println("删除文件： " + remoteNext.filename.getName());
							//NetworkTask deleteRemoteFile = new NetworkTask(remoteNext.filename, remoteNext.filename.getName());
							//networkAgent.deleteQueue.add(deleteRemoteFile);
							//remoteNext.setNetworkAgent(this.networkAgent);
							remoteNext.delete(this.networkAgent);
							
							//CloudFile.fileSet.remove(localNext);
							localRemoveSet.add(localNext);
							remoteRemoveSet.add(remoteNext);
							//remoteSet.remove(remoteNext);
							//remoteNext.flag = true;
							//remoteSet.add(remoteNext);
							continue;
							}
						else if(remoteNext.flag && (!localNext.flag)){
							//服务器文件被删除
							if(localNext.lastModify > remoteNext.deleteTime){
								//本地修改时间在异地删除时间后，上传(1)
								System.out.println("（1）上传文件：" + localNext.filename.getName());
								//NetworkTask uploadFile = new NetworkTask(localNext.filename, localNext.filename.getName());
								//networkAgent.uploadQueue.add(uploadFile);
								try{
									//localNext.setNetworkAgent(this.networkAgent);
									localNext.upload(this.networkAgent);
								}catch(Exception e){}
								
							}
							else{
								//删除本地文件
								File deleteLocalFile = new File(localNext.filename.getPath());
								deleteLocalFile.delete();
								//CloudFile.fileSet.remove(localNext);
								localRemoveSet.add(localNext);
							}
						}
						else if(remoteNext.lastModify < localNext.lastModify){
							//本地文件有修改，需要上传(2)
							System.out.println("（2）上传文件：" + localNext.filename.getName());
							//NetworkTask uploadModifyFile = new NetworkTask(localNext.filename, localNext.filename.getName());
							//networkAgent.uploadQueue.add(uploadModifyFile);
							try{
								//localNext.setNetworkAgent(this.networkAgent);
								localNext.upload(this.networkAgent);
							}catch(Exception e){}
							
							
						}else if((remoteNext.lastModify > localNext.lastModify) && (!localNext.flag)){
							//服务器文件有修改，需要下载
							//NetworkTask downloadModifyFile = new NetworkTask(localNext.filename, localNext.filename.getName());
							//networkAgent.downloadQueue.add(downloadModifyFile);
							try{
								//localNext.setNetworkAgent(this.networkAgent);
								localNext.download(this.networkAgent);
							}catch(Exception e){}
							
							//CloudFile.fileSet.add(remoteNext);
							localAddSet.add(remoteNext);
						}
					}
				}
				if((!remoteNext.flag) && (!comFlag)){//该服务器文件在本地没有找到，且没被删除，下载
					//NetworkTask downloadUnfindFile = new NetworkTask(remoteNext.filename, remoteNext.filename.getName());
					//networkAgent.downloadQueue.add(downloadUnfindFile);
					try{
						//remoteNext.setNetworkAgent(this.networkAgent);
						remoteNext.download(this.networkAgent);
					}catch(Exception e){}
					
					//CloudFile.fileSet.add(remoteNext);
					localAddSet.add(remoteNext);
					System.out.println("下载： " + remoteNext.filename.getName());
				}
			}
			
			//CloudFile.fileSet.removeAll(localRemoveSet);
			
			if(!localAddSet.isEmpty()){
				CloudFile.fileSet.addAll(localAddSet);
			}
			if(!localRemoveSet.isEmpty()){
				CloudFile.fileSet.removeAll(localRemoveSet);
			}
			remoteSet.removeAll(remoteRemoveSet);
			
			
			for(Iterator<CloudFile> localIt = CloudFile.fileSet.iterator();localIt.hasNext();){//遍历本地文件信息
				CloudFile localNext = localIt.next();
				boolean comFlag1 = false;
				for(Iterator<CloudFile> remoteIt = remoteSet.iterator();remoteIt.hasNext();){
					CloudFile remoteNext = remoteIt.next();
					if(remoteNext.filename.equals(localNext.filename)){
						comFlag1 = true;
					}
				}
				if(!comFlag1){//该文件在异地不存在，上传(3)
					System.out.println("（3）上传文件：" + localNext.filename.getName());
					//NetworkTask uploadUnfindFile = new NetworkTask(localNext.filename, localNext.filename.getName());
					//networkAgent.uploadQueue.add(uploadUnfindFile);
					try{
						//localNext.setNetworkAgent(this.networkAgent);
						localNext.upload(this.networkAgent);
					}catch(Exception e){}
					
				}
			}
		}
	}

	public void run() {
		scan();
	}

}
