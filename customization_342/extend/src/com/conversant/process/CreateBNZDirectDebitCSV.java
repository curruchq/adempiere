package com.conversant.process;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.text.DateFormat;

import org.compiere.model.I_C_BPartner;
import org.compiere.model.I_C_Invoice;
import org.compiere.model.MBPartner;
import org.compiere.model.MInvoice;
import org.compiere.model.MBank;
import org.compiere.model.MInvoiceLine;
import org.compiere.model.MInvoicePaySchedule;
import org.compiere.model.MBankAccount;
import org.compiere.model.MPayment;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.TimeUtil;

/**
 * @author lnandyal
 * 
 */
public class CreateBNZDirectDebitCSV extends SvrProcess {
	
	//Delimiter used in CSV file
	private static final String COMMA_DELIMITER = ",";
	private static final String NEW_LINE_SEPARATOR = "\n";
	
	/** Logger */
	private static CLogger log = CLogger.getCLogger(CreateBNZDirectDebitCSV.class);

	/** Conversant Client */
	private int p_AD_Client_ID = 1000000;

	/** Conversant Org */
	private int p_AD_Org_ID = 1000001;
	
	/**Default Bank Account for BNZ*/
	private int p_C_BankAccount_ID;
	
	/**Bank */
	private int p_C_Bank_ID;

	@Override
	protected String doIt() throws Exception {
		// TODO Auto-generated method stub

		File BNZDDFile = new File("/Adempiere/BNZDirectDebit");
		if(!BNZDDFile.exists())
			BNZDDFile.mkdir();
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MMM-dd");
        String todaysDate = dateFormat.format(System.currentTimeMillis());
		String fileName ="/Adempiere/BNZDirectDebit/BNZDD" + todaysDate+".csv";
		File newFile = new File(fileName);
			newFile.createNewFile();
		
		// Set client and org (useful when run via scheduler)
		int originalAD_Client_ID = Env.getAD_Client_ID(getCtx());
		Env.setContext(getCtx(), "#AD_Client_ID", p_AD_Client_ID);
		
		int originalAD_Org_ID = Env.getAD_Org_ID(getCtx());
		Env.setContext(getCtx(), "#AD_Org_ID", p_AD_Org_ID);
		try
		{
			// Validate parameters
			String msg = validate();
			if(msg==null)
			{
					msg=createBNZDDCSV(fileName);
			}
			else
				return msg;
			//addLog(getProcessInfo().getAD_Process_ID(), new Timestamp(System.currentTimeMillis()), null, msg);
			return "BNZ Direct Debit file created successfully";
		}
		catch (Exception ex)
		{
			throw ex;
		}
		finally
		{
			// Reset client and org 
			Env.setContext(getCtx(), "#AD_Client_ID", originalAD_Client_ID);
			Env.setContext(getCtx(), "#AD_Org_ID", originalAD_Org_ID);
		}	
		
	}

	@Override
	protected void prepare() {
		// TODO Auto-generated method stub
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++) {
			String name = para[i].getParameterName();
			if (para[i].getParameter() == null)
				;
			else if (name.equals("AD_Client_ID")) {
				p_AD_Client_ID = ((BigDecimal) para[i].getParameter()).intValue();
			} else if (name.equals("AD_Org_ID")) {
				p_AD_Org_ID = ((BigDecimal) para[i].getParameter()).intValue();
			} else if (name.equals("C_Bank_ID")) {
				p_C_Bank_ID = ((BigDecimal) para[i].getParameter())
						.intValue();
			} 
			else {
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
			}
		}
	}
	
	private String validate()
	{
		StringBuilder sb = new StringBuilder();
		if(p_C_Bank_ID > 0)
		{
			MBank bp=new MBank(getCtx(),p_C_Bank_ID,get_TrxName());
			if(bp == null || bp.get_ID() == 0)
			{
				if (sb.length() > 0)
					sb.append(", ");
				
				sb.append("Cannot load Bank [" + p_C_Bank_ID + "]");
			}
			
		}
		if (sb.length() > 0)
			return "@Error@" + sb.toString();

		return null;
	}
	
	private String createBNZDDCSV(String fileName)
	{
		String retValue = "";
		FileWriter fileWriter=null;
		MBankAccount ba=null;
		String sql="SELECT * FROM C_BANKACCOUNT WHERE C_BANK_ID =? AND ISDEFAULT='Y'";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			// Create statement and set parameters
			pstmt = DB.prepareStatement(sql.toString(), get_TrxName());
			pstmt.setInt(1, p_C_Bank_ID);
			
			// Execute query and process result set
			rs = pstmt.executeQuery();
			while (rs.next())
				ba=new MBankAccount(getCtx(),rs,get_TrxName());
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
		try
		{
			
			//Create BNZ Header Record
			fileWriter = new FileWriter(fileName);
			fileWriter.append('1'); // Record Type
			fileWriter.append(COMMA_DELIMITER);
			fileWriter.append(ba.getBBAN()); //Direct Debit Authority Number
			fileWriter.append(COMMA_DELIMITER);
			//fileWriter.append(' ');  //Spare
			fileWriter.append(COMMA_DELIMITER);
			//fileWriter.append(' ');  //Spare
			fileWriter.append(COMMA_DELIMITER);
			fileWriter.append(ba.getAccountNo()); //Originator Account Number
			fileWriter.append(COMMA_DELIMITER);
			fileWriter.append('6');  //File Type
			fileWriter.append(COMMA_DELIMITER);
			DateFormat dateFormat = new SimpleDateFormat("yyMMdd");
	        String fileDueDate = dateFormat.format(System.currentTimeMillis());
			fileWriter.append(fileDueDate); //File Due Date 
			fileWriter.append(COMMA_DELIMITER);
	        String fileCreationDate = dateFormat.format(System.currentTimeMillis());
			fileWriter.append(fileCreationDate); // File Creation Date
			fileWriter.append(COMMA_DELIMITER);
			fileWriter.append('I'); //Bulk or Individual Listing Indicator
			fileWriter.append(NEW_LINE_SEPARATOR);
			
			BigDecimal totalTrxAmount=Env.ZERO;
			int count =0;
			Long hashtotal=Env.ZERO.longValue();
			//Create BNZ Transaction Records
			for(MInvoicePaySchedule payableInvoices:getInvoicesScheduledForPayment())
			{  	
				I_C_Invoice invoice=payableInvoices.getC_Invoice();
				I_C_BPartner bp=invoice.getC_BPartner();
				
				//create a payment record for each invoice
				String s="SELECT COUNT(*) FROM C_PAYMENT WHERE C_INVOICE_ID=?";
				int cnt=DB.getSQLValue(get_TrxName(), s, invoice.getC_Invoice_ID());
				if(cnt==0)
				{
					MPayment payment = new MPayment(getCtx(), 0,null);
	
					//payment.setDocumentNo(paymentid.getTextContent());
					payment.setDescription("BNZ Direct Debit Transaction ");
					payment.setDateAcct(payableInvoices.getDueDate());
					payment.setDateTrx(payableInvoices.getDueDate());
					payment.setPayAmt(payableInvoices.getDueAmt());
					payment.setC_Currency_ID(invoice.getC_Currency_ID());
					payment.setC_BPartner_ID(invoice.getC_BPartner_ID());
					payment.setC_Invoice_ID(invoice.getC_Invoice_ID());
					payment.setC_BankAccount_ID(1000000);
					payment.setTenderType("D");
					payment.setC_DocType_ID(true);
					if (!payment.save())
						log.warning("Automatic payment creation failure - payment not saved");
			    }
				
				fileWriter.append('2'); //Record Type
				fileWriter.append(COMMA_DELIMITER);
				String sql1="SELECT ACCOUNTNO FROM C_BP_BANKACCOUNT WHERE ISACH='Y' AND C_BPARTNER_ID =? AND ISACTIVE='Y'";
				String account=DB.getSQLValueString(null, sql1, invoice.getC_BPartner_ID());
				if(account.length()>=15)
				{
					String temp=account.substring(2, 13);
					hashtotal=hashtotal+new Long(temp);
				}
				fileWriter.append(account);  //Other party bank account number
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append("00"); //Transaction code
				fileWriter.append(COMMA_DELIMITER);
				BigDecimal transactionAmount=invoice.getGrandTotal().multiply(Env.ONEHUNDRED);
				Integer tempInt=transactionAmount.intValue();
				fileWriter.append(tempInt.toString());//Transaction amount
				fileWriter.append(COMMA_DELIMITER);
				
				fileWriter.append((bp.getName().length()>20) ? bp.getName().substring(0,20) : bp.getName()); //Other party name
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(invoice.getDocumentNo()); //Other party reference
				fileWriter.append(COMMA_DELIMITER);
				//fileWriter.append(''); //Other party code
				fileWriter.append(COMMA_DELIMITER);
				//fileWriter.append(''); //Other party alpha reference
				fileWriter.append(COMMA_DELIMITER);
				//fileWriter.append('');  //Other party particulars
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append("Conversant"); //Originator name
				fileWriter.append(COMMA_DELIMITER);
				//fileWriter.append(''); //Originator code
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(invoice.getDocumentNo());  //Originator reference
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(bp.getValue()); //Originator particulars
				fileWriter.append(NEW_LINE_SEPARATOR);
				totalTrxAmount=totalTrxAmount.add(transactionAmount);
				count++;
				
				
			}
			
			//Create BNZ Trailer Record
			fileWriter.append('3'); //Record Type
			fileWriter.append(COMMA_DELIMITER);
			Integer tempInt=totalTrxAmount.intValue();
			fileWriter.append(tempInt.toString()); //Total transaction amount
			fileWriter.append(COMMA_DELIMITER);
			fileWriter.append(String.valueOf(count)); // Transaction record count
			fileWriter.append(COMMA_DELIMITER);
			String str=new String(hashtotal.toString());
			if(str.length() < 11)
				str=String.format("%011d", Long.parseLong(str));
			else if(str.length() > 11)
				str = str.substring(1);
			fileWriter.append(str); // Hash Total

			
			
		}
		catch(IOException ioe)
		{
			log.log(Level.SEVERE,"Unable to write to file", ioe);
		}
		catch(Exception e)
		{
			log.log(Level.SEVERE,"Unable to write to file", e);
		}
		finally
		{
			try
			{
				fileWriter.flush();
				fileWriter.close();
			}
			catch(IOException ioe)
			{
				log.log(Level.SEVERE,"Error while flushing/closing fileWriter !!!", ioe);
			}
		}
		return null;
	}
	
	private List<MInvoicePaySchedule> getInvoicesScheduledForPayment()
	{
		// Load invoice(s)
		Calendar today=Calendar.getInstance();
		SimpleDateFormat dateFormat=new SimpleDateFormat("dd-MMM-yy");
		ArrayList<MInvoicePaySchedule> paySchedules = new ArrayList<MInvoicePaySchedule>();		
			
			String sql = "SELECT INVSCH.C_InvoicePaySchedule_ID FROM " + MInvoicePaySchedule.Table_Name + " INVSCH ";
			sql+=" LEFT OUTER JOIN C_ALLOCATIONLINE PAY ON (INVSCH.C_INVOICE_ID=PAY.C_INVOICE_ID)";
			sql+=" INNER JOIN C_INVOICE INV ON (INVSCH.C_INVOICE_ID = INV.C_INVOICE_ID) ";
			sql+=" INNER JOIN C_BPARTNER BP ON (BP.C_BPARTNER_ID=INV.C_BPARTNER_ID) ";
			sql+=" INNER JOIN C_BP_BANKACCOUNT BNKACCT ON (BP.C_BPARTNER_ID=BNKACCT.C_BPARTNER_ID) ";
			sql+=" WHERE INV.AD_Client_ID=?" + // 1
			   " AND INV.Processing='N' AND INV.PaymentRule='D'" + 
			   //" AND INV.Posted='N' " + 
			   " AND INV.IsActive='Y'" + 
			   " AND INV.DocStatus='CO' AND BNKACCT.C_Bank_ID=? AND INVSCH.DUEAMT>0"  //2  
			   +" AND INVSCH.DUEDATE=' "+dateFormat.format(today.getTime()) +"'";//3

			PreparedStatement pstmt = null;
			ResultSet rs = null;
			try
			{
				// Create statement and set parameters
				pstmt = DB.prepareStatement(sql.toString(), get_TrxName());
				pstmt.setInt(1, p_AD_Client_ID);
				pstmt.setInt(2, p_C_Bank_ID);
				
				// Execute query and process result set
				rs = pstmt.executeQuery();
				while (rs.next())
					paySchedules.add(new MInvoicePaySchedule(getCtx(), rs.getInt(1), get_TrxName()));
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
		return paySchedules;
	}
}
