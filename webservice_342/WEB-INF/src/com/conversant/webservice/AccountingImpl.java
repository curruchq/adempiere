package com.conversant.webservice;

import java.math.BigDecimal;
import java.util.Properties;

import javax.jws.WebService;

import org.compiere.model.MBPBankAccount;
import org.compiere.model.MBPartner;
import org.compiere.model.MPayment;
import org.compiere.model.MPaymentValidate;
import org.compiere.model.MUser;
import org.compiere.process.DocAction;
import org.compiere.util.Env;
import org.compiere.util.Msg;

import com.conversant.webservice.util.WebServiceConstants;

@WebService(endpointInterface = "com.conversant.webservice.Accounting")
public class AccountingImpl extends GenericWebServiceImpl implements Accounting
{
	// TODO: Check userId belongs to BP or vice versa
	public StandardResponse createPayment(CreatePaymentRequest createPaymentRequest)
	{
		// Create ctx and trxName (if not specified)
		Properties ctx = Env.getCtx(); 
		String trxName = getTrxName(createPaymentRequest.getLoginRequest());
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("ADMIN_WEBSERVICE"), WebServiceConstants.ACCOUNTING_WEBSERVICE_METHODS.get("CREATE_PAYMENT_METHOD_ID"), createPaymentRequest.getLoginRequest(), trxName);		
		if (error != null)	
			return getErrorStandardResponse(error, trxName);
		
		// Load and validate parameters
		Integer userId = createPaymentRequest.getBusinessPartnerId();
		if (userId == null || userId < 1 || !validateADId(MUser.Table_Name, userId, trxName))
			return getErrorStandardResponse("Invalid userId", trxName);
		
		Integer businessPartnerId = createPaymentRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || !validateADId(MBPartner.Table_Name, businessPartnerId, trxName))
			return getErrorStandardResponse("Invalid businessPartnerId", trxName);
						
		// Credit card data
		String creditCardType = createPaymentRequest.getCreditCardType();
		if (!validateString(creditCardType))
			creditCardType = null;
		else
			creditCardType = creditCardType.trim();
		
		String creditCardNumber = createPaymentRequest.getCreditCardNumber();
		if (!validateString(creditCardNumber))
			creditCardNumber = null;
		else
			creditCardNumber = creditCardNumber.trim();
		
		String creditCardVerificationCode = createPaymentRequest.getCreditCardVerificationCode();
		if (!validateString(creditCardVerificationCode))
			creditCardVerificationCode = null;
		else
			creditCardVerificationCode = creditCardVerificationCode.trim();
		
		String creditCardExpiryMonth = createPaymentRequest.getCreditCardExpiryMonth();
		if (!validateString(creditCardExpiryMonth))
			creditCardExpiryMonth = null;
		else
			creditCardExpiryMonth = creditCardExpiryMonth.trim();
				
		String creditCardExpiryYear = createPaymentRequest.getCreditCardExpiryYear();
		if (!validateString(creditCardExpiryYear))
			creditCardExpiryYear = null;
		else
			creditCardExpiryYear = creditCardExpiryYear.trim();
		
		String accountName = createPaymentRequest.getAccountName();
		if (!validateString(accountName))
			accountName = null;
		else
			accountName = accountName.trim();
		
		String accountStreet = createPaymentRequest.getAccountStreet();
		if (!validateString(accountStreet))
			accountStreet = null;
		else
			accountStreet = accountStreet.trim();
		
		String accountCity = createPaymentRequest.getAccountCity();
		if (!validateString(accountCity))
			accountCity = null;
		else
			accountCity = accountCity.trim();
		
		String accountZip = createPaymentRequest.getAccountZip();
		if (!validateString(accountZip))
			accountZip = null;
		else
			accountZip = accountZip.trim();
		
		String accountState = createPaymentRequest.getAccountState();
		if (!validateString(accountState))
			accountState = null;
		else
			accountState = accountState.trim();
		
		String accountCountry = createPaymentRequest.getAccountCountry();
		if (!validateString(accountCountry))
			accountCountry = null;
		else
			accountCountry = accountCountry.trim();
				
		// Check if BP Bank account exists
		boolean createBPBankAccount = false;
		Integer businessPartnerBankAccountId = createPaymentRequest.getBusinessPartnerBankAccountId();
		if (businessPartnerBankAccountId == null || businessPartnerBankAccountId < 1 || !validateADId(MBPBankAccount.Table_Name, businessPartnerBankAccountId, trxName))
			createBPBankAccount = true;
		
		if (createBPBankAccount)
		{
			// Validate new bank account details
			if (creditCardType == null)
				return getErrorStandardResponse("Invalid creditCardType", trxName);
				
			if (creditCardNumber == null)
				return getErrorStandardResponse("Invalid creditCardNumber", trxName);
			
			if (creditCardVerificationCode == null)
				return getErrorStandardResponse("Invalid creditCardVerificationCode", trxName);
			
			if (creditCardExpiryMonth == null)
				return getErrorStandardResponse("Invalid creditCardExpiryMonth", trxName);
			
			if (creditCardExpiryYear == null)
				return getErrorStandardResponse("Invalid creditCardExpiryYear", trxName);
			
			// TODO: Find out which account details mandatory
			if (accountName == null)
				return getErrorStandardResponse("Invalid accountName", trxName);
			
			if (accountStreet == null)
				return getErrorStandardResponse("Invalid accountStreet", trxName);
			
			if (accountCity == null)
				return getErrorStandardResponse("Invalid accountCity", trxName);
			
			if (accountZip == null)
				return getErrorStandardResponse("Invalid accountZip", trxName);
			
			if (accountState == null)
				return getErrorStandardResponse("Invalid accountState", trxName);
			
			if (accountCountry == null)
				return getErrorStandardResponse("Invalid accountCountry", trxName);
		}

		String amount = createPaymentRequest.getAmount();
		if (!validateString(amount))
			return getErrorStandardResponse("Invalid amount", trxName);
		
		BigDecimal amt = null;
		try
		{
			amt = new BigDecimal(amount.trim());
		}
		catch (NumberFormatException ex)
		{
			return getErrorStandardResponse("Invalid amount - Cannot parse", trxName);
		}
			
		// TODO: Allow ZERO amount?
		if (amt.compareTo(Env.ZERO) < 0)
			return getErrorStandardResponse("Invalid amount - Less or equal to 0", trxName);
		
		Integer invoiceId = createPaymentRequest.getInvoiceId();
		if (invoiceId == null || invoiceId < 1 || !validateADId(MUser.Table_Name, invoiceId, trxName))
			return getErrorStandardResponse("Invalid invoiceId", trxName);
				
		boolean savePaymentInformation = createPaymentRequest.isSavePaymentInformation();

		// Create payment
		MPayment payment = new MPayment(ctx, 0, trxName);
		payment.setIsSelfService(true);
		payment.setIsOnline(true);
		payment.setAmount(0, amt);	
		
		// Sales transaction
		payment.setC_DocType_ID(true);
		payment.setTrxType(MPayment.TRXTYPE_Sales);
		payment.setTenderType(MPayment.TENDERTYPE_CreditCard);
		
		// Payment Info
		payment.setC_Invoice_ID(invoiceId);
		
		// Create new bank account
		if (createBPBankAccount)
		{
			StringBuffer sb = new StringBuffer();
			
			String message = MPaymentValidate.validateCreditCardNumber(creditCardNumber, creditCardType);
			if (message.length() > 0)
				sb.append(Msg.getMsg(ctx, message)).append(" - ");
			
			message = MPaymentValidate.validateCreditCardVV(creditCardVerificationCode, creditCardType);
			if (message.length() > 0)
				sb.append(Msg.getMsg(ctx, message)).append (" - ");
			
			int creditCardExpMM = 0;
			int creditCardExpYY = 0;
			try
			{
				creditCardExpMM = Integer.parseInt(creditCardExpiryMonth);
				creditCardExpYY = Integer.parseInt(creditCardExpiryYear);
			}
			catch (NumberFormatException ex)
			{
				// Do nothing, MPaymentValidate.validateCreditCardExp() will pick it up
			}
			
			message = MPaymentValidate.validateCreditCardExp(creditCardExpMM, creditCardExpYY);
			if (message.length() > 0)
				sb.append(Msg.getMsg(ctx, message)).append(" - ");
			
			if (sb.length() > 0)
				return getErrorStandardResponse("Invalid credit card details: " + sb.toString(), trxName);				
			
			payment.setCreditCardType(creditCardType);
			payment.setCreditCardNumber(creditCardNumber);
			payment.setCreditCardVV(creditCardVerificationCode);
			payment.setCreditCardExpMM(creditCardExpMM);
			payment.setCreditCardExpYY(creditCardExpYY);
			payment.setA_Name(accountName);
			payment.setA_Street(accountStreet);
			payment.setA_City(accountCity);
			payment.setA_Zip(accountZip);
			payment.setA_State(accountState);
			payment.setA_Country(accountCountry);
			
			if (savePaymentInformation)
			{
				// Create new bank account
				MBPBankAccount bpBankAccount = new MBPBankAccount(ctx, 0, trxName);
				bpBankAccount.setAD_User_ID(userId);
				if (!bpBankAccount.save())
					return getErrorStandardResponse("Invalid credit card details: " + sb.toString(), trxName);				
				
				payment.saveToBP_BankAccount(bpBankAccount);
			}
		}
		// Load credit card details from bank account
		else
		{
			boolean bpBankAccountSet = false;
			
			MBPBankAccount[] bpBankAccounts = MBPBankAccount.getOfBPartner(ctx, businessPartnerId);
			for (MBPBankAccount bpBankAccount : bpBankAccounts)
			{
				if (bpBankAccount.getC_BP_BankAccount_ID() == businessPartnerBankAccountId)
				{
					payment.setBP_BankAccount(bpBankAccount);
					bpBankAccountSet = true;
				}
			}	
			
			if (!bpBankAccountSet)
				return getErrorStandardResponse("Invalid businessPartnerBankAccountId - Cannot load from Business Partner", trxName);
		}
		
		// Process payment
		// TODO: Should payment be saved here (bank account would have already been created)
		if (!payment.processOnline())
			return getErrorStandardResponse("Failed to process payment: " + payment.getErrorMessage(), trxName);				
			
		payment.processIt(DocAction.ACTION_Complete);
		payment.save();
		
		// TODO: Send thanks email?
		// TODO: Handle credit card validation?
		// TODO: Handle web orders or normal orders?
		
		return getStandardResponse(true, "Payment has been created and processed", trxName, payment.getC_Payment_ID());
	}
	
	public StandardResponse readPayment(ReadPaymentRequest readPaymentRequest)
	{
		return getErrorStandardResponse("Failed - readPayment() hasn't been implemented", null);
	}
	
	public StandardResponse updatePayment(UpdatePaymentRequest updatePaymentRequest)
	{
		return getErrorStandardResponse("Failed - updatePayment() hasn't been implemented", null);
	}
	
	public StandardResponse deletePayment(DeletePaymentRequest deletePaymentRequest)
	{
		return getErrorStandardResponse("Failed - deletePayment() hasn't been implemented", null);
	}
	
	public StandardResponse processPayment(ProcessPaymentRequest processPaymentRequest)
	{
		return getErrorStandardResponse("Failed - processPayment() hasn't been implemented", null);
	}
}
