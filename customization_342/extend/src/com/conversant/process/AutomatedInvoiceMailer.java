package com.conversant.process;

import java.io.File;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.logging.Level;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.compiere.db.CConnection;
import org.compiere.interfaces.Server;
import org.compiere.model.MBPartner;
import org.compiere.model.MClient;
import org.compiere.model.MInvoice;
import org.compiere.model.MMailText;
import org.compiere.model.MUser;
import org.compiere.model.MUserMail;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.CLogger;
import org.compiere.util.EMail;
import org.compiere.util.Env;
import org.compiere.util.Ini;

// TODO: Remove listOnly?
// TODO: Adjust message based on payment method (see ticket)
// TODO: Save in to UserMail?
public class AutomatedInvoiceMailer extends SvrProcess
{
	/** Logger */
	private static CLogger log = CLogger.getCLogger(AutomatedInvoiceMailer.class);
	
	private int AD_Client_ID = 1000000; // Conversant
	
	private int AD_Org_ID = 1000001; // Conversant
	
	private int Default_R_MailText_ID;
	
	private int Cash_R_MailText_ID;
	
	private int Check_R_MailText_ID;
	
	private int CreditCard_R_MailText_ID;
	
	private int DirectDeposit_R_MailText_ID;
	
	private int DirectDebit_R_MailText_ID;
	
	private int OnCredit_R_MailText_ID;
	
	private boolean listOnly = false;
	
	private boolean useInvoiceSendEmailFlag = false;
	
	private boolean useBusinessPartnerSendEmailFlag = false;

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
			else if (name.equals("AD_Client_ID"))
			{
				AD_Client_ID = ((BigDecimal)para[i].getParameter()).intValue();
			}
			else if (name.equals("AD_Org_ID"))
			{
				AD_Org_ID = ((BigDecimal)para[i].getParameter()).intValue();
			}
			else if (name.equals("ListOnly"))
			{
				listOnly = "Y".equals(para[i].getParameter());
			}
			else if (name.equals("UseInvoiceSendEmailFlag"))
			{
				useInvoiceSendEmailFlag = "Y".equals(para[i].getParameter());
			}
			else if (name.equals("UseBusinessPartnerSendEmailFlag"))
			{
				useBusinessPartnerSendEmailFlag = "Y".equals(para[i].getParameter());
			}
			else if (name.equals("Default_R_MailText_ID"))
			{
				Default_R_MailText_ID = ((BigDecimal)para[i].getParameter()).intValue();
			}
			else if (name.equals("Cash_R_MailText_ID"))
			{
				Cash_R_MailText_ID = ((BigDecimal)para[i].getParameter()).intValue();
			}
			else if (name.equals("Check_R_MailText_ID"))
			{
				Check_R_MailText_ID = ((BigDecimal)para[i].getParameter()).intValue();
			}
			else if (name.equals("CreditCard_R_MailText_ID"))
			{
				CreditCard_R_MailText_ID = ((BigDecimal)para[i].getParameter()).intValue();
			}
			else if (name.equals("DirectDeposit_R_MailText_ID"))
			{
				DirectDeposit_R_MailText_ID = ((BigDecimal)para[i].getParameter()).intValue();
			}
			else if (name.equals("DirectDebit_R_MailText_ID"))
			{
				DirectDebit_R_MailText_ID = ((BigDecimal)para[i].getParameter()).intValue();
			}
			else if (name.equals("OnCredit_R_MailText_ID"))
			{
				OnCredit_R_MailText_ID = ((BigDecimal)para[i].getParameter()).intValue();
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
		int originalAD_Client_ID = Env.getAD_Client_ID(getCtx());
		Env.setContext(getCtx(), "#AD_Client_ID", AD_Client_ID);
		
		int originalAD_Org_ID = Env.getAD_Org_ID(getCtx());
		Env.setContext(getCtx(), "#AD_Org_ID", AD_Org_ID);
		
		// Validate 
		String msg = validate();
		
		// Validation passed
		if (msg == null)
			msg = mailInvoices();
		
		Env.setContext(getCtx(), "#AD_Client_ID", originalAD_Client_ID);
		Env.setContext(getCtx(), "#AD_Org_ID", originalAD_Org_ID);
		
		return msg;
	}
	
	private String validate()
	{
		StringBuilder sb = new StringBuilder();
		
		// Check at least one send email flag is set to be used
		if (!useBusinessPartnerSendEmailFlag && !useInvoiceSendEmailFlag)
		{
			log.warning("Cannot mail invoices without checking at least one Send Email flag");
			sb.append("Cannot mail invoices without checking at least one Send Email flag");
		}
		
		// Check mail messages can be loaded
		MMailText default_mailText = new MMailText(getCtx(), Default_R_MailText_ID, get_TrxName());
		if (default_mailText == null || default_mailText.is_new())
		{
			log.warning("Cannot load Default mail text MMailText[" + Default_R_MailText_ID + "]");
			sb.append("Cannot load Default mail text MMailText[" + Default_R_MailText_ID + "]");
		}
		
		MMailText cash_mailText = new MMailText(getCtx(), Cash_R_MailText_ID, get_TrxName());
		if (cash_mailText == null || cash_mailText.is_new())
		{
			log.warning("Cannot load Cash mail text MMailText[" + Cash_R_MailText_ID + "]");
			sb.append("Cannot load Cash Card mail text MMailText[" + Cash_R_MailText_ID + "]");
		}
		
		MMailText check_mailText = new MMailText(getCtx(), Check_R_MailText_ID, get_TrxName());
		if (check_mailText == null || check_mailText.is_new())
		{
			log.warning("Cannot load Check mail text MMailText[" + Check_R_MailText_ID + "]");
			sb.append("Cannot load Check mail text MMailText[" + Check_R_MailText_ID + "]");
		}
		
		MMailText creditCard_mailText = new MMailText(getCtx(), CreditCard_R_MailText_ID, get_TrxName());
		if (creditCard_mailText == null || creditCard_mailText.is_new())
		{
			log.warning("Cannot load Credit Card mail text MMailText[" + CreditCard_R_MailText_ID + "]");
			sb.append("Cannot load Credit Card mail text MMailText[" + CreditCard_R_MailText_ID + "]");
		}
		
		MMailText directDeposit_mailText = new MMailText(getCtx(), DirectDeposit_R_MailText_ID, get_TrxName());
		if (directDeposit_mailText == null || directDeposit_mailText.is_new())
		{
			log.warning("Cannot load Direct Deposit mail text MMailText[" + DirectDeposit_R_MailText_ID + "]");
			sb.append("Cannot load Direct Deposit mail text MMailText[" + DirectDeposit_R_MailText_ID + "]");
		}
		
		MMailText directDebit_mailText = new MMailText(getCtx(), DirectDebit_R_MailText_ID, get_TrxName());
		if (directDebit_mailText == null || directDebit_mailText.is_new())
		{
			log.warning("Cannot load Direct Debit mail text MMailText[" + DirectDebit_R_MailText_ID + "]");
			sb.append("Cannot load Direct Debit mail text MMailText[" + DirectDebit_R_MailText_ID + "]");
		}
		
		MMailText onCredit_mailText = new MMailText(getCtx(), OnCredit_R_MailText_ID, get_TrxName());
		if (onCredit_mailText == null || onCredit_mailText.is_new())
		{
			log.warning("Cannot load On Credit mail text MMailText[" + OnCredit_R_MailText_ID + "]");
			sb.append("Cannot load On Credit mail text MMailText[" + OnCredit_R_MailText_ID + "]");
		}
		
		if (sb.length() > 0)
			return sb.toString();
		
		return null;
	}
	
	private String mailInvoices()
	{
		// Create where clause
		String whereClause = "UPPER(IsActive)='Y' AND AD_Client_ID=" + AD_Client_ID + " AND EmailSent IS NULL"; 
		
		if (useInvoiceSendEmailFlag)
			whereClause += " AND UPPER(SendEmail)='Y'";
		
		if (useBusinessPartnerSendEmailFlag)
			whereClause += " AND " + MInvoice.COLUMNNAME_C_BPartner_ID + " IN (SELECT " + MBPartner.COLUMNNAME_C_BPartner_ID + " FROM " + MBPartner.Table_Name + " WHERE UPPER(SENDEMAIL)='Y')";
		
		// Load invoices
		int[] invoiceIds = MInvoice.getAllIDs(MInvoice.Table_Name, whereClause, get_TrxName());
		
		if (invoiceIds.length < 1)
			return "@Success@ - No invoices to send";
		
		// Load messages
		MMailText default_mailText = new MMailText(getCtx(), Default_R_MailText_ID, get_TrxName());
		MMailText cash_mailText = new MMailText(getCtx(), Cash_R_MailText_ID, get_TrxName());
		MMailText check_mailText = new MMailText(getCtx(), Check_R_MailText_ID, get_TrxName());
		MMailText creditCard_mailText = new MMailText(getCtx(), CreditCard_R_MailText_ID, get_TrxName());
		MMailText directDeposit_mailText = new MMailText(getCtx(), DirectDeposit_R_MailText_ID, get_TrxName());
		MMailText directDebit_mailText = new MMailText(getCtx(), DirectDebit_R_MailText_ID, get_TrxName());
		MMailText onCredit_mailText = new MMailText(getCtx(), OnCredit_R_MailText_ID, get_TrxName());
		
		// Create list for responses
		ArrayList<String> emailResponses = new ArrayList<String>();
		
		// Loop through each invoice
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

			// Set message depending on payment rule
			MMailText mailText = default_mailText;
			if (invoice.getPaymentRule().equals(MInvoice.PAYMENTRULE_Cash))
				mailText = cash_mailText;
			else if (invoice.getPaymentRule().equals(MInvoice.PAYMENTRULE_Check))
				mailText = check_mailText;
			else if (invoice.getPaymentRule().equals(MInvoice.PAYMENTRULE_CreditCard))
				mailText = creditCard_mailText;
			else if (invoice.getPaymentRule().equals(MInvoice.PAYMENTRULE_DirectDeposit))
				mailText = directDeposit_mailText;
			else if (invoice.getPaymentRule().equals(MInvoice.PAYMENTRULE_DirectDebit))
				mailText = directDebit_mailText;
			else if (invoice.getPaymentRule().equals(MInvoice.PAYMENTRULE_OnCredit))
				mailText = onCredit_mailText;
			
			// Create email and add attachment
			EMail email = createEmail(user.getEMail(), mailText.getMailHeader(), mailText.getMailText(true), mailText.isHtml());
			email.addAttachment(file);

			// Send email and store response
			String emailResponse = email.send();
			emailResponses.add("MInvoice[" + invoice.get_ID() + "] " + emailResponse);
			
			// Record email being sent
			MUserMail um = new MUserMail(mailText, user.getAD_User_ID(), email);
			um.save();
			
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
