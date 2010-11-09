package com.conversant.webservice;

import java.math.BigDecimal;
import java.util.Properties;

import javax.jws.WebService;

import org.compiere.model.MBPBankAccount;
import org.compiere.model.MBPartner;
import org.compiere.model.MBankAccount;
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
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("ACCOUNTING_WEBSERVICE"), WebServiceConstants.ACCOUNTING_WEBSERVICE_METHODS.get("CREATE_PAYMENT_METHOD_ID"), createPaymentRequest.getLoginRequest(), trxName);		
		if (error != null)	
			return getErrorStandardResponse(error, trxName);
		
		// Load and validate parameters
		Integer bankAccountId = createPaymentRequest.getBankAccountId();
		if (bankAccountId == null || bankAccountId < 1 || !validateADId(MBankAccount.Table_Name, bankAccountId, trxName))
			return getErrorStandardResponse("Invalid bankAccountId", trxName);
		
		Integer businessPartnerId = createPaymentRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || !validateADId(MBPartner.Table_Name, businessPartnerId, trxName))
			return getErrorStandardResponse("Invalid businessPartnerId", trxName);
		
		Integer businessPartnerBankAccountId = createPaymentRequest.getBusinessPartnerBankAccountId();
		if (businessPartnerBankAccountId == null || businessPartnerBankAccountId < 1 || !validateADId(MBPBankAccount.Table_Name, businessPartnerBankAccountId, trxName))
			return getErrorStandardResponse("Invalid businessPartnerBankAccountId", trxName);
		
		BigDecimal amount = createPaymentRequest.getAmount();
		if (amount == null || amount.compareTo(Env.ZERO) < 1)
			return getErrorStandardResponse("Invalid amount", trxName);

		// Create payment
		MPayment payment = new MPayment(ctx, 0, trxName);
		payment.setIsSelfService(true);
		payment.setIsOnline(true);
		payment.setAmount(0, amount);
//		payment.setAD_Org_ID(Env.getAD_Org_ID(ctx)); 
		payment.setBankAccountDetails(bankAccountId);
		
		// Sales transaction
		payment.setC_DocType_ID(true);
		payment.setTrxType(MPayment.TRXTYPE_Sales);
		payment.setTenderType(MPayment.TENDERTYPE_CreditCard);

		// Load credit card details from bank account	
		boolean bpBankAccountSet = false;		
		MBPBankAccount[] bpBankAccounts = MBPBankAccount.getOfBPartner(ctx, businessPartnerId, trxName);
		for (MBPBankAccount bpBankAccount : bpBankAccounts)
		{
			if (bpBankAccount.getC_BP_BankAccount_ID() == businessPartnerBankAccountId)
			{
				payment.setBP_BankAccount(bpBankAccount);
				bpBankAccountSet = true;
				break;
			}
		}	
		
		if (!bpBankAccountSet)
			return getErrorStandardResponse("Invalid businessPartnerBankAccountId - Cannot load from Business Partner", trxName);
		
		// Save payment
		if (!payment.save())
			return getErrorStandardResponse("Failed to save payment", trxName);
		
		// TODO: Send thanks email?
		// TODO: Handle credit card validation?
		// TODO: Handle web orders or normal orders?
		
		return getStandardResponse(true, "Payment has been created", trxName, payment.getC_Payment_ID());
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
		// Create ctx and trxName (if not specified)
		Properties ctx = Env.getCtx(); 
		String trxName = getTrxName(processPaymentRequest.getLoginRequest());
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("ACCOUNTING_WEBSERVICE"), WebServiceConstants.ACCOUNTING_WEBSERVICE_METHODS.get("PROCESS_PAYMENT_METHOD_ID"), processPaymentRequest.getLoginRequest(), trxName);		
		if (error != null)	
			return getErrorStandardResponse(error, trxName);
		
		// Load and validate parameters
		Integer businessPartnerId = processPaymentRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || !validateADId(MBPartner.Table_Name, businessPartnerId, trxName))
			return getErrorStandardResponse("Invalid businessPartnerId", trxName);
		
		Integer paymentId = processPaymentRequest.getPaymentId();
		if (paymentId == null || paymentId < 1 || !validateADId(MPayment.Table_Name, paymentId, trxName))
			return getErrorStandardResponse("Invalid paymentId", trxName);
		
		String creditCardVerificationCode = processPaymentRequest.getCreditCardVerificationCode();
		if (!validateString(creditCardVerificationCode) || !MPaymentValidate.checkNumeric(creditCardVerificationCode).equals(creditCardVerificationCode))
			return getErrorStandardResponse("Invalid creditCardVerificationCode", trxName);
		
		// Load payment
		MPayment payment = new MPayment(ctx, paymentId, trxName);
		if (payment == null)
			return getErrorStandardResponse("Failed to load payment", trxName);
			
		// Set CCVC for processing online
		payment.setCreditCardVV(creditCardVerificationCode); // already been validated in createBPBankAccount()
		
		// Process payment
		if (!payment.processOnline())
			return getErrorStandardResponse("Failed to process payment online - " + payment.getErrorMessage(), trxName);				
			
		// Set CCVC field blank (never store)
		payment.setCreditCardVV("");
		
		// Complete payment document
		StringBuilder errorMsg = new StringBuilder();
		if (!payment.processIt(DocAction.ACTION_Complete))
			errorMsg.append("Failed to action 'Complete' process on payment document - ");			
			
		// Save payment
		if (!payment.save())
			errorMsg.append("Failed to save payment - ");
			
		// Payment was processed online, need to fix manually
		if (errorMsg.length() > 1)
		{
			errorMsg.append("Payment has been processed online, please fix manually - " + payment);
			return getErrorStandardResponse(errorMsg.toString(), trxName);
		}
		
		return getStandardResponse(true, "Payment has been processed", trxName, payment.getC_Payment_ID());
	}
	
	public StandardResponse createBPBankAccount(CreateBPBankAccountRequest createBPBankAccountRequest)
	{
		// Create ctx and trxName (if not specified)
		Properties ctx = Env.getCtx(); 
		String trxName = getTrxName(createBPBankAccountRequest.getLoginRequest());
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("ACCOUNTING_WEBSERVICE"), WebServiceConstants.ACCOUNTING_WEBSERVICE_METHODS.get("CREATE_BP_BANK_ACCOUNT_METHOD_ID"), createBPBankAccountRequest.getLoginRequest(), trxName);		
		if (error != null)	
			return getErrorStandardResponse(error, trxName);
		
		// Load and validate parameters
		Integer userId = createBPBankAccountRequest.getUserId();
		if (userId == null || userId < 1 || !validateADId(MUser.Table_Name, userId, trxName))
			return getErrorStandardResponse("Invalid userId", trxName);
		
		Integer businessPartnerId = createBPBankAccountRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || !validateADId(MBPartner.Table_Name, businessPartnerId, trxName))
			return getErrorStandardResponse("Invalid businessPartnerId", trxName);
		
		// TODO: Return error message on mandatory missing params
		// Credit card data
		String creditCardType = createBPBankAccountRequest.getCreditCardType();
		if (!validateString(creditCardType))
			return getErrorStandardResponse("Invalid creditCardType", trxName);
		
		String creditCardNumber = createBPBankAccountRequest.getCreditCardNumber();
		if (!validateString(creditCardNumber))
			return getErrorStandardResponse("Invalid creditCardNumber", trxName);
		
		String creditCardVerificationCode = createBPBankAccountRequest.getCreditCardVerificationCode();
		if (!validateString(creditCardVerificationCode))
			return getErrorStandardResponse("Invalid creditCardVerificationCode", trxName);
		
		Integer creditCardExpiryMonth = createBPBankAccountRequest.getCreditCardExpiryMonth();
		if (creditCardExpiryMonth == null || creditCardExpiryMonth < 1 || creditCardExpiryMonth > 12)
			return getErrorStandardResponse("Invalid creditCardExpiryMonth", trxName);
				
		Integer creditCardExpiryYear = createBPBankAccountRequest.getCreditCardExpiryYear();
		if (creditCardExpiryYear == null || creditCardExpiryYear < 0 || creditCardExpiryYear > 99)
			return getErrorStandardResponse("Invalid creditCardExpiryYear", trxName);
		
		String accountName = createBPBankAccountRequest.getAccountName();
		if (!validateString(accountName))
			return getErrorStandardResponse("Invalid accountName", trxName);
		
		String accountStreet = createBPBankAccountRequest.getAccountStreet();
		if (!validateString(accountStreet))
			return getErrorStandardResponse("Invalid accountStreet", trxName);
		
		String accountCity = createBPBankAccountRequest.getAccountCity();
		if (!validateString(accountCity))
			return getErrorStandardResponse("Invalid accountCity", trxName);
		
		String accountZip = createBPBankAccountRequest.getAccountZip();
		if (!validateString(accountZip))
			return getErrorStandardResponse("Invalid accountZip", trxName);
		
		String accountState = createBPBankAccountRequest.getAccountState();
		if (!validateString(accountState))
			return getErrorStandardResponse("Invalid accountState", trxName);
		
		String accountCountry = createBPBankAccountRequest.getAccountCountry();
		if (!validateString(accountCountry))
			return getErrorStandardResponse("Invalid accountCountry", trxName);
		
		// Validate credit card details
		StringBuffer sb = new StringBuffer();
		
		String message = MPaymentValidate.validateCreditCardNumber(creditCardNumber, creditCardType);
		if (message.length() > 0)
			sb.append(Msg.getMsg(ctx, message)).append(" - ");
		
		message = MPaymentValidate.validateCreditCardVV(creditCardVerificationCode, creditCardType);
		if (message.length() > 0)
			sb.append(Msg.getMsg(ctx, message)).append (" - ");
		
		message = MPaymentValidate.validateCreditCardExp(creditCardExpiryMonth, creditCardExpiryYear);
		if (message.length() > 0)
			sb.append(Msg.getMsg(ctx, message)).append(" - ");
		
		if (sb.length() > 0)
			return getErrorStandardResponse("Invalid credit card details: " + sb.toString(), trxName);	
		
		// Create bank account
		MBPBankAccount bpBankAccount = new MBPBankAccount(ctx, 0, trxName);
		bpBankAccount.setAD_User_ID(userId);
		bpBankAccount.setC_BPartner_ID(businessPartnerId);
		bpBankAccount.setA_Name(accountName);
		bpBankAccount.setA_Street(accountStreet);
		bpBankAccount.setA_City(accountCity);
		bpBankAccount.setA_Zip(accountZip);
		bpBankAccount.setA_State(accountState);
		bpBankAccount.setA_Country(accountCountry);
		bpBankAccount.setCreditCardType(creditCardType);
		bpBankAccount.setCreditCardNumber(creditCardNumber);
//		bpBankAccount.setCreditCardVV(creditCardVerificationCode); // Don't save CCVC (just validate)
		bpBankAccount.setCreditCardExpMM(creditCardExpiryMonth);
		bpBankAccount.setCreditCardExpYY(creditCardExpiryYear);
		
		if (!bpBankAccount.save())
			return getErrorStandardResponse("Failed to save BP Bank Account", trxName);				

		return getStandardResponse(true, "BP Bank Account has been created", trxName, bpBankAccount.getC_BP_BankAccount_ID());
	}
	
	public StandardResponse readBPBankAccount(ReadBPBankAccountRequest readBPBankAccountRequest)
	{
		return getErrorStandardResponse("Failed - readBPBankAccount() hasn't been implemented", null);
	}
	
	public StandardResponse updateBPBankAccount(UpdateBPBankAccountRequest updateBPBankAccountRequest)
	{
		return getErrorStandardResponse("Failed - updateBPBankAccount() hasn't been implemented", null);
	}
	
	public StandardResponse deleteBPBankAccount(DeleteBPBankAccountRequest deleteBPBankAccountRequest)
	{
		return getErrorStandardResponse("Failed - deleteBPBankAccount() hasn't been implemented", null);
	}
}