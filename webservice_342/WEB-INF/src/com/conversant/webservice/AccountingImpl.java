package com.conversant.webservice;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;

import javax.jws.WebService;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.compiere.model.MAttributeSetInstance;
import org.compiere.model.MBPBankAccount;
import org.compiere.model.MBPartner;
import org.compiere.model.MBPartnerEx;
import org.compiere.model.MBPartnerLocation;
import org.compiere.model.MBankAccount;
import org.compiere.model.MDocType;
import org.compiere.model.MInvoice;
import org.compiere.model.MInvoiceLine;
import org.compiere.model.MInvoiceEx;
import org.compiere.model.MLocation;
import org.compiere.model.MPayment;
import org.compiere.model.MPaymentValidate;
import org.compiere.model.MUser;
import org.compiere.model.I_M_Product;
import org.compiere.model.MProductCategory;
import org.compiere.model.MOrg;
import org.compiere.model.MBank;
import org.compiere.model.MInvoiceTax;
import org.compiere.process.DocAction;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Msg;

import com.conversant.util.Validation;
import com.conversant.webservice.util.WebServiceConstants;
/** Braintree jars*/
import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.Environment;
import com.braintreegateway.Result;
import com.braintreegateway.TransactionRequest;
import com.braintreegateway.Transaction;

@WebService(endpointInterface = "com.conversant.webservice.Accounting")
public class AccountingImpl extends GenericWebServiceImpl implements Accounting
{
	private String defaultMerchantAccount = null;
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
		if (bankAccountId == null || bankAccountId < 1 || !Validation.validateADId(MBankAccount.Table_Name, bankAccountId, trxName))
			return getErrorStandardResponse("Invalid bankAccountId", trxName);
		
		Integer businessPartnerId = createPaymentRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || !Validation.validateADId(MBPartner.Table_Name, businessPartnerId, trxName))
			return getErrorStandardResponse("Invalid businessPartnerId", trxName);
		
		Integer businessPartnerBankAccountId = createPaymentRequest.getBusinessPartnerBankAccountId();
		if (businessPartnerBankAccountId == null || businessPartnerBankAccountId < 1 || !Validation.validateADId(MBPBankAccount.Table_Name, businessPartnerBankAccountId, trxName))
			return getErrorStandardResponse("Invalid businessPartnerBankAccountId", trxName);
		
		BigDecimal amount = createPaymentRequest.getAmount();
		if (amount == null || amount.compareTo(Env.ZERO) < 1)
			return getErrorStandardResponse("Invalid amount", trxName);
		
		Integer organizationId = createPaymentRequest.getOrgId();
		boolean validOrgId = Validation.validateADId(MOrg.Table_Name, organizationId, trxName);
		if(organizationId > 1 && !validOrgId)
			return getErrorStandardResponse("Invalid Organization id" , trxName);
		else if (organizationId > 1 && validOrgId)
			Env.setContext(ctx, "#AD_Org_ID" ,organizationId);

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
		if (businessPartnerId == null || businessPartnerId < 1 || !Validation.validateADId(MBPartner.Table_Name, businessPartnerId, trxName))
			return getErrorStandardResponse("Invalid businessPartnerId", trxName);
		
		Integer paymentId = processPaymentRequest.getPaymentId();
		if (paymentId == null || paymentId < 1 || !Validation.validateADId(MPayment.Table_Name, paymentId, trxName))
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
		if ((userId != null || userId > 1) && !Validation.validateADId(MUser.Table_Name, userId, trxName))
			return getErrorStandardResponse("Invalid User Id", trxName);
				
		Integer businessPartnerId = createBPBankAccountRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || !Validation.validateADId(MBPartner.Table_Name, businessPartnerId, trxName))
			return getErrorStandardResponse("Invalid businessPartnerId", trxName);
		
		Integer organizationId = createBPBankAccountRequest.getOrgId();
		boolean validOrgId = Validation.validateADId(MOrg.Table_Name, organizationId, trxName);
		if(organizationId > 1 && !validOrgId)
			return getErrorStandardResponse("Invalid Organization id" , trxName);
		else if (organizationId > 1 && validOrgId)
			Env.setContext(ctx, "#AD_Org_ID" ,organizationId);
		
		Integer locationId = createBPBankAccountRequest.getLocationId();
		if (locationId != null && locationId > 1 && !Validation.validateADId(MBPartnerLocation.Table_Name, locationId, trxName))
			return getErrorStandardResponse("Invalid locationId", trxName);
		if (locationId > 0)
		{
			String sql = "SELECT COUNT(*) FROM C_BPARTNER_LOCATION WHERE C_BPARTNER_ID = ? AND C_BPARTNER_LOCATION_ID = ?";
			int success = DB.getSQLValue(trxName, sql.toString(),businessPartnerId,locationId);
			if (success == -1)
				return getErrorStandardResponse("Business Partner Location doesn't belong to the Business Partner [ "+businessPartnerId+"]",trxName);
		}
		
		// TODO: Return error message on mandatory missing params
		// Credit card data
		String creditCardType = createBPBankAccountRequest.getCreditCardType();
		if (!validateString(creditCardType))
			creditCardType = null;
		else
			creditCardType = creditCardType.trim();
		
		String creditCardNumber = createBPBankAccountRequest.getCreditCardNumber();
		if (!validateString(creditCardNumber))
			creditCardNumber = null;
		else
			creditCardNumber = creditCardNumber.trim();
		
		String creditCardVerificationCode = createBPBankAccountRequest.getCreditCardVerificationCode();
		if (!validateString(creditCardVerificationCode))
			creditCardVerificationCode = null;
		else 
			creditCardVerificationCode = creditCardVerificationCode.trim();
		
		int creditCardExpiryMonth = createBPBankAccountRequest.getCreditCardExpiryMonth();
		/*if (creditCardExpiryMonth != 0 && (creditCardExpiryMonth < 1 || creditCardExpiryMonth > 12))
			return getErrorStandardResponse("Invalid creditCardExpiryMonth", trxName);*/
				
		int creditCardExpiryYear = createBPBankAccountRequest.getCreditCardExpiryYear();
		/*if (creditCardExpiryYear != 0 && (creditCardExpiryYear < 0 || creditCardExpiryYear > 99))
			return getErrorStandardResponse("Invalid creditCardExpiryYear", trxName);*/
		
		String accountName = createBPBankAccountRequest.getAccountName();
		if (!validateString(accountName))
			return getErrorStandardResponse("Invalid accountName", trxName);
		
		String accountStreet = createBPBankAccountRequest.getAccountStreet();
		if (!validateString(accountStreet))
			accountStreet =  null;
		else
			accountStreet = accountStreet.trim();
		
		String accountCity = createBPBankAccountRequest.getAccountCity();
		if (!validateString(accountCity))
			accountCity = null;
		else
			accountCity = accountCity.trim();
		
		String accountZip = createBPBankAccountRequest.getAccountZip();
		if (!validateString(accountZip))
			accountZip = null;
		else
			accountZip = accountZip.trim();
		
		String accountState = createBPBankAccountRequest.getAccountState();
		if (!validateString(accountState))
			accountState = null;
		else
			accountState = accountState.trim();
		
		String accountCountry = createBPBankAccountRequest.getAccountCountry();
		if (!validateString(accountCountry))
			accountCountry = null;
		else
			accountCountry = accountCountry.trim();
		
		String accountUsage = createBPBankAccountRequest.getAccountUsage();
		
		boolean ach = createBPBankAccountRequest.isACH();
		String accountType =  createBPBankAccountRequest.getAccountType();
		if (!validateString(accountType))
			accountType = null;
		else
			accountType = accountType.trim();
		Integer bankId = createBPBankAccountRequest.getBankId();
		if (bankId != null && bankId > 1 && !Validation.validateADId(MBank.Table_Name, bankId, trxName))
			return getErrorStandardResponse("Invalid Bank Id", trxName);
		
		String accountNumber = createBPBankAccountRequest.getAccountNo();
		if (!validateString(accountNumber))
			accountNumber = null;
		else
			accountNumber = accountNumber.trim();
		
		
		// Validate credit card details
		StringBuffer sb = new StringBuffer();
		if (creditCardType != null && creditCardNumber != null && creditCardVerificationCode != null && creditCardExpiryMonth > 0 && creditCardExpiryYear > 0)
		{
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
		}
		
		// Create bank account
		MBPBankAccount bpBankAccount = new MBPBankAccount(ctx, 0, trxName);
		if (userId >1)
			bpBankAccount.setAD_User_ID(userId);
		bpBankAccount.setC_BPartner_ID(businessPartnerId);
		bpBankAccount.setA_Name(accountName);
		if (accountStreet != null)
			bpBankAccount.setA_Street(accountStreet);
		if (accountCity != null)
			bpBankAccount.setA_City(accountCity);
		if (accountZip != null)
			bpBankAccount.setA_Zip(accountZip);
		if (accountState != null)
			bpBankAccount.setA_State(accountState);
		if (accountCountry != null)
			bpBankAccount.setA_Country(accountCountry);
		if (creditCardType != null)
			bpBankAccount.setCreditCardType(creditCardType);
		if (creditCardNumber != null)
			bpBankAccount.setCreditCardNumber(creditCardNumber);
//		bpBankAccount.setCreditCardVV(creditCardVerificationCode); // Don't save CCVC (just validate)
		if (creditCardExpiryMonth > 0)
			bpBankAccount.setCreditCardExpMM(creditCardExpiryMonth);
		if (creditCardExpiryYear > 0)
			bpBankAccount.setCreditCardExpYY(creditCardExpiryYear);
		bpBankAccount.setIsACH(ach);
		if (accountNumber != null)
			bpBankAccount.setAccountNo(accountNumber);
		if (accountType !=null && validateAccountType(accountType))
			bpBankAccount.setBankAccountType(accountType);
		if (accountUsage !=null && validateAccountUse(accountUsage))
			bpBankAccount.setBPBankAcctUse(accountUsage);
		if (bankId != null && bankId > 1)
			bpBankAccount.setC_Bank_ID(bankId);
		if(locationId > 0)
			bpBankAccount.setC_BPartner_Location_ID(locationId);
		if (!bpBankAccount.save())
			return getErrorStandardResponse("Failed to save BP Bank Account", trxName);			
		
		return getStandardResponse(true, "BP Bank Account has been created", trxName, bpBankAccount.getC_BP_BankAccount_ID());
	}
	
	public ReadBPBankAccountResponse readBPBankAccount(ReadBPBankAccountRequest readBPBankAccountRequest)
	{
		// Create ctx and trxName (if not specified)
		Properties ctx = Env.getCtx(); 
		String trxName = getTrxName(readBPBankAccountRequest.getLoginRequest());
		// Create response
		ObjectFactory objectFactory = new ObjectFactory();
		ReadBPBankAccountResponse readBPBankAccountResponse = objectFactory.createReadBPBankAccountResponse();
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("ACCOUNTING_WEBSERVICE"), WebServiceConstants.ACCOUNTING_WEBSERVICE_METHODS.get("READ_BP_BANK_ACCOUNT_METHOD_ID"), readBPBankAccountRequest.getLoginRequest(), trxName);		
		if (error != null)	
		{
			readBPBankAccountResponse.setStandardResponse(getErrorStandardResponse(error, trxName));
			return readBPBankAccountResponse;
		}
		
		// Load and validate parameters
		Integer businessPartnerId = readBPBankAccountRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || !Validation.validateADId(MBPartner.Table_Name, businessPartnerId, trxName))
		{
			readBPBankAccountResponse.setStandardResponse(getErrorStandardResponse("Invalid businessPartnerId", trxName));
			return readBPBankAccountResponse;
		}
		
		// Get all Bank Account details belonging to business partner
		MBPBankAccount[] bankAccounts = MBPBankAccount.getOfBPartner(ctx, businessPartnerId, trxName);
		
		// Create response elements
		ArrayList<BPBankAccount> xmlBPBankAccounts = new ArrayList<BPBankAccount>();		
		for (MBPBankAccount acct : bankAccounts)
		{
			BPBankAccount xmlBPBankAccount = objectFactory.createBPBankAccount();
			xmlBPBankAccount.setBusinessPartnerId(acct.getC_BPartner_ID());
	        xmlBPBankAccount.setUserId(acct.getAD_User_ID());
	        xmlBPBankAccount.setBankId(acct.getC_Bank_ID());
	        xmlBPBankAccount.setACH(acct.isACH());
	        xmlBPBankAccount.setAccountName(acct.getA_Name());
	        xmlBPBankAccount.setAccountNumber(acct.getAccountNo());
	        xmlBPBankAccount.setAccountType(acct.getBankAccountType());
	        xmlBPBankAccount.setAccountUsage(acct.getBPBankAcctUse());
	        xmlBPBankAccount.setCreditCardNumber(acct.getCreditCardNumber());
	        xmlBPBankAccount.setCreditCardType(acct.getCreditCardType());
	        xmlBPBankAccount.setCreditCardExpiryMonth(acct.getCreditCardExpMM());
	        xmlBPBankAccount.setCreditCardExpiryYear(acct.getCreditCardExpYY());
	        xmlBPBankAccount.setCreditCardVerificationCode(acct.getCreditCardVV());
	        xmlBPBankAccount.setOrgId(acct.getAD_Org_ID());
	        xmlBPBankAccount.setLocationId(acct.getC_BPartner_Location_ID());
	        
	        xmlBPBankAccounts.add(xmlBPBankAccount);
		}
		
		// Set response elements
		readBPBankAccountResponse.bpBankAccount = xmlBPBankAccounts;		
		readBPBankAccountResponse.setStandardResponse(getStandardResponse(true, "Bank Accounts have been read for BusinessPartner[" + businessPartnerId + "]", trxName, xmlBPBankAccounts.size()));
		
		return readBPBankAccountResponse;
	}
	
	public StandardResponse updateBPBankAccount(UpdateBPBankAccountRequest updateBPBankAccountRequest)
	{
		// Create ctx and trxName (if not specified)
		Properties ctx = Env.getCtx(); 
		String trxName = getTrxName(updateBPBankAccountRequest.getLoginRequest());
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("ACCOUNTING_WEBSERVICE"), WebServiceConstants.ACCOUNTING_WEBSERVICE_METHODS.get("UPDATE_BP_BANK_ACCOUNT_METHOD_ID"), updateBPBankAccountRequest.getLoginRequest(), trxName);		
		if (error != null)	
			return getErrorStandardResponse(error, trxName);
		
		//Load and validate parameters
		Integer bpBankAccountId = updateBPBankAccountRequest.getBpBankAccountId();
		if (bpBankAccountId != null && bpBankAccountId > 0 && !Validation.validateADId(MBPBankAccount.Table_Name, bpBankAccountId, trxName))
			return getErrorStandardResponse("Invalid Business Partner Account Id", trxName);
		
		MBPBankAccount bpBankAccount = new MBPBankAccount(ctx , bpBankAccountId , trxName);
		
		Integer userId = updateBPBankAccountRequest.getUserId();
		if (userId !=null && userId > 1 && Validation.validateADId(MUser.Table_Name, userId, trxName))
			bpBankAccount.setAD_User_ID(userId);
		
		Integer businessPartnerId = updateBPBankAccountRequest.getBusinessPartnerId();
		if (businessPartnerId != null && businessPartnerId > 0 && Validation.validateADId(MBPartner.Table_Name, businessPartnerId, trxName))
			bpBankAccount.setC_BPartner_ID(businessPartnerId)	;
		
		Integer bankId = updateBPBankAccountRequest.getBankId();
		if (bankId != null && bankId > 0 && Validation.validateADId(MBank.Table_Name, bankId, trxName))
			bpBankAccount.setC_Bank_ID(bankId)	;
		
		Integer locationId = updateBPBankAccountRequest.getLocationId();
		if (locationId != null && locationId > 0 && Validation.validateADId(MBPartnerLocation.Table_Name, locationId, trxName))
			bpBankAccount.setC_BPartner_Location_ID(locationId)	;
		
		String creditCardType = updateBPBankAccountRequest.getCreditCardType();
		if (!validateString(creditCardType))
			creditCardType = null;
		else
			creditCardType = creditCardType.trim();
		
		if (creditCardType != null)
			bpBankAccount.setCreditCardType(creditCardType);
		
		String creditCardNumber = updateBPBankAccountRequest.getCreditCardNumber();
		if (!validateString(creditCardNumber))
			creditCardNumber = null;
		else
			creditCardNumber = creditCardNumber.trim();
		
		if (creditCardNumber != null)
			bpBankAccount.setCreditCardNumber(creditCardNumber);
		
		String creditCardVerificationCode = updateBPBankAccountRequest.getCreditCardVerificationCode();
		if (!validateString(creditCardVerificationCode))
			creditCardVerificationCode = null;
		else
			creditCardVerificationCode = creditCardVerificationCode.trim();
		
		if (creditCardVerificationCode != null)
			bpBankAccount.setCreditCardVV(creditCardVerificationCode);
		
	    int creditCardExpiryMonth = updateBPBankAccountRequest.getCreditCardExpiryMonth();
		if (creditCardExpiryMonth != 0 && creditCardExpiryMonth < 1 && creditCardExpiryMonth > 12)
			return getErrorStandardResponse("Invalid creditCardExpiryMonth", trxName);
		else 
			bpBankAccount.setCreditCardExpMM(creditCardExpiryMonth);
				
		Integer creditCardExpiryYear = updateBPBankAccountRequest.getCreditCardExpiryYear();
		if (creditCardExpiryYear != 0 && creditCardExpiryYear < 0 && creditCardExpiryYear > 99)
			return getErrorStandardResponse("Invalid creditCardExpiryYear", trxName);
		else
			bpBankAccount.setCreditCardExpYY(creditCardExpiryYear);
		
		String accountName = updateBPBankAccountRequest.getAccountName();
		if (!validateString(accountName))
			accountName = null;
		else
			accountName = accountName.trim();
		
		if (accountName != null)
			bpBankAccount.setA_Name(accountName);
		
		String accountStreet = updateBPBankAccountRequest.getAccountStreet();
		if (!validateString(accountStreet))
			accountStreet = null;
		else
			accountStreet = accountStreet.trim();
		
		if (accountStreet != null)
			bpBankAccount.setA_Street(accountStreet);
		
		String accountCity = updateBPBankAccountRequest.getAccountCity();
		if (!validateString(accountCity))
			accountCity = null;
		else
			accountCity = accountCity.trim();
		
		if (accountCity != null)
			bpBankAccount.setA_City(accountCity);
		
		String accountZip = updateBPBankAccountRequest.getAccountZip();
		if (!validateString(accountZip))
			accountZip = null;
		else
			accountZip = accountZip.trim();
		
		if (accountZip != null)
			bpBankAccount.setA_Zip(accountZip);
		
		String accountState = updateBPBankAccountRequest.getAccountState();
		if (!validateString(accountState))
			accountState = null;
		else
			accountState = accountState.trim();
		
		if (accountState != null)
			bpBankAccount.setA_State(accountState);
		
		String accountCountry = updateBPBankAccountRequest.getAccountCountry();
		if (!validateString(accountCountry))
			accountCountry = null;
		else
			accountCountry = accountCountry.trim();
		
		if (accountCountry != null)
			bpBankAccount.setA_Country(accountCountry);
		
		String accountUsage = updateBPBankAccountRequest.getAccountUsage();
		if (!validateString(accountUsage))
			accountUsage = null;
		else
			accountUsage = accountUsage.trim();
		
		if (accountUsage != null)
			bpBankAccount.setBPBankAcctUse(accountUsage);
		
		String accountType = updateBPBankAccountRequest.getAccountType();
		if (!validateString(accountType))
			accountType = null;
		else
			accountType = accountType.trim();
		
		if (accountType != null)
			bpBankAccount.setBankAccountType(accountType);
		
		String accountNo = updateBPBankAccountRequest.getAccountNo();
		if (!validateString(accountNo))
			accountNo = null;
		else
			accountNo = accountNo.trim();
		
		if (accountNo != null)
			bpBankAccount.setAccountNo(accountNo);
		
		boolean ach =  updateBPBankAccountRequest.isACH();
		bpBankAccount.setIsACH(ach);
		
		if(!bpBankAccount.save())
			return getErrorStandardResponse("Failed to save Business Partner Bank Account Details for " +  bpBankAccountId, trxName);
		
		return getStandardResponse(true, "Business Partner Bank Account " + bpBankAccountId + " has been updated", trxName, bpBankAccountId);
	}
	
	public StandardResponse deleteBPBankAccount(DeleteBPBankAccountRequest deleteBPBankAccountRequest)
	{
		boolean success = false;
		// Create ctx and trxName (if not specified)
		Properties ctx = Env.getCtx(); 
		String trxName = getTrxName(deleteBPBankAccountRequest.getLoginRequest());
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("ACCOUNTING_WEBSERVICE"), WebServiceConstants.ACCOUNTING_WEBSERVICE_METHODS.get("DELETE_BP_BANK_ACCOUNT_METHOD_ID"), deleteBPBankAccountRequest.getLoginRequest(), trxName);		
		if (error != null)	
			return getErrorStandardResponse(error, trxName);

		// Load and validate parameters
		Integer bpBankAccountId = deleteBPBankAccountRequest.getBpBankAccountId();
		/*if (bpBankAccountId == null || bpBankAccountId < 1 || !Validation.validateADId(MBPBankAccount.Table_Name, bpBankAccountId, trxName))
			return getErrorStandardResponse("Invalid Business Partner Bank Account Id", trxName);*/
		
		String name = deleteBPBankAccountRequest.getAccountName();
		
		if((bpBankAccountId == null || bpBankAccountId <= 0) && !validateString(name))
			return getErrorStandardResponse("Both Business Partner Bank Account Id and Bank Account Name cannot be empty", trxName);
		
		
		if (bpBankAccountId > 1 && !Validation.validateADId(MBPBankAccount.Table_Name, bpBankAccountId, trxName) && name.length() == 0)
		{
			return getErrorStandardResponse("Invalid Bank Account Id", trxName);
		}
		
		
		if (bpBankAccountId == 0  && !validateString(name) ) 
		{
			return getErrorStandardResponse("Invalid Bank Account name", trxName);
		}
		else
			name = name.trim();
		
		MBPBankAccount bpBankAccount = null;
		if(bpBankAccountId > 0)
		{
			bpBankAccount = new MBPBankAccount(ctx , bpBankAccountId , trxName);
			if(name.length() > 0 && !name.equals(bpBankAccount.getA_Name()))
			{
				return getErrorStandardResponse("Bank Account name belongs to a different bank account id", trxName);
			}
		}
		else if(name.length() > 0)
		{
			String s = "SELECT * FROM " +MBPBankAccount.Table_Name+ " WHERE "+MBPBankAccount.COLUMNNAME_A_Name + " = ?";
			PreparedStatement pstmt = null;
			try
			{
				pstmt = DB.prepareStatement(s, trxName);
				pstmt.setString(1, name);
				ResultSet rs = pstmt.executeQuery();
				while (rs.next())
					bpBankAccount = new MBPBankAccount(ctx , rs, trxName);
				rs.close();
				pstmt.close();
				pstmt = null;
			}
			catch (Exception e)
			{
				
			}
		}
		
		int bpId = bpBankAccount.getC_BPartner_ID();
		
		if (bpBankAccount.delete(true))
			success = true;
		else
			return getErrorStandardResponse("Failed to delete BP Bank Account for MBPartner[" + bpId + "]", trxName);
		if (success)
			return getStandardResponse(true, "BP Bank Account has been deleted for MBPartner[" + bpId + "]", trxName, bpId);
		else
			return getErrorStandardResponse("Failed to load BP Bank Account for MBPartner[" + bpId + "]", trxName);
	}
	
	public StandardResponse createInvoice(CreateInvoiceRequest createInvoiceRequest)
	{
		return getErrorStandardResponse("Failed - createInvoice() hasn't been implemented", null);
	}
	
	public ReadInvoiceResponse readInvoice(ReadInvoiceRequest readInvoiceRequest)
	{
		// Create response
		ObjectFactory objectFactory = new ObjectFactory();
		ReadInvoiceResponse readInvoiceResponse = objectFactory.createReadInvoiceResponse();
		
		// Create ctx and trxName (if not specified)
		Properties ctx = Env.getCtx(); 
		String trxName = getTrxName(readInvoiceRequest.getLoginRequest());
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("ACCOUNTING_WEBSERVICE"), WebServiceConstants.ACCOUNTING_WEBSERVICE_METHODS.get("READ_INVOICE_METHOD_ID"), readInvoiceRequest.getLoginRequest(), trxName);		
		if (error != null)	
		{
			readInvoiceResponse.setStandardResponse(getErrorStandardResponse(error, trxName));
			return readInvoiceResponse;
		}
		
		// Load and validate parameters
		Integer invoiceId = readInvoiceRequest.getInvoiceId();
		String guid =  readInvoiceRequest.getGuid();
		if ((invoiceId == null || invoiceId <= 0) && !validateString(guid))
		{
			readInvoiceResponse.setStandardResponse(getErrorStandardResponse("Invalid Invoice Id and GUID", trxName));
			return readInvoiceResponse;
		}
		
		if (invoiceId > 1 && !Validation.validateADId(MInvoice.Table_Name, invoiceId, trxName) && guid.length() == 0)
		{
			readInvoiceResponse.setStandardResponse(getErrorStandardResponse("Invalid Invoice Id", trxName));
			return readInvoiceResponse;
		}
		
		
		if (invoiceId == 0  && !validateString(guid) ) //&& guid.length() > 0
		{
			readInvoiceResponse.setStandardResponse(getErrorStandardResponse("Invalid GUID", trxName));
			return readInvoiceResponse;
		}
		else
			guid = guid.trim();
		
		MInvoice invoice = null;
		if(invoiceId > 0)
		{
			invoice =new MInvoice(ctx, invoiceId,trxName);
			if(guid.length() > 0 && !invoice.getGUID().equals(guid))
			{
				readInvoiceResponse.setStandardResponse(getErrorStandardResponse("GUID belongs to different Invoice", trxName));
				return readInvoiceResponse;
			}
		}
		else
		{
			invoice = MInvoiceEx.getInvoiceByGUID(ctx,guid,trxName);
		}
		
		// Get Invoice
		if(invoice == null)
		{
			readInvoiceResponse.setStandardResponse(getErrorStandardResponse("Cannot load Invoice", trxName));
			return readInvoiceResponse;
		}

		// Create response user element
		Invoice xmlInvoice = objectFactory.createInvoice();
		xmlInvoice.setInvoiceId(invoice.getC_Invoice_ID());
		xmlInvoice.setDocumentNo(invoice.getDocumentNo());
		xmlInvoice.setDocTypeTargetId(invoice.getC_DocTypeTarget_ID());
		xmlInvoice.setBusinessPartnerId(invoice.getC_BPartner_ID());
		
		String bpSearchKey = DB.getSQLValueString(null,"SELECT Value from C_BPartner where C_BPartner_ID = ?",invoice.getC_BPartner_ID());
		xmlInvoice.setBpSearchKey(bpSearchKey);
		
		xmlInvoice.setSalesRepId(invoice.getSalesRep_ID());
		xmlInvoice.setPaymentMethod(invoice.getPaymentRule());
		xmlInvoice.setPaymentTermId(invoice.getC_PaymentTerm_ID()); 
		Timestamp invoiceDueDate= DB.getSQLValueTS(null, "SELECT DueDate FROM C_InvoicePaySchedule WHERE C_Invoice_ID = ?", invoice.get_ID());
		if (invoiceDueDate != null)
		{
			try
			{
				GregorianCalendar c = new GregorianCalendar();
				c.setTime(invoiceDueDate);
				xmlInvoice.setInvoiceDueDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(c));
			}
			catch (DatatypeConfigurationException ex)
			{
				log.severe("Failed to set Invoice Due Date for web service request readInvoice() for " + invoice + " - " + ex);
			}
		}

		xmlInvoice.setBusinessPartnerLocationId(invoice.getC_BPartner_Location_ID());	
		xmlInvoice.setTotalLines(invoice.getTotalLines().intValue());
		xmlInvoice.setGrandTotal(invoice.getGrandTotal());
		xmlInvoice.setOrganizationId(invoice.getAD_Org_ID());
		
		if (invoice.getDocStatus().equals(MInvoice.DOCSTATUS_Reversed))
			xmlInvoice.setAmountOwing(BigDecimal.ZERO);
		else
			xmlInvoice.setAmountOwing(invoice.getGrandTotal());
		
		if (invoice.getGUID() != null)
			xmlInvoice.setGuid(invoice.getGUID());
		else
			xmlInvoice.setGuid("");
		
		// Get amount owing (with or without pay schedule)
		String sql = "SELECT invoiceOpen(i.C_Invoice_ID, NULL) FROM C_Invoice i WHERE i.C_Invoice_ID = ? AND i.IsPayScheduleValid<>'Y'";
		sql += " UNION ";
		sql += "SELECT invoiceOpen(i.C_Invoice_ID, ips.C_InvoicePaySchedule_ID) FROM C_Invoice i INNER JOIN C_InvoicePaySchedule ips ON i.C_Invoice_ID = ips.C_Invoice_ID WHERE i.C_Invoice_ID = ? AND i.IsPayScheduleValid='Y' AND ips.IsValid='Y' AND ips.DueAmt>0 ";
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement(sql, null);
			pstmt.setInt(1, invoice.getC_Invoice_ID());
			pstmt.setInt(2, invoice.getC_Invoice_ID());
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				BigDecimal amountOwing = rs.getBigDecimal(1);
				if (amountOwing != null)
					xmlInvoice.setAmountOwing(amountOwing);
			}
		}
		catch (Exception ex)
		{
			log.log (Level.SEVERE, sql, ex);
		}
		finally
		{
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}
		
		try
		{
			GregorianCalendar c = new GregorianCalendar();
			c.setTime(invoice.getDateInvoiced());
			xmlInvoice.setDateInvoiced(DatatypeFactory.newInstance().newXMLGregorianCalendar(c));
		}
		catch (DatatypeConfigurationException ex)
		{
			log.severe("Failed to set DateInvoiced for web service request to readInvoicesByBusinessPartner() for " + invoice + " - " + ex);
		}
		
		try
		{
			xmlInvoice.setCurrency(invoice.getC_Currency().getISO_Code());
		}
		catch (Exception ex)
		{
			xmlInvoice.setCurrency("");
		}
		
		
		// Set response elements
		readInvoiceResponse.invoice = xmlInvoice;		
		readInvoiceResponse.setStandardResponse(getStandardResponse(true, "Invoice have been read for Invoice Id[" + invoiceId + "]", trxName, invoiceId));
		
		return readInvoiceResponse;
	}
	
	public StandardResponse updateInvoice(UpdateInvoiceRequest updateInvoiceRequest)
	{
		return getErrorStandardResponse("Failed - updateInvoice() hasn't been implemented", null);
	}
	
	public StandardResponse deleteInvoice(DeleteInvoiceRequest deleteInvoiceRequest)
	{
		return getErrorStandardResponse("Failed - deleteInvoice() hasn't been implemented", null);
	}
	
	public ReadInvoicesByBusinessPartnerResponse readInvoicesByBusinessPartner(ReadInvoicesByBusinessPartnerRequest readInvoicesByBusinessPartnerRequest)
	{
		// Create response
		ObjectFactory objectFactory = new ObjectFactory();
		ReadInvoicesByBusinessPartnerResponse readInvoicesByBusinessPartnerResponse = objectFactory.createReadInvoicesByBusinessPartnerResponse();
		
		// Create ctx and trxName (if not specified)
		Properties ctx = Env.getCtx(); 
		String trxName = getTrxName(readInvoicesByBusinessPartnerRequest.getLoginRequest());
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("ACCOUNTING_WEBSERVICE"), WebServiceConstants.ACCOUNTING_WEBSERVICE_METHODS.get("READ_INVOICES_BY_BUSINESS_PARTNER_METHOD_ID"), readInvoicesByBusinessPartnerRequest.getLoginRequest(), trxName);		
		if (error != null)	
		{
			readInvoicesByBusinessPartnerResponse.setStandardResponse(getErrorStandardResponse(error, trxName));
			return readInvoicesByBusinessPartnerResponse;
		}

		// Load and validate parameters
		Integer docTypeTargetId = readInvoicesByBusinessPartnerRequest.getDocTypeTargetId();
		if (docTypeTargetId == null || docTypeTargetId < 1 || !Validation.validateADId(MDocType.Table_Name, docTypeTargetId, trxName))
			docTypeTargetId = null;
		
		Integer businessPartnerId = readInvoicesByBusinessPartnerRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || !Validation.validateADId(MBPartner.Table_Name, businessPartnerId, trxName))
		{
			readInvoicesByBusinessPartnerResponse.setStandardResponse(getErrorStandardResponse("Invalid businessPartnerId", trxName));
			return readInvoicesByBusinessPartnerResponse;
		}
		
		// Get all invoices belonging to business partner
		MInvoice[] invoices = MInvoiceEx.getOfBPartnerOrdered(ctx, businessPartnerId, trxName);
		
		// Create response elements
		ArrayList<Invoice> xmlInvoices = new ArrayList<Invoice>();		
		for (MInvoice invoice : invoices)
		{
			// Exclude incomplete invoices
			if (!invoice.isComplete())
				continue;
			
			// If Doc Type Target specified then match against invoice
			if (docTypeTargetId != null && invoice.getC_DocTypeTarget_ID() != docTypeTargetId)
				continue;
			
			Invoice xmlInvoice = objectFactory.createInvoice();
			xmlInvoice.setInvoiceId(invoice.getC_Invoice_ID());
			xmlInvoice.setDocumentNo(invoice.getDocumentNo());
			xmlInvoice.setDocTypeTargetId(invoice.getC_DocTypeTarget_ID());
			xmlInvoice.setBusinessPartnerId(invoice.getC_BPartner_ID());
			xmlInvoice.setBusinessPartnerLocationId(invoice.getC_BPartner_Location_ID());			
			xmlInvoice.setTotalLines(invoice.getTotalLines().intValue());
			xmlInvoice.setGrandTotal(invoice.getGrandTotal());
			xmlInvoice.setOrganizationId(invoice.getAD_Org_ID());
			xmlInvoice.setGuid(invoice.getGUID());
			if (invoice.getDocStatus().equals(MInvoice.DOCSTATUS_Reversed))
				xmlInvoice.setAmountOwing(BigDecimal.ZERO);
			else
				xmlInvoice.setAmountOwing(invoice.getGrandTotal());
			
			xmlInvoice.setSalesRepId(invoice.getSalesRep_ID());
			xmlInvoice.setPaymentMethod(invoice.getPaymentRule());
			xmlInvoice.setPaymentTermId(invoice.getC_PaymentTerm_ID()); 
			Timestamp invoiceDueDate= DB.getSQLValueTS(null, "SELECT DueDate FROM C_InvoicePaySchedule WHERE C_Invoice_ID = ?", invoice.get_ID());
			if (invoiceDueDate != null)
			{
				try
				{
					GregorianCalendar c = new GregorianCalendar();
					c.setTime(invoiceDueDate);
					xmlInvoice.setInvoiceDueDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(c));
				}
				catch (DatatypeConfigurationException ex)
				{
					log.severe("Failed to set Invoice Due Date for web service request readInvoice() for " + invoice + " - " + ex);
				}
			}
			
			
			// Get amount owing (with or without pay schedule)
			String sql = "SELECT invoiceOpen(i.C_Invoice_ID, NULL) FROM C_Invoice i WHERE i.C_Invoice_ID = ? AND i.IsPayScheduleValid<>'Y'";
			sql += " UNION ";
			sql += "SELECT invoiceOpen(i.C_Invoice_ID, ips.C_InvoicePaySchedule_ID) FROM C_Invoice i INNER JOIN C_InvoicePaySchedule ips ON i.C_Invoice_ID = ips.C_Invoice_ID WHERE i.C_Invoice_ID = ? AND i.IsPayScheduleValid='Y' AND ips.IsValid='Y' AND ips.DueAmt>0 ";
			
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			try
			{
				pstmt = DB.prepareStatement(sql, null);
				pstmt.setInt(1, invoice.getC_Invoice_ID());
				pstmt.setInt(2, invoice.getC_Invoice_ID());
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					BigDecimal amountOwing = rs.getBigDecimal(1);
					if (amountOwing != null)
						xmlInvoice.setAmountOwing(amountOwing);
				}
			}
			catch (Exception ex)
			{
				log.log (Level.SEVERE, sql, ex);
			}
			finally
			{
				DB.close(rs, pstmt);
				rs = null; pstmt = null;
			}
			
			try
			{
				GregorianCalendar c = new GregorianCalendar();
				c.setTime(invoice.getDateInvoiced());
				xmlInvoice.setDateInvoiced(DatatypeFactory.newInstance().newXMLGregorianCalendar(c));
			}
			catch (DatatypeConfigurationException ex)
			{
				log.severe("Failed to set DateInvoiced for web service request to readInvoicesByBusinessPartner() for " + invoice + " - " + ex);
			}
			
			try
			{
				xmlInvoice.setCurrency(invoice.getC_Currency().getISO_Code());
			}
			catch (Exception ex)
			{
				xmlInvoice.setCurrency("");
			}
			
			xmlInvoices.add(xmlInvoice);
		}
		
		// Set response elements
		readInvoicesByBusinessPartnerResponse.invoice = xmlInvoices;		
		readInvoicesByBusinessPartnerResponse.setStandardResponse(getStandardResponse(true, "Invoices have been read for BusinessPartner[" + businessPartnerId + "]", trxName, xmlInvoices.size()));
		
		return readInvoicesByBusinessPartnerResponse;
	}
	
	public ReadInvoiceLinesResponse readInvoiceLines(ReadInvoiceLinesRequest readInvoiceLinesRequest)
	{
		// Create response
		ObjectFactory objectFactory = new ObjectFactory();
		ReadInvoiceLinesResponse readInvoiceLinesResponse = objectFactory.createReadInvoiceLinesResponse();
		
		// Create ctx and trxName (if not specified)
		Properties ctx = Env.getCtx(); 
		String trxName = getTrxName(readInvoiceLinesRequest.getLoginRequest());
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("ACCOUNTING_WEBSERVICE"), WebServiceConstants.ACCOUNTING_WEBSERVICE_METHODS.get("READ_INVOICE_LINES_METHOD_ID"), readInvoiceLinesRequest.getLoginRequest(), trxName);		
		if (error != null)	
		{
			readInvoiceLinesResponse.setStandardResponse(getErrorStandardResponse(error, trxName));
			return readInvoiceLinesResponse;
		}
		
		// Load and validate parameters
		Integer invoiceId = readInvoiceLinesRequest.getInvoiceId();
		String guid =  readInvoiceLinesRequest.getGuid();
		if ((invoiceId == null || invoiceId <= 0) && !validateString(guid))
		{
			readInvoiceLinesResponse.setStandardResponse(getErrorStandardResponse("Invalid Invoice Id and GUID", trxName));
			return readInvoiceLinesResponse;
		}
		
		if (invoiceId > 1 && !Validation.validateADId(MInvoice.Table_Name, invoiceId, trxName) && guid.length() == 0)
		{
			readInvoiceLinesResponse.setStandardResponse(getErrorStandardResponse("Invalid Invoice Id", trxName));
			return readInvoiceLinesResponse;
		}
		
		
		if (invoiceId == 0  && !validateString(guid) ) //&& guid.length() > 0
		{
			readInvoiceLinesResponse.setStandardResponse(getErrorStandardResponse("Invalid GUID", trxName));
			return readInvoiceLinesResponse;
		}
		else
			guid = guid.trim();
		
		MInvoice invoice = null;
		if(invoiceId > 0)
		{
			invoice =new MInvoice(ctx, invoiceId,trxName);
			if(guid.length() > 0 && !invoice.getGUID().equals(guid))
			{
				readInvoiceLinesResponse.setStandardResponse(getErrorStandardResponse("GUID belongs to different Invoice", trxName));
				return readInvoiceLinesResponse;
			}
		}
		else
		{
			invoice = MInvoiceEx.getInvoiceByGUID(ctx,guid,trxName);
		}
		
		// Get Invoice
		if(invoice == null)
		{
			readInvoiceLinesResponse.setStandardResponse(getErrorStandardResponse("Cannot load Invoice", trxName));
			return readInvoiceLinesResponse;
		}
		
		MInvoiceLine[] invoiceLine=invoice.getLines();
		// Create response elements
		ArrayList<InvoiceLine> xmlInvoiceLines = new ArrayList<InvoiceLine>();
		if(invoiceLine.length > 0)
		{
			for (int i = 0; i < invoiceLine.length; i++)
			{
				InvoiceLine xmlInvoiceLine=objectFactory.createInvoiceLine();
				
				xmlInvoiceLine.setInvoiceId(invoiceLine[i].getC_Invoice_ID());
				xmlInvoiceLine.setInvoiceLineId(invoiceLine[i].getC_InvoiceLine_ID());
				xmlInvoiceLine.setLine(invoiceLine[i].getLine());
				xmlInvoiceLine.setDescription(invoiceLine[i].getDescription());
				xmlInvoiceLine.setProductId(invoiceLine[i].getM_Product_ID());
				xmlInvoiceLine.setQtyInvoiced(invoiceLine[i].getQtyInvoiced());
				xmlInvoiceLine.setChargeId(invoiceLine[i].getC_Charge_ID());
				xmlInvoiceLine.setUomId(invoiceLine[i].getC_UOM_ID());
				xmlInvoiceLine.setQtyEntered(invoiceLine[i].getQtyEntered());
				xmlInvoiceLine.setPriceEntered(invoiceLine[i].getPriceEntered());
				xmlInvoiceLine.setPriceActual(invoiceLine[i].getPriceActual());
				xmlInvoiceLine.setTaxId(invoiceLine[i].getC_Tax_ID());
				xmlInvoiceLine.setLineNetAmt(invoiceLine[i].getLineNetAmt());
				xmlInvoiceLine.setLineTotalAmt(invoiceLine[i].getLineTotalAmt());
				xmlInvoiceLine.setTaxAmount(invoiceLine[i].getTaxAmt());
				//additional fields
				I_M_Product product;
				try {
					product = invoiceLine[i].getM_Product();
					xmlInvoiceLine.setProductSearchKey(product.getValue());
					xmlInvoiceLine.setProductName(product.getName());
					xmlInvoiceLine.setProductDescription(product.getDescription());
					xmlInvoiceLine.setProductType(product.getProductType());
					xmlInvoiceLine.setProductCategoryId(product.getM_Product_Category_ID());
					
					MProductCategory productCategory = (MProductCategory)product.getM_Product_Category();
					xmlInvoiceLine.setProductCategoryDescription(productCategory.getDescription() != null ? productCategory.getDescription() : "");
					xmlInvoiceLine.setProductCategoryName(productCategory.getName());
					
					if(productCategory.getM_Product_Category_Parent_ID() > 0)
					{
						MProductCategory parentProductCategory = MProductCategory.get(ctx,productCategory.getM_Product_Category_Parent_ID());
						xmlInvoiceLine.setParentProductCategoryName(parentProductCategory != null ? parentProductCategory.getName() : "");
					}
					else	
						xmlInvoiceLine.setParentProductCategoryName("");
					
					int m_asi_id = invoiceLine[i].getM_AttributeSetInstance_ID();
					if(m_asi_id > 0)
					{
						MAttributeSetInstance m_asi = new MAttributeSetInstance(ctx,m_asi_id,trxName);
						xmlInvoiceLine.setAttributeSubscriptionOccurance(m_asi != null ? m_asi.getDescription() : "");
					}
					else
						xmlInvoiceLine.setAttributeSubscriptionOccurance("");
					xmlInvoiceLine.setPeriodQty(invoiceLine[i].getPeriodQty() != null ? invoiceLine[i].getPeriodQty() : Env.ZERO);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				xmlInvoiceLines.add(xmlInvoiceLine);
			}
		}
		readInvoiceLinesResponse.invoiceLine = xmlInvoiceLines;		
		readInvoiceLinesResponse.setStandardResponse(getStandardResponse(true, "Invoice Lines have been read for MInvoice[" + invoiceId + "]", trxName, xmlInvoiceLines.size()));
		
		return readInvoiceLinesResponse;
	}
	
	/**
	 * Vaildates Bank Account Type
	 * 
	 * @param s string to validate
	 * @return true if valid
	 */
	public boolean validateAccountType(String s)
	{
		if (s.matches("C|S"))
			return true;
		return false;
	}
	
	/**
	 * Vaildates Bank Account Usage
	 * 
	 * @param s string to validate
	 * @return true if valid
	 */
	public boolean validateAccountUse(String s)
	{
		if (s.matches("B|D|N|T"))
			return true;
		return false;
	}

	public ReadInvoiceTaxLinesResponse readInvoiceTaxLines(ReadInvoiceTaxLinesRequest readInvoiceTaxLinesRequest) 
	{
		// Create response
		ObjectFactory objectFactory = new ObjectFactory();
		ReadInvoiceTaxLinesResponse readInvoiceTaxLinesResponse = objectFactory.createReadInvoiceTaxLinesResponse();
		
		// Create ctx and trxName (if not specified)
		Properties ctx = Env.getCtx(); 
		String trxName = getTrxName(readInvoiceTaxLinesRequest.getLoginRequest());
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("ACCOUNTING_WEBSERVICE"), WebServiceConstants.ACCOUNTING_WEBSERVICE_METHODS.get("READ_INVOICE_TAX_LINES_METHOD_ID"), readInvoiceTaxLinesRequest.getLoginRequest(), trxName);		
		if (error != null)	
		{
			readInvoiceTaxLinesResponse.setStandardResponse(getErrorStandardResponse(error, trxName));
			return readInvoiceTaxLinesResponse;
		}
		
		// Load and validate parameters
		Integer invoiceId = readInvoiceTaxLinesRequest.getInvoiceId();
		String guid =  readInvoiceTaxLinesRequest.getGuid();
		if ((invoiceId == null || invoiceId <= 0) && !validateString(guid))
		{
			readInvoiceTaxLinesResponse.setStandardResponse(getErrorStandardResponse("Invalid Invoice Id and GUID", trxName));
			return readInvoiceTaxLinesResponse;
		}
		
		if (invoiceId > 1 && !Validation.validateADId(MInvoice.Table_Name, invoiceId, trxName) && guid.length() == 0)
		{
			readInvoiceTaxLinesResponse.setStandardResponse(getErrorStandardResponse("Invalid Invoice Id", trxName));
			return readInvoiceTaxLinesResponse;
		}
		
		
		if (invoiceId == 0  && !validateString(guid) ) //&& guid.length() > 0
		{
			readInvoiceTaxLinesResponse.setStandardResponse(getErrorStandardResponse("Invalid GUID", trxName));
			return readInvoiceTaxLinesResponse;
		}
		else
			guid = guid.trim();
		
		MInvoice invoice = null;
		if(invoiceId > 0)
		{
			invoice =new MInvoice(ctx, invoiceId,trxName);
			if(guid.length() > 0 && !invoice.getGUID().equals(guid))
			{
				readInvoiceTaxLinesResponse.setStandardResponse(getErrorStandardResponse("GUID belongs to different Invoice", trxName));
				return readInvoiceTaxLinesResponse;
			}
		}
		else
		{
			invoice = MInvoiceEx.getInvoiceByGUID(ctx,guid,trxName);
		}
		
		// Get Invoice
		if(invoice == null)
		{
			readInvoiceTaxLinesResponse.setStandardResponse(getErrorStandardResponse("Cannot load Invoice", trxName));
			return readInvoiceTaxLinesResponse;
		}
		
		MInvoiceTax[] invTaxLines = invoice.getTaxes(false);
		// Create response elements
		ArrayList<InvoiceTaxLine> xmlInvoiceTaxLines = new ArrayList<InvoiceTaxLine>();
		if (invTaxLines.length > 0)
		{
			for(int i = 0; i < invTaxLines.length ; i++)
			{
				InvoiceTaxLine xmlInvoiceTaxLine=objectFactory.createInvoiceTaxLine();
				
				xmlInvoiceTaxLine.setInvoiceId(invTaxLines[i].getC_Invoice_ID());
				xmlInvoiceTaxLine.setTaxId(invTaxLines[i].getC_Tax_ID());
				xmlInvoiceTaxLine.setPriceIncludesTax(invTaxLines[i].isTaxIncluded());
				xmlInvoiceTaxLine.setTaxAmt(invTaxLines[i].getTaxAmt());
				xmlInvoiceTaxLine.setTaxBaseAmt(invTaxLines[i].getTaxBaseAmt());
				
				xmlInvoiceTaxLines.add(xmlInvoiceTaxLine);
			}
		}
		
		readInvoiceTaxLinesResponse.invoiceTaxLine = xmlInvoiceTaxLines;		
		readInvoiceTaxLinesResponse.setStandardResponse(getStandardResponse(true, "Invoice Tax Lines have been read for MInvoice[" + invoiceId + "]", trxName, xmlInvoiceTaxLines.size()));
		
		return readInvoiceTaxLinesResponse;
	}

	
	public ReadPaymentsByBusinessPartnerResponse readPaymentByBusinessPartner(ReadPaymentsByBusinessPartnerRequest readPaymentsByBusinessPartnerRequest) 
	{
		// Create response
		ObjectFactory objectFactory = new ObjectFactory();
		ReadPaymentsByBusinessPartnerResponse readPaymentsByBusinessPartnerResponse = objectFactory.createReadPaymentsByBusinessPartnerResponse();
		
		// Create ctx and trxName (if not specified)
		Properties ctx = Env.getCtx(); 
		String trxName = getTrxName(readPaymentsByBusinessPartnerRequest.getLoginRequest());
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("ACCOUNTING_WEBSERVICE"), WebServiceConstants.ACCOUNTING_WEBSERVICE_METHODS.get("READ_PAYMENTS_BY_BUSINESS_PARTNER_METHOD_ID"), readPaymentsByBusinessPartnerRequest.getLoginRequest(), trxName);		
		if (error != null)	
		{
			readPaymentsByBusinessPartnerResponse.setStandardResponse(getErrorStandardResponse(error, trxName));
			return readPaymentsByBusinessPartnerResponse;
		}

		// Load and validate parameters
		Integer docTypeTargetId = readPaymentsByBusinessPartnerRequest.getDocTypeTargetId();
		if (docTypeTargetId == null || docTypeTargetId < 1 || !Validation.validateADId(MDocType.Table_Name, docTypeTargetId, trxName))
			docTypeTargetId = null;
		
		Integer businessPartnerId = readPaymentsByBusinessPartnerRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || !Validation.validateADId(MBPartner.Table_Name, businessPartnerId, trxName))
		{
			readPaymentsByBusinessPartnerResponse.setStandardResponse(getErrorStandardResponse("Invalid businessPartnerId", trxName));
			return readPaymentsByBusinessPartnerResponse;
		}
		
		// Get all payments belonging to business partner
		MPayment[] payments = MPayment.getOfBPartner(ctx, businessPartnerId, trxName);
		// Create response elements
		ArrayList<Payment> xmlPayments = new ArrayList<Payment>();		
		for (MPayment payment : payments)
		{
			// Exclude incomplete invoices
			if (payment.getDocStatus().equals(MPayment.DOCSTATUS_Drafted))
				continue;
			
			// If Doc Type Target specified then match against invoice
			if (docTypeTargetId != null && payment.getC_DocType_ID() != docTypeTargetId)
				continue;
			Payment xmlPayment = objectFactory.createPayment();
			xmlPayment.setPaymentId(payment.get_ID());
			xmlPayment.setBusinessPartnerId(payment.getC_BPartner_ID());
			xmlPayment.setCurrency(payment.getCurrencyISO());
			xmlPayment.setDiscountAmount(payment.getDiscountAmt());
			xmlPayment.setDocTypeTargetId(payment.getC_DocType_ID());
			xmlPayment.setDocumentNo(payment.getDocumentNo());
			xmlPayment.setPaymentAmount(payment.getPayAmt());
			try
			{
				GregorianCalendar c = new GregorianCalendar();
				c.setTime(payment.getDateTrx());
				xmlPayment.setTransDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(c));
			}
			catch (DatatypeConfigurationException ex)
			{
				log.severe("Failed to set Transaction Date for web service request readPaymentsByBusinessPartner() for " + payment + " - " + ex);
			}
			xmlPayment.setWriteOffAmount(payment.getWriteOffAmt());
			
			xmlPayments.add(xmlPayment);
		}
		
		// Set response elements
		readPaymentsByBusinessPartnerResponse.payment = xmlPayments;		
		readPaymentsByBusinessPartnerResponse.setStandardResponse(getStandardResponse(true, "Payments have been read for BusinessPartner[" + businessPartnerId + "]", trxName, xmlPayments.size()));
		
		return readPaymentsByBusinessPartnerResponse;	
		
	}
	
	public StandardResponse createOneOffPayment(CreateOneOffPaymentRequest createOneOffPaymentRequest)
	{
		// Create ctx and trxName (if not specified)
		Properties ctx = Env.getCtx(); 
		String trxName = getTrxName(createOneOffPaymentRequest.getLoginRequest());
		int MIN_CARDDATA_LENGTH = 12;
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("ACCOUNTING_WEBSERVICE"), WebServiceConstants.ACCOUNTING_WEBSERVICE_METHODS.get("CREATE_ONE_OFF_PAYMENT_METHOD_ID"), createOneOffPaymentRequest.getLoginRequest(), trxName);		
		if (error != null)	
			return getErrorStandardResponse(error, trxName);
		
		// Load and validate parameters
		String creditCardNo =createOneOffPaymentRequest.getCreditCardNumber();
		if (creditCardNo == null || creditCardNo.length() < MIN_CARDDATA_LENGTH)
		{
			log.severe("Creditcard number must be " + MIN_CARDDATA_LENGTH + " digits or longer");
			return getErrorStandardResponse("Creditcard number must be " + MIN_CARDDATA_LENGTH + " digits or longer" ,trxName);
		}
		
		int creditCardExpiryMonth= createOneOffPaymentRequest.getCreditCardExpiryMonth();
		int creditCardExpiryYear= createOneOffPaymentRequest.getCreditCardExpiryYear();
		String errorMsg =  MPaymentValidate.validateCreditCardExp(creditCardExpiryMonth, creditCardExpiryYear);
		if (errorMsg.length() > 0)
			return getErrorStandardResponse("Credit Card has expired " , trxName);
		
		String cvv = createOneOffPaymentRequest.getCreditCardVerificationCode();
		if (!validateString(cvv) || !MPaymentValidate.checkNumeric(cvv).equals(cvv))
			return getErrorStandardResponse("Invalid creditCardVerificationCode", trxName);
		
		Integer invoiceId = createOneOffPaymentRequest.getInvoiceId();
		if (invoiceId == null || invoiceId < 1 || !Validation.validateADId(MInvoice.Table_Name, invoiceId, trxName))
		{
			return getErrorStandardResponse("Invalid Invoice Id", trxName);
		}
		MInvoice invoice = new MInvoice(ctx,invoiceId,trxName);
		
		Integer businessPartnerId = createOneOffPaymentRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || !Validation.validateADId(MBPartner.Table_Name, businessPartnerId, trxName))
			return getErrorStandardResponse("Invalid businessPartnerId", trxName);
		
		if(invoice.getC_BPartner_ID() != businessPartnerId)
			return getErrorStandardResponse("Entered Business Partner Id and Invoice BP mismatch", trxName);
		
		Integer bpLocationId = createOneOffPaymentRequest.getBusinessPartnerLocationId();
		if (bpLocationId == null || bpLocationId < 1 || !Validation.validateADId(MBPartnerLocation.Table_Name, bpLocationId, trxName))
			return getErrorStandardResponse("Invalid businessPartner location ", trxName);
		
		BigDecimal amount = createOneOffPaymentRequest.getAmount();
		if (amount == null || amount.compareTo(Env.ZERO) < 1)
			return getErrorStandardResponse("Invalid amount", trxName);
		
		Integer organizationId = createOneOffPaymentRequest.getOrgId();
		boolean validOrgId = Validation.validateADId(MOrg.Table_Name, organizationId, trxName);
		if(organizationId > 1 && !validOrgId)
			return getErrorStandardResponse("Invalid Organization id" , trxName);
		else if (organizationId > 1 && validOrgId)
			Env.setContext(ctx, "#AD_Org_ID" ,organizationId);

		BraintreeGateway gateway = getBraintreeGateway(organizationId);
		if(gateway == null)
        {
			return getErrorStandardResponse("GATEWAY(null) ERROR!!!!" , trxName);
		}
		
		if(!invoice.isPaid())
		{
			String paymentMethodToken = gateway.clientToken().generate();

			TransactionRequest request = new TransactionRequest()
			.creditCard()
				.number(creditCardNo)
				.cvv(cvv)
				.expirationMonth(creditCardExpiryMonth+"")
				.expirationYear(creditCardExpiryYear+"")
				.done()
		    .amount(amount)
		    .paymentMethodNonce(paymentMethodToken)
		    .merchantAccountId(defaultMerchantAccount)
		    .options()
		    	.submitForSettlement(true)
		    	.done();

			Result<Transaction> result = gateway.transaction().sale(request);
			String transactionMessage = result.getMessage();
			if(transactionMessage != null)
			{
				return getErrorStandardResponse("Braintree Transaction Message MInvoice [ "+invoice.getDocumentNo()+" ] "+transactionMessage , trxName);
			}
			
			Transaction transaction = result.getTarget();
			if(transaction == null)
			{
				return getErrorStandardResponse("Braintree Transaction not created for  MInvoice [ "+invoice.getDocumentNo()+" ]" , trxName);
			}
			
			// Create payment
			MPayment payment = new MPayment(ctx, 0, trxName);
			payment.setIsSelfService(true);
			payment.setIsOnline(true);
			payment.setAmount(0, amount); 
			//payment.setBankAccountDetails(bankAccountId);
			
			// Sales transaction
			payment.setC_DocType_ID(true);
			payment.setTrxType(MPayment.TRXTYPE_Sales);
			payment.setTenderType(MPayment.TENDERTYPE_CreditCard);
			payment.setCreditCardNumber(creditCardNo);
			payment.setCreditCardExpMM(creditCardExpiryMonth);
			payment.setCreditCardExpYY(creditCardExpiryYear);
			payment.setCreditCardVV(cvv);
			
			// Save payment
			if (!payment.save())
				return getErrorStandardResponse("Failed to save payment", trxName);
			
			// TODO: Send thanks email?
			// TODO: Handle credit card validation?
			// TODO: Handle web orders or normal orders?
			return getStandardResponse(true, "Payment has been created", trxName, payment.getC_Payment_ID());
		}
		return null;
	}
	
	public BraintreeGateway getBraintreeGateway(int p_AD_Org_ID) 
	{
		log.log( Level.INFO, "Retrieving Braintree Credentials from Payment Processor Form");
		
		String merchantId =null;
		String context = null;
		String publicKey = null;
		String privateKey = null;
		
		String sql = "SELECT PAYPRO.HOSTADDRESS , PAYPRO.USERID , PAYPRO.PARTNERID , PAYPRO.VENDORID , PAYPRO.PROXYADDRESS  FROM C_PAYMENTPROCESSOR PAYPRO " +
				     "WHERE PAYPRO.AD_ORG_ID = " + p_AD_Org_ID + " AND  UPPER(PAYPRO.NAME) LIKE 'BRAINTREE%'";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{	
			// Create statement and set parameters
			pstmt = DB.prepareStatement(sql.toString(), null);
			
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
		
		log.log( Level.INFO, "ENVIRONMENT :: "+context);
		log.log( Level.INFO, "MERCHANT ID :: "+merchantId);
		log.log( Level.INFO, "PUBLIC KEY :: "+publicKey);
		log.log( Level.INFO, "PRIVATE KEY :: "+privateKey);
		
		if (context.contentEquals("SANDBOX"))
			return new BraintreeGateway(Environment.SANDBOX,merchantId,publicKey,privateKey);
		else if (context.contentEquals("PRODUCTION"))
			return new BraintreeGateway(Environment.PRODUCTION,merchantId,publicKey,privateKey);
		
		return null;
    }
}
