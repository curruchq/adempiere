package com.conversant.webservice.util;

import java.sql.Timestamp;
import java.util.Properties;

import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.compiere.util.Ini;
import org.compiere.util.KeyNamePair;
import org.compiere.util.Login;

public class WebServiceContext
{
	private static CLogger log = CLogger.getCLogger(WebServiceContext.class);
	
	private Properties ctx = new Properties();
	
	private int AD_Client_ID = -1;
	private int AD_Org_ID = -1;
	private int AD_User_ID = -1;
	private int AD_Role_ID = -1;

	private boolean LoggedIn = false;

	protected void loadLoginData()
	{
		
	}
	
	// TODO: Check Org being set in login.loadPreferences() is OK and that it doesn't need to be set before login.validateLogin()
	protected void saveLoginData(Login login, KeyNamePair orgLogin)
	{
		login.loadPreferences(orgLogin, null, new Timestamp(System.currentTimeMillis()), null);

		setLoggedIn(true);
		setAD_Client_ID(Env.getAD_Client_ID(getCtx()));
		setAD_Org_ID(Env.getAD_Org_ID(getCtx()));
		setAD_User_ID(Env.getAD_User_ID(getCtx()));
		setAD_Role_ID(Env.getAD_Role_ID(getCtx()));
	}
	
	protected void resetLoginData()
	{
		setLoggedIn(false);
		setAD_Client_ID(-1);
		setAD_Org_ID(-1);
		setAD_User_ID(-1);
		setAD_Role_ID(-1);
	}
		
	public Properties getCtx()
	{
		return ctx;
	}

	public void setCtx(Properties ctx)
	{
		this.ctx = ctx;
	}

	public int getAD_Client_ID()
	{
		return AD_Client_ID;
	}

	public void setAD_Client_ID(int client_ID)
	{
		AD_Client_ID = client_ID;
	}

	public int getAD_Org_ID()
	{
		return AD_Org_ID;
	}

	public void setAD_Org_ID(int org_ID)
	{
		AD_Org_ID = org_ID;
	}

	public int getAD_User_ID()
	{
		return AD_User_ID;
	}

	public void setAD_User_ID(int user_ID)
	{
		AD_User_ID = user_ID;
	}

	public int getAD_Role_ID()
	{
		return AD_Role_ID;
	}

	public void setAD_Role_ID(int role_ID)
	{
		AD_Role_ID = role_ID;
	}
	
	public boolean isLoggedIn()
	{
		return LoggedIn;
	}

	public void setLoggedIn(boolean loggedIn)
	{
		LoggedIn = loggedIn;
	}
}
