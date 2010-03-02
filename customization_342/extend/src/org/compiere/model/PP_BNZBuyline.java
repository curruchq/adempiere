/******************************************************************************
 * Product: Compiere ERP & CRM Smart Business Solution                        *
 * Copyright (C) 1999-2006 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
package org.compiere.model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.logging.Level;

import org.compiere.model.MPayment;
import org.compiere.util.Ini;

import webpay.client.Webpay;
import webpay.net.TransactionBundle;

/**
 *  Payment Processor for BNZ Buyline+
 * 	Needs Certification File (get from BNZ)
 *
 *  @author  Josh Hill
 *  @version $Id: PP_BNZBuyline.java,v 1.0 2008/01/17 12:11:12 jhill Exp $
 */
public final class PP_BNZBuyline extends PaymentProcessor implements Serializable
{
	private static final String PERIOD = ".";
	private static final int DEBUG_LEVEL = 0; // 0 = off, 1 = lowest, 3 = highest
	
	private static final int MAX_STATUS_REQUESTS = 3;
	private static final int POLLING_DELAY_MS = 500;
	
	private static final int MIN_CARD_DATA_LENGTH = 12;
	private static final int MIN_CCVV_LENGTH = 3;
	private static final int MAX_CCVV_LENGTH = 6;
	private static final int MAX_CARDHOLDER_NAME_LENGTH = 50;
	
	// Note: If CCI_ILLEGIBLE or CCI_NO_SECURE_ID are used make sure CVC2 field is blank
	private static final String CCI_ENTERED = "1";
	private static final String CCI_ILLEGIBLE = "2";
	private static final String CCI_NO_SECURE_ID = "9";
	
	// Transaction fields
	private static final String RF_TRANSACTION_TYPE = "TRANSACTIONTYPE";
	
	// Transaction request fields
	private static final String RQF_CARD_EXPIRY_DATE = "CARDEXPIRYDATE";
	private static final String RQF_CARD_DATA = "CARDDATA";
	private static final String RQF_TOTAL_AMOUNT = "TOTALAMOUNT";
	private static final String RQF_TRANSACTION_TYPE_PURCHASE = "PURCHASE";
	private static final String RQF_TRANSACTION_TYPE_REFUND = "REFUND";
	private static final String RQF_TRANSACTION_TYPE_STATUS = "STATUS";
	private static final String RQF_TRANSACTION_TYPE_VOID = "VOID";
	private static final String RQF_CVC2 = "CVC2";
	private static final String RQF_CCI = "CCI";
	private static final String RQF_INTERFACE = "INTERFACE";	
	private static final String RQF_INTERFACE_CREDIT_CARD = "CREDITCARD";
	private static final String RQF_CARDHOLDER_NAME = "MERCHANT_CARDHOLDERNAME";
	private static final String RQF_ORIGINAL_TXNREF = "ORIGINALTXNREF";
	
	// Transaction response fields
	private static final String RSPF_TRANSACTION_REF = "TXNREFERENCE";
	private static final String RSPF_RESPONSE_CODE = "RESPONSECODE";
	private static final String RSPF_RESPONSE_TEXT = "RESPONSETEXT";
	private static final String RSPF_AUTH_CODE = "AUTHCODE";
	private static final String RSPF_CVC2_RESP = "CVC2RESPONSE";
	private static final String RSPF_ERROR = "ERROR";
	
	// Transaction response code
	private static final String RC_ACCEPTED = "00";
	private static final String RC_ACCEPTED_WITH_SIG = "08";
	private static final String RC_IN_PROGRESS = "IP";
	private static final String RC_NUM_IN_PROGRESS = "88";
	
	
	private boolean m_ok = false;
	
	// TODO: Rigorous testing of all methods/fields
	// TODO: Validate CVC2 processing
	// TODO: Test account name is set in MPayment.getA_Name() when using webstore
	// TODO: Add messages to ApplicationDictionary -> Message (so that they can be multilingual)
	
	/**
	 * PP_BNZBuyline Constructor 
	 */
	public PP_BNZBuyline()
	{
		super();
	}
	
	/**
	 * Checks if transaction has been processed
	 * 
	 * @return true if processed successfully
	 */
	public boolean isProcessedOK()
	{
		return this.m_ok;
	}
	
	/**
	 *  Process Credit Card
	 *  
	 *  @return true if processed successfully
	 *  @throws IllegalArgumentException
	 */
	public boolean processCC() throws IllegalArgumentException
	{			
		// Get webpay object
		Webpay pp_webpay = this.getWebpayClient();
		
		if (pp_webpay != null)
		{				
			try
			{
				// Set request fields and values
				TransactionBundle tb = getRequestFields(this.p_mp.isRefundTxn());
				for (String requestField : tb.getNames())
				{
					pp_webpay.put(requestField, tb.get(requestField));
				}
				
				// Execute transaction
				if (this.executeTransaction(pp_webpay))
				{
					this.m_ok = this.processResponse(pp_webpay);
				}
				else
					this.m_ok = false;
			}
			catch (IllegalArgumentException ex)
			{
				log.info("IllegalArgumentException caught and handled, ex=" + ex.getMessage());
				this.p_mp.setR_RespMsg(ex.getMessage());
				this.m_ok = false;
			}
		}
		else
			this.m_ok = false;
		
		return this.m_ok;
	}

	/**
	 * Gets request fields and values (a TransactionBundle)
	 * 
	 * @return TransactionBundle of param key/value pairs
	 */
	private TransactionBundle getRequestFields(boolean refundTrx)
	{
		TransactionBundle params = new TransactionBundle();
		
		/*
		 * NOTE: Implemented front end validation as suggested 
		 * by "Buy-Line+ Credit Card Transaction Guide - Jan 08, Appendix G (PDF)".
		 * All other validation is left up to the Buyline servers.
		 */
		
		// Validate transaction type (only allow sales at this stage)
		String trxType = p_mp.getTrxType().trim();
		if (trxType!= null && trxType.equalsIgnoreCase(MPayment.TRXTYPE_Sales))
		{
			// set refund transaction params
			if (refundTrx)
			{
				String origTxnRef = this.p_mp.getOrig_TrxID();
				if (origTxnRef != null && origTxnRef.length() > 0)
				{
					params.put(PP_BNZBuyline.RF_TRANSACTION_TYPE, PP_BNZBuyline.RQF_TRANSACTION_TYPE_REFUND);
					params.put(PP_BNZBuyline.RQF_ORIGINAL_TXNREF, origTxnRef);
				}
				else
				{
					throw new IllegalArgumentException("Invalid Original Transaction ID '" + origTxnRef + "'");
				}
			}
			else
				params.put(PP_BNZBuyline.RF_TRANSACTION_TYPE, PP_BNZBuyline.RQF_TRANSACTION_TYPE_PURCHASE);
		}
		else
			throw new IllegalArgumentException("TrxType not supported '" + trxType + "'");
		
		// Validate card expiry date
		String validateCCExp = MPaymentValidate.validateCreditCardExp(p_mp.getCreditCardExpMM(), p_mp.getCreditCardExpYY());
		if (validateCCExp != null && validateCCExp.equals("")) // no error msg
		{
			params.put(PP_BNZBuyline.RQF_CARD_EXPIRY_DATE, p_mp.getCreditCardExp(null));
		}
		else
			throw new IllegalArgumentException("Invalid card expiry date '" + p_mp.getCreditCardExp("/") + "'");
		
		// Validate card number
		String cardData = p_mp.getCreditCardNumber(); // already validated when set using MPayment.setCreditCardNumber(String)
		if (cardData != null && cardData.length() >= PP_BNZBuyline.MIN_CARD_DATA_LENGTH)
		{
			params.put(PP_BNZBuyline.RQF_CARD_DATA, cardData);
		}
		else
			throw new IllegalArgumentException("Invalid card number" + cardData == null || cardData.length() < 1 ? "" : " '" + cardData + "'");
		
		// validate CVC2
		String cvc2 = p_mp.getCreditCardVV(); // already validated when set using MPayment.setCreditCardVV(String)
		if (cvc2 != null && (cvc2.length() >= PP_BNZBuyline.MIN_CCVV_LENGTH) && (cvc2.length() <= PP_BNZBuyline.MAX_CCVV_LENGTH))
		{
			params.put(PP_BNZBuyline.RQF_CVC2, cvc2);
			params.put(PP_BNZBuyline.RQF_CCI, PP_BNZBuyline.CCI_ENTERED);
			// TODO: Add dialog is CVC2 field left blank to ask user if CVC2 exists or isn't readable
		}
		else
			log.fine("No CVC2 field set");
//			throw new IllegalArgumentException("Invalid Credit Card Validation Code" + cvc2 == null || cvc2.length() < 1 ? "" : " '" + cvc2 + "'");
	
		// set cardholder name
		String cardholderName = p_mp.getA_Name();
		if (cardholderName != null && cardholderName.trim().length() > 0)
		{
			cardholderName = cardholderName.trim();
			
			// trim cardholder name down to max size specified by BNZ
			if (cardholderName.length() > PP_BNZBuyline.MAX_CARDHOLDER_NAME_LENGTH)
			{
				cardholderName = cardholderName.substring(0, PP_BNZBuyline.MAX_CARDHOLDER_NAME_LENGTH - 1);
			}
			params.put(PP_BNZBuyline.RQF_CARDHOLDER_NAME, cardholderName);
		}
		
		// TODO: Add address (and other fields from webstore form) to MERCHANT_DESCRIPTION or COMMENT request field
		
		// Set amount (validated by compiere before this method is called)
		params.put(PP_BNZBuyline.RQF_TOTAL_AMOUNT, PP_BNZBuyline.getFormattedAmount(p_mp.getPayAmt().toString()));
		
		// Always set interface as Credit Card
		params.put(PP_BNZBuyline.RQF_INTERFACE, PP_BNZBuyline.RQF_INTERFACE_CREDIT_CARD);
		
		return params;
	}	// 
	
	/**
	 * Execute a Webpay transaction
	 * 
	 * @param pp_webpay Webpay object to execute
	 * @return true if processed successfully
	 */
	private boolean executeTransaction(Webpay pp_webpay)
	{	
		if (pp_webpay != null)
		{
			String responseCode = null;
		
			// Execute the transaction
			try
			{
				pp_webpay.execute();
			}
			catch (IOException ex)
			{	
				// If TXNREFERENCE is null the transaction has failed.
				if (pp_webpay.get(PP_BNZBuyline.RSPF_TRANSACTION_REF) == null)
				{
					log.log(Level.SEVERE, "BNZBuyline transaction failed - TRANSACTIONTYPE = " + pp_webpay.get(PP_BNZBuyline.RF_TRANSACTION_TYPE) + ", Msg = " + ex.getMessage());
					this.p_mp.setR_RespMsg("Transaction failed, please try again later.");
					return false;
				}
				
				// Set responseCode to IN_PROGRESS so that status request is sent
				responseCode = PP_BNZBuyline.RC_IN_PROGRESS;
			}
			
			/* 
			 * No exception has occurred, so the transaction has returned a
			 * response code. Check to see if the transaction is still "IN PROGRESS".
			 * If it is, poll server with status requests until either the response
			 * code changes or to a maximum of three status requests.
			 */
			
			// If responseCode is null then no exception occured and transaction has returned a response code.
			if (responseCode == null) 
			{
				responseCode = pp_webpay.get(PP_BNZBuyline.RSPF_RESPONSE_CODE);
			}
			
			// If responseCode = IN_PROGRESS then poll server with status request
			for (int statusCheckCount = 0; statusCheckCount < PP_BNZBuyline.MAX_STATUS_REQUESTS; statusCheckCount++)
			{
				if (PP_BNZBuyline.RC_IN_PROGRESS.equals(responseCode) || PP_BNZBuyline.RC_NUM_IN_PROGRESS.equals(responseCode))
				{
					// The transaction is still in progress. Send a status request.
					pp_webpay.put(PP_BNZBuyline.RF_TRANSACTION_TYPE, PP_BNZBuyline.RQF_TRANSACTION_TYPE_STATUS);
					try
					{
						try
						{
							Thread.sleep(PP_BNZBuyline.POLLING_DELAY_MS);
						}
						catch (InterruptedException ex){}
						
						pp_webpay.execute();
					}
					catch (IOException ex)
					{
						// If TXNREFERENCE is null the transaction has failed.
						if (pp_webpay.get(PP_BNZBuyline.RSPF_TRANSACTION_REF) == null)
						{
							log.log(Level.SEVERE, "BNZBuyline transaction failed -- TRANSACTIONTYPE = " + pp_webpay.get(PP_BNZBuyline.RF_TRANSACTION_TYPE)  + ", Msg = " + ex.getMessage() + " -- after " + (statusCheckCount + 1) + " attempts.");
							this.p_mp.setR_RespMsg("Transaction failed, please try again later.");
							return false;
						}
					}

					responseCode = pp_webpay.get(PP_BNZBuyline.RSPF_RESPONSE_CODE);
				}
				else
					// Break out of loop, transaction is no longer IN_PROGRESS
					break;
			}
			
			/* 
			 * Finished polling server..
			 * 
			 * Check if still in progress or null/empty response code,
			 * if still IN_PROGRESS then transaction can be treated 
			 * as declined (as stated in the "Buy-Line+ Credit Card Transaction Guide - Jan 08, Appendix G (PDF)").
			 */
			if (PP_BNZBuyline.RC_IN_PROGRESS.equals(responseCode) || 
				PP_BNZBuyline.RC_NUM_IN_PROGRESS.equals(responseCode) || 
				Null.NULL.equals(responseCode) || 
				"".equals(responseCode))
			{
				boolean voidRequestSuccessful = true;
				
				// Send a VOID request as transaction has been IN_PROGRESS too long (can only send VOID if TXNREF has been set
				if (pp_webpay.get(PP_BNZBuyline.RSPF_TRANSACTION_REF) != null)
				{
					pp_webpay.put(PP_BNZBuyline.RF_TRANSACTION_TYPE, PP_BNZBuyline.RQF_TRANSACTION_TYPE_VOID);
					try
					{
						pp_webpay.execute();
					}
					catch (IOException ex)
					{
						voidRequestSuccessful = false;
					}
					
					// set the original transaction id (even if was voided)
					this.p_mp.setOrig_TrxID(pp_webpay.get(PP_BNZBuyline.RSPF_TRANSACTION_REF));
				}
				
				log.log(Level.SEVERE, "BNZBuyline transaction still 'In Progress' after polling " + 
						PP_BNZBuyline.MAX_STATUS_REQUESTS + " times. Contact BNZ - " + PP_BNZBuyline.RSPF_TRANSACTION_REF + 
						"=" + pp_webpay.get(PP_BNZBuyline.RSPF_TRANSACTION_REF) + " - Void request sent = " + voidRequestSuccessful);
				this.p_mp.setR_RespMsg("Transaction failed, please try again later.");
				return false;
			}
		}
		
		// Transaction successful
		return true;
	}	// executeTransaction
	
	/**
	 * Process response
	 * 
	 * @param pp_webpay Webpay object to get response from 
	 * @return true if processed
	 */
	private boolean processResponse(Webpay pp_webpay)
	{
		if (pp_webpay != null)
		{
			// get response fields
			String responseCode = pp_webpay.get(PP_BNZBuyline.RSPF_RESPONSE_CODE);
			String txnRef = pp_webpay.get(PP_BNZBuyline.RSPF_TRANSACTION_REF);
			String respText = pp_webpay.get(PP_BNZBuyline.RSPF_RESPONSE_TEXT);
			String authCode = pp_webpay.get(PP_BNZBuyline.RSPF_AUTH_CODE);
			String cvc2Resp = pp_webpay.get(PP_BNZBuyline.RSPF_CVC2_RESP);
			String error = pp_webpay.get(PP_BNZBuyline.RSPF_ERROR);
			
			// set up respmsg, add error msg if exists
			String respMsg = respText;
			if (error != null && error.length() > 0 && !error.equalsIgnoreCase(respText))
			{
				respMsg = respMsg + ", ErrorMsg=" + error; 
			}

			// set MPayment fields
			this.p_mp.setR_PnRef(txnRef);
			this.p_mp.setR_AuthCode(authCode);
			this.p_mp.setR_RespMsg(respText);
			this.p_mp.setOrig_TrxID(txnRef);
			
			// return true if responseCode = RC_ACCEPTED ('00') || responseCode = RC_ACCEPTED_WITH_SIG ('08') else false
			return PP_BNZBuyline.RC_ACCEPTED.equals(responseCode) || PP_BNZBuyline.RC_ACCEPTED_WITH_SIG.equals(responseCode);
			
			// TODO: Handle cvc2 response - ask Cameron what procedure he would like to use
		}
		return false;
	}	// processResponse
	
	/**
	 * Gets a Webpay object
	 * 
	 * @return Webpay object is succesful or null if unsuccessful
	 */
	private Webpay getWebpayClient()
	{
		// Check if cert file exists
		String certPath = Ini.findAdempiereHome() + File.separator + p_mpp.getCertFilename();
		File certFile = new File(certPath);
		if (!certFile.exists())
		{
			certPath = System.getProperty("user.dir") + File.separator + p_mpp.getCertFilename();
			certFile = new File(certPath);
			
			if (!certFile.exists())
			{
				log.log(Level.SEVERE, "Certificate file doesn't exist - " + certFile.getAbsolutePath());
				this.p_mp.setR_RespMsg("Service unavailable, please contact the administrator.");			
				return null;
			}
		}
		
		return this.getWebpayClient(certFile.getAbsolutePath());
	}	// getWebpayClient
	
	/**
	 * Gets a Webpay object
	 * 
	 * @param cert The full path to cert file
	 * @return Webpay object is succesful or null if unsuccessful
	 */
	private Webpay getWebpayClient(String cert)
	{
		// Not an instance variable to avoid any possible concurrency issues
		Webpay pp_webpay = null;
		
		// Check if cert file exists
		File certFile = new File(cert);
		if (!certFile.exists())
		{
			log.log(Level.SEVERE, "Certificate file doesn't exist - " + certFile.getAbsolutePath());
			this.p_mp.setR_RespMsg("Service unavailable, please contact the administrator.");			
			return null;
		}
		
		// Log basic params
		log.fine("Server=" + p_mpp.getHostAddress() + ":" + p_mpp.getHostPort() +  
				 ", UserID=" + p_mpp.getUserID() +
				 ", Cert=" + certFile.getAbsolutePath() + 
				 ", Timeout=" + getTimeout());
		
		// Set up webpay object -> Webpay(clientID, certificatePath, certificatePassword)
		try
		{
			pp_webpay = new Webpay(p_mpp.getUserID(), certFile.getAbsolutePath(), p_mpp.getPassword());
		}
		catch(Exception ex)
		{
			log.log(Level.SEVERE, "Webpay object could not be setup - " + ex.getMessage());
			this.p_mp.setR_RespMsg("Service unavailable, please contact the administrator.");
			return null;
		}
		
		// Set server
		pp_webpay.setServers(new String[]{p_mpp.getHostAddress()});
		
		// Set port
		pp_webpay.setPort(p_mpp.getHostPort());
		
		// Set debugging level
		pp_webpay.setDebugLevel(PP_BNZBuyline.DEBUG_LEVEL);
		
		return pp_webpay;
	}	// getWebpayClient

	/**
	 * Rounds a double to a specified number of decimal places
	 * 
	 * @param d double to round
	 * @param places number of places to round too
	 * @return the rounded double
	 */
	public static final double roundDouble(double d, int places)
	{
		return Math.round(d * Math.pow(10, (double) places)) / Math.pow(10, (double) places);
	}	// roundDouble
    
	/**
	 * Formats the amount to result in *.00
	 * 
	 * @param amount to format
	 * @return formatted amount
	 */
    public static final String getFormattedAmount(String amount)
    {
		if (amount.contains(PP_BNZBuyline.PERIOD))
		{
	    	if ((amount.indexOf(PP_BNZBuyline.PERIOD) + 3) > amount.length())
			{
				amount = amount + "0";
			}
		}
		else
		{
			amount = amount + ".00";
		}
		return amount;
    }	// getFormattedAmount
	
	public static void main(String[] args)
	{
		PP_BNZBuyline pp_BnzBuyline = new PP_BNZBuyline();
		PrintWriter pw = null;
		try
		{
			pw = new PrintWriter(new FileWriter("D:\\webpayALWAYSACCEPT.txt"));
		}
		catch (IOException ex)
		{
			System.err.println("Error opening output file");
		}
		ArrayList<String> failedAmounts = new ArrayList<String>();
		double amount = 1.00;
		double one_cent = 0.01;
		while(amount < 2)
		{
			Webpay webpay = null;
			
			String sAmount = Double.toString(amount);
			if ((sAmount.indexOf(".") + 3) > sAmount.length())
			{
				sAmount = sAmount + "0";
			}
			
			try
			{
				webpay = new Webpay("10001809", System.getProperty("user.dir") + File.separator + "BNZTest.ks", "Ke3Ds57G");
				webpay.setServers(new String[]{"trans2.buylineplus.co.nz"});
				webpay.setPort(3007);
				webpay.setDebugLevel(PP_BNZBuyline.DEBUG_LEVEL);
			}
			catch(Exception ex)
			{
				System.err.println("Exception raised when creating webpay object");
			}
			
			if (webpay != null)
			{
				webpay.put(PP_BNZBuyline.RQF_CARD_EXPIRY_DATE, "1010");
				webpay.put(PP_BNZBuyline.RQF_CARD_DATA, "4242424242424242");//MC="5430489999999992");//VISA="4564456445644564");
				webpay.put(PP_BNZBuyline.RQF_TOTAL_AMOUNT, sAmount);
				webpay.put(PP_BNZBuyline.RF_TRANSACTION_TYPE, PP_BNZBuyline.RQF_TRANSACTION_TYPE_PURCHASE);
				webpay.put(PP_BNZBuyline.RQF_INTERFACE, PP_BNZBuyline.RQF_INTERFACE_CREDIT_CARD);
				
				if (pp_BnzBuyline.executeTransaction(webpay))
				{					
					pw.println("---------------------------------");
					pw.println("Amount = " + sAmount + "\n");
					for (String resName : webpay.getResponseNames())
					{
						pw.println(resName + ": " + webpay.get(resName));
					}
					pw.println("---------------------------------");
				}
				else
				{
					failedAmounts.add(sAmount);
				}
				
				amount = roundDouble(amount + one_cent, 2);
				
			}
			else
			{
				failedAmounts.add(sAmount);
			}
		}
		
		if (failedAmounts.size() > 0)
		{
			pw.println("--------------The following amounts failed------------------");
			for (String s : failedAmounts)
			{
				pw.println(s);
			}
			pw.println("------------------------------------------------------------");
		}
		
		pw.close();
	}

}
