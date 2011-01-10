package org.compiere.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.logging.Level;

import org.compiere.util.Ini;

import webpay.client.Webpay;

public class PP_BNZBuylinePlus extends PaymentProcessor implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	private static final int WEBPAY_DEBUG_LEVEL = 0; // 0 = off, 1 = lowest, 3 = highest
	
	private static final int MAX_STATUS_REQUESTS = 3;
	private static final int POLLING_DELAY_MS = 500;
	
	private static final int MIN_CARDDATA_LENGTH = 12;
	private static final int MAX_MERCHANT_CARDHOLDERNAME_LENGTH = 50;
	
	// Request fields
	private static final String REQ_TRANSACTIONTYPE = "TRANSACTIONTYPE";	
	private static final String REQ_TRANSACTIONTYPE_PURCHASE = "PURCHASE";
	private static final String REQ_TRANSACTIONTYPE_REFUND = "REFUND";
	private static final String REQ_TRANSACTIONTYPE_STATUS = "STATUS";
	private static final String REQ_TRANSACTIONTYPE_VOID = "VOID";
	
	private static final String REQ_INTERFACE = "INTERFACE";		
	private static final String REQ_INTERFACE_CREDITCARD = "CREDITCARD";
	
	private static final String REQ_TOTALAMOUNT = "TOTALAMOUNT";
	private static final String REQ_CARDDATA = "CARDDATA";
	private static final String REQ_CARDEXPIRYDATE = "CARDEXPIRYDATE";
	private static final String REQ_MERCHANT_CARDHOLDERNAME = "MERCHANT_CARDHOLDERNAME";
	
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
	
	@Override
	public boolean processCC() throws IllegalArgumentException
	{
		try
		{
			Webpay webpayClient = createWebpayClient();
		
			validate();
			
			loadRequestFields(webpayClient);
		
			executeTransaction(webpayClient);
			
			processResponse(webpayClient);
			
			setProcessedOK(true);
		}
		catch (Exception ex)
		{
			log.log(Level.WARNING, ex.getMessage());
			
			if (p_mp.getR_RespMsg() == null || p_mp.getR_RespMsg().length() < 1)
				p_mp.setR_RespMsg("Transaction failed");
			
			if (ex instanceof IllegalArgumentException)
				throw (IllegalArgumentException)ex;
		}
		
		return isProcessedOK();
	}
	
	private void validate() throws IllegalArgumentException
	{
		// Number
		if (p_mp.getCreditCardNumber() == null || p_mp.getCreditCardNumber().length() < MIN_CARDDATA_LENGTH)
			throw new IllegalArgumentException("Creditcard number must be " + MIN_CARDDATA_LENGTH + " digits or longer");
		
		// Exp
		String errorMsg = MPaymentValidate.validateCreditCardExp(p_mp.getCreditCardExpMM(), p_mp.getCreditCardExpYY());
		if (errorMsg.length() > 0)
			throw new IllegalArgumentException("Creditcard expiry must be valid - " + errorMsg);
		
		// VV (can be null/empty)
		if (p_mp.getCreditCardVV() != null)
		{
			if (p_mp.getCreditCardVV().length() < REQ_CVC2_MIN_LENGTH || p_mp.getCreditCardVV().length() > REQ_CVC2_MAX_LENGTH)
				throw new IllegalArgumentException("Creditcard verification code must be between " + REQ_CVC2_MIN_LENGTH + " and " + REQ_CVC2_MAX_LENGTH + " inclusive");
		}
		
		// Original Transaction Reference
		if (p_mp.isRefundTxn())
		{
			if (p_mp.getOrig_TrxID() == null || p_mp.getOrig_TrxID().length() < 1)
				throw new IllegalArgumentException("Invalid original transaction id (bank transaction reference)");
		}
	}
	
	private Webpay createWebpayClient() throws Exception
	{	
		try
		{
			// Create client and set parameters
			Webpay webpayClient = new Webpay(p_mpp.getUserID(), getCertificatePath(), p_mpp.getPassword());			
			webpayClient.setServers(new String[]{p_mpp.getHostAddress()});
			webpayClient.setPort(p_mpp.getHostPort());
			webpayClient.setDebugLevel(WEBPAY_DEBUG_LEVEL);
			
			return webpayClient;
		}
		catch(Exception ex)
		{
			throw new Exception("Failed to create Webpay client - " + ex.getMessage());
		}
	}
	
	private String getCertificatePath() throws FileNotFoundException
	{
		File certificate = new File(Ini.findAdempiereHome() + File.separator + p_mpp.getCertFilename());
		if (certificate.exists())
			return certificate.getAbsolutePath();
		
		certificate = new File(System.getProperty("user.dir") + File.separator + p_mpp.getCertFilename());
		if (certificate.exists())
			return certificate.getAbsolutePath();
		
		throw new FileNotFoundException("Cannot load certificate");
	}
	
	private void loadRequestFields(Webpay webpayClient)
	{
		// Get formatted values
		String totalAmount = formatTotalAmount(p_mp.getPayAmt());
		String merchantCardholderName = "";
		if (p_mp.getA_Name() != null && p_mp.getA_Name().length() > 0)
			merchantCardholderName = p_mp.getA_Name();
		
		if (merchantCardholderName.length() > MAX_MERCHANT_CARDHOLDERNAME_LENGTH)
			merchantCardholderName = merchantCardholderName.substring(0, MAX_MERCHANT_CARDHOLDERNAME_LENGTH - 1);
		
		// Set request fields
		if (p_mp.isRefundTxn())
			webpayClient.put(REQ_TRANSACTIONTYPE, REQ_TRANSACTIONTYPE_REFUND);
		else
			webpayClient.put(REQ_TRANSACTIONTYPE, REQ_TRANSACTIONTYPE_PURCHASE);
		
		webpayClient.put(REQ_INTERFACE, REQ_INTERFACE_CREDITCARD);
		webpayClient.put(REQ_TOTALAMOUNT, totalAmount);
		webpayClient.put(REQ_CARDDATA, p_mp.getCreditCardNumber());
		webpayClient.put(REQ_CARDEXPIRYDATE, p_mp.getCreditCardExp(null));
		webpayClient.put(REQ_MERCHANT_CARDHOLDERNAME, merchantCardholderName);

		// Set optional CVC2 field
		String cvc2 = p_mp.getCreditCardVV(); 
		if (cvc2 != null)
		{
			webpayClient.put(REQ_CVC2, cvc2);
			webpayClient.put(REQ_CCI, REQ_CCI_ENTERED);
		}
	}
	
	/**
	 * Formats the amount to result in *.00
	 * 
	 * @param amount to format
	 * @return formatted amount
	 */
    private String formatTotalAmount(BigDecimal amount)
    {
    	String formattedAmount = amount.toString();
    	
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
				
				// Set the original transaction id (even if was voided)
				p_mp.setOrig_TrxID(webpayClient.get(RES_TXNREFERENCE));
			}
			
			throw new Exception("Transaction failed - Still in progress or response code null/empty");
		}	
    }
    
	private void processResponse(Webpay webpayClient) throws Exception
	{
		String responseCode = webpayClient.get(RES_RESPONSECODE);
		String txnReference = webpayClient.get(RES_TXNREFERENCE);
		String responseText = webpayClient.get(RES_RESPONSETEXT);
		String authCode = webpayClient.get(RES_AUTHCODE);
		String cvc2Response = webpayClient.get(RES_CVC2RESPONSE);
		String error = webpayClient.get(RES_ERROR);
		
		// Create response message
		String responseMessage = "Response = " + responseText;
		if (error != null && error.length() > 0 && !error.equalsIgnoreCase(responseText))
			responseMessage += ", Error = " + error; 

		// Set MPayment fields
		p_mp.setR_PnRef(txnReference);
		p_mp.setOrig_TrxID(txnReference);
		p_mp.setR_AuthCode(authCode);
		p_mp.setR_RespMsg(responseText);
				
		if (!RC_ACCEPTED.equals(responseCode) && !RC_ACCEPTED_WITH_SIG.equals(responseCode))
			throw new Exception("Transaction failed - " + responseText + " ResponseCode[" + responseCode + "]" + error != null ? " Error[" + error + "]" : "");
	}	
	
	@Override
	public boolean isProcessedOK()
	{
		return processedOK;
	}

	public void setProcessedOK(boolean processedOK)
	{
		this.processedOK = processedOK;
	}
}
