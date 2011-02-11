package com.conversant.process;

import java.io.File;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.logging.Level;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.compiere.db.CConnection;
import org.compiere.interfaces.Server;
import org.compiere.model.MClient;
import org.compiere.model.MInvoice;
import org.compiere.model.MMailMsg;
import org.compiere.model.MUser;
import org.compiere.model.MUserMail;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.CLogger;
import org.compiere.util.EMail;
import org.compiere.util.Ini;

// TODO: Handle client org when run via scheduler
// TODO: Remove listOnly?
// TODO: Adjust message based on payment method (see ticket)
// TODO: Save in to UserMail?
public class AutomatedInvoiceMailer extends SvrProcess
{
	/** Logger */
	private static CLogger log = CLogger.getCLogger(AutomatedInvoiceMailer.class);
	
	private boolean listOnly = false;

	/**
	 * Prepare - e.g., get Parameters.
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
			else if (name.equals("ListOnly"))
			{
				listOnly = "Y".equals(para[i].getParameter());
			}
			else
			{
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
			}
		}
	}

	/**
	 * Process
	 * 
	 * @return message
	 * @throws Exception
	 */
	@Override
	protected String doIt() throws Exception
	{
		ArrayList<String> emailResponses = new ArrayList<String>();
		
		int[] invoiceIds = MInvoice.getAllIDs(MInvoice.Table_Name, "UPPER(IsActive)='Y' AND UPPER(SendEmail)='Y' AND EmailSent IS NULL", null);
		for (int id : invoiceIds)
		{
			// Load invoice and make sure its completed
			MInvoice invoice = MInvoice.get(getCtx(), id);
			if (!invoice.isComplete())
				continue;

			// Find user with valid email
			MUser user = MUser.get(getCtx(), invoice.getAD_User_ID());
			if (user == null || user.get_ID() == 0 || !isEmailValid(user.getEMail()))
			{
				MUser[] bpUsers = MUser.getOfBPartner(getCtx(), invoice.getC_BPartner_ID());
				for (MUser bpUser : bpUsers)
				{
					if (bpUser != null && bpUser.get_ID() > 0 && isEmailValid(bpUser.getEMail()))
					{
						user = bpUser;
						break;
					}
				}
				
				if (user == null || user.get_ID() == 0)
				{
					log.warning("Cannot mail MInvoice[" + invoice.get_ID() + "] without a user");
					emailResponses.add("MInvoice[" + invoice.get_ID() + "] No user");
					continue;
				}
			} 

			// Validate email
			if (!isEmailValid(user.getEMail()))
			{
				log.warning("Cannot mail MInvoice[" + invoice.get_ID() + "] to invalid email for MUser[" + user.get_ID() + "]");
				emailResponses.add("MInvoice[" + invoice.get_ID() + "] Invalid email");
				continue;
			}
			
			// Don't send email, just list invoices
			if (listOnly)
			{
				emailResponses.add("MInvoice[" + invoice.get_ID() + "]");
				continue;
			}

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

			// Load message
			
			
			// Create email and add attachment
			EMail email = createEmail(user.getEMail(), "Conversant Invoice", "Please find your invoice attached", true);
			email.addAttachment(file);

			// Send email and store response
			String emailResponse = email.send();
			emailResponses.add("MInvoice[" + invoice.get_ID() + "] " + emailResponse);
			
//			MUserMail um = new MUserMail(mailMsg, 1000000, email);
//			um.save();
			
			// Set email sent field
			if (EMail.SENT_OK.equals(emailResponse))
			{
				invoice.set_CustomColumn("EmailSent", new Timestamp(System.currentTimeMillis()));
				invoice.save();
			}
		}
		
		// Build response message
		StringBuilder sb = new StringBuilder("@Success@ - ");
		
		if (listOnly)
			sb.append("The following invoices will be mailed when 'List Only' isn't checked - ");
		
		for (String response : emailResponses)
		{
			sb.append(response).append(", ");
		}		
		
		String msg = sb.substring(0, sb.length() - 2);	
		return msg;
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
	 * Is Email address valid
	 * 
	 * @param email
	 *            address
	 * @return true if valid
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
