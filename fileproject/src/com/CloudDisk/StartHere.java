package com.CloudDisk;
import java.io.File;
import java.util.HashSet;

public class StartHere {
	public static FileControl filecontrol;
	public static void main(String[] args) {
		// ���ֳ�ʼ��
		//�趨·��
		File localDir = new File("D:\\CloudFileTest");
		//��ʼ��fileset
		CloudFile.fileSet = new HashSet<CloudFile>();
		//��ʼ��NetworkAgent
		NetworkAgent network = new NetworkAgent();
		
		//��½
		filecontrol = new FileControl(System.out, System.in);
		try{
			filecontrol.do_login();
		}catch(Exception e){}

		// ��ʼִ��filemonister
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
