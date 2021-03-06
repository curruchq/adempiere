package com.conversant.model;

import java.util.logging.Level;

import org.compiere.util.CLogger;

public class BillingAccount
{
	/** Logger						*/
	private static CLogger log = CLogger.getCLogger(BillingAccount.class);
	
	private Integer billingAccountId = null;
	private String login = null;
	private String username = null;
	private String password = null;
	
	private boolean valid = false;
	
	public static BillingAccount createFromDB(Object[] dbRow)
	{
		BillingAccount billingAccount = new BillingAccount(dbRow);
		return billingAccount.isValid() ? billingAccount : null;
	}
	
	private BillingAccount(Object[] dbRow)
	{
		try
		{
			billingAccountId = (Integer)dbRow[0];
			login = (String)dbRow[1];
			username = (String)dbRow[2];
			password = (String)dbRow[3];
			
			setValid(true);
		}
		catch (Exception ex)
		{
			setValid(false);
			log.log(Level.SEVERE, "Error casting data from DB row", ex);
		}
	}
	
	public String toString()
	{
		return "BillingAccount[" + getBillingAccountId() + "]";
	}

	public Integer getBillingAccountId()
	{
		return billingAccountId;
	}

	public void setBillingAccountId(Integer billingAccountId)
	{
		this.billingAccountId = billingAccountId;
	}

	public String getLogin()
	{
		return login;
	}

	public void setLogin(String login)
	{
		this.login = login;
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public boolean isValid()
	{
		return valid;
	}

	public void setValid(boolean valid)
	{
		this.valid = valid;
	}
}
