package com.conversant.db;

import java.sql.Connection;

import org.compiere.util.CLogger;
import org.compiere.util.Env;

public class AsteriskConnector extends MySQLConnector 
{
	/** Logger			 */
	private static CLogger log = CLogger.getCLogger(AsteriskConnector.class);
	
	/** Schema 			 */
	private static final String SCHEMA = "asterisk";
	
	private static Connection getConnection()
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
	
	public static boolean addAvp(String number, String bpSearchKey)
	{
		String attribute = "DEVICE/" + number + "/csbcontext";
		
		String table = "avp";
		String[] columns = new String[]{"attribute", "value"};
		Object[] values = new Object[]{attribute, bpSearchKey};
		
		return insert(getConnection(), table, columns, values);
	}
}
