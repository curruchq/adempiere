package com.conversant.process;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;

import org.compiere.model.MBPBankAccount;
import org.compiere.model.MInvoice;
import org.compiere.model.MInvoiceLine;
import org.compiere.model.MInvoicePaySchedule;
import org.compiere.model.MPayment;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;

/** Braintree jars*/
import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.Environment;
import com.braintreegateway.Result;
import com.braintreegateway.TransactionRequest;
import com.braintreegateway.Transaction;

public class BraintreeCreditCardProcessing extends SvrProcess 
{
	/** Logger 																	*/
	private static CLogger log = CLogger.getCLogger(BraintreeCreditCardProcessing.class);
	/** Conversant Client														*/
	private int p_AD_Client_ID = 1000000; 
	
	/** Conversant Org															*/
	private int p_AD_Org_ID ; 
	//private boolean processedOK = false;
	private int countSuccess=0;
	private ArrayList<MInvoicePaySchedule> paySchedules = new ArrayList<MInvoicePaySchedule>();
	private String defaultMerchantAccount = null;
	private static final int DOCTYPE_PREPAID_GOODS_ID = 1000145;
	
	@Override
	protected String doIt() throws Exception 
	{
		/*p_AD_Client_ID = Env.getAD_Client_ID(getCtx());
			
		p_AD_Org_ID = Env.getAD_Org_ID(getCtx());*/
		
		log.log( Level.INFO, "Entered doIt() of the Braintree Credit Card Process");
		
		try
		{
            HashMap<Integer, BraintreeGateway> gatewayList = getBraintreeGateway();
            Iterator<Integer> keySetIterator = gatewayList.keySet().iterator(); 
            while(keySetIterator.hasNext())
            { 
            	Integer key = keySetIterator.next(); 
            	BraintreeGateway gateway = gatewayList.get(key);
           
	            if(gateway == null)
	            {
					addLog(getProcessInfo().getAD_Process_ID(), new Timestamp(System.currentTimeMillis()), null, "NULL Gateway !!!! Payment processor issue");
					return "GATEWAY ERROR!!!!";
				}
	            	
				getScheduledPayments(key);
				if(paySchedules.isEmpty())
				{
					addLog(getProcessInfo().getAD_Process_ID(), new Timestamp(System.currentTimeMillis()), null, "No invoices are scheduled to be processed today for Organization "+ key);
					//return "0 invoices processed";
				}
				
				log.log( Level.INFO, "Loop through invoices and create transactions in Braintree");
				
				for(MInvoicePaySchedule invoicePaySchedule : paySchedules)
			    {	
					MInvoice invoice = new MInvoice(getCtx(),invoicePaySchedule.getC_Invoice_ID(),get_TrxName());
					boolean paymentMade = isPaymentCreated(invoice.get_ID());
					if(!paymentMade)
					{
						log.log(Level.INFO , "First Invoice to be processed is MInvoice [ "+invoice.getDocumentNo()+" ]");
						
						MBPBankAccount bpAcct = getBPBankAccount(invoice.getC_BPartner_ID(), invoice.getC_BPartner_Location_ID());
						if (bpAcct == null)
						{
							addLog(getProcessInfo().getAD_Process_ID(), new Timestamp(System.currentTimeMillis()), null, "No Bank Information for MInvoice [ "+invoice.getDocumentNo()+" ]");
							continue;
						}
						String paymentToken = bpAcct.getA_Name();
						TransactionRequest request = new TransactionRequest()
					    .paymentMethodToken(paymentToken).merchantAccountId(defaultMerchantAccount)
					    .amount(invoicePaySchedule.getDueAmt())
					    .options()
					    	.submitForSettlement(true)
					    	.done();
		
					Result<Transaction> result = gateway.transaction().sale(request);
					String transactionMessage = result.getMessage();
					if(transactionMessage != null)
					{
						addLog(getProcessInfo().getAD_Process_ID(), new Timestamp(System.currentTimeMillis()), null, "Braintree Transaction Message MInvoice [ "+invoice.getDocumentNo()+" ] , "+transactionMessage);
					}
					
					Transaction transaction = result.getTarget();
					if(transaction == null)
					{
						addLog(getProcessInfo().getAD_Process_ID(), new Timestamp(System.currentTimeMillis()), null, "Braintree Transaction not created for  MInvoice [ "+invoice.getDocumentNo()+" ]");
						continue;
					}
					
					if (result.isSuccess())
					{
						log.log( Level.INFO, "Transaction record created in Braintree");
						
						String sql = "SELECT C_BANKACCOUNT_ID FROM C_BankAccount BA " +
								     "INNER JOIN C_BANK BNK ON (BNK.C_BANK_ID = BA.C_BANK_ID) " +
								     "INNER JOIN C_BP_BANKACCOUNT BPBA ON (BPBA.C_BANK_ID = BNK.C_BANK_ID)" +
								     " WHERE BPBA.C_BPARTNER_ID = ?";
					    int bankId = DB.getSQLValue(null,sql , invoice.getC_BPartner_ID());
		
					    //Create Payment record
					    String sql1 = "SELECT DUEDATE FROM C_INVOICEPAYSCHEDULE WHERE C_INVOICE_ID = ? AND DUEAMT > 0";
					    Timestamp duedate = DB.getSQLValueTS(null, sql1, invoice.getC_Invoice_ID());
					    
						MPayment payment=new MPayment(getCtx(),0,null);
						payment.setDocumentNo(transaction.getId());
						payment.setDateAcct(duedate);
						payment.setDateTrx(duedate);
						payment.setPayAmt(invoicePaySchedule.getDueAmt());
						payment.setC_Currency_ID(invoice.getC_Currency_ID());
						payment.setC_BPartner_ID(invoice.getC_BPartner_ID());
						payment.setC_Invoice_ID(invoice.getC_Invoice_ID());
						payment.setC_BPartner_Location_ID(invoice.getC_BPartner_Location_ID());
						payment.setC_BankAccount_ID(bankId);
						payment.setTenderType("K");
						payment.setAD_Org_ID(key);
						payment.setR_RespMsg(transaction.getStatus().toString());
						payment.setIsOnline(true);
						payment.setTrxType("S");
						payment.setC_DocType_ID(true);
						payment.setIsReceipt(true);
						payment.setIsApproved(true);
						
						if (!payment.save())
							log.severe("Automatic payment creation failure - payment not saved");
						
						log.log( Level.INFO, "Payment record created in Adempiere : "+ payment.getDocumentNo()+" - "+payment.get_ID());
					}
					String msg="Transaction created for " +invoice.getDocumentNo();
					addLog(getProcessInfo().getAD_Process_ID(), new Timestamp(System.currentTimeMillis()), null, msg);
			    }
				else
					addLog(getProcessInfo().getAD_Process_ID(), new Timestamp(System.currentTimeMillis()), null, "Payment is already made for MInvoice [ "+ invoice.get_ID()+" ]");
				
				countSuccess++;
			    }
				paySchedules.clear();
            }//Iterator
		}//try
		catch (Exception ex)
		{
			log.severe(ex.getMessage());
			if (ex instanceof IllegalArgumentException)
				throw (IllegalArgumentException)ex;
		}
		
		
		log.log( Level.INFO, "Exiting doIt() of the Braintree Credit Card Process");
		return countSuccess + " invoices processed successfully";
		
	}

	@Override
	protected void prepare()
	{
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (para[i].getParameter() == null)
				;
			else if (name.equals("AD_Org_ID"))
			{
				p_AD_Org_ID = ((BigDecimal)para[i].getParameter()).intValue();
			}
			else
			{
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
			}
		}	

	}
	
	private void getScheduledPayments(int m_AD_Org_ID)
	{
		log.log( Level.INFO, "Getting Payments scheduled for current day");
		
		Calendar today=Calendar.getInstance();
		SimpleDateFormat dateFormat=new SimpleDateFormat("dd-MMM-yy");

		String sql="SELECT UNIQUE(PAYSCH.C_InvoicePaySchedule_ID) FROM "+MInvoicePaySchedule.Table_Name + " PAYSCH " +
				"LEFT OUTER JOIN C_ALLOCATIONLINE PAY ON (PAYSCH.C_INVOICE_ID=PAY.C_INVOICE_ID) " +
				"INNER JOIN C_INVOICE INV ON (PAYSCH.C_INVOICE_ID=INV.C_INVOICE_ID) " +
				"INNER JOIN C_BPARTNER BP ON (BP.C_BPARTNER_ID = INV.C_BPARTNER_ID) " +
				"INNER JOIN C_BP_BANKACCOUNT BPBANKACCT ON (INV.C_BPARTNER_ID = BPBANKACCT.C_BPARTNER_ID) " +
				"INNER JOIN C_BANK BNK ON (BPBANKACCT.C_BANK_ID = BNK.C_BANK_ID) " +
				"INNER JOIN C_BANKACCOUNT BA ON (BA.C_BANK_ID = BNK.C_BANK_ID) " +
				"INNER JOIN C_PAYMENTPROCESSOR PAYPRO ON (PAYPRO.C_BANKACCOUNT_ID =  BA.C_BANKACCOUNT_ID) " +
				"WHERE PAYSCH.DUEDATE='"+dateFormat.format(today.getTime())+"' AND PAYSCH.PROCESSED='N' AND PAYSCH.DUEAMT >0 AND INV.DOCSTATUS='CO' AND INV.ISPAID = 'N' " +
			    "AND PAYPRO.NAME LIKE 'Braintree%' AND INV.AD_CLIENT_ID = " +p_AD_Client_ID +" AND INV.AD_ORG_ID = "+m_AD_Org_ID+" AND INV.PAYMENTRULE = 'K' " +  
			    " AND INV.IsSOTrx='Y' AND INV.C_DOCTYPE_ID != "+DOCTYPE_PREPAID_GOODS_ID;
		
		log.log( Level.INFO, "SQL to retrieve Invoices :: "+sql);
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{	
			// Create statement and set parameters
			pstmt = DB.prepareStatement(sql.toString(), get_TrxName());
			
			// Execute query and process result set
			rs = pstmt.executeQuery();
			
			while (rs.next())
				paySchedules.add(new MInvoicePaySchedule(getCtx(),rs.getInt(1),get_TrxName()));
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
		
		log.log( Level.INFO, paySchedules.size()+" Payment schedules retrieved");
	}

	public HashMap<Integer , BraintreeGateway> getBraintreeGateway() 
	{
		log.log( Level.INFO, "Retrieving Braintree Credentials from Payment Processor Form");
		
		String merchantId =null;
		String context = null;
		String publicKey = null;
		String privateKey = null;
		int org_id ;
		
		HashMap<Integer , BraintreeGateway> hMap = new HashMap<Integer , BraintreeGateway>();
		/*String sql = "SELECT PAYPRO.HOSTADDRESS , PAYPRO.USERID , PAYPRO.PARTNERID , PAYPRO.VENDORID , PAYPRO.PROXYADDRESS  " +
				"FROM C_BANK BNK " +
				"INNER JOIN C_BANKACCOUNT ACCT ON (BNK.C_BANK_ID = ACCT.C_BANK_ID) " +
				"INNER JOIN C_PAYMENTPROCESSOR PAYPRO ON (PAYPRO.C_BANKACCOUNT_ID = ACCT.C_BANKACCOUNT_ID) " +
				"WHERE BNK.AD_ORG_ID = " + p_AD_Org_ID + " AND  UPPER(BNK.NAME) LIKE 'BRAINTREE%'";*/
		
		String sql = "SELECT PAYPRO.HOSTADDRESS , PAYPRO.USERID , PAYPRO.PARTNERID , PAYPRO.VENDORID , PAYPRO.PROXYADDRESS , PAYPRO.AD_ORG_ID FROM C_PAYMENTPROCESSOR PAYPRO " +
				     "WHERE UPPER(PAYPRO.NAME) LIKE 'BRAINTREE%' AND PAYPRO.ISACTIVE = 'Y' " ;
		if (p_AD_Org_ID > 0)
				     sql += " AND PAYPRO.AD_ORG_ID = " + p_AD_Org_ID ;
		
		log.log( Level.INFO, "SQL to retrieve Braintree Info :: "+sql);
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
				org_id = rs.getInt(6);
				
				log.log( Level.INFO, "ENVIRONMENT :: "+context);
				log.log( Level.INFO, "MERCHANT ID :: "+merchantId);
				log.log( Level.INFO, "PUBLIC KEY :: "+publicKey);
				log.log( Level.INFO, "PRIVATE KEY :: "+privateKey);
				
				if (context.contentEquals("SANDBOX"))
					hMap.put(org_id, new BraintreeGateway(Environment.SANDBOX,merchantId,publicKey,privateKey));
				else if (context.contentEquals("PRODUCTION"))
					hMap.put(org_id, new BraintreeGateway(Environment.PRODUCTION,merchantId,publicKey,privateKey));
				
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
		
		/*log.log( Level.INFO, "ENVIRONMENT :: "+context);
		log.log( Level.INFO, "MERCHANT ID :: "+merchantId);
		log.log( Level.INFO, "PUBLIC KEY :: "+publicKey);
		log.log( Level.INFO, "PRIVATE KEY :: "+privateKey);
		
		if (context.contentEquals("SANDBOX"))
			return new BraintreeGateway(Environment.SANDBOX,merchantId,publicKey,privateKey);
		else if (context.contentEquals("PRODUCTION"))
			return new BraintreeGateway(Environment.PRODUCTION,merchantId,publicKey,privateKey);*/
		
		return hMap;
    }
	
	private MBPBankAccount getBPBankAccount(int bp_ID , int bp_location_ID)
	{
		log.log( Level.INFO, "Getting Bank Account Details from Business Partner Form");
		
		MBPBankAccount bpBnkAcct = null;
		String sql_new = null;
		
		if (bp_location_ID > 0)
			sql_new = "SELECT * FROM C_BP_BANKACCOUNT WHERE C_BPARTNER_ID = ? AND C_BPartner_Location_ID =  ? AND ISACTIVE='Y'";
		
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
			
			if (bpBnkAcct == null)
			{
				String s = "SELECT * FROM C_BP_BANKACCOUNT WHERE C_BPARTNER_ID = ? AND C_BPartner_Location_ID IS NULL  AND ISACTIVE='Y'";
				int bp_BnkAcct_ID = DB.getSQLValue(get_TrxName(), s,bp_ID);
				bpBnkAcct = new MBPBankAccount(getCtx(),bp_BnkAcct_ID,get_TrxName());
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
		
		log.log( Level.INFO, "Business Partner Bank Account [ "+bpBnkAcct.get_ID()+" ]");
		
		return bpBnkAcct;
	}
	
	private boolean isPaymentCreated(int p_C_Invoice_ID)
	{
		String sql =  "SELECT COUNT(*) FROM C_PAYMENT PAY " +
				      "INNER JOIN  C_INVOICEPAYSCHEDULE SCH ON (SCH.C_INVOICE_ID = PAY.C_INVOICE_ID) " +
				      "WHERE PAY.C_INVOICE_ID = ? AND PAY.PAYAMT = SCH.DUEAMT AND SCH.DUEAMT > 0 ";
		
		int count = DB.getSQLValue(get_TrxName(), sql, p_C_Invoice_ID);
		if (count > 0)
			return true;
		
		return false;
	}
}
