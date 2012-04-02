package org.compiere.util;

import java.util.LinkedHashMap;

public class Flo2CashConstants {

// **********************************************************************************

	public static final int STANDARD_RESPONSE_DEFAULT_ID = -1;
	
// **********************************************************************************
	
	public static final LinkedHashMap<String, String> FLO2CASHWEBSERVICES = new LinkedHashMap<String, String>() 
	{
		{
			put("SOAP_ACTION", "http://www.flo2cash.co.nz/webservices/ddwebservice/versionone/SchedulePerInvoicePayment");			
			put("VERSION", "http://www.flo2cash.co.nz/webservices/ddwebservice/versionone");
			put("DEMO_DESTINATION_WEBSERVICE", "http://demo.flo2cash.co.nz/ddws/versionone/recurringpayments.asmx?op=SchedulePerInvoicePayment");
			put("DESTINATION_WEBSERVICE", "https://secure.flo2cash.co.nz/ddws/versionone/recurringpayments.asmx?op=SchedulePerInvoicePayment");
			put("REQUEST_USERNAME","Username");
			put("REQUEST_PASSWORD","Password");
			put("USERNAME","100950");
			put("PASSWORD","dbi33jfg");
			put("REQUEST_PLANID","PlanId");
			put("REQUEST_AMOUNT","Amount");
			put("REQUEST_PAYMENTDATE","PaymentDate");
			put("REQUEST_REFERENCE","Reference");
			put("REQUEST_PARTICULAR","Particular");
			put("PAYMENT_METHOD","SchedulePerInvoicePayment");
			put("REQUEST_PLANDETAILSNODE","SchedulePerInvoicePaymentLineInput");
			
			
			put("RESPONSE_PLANID","PlanId");
			put("RESPONSE_AMOUNT","Amount");
			put("RESPONSE_PAYMENTDATE","PaymentDate");
			put("RESPONSE_REFERENCE","Reference");
			put("RESPONSE_PARTICULAR","Particular");
			put("RESPONSE_PAYMENTID","PaymentId");
			put("RESPONSE_DDTRANSACTIONNUMBER","DDTransactionNumber");
			put("RESPONSE_STATUSID","StatusId");
			put("RESPONSE_PLANDETAILSNODE","SchedulePerInvoicePaymentLineOutput");
		}
	};
	
	
}
