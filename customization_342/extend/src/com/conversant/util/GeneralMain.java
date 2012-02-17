package com.conversant.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.compiere.model.MUser;
import org.compiere.model.MUserRoles;
import org.compiere.util.Env;
import org.compiere.util.Ini;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import com.conversant.db.BillingConnector;
import com.conversant.db.RadiusConnector;
import com.conversant.model.BillingRecord;
import com.conversant.model.RadiusAccount;

public class GeneralMain
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		removeNumberWhitespace();
	}
	
	public static void removeNumberWhitespace() {
		
		try
		{
			CSVReader reader = new CSVReader(new FileReader("c:\\Formatted Business Partners - 110212 - v1.csv"));
			CSVWriter writer = new CSVWriter(new FileWriter("c:\\Formatted Business Partners - 110212 - v2.csv"));
			
			String[] nextLine = reader.readNext(); // skip header
		    while ((nextLine = reader.readNext()) != null) 
		    {
		    	if (nextLine.length != 11) {
		    		while (nextLine.length < 11) {
//		    			nextLine = (String[])ArrayUtils.add(nextLine, new String());
		    		}
		    	}
		    	
		    	nextLine[4] = nextLine[4].replaceAll("\\s", "");
		    	nextLine[6] = nextLine[6].replaceAll("\\s", "").replaceAll("-", "");
		    	nextLine[7] = nextLine[7].replaceAll("\\s", "").replaceAll("-", "");
		    	nextLine[8] = nextLine[8].replaceAll("\\s", "").replaceAll("-", "");
		    	nextLine[10] = "262";	
		    	
		    	writer.writeNext(nextLine);
		    }
		    
		    writer.close();
		}
		catch (Exception ex)
		{
			System.out.println(ex);
		}
		
	}
	
	public static void generateRadacctMatchSql()
	{
		Ini.setClient(false);
        org.compiere.Adempiere.startup(false);
        
		RadiusAccount radiusAccount = RadiusConnector.getRadiusAccount(162545318);
		try
		{
			BillingRecord billingRecord = BillingConnector.getBillingRecord(radiusAccount);
		}
		catch (Exception ex)
		{
			
		}
	}
	
	public static void checkMissingCvoxUsers()
	{
		HashMap<Integer, String> drupalUsers = new HashMap<Integer, String>();
		
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try
        {
			String sql = "SELECT us.c_bpartner_id, u.name FROM drupal_console.users u INNER JOIN drupal_console.user_sync_adempiere us ON u.uid=us.uid WHERE u.uid > 1 AND u.status = 1";
			
			String url = "jdbc:mysql://" + "localhost" + ":" + "3306" + "/";
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(url, "root", "p");

			ps = conn.prepareStatement(sql.toString());

			rs = ps.executeQuery();
			while (rs.next())
			{
				drupalUsers.put(rs.getInt(1), rs.getString(2));
			}
        }
		catch (ClassNotFoundException ex)
		{
			System.err.println(ex);
		}
        catch (SQLException ex)
        {
        	System.err.println(ex);
        }
        finally
        {
        	try
        	{
        		if (rs != null) rs.close();
        		if (ps != null) ps.close();
        		if (conn != null) conn.close();
        		
        		rs = null;
        		ps = null;
        		conn = null;
        	}
        	catch (SQLException ex)
        	{
        		System.err.println(ex);
        	}
        }
        
        for (Integer C_BPartner_ID : drupalUsers.keySet())
        {
        	String name = drupalUsers.get(C_BPartner_ID);
        	
        	
        }
	}
	
	public static void checkRoles()
	{
		ArrayList<Integer> drupalIds = new ArrayList<Integer>();
		
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try
        {
			String sql = "SELECT ad_user_id FROM user_sync_adempiere WHERE uid > 1";
			
			String url = "jdbc:mysql://" + "localhost" + ":" + "3306" + "/" + "drupal_console";
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(url, "root", "p");

			ps = conn.prepareStatement(sql.toString());

			rs = ps.executeQuery();
			while (rs.next())
			{
				drupalIds.add(rs.getInt(1));
			}
        }
		catch (ClassNotFoundException ex)
		{
			System.err.println(ex);
		}
        catch (SQLException ex)
        {
        	System.err.println(ex);
        }
        finally
        {
        	try
        	{
        		if (rs != null) rs.close();
        		if (ps != null) ps.close();
        		if (conn != null) conn.close();
        		
        		rs = null;
        		ps = null;
        		conn = null;
        	}
        	catch (SQLException ex)
        	{
        		System.err.println(ex);
        	}
        }
        
		Ini.setClient(false);
        org.compiere.Adempiere.startup(false);
        
        for (Integer id : drupalIds)
        {
        	// Check user exists
        	MUser user = new MUser(Env.getCtx(), id, null);
        	if (user.is_new() || user.getAD_User_ID() == 0)
        	{
        		System.out.println("User not found - " + id);
        		continue;
        	}
        	
        	// Check user has Account user role
        	boolean found = false;
        	MUserRoles[] roles = MUserRoles.getOfUser(Env.getCtx(), id);
    		for (MUserRoles role : roles)
    		{
    			if (role.getAD_Role_ID() == 1000017) // AD_Role - Account user (represents a drupal user)
    			{
    				found = true;
    			}
    		}
    		
    		if (!found)
//    			System.out.println("Role not found - " + id);
    			System.out.println("INSERT INTO AD_User_Roles VALUES (" + id + ", 1000017, 1000000, 1000001, 'Y', SYSDATE, 100, SYSDATE, 100);");
        }
	}
	
	public static void readActiveDrupalUsers()
	{
		try
		{
			CSVReader reader = new CSVReader(new FileReader("c:\\ActiveDrupalUsers.csv"));
		    String[] nextLine = reader.readNext(); // skip header
		    while ((nextLine = reader.readNext()) != null) 
		    {
		    	String uid = nextLine[0];	        
		    	System.out.print(uid + ",");
		    }
		}
		catch (Exception ex)
		{
			System.out.println(ex);
		}
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
