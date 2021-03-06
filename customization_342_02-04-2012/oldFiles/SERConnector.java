package org.compiere.wstore;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;

import org.compiere.util.CLogger;

import com.conversant.db.MySQLConnector;
import com.conversant.model.SIPAccount;

public class SERConnector extends MySQLConnector
{
	private static CLogger log = CLogger.getCLogger(SERConnector.class);

	private static final String SCHEMA = "ser";
	private static final String	USERNAME = "erp_local";
	private static final String	PASSWORD = "naFJ487CB(Xp";
	
	/*****************************************************************************************/
	
	private static Connection getDefaultConnection()
	{
		return getConnection(DEFAULT_HOST, DEFAULT_PORT, SCHEMA, USERNAME, PASSWORD);
	}

	private static String getMD5(String value)
	{
		MessageDigest algorithm = null;

        try
        {
            algorithm = MessageDigest.getInstance("MD5");
        }
        catch (NoSuchAlgorithmException nsae)
        {
            System.out.println("Cannot find digest algorithm");
            return null;
        }
        
        byte[] defaultBytes = value.getBytes();
        algorithm.reset();
        algorithm.update(defaultBytes);
        
        byte messageDigest[] = algorithm.digest();
        
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < messageDigest.length; i++)
        {
            String hex = Integer.toHexString(0xFF & messageDigest[i]);
            if (hex.length() == 1)
            {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        
        return hexString.toString();
	}
	
	private static ArrayList<SIPAccount> getSIPAccounts()
	{
		ArrayList<SIPAccount> allAccounts = new ArrayList<SIPAccount>();
		
		String table = "subscriber";
		String[] columns = new String[]{"*"};
		
		for (Object[] row : select(getDefaultConnection(), table, columns, "", null))
		{
			SIPAccount sipAccount = SIPAccount.get(row);
			if (sipAccount != null) allAccounts.add(sipAccount);
		}
		
		return allAccounts;
	}
	
	private static boolean addRTExtension(String context, String exten, String priority, String app, String appdata)
	{
		String table = "rt_extensions";
		String[] columns = new String[]{"context", "exten", "priority", "app", "appdata"};
		Object[] values = new Object[]{context, exten, priority, app, appdata};
		
		return insert(getDefaultConnection(), table, columns, values);
	}
	
	private static boolean removeRTExtension(String context, String exten, String priority, String app, String appdata)
	{
		String table = "rt_extensions";
		String whereClause = "context=? AND exten=? AND priority=? AND app=? AND appdata=?";
		String[] whereValues = new String[]{context, exten, priority, app, appdata};
		
		return delete(getDefaultConnection(), table, whereClause, whereValues);
	}
	
	protected static boolean checkSIPUsernameDomainComboExist(String username, String domain)
	{
		for (SIPAccount account : getSIPAccounts())
		{
			if (account.getUsername().equalsIgnoreCase(username) &&
				account.getDomain().equalsIgnoreCase(domain))
				return true;
		}
		
		return false;
	}
	
	protected static ArrayList<SIPAccount> getSIPAccounts(int businessPartenerId)
	{
		ArrayList<SIPAccount> allAccounts = new ArrayList<SIPAccount>();
		
		String table = "subscriber";
		String[] columns = new String[]{"*"};
		String whereClause = "uuid=?";
		Object[] whereValues = new Object[]{businessPartenerId};
		
		for (Object[] row : select(getDefaultConnection(), table, columns, whereClause, whereValues))
		{
			SIPAccount sipAccount = SIPAccount.get(row);
			if (sipAccount != null) allAccounts.add(sipAccount);
		}
		
		return allAccounts;
	}
	
	protected static SIPAccount getSIPAccount(int subscriberId)
	{
		String table = "subscriber";
		String[] columns = new String[]{"*"};
		String whereClause = "id=?";
		Object[] whereValues = new Object[]{subscriberId};
		
		ArrayList<Object[]> rows = select(getDefaultConnection(), table, columns, whereClause, whereValues);
		if (rows.size() > 0 && rows.get(0) != null)
			return SIPAccount.get(rows.get(0));
		
		return null;
	}
	
	protected static SIPAccount getSIPAccount(String username, String domain)
	{
		String table = "subscriber";
		String[] columns = new String[]{"*"};
		String whereClause = "username=? AND domain=?";
		Object[] whereValues = new Object[]{username, domain};
		
		ArrayList<Object[]> rows = select(getDefaultConnection(), table, columns, whereClause, whereValues);
		if (rows.size() > 0 && rows.get(0) != null)
			return SIPAccount.get(rows.get(0));
		
		return null;
	}
	
	protected static ArrayList<String> getTimezones()
	{
		ArrayList<String> timezones = new ArrayList<String>();
		
		String table = "timezone";
		String[] columns = new String[]{"TZ"};
		
		for (Object[] row : select(getDefaultConnection(), table, columns, "", null))
		{
			if (row[0] instanceof String)
				timezones.add((String)row[0]);
			else
				log.warning("String expected but not found, Table=timezone, Column=TZ");
		}
		
		return timezones;
	}
	
	public static int addSIPAccount(String username, String password, String domain, String firstName, String lastName, String email, String timezone, String buisnessPartenerId)
	{
		Timestamp now = new Timestamp(System.currentTimeMillis());
		Double random = Math.random();
		
		String phplib_id = getMD5(username + now.toString() + random);
		String ha1 = getMD5(username + ":" + domain + ":" + password);
		String ha1b = getMD5(username + "@" + domain + ":" + domain + ":" + password);
		String confirmation = getMD5(phplib_id);
		String IPAddress = "147.202.44.140"; // TODO: Check what this IP is for
		int regSeconds = 3060; // TODO: Check this is constant
		String port = "5060"; // TODO: Check this is constant
		
		String table = "subscriber";
		String[] columns = new String[]{"phplib_id", "username", "domain", "password", "first_name", "last_name", 
										"phone", "email_address", "datetime_created", "datetime_modified", "confirmation", 
										"flag", "sendnotification", "greeting", "ha1", "ha1b", "allow_find", "timezone", 
										"rpid", "domn", "uuid", "vmail_password", "vmail", "ipaddr", "regseconds", "port"};
				
		// TODO: Check values below which are hard coded are constant
		Object[] values = new Object[]{phplib_id, username, domain, "", firstName, lastName,
									   "", email, now, now, confirmation, 
									   "o", "", "", ha1, ha1b, "0", timezone,
									   null, null, buisnessPartenerId, null, 0, IPAddress, regSeconds, port};
									   	
		if (insert(getDefaultConnection(), table, columns, values))
		{
			ArrayList<Object[]> rows = select(getDefaultConnection(), table, new String[]{"id"}, new String[]{"phplib_id", "datetime_created", "uuid"}, new Object[]{phplib_id, now, buisnessPartenerId});
			if (rows != null && rows.size() > 0 && rows.get(0) != null)
			{
				return (Integer)((Object[])rows.get(0))[0];
			}
		}
		else
			log.severe("Failed to add subscriber, check logs");
		
		return -1;
	}
	
	public static boolean removeSIPAccount(String username, String domain, String buisnessPartenerId)
	{
		String table = "subscriber";
		String whereClause = "username=? AND domain=? AND uuid=?";		
		String[] whereValues = new String[]{username, domain, buisnessPartenerId};
		
		return delete(getDefaultConnection(), table, whereClause, whereValues);
	}
	
	public static boolean addUserPreference(String uuid, String username, String domain, String attribute, String value, String type, String subscriberId)
	{
		Timestamp now = new Timestamp(System.currentTimeMillis());
		String date_end = "2025-12-31";
		
		String table = "usr_preferences";
		String[] columns = new String[]{"uuid", "username", "domain", "attribute", "value", 
										"type", "modified", "date_start", "date_end", "subscriber_id"};
		Object[] values = new Object[]{uuid, username, domain, attribute, value,
									   type, now, now, date_end, subscriberId};
		
		return insert(getDefaultConnection(), table, columns, values);
	}
	
	public static boolean removeUserPreference(String uuid, String username, String domain, String attribute, String value, String type, String subscriberId)
	{
		String table = "usr_preferences";
		String whereClause = "uuid=? AND username=? AND domain=? AND attribute=? AND value=? AND type=? AND subscriber_id=?";
		String[] whereValues = new String[]{uuid, username, domain, attribute, value, type, subscriberId};
		
		return delete(getDefaultConnection(), table, whereClause, whereValues);
	}
	
	protected static boolean updateUserPreference(String uuid, String username, String attributeName, String value)
	{
		// get the id using uuid, username and attribute name	
		String table = "usr_preferences";
		String[] columns = new String[]{"id"};
		String whereClause = "uuid=? AND username=? AND attribute=?";
		String[] whereValues = new String[]{uuid, username, attributeName};
		
		String rowToUpdateName = "id";
		String rowToUpdateValue = "";
		
		ArrayList<Object[]> rows = select(getDefaultConnection(), table, columns, whereClause, whereValues);
		if (rows.size() == 1)
		{
			Object[] row = rows.get(0);
			if (row[0] instanceof Integer)
				rowToUpdateValue = Integer.toString((Integer)row[0]);
			else if (row[0] instanceof String)
				rowToUpdateValue = (String)row[0];
		}
		else
		{
			log.warning("Could not get usr_preference id where uuid=" + uuid + ", username=" + username + " & attribute=" + attributeName);
			return false;
		}
		
		Timestamp now = new Timestamp(System.currentTimeMillis());
		
		columns = new String[]{"value", "modified"};
		Object[] values = new Object[]{value, now};
		
		if (update(getDefaultConnection(), table, rowToUpdateName, rowToUpdateValue, columns, values))
		{
			return true;
		}
		else
			log.severe("Failed to update user preference, check logs");
		
		return false;
	}
	
	protected static boolean endDateUserPreference(String uuid, String username, String attributeName)
	{
		// get the id using uuid, username and attribute name	
		String table = "usr_preferences";
		String[] columns = new String[]{"id"};
		String whereClause = "uuid=? AND username=? AND attribute=?";
		String[] whereValues = new String[]{uuid, username, attributeName};
		
		String rowToUpdateName = "id";
		String rowToUpdateValue = "";
		
		ArrayList<Object[]> rows = select(getDefaultConnection(), table, columns, whereClause, whereValues);
		if (rows.size() == 1)
		{
			Object[] row = rows.get(0);
			if (row[0] instanceof Integer)
				rowToUpdateValue = Integer.toString((Integer)row[0]);
			else if (row[0] instanceof String)
				rowToUpdateValue = (String)row[0];
		}
		else
		{
			log.warning("Could not get usr_preference id where uuid=" + uuid + ", username=" + username + " & attribute=" + attributeName);
			return false;
		}
		
		Timestamp now = new Timestamp(System.currentTimeMillis());
		
		columns = new String[]{"date_end", "modified"};
		Object[] values = new Object[]{now, now};
		
		if (update(getDefaultConnection(), table, rowToUpdateName, rowToUpdateValue, columns, values))
		{
			return true;
		}
		else
			log.severe("Failed to update 'date_end' for user preference row, check logs where id=" + rowToUpdateValue + ", uuid=" + uuid + ", username=" + username);
		
		return false;
	}
	
	protected static boolean updateSIPAccount(String id, String username, String domain, String password, String timezone)
	{	
		Timestamp now = new Timestamp(System.currentTimeMillis());
		
		String ha1 = getMD5(username + ":" + domain + ":" + password);
		String ha1b = getMD5(username + "@" + domain + ":" + domain + ":" + password);
		
		String table = "subscriber";
		String[] columns = new String[]{"username", "domain", "ha1", "ha1b", "timezone", "datetime_modified"};
		Object[] values = new Object[]{username, domain, ha1, ha1b, timezone, now};
		String rowToUpdateName = "id";
		String rowToUpdateValue = id;
									   	
		if (update(getDefaultConnection(), table, rowToUpdateName, rowToUpdateValue, columns, values))
		{
			return true;
		}
		else
			log.severe("Failed to update subscriber, check logs");
		
		return false;
	}
	
	protected static boolean updateSIPAccount(String id, String username, String domain, String timezone)
	{	
		Timestamp now = new Timestamp(System.currentTimeMillis());
		
		String table = "subscriber";
		String[] columns = new String[]{"username", "domain", "timezone", "datetime_modified"};
		Object[] values = new Object[]{username, domain, timezone, now};
		String rowToUpdateName = "id";
		String rowToUpdateValue = id;
									   	
		if (update(getDefaultConnection(), table, rowToUpdateName, rowToUpdateValue, columns, values))
		{
			return true;
		}
		else
			log.severe("Failed to add subscriber, check logs");
		
		return false;
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
		
		return insert(getDefaultConnection(), table, columns, values);
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
		
		return delete(getDefaultConnection(), table, whereClause, whereValues);
	}
	
	// TODO: Use transactions
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

	public static void removeVoicemailToDialPlan(String number, String bpSearchKey)
	{
		String context = "proxy_default";
		String numberBUSY = number + "BUSY";
		String appSIP = "SIP/" + number;
		String macro = "Macro";
		String hangup = "Hangup";
		String wildcard = "%value%";
		String macroAppData = "proxy-vm|" + number + "|" + wildcard + "|" + bpSearchKey;
		
		removeRTExtension(context, number, "-1", appSIP, "");
		removeRTExtension(context, number, "1", macro, macroAppData.replace(wildcard, number));
		removeRTExtension(context, number, "2", hangup, "");
		removeRTExtension(context, numberBUSY, "-1", appSIP, "");
		removeRTExtension(context, numberBUSY, "1", macro, macroAppData.replace(wildcard, "BUSY"));
		removeRTExtension(context, numberBUSY, "2", hangup, "");
	}
	
	public static boolean addVoicemailPreferences(String bpId, String number, String domain, String bpSearchKey)
	{
		String wildcard = "%value%";
		String uri = "sip:" + number + wildcard + "@c-vm-02.conversant.co.nz";
		String timeout = "15";
		String dummySubscriberId = "999";		
		String sipAccount = "sip:" + number + "@" + domain;
		
		boolean retValue = addUserPreference(bpId, number, domain, "20106", uri.replace(wildcard, "BUSY"), SIPServlet.USR_PREF_ATTR_TYPE_NUMERIC, dummySubscriberId);
		if (retValue)
		{
			retValue = addUserPreference(bpId, number, domain, "20111", uri.replace(wildcard, ""), SIPServlet.USR_PREF_ATTR_TYPE_NUMERIC, dummySubscriberId);
			if (retValue)
			{
				retValue = addUserPreference(bpId, number, domain, "20116", uri.replace(wildcard, ""), SIPServlet.USR_PREF_ATTR_TYPE_NUMERIC, dummySubscriberId);
				if (retValue)
				{
					retValue = addUserPreference(bpId, number, domain, "20201", timeout, SIPServlet.USR_PREF_ATTR_TYPE_NUMERIC, dummySubscriberId);
					if (retValue)
					{
						// value = SIP account (used to present caller id)
						retValue = addUserPreference(bpId, number, domain, "37501", sipAccount, SIPServlet.USR_PREF_ATTR_TYPE_NUMERIC, dummySubscriberId);
						if (retValue)
						{
							// value = DID number (this captures the mailbox number and enables separation of the phone number, SIP account and mailbox number)
							retValue = addUserPreference(bpId, number, domain, "10501", number, SIPServlet.USR_PREF_ATTR_TYPE_NUMERIC, dummySubscriberId);
							if (retValue)
							{
								// value = public customer number (this is so we know what voicemail "context" is the correct one for this SIP account)
								retValue = addUserPreference(bpId, number, domain, "90001", bpSearchKey, SIPServlet.USR_PREF_ATTR_TYPE_NUMERIC, dummySubscriberId);
								if (!retValue)
								{
									removeUserPreference(bpId, number, domain, "20106", uri.replace(wildcard, "BUSY"), SIPServlet.USR_PREF_ATTR_TYPE_NUMERIC, dummySubscriberId);
									removeUserPreference(bpId, number, domain, "20111", uri.replace(wildcard, ""), SIPServlet.USR_PREF_ATTR_TYPE_NUMERIC, dummySubscriberId);
									removeUserPreference(bpId, number, domain, "20116", uri.replace(wildcard, ""), SIPServlet.USR_PREF_ATTR_TYPE_NUMERIC, dummySubscriberId);
									removeUserPreference(bpId, number, domain, "20201", timeout, SIPServlet.USR_PREF_ATTR_TYPE_NUMERIC, dummySubscriberId);
									removeUserPreference(bpId, number, domain, "37501", sipAccount, SIPServlet.USR_PREF_ATTR_TYPE_NUMERIC, dummySubscriberId);
									removeUserPreference(bpId, number, domain, "10501", number, SIPServlet.USR_PREF_ATTR_TYPE_NUMERIC, dummySubscriberId);
								}
							}
							else
							{
								removeUserPreference(bpId, number, domain, "20106", uri.replace(wildcard, "BUSY"), SIPServlet.USR_PREF_ATTR_TYPE_NUMERIC, dummySubscriberId);
								removeUserPreference(bpId, number, domain, "20111", uri.replace(wildcard, ""), SIPServlet.USR_PREF_ATTR_TYPE_NUMERIC, dummySubscriberId);
								removeUserPreference(bpId, number, domain, "20116", uri.replace(wildcard, ""), SIPServlet.USR_PREF_ATTR_TYPE_NUMERIC, dummySubscriberId);
								removeUserPreference(bpId, number, domain, "20201", timeout, SIPServlet.USR_PREF_ATTR_TYPE_NUMERIC, dummySubscriberId);
								removeUserPreference(bpId, number, domain, "37501", sipAccount, SIPServlet.USR_PREF_ATTR_TYPE_NUMERIC, dummySubscriberId);
							}
						}
						else
						{
							removeUserPreference(bpId, number, domain, "20106", uri.replace(wildcard, "BUSY"), SIPServlet.USR_PREF_ATTR_TYPE_NUMERIC, dummySubscriberId);
							removeUserPreference(bpId, number, domain, "20111", uri.replace(wildcard, ""), SIPServlet.USR_PREF_ATTR_TYPE_NUMERIC, dummySubscriberId);
							removeUserPreference(bpId, number, domain, "20116", uri.replace(wildcard, ""), SIPServlet.USR_PREF_ATTR_TYPE_NUMERIC, dummySubscriberId);
							removeUserPreference(bpId, number, domain, "20201", timeout, SIPServlet.USR_PREF_ATTR_TYPE_NUMERIC, dummySubscriberId);
						}
					}
					else
					{
						removeUserPreference(bpId, number, domain, "20106", uri.replace(wildcard, "BUSY"), SIPServlet.USR_PREF_ATTR_TYPE_NUMERIC, dummySubscriberId);
						removeUserPreference(bpId, number, domain, "20111", uri.replace(wildcard, ""), SIPServlet.USR_PREF_ATTR_TYPE_NUMERIC, dummySubscriberId);
						removeUserPreference(bpId, number, domain, "20116", uri.replace(wildcard, ""), SIPServlet.USR_PREF_ATTR_TYPE_NUMERIC, dummySubscriberId);
					}
				}
				else
				{
					removeUserPreference(bpId, number, domain, "20106", uri.replace(wildcard, "BUSY"), SIPServlet.USR_PREF_ATTR_TYPE_NUMERIC, dummySubscriberId);
					removeUserPreference(bpId, number, domain, "20111", uri.replace(wildcard, ""), SIPServlet.USR_PREF_ATTR_TYPE_NUMERIC, dummySubscriberId);
				}
			}
			else
				removeUserPreference(bpId, number, domain, "20106", uri.replace(wildcard, "BUSY"), SIPServlet.USR_PREF_ATTR_TYPE_NUMERIC, dummySubscriberId);
		}
			
		return retValue;
	}
	
	public static void main(String[] args)
	{
		
	}
}
