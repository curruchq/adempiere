package com.conversant.process;

import java.io.File;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.logging.Level;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.compiere.db.CConnection;
import org.compiere.interfaces.Server;
import org.compiere.model.MClient;
import org.compiere.model.MInvoice;
import org.compiere.model.MMailText;
import org.compiere.model.MUserMail;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.CLogger;
import org.compiere.util.EMail;
import org.compiere.util.Env;
import org.compiere.util.Ini;

public class InvoiceMailer extends SvrProcess
{
	/** Logger										*/
	private static CLogger log = CLogger.getCLogger(InvoiceMailer.class);
	
	private int R_MailText_ID;
	
	private int C_Invoice_ID;
	
	private String emailAddress;
	
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	@Override
	protected void prepare()
	{
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (para[i].getParameter() == null)
				;
			else if (name.equals("R_MailText_ID"))
			{
				R_MailText_ID = ((BigDecimal)para[i].getParameter()).intValue();
			}
			else if (name.equals("C_Invoice_ID"))
			{
				C_Invoice_ID = ((BigDecimal)para[i].getParameter()).intValue();
			}
			else if (name.equals("EmailAddress"))
			{
				emailAddress = (String)para[i].getParameter();		
			}
			else
			{
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
			}
		}
	}

	/**
	 * 	Process
	 *	@return message
	 *	@throws Exception
	 */
	@Override
	protected String doIt() throws Exception
	{
		return mailInvoice();
	}
	
	private String mailInvoice()
	{
		// Load Message
		MMailText mailText = new MMailText(getCtx(), R_MailText_ID, get_TrxName());
		
		// Load invoice
		MInvoice invoice = MInvoice.get(getCtx(), C_Invoice_ID);
		if (invoice == null)
			return "@Error@ Invoice[" + C_Invoice_ID + "] does not exist";
		
		// Validate Email Address
		if (!isEmailValid(emailAddress))
			return "@Error@ Invalid email address '" + emailAddress + "'";
		
		// Check Directory
		String directory = getCtx().getProperty("documentDir", ".");
		try
		{
			File dir = new File(directory);
			if (!dir.exists())
				dir.mkdir();
		}
		catch (Exception ex)
		{
			log.log(Level.SEVERE, "Could not create directory " + directory, ex);
			return "@Error@ Could not create directory for Invoices";
		}

		// Check if invoice already created
		String fileName = invoice.getPDFFileName(directory);
		File file = new File(fileName);
		if (file.exists() && file.isFile() && file.length() > 2000)
			log.info("Existing: " + file + " - " + new Timestamp(file.lastModified()));
		else
		{
			log.info("New: " + fileName);
			file = invoice.createPDF(file);
			if (file != null)
			{
				invoice.setDatePrinted(new Timestamp(System.currentTimeMillis()));
				invoice.save();
			}
		}
		
		// Create email and add attachment
		EMail email = createEmail(emailAddress, mailText.getMailHeader(), mailText.getMailText(true), mailText.isHtml());
		email.addAttachment(file);
		
		// Send email and store response
		String retValue = email.send();
		
		// Record email being sent
		MUserMail um = new MUserMail(mailText, Env.getAD_User_ID(getCtx()), email);
		um.save();
		
		return retValue;
	}

	public EMail createEmail(String to, String subject, String message, boolean html)
	{
		EMail email = null;
		MClient client = MClient.get(getCtx(), getAD_Client_ID());
		if (client.isServerEMail() && Ini.isClient())
		{
			Server server = CConnection.get().getServer();
			try
			{
				if (server != null)
					email = server.createEMail(getCtx(), getAD_Client_ID(), to, subject, message);
				else
					log.log(Level.WARNING, "No AppsServer");
			}
			catch (RemoteException ex)
			{
				log.log(Level.SEVERE, getName() + " - AppsServer error", ex);
			}
		}
		String from = client.getRequestEMail();

		if (email == null)
			email = new EMail(client, from, to, subject, message, html);

		email.addBcc(from);

		return email;
	}
	
	/**
	 *	Is Email address valid
	 *
	 * 	@param email address
	 * 	@return true if valid
	 */
	public static boolean isEmailValid(String email)
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
			log.warning(email + " - " + ex.getLocalizedMessage());
		}
		
		return false;
	}
}
