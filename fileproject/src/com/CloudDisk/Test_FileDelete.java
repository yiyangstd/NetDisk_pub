package com.CloudDisk;

import java.io.*;

public class Test_FileDelete implements Runnable{

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		/*try {
			input.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		Test_FileDelete test = new Test_FileDelete();
		Thread t1 = new Thread(test);
		t1.start();
		File deleFile = new File("F:\\cs.txt");
		System.out.println(deleFile.delete());
		
		//System.out.println(deleFile2.delete());

	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		File deleFile = new File("F:\\cs.txt");
		FileInputStream input = null;
		try {
			input = new FileInputStream(deleFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			System.out.println(input.read());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
