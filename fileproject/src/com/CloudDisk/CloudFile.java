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
	boolean flag = false;//ɾ����ʶ
	long deleteTime;
	int m = 3;// ����������Ĭ��ֵ
	int n = 5;// 3��5��ζ�Ŷ�ÿ���ļ������5�顢��������3����������Ļָ���ԭ�����ļ�
	int blockSize = 2048;
	CloudDisk[] location;
	boolean[] consistency;// һ���ԡ�����uploadʱ�е�clouddisk���ܻ�offline����ɸ����ϵĿ��������̵Ĳ�һ��
	String hash;// �ļ��Ĺ�ϣֵ
	String[] blockHash;// �ļ����������ֿ�Ĺ�ϣֵ
	//NetworkAgent networkAgent;

	public CloudFile(File filename) {
		this.filename = filename;
		this.lastModify = this.filename.lastModified();
		this.location = new CloudDisk[n];
		this.consistency = new boolean [n];
		this.blockHash = new String[n];
		//this.networkAgent = new NetworkAgent();
		//��ʼ��fileBlocks
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
		// ͨ����������������õ�i���ļ����location
		this.location[i] = location;
	}

	public void autoSetLocation(CloudDisk location) {
		// ͨ��������������Զ������ļ����location
		// ��Ҫ�ο����̵�QOS����
	}

	public boolean upload(NetworkAgent Agent) throws FileNotFoundException, IOException{
		// ���ȶԸ��ļ������n���飬Ȼ��ֱ��ϴ���location�ĸ��������С�
		// ע�⣬��Ҫ������ֱ���ϴ�����Ȼ�����Թ���
		// ���һ��NetAgent����������ȫ���е��ϴ������ع���
		// ������ֻ�ǲ���agent���ϴ����񼴿�
		// ���agent����̫�࣬�����ϴ�����ʧ�ܣ�����false
		this.blockNum = 0;
		NetworkAgent networkAgent = Agent;
		FileCoder upFile = new FileCoder(8, this.m, this.n, this.blockSize);
		//upFile.iniEncodeMatrix();
		this.fileBlocks = upFile.Encode(filename);
		System.out.print(filename + "been encoded into ");
		for (int i = 0; i < fileBlocks.length; i++)
			System.out.print(fileBlocks[i].getName() + " ");
		
		//���ļ�������ϴ�����
		
		for(File upBlock: fileBlocks){
			NetworkTask upBlockTask = new NetworkTask(upBlock, upBlock.getName(), this);
			networkAgent.uploadQueue.add(upBlockTask);
		}
		
		
		
		return true;
	}

	public boolean download(NetworkAgent Agent) throws FileNotFoundException, IOException{
		// ���ز�����
		// ע�⣬��Ҫ������ֱ�����أ���Ȼ�����Թ���
		// ���һ��NetAgent����������ȫ���е��ϴ������ع���
		// ������ֻ�ǲ���agent���������񼴿�
		// ���agent����̫�࣬������������ʧ�ܣ�����false
		// �����첽���ã�NetAgent������ɺ��ѱ��̣߳���ɽ���
		//NetworkTask downloadFile = new NetworkTask();
		//downloadFile.localFile = this.filename;
		this.blockNum = 0;
		NetworkAgent networkAgent = Agent;
		for(File downBlock: this.fileBlocks){
			NetworkTask downBlockTask = new NetworkTask(downBlock, downBlock.getName(), this);
			networkAgent.downloadQueue.add(downBlockTask);
			
		}
		
		//��callback���������
		
		//����
		
		
		
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
		// �������location�ķֿ��Ƿ���ȷ
		for(checkFile){
			if(location[i].available() != true)
				return false;
		}
		return true;
	}*/

	public boolean availableCheck() {
		// ����Ƿ����㹻��location�ϵķֿ�
		// �������е�n���ֿ鶼��ȷ���ã�fullCheck����true
		// ֻҪ��m���ֿ���ȷ���ã�availableCheck����true
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
		// ���Ե�i���Ƿ����;
		if(location[i].available() != true)
			return false;
		return true;
	}

	public boolean consistencyMaintain() {
		// ������һ���ԣ�
		return true;
	}

	public void callback(String control) {
		// ����NetworkAgent���з��ص���
		// ������Լ����
		if(control.equals("upload")){
			//�����ļ���
			this.blockNum ++;
			if(this.blockNum.equals(this.fileBlocks.length)){
				for(File f: this.fileBlocks){
					System.out.println(f.getName() + " " + f.delete());
					//f.deleteOnExit();
				}
				System.out.println("�ϴ��ļ����");
				this.blockNum = 0;
			}
		}
		else if(control.equals("delete")){
			
		}
		else if(control.equals("download")){
			this.blockNum ++;
			if(this.blockNum.equals(this.fileBlocks.length)){
				//����������еĿ�
				//����
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
