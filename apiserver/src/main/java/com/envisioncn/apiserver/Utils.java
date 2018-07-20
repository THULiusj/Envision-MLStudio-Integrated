package com.envisioncn.apiserver;

import org.springframework.beans.factory.annotation.Value;

public class Utils {

	
	public static void main(String args[]) {
		String fileName ="/home/enuser/data/huajun.xu/1527734371097_37e41618-aa16-4f66-a483-293a82a5c190.csv";
		//System.out.println("hello"+System.currentTimeMillis()+":"+ expired(fileName));
		
		
	}
	
	// Check if the data file is expired
	// expiration in minutes
	public static boolean expired(String fileName, long expiration) {
		if(fileName == null)
			return true;
		
		//Get the current time value
		long current = System.currentTimeMillis();
		
		//Get the creation time of the file
		//Get the real filename
		String[] array1 = fileName.split("/");
		fileName = array1[array1.length-1].trim();
		//Get the milli-second value of the filename
		String[] array2 = fileName.split("_");
		long old = Long.parseLong(array2[0].trim());
		
		long diff = current - old;
		diff = diff/(60000);
		System.out.println("diff:"+diff+" expiration:"+expiration);
		
		if(diff>expiration)
			return true;
		else
			return false;
	}
	
	

}
