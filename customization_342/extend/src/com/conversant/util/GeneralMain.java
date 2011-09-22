package com.conversant.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import au.com.bytecode.opencsv.CSVReader;

public class GeneralMain
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
	}
	
	public static void readDuplicateAvp()
	{
		try
		{
			CSVReader reader = new CSVReader(new FileReader("c:\\duplicate_avp.csv"));
		    String[] nextLine = reader.readNext(); // skip header
		    while ((nextLine = reader.readNext()) != null) 
		    {
		    	String attribute = nextLine[0];
		    	if (!attribute.startsWith("DEVICE/"))
		    		continue;
		    	
		    	attribute = attribute.replace("DEVICE/", "");
		    	attribute = attribute.replace("/csbcontext", "");		        
		    	System.out.println(attribute);
		    }
		}
		catch (Exception ex)
		{
			System.out.println(ex);
		}
	}
	
	public static void readBillingIds()
	{
		try
		{
			CSVReader reader = new CSVReader(new FileReader("c:\\ids.txt"));
		    String[] nextLine;
		    while ((nextLine = reader.readNext()) != null) 
		    {
		    	int billingId = Integer.parseInt(nextLine[0]);
		        
		    	System.out.println("UPDATE MOD_BILLING_RECORD SET SYNCRONISED='Y' WHERE MOD_BILLING_RECORD_ID=" + billingId + ";");
		    }
		}
		catch (Exception ex)
		{
			System.out.println(ex);
		}
	}
	
	public static void readUserRoles()
	{
		try
		{
			CSVReader reader = new CSVReader(new FileReader("E:\\BusinessPartnersUsers-Completed.csv"));
		    String[] nextLine = reader.readNext(); // skip headers
		    while ((nextLine = reader.readNext()) != null) 
		    {
		    	int userId = Integer.parseInt(nextLine[3]);
		    	String roles = nextLine[6];
		        
		    	boolean isTechnical = false;
		    	if (roles.contains("T") || roles.contains("t"))
		    	{
		    		isTechnical = true;
		    	}
		    	
		    	boolean isBilling = false;
		    	if (roles.contains("B") || roles.contains("b"))
		    	{
		    		isBilling = true;
		    	}
		    	
		    	if (isTechnical)
		    	{
		    		System.out.println("INSERT INTO AD_USER_ROLES (AD_USER_ID, AD_ROLE_ID, AD_CLIENT_ID, AD_ORG_ID, CREATEDBY, UPDATEDBY) VALUES (" + userId + ", " + 1000018 + ", 1000000, 1000001, 100, 100);");
		    	}
		    	
		    	if (isBilling)
		    	{
		    		System.out.println("INSERT INTO AD_USER_ROLES (AD_USER_ID, AD_ROLE_ID, AD_CLIENT_ID, AD_ORG_ID, CREATEDBY, UPDATEDBY) VALUES (" + userId + ", " + 1000019 + ", 1000000, 1000001, 100, 100);");
		    	}
		    }
		}
		catch (Exception ex)
		{
			System.out.println(ex);
		}
	}
	
	public static void readBPIsProspect()
	{
		try
		{
			CSVReader reader = new CSVReader(new FileReader("E:\\BusinessPartnersIsProspectValues-Completed.csv"));
		    String[] nextLine = reader.readNext(); // skip headers
		    while ((nextLine = reader.readNext()) != null) 
		    {
		    	int businessPartnerId = Integer.parseInt(nextLine[0]);
		    	String currentIsProspect = nextLine[3];
		    	String newIsProspect = nextLine[4];
		        
		    	if (!currentIsProspect.equalsIgnoreCase(newIsProspect))
		    		System.out.println("UPDATE C_BPARTNER SET ISPROSPECT='" + newIsProspect + "' WHERE C_BPARTNER_ID=" + businessPartnerId + ";");
		    }
		}
		catch (Exception ex)
		{
			System.out.println(ex);
		}
	}
	
	public static void parseUsers()
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
