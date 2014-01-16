package com.CloudDisk;
import java.io.File;
import java.util.HashSet;

public class StartHere {
	public static FileControl filecontrol;
	public static void main(String[] args) {
		// 各种初始化
		//设定路径
		File localDir = new File("D:\\CloudFileTest");
		//初始化fileset
		CloudFile.fileSet = new HashSet<CloudFile>();
		//初始化NetworkAgent
		NetworkAgent network = new NetworkAgent();
		
		//登陆
		filecontrol = new FileControl(System.out, System.in);
		try{
			filecontrol.do_login();
		}catch(Exception e){}

		// 开始执行filemonister
		FileMonitor filemonitor = new FileMonitor(localDir, filecontrol, network);
		Thread filemonitor_thread = new Thread(filemonitor);
		filemonitor_thread.start();
		try{
			Thread.sleep(10000);
		}catch(Exception e){}
		Thread network_thread = new Thread(network);
		network_thread.start();
		
		//
		
	}
}
