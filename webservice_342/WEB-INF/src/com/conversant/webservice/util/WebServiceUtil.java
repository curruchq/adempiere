package com.conversant.webservice.util;

import java.util.logging.Level;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.compiere.Adempiere;
import org.compiere.util.CLogger;

public class WebServiceUtil
{
	private static CLogger log = CLogger.getCLogger(WebServiceUtil.class);
	
	private static boolean serverStarted;
	
	public static boolean startADempiere()
	{
		try
		{
			if (!isServerStarted())
				setServerStarted(Adempiere.startup(false));
		}
		catch (Exception ex)
		{
			log.log(Level.SEVERE, "Failed to start ADempiere server", ex);
			setServerStarted(false);
		}
		
		return isServerStarted();
	}

	public static boolean isServerStarted()
	{
		return serverStarted;
	}

	public static void setServerStarted(boolean serverStarted)
	{
		WebServiceUtil.serverStarted = serverStarted;
	}
	
	/**
	 *	Is Email address valid
	 *
	 * 	@param email address
	 * 	@return true if valid
	 */
	public static boolean isEmailValid (String email)
	{
		if (email == null || email.length() < 1)
			return false;
		
		try
		{
			InternetAddress ia = new InternetAddress(email, true);
			if (ia != null)
				return true;
		}
		catch (AddressException ex)
		{
			log.warning (email + " - " + ex.getLocalizedMessage());
		}
		
		return false;
	}
}
