package com.CloudDisk;

import java.io.File;
import java.util.Queue;
import java.util.LinkedList;


public class NetworkAgent implements Runnable {
	public  Queue<NetworkTask> uploadQueue;
	public  Queue<NetworkTask> downloadQueue;
	public  Queue<NetworkTask> deleteQueue;
	
	int maxConnection;
	
	public NetworkAgent(){
		this.uploadQueue = new LinkedList();
		this.downloadQueue = new LinkedList();
		this.deleteQueue = new LinkedList();
		
	}

	public boolean upload() {
		while(uploadQueue.peek() != null){
			NetworkTask uploadFile = uploadQueue.poll();
		   try{
			   ConsoleControl.filecontrol.do_upload(uploadFile.remoteFile, uploadFile.localFile.getPath());
			   //StartHere.filecontrol.do_upload(uploadFile.remoteFile, uploadFile.localFile.getPath());
			   System.out.println("Upload File: " + uploadFile.localFile.getName());
			   uploadFile.file.callback("upload");
		   }catch(Exception e){}
		
		}
		if(uploadQueue.isEmpty())
			return true;
		return false;
	}

	public boolean download() {
		while(downloadQueue.peek() != null){
			NetworkTask downloadFile = downloadQueue.poll();
			try{
				ConsoleControl.filecontrol.do_download(downloadFile.localFile.getPath(), downloadFile.localFile.getName());
				//StartHere.filecontrol.do_download(downloadFile.localFile.getPath(), downloadFile.localFile.getName());
				System.out.println("Download File: " + downloadFile.localFile.getName());
				downloadFile.file.callback("download");
			}catch(Exception e){}
		}
		if(uploadQueue.isEmpty())
			return true;
		return false;
	}
	
	public boolean delete() {
		while(deleteQueue.peek() != null){
			NetworkTask deleteFile = deleteQueue.poll();
			try{
				ConsoleControl.filecontrol.do_rm(deleteFile.localFile.getName());
				//StartHere.filecontrol.do_rm(deleteFile.localFile.getName());
				System.out.println("Delete RemoteFile: " + deleteFile.localFile.getName());
				deleteFile.file.callback("delete");
			}catch(Exception e){}
		}
		if(deleteQueue.isEmpty())
			return true;
		return false;
	}

	public void run() {
		System.out.println("Network Thread begain:");
		// 执行上面的两个队列
		//upload();
		delete();
		download();
		//delete();
		upload();
		System.out.println("Network Thread done. Please press ENTER to continue!");
	}
}

class NetworkTask {
	String taskType;// upload or download
	// if upload, localFile >> remoteFile@disk
	// else remoteFile@disk >> localFile
	File localFile;
	String remoteFile;
	CloudDisk disk;
	CloudFile file;// 用于callback()
	
	public NetworkTask(File localFile, String remoteFile){
		this.localFile = localFile;
		this.remoteFile = remoteFile;
	}
	
	public NetworkTask(File localFile, String remoteFile, CloudFile file){
		this.localFile = localFile;
		this.remoteFile = remoteFile;
		this.file = file;
	}
}