package com.conversant.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class GeneralMain
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		File file = new File("E:\\users.csv");
		ArrayList<Integer> ids = getUserIds(file);
		for (Integer id : ids)
		{
			System.out.println("INSERT INTO AD_USER_ROLES (AD_USER_ID, AD_ROLE_ID, AD_CLIENT_ID, AD_ORG_ID, CREATEDBY, UPDATEDBY) VALUES (" + id + ", 1000016, 1000000, 1000001, 100, 100);");
			System.out.println("INSERT INTO AD_USER_ROLES (AD_USER_ID, AD_ROLE_ID, AD_CLIENT_ID, AD_ORG_ID, CREATEDBY, UPDATEDBY) VALUES (" + id + ", 1000017, 1000000, 1000001, 100, 100);");
		}
	}

	public static ArrayList<Integer> getUserIds(File aFile)
	{
		ArrayList<Integer> ids = new ArrayList<Integer>();

		try
		{
			BufferedReader input = new BufferedReader(new FileReader(aFile));
			try
			{
				String line = null; 
				
				while ((line = input.readLine()) != null)
				{
					ids.add(Integer.parseInt(line));
				}
			}
			finally
			{
				input.close();
			}
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}

		return ids;
	}
}
