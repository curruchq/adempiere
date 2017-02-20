package com.conversant.process;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.compiere.db.CConnection;
import org.compiere.interfaces.Server;
import org.compiere.model.MClient;
import org.compiere.model.MInvoice;
import org.compiere.model.MMailText;
import org.compiere.model.MUser;
import org.compiere.model.MUserMail;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.EMail;
import org.compiere.util.Env;
import org.compiere.util.Ini;

public class AutomatedInvoiceMailer extends SvrProcess
{
	/** Logger */
	private static CLogger log = CLogger.getCLogger(AutomatedInvoiceMailer.class);
	
	/** Conversant Client														*/
	private int AD_Client_ID = 1000000; // Conversant
	
	/** Conversant Org															*/
	private int AD_Org_ID = 1000001; // Conversant
	
	/** Mail template/text ids (defined in ADempiere)							*/
	private int Default_R_MailText_ID;
	
	private int Cash_R_MailText_ID;
	
	private int Check_R_MailText_ID;
	
	private int CreditCard_R_MailText_ID;
	
	private int DirectDeposit_R_MailText_ID;
	
	private int DirectDebit_R_MailText_ID;
	
	private int OnCredit_R_MailText_ID;
	
	
	/** Only show list, don't send any invoices									*/
	private boolean listOnly = true;
	
	/** Use invoice's SendEmail flag to retrieve invoices to send				*/
	private boolean useInvoiceSendEmailFlag = false;
	
	/**	Use business partner's SendEmail flag to reteieve invoices to send		*/
	private boolean useBusinessPartnerSendEmailFlag = false;
	
	public final String INVOICE_IDENTIFIER = "##DOCUMENTNO##";
	public final String PAYMENTMETHOD_IDENTIFIER = "##PAYMENTMETHOD##";
	public final String DUEDATE_IDENTIFIER = "##DUEDATE##";
	public final String GUID_IDENTIFIER = "##GUID##";

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
		if (default_mailText == null || default_mailText.get_ID() == 0 || default_mailText.is_new())
		{
			log.warning("Cannot load Default mail text MMailText[" + Default_R_MailText_ID + "]");
			if (sb.length() > 0)
				sb.append(", ");
			sb.append("Cannot load Default mail text MMailText[" + Default_R_MailText_ID + "]");
		}
		
		MMailText cash_mailText = new MMailText(getCtx(), Cash_R_MailText_ID, get_TrxName());
		if (cash_mailText == null || cash_mailText.get_ID() == 0 || cash_mailText.is_new())
		{
			log.warning("Cannot load Cash mail text MMailText[" + Cash_R_MailText_ID + "]");
			if (sb.length() > 0)
				sb.append(", ");
			sb.append("Cannot load Cash Card mail text MMailText[" + Cash_R_MailText_ID + "]");
		}
		
		MMailText check_mailText = new MMailText(getCtx(), Check_R_MailText_ID, get_TrxName());
		if (check_mailText == null || check_mailText.get_ID() == 0 || check_mailText.is_new())
		{
			log.warning("Cannot load Check mail text MMailText[" + Check_R_MailText_ID + "]");
			if (sb.length() > 0)
				sb.append(", ");
			sb.append("Cannot load Check mail text MMailText[" + Check_R_MailText_ID + "]");
		}
		
		MMailText creditCard_mailText = new MMailText(getCtx(), CreditCard_R_MailText_ID, get_TrxName());
		if (creditCard_mailText == null || creditCard_mailText.get_ID() == 0 || creditCard_mailText.is_new())
		{
			log.warning("Cannot load Credit Card mail text MMailText[" + CreditCard_R_MailText_ID + "]");
			if (sb.length() > 0)
				sb.append(", ");
			sb.append("Cannot load Credit Card mail text MMailText[" + CreditCard_R_MailText_ID + "]");
		}
		
		MMailText directDeposit_mailText = new MMailText(getCtx(), DirectDeposit_R_MailText_ID, get_TrxName());
		if (directDeposit_mailText == null || directDeposit_mailText.get_ID() == 0 || directDeposit_mailText.is_new())
		{
			log.warning("Cannot load Direct Deposit mail text MMailText[" + DirectDeposit_R_MailText_ID + "]");
			if (sb.length() > 0)
				sb.append(", ");
			sb.append("Cannot load Direct Deposit mail text MMailText[" + DirectDeposit_R_MailText_ID + "]");
		}
		
		MMailText directDebit_mailText = new MMailText(getCtx(), DirectDebit_R_MailText_ID, get_TrxName());
		if (directDebit_mailText == null || directDebit_mailText.get_ID() == 0 || directDebit_mailText.is_new())
		{
			log.warning("Cannot load Direct Debit mail text MMailText[" + DirectDebit_R_MailText_ID + "]");
			if (sb.length() > 0)
				sb.append(", ");
			sb.append("Cannot load Direct Debit mail text MMailText[" + DirectDebit_R_MailText_ID + "]");
		}
		
		MMailText onCredit_mailText = new MMailText(getCtx(), OnCredit_R_MailText_ID, get_TrxName());
		if (onCredit_mailText == null || onCredit_mailText.get_ID() == 0 || onCredit_mailText.is_new())
		{
			log.warning("Cannot load On Credit mail text MMailText[" + OnCredit_R_MailText_ID + "]");
			if (sb.length() > 0)
				sb.append(", ");
			sb.append("Cannot load On Credit mail text MMailText[" + OnCredit_R_MailText_ID + "]");
		}
		
		if (sb.length() > 0)
			return sb.toString();
		
		return null;
	}
	
	private String mailInvoices()
	{
		String sql = "SELECT * FROM " + MInvoice.Table_Name + " WHERE " + 
		   " AD_Client_ID=?" +
		   " AND AD_Org_ID = ?"  +
		   " AND IsActive='Y'" + 
		   " AND EmailSent IS NULL" + 
		   " AND C_DocTypeTarget_ID IN (SELECT C_DocType_ID FROM C_DocType WHERE DocBaseType='ARI')" +
		   " AND DocStatus='CO'";
		
		if (useInvoiceSendEmailFlag)
			sql += " AND UPPER(SendEmail)='Y'";
		
		if (useBusinessPartnerSendEmailFlag)
			sql += " AND C_BPartner_ID IN (SELECT C_BPartner_ID FROM C_BPartner WHERE UPPER(SendEmail)='Y')";
		
		// Load invoices
		ArrayList<MInvoice> invoices = new ArrayList<MInvoice>();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			// Create statement and set parameters
			pstmt = DB.prepareStatement(sql.toString(), get_TrxName());
			pstmt.setInt(1, AD_Client_ID);
			pstmt.setInt(2, AD_Org_ID);

			// Execute query and process result set
			rs = pstmt.executeQuery();
			while (rs.next())
				invoices.add(new MInvoice(getCtx(), rs, get_TrxName()));
		}
		catch (SQLException ex)
		{
			log.log(Level.SEVERE, sql.toString(), ex);
		}
		finally 
		{
			DB.close(rs, pstmt);
			rs = null; 
			pstmt = null;
		}
		
		// Check there are invoices to mail
		if (invoices.size() < 1)
			return "@Success@ There are no invoices to send.";
			
		// Load messages
		MMailText default_mailText = new MMailText(getCtx(), Default_R_MailText_ID, get_TrxName());
		MMailText cash_mailText = new MMailText(getCtx(), Cash_R_MailText_ID, get_TrxName());
		MMailText check_mailText = new MMailText(getCtx(), Check_R_MailText_ID, get_TrxName());
		MMailText creditCard_mailText = new MMailText(getCtx(), CreditCard_R_MailText_ID, get_TrxName());
		MMailText directDeposit_mailText = new MMailText(getCtx(), DirectDeposit_R_MailText_ID, get_TrxName());
		MMailText directDebit_mailText = new MMailText(getCtx(), DirectDebit_R_MailText_ID, get_TrxName());
		MMailText onCredit_mailText = new MMailText(getCtx(), OnCredit_R_MailText_ID, get_TrxName());
		
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
			log.log(Level.SEVERE, "Failed to create directory for Invoices - " + directory, ex);
			return "@Error@ Failed to create directory for Invoices - " + directory;
		}
		
		// Keep count of completed (sent) and failed invoices
		int countSuccess = 0;
		int countError = 0;
		
		// Loop through invoices
		for (MInvoice invoice : invoices)
		{
			for(MUser user : getContactsList(invoice.getC_BPartner_ID(), invoice))
			{

			// Validate email
			if (!isEmailValid(user.getEMail()))
			{
				addLog(getProcessInfo().getAD_Process_ID(), new Timestamp(System.currentTimeMillis()), null, invoice.getDocumentInfo() + " - Invalid email for " + user);
				continue;
			}
			
			// Don't send email, just list invoices
			if (listOnly)
			{
				addLog(getProcessInfo().getAD_Process_ID(), new Timestamp(System.currentTimeMillis()), null, invoice.getDocumentInfo());
				continue;
			}
				
				
				// Check if invoice already created
				String fileName = directory + invoice.getDocumentNo() + ".pdf";
				File file = new File(fileName);
				if (file.exists() && file.isFile() && file.length() > 2000)
					log.info("Existing: " + file + " - " + new Timestamp(file.lastModified()));
				else
				{
					log.info("New: " + fileName);
					//file = invoice.createPDF(file);
					try {
						saveFileFromUrlWithJavaIO(fileName,	"https://c-api.conversanthq.com/v2/invoices/"+invoice.getGUID()+"/pdf");
					} catch (MalformedURLException e) {
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
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
			
			String invoiceInfo ;
			invoiceInfo =  mailText.getMailText(true);
			if (invoiceInfo.contains(INVOICE_IDENTIFIER))
				invoiceInfo = invoiceInfo.replace(INVOICE_IDENTIFIER, invoice.getDocumentNo());
			if ( invoiceInfo.contains(PAYMENTMETHOD_IDENTIFIER))
			{
				if(invoice.getPaymentRule().equals(MInvoice.PAYMENTRULE_Cash))
					invoiceInfo = invoiceInfo.replace(PAYMENTMETHOD_IDENTIFIER,"Cash");
				else if (invoice.getPaymentRule().equals(MInvoice.PAYMENTRULE_Check))
					invoiceInfo = invoiceInfo.replace(PAYMENTMETHOD_IDENTIFIER,"Cheque");
				else if (invoice.getPaymentRule().equals(MInvoice.PAYMENTRULE_CreditCard))
					invoiceInfo = invoiceInfo.replace(PAYMENTMETHOD_IDENTIFIER,"Credit Card");
				else if (invoice.getPaymentRule().equals(MInvoice.PAYMENTRULE_DirectDeposit))
					invoiceInfo = invoiceInfo.replace(PAYMENTMETHOD_IDENTIFIER,"Direct Deposit");
				else if (invoice.getPaymentRule().equals(MInvoice.PAYMENTRULE_DirectDebit))
					invoiceInfo = invoiceInfo.replace(PAYMENTMETHOD_IDENTIFIER,"Direct Debit");
				else if (invoice.getPaymentRule().equals(MInvoice.PAYMENTRULE_OnCredit))
					invoiceInfo = invoiceInfo.replace(PAYMENTMETHOD_IDENTIFIER,"On Credit");
			}
			
			String sqlDueDate = "SELECT DUEDATE FROM C_INVOICEPAYSCHEDULE WHERE C_INVOICE_ID = ? AND DUEAMT > 0";
			Timestamp dueDate = DB.getSQLValueTS(get_TrxName(), sqlDueDate, invoice.get_ID());
			if(dueDate != null)
			{
				SimpleDateFormat dt1 = new SimpleDateFormat("dd-MM-yyyy"); 
		         sqlDueDate = dt1.format(dueDate); 
			}
			else 
				sqlDueDate = " ";
			if (invoiceInfo.contains(DUEDATE_IDENTIFIER))
				invoiceInfo = invoiceInfo.replace(DUEDATE_IDENTIFIER,sqlDueDate);
			// Check for null GUID
			boolean isGUIDEmpty = invoice.getGUID() == null || invoice.getGUID().trim().length() == 0;
			if (isGUIDEmpty)
			{
				addLog(getProcessInfo().getAD_Process_ID(), new Timestamp(System.currentTimeMillis()), null, invoice.getDocumentInfo() + " - GUID is empty . Please run Generate GUID process !!!" );
				continue;
			}
			if (invoiceInfo.contains(GUID_IDENTIFIER))
				invoiceInfo = invoiceInfo.replace(GUID_IDENTIFIER,invoice.getGUID());
			
			// Create email and add attachment
			EMail email = createEmail(user.getEMail(), mailText.getMailHeader(), invoiceInfo, mailText.isHtml());
			email.addAttachment(file);

			// Send email and store response
			String emailResponse = email.send();
			addLog(getProcessInfo().getAD_Process_ID(), new Timestamp(System.currentTimeMillis()), null, invoice.getDocumentInfo() + " - " + emailResponse);
			
			// Record email being sent/failure (Mail Template window, User Mail tab)
			MUserMail um = new MUserMail(mailText, user.getAD_User_ID(), email);
			um.save();
			
			// Set email sent field
			if (EMail.SENT_OK.equals(emailResponse))
			{
				invoice.set_CustomColumn("EmailSent", new Timestamp(System.currentTimeMillis()));
				invoice.setDatePrinted(new Timestamp(System.currentTimeMillis()));
				invoice.save();
				
				countSuccess++;
			}
			else
			{
				countError++;
			}
			}
		}		
		
		// Report counts
		return "@Completed@ = " + countSuccess + " - @Errors@ = " + countError + (listOnly ? " ** List Only" : "");
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
		
		if (client.isSmtpAuthorization())
		{
			email.createAuthenticator(client.getRequestUser(), client.getRequestUserPW());
		}

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
	
	public List<MUser> getContactsList(int m_C_BPartner_ID,MInvoice invoice)
	{
		List<MUser> contacts=new ArrayList<MUser>();
		String sql="SELECT COUNT(*) FROM AD_USER USR INNER JOIN AD_USER_ROLES USRROLE ON (USRROLE.AD_USER_ID=USR.AD_USER_ID) " +
				"INNER JOIN AD_ROLE ROLE ON (USRROLE.AD_ROLE_ID=ROLE.AD_ROLE_ID) WHERE USR.C_BPARTNER_ID = "+m_C_BPartner_ID +" AND USRROLE.ISACTIVE='Y' AND USR.ISACTIVE='Y' AND LOWER(ROLE.NAME)='billing contact' AND USR.C_BPARTNER_LOCATION_ID="+invoice.getC_BPartner_Location_ID();
		int no=DB.getSQLValue(null, sql);
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		if(no>0)
		{
			sql="SELECT DISTINCT USR.AD_USER_ID FROM AD_USER USR INNER JOIN AD_USER_ROLES USRROLE ON (USRROLE.AD_USER_ID=USR.AD_USER_ID) INNER JOIN AD_ROLE ROLE ON (USRROLE.AD_ROLE_ID=ROLE.AD_ROLE_ID) WHERE USR.C_BPARTNER_ID = ? AND USR.ISACTIVE='Y' AND LOWER(ROLE.NAME)='billing contact'  AND USRROLE.ISACTIVE='Y' AND USR.EMAIL IS NOT NULL AND USR.C_BPARTNER_LOCATION_ID="+invoice.getC_BPartner_Location_ID();
			try
			{
				pstmt = DB.prepareStatement (sql, null);
				pstmt.setInt (1, m_C_BPartner_ID);
				//pstmt.setInt (2, m_AD_Role_ID);
				rs = pstmt.executeQuery ();
				while (rs.next ())
				{
					contacts.add(new MUser (getCtx(), rs.getInt(1), null));
				}
	 		}
			catch (Exception e)
			{
				log.log(Level.SEVERE, sql, e);
			}
			finally
			{
				DB.close(rs, pstmt);
				rs = null; pstmt = null;
			}
		}
		else
		{
			sql="SELECT COUNT(*) FROM AD_USER USR INNER JOIN AD_USER_ROLES USRROLE ON (USRROLE.AD_USER_ID=USR.AD_USER_ID) " +
			"INNER JOIN AD_ROLE ROLE ON (USRROLE.AD_ROLE_ID=ROLE.AD_ROLE_ID) WHERE USR.C_BPARTNER_ID = "+m_C_BPartner_ID +" AND USRROLE.ISACTIVE='Y' AND USR.ISACTIVE='Y' AND LOWER(ROLE.NAME)='account administrator'AND USR.C_BPARTNER_LOCATION_ID="+invoice.getC_BPartner_Location_ID();
	        no=DB.getSQLValue(null, sql);
	        
	        if(no>0)
	        {
	        	sql="SELECT DISTINCT USR.AD_USER_ID FROM AD_USER USR INNER JOIN AD_USER_ROLES USRROLE ON (USRROLE.AD_USER_ID=USR.AD_USER_ID) INNER JOIN AD_ROLE ROLE ON (USRROLE.AD_ROLE_ID=ROLE.AD_ROLE_ID) WHERE USR.C_BPARTNER_ID = ? AND USR.ISACTIVE='Y' AND LOWER(ROLE.NAME)='account administrator'  AND USRROLE.ISACTIVE='Y' AND USR.EMAIL IS NOT NULL AND USR.C_BPARTNER_LOCATION_ID="+invoice.getC_BPartner_Location_ID();
				try
				{
					pstmt = DB.prepareStatement (sql, null);
					pstmt.setInt (1, m_C_BPartner_ID);
					rs = pstmt.executeQuery ();
					while (rs.next ())
					{
						contacts.add(new MUser (getCtx(), rs.getInt(1), null));
					}
		 		}
				catch (Exception e)
				{
					log.log(Level.SEVERE, sql, e);
				}
				finally
				{
					DB.close(rs, pstmt);
					rs = null; pstmt = null;
				}
	        }
	        else
	        {
	        	MUser user=MUser.get(getCtx(), invoice.getAD_User_ID());
	        	if (user == null || user.get_ID() == 0)
				{
					addLog(getProcessInfo().getAD_Process_ID(), new Timestamp(System.currentTimeMillis()), null, invoice.getDocumentInfo() + " - No user");
				}
	        	contacts.add(user);
	        }
		}
		return contacts;
	}
	
	// Using Java IO
	public static void saveFileFromUrlWithJavaIO(String fileName, String fileUrl)
			throws MalformedURLException, IOException {
		BufferedInputStream in = null;
		FileOutputStream fout = null;
		try {
			in = new BufferedInputStream(new URL(fileUrl).openStream());
			fout = new FileOutputStream(fileName);

			byte data[] = new byte[1024];
			int count;
			while ((count = in.read(data, 0, 1024)) != -1) 
			{
				fout.write(data, 0, count);
			}
		} finally 
		{
			if (in != null)
				in.close();
			if (fout != null)
				fout.close();
		}
	}
	 
}
