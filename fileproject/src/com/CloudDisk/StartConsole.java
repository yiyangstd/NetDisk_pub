package com.CloudDisk;

public class StartConsole {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ConsoleControl consoleCtrl = new ConsoleControl(System.out, System.in);
		
		while (consoleCtrl.execute()) 
			;

	}

}
