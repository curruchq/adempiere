package com.conversant.db;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;

import org.compiere.util.CLogger;
import org.compiere.util.Env;

public class AsteriskConnector extends MySQLConnector 
{
	/** Logger			 */
	private static CLogger log = CLogger.getCLogger(AsteriskConnector.class);
	
	/** Schema 			 */
	private static final String SCHEMA = "asterisk";
	
	/** Default VM Server */
	private static final String DEFAULT_VM_SERVER = "c-vm-02.conversant.co.nz";
	
	public static Connection getConnection()
	{
		return getConnection(Env.getCtx(), SCHEMA);
	}
	
	public static boolean addRTExtension(String context, String exten, String priority, String app, String appdata)
	{
		String table = "rt_extensions";
		String[] columns = new String[]{"context", "exten", "priority", "app", "appdata"};
		Object[] values = new Object[]{context, exten, priority, app, appdata};
		
		return insert(getConnection(), table, columns, values);
	}
	
	public static boolean removeRTExtension(String context, String exten, String priority, String app, String appdata)
	{
		String table = "rt_extensions";
		String whereClause = "context=? AND exten=? AND priority=? AND app=? AND appdata=?";
		String[] whereValues = new String[]{context, exten, priority, app, appdata};
		
		return delete(getConnection(), table, whereClause, whereValues);
	}
	
	public static boolean addVoicemailUser(String bpId, String bpSearchKey, String number, String fullname, String email)
	{
		String context = bpSearchKey;
		String mailbox = number;
		String password = number;
		String pager = "";
		
		String table = "voicemail_users";
		String[] columns = new String[]{"uuid", "context", "mailbox", "password", "fullname", "email", "pager"};
		Object[] values = new Object[]{bpId, context, mailbox, password, fullname, email, pager};
		
		return insert(getConnection(), table, columns, values);
	}
	
	public static boolean removeVoicemailUser(String bpId, String number)
	{
		String uuid = bpId;
		String mailbox = number;
		
		String table = "voicemail_users";
		String whereClause = "uuid=? AND mailbox=?";
		String[] whereValues = new String[]{uuid, mailbox};
		
		return delete(getConnection(), table, whereClause, whereValues);
	}

	public static boolean removeVoicemailUser(String bpId, String bpSearchKey, String number, String fullname, String email)
	{
		String context = bpSearchKey;
		String mailbox = number;
		String password = number;
		String pager = "";
		
		String table = "voicemail_users";
		String whereClause = "uuid=? AND context=? AND mailbox=? AND password=? AND fullname=? AND email=? AND pager=?";
		String[] whereValues = new String[]{bpId, context, mailbox, password, fullname, email, pager};
		
		return delete(getConnection(), table, whereClause, whereValues);
	}

	public static boolean addVoicemailToDialPlan(String number, String bpSearchKey)
	{
		String context = "proxy_default";
		String numberBUSY = number + "BUSY";
		String appSIP = "SIP/" + number;
		String macro = "Macro";
		String hangup = "Hangup";
		String wildcard = "%value%";
		String macroAppData = "proxy-vm|" + number + "|" + wildcard + "|" + bpSearchKey;
		
		boolean retValue = addRTExtension(context, number, "-1", appSIP, "");
		if (retValue)
		{
			retValue = addRTExtension(context, number, "1", macro, macroAppData.replace(wildcard, number));
			if (retValue)
			{
				retValue = addRTExtension(context, number, "2", hangup, "");
				if (retValue)
				{
					retValue = addRTExtension(context, numberBUSY, "-1", appSIP, "");
					if (retValue)
					{
						retValue = addRTExtension(context, numberBUSY, "1", macro, macroAppData.replace(wildcard, "BUSY"));
						if (retValue)
						{
							retValue = addRTExtension(context, numberBUSY, "2", hangup, "");
							if (!retValue)
							{
								removeRTExtension(context, number, "-1", appSIP, "");
								removeRTExtension(context, number, "1", macro, macroAppData.replace(wildcard, number));
								removeRTExtension(context, number, "2", hangup, "");
								removeRTExtension(context, numberBUSY, "-1", appSIP, "");
								removeRTExtension(context, numberBUSY, "1", macro, macroAppData.replace(wildcard, "BUSY"));
							}
						}
						else
						{
							removeRTExtension(context, number, "-1", appSIP, "");
							removeRTExtension(context, number, "1", macro, macroAppData.replace(wildcard, number));
							removeRTExtension(context, number, "2", hangup, "");
							removeRTExtension(context, numberBUSY, "-1", appSIP, "");
						}
					}
					else
					{
						removeRTExtension(context, number, "-1", appSIP, "");
						removeRTExtension(context, number, "1", macro, macroAppData.replace(wildcard, number));
						removeRTExtension(context, number, "2", hangup, "");
					}
				}
				else
				{
					removeRTExtension(context, number, "-1", appSIP, "");
					removeRTExtension(context, number, "1", macro, macroAppData.replace(wildcard, number));
				}
			}
			else
				removeRTExtension(context, number, "-1", appSIP, "");
		}
		
		return retValue;
	}
	
	public static boolean removeVoicemailFromDialPlan(String number, String bpSearchKey)
	{
		String context = "proxy_default";
		String numberBUSY = number + "BUSY";
		String appSIP = "SIP/" + number;
		String macro = "Macro";
		String hangup = "Hangup";
		String wildcard = "%value%";
		String macroAppData = "proxy-vm|" + number + "|" + wildcard + "|" + bpSearchKey;
		
		boolean success = true;
		
		// Try remove each extension even if previous failed
		if (!removeRTExtension(context, number, "-1", appSIP, ""))
			success = false;		
		if (!removeRTExtension(context, number, "1", macro, macroAppData.replace(wildcard, number)))
			success = false;
		if (!removeRTExtension(context, number, "2", hangup, ""))
			success = false;
		if (!removeRTExtension(context, numberBUSY, "-1", appSIP, ""))
			success = false;
		if (!removeRTExtension(context, numberBUSY, "1", macro, macroAppData.replace(wildcard, "BUSY")))
			success = false;
		if (!removeRTExtension(context, numberBUSY, "2", hangup, ""))
			success = false;
		
		return success;
	}
	
	public static ArrayList<Object[]> getAvp(String attribute, String value)
	{
		String table = "avp";
		String[] columns = new String[]{"*"};
		
		StringBuilder whereClause = new StringBuilder();		
		whereClause.append("attribute LIKE ? AND value LIKE ? AND date_end >= ?");
		
		Object[] whereValues = new Object[]{attribute, value, new Timestamp(System.currentTimeMillis())};					
		
		return select(getConnection(), table, columns, whereClause.toString(), whereValues);
	}
	
	public static ArrayList<Object[]> getAvpCSBContext()
	{
		String table = "avp";
		String[] columns = new String[]{"*"};
		
		StringBuilder whereClause = new StringBuilder();		
		whereClause.append("attribute LIKE ? AND date_end >= ?");
		
		Object[] whereValues = new Object[]{"DEVICE/%/csbcontext", new Timestamp(System.currentTimeMillis())};					
		
		return select(getConnection(), table, columns, whereClause.toString(), whereValues);
	}
	
	public static String getAvpDefaultServer(String bpSearchKey)
	{
		String table = "avp";
		String[] columns = new String[]{"value"};
		
		StringBuilder whereClause = new StringBuilder();		
		whereClause.append("attribute LIKE ? AND date_end >= ?");
		
		Object[] whereValues = new Object[]{"AMPUSER/" + bpSearchKey + "/DefaultServer", new Timestamp(System.currentTimeMillis())};					
		
		ArrayList<Object[]> result = select(getConnection(), table, columns, whereClause.toString(), whereValues);
		if (result.size() > 0)
			return (String)result.get(0)[0];
		
		return DEFAULT_VM_SERVER;
	}
	
	public static boolean addAvpCSBContext(String number, String bpSearchKey)
	{
		String attribute = "DEVICE/" + number + "/csbcontext";
		return addAvp(attribute, bpSearchKey);
	}
	
	public static boolean addAvp(String attribute, String value)
	{
		String table = "avp";
		String[] columns = new String[]{"attribute", "value", "date_start"};
		Object[] values = new Object[]{attribute, value, new Timestamp(System.currentTimeMillis())};
		
		return insert(getConnection(), table, columns, values);
	}
	
	public static boolean endDateAvp(String bpSearchKey, String number, Timestamp endDate)
	{
		String attribute = "DEVICE/" + number + "/csbcontext";
		Timestamp now = new Timestamp(System.currentTimeMillis());
		
		// Set parameters	
		String table = "avp";
		String[] columnsToUpdate = new String[]{"modified", "date_end"};
		Object[] valuesToUpdate = new Object[]{now, endDate};		
		
		String[] whereColumns = new String[]{"attribute", "value", "date_start", "date_end"};
		Object[] whereValues = new Object[]{attribute, bpSearchKey, now, now};
		String[] whereOps = new String[]{"=", "=", "<=", ">="};
		
		if (!update(getConnection(), table, columnsToUpdate, valuesToUpdate, whereColumns, whereValues, whereOps))
		{
			log.severe("Failed to update date_end[" + endDate + "] and modified[" + now + "] where attribute[" + attribute + "]");
			return false;
		}
		
		return true;
	}
	
	public static boolean addAmpUser(String username, String password, String deptname)
	{
		String table = "ampusers";
		String[] columns = new String[]{"username", "password", "extension_low", "extension_high", "deptname", "sections"};
		Object[] values = new Object[]{username, password, 0, 0, deptname, ""};
		
		return insert(getConnection(), table, columns, values);
	}
	
	public static boolean updateAmpUserPassword(String username, String password, String deptname)
	{
		String table = "ampusers";
		
		String[] columnsToUpdate = new String[]{"password"};
		Object[] valuesToUpdate = new Object[]{password};		
		
		String[] whereColumns = new String[]{"username", "deptname"};
		Object[] whereValues = new Object[]{username, deptname};
		String[] whereOps = new String[]{"=", "="};
		
		if (!update(getConnection(), table, columnsToUpdate, valuesToUpdate, whereColumns, whereValues, whereOps))
		{
			log.severe("Failed to update password for username[" + username + "] deptname[" + deptname + "]");
			return false;
		}
		
		return true;
	}
}
