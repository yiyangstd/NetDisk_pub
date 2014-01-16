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
 * ���ӱ����ļ��еı仯��
 */
public class FileMonitor implements Runnable {

	File localDir;// �����ļ���
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
	 * ��ȡ���غ���ص��ļ��б���Ϣ�����жԱȣ�������Ӧ�ϴ����ز���
	 */
	public void scan() {
		scanDir(this.localDir);
		Set<CloudFile> localSet = null;//�����ļ��б���Ϣ
		Set<CloudFile> remoteSet = null;//����ļ��б���Ϣ
		File localFileInfo = new File(ConsoleControl.fileRoot + "\\" + "FileInfo");
		//File localFileInfo = new File("D:\\CloudFileTest\\FileInfo");
		Gson gson = new Gson();
		
		
		//���ط��������ļ���Ϣ�б�
		try{
			if(filecontrol.do_download(ConsoleControl.fileRoot + "\\" + "remoteFileInfo", "remoteFileInfo")){
		    	try{
		    		//�����������ļ��б���Ϣ��jsonת����Set
		    		File fremote = new File(ConsoleControl.fileRoot + "\\" + "remoteFileInfo");
					FileReader fr1 = new FileReader(fremote);
					remoteSet = gson.fromJson(fr1,new TypeToken<Set<CloudFile>>(){}.getType());//��jsonת����java����
					fr1.close();
					fremote.delete();
				}catch(IOException a){
					a.printStackTrace();
				}
		    }
			else{
				//���������ļ��б���Ϣ
				
				
			}
		}catch(Exception e){}
		
		File remoteFileInfo = new File(ConsoleControl.fileRoot + "\\" + "remoteFileInfo");
		try{
			if(remoteFileInfo.exists()){
				remoteFileInfo.delete();
			}
		}catch(Exception e){}
		
		
		//��Ȿ���ļ���Ϣ�б����
		if(!localFileInfo.canExecute()){//�����ļ���Ϣ�����ڣ��������µ��ļ�
			try{
				localFileInfo.delete();
				localFileInfo.createNewFile();
				String sets =  "attrib +H \"" + localFileInfo.getAbsolutePath() + "\""; 
				Runtime.getRuntime().exec(sets); 
			}catch(IOException e){
				e.printStackTrace();
			}finally{
				//scanDir(localDir);//ɨ���ļ���
			}
			fileSetCompare(remoteSet);
		}else{
			
			//�����ļ���Ϣ�б�������ȡFileInfo�ļ����localSet
			try{
				FileReader fr = new FileReader(localFileInfo);
				localSet = gson.fromJson(fr,new TypeToken<Set<CloudFile>>(){}.getType());//��jsonת����java����
				fr.close();
			}catch(IOException a){
				a.printStackTrace();
			}
			//�Ƚϱ����ļ���Ϣ��ɨ����ļ���Ϣ
			//�����½��ļ�����Ӱ��
			//�����޸��ļ������޸����ļ����Լ��ļ����ݣ��򵱱����ļ���ɾ�������½���һ���ļ�
			//����ɾ���ļ���Ҫ��������½��ļ�����
			for(Iterator<CloudFile> localIt = localSet.iterator();localIt.hasNext();){
				CloudFile localFile = localIt.next();
				boolean flag = false;
				for(Iterator<CloudFile> newIt = CloudFile.fileSet.iterator();newIt.hasNext();){
					CloudFile newFile = newIt.next();
					if(localFile.filename.equals(newFile.filename))
						flag = true;
				}
				if(flag == false){//��ʾlocaFile�Ѿ��ڱ��ر�ɾ��
					localFile.setFlag(true);//�����ļ���ʶΪ��ɾ��
					CloudFile.fileSet.add(localFile);
					
				}
			}
			
			fileSetCompare(remoteSet);
		}
		

		//���µ��ļ���Ϣת��Ϊjsonд���ļ�FileInfo	
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
		
		//���µ��ļ���Ϣ�ϴ�
		NetworkTask newFileInfo = new NetworkTask(localFileInfo, "remoteFileInfo");
		networkAgent.uploadQueue.add(newFileInfo);
		
		
		System.out.println("Monitor done.");
		System.out.println("Wait ...");
	}

	/**
	 * ɨ�豾���ļ��У��������ļ���Ϣ����CloudFile.fileSet��
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
	 * �����ļ��б�ͷ������ļ��б���жԱȣ�ȷ���������
	 * ���⣺����ɾ���ļ��ͷ������½��ļ������ؽ����
	 * ���⣺���ɾ�����ļ��ͱ������ϴ��ĳ�ͻ���
	 * @param remoteSet
	 */
	public  void fileSetCompare(Set<CloudFile> remoteSet){
		if(remoteSet == null){
			//�ϴ������ļ�
			for(Iterator<CloudFile> localIt = CloudFile.fileSet.iterator();localIt.hasNext();){
				CloudFile localNext = localIt.next();
				System.out.println("��0���ϴ��ļ���" + localNext.filename.getName());
				try{
					//localNext.setNetworkAgent(this.networkAgent);
					localNext.upload(this.networkAgent);
				}catch(Exception e){}
			}
		}
		else{
			//�Աȱ����ļ��б���µ��ļ��б��õ��ļ��䶯��Ϣ
			boolean comFlag;
			Set<CloudFile> localRemoveSet = new HashSet<CloudFile>(); 
			Set<CloudFile> remoteRemoveSet = new HashSet<CloudFile>();
			Set<CloudFile> localAddSet = new HashSet<CloudFile>();
			for(Iterator<CloudFile> remoteIt = remoteSet.iterator();remoteIt.hasNext();){//��������ļ���Ϣ�б���ҵ��뱾���б�ͬ�ı���
				CloudFile remoteNext = remoteIt.next();
				comFlag = false;//��Ǹ��ļ��Ƿ��ڱ��ر��ҵ�
				for(Iterator<CloudFile> localIt = CloudFile.fileSet.iterator();localIt.hasNext();){
					CloudFile localNext = localIt.next();
					if(remoteNext.filename.getName().equals(localNext.filename.getName())){
						comFlag = true;
						if(localNext.flag && (!remoteNext.flag)){
							//�����ļ��Ѿ�ɾ������ɾ����������Ӧ�ļ�
							System.out.println("ɾ���ļ��� " + remoteNext.filename.getName());
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
							//�������ļ���ɾ��
							if(localNext.lastModify > remoteNext.deleteTime){
								//�����޸�ʱ�������ɾ��ʱ����ϴ�(1)
								System.out.println("��1���ϴ��ļ���" + localNext.filename.getName());
								//NetworkTask uploadFile = new NetworkTask(localNext.filename, localNext.filename.getName());
								//networkAgent.uploadQueue.add(uploadFile);
								try{
									//localNext.setNetworkAgent(this.networkAgent);
									localNext.upload(this.networkAgent);
								}catch(Exception e){}
								
							}
							else{
								//ɾ�������ļ�
								File deleteLocalFile = new File(localNext.filename.getPath());
								deleteLocalFile.delete();
								//CloudFile.fileSet.remove(localNext);
								localRemoveSet.add(localNext);
							}
						}
						else if(remoteNext.lastModify < localNext.lastModify){
							//�����ļ����޸ģ���Ҫ�ϴ�(2)
							System.out.println("��2���ϴ��ļ���" + localNext.filename.getName());
							//NetworkTask uploadModifyFile = new NetworkTask(localNext.filename, localNext.filename.getName());
							//networkAgent.uploadQueue.add(uploadModifyFile);
							try{
								//localNext.setNetworkAgent(this.networkAgent);
								localNext.upload(this.networkAgent);
							}catch(Exception e){}
							
							
						}else if((remoteNext.lastModify > localNext.lastModify) && (!localNext.flag)){
							//�������ļ����޸ģ���Ҫ����
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
				if((!remoteNext.flag) && (!comFlag)){//�÷������ļ��ڱ���û���ҵ�����û��ɾ��������
					//NetworkTask downloadUnfindFile = new NetworkTask(remoteNext.filename, remoteNext.filename.getName());
					//networkAgent.downloadQueue.add(downloadUnfindFile);
					try{
						//remoteNext.setNetworkAgent(this.networkAgent);
						remoteNext.download(this.networkAgent);
					}catch(Exception e){}
					
					//CloudFile.fileSet.add(remoteNext);
					localAddSet.add(remoteNext);
					System.out.println("���أ� " + remoteNext.filename.getName());
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
			
			
			for(Iterator<CloudFile> localIt = CloudFile.fileSet.iterator();localIt.hasNext();){//���������ļ���Ϣ
				CloudFile localNext = localIt.next();
				boolean comFlag1 = false;
				for(Iterator<CloudFile> remoteIt = remoteSet.iterator();remoteIt.hasNext();){
					CloudFile remoteNext = remoteIt.next();
					if(remoteNext.filename.equals(localNext.filename)){
						comFlag1 = true;
					}
				}
				if(!comFlag1){//���ļ�����ز����ڣ��ϴ�(3)
					System.out.println("��3���ϴ��ļ���" + localNext.filename.getName());
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
