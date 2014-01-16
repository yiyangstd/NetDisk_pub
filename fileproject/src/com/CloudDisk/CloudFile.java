package com.CloudDisk;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Set;

public class CloudFile implements Cloneable {
	static Set<CloudFile> fileSet;

	Integer blockNum = 0;
	File filename;
	File[] fileBlocks;
	long lastModify;
	boolean flag = false;//删除标识
	long deleteTime;
	int m = 3;// 基本参数的默认值
	int n = 5;// 3、5意味着对每个文件编码成5块、其中任意3块可以完整的恢复出原来的文件
	int blockSize = 2048;
	CloudDisk[] location;
	boolean[] consistency;// 一致性。由于upload时有的clouddisk可能会offline，造成该盘上的块与其他盘的不一致
	String hash;// 文件的哈希值
	String[] blockHash;// 文件各个编码后分块的哈希值
	//NetworkAgent networkAgent;

	public CloudFile(File filename) {
		this.filename = filename;
		this.lastModify = this.filename.lastModified();
		this.location = new CloudDisk[n];
		this.consistency = new boolean [n];
		this.blockHash = new String[n];
		//this.networkAgent = new NetworkAgent();
		//初始化fileBlocks
		this.fileBlocks = new File[n];
		for(int i = 0; i < n; i ++){
			String file = this.filename.getName() + "." + i;
			//System.out.println(file);
			this.fileBlocks[i] = new File(file);
		}
	}

	public CloudFile(File filename, int m, int n) {
		this.filename = filename;
		this.m = m;
		this.n = n;
		location = new CloudDisk[n];
		consistency = new boolean [n];
		blockHash = new String[n];
	}
	
	public String getFileBlocks(){
		String fileBlockSt = "";
		for(File f: this.fileBlocks){
			fileBlockSt += f.toString() + " ";
		}
		return fileBlockSt;
	}

	

	public void setFlag(boolean flag){
		this.flag = flag;
	}
	
	public void setDeleteTime(){
		
	}
	
	//public void setNetworkAgent(NetworkAgent networkAgent){
		//this.networkAgent = networkAgent;
	//}
	
	public void setLocation(CloudDisk location, int i) {
		// 通过这个方法可以设置第i个文件块的location
		this.location[i] = location;
	}

	public void autoSetLocation(CloudDisk location) {
		// 通过这个方法可以自动设置文件块的location
		// 主要参考云盘的QOS属性
	}

	public boolean upload(NetworkAgent Agent) throws FileNotFoundException, IOException{
		// 首先对该文件编码成n个块，然后分别上传到location的各个云盘中。
		// 注意，不要在这里直接上传，不然很难以管理
		// 设计一个NetAgent，在里面完全所有的上传和下载工作
		// 在这里只是产生agent的上传任务即可
		// 如果agent任务太多，则建立上传任务失败，返回false
		this.blockNum = 0;
		NetworkAgent networkAgent = Agent;
		FileCoder upFile = new FileCoder(8, this.m, this.n, this.blockSize);
		//upFile.iniEncodeMatrix();
		this.fileBlocks = upFile.Encode(filename);
		System.out.print(filename + "been encoded into ");
		for (int i = 0; i < fileBlocks.length; i++)
			System.out.print(fileBlocks[i].getName() + " ");
		
		//将文件块加入上传队列
		
		for(File upBlock: fileBlocks){
			NetworkTask upBlockTask = new NetworkTask(upBlock, upBlock.getName(), this);
			networkAgent.uploadQueue.add(upBlockTask);
		}
		
		
		
		return true;
	}

	public boolean download(NetworkAgent Agent) throws FileNotFoundException, IOException{
		// 下载并解码
		// 注意，不要在这里直接下载，不然很难以管理
		// 设计一个NetAgent，在里面完全所有的上传和下载工作
		// 在这里只是产生agent的下载任务即可
		// 如果agent任务太多，则建立下载任务失败，返回false
		// 采用异步调用，NetAgent下载完成后唤醒本线程，完成解码
		//NetworkTask downloadFile = new NetworkTask();
		//downloadFile.localFile = this.filename;
		this.blockNum = 0;
		NetworkAgent networkAgent = Agent;
		for(File downBlock: this.fileBlocks){
			NetworkTask downBlockTask = new NetworkTask(downBlock, downBlock.getName(), this);
			networkAgent.downloadQueue.add(downBlockTask);
			
		}
		
		//等callback，下载完成
		
		//解码
		
		
		
		return true;
	}
	
	public boolean delete(NetworkAgent Agent){
		NetworkAgent networkAgent = Agent;
		for(File deleteFile: this.fileBlocks){
			NetworkTask deleteRemoteFile = new NetworkTask(deleteFile, deleteFile.getName());
			networkAgent.deleteQueue.add(deleteRemoteFile);
		}
		
		return true;
	}

	/*public boolean fullCheck() {
		// 检测所有location的分块是否正确
		for(checkFile){
			if(location[i].available() != true)
				return false;
		}
		return true;
	}*/

	public boolean availableCheck() {
		// 检测是否有足够的location上的分块
		// 区别：所有的n个分块都正确可用，fullCheck返回true
		// 只要有m个分块正确可用，availableCheck返回true
		int sum = 0;
		for (int i = 0;i < this.n;i ++){
			if(location[i].available() == true){
				sum++;
			}
		}
		if (sum < this.m)
			return false;
		return true;
	}

	public boolean availableCheck(int i) {
		// 测试第i块是否可用;
		if(location[i].available() != true)
			return false;
		return true;
	}

	public boolean consistencyMaintain() {
		// 消除不一致性；
		return true;
	}

	public void callback(String control) {
		// 用于NetworkAgent进行返回调用
		// 具体的自己设计
		if(control.equals("upload")){
			//清理文件块
			this.blockNum ++;
			if(this.blockNum.equals(this.fileBlocks.length)){
				for(File f: this.fileBlocks){
					System.out.println(f.getName() + " " + f.delete());
					//f.deleteOnExit();
				}
				System.out.println("上传文件完成");
				this.blockNum = 0;
			}
		}
		else if(control.equals("delete")){
			
		}
		else if(control.equals("download")){
			this.blockNum ++;
			if(this.blockNum.equals(this.fileBlocks.length)){
				//下载完成所有的块
				//解码
				this.blockNum = 0;
				FileCoder fileCoder = new FileCoder(8, this.m, this.n, this.blockSize);
				fileCoder.iniEncodeMatrix();
				
				try{
					File downFile = fileCoder.Decode(this.fileBlocks);
					File newFileLocation = new File(ConsoleControl.fileRoot + "\\" + this.filename.getName());
					downFile.renameTo(newFileLocation);
				}catch(Exception e){}
				for(File f: this.fileBlocks){
					System.out.println(f.getName() + f.delete());
				}
				System.out.println("");
				System.out.println(this.filename.getName() + " has been Decoded.");
			}
		}
		
	}
	
	public Object clone(){
		CloudFile CFclone = null;
		try{
			CFclone = (CloudFile)super.clone();
		}catch(CloneNotSupportedException e){
			e.printStackTrace();
		}
		return CFclone;
	}
	
	@Override
	public String toString(){
		return (this.filename.toString() + " " );
	}

}
