package com.CloudDisk;

import java.io.File;

public class CloudFileGetTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		File f = new File("E:\\CloudFileTest\\1.doc");
		CloudFile f1 = new CloudFile(f);
		System.out.println(f1.getFileBlocks());

	}

}
