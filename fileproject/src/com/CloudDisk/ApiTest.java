package com.CloudDisk;

public class ApiTest {
	public static void main(String agss[]){
		FileControl filecontrol1 = new FileControl(System.out, System.in);
		try{
			filecontrol1.do_login();
			filecontrol1.do_ls();
		}catch(Exception e){}
	}

}
