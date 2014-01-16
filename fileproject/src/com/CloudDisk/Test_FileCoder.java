package com.CloudDisk;

import java.io.File;

public class Test_FileCoder {

	/**
	 * @param args
	 */
	public static void main(String[] args)throws Exception {
		// TODO Auto-generated method stub
		File f1 = new File("D:\\±‡¬Î≤‚ ‘.txt");
		CloudFile cf1 = new CloudFile(f1);
		int m = 3;
		int n = 5;
		int blockSize = 2048;
		//º”√‹
		FileCoder fileCoder = new FileCoder(8, m, n, blockSize);
		File[] out = fileCoder.Encode(f1);
		System.out.print(f1.getName() + " been encoded into ");
		for (int i = 0; i < out.length; i++)
			System.out.print(out[i].getName() + " ");
		System.out.println("");
		
		for(File f: out){
			System.out.println(f.getName() + " " + f.delete());
		}
		//Ω‚√‹
		/*
		FileCoder fileCoder2 = new FileCoder(8, m, n, blockSize);
		fileCoder2.iniEncodeMatrix();
		File outFile = fileCoder2.Decode(out);
		System.out.print(outFile.getName() + " has been decoded! ");
		*/

	}

}
