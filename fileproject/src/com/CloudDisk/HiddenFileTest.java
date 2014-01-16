package com.CloudDisk;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException; 

public class HiddenFileTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		
		File f3 = new File("D:\\HidenFileTest.txt");
		System.out.println(f3.isHidden());
		/*FileWriter fwriter = new FileWriter("D:\\HidenFileTest.txt");
		fwriter.write("This is a test");
		fwriter.flush();*/
		BufferedReader br = new BufferedReader(new FileReader("D:\\HidenFileTest.txt"));
		String s1 = br.readLine();
		System.out.println(s1);
		br.close();
		

	}
	public void setFile()throws IOException{
		File f1 = new File("D:\\HidenFileTest.txt");
		f1.delete();
		f1.createNewFile();
		String sets =  "attrib +H \"" + f1.getAbsolutePath() + "\""; 
		Runtime.getRuntime().exec(sets); 
		
		
		
	}

}
