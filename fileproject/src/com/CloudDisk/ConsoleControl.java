package com.CloudDisk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;

import com.google.gson.Gson;
import com.kuaipan.client.exception.KuaipanAuthExpiredException;
import com.kuaipan.client.exception.KuaipanIOException;
import com.kuaipan.client.exception.KuaipanServerException;

public class ConsoleControl {
	private BufferedWriter stdout;
	private BufferedReader stdin;
	private File fileRootCfg = new File("fileRoot");
	public static FileControl filecontrol; 
	public static String fileRoot = null;
	
	public ConsoleControl(OutputStream stdout, InputStream stdin){
		try {
			this.stdout = new BufferedWriter(new OutputStreamWriter(stdout, System.getProperty("file.encoding")));
			this.stdin = new BufferedReader(new InputStreamReader(stdin, System.getProperty("file.encoding")));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		filecontrol = new FileControl(this.stdout, this.stdin);
		this.get_fileRoot();
		if(fileRoot == null)
			System.out.println("Please set local file!");
		else{
			System.out.println("The local file is:" + fileRoot);
			do_delete("remoteFileInfo");
		}
		this.do_help();
	}
	
	public boolean execute(){
		System.out.print(">");
		String[] args = readOneLine();
		if (args.length == 0)
			;
		else {
			String cmd = args[0];
			try{
				if(cmd.equals("login"))
					filecontrol.do_login();
				else if(cmd.equals("set") && args.length > 1)
					do_setFile(args[1]);
				else if(cmd.equals("sync"))
					do_sync();
				else if(cmd.equals("delete") && args.length > 1)
					do_delete(args[1]);
				else if(cmd.equals("add") && args.length > 1)
					do_addFile(args[1]);
				else if(cmd.equals("help"))
					do_help();
				else if(cmd.equals("ls"))
					do_ls();
				else if(cmd.equals("exit")){
					do_delete("remoteFileInfo");
					System.out.println("Thanks for using !!!");
					return false;
				}
			}catch(KuaipanIOException e) {
				e.printStackTrace();
			} catch (KuaipanServerException e) {
				e.printStackTrace();
			} catch (KuaipanAuthExpiredException e) {
				System.out.println("You ought to login first.");
			}
		}
		return true;
	}
	
	
	public void sync(){
		this.do_sync();
	}
	
	public void login(){
		try {
			this.filecontrol.do_login();
		} catch (KuaipanIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KuaipanServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KuaipanAuthExpiredException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private String[] readOneLine() {
		try {
			return stdin.readLine().split(" ");
		} catch (IOException e) {}
		return null;
	}
	
	private void do_sync(){
		//File localDir = new File("D:\\CloudFileTest");
		do_delete("remoteFileInfo");
		get_fileRoot();
		File localDir = new File(ConsoleControl.fileRoot);
		if(!localDir.isDirectory())
			localDir.mkdir();
		CloudFile.fileSet = new HashSet<CloudFile>();
		//初始化NetworkAgent
		NetworkAgent network = new NetworkAgent();

		// 开始执行filemonister
		FileMonitor filemonitor = new FileMonitor(localDir, filecontrol, network);
		Thread filemonitor_thread = new Thread(filemonitor);
		filemonitor_thread.start();
		try{
			Thread.sleep(10000);
		}catch(Exception e){}
		Thread network_thread = new Thread(network);
		network_thread.start();
		//System.out.println("同步完成");
		do_delete("remoteFileInfo");
	}
	
	private void do_delete(String fileName){
		File deleteFile = new File(ConsoleControl.fileRoot + "\\" + fileName);
		if(deleteFile.exists()){
			boolean deleteFlag = deleteFile.delete();
			System.out.println("");
			if(deleteFlag){
				System.out.println(fileName + " deleted");
			}else{
				System.out.println(fileName + " is not deleted");
			}
		}
		//do_delete("remoteFileInfo");
	}
	
	private void do_addFile(String filePath){
		File addFile = new File(filePath);
		String filename = addFile.getName();
		File newFile = new File(ConsoleControl.fileRoot + "\\" + filename);
		addFile.renameTo(newFile);
		System.out.println("");
		System.out.println(newFile.getName() + " has been added");
		do_delete("remoteFileInfo");
	}
	
	private void do_help(){
		System.out.println("");
		System.out.println("help : Show help information");
		System.out.println("login : login in Cloud File (You should login in first!!!)");
		System.out.println("set [file path] : set local file  eg. set D:\\\\CloudFileTest");
		System.out.println("ls : show all files");
		System.out.println("add [file path] : add file  eg. add D:\\\\filename.txt");
		System.out.println("delete [file name] : delete file  eg. delete filename.txt");
		System.out.println("sync : begin sync");
		System.out.println("exit : exit the program");
		do_delete("remoteFileInfo");
	}
	
	private void do_setFile(String filePath){
		ConsoleControl.fileRoot = filePath;
		if(!fileRootCfg.exists()){
			try {
				fileRootCfg.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			FileWriter write = new FileWriter(fileRootCfg);
			write.write(filePath);
			write.flush();
			write.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	
	private void do_ls(){
		File dir = new File(ConsoleControl.fileRoot);
		if (!dir.isAbsolute()){
			System.out.println("do not build");
		}
		else{
			File[] files = dir.listFiles();
			for(File f1: files){
				if(!f1.isHidden())
					System.out.println(f1.getName());
			}
		}
	}
	
	private void get_fileRoot(){
		//this.fileRootCfg = new File("fileRoot");
		String s = null;
		if(this.fileRootCfg.exists()){
			try{
				FileReader read = new FileReader(this.fileRootCfg);
				BufferedReader br = new BufferedReader(read);
				s = br.readLine();
				br.close();
				read.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException ea){
				ea.printStackTrace();
			}
			if(s != null){
				ConsoleControl.fileRoot = s;
			}
		}
	}
	
}
