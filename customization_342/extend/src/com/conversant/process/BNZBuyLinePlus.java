package com.conversant.process;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.logging.Level;

import org.compiere.process.SvrProcess;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Ini;

import webpay.client.Webpay;

import org.compiere.model.MPayment;
import org.compiere.model.MPaymentProcessor;
import org.compiere.model.MPaymentValidate;
import org.compiere.model.MBPBankAccount;
import org.compiere.model.MInvoicePaySchedule;
import org.compiere.model.I_C_Invoice;


public class BNZBuyLinePlus extends SvrProcess{
	/** Logger										*/
	private static CLogger log = CLogger.getCLogger(BNZBuyLinePlus.class);
private static final int WEBPAY_DEBUG_LEVEL = 0; // 0 = off, 1 = lowest, 3 = highest
	
	private static final int MAX_STATUS_REQUESTS = 3;
	private static final int POLLING_DELAY_MS = 500;
	
	private static final int MIN_CARDDATA_LENGTH = 12;
	private static final int MAX_MERCHANT_CARDHOLDERNAME_LENGTH = 50;
	private int AD_Client_ID = 1000000; // Conversant
	
	// Request fields
	private static final String REQ_TRANSACTIONTYPE = "TRANSACTIONTYPE";	
	private static final String REQ_TRANSACTIONTYPE_PURCHASE = "PURCHASE";
	private static final String REQ_TRANSACTIONTYPE_STATUS = "STATUS";
	private static final String REQ_TRANSACTIONTYPE_VOID = "VOID";
	
	private static final String REQ_INTERFACE = "INTERFACE";		
	private static final String REQ_INTERFACE_CREDITCARD = "CREDITCARD";
	
	private static final String REQ_TOTALAMOUNT = "TOTALAMOUNT";
	private static final String REQ_CARDDATA = "CARDDATA";
	private static final String REQ_CARDEXPIRYDATE = "CARDEXPIRYDATE";
	private static final String REQ_MERCHANT_CARDHOLDERNAME = "MERCHANT_CARDHOLDERNAME";
	private static final String REQ_ORIGINALTXNREF = "ORIGINALTXNREF";
	
	private static final String REQ_CVC2 = "CVC2";
	private static final int REQ_CVC2_MIN_LENGTH = 3;
	private static final int REQ_CVC2_MAX_LENGTH = 6;
	
	private static final String REQ_CCI = "CCI";
	private static final String REQ_CCI_ENTERED = "1";
	private static final String REQ_CCI_ILLEGIBLE = "2";
	private static final String REQ_CCI_NO_SECURE_ID = "9";
	
	// Response fields
	private static final String RES_TXNREFERENCE = "TXNREFERENCE";
	private static final String RES_RESPONSECODE = "RESPONSECODE";
	private static final String RES_RESPONSETEXT = "RESPONSETEXT";
	private static final String RES_AUTHCODE = "AUTHCODE";
	private static final String RES_CVC2RESPONSE = "CVC2RESPONSE";
	private static final String RES_ERROR = "ERROR";
	
	// Response codes
	private static final String RC_INPROGRESS = "IP";
	private static final String RC_NUM_INPROGRESS = "88";
	private static final String RC_ACCEPTED = "00";
	private static final String RC_ACCEPTED_WITH_SIG = "08";
	
	private boolean processedOK = false;
	private ArrayList<MInvoicePaySchedule> paySchedules = new ArrayList<MInvoicePaySchedule>();
	
	
	@Override
	protected String doIt() throws Exception {
		
		try
		{

			Webpay webpayClient = createWebpayClient();
			
			getScheduledPayments();
		    for(MInvoicePaySchedule bnz : paySchedules)
		    {	   
		    	I_C_Invoice invoice= bnz.getC_Invoice();
		    	MBPBankAccount m_bp=retrieveBPCreditCardDetails(invoice.getC_BPartner_ID());
		    	
				validate(m_bp);
				
				loadRequestFields(webpayClient,bnz,m_bp);
			
				executeTransaction(webpayClient);
				
				String responseCode = webpayClient.get(RES_RESPONSECODE);		
				String responseText = webpayClient.get(RES_RESPONSETEXT);
				String error = webpayClient.get(RES_ERROR);
				// Create response message
				String responseMessage = "Response = " + responseText;
				if (error != null && error.length() > 0 && !error.equalsIgnoreCase(responseText))
					responseMessage += ", Error = " + error; 
				if (!RC_ACCEPTED.equals(responseCode) && !RC_ACCEPTED_WITH_SIG.equals(responseCode))
				{
					log.warning("Transaction failed - " + responseText + " ResponseCode[" + responseCode + "]" + error != null ? " Error[" + error + "]" : ""+"payment failed for invoice no :"+invoice.getDocumentNo());
					//continue;
					//throw new Exception("Transaction failed - " + responseText + " ResponseCode[" + responseCode + "]" + error != null ? " Error[" + error + "]" : "");
				}
			//	if (error==null)
				processResponse(webpayClient,bnz,m_bp);
		    }
		}
		catch (Exception ex)
		{
			log.severe(ex.getMessage());
			if (ex instanceof IllegalArgumentException)
				throw (IllegalArgumentException)ex;
		}
		
		return null;
	}

	private MBPBankAccount retrieveBPCreditCardDetails(int C_BP_ID)
	{
	    String sql="SELECT * FROM C_BP_BANKACCOUNT WHERE C_BPARTNER_ID=?";	
	    PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{	
			// Create statement and set parameters
			pstmt = DB.prepareStatement(sql.toString(), get_TrxName());
			pstmt.setInt(1, C_BP_ID);
			// Execute query and process result set
			rs = pstmt.executeQuery();
			while (rs.next())
				return new MBPBankAccount(getCtx(),rs,get_TrxName());
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
		return null;
	}

	private void validate(MBPBankAccount p_mp) {
		// Number
		if (p_mp.getCreditCardNumber() == null || p_mp.getCreditCardNumber().length() < MIN_CARDDATA_LENGTH)
		{
			log.severe("Creditcard number must be " + MIN_CARDDATA_LENGTH + " digits or longer");
			throw new IllegalArgumentException("Creditcard number must be " + MIN_CARDDATA_LENGTH + " digits or longer");
		}
		// Exp
		String errorMsg = MPaymentValidate.validateCreditCardExp(p_mp.getCreditCardExpMM(), p_mp.getCreditCardExpYY());
		if (errorMsg.length() > 0)
		{
			log.severe("Creditcard expiry must be valid - " + errorMsg);
			throw new IllegalArgumentException("Creditcard expiry must be valid - " + errorMsg);
		}
		// VV (can be null/empty)
		if (p_mp.getCreditCardVV() != null)
		{
			if (p_mp.getCreditCardVV().length() < REQ_CVC2_MIN_LENGTH || p_mp.getCreditCardVV().length() > REQ_CVC2_MAX_LENGTH)
			{
				log.severe("Creditcard verification code must be between " + REQ_CVC2_MIN_LENGTH + " and " + REQ_CVC2_MAX_LENGTH + " inclusive");
				throw new IllegalArgumentException("Creditcard verification code must be between " + REQ_CVC2_MIN_LENGTH + " and " + REQ_CVC2_MAX_LENGTH + " inclusive");
			}
		}
	}

	private void processResponse(Webpay webpayClient, MInvoicePaySchedule bnz, MBPBankAccount mBp) throws Exception
	{
		int OriAD_Client_ID = Env.getAD_Client_ID(getCtx());
		Env.setContext(getCtx(), "#AD_Client_ID", "1000000");
		
		int OriAD_Org_ID = Env.getAD_Client_ID(getCtx());
		Env.setContext(getCtx(), "#AD_Org_ID", "1000001");
		
		String txnReference = webpayClient.get(RES_TXNREFERENCE);
		String authCode = webpayClient.get(RES_AUTHCODE);
		String cvc2Response = webpayClient.get(RES_CVC2RESPONSE);
		String responseText = webpayClient.get(RES_RESPONSETEXT);
		
			MPayment payment=new MPayment(getCtx(),0,null);
			payment.setDateAcct(bnz.getDueDate());
			payment.setDateTrx(bnz.getDueDate());
			payment.setPayAmt(bnz.getDueAmt());
			payment.setC_Currency_ID(121);
			payment.setC_BPartner_ID(mBp.getC_BPartner_ID());
			payment.setC_Invoice_ID(bnz.getC_Invoice_ID());
			payment.setC_BankAccount_ID(1000000);
			payment.setTenderType("C");
			payment.setR_PnRef(txnReference);
			payment.setOrig_TrxID(txnReference);
			payment.setR_AuthCode(authCode);
			payment.setR_RespMsg(responseText);
			payment.setIsOnline(true);
			payment.setCreditCardType(mBp.getCreditCardType());
			payment.setCreditCardVV(mBp.getCreditCardVV());
			payment.setCreditCardNumber(mBp.getCreditCardNumber());
			payment.setCreditCardExpMM(mBp.getCreditCardExpMM());
			payment.setCreditCardExpYY(mBp.getCreditCardExpYY());
			payment.setA_Name(mBp.getA_Name());
			payment.setTrxType("S");
			payment.setC_DocType_ID(1000008);
			payment.setIsReceipt(true);
			payment.setIsApproved(true);
			if (!payment.save())
				log.severe("Automatic payment creation failure - payment not saved");
			if(webpayClient.get(RES_ERROR)==null)
			{
				payment.processIt("CO");
				payment.save();
			}
	        setProcessedOK(bnz.getC_Invoice_ID());
	        
		Env.setContext(getCtx(), "#AD_Client_ID", OriAD_Client_ID);
		Env.setContext(getCtx(), "#AD_Org_ID", OriAD_Org_ID);
	}

	private void executeTransaction(Webpay webpayClient) throws Exception
	{
		String responseCode = null;
    	
		try
		{
			webpayClient.execute();
		}
		catch (IOException ex)
		{	
			// If transaction reference exists then it's in progress
			if (webpayClient.get(RES_TXNREFERENCE) != null)
				responseCode = RC_INPROGRESS;
			else
				throw new Exception("Transaction failed - " + ex.getMessage());
		}
		
		// Get response code if not already set by exception clause
		if (responseCode == null) 
			responseCode = webpayClient.get(RES_RESPONSECODE);
		
		// Poll server while transaction still in progress or until max status requests reached
		for (int statusCheckCount = 1; statusCheckCount < MAX_STATUS_REQUESTS; statusCheckCount++)
		{
			if (RC_INPROGRESS.equals(responseCode) || RC_NUM_INPROGRESS.equals(responseCode))
			{
				// Change transaction type to status request
				webpayClient.put(REQ_TRANSACTIONTYPE, REQ_TRANSACTIONTYPE_STATUS);
				
				try
				{
					try
					{
						Thread.sleep(POLLING_DELAY_MS);
					}
					catch (InterruptedException ex){}
					
					webpayClient.execute();
				}
				catch (IOException ex)
				{
					// If transaction reference exists then it's in progress
					if (webpayClient.get(RES_TXNREFERENCE) != null)
						responseCode = RC_INPROGRESS;
					else
						throw new Exception("Transaction failed after " + statusCheckCount + " attempts - " + ex.getMessage());
				}
				
				if (responseCode == null) 
					responseCode = webpayClient.get(RES_RESPONSECODE);
			}
			else
			{
				// Break out of loop as transaction is no longer in progress
				break;
			}
		}
		
		/* 
		 * Finished polling server..
		 * 
		 * Check if still in progress or null/empty response code,
		 * if still IN_PROGRESS then transaction can be treated 
		 * as declined (as stated in the "Buy-Line+ Credit Card Transaction Guide - Jan 08, Appendix G (PDF)").
		 */
		if (RC_INPROGRESS.equals(responseCode) || RC_NUM_INPROGRESS.equals(responseCode) || responseCode == null || responseCode.length() < 1)
		{
			// Void transaction (only if transaction reference has been set)
			if (webpayClient.get(RES_TXNREFERENCE) != null)
			{
				webpayClient.put(REQ_TRANSACTIONTYPE, REQ_TRANSACTIONTYPE_VOID);
				
				try
				{
					webpayClient.execute();
				}
				catch (IOException ex)
				{
					log.severe("Failed to void transaction " + webpayClient.get(RES_TXNREFERENCE));
				}
			}
			
			throw new Exception("Transaction failed - Still in progress or response code null/empty");
		}	
		
	}

	private void loadRequestFields(Webpay webpayClient,MInvoicePaySchedule bnz,MBPBankAccount p_mp) {
		// Get formatted values
		String totalAmount=formatTotalAmount(bnz.getDueAmt());
		
		String merchantCardholderName = "";
		if (p_mp.getA_Name() != null && p_mp.getA_Name().length() > 0)
			merchantCardholderName = p_mp.getA_Name();
		
		if (merchantCardholderName.length() > MAX_MERCHANT_CARDHOLDERNAME_LENGTH)
			merchantCardholderName = merchantCardholderName.substring(0, MAX_MERCHANT_CARDHOLDERNAME_LENGTH - 1);

			webpayClient.put(REQ_TRANSACTIONTYPE, REQ_TRANSACTIONTYPE_PURCHASE);		
			webpayClient.put(REQ_INTERFACE, REQ_INTERFACE_CREDITCARD);
			webpayClient.put(REQ_TOTALAMOUNT, totalAmount);
			webpayClient.put(REQ_CARDDATA, p_mp.getCreditCardNumber());
			webpayClient.put(REQ_CARDEXPIRYDATE,getCreditCardExp(p_mp.getCreditCardExpMM(), p_mp.getCreditCardExpYY()));
			webpayClient.put(REQ_MERCHANT_CARDHOLDERNAME, merchantCardholderName);

		// Set optional CVC2 field
		String cvc2 = p_mp.getCreditCardVV(); 
		if (cvc2 != null)
		{
			webpayClient.put(REQ_CVC2, cvc2);
			webpayClient.put(REQ_CCI, REQ_CCI_ENTERED);
		}
	}

	private Webpay createWebpayClient() throws Exception
	{
		try
		{
			String sql="SELECT C_PaymentProcessor_ID FROM C_PAYMENTPROCESSOR WHERE NAME='BNZBuyline' AND AD_CLIENT_ID="+AD_Client_ID;
			int C_PaymentProcessor_ID=DB.getSQLValue(null, sql);
			MPaymentProcessor mpp=new MPaymentProcessor(getCtx(),C_PaymentProcessor_ID,get_TrxName());
			
			// Create client and set parameters
			Webpay webpayClient = new Webpay(mpp.getUserID(), getCertificatePath(mpp.getCertFilename()), mpp.getPassword());
			webpayClient.setServers(new String[]{mpp.getHostAddress()});
			webpayClient.setPort(mpp.getHostPort());
			webpayClient.setDebugLevel(WEBPAY_DEBUG_LEVEL);
			
			return webpayClient;
		}
		catch(Exception ex)
		{
			log.severe("Failed to create Webpay client - " + ex.getMessage());
			throw new Exception("Failed to create Webpay client - " + ex.getMessage());
		}
	}

	/**
	 *  Prepare - e.g., get Parameters.
	 */
	@Override
	protected void prepare() {
		// TODO Auto-generated method stub
		
	}

	private void getScheduledPayments()
	{
		Calendar today=Calendar.getInstance();
		SimpleDateFormat dateFormat=new SimpleDateFormat("dd-MMM-yy");

		String sql="SELECT PAYSCH.* FROM "+MInvoicePaySchedule.Table_Name + " PAYSCH LEFT OUTER JOIN C_PAYMENT PAY ON (PAYSCH.C_INVOICE_ID=PAY.C_INVOICE_ID) WHERE PAYSCH.DUEDATE='"+dateFormat.format(today.getTime())+"' AND PAYSCH.PROCESSED='N' AND PAYSCH.DUEAMT >0";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{	
			// Create statement and set parameters
			pstmt = DB.prepareStatement(sql.toString(), get_TrxName());
			
			// Execute query and process result set
			rs = pstmt.executeQuery();
			while (rs.next())
				paySchedules.add(new MInvoicePaySchedule(getCtx(),rs,get_TrxName()));
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
	}
	
	private String getCertificatePath(String certFileName) throws FileNotFoundException
	{
		File certificate = new File(Ini.findAdempiereHome() + File.separator + certFileName);
		if (certificate.exists())
			return certificate.getAbsolutePath();
		
		certificate = new File(System.getProperty("user.dir") + File.separator + certFileName);
		if (certificate.exists())
			return certificate.getAbsolutePath();
		log.severe("Cannot load certificate");
		throw new FileNotFoundException("Cannot load certificate");
	}
	/**
	 * Formats the amount to result in *.00
	 * 
	 * @param amount to format
	 * @return formatted amount
	 */
    private static String formatTotalAmount(BigDecimal amount)
    {
    	String formattedAmount = amount.abs().toString();
    	
    	if (formattedAmount.contains("."))
		{
    		// Add a ones cent placeholder e.g. 18.3 -> 18.30
	    	if ((formattedAmount.indexOf(".") + 3) > formattedAmount.length())
	    		formattedAmount = formattedAmount + "0";
		}
		else
			// Add cents
			formattedAmount = formattedAmount + ".00";
    	
		return formattedAmount;
    }
    /**
	 *  CreditCard Exp  MMYY
	 *  @param delimiter / - or null
	 *  @return Exp
	 */
	public static String getCreditCardExp(int month,int year)
	{
		String mm = String.valueOf(month);
		String yy = String.valueOf(year);

		StringBuffer retValue = new StringBuffer();
		if (mm.length() == 1)
			retValue.append("0");
		retValue.append(mm);
		
		if (yy.length() == 1)
		{
			retValue.append("0");
			retValue.append(yy);
		}
		else if(yy.length()==4)
			retValue.append(yy.substring(2));
		else
			retValue.append(yy);
		//
		System.out.println(retValue);
		return (retValue.toString());
	}   //  getCreditCardExp
	
	public void setProcessedOK(int C_Invoice_ID)
	{
		String sql="UPDATE C_InvoicePaySchedule SET PROCESSED='Y' WHERE C_iNVOICE_ID=?";
		int i=DB.getSQLValue(get_TrxName(), sql, C_Invoice_ID);
	}
}
