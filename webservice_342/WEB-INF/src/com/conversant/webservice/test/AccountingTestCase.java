package com.conversant.webservice.test;

import java.math.BigDecimal;

import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.compiere.util.CLogger;

import com.conversant.test.AdempiereTestCase;
import com.conversant.webservice.Accounting;
import com.conversant.webservice.CreateBPBankAccountRequest;
import com.conversant.webservice.CreatePaymentRequest;
import com.conversant.webservice.LoginRequest;
import com.conversant.webservice.ObjectFactory;
import com.conversant.webservice.ProcessPaymentRequest;
import com.conversant.webservice.StandardResponse;

public class AccountingTestCase extends AdempiereTestCase 
{
	private static CLogger log = CLogger.getCLogger(AccountingTestCase.class);	

	private static String URL = "http://localhost/webservice/accounting";
	
	@Override
	protected void setUp() throws Exception 
	{
		super.setUp();
		log.info("Done setting up AccountingTestCase");
	}
	
	@Override
	protected void tearDown() throws Exception 
	{
		super.tearDown();
		log.info("Done tearing down AccountingTestCase");
	}
	private static JaxWsProxyFactoryBean getFactory(Class clazz)
	{
		JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
    	factory.getInInterceptors().add(new LoggingInInterceptor());
    	factory.getOutInterceptors().add(new LoggingOutInterceptor());
    	factory.setServiceClass(clazz);
    	factory.setAddress(URL);
    	return factory;
	}

	public void testCreatePayment()
	{
		JaxWsProxyFactoryBean factory = getFactory(Accounting.class);
		Accounting client = (Accounting)factory.create();

    	ObjectFactory objFactory = new ObjectFactory();
    	
    	// Test without Trx
    	LoginRequest loginRequest = objFactory.createLoginRequest();    	
    	loginRequest.setUsername("IntalioUser");
    	loginRequest.setPassword("password");
    	loginRequest.setType("AC-createPayment-Intalio"); 
    	loginRequest.setTrxName(""); 
    	
    	CreatePaymentRequest createPaymentRequest = objFactory.createCreatePaymentRequest();
    	createPaymentRequest.setLoginRequest(loginRequest);
    	createPaymentRequest.setBankAccountId(1000000);
    	createPaymentRequest.setBusinessPartnerId(1000071);
    	createPaymentRequest.setBusinessPartnerBankAccountId(1000025);    	
    	createPaymentRequest.setAmount(new BigDecimal(10.12));
    	    	
    	StandardResponse res = client.createPayment(createPaymentRequest);
    	if (!res.isSuccess())
    		fail("Failed to create Payment - " + res.getMessage());
	}
	
	public void testProcessPayment()
	{
		JaxWsProxyFactoryBean factory = getFactory(Accounting.class);
		Accounting client = (Accounting)factory.create();

    	ObjectFactory objFactory = new ObjectFactory();
    	
    	// Test without Trx
    	LoginRequest loginRequest = objFactory.createLoginRequest();    	
    	loginRequest.setUsername("IntalioUser");
    	loginRequest.setPassword("password");
    	loginRequest.setType("AC-processPayment-Intalio"); 
    	loginRequest.setTrxName(""); 
    	
    	ProcessPaymentRequest processPaymentRequest = objFactory.createProcessPaymentRequest();
    	processPaymentRequest.setLoginRequest(loginRequest);
    	processPaymentRequest.setBusinessPartnerId(1000071);
    	processPaymentRequest.setPaymentId(1003747);
    	
    	StandardResponse res = client.processPayment(processPaymentRequest);
    	if (!res.isSuccess())
    		fail("Failed to process Payment - " + res.getMessage());
	}
	
	public void testCreateBPBankAccount()
	{
		JaxWsProxyFactoryBean factory = getFactory(Accounting.class);
		Accounting client = (Accounting)factory.create();

    	ObjectFactory objFactory = new ObjectFactory();
    	
    	// Test without Trx
    	LoginRequest loginRequest = objFactory.createLoginRequest();    	
    	loginRequest.setUsername("IntalioUser");
    	loginRequest.setPassword("password");
    	loginRequest.setType("AC-createBpBankAccount-Intalio"); 
    	loginRequest.setTrxName(""); 
	
    	CreateBPBankAccountRequest createBPBankAccountRequest = objFactory.createCreateBPBankAccountRequest();    	
    	createBPBankAccountRequest.setLoginRequest(loginRequest);
    	createBPBankAccountRequest.setUserId(1000020);
    	createBPBankAccountRequest.setBusinessPartnerId(1000071);
    	createBPBankAccountRequest.setCreditCardType("V");
    	createBPBankAccountRequest.setCreditCardNumber("4564456445644564");
    	createBPBankAccountRequest.setCreditCardVerificationCode("100");
    	createBPBankAccountRequest.setCreditCardExpiryMonth(9);
    	createBPBankAccountRequest.setCreditCardExpiryYear(13);
    	createBPBankAccountRequest.setAccountName("TEST USER");
    	createBPBankAccountRequest.setAccountStreet("123 Test St");
    	createBPBankAccountRequest.setAccountCity("Test City");
    	createBPBankAccountRequest.setAccountZip("12345");
    	createBPBankAccountRequest.setAccountState("Test State");
    	createBPBankAccountRequest.setAccountCountry("Test Country");
    	
    	StandardResponse res = client.createBPBankAccount(createBPBankAccountRequest);
    	if (!res.isSuccess())
    		fail("Failed to create BP Bank Account - " + res.getMessage());
	}
}
