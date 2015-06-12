package com.conversant.webservice;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Properties;
import java.util.logging.Level;

import javax.jws.WebService;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.compiere.model.MAttributeSetInstance;
import org.compiere.model.MBPBankAccount;
import org.compiere.model.MBPartner;
import org.compiere.model.MBankAccount;
import org.compiere.model.MDocType;
import org.compiere.model.MInvoice;
import org.compiere.model.MInvoiceLine;
import org.compiere.model.MInvoiceEx;
import org.compiere.model.MInvoicePaySchedule;
import org.compiere.model.MPayment;
import org.compiere.model.MPaymentValidate;
import org.compiere.model.MUser;
import org.compiere.model.I_M_Product;
import org.compiere.model.MProductCategory;
import org.compiere.model.MOrg;
import org.compiere.process.DocAction;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Msg;

import com.conversant.util.Validation;
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
		if (userId == null || userId < 1 || !Validation.validateADId(MUser.Table_Name, userId, trxName))
			return getErrorStandardResponse("Invalid userId", trxName);
		
		Integer businessPartnerId = createBPBankAccountRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || !Validation.validateADId(MBPartner.Table_Name, businessPartnerId, trxName))
			return getErrorStandardResponse("Invalid businessPartnerId", trxName);
		
		Integer organizationId = createBPBankAccountRequest.getOrgId();
		boolean validOrgId = Validation.validateADId(MOrg.Table_Name, organizationId, trxName);
		if(organizationId > 1 && !validOrgId)
			return getErrorStandardResponse("Invalid Organization id" , trxName);
		else if (organizationId > 1 && validOrgId)
			Env.setContext(ctx, "#AD_Org_ID" ,organizationId);
		
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
	
	public StandardResponse createInvoice(CreateInvoiceRequest createInvoiceRequest)
	{
		return getErrorStandardResponse("Failed - createInvoice() hasn't been implemented", null);
	}
	
	public StandardResponse readInvoice(ReadInvoiceRequest readInvoiceRequest)
	{
		return getErrorStandardResponse("Failed - readInvoice() hasn't been implemented", null);
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
			xmlInvoice.setAmountOwing(invoice.getGrandTotal());
			
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
		
		Integer invoiceId = readInvoiceLinesRequest.getInvoiceId();
		if (invoiceId == null || invoiceId < 1 || !Validation.validateADId(MInvoice.Table_Name, invoiceId, trxName))
		{
			readInvoiceLinesResponse.setStandardResponse(getErrorStandardResponse("Invalid Invoice Id", trxName));
			return readInvoiceLinesResponse;
		}
		// TODO Auto-generated method stub
		MInvoice invoice=new MInvoice(ctx,invoiceId,trxName);
		if (invoice == null)
		{
			readInvoiceLinesResponse.setStandardResponse(getErrorStandardResponse("Failed to load invoice", trxName));
			return readInvoiceLinesResponse;
		}
		MInvoiceLine[] invoiceLine=invoice.getLines();
		// Create response elements
		ArrayList<InvoiceLine> xmlInvoiceLines = new ArrayList<InvoiceLine>();
		if(invoiceLine!=null)
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
					MAttributeSetInstance m_asi = new MAttributeSetInstance(ctx,m_asi_id,trxName);
					xmlInvoiceLine.setAttributeSubscriptionOccurance(m_asi != null ? m_asi.getDescription() : "");
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
}
