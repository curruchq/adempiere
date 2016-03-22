package com.conversant.process;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.logging.Level;

import org.compiere.model.MBPBankAccount;
import org.compiere.model.MInvoice;
import org.compiere.model.MInvoicePaySchedule;
import org.compiere.model.MPayment;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;
import org.compiere.util.Env;

//Braintree 
import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.Environment;
import com.braintreegateway.Result;
import com.braintreegateway.TransactionRequest;
import com.braintreegateway.Transaction;

public class BraintreeCreditCardProcessing extends SvrProcess 
{
	private int p_AD_Client_ID ; // Conversant
	private int p_AD_Org_ID ;
	//private boolean processedOK = false;
	private int countSuccess=0;
	private ArrayList<MInvoice> paySchedules = new ArrayList<MInvoice>();
	private String defaultMerchantAccount = null;
	
	@Override
	protected String doIt() throws Exception 
	{
		p_AD_Client_ID = Env.getAD_Client_ID(getCtx());
			
		p_AD_Org_ID = Env.getAD_Org_ID(getCtx());
		
		log.info("Entered doIt() of the Braintree Credit Card Process");
		
		try
		{
            BraintreeGateway gateway = getBraintreeGateway();
			getScheduledPayments();
			if(paySchedules.isEmpty())
			{
				addLog(getProcessInfo().getAD_Process_ID(), new Timestamp(System.currentTimeMillis()), null, "No invoices are scheduled to be processed today");
				return "0 invoices processed";
			}
			
			log.info("Loop through invoices and create transactions in Braintree");
			
			for(MInvoice invoice : paySchedules)
		    {	
				MBPBankAccount bpAcct = getBPBankAccount(invoice.getC_BPartner_ID(), invoice.getC_BPartner_Location_ID());
				if (bpAcct == null)
				{
					addLog(getProcessInfo().getAD_Process_ID(), new Timestamp(System.currentTimeMillis()), null, "No Bank Information for MInvoice [ "+invoice.getDocumentNo()+" ]");
					continue;
				}
				String paymentToken = bpAcct.getAccountNo();
				//String paymentToken = DB.getSQLValueString(null, "SELECT ACCOUNTNO FROM C_BP_BankAccount WHERE C_BPARTNER_ID = ? AND C_BPartner_Location_ID = ? ", invoice.getC_BPartner_ID(),invoice.getC_BPartner_Location_ID());
				TransactionRequest request = new TransactionRequest()
			    .paymentMethodToken(paymentToken).merchantAccountId(defaultMerchantAccount)
			    .amount(invoice.getGrandTotal())
			    .options()
			    	.submitForSettlement(true)
			    	.done();

			Result<Transaction> result = gateway.transaction().sale(request);
			Transaction transaction = result.getTarget();
			
			log.info("Transaction record created in Braintree");
			
			if (result.isSuccess())
			{
				String sql = "SELECT C_BANKACCOUNT_ID FROM C_BankAccount BA " +
						     "INNER JOIN C_BANK BNK ON (BNK.C_BANK_ID = BA.C_BANK_ID) " +
						     "INNER JOIN C_BP_BANKACCOUNT BPBA ON (BPBA.C_BANK_ID = BNK.C_BANK_ID)" +
						     " WHERE C_BPARTNER_ID = ?";
			    int bankId = DB.getSQLValue(null,sql , invoice.getC_BPartner_ID());

			    //Create Payment record
			    String sql1 = "SELECT DUEDATE FROM C_INVOICEPAYSCHEDULE WHERE C_INVOICE_ID = ? AND DUEAMT > 0";
			    Timestamp duedate = DB.getSQLValueTS(null, sql1, invoice.getC_Invoice_ID());
			    
				MPayment payment=new MPayment(getCtx(),0,null);
				payment.setDateAcct(duedate);
				payment.setDateTrx(duedate);
				payment.setPayAmt(invoice.getGrandTotal());
				payment.setC_Currency_ID(invoice.getC_Currency_ID());
				payment.setC_BPartner_ID(invoice.getC_BPartner_ID());
				payment.setC_Invoice_ID(invoice.getC_Invoice_ID());
				payment.setC_BankAccount_ID(bankId);
				payment.setTenderType("K");
				/*payment.setCreditCardNumber(bpAcct.getCreditCardNumber());
				payment.setCreditCardExpMM(bpAcct.getCreditCardExpMM());
				payment.setCreditCardExpYY(bpAcct.getCreditCardExpYY());
				payment.setCreditCardType(bpAcct.getCreditCardType());*/
				payment.setR_RespMsg(transaction.getStatus().toString());
				payment.setIsOnline(true);
				payment.setTrxType("S");
				payment.setC_DocType_ID(true);
				payment.setIsReceipt(true);
				payment.setIsApproved(true);
				if (!payment.save())
					log.severe("Automatic payment creation failure - payment not saved");
				
				log.info("Payment record created in Adempiere");
			}
			
			String msg="Transaction created for " +invoice.getDocumentNo();
			addLog(getProcessInfo().getAD_Process_ID(), new Timestamp(System.currentTimeMillis()), null, msg);
			
			countSuccess++;
		    }
		}
		catch (Exception ex)
		{
			log.severe(ex.getMessage());
			if (ex instanceof IllegalArgumentException)
				throw (IllegalArgumentException)ex;
		}
		
		log.info("Exiting doIt() of the Braintree Credit Card Process");
		return countSuccess + " invoices processed successfully";
		
	}

	@Override
	protected void prepare() {
		// TODO Auto-generated method stub

	}
	
	private void getScheduledPayments()
	{
		log.info("Getting Payments scheduled for current day");
		
		Calendar today=Calendar.getInstance();
		SimpleDateFormat dateFormat=new SimpleDateFormat("dd-MMM-yy");

		String sql="SELECT UNIQUE(PAYSCH.C_INVOICE_ID) FROM "+MInvoicePaySchedule.Table_Name + " PAYSCH " +
				"LEFT OUTER JOIN C_ALLOCATIONLINE PAY ON (PAYSCH.C_INVOICE_ID=PAY.C_INVOICE_ID) " +
				"INNER JOIN C_INVOICE INV ON (PAYSCH.C_INVOICE_ID=INV.C_INVOICE_ID) " +
				"INNER JOIN C_BPARTNER BP ON (BP.C_BPARTNER_ID = INV.C_BPARTNER_ID) " +
				"INNER JOIN C_BP_BANKACCOUNT BPBANKACCT ON (INV.C_BPARTNER_ID = BPBANKACCT.C_BPARTNER_ID) " +
				"INNER JOIN C_BANK BNK ON (BPBANKACCT.C_BANK_ID = BNK.C_BANK_ID) " +
				"INNER JOIN C_BANKACCOUNT BA ON (BA.C_BANK_ID = BNK.C_BANK_ID)" +
				"INNER JOIN C_PAYMENTPROCESSOR PAYPRO ON (PAYPRO.C_BANKACCOUNT_ID =  BA.C_BANKACCOUNT_ID)" +
				"WHERE PAYSCH.DUEDATE='"+dateFormat.format(today.getTime())+"' AND PAYSCH.PROCESSED='N' AND PAYSCH.DUEAMT >0 AND INV.DOCSTATUS='CO' " +
			    "AND PAYPRO.NAME LIKE 'Braintree%' AND INV.AD_CLIENT_ID = " +p_AD_Client_ID +" AND INV.AD_ORG_ID = "+p_AD_Org_ID;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{	
			// Create statement and set parameters
			pstmt = DB.prepareStatement(sql.toString(), get_TrxName());
			
			// Execute query and process result set
			rs = pstmt.executeQuery();
			
			while (rs.next())
				paySchedules.add(new MInvoice(getCtx(),rs.getInt(1),get_TrxName()));
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
		
		log.info(paySchedules.size()+" Payment schedules retrieved");
	}

	public BraintreeGateway getBraintreeGateway() 
	{
		log.info("Retrieving Braintree Credentials from Payment Processor Form");
		
		String merchantId =null;
		String context = null;
		String publicKey = null;
		String privateKey = null;
		
		/*String sql = "SELECT PAYPRO.HOSTADDRESS , PAYPRO.USERID , PAYPRO.PARTNERID , PAYPRO.VENDORID , PAYPRO.PROXYADDRESS  " +
				"FROM C_BANK BNK " +
				"INNER JOIN C_BANKACCOUNT ACCT ON (BNK.C_BANK_ID = ACCT.C_BANK_ID) " +
				"INNER JOIN C_PAYMENTPROCESSOR PAYPRO ON (PAYPRO.C_BANKACCOUNT_ID = ACCT.C_BANKACCOUNT_ID) " +
				"WHERE BNK.AD_ORG_ID = " + p_AD_Org_ID + " AND  UPPER(BNK.NAME) LIKE 'BRAINTREE%'";*/
		
		String sql = "SELECT PAYPRO.HOSTADDRESS , PAYPRO.USERID , PAYPRO.PARTNERID , PAYPRO.VENDORID , PAYPRO.PROXYADDRESS  FROM C_PAYMENTPROCESSOR PAYPRO " +
				     "WHERE PAYPRO.AD_ORG_ID = " + p_AD_Org_ID + " AND  UPPER(PAYPRO.NAME) LIKE 'BRAINTREE%'";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{	
			// Create statement and set parameters
			pstmt = DB.prepareStatement(sql.toString(), get_TrxName());
			
			// Execute query and process result set
			rs = pstmt.executeQuery();
			while (rs.next())
			{
				context = rs.getString(1);
				merchantId = rs.getString(2);
				publicKey = rs.getString(3);
				privateKey = rs.getString(4);
				defaultMerchantAccount =  rs.getString(5);
			}
				
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
		if (context.contentEquals("SANDBOX"))
			return new com.braintreegateway.BraintreeGateway(Environment.SANDBOX,merchantId,publicKey,privateKey);
		else if (context.contentEquals("PRODUCTION"))
			return new BraintreeGateway(Environment.PRODUCTION,merchantId,publicKey,privateKey);
		
		log.info("ENVIRONMENT :: "+context);
		log.info("MERCHANT ID :: "+merchantId);
		log.info("PUBLIC KEY :: "+publicKey);
		log.info("PRIVATE KEY :: "+privateKey);
		
		return null;
    }
	
	private MBPBankAccount getBPBankAccount(int bp_ID , int bp_location_ID)
	{
		log.info("Getting Bank Account Details from Business Partner Form");
		
		MBPBankAccount bpBnkAcct = null;
		String sql_new = "SELECT * FROM C_BP_BANKACCOUNT WHERE C_BPARTNER_ID = ? AND C_BPartner_Location_ID =  ?";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{	
			// Create statement and set parameters
			pstmt = DB.prepareStatement(sql_new.toString(), get_TrxName());
			pstmt.setInt(1, bp_ID);
			pstmt.setInt(2, bp_location_ID);
			// Execute query and process result set
			rs = pstmt.executeQuery();
			while (rs.next())
			{
				bpBnkAcct = new MBPBankAccount(getCtx(),rs,get_TrxName());
			}
				
		}
		catch (SQLException ex)
		{
			log.log(Level.SEVERE, sql_new.toString(), ex);
		}
		finally 
		{
			DB.close(rs, pstmt);
			rs = null; 
			pstmt = null;
		}
		
		log.info("Business Partner Bank Account [ "+bpBnkAcct.get_ID()+" ]");
		
		return bpBnkAcct;
	}
}
