package com.CloudDisk;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class Test_FileInfo {
	public static void scanDir(File dir){
		File[] files = dir.listFiles();
		for(File f1: files){
			if(!f1.isHidden()){
				if(!f1.isDirectory())
					CloudFile.fileSet.add(new CloudFile(f1));
				else{
					scanDir(f1);
				}
			}
		}
	}
	
	public static void main(String[] args){
		CloudFile.fileSet = new HashSet<CloudFile>();
		Gson gson = new Gson();
		File localFile = new File("F:\\CloudTest");
		scanDir(localFile);
		String fileInf = gson.toJson(CloudFile.fileSet);
		try{
			File f1 = new File("F:\\CloudTest\\FileInfo");
			f1.delete();
			f1.createNewFile();
			String sets =  "attrib +H \"" + f1.getAbsolutePath() + "\""; 
			Runtime.getRuntime().exec(sets); 
			
			//FileWriter f = new FileWriter(localFileInfo);
			FileWriter f = new FileWriter(f1);
			f.write(fileInf);
			f.close();
		}catch(IOException ea){
			ea.printStackTrace();
		}
		
		
		
		
		//读出信息并打印
		System.out.println("打印信息");
		Gson gson1 = new Gson();
		CloudFile.fileSet = new HashSet<CloudFile>();
		try{
			FileReader f1 = new FileReader("F:\\CloudTest\\FileInfo");
			CloudFile.fileSet = gson1.fromJson(f1,new TypeToken<Set<CloudFile>>(){}.getType());
			f1.close();
		}catch(Exception e){}
		Iterator<CloudFile> itFileset = CloudFile.fileSet.iterator();
		while(itFileset.hasNext()){
			CloudFile fout = itFileset.next();
			System.out.println(fout + fout.getFileBlocks());
		}
	}

}
