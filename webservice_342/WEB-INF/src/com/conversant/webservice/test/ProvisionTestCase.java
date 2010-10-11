package com.conversant.webservice.test;

import java.util.GregorianCalendar;
import java.util.Random;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.compiere.model.MProduct;
import org.compiere.util.CLogger;
import org.compiere.util.Trx;

import com.conversant.did.DIDConstants;
import com.conversant.did.DIDUtil;
import com.conversant.test.AdempiereTestCase;
import com.conversant.webservice.CommitTrxRequest;
import com.conversant.webservice.CreateDIDProductRequest;
import com.conversant.webservice.CreateDIDSubscriptionRequest;
import com.conversant.webservice.CreateSIPProductRequest;
import com.conversant.webservice.CreateSIPSubscriptionRequest;
import com.conversant.webservice.CreateTrxRequest;
import com.conversant.webservice.CreateVoicemailProductRequest;
import com.conversant.webservice.CreateVoicemailSubscriptionRequest;
import com.conversant.webservice.LoginRequest;
import com.conversant.webservice.ObjectFactory;
import com.conversant.webservice.Provision;
import com.conversant.webservice.ProvisionImpl;
import com.conversant.webservice.RollbackTrxRequest;
import com.conversant.webservice.StandardResponse;
import com.conversant.webservice.UpdateDIDProductRequest;
import com.conversant.webservice.UpdateUserPreferenceEndDateRequest;
import com.conversant.webservice.UpdateUserPreferenceStartDateRequest;
import com.conversant.webservice.UpdateUserPreferenceValueRequest;

public class ProvisionTestCase extends AdempiereTestCase 
{
	private static CLogger log = CLogger.getCLogger(ProvisionTestCase.class);	
	private static boolean SHOW_TIMING = true;

	private static String URL = "http://localhost/webservice/provision";

	private static String COUNTRY_ID = "147";
	private static String COUNTRY_CODE = "64";
	private static String AREA_CODE = "9429";
	private static String AREA_CODE_DESCRIPTION = "Auckland - Red Beach Test";
	private static String PER_MIN_CHARGE = "1";
	private static String FREE_MINS = "10";
	private static int BP_2TALK_ID = 1000076;

	@Override
	protected void setUp() throws Exception 
	{
		super.setUp();
		log.info("Done setting up ProvisionTestCase");
	}

	@Override
	protected void tearDown() throws Exception 
	{
		super.tearDown();
		log.info("Done tearing down ProvisionTestCase");
	}

	private static String getRandomDID()
	{
		Random rn = new Random();
		String didNumber = COUNTRY_CODE + AREA_CODE + (rn.nextInt(8999) + 1000);
		return didNumber;
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

	/**
	 * Simulates the Intalio process (for debugging purposes)
	 */
	public void testADempiereProvisioningProcess()
	{
		/*
		 	1. Create trx
		 	2. Create SIP product
		 	3. Create Voicemail product
		 	4. Create DID subscriptions
		 	5. Create SIP subscriptions
		 	6. Create Voicemail subscriptions
		 	7. Update DID products subscribed
		 	8. Commit trx
		 */
		
		// Params
		String didNumber = "6494266151";		
		String sipAddress = didNumber;
		String sipDomain = "conversant.co.nz";
		String voicemailMailboxNumber = didNumber;
		String voicemailContext = "proxy_default";
		String voicemailMacroName = "macroName";
		int businessPartnerId = 1000071;
		int businessPartnerLocationId = 1000149;
		
		boolean success = false;
		
		JaxWsProxyFactoryBean factory = getFactory(Provision.class);
		Provision client = (Provision)factory.create();
		
		ObjectFactory objFactory = new ObjectFactory();
		
		// 1. ---------------------------------------
		LoginRequest loginRequest = objFactory.createLoginRequest();    	
		loginRequest.setUsername("IntalioUser");
		loginRequest.setPassword("password");
		loginRequest.setType("G-createTrx-Intalio"); 
		loginRequest.setTrxName("");
		
		CreateTrxRequest createTrxRequest = objFactory.createCreateTrxRequest();
		createTrxRequest.setLoginRequest(loginRequest);
		createTrxRequest.setTrxNamePrefix("testADempiereProvisioningProcess");
		
		StandardResponse res = client.createTrx(createTrxRequest);
		if (!res.isSuccess())
			fail("Failed to create trx - " + res.getMessage());
		
		String trxName = res.getTrxName();
		
		try
		{
		// 2. ---------------------------------------
			loginRequest = objFactory.createLoginRequest();    	
			loginRequest.setUsername("IntalioUser");
			loginRequest.setPassword("password");
			loginRequest.setType("P-createSIPProduct-Intalio"); 
			loginRequest.setTrxName(trxName);
			
			CreateSIPProductRequest createSIPProductRequest = objFactory.createCreateSIPProductRequest();
			createSIPProductRequest.setLoginRequest(loginRequest);
			createSIPProductRequest.setAddress(sipAddress);
			createSIPProductRequest.setDomain(sipDomain);
			
			res = client.createSIPProduct(createSIPProductRequest);
			if (!res.isSuccess())
				throw new Exception("Failed to create SIP product - " + res.getMessage());
			
		// 3. ---------------------------------------
			loginRequest = objFactory.createLoginRequest();    	
			loginRequest.setUsername("IntalioUser");
			loginRequest.setPassword("password");
			loginRequest.setType("P-createVoicemailProduct-Intalio"); 
			loginRequest.setTrxName(trxName);
			
			CreateVoicemailProductRequest createVoicemailProductRequest = objFactory.createCreateVoicemailProductRequest();
			createVoicemailProductRequest.setLoginRequest(loginRequest);
			createVoicemailProductRequest.setMailboxNumber(voicemailMailboxNumber);
			createVoicemailProductRequest.setContext(voicemailContext);
			createVoicemailProductRequest.setMacroName(voicemailMacroName);
			
			res = client.createVoicemailProduct(createVoicemailProductRequest);
			if (!res.isSuccess())
				throw new Exception("Failed to create Voicemail product - " + res.getMessage());
			
		// 4. ---------------------------------------
			loginRequest = objFactory.createLoginRequest();    	
			loginRequest.setUsername("IntalioUser");
			loginRequest.setPassword("password");
			loginRequest.setType("P-createDIDSubscription-Intalio"); 
			loginRequest.setTrxName(trxName);
			
			CreateDIDSubscriptionRequest createDIDSubscriptionRequest = objFactory.createCreateDIDSubscriptionRequest();
			createDIDSubscriptionRequest.setLoginRequest(loginRequest);
			createDIDSubscriptionRequest.setNumber(didNumber);
			createDIDSubscriptionRequest.setBusinessPartnerId(businessPartnerId);
			createDIDSubscriptionRequest.setBusinessPartnerLocationId(businessPartnerLocationId);
			
			res = client.createDIDSubscription(createDIDSubscriptionRequest);
			if (!res.isSuccess())
				throw new Exception("Failed to create DID subscription - " + res.getMessage());
			
		// 5. ---------------------------------------
			loginRequest = objFactory.createLoginRequest();    	
			loginRequest.setUsername("IntalioUser");
			loginRequest.setPassword("password");
			loginRequest.setType("P-createSIPSubscription-Intalio"); 
			loginRequest.setTrxName(trxName);
			
			CreateSIPSubscriptionRequest createSIPSubscriptionRequest = objFactory.createCreateSIPSubscriptionRequest();
			createSIPSubscriptionRequest.setLoginRequest(loginRequest);
			createSIPSubscriptionRequest.setAddress(sipAddress);
			createSIPSubscriptionRequest.setDomain(sipDomain);
			createSIPSubscriptionRequest.setBusinessPartnerId(businessPartnerId);
			createSIPSubscriptionRequest.setBusinessPartnerLocationId(businessPartnerLocationId);
			
			res = client.createSIPSubscription(createSIPSubscriptionRequest);
			if (!res.isSuccess())
				throw new Exception("Failed to create SIP subscription - " + res.getMessage());
			
		// 6. ---------------------------------------
			loginRequest = objFactory.createLoginRequest();    	
			loginRequest.setUsername("IntalioUser");
			loginRequest.setPassword("password");
			loginRequest.setType("P-createVoicemailSubscription-Intalio"); 
			loginRequest.setTrxName(trxName);
			
			CreateVoicemailSubscriptionRequest createVoicemailSubscriptionRequest = objFactory.createCreateVoicemailSubscriptionRequest();
			createVoicemailSubscriptionRequest.setLoginRequest(loginRequest);
			createVoicemailSubscriptionRequest.setMailboxNumber(voicemailMailboxNumber);
			createVoicemailSubscriptionRequest.setBusinessPartnerId(businessPartnerId);
			createVoicemailSubscriptionRequest.setBusinessPartnerLocationId(businessPartnerLocationId);
			
			res = client.createVoicemailSubscription(createVoicemailSubscriptionRequest);
			if (!res.isSuccess())
				throw new Exception("Failed to create Voicemail subscription - " + res.getMessage());
			
		// 7. ---------------------------------------
			loginRequest = objFactory.createLoginRequest();    	
			loginRequest.setUsername("IntalioUser");
			loginRequest.setPassword("password");
			loginRequest.setType("P-updateDIDProduct-Intalio"); 
			loginRequest.setTrxName(trxName);
			
			UpdateDIDProductRequest updateDIDProductRequest = objFactory.createUpdateDIDProductRequest();
			updateDIDProductRequest.setLoginRequest(loginRequest);
			updateDIDProductRequest.setNumber(didNumber);
			updateDIDProductRequest.setSubscribed(true);
			
			res = client.updateDIDProduct(updateDIDProductRequest);
			if (!res.isSuccess())
				throw new Exception("Failed to update DID product - " + res.getMessage());
			
		// 8. ---------------------------------------
			loginRequest = objFactory.createLoginRequest();    	
			loginRequest.setUsername("IntalioUser");
			loginRequest.setPassword("password");
			loginRequest.setType("G-commitTrx-Intalio"); 
			loginRequest.setTrxName(trxName);
			
			CommitTrxRequest commitTrxRequest = objFactory.createCommitTrxRequest();
			commitTrxRequest.setLoginRequest(loginRequest);
			
			res = client.commitTrx(commitTrxRequest);
			if (!res.isSuccess())
				throw new Exception("Failed to commit Trx - " + res.getMessage());
			
			success = true;
		}
		catch (Exception ex)
		{
			fail(ex.getMessage());
		}
		finally
		{
			if (!success)
			{
				loginRequest = objFactory.createLoginRequest();    	
				loginRequest.setUsername("IntalioUser");
				loginRequest.setPassword("password");
				loginRequest.setType("G-rollbackTrx-Intalio"); 
				loginRequest.setTrxName(trxName);
				
				RollbackTrxRequest rollbackTrxRequest = objFactory.createRollbackTrxRequest();
				rollbackTrxRequest.setLoginRequest(loginRequest);
				
				res = client.rollbackTrx(rollbackTrxRequest);
				if (!res.isSuccess())
					fail("Failed to rollback trx - " + res.getMessage());
			}
		}
	}
	
	// TODO: Why can't load dids before commit when testTrx() works (possibly due to opening on different connections)
	public void testCreateDIDProduct()
	{
		JaxWsProxyFactoryBean factory = getFactory(Provision.class);
		Provision client = (Provision)factory.create();

		ObjectFactory objFactory = new ObjectFactory();

		// Test without Trx
		LoginRequest loginRequest = objFactory.createLoginRequest();    	
		loginRequest.setUsername("IntalioUser");
		loginRequest.setPassword("password");
		loginRequest.setType("P-createDIDProduct-Intalio"); 
		loginRequest.setTrxName(""); 

		String didNumber = getRandomDID();

		CreateDIDProductRequest createDIDProductRequest = objFactory.createCreateDIDProductRequest();
		createDIDProductRequest.setLoginRequest(loginRequest);
		createDIDProductRequest.setNumber(didNumber);
		createDIDProductRequest.setCountryId(COUNTRY_ID);
		createDIDProductRequest.setCountryCode(COUNTRY_CODE);
		createDIDProductRequest.setAreaCode(AREA_CODE);
		createDIDProductRequest.setAreaCodeDescription(AREA_CODE_DESCRIPTION);
		createDIDProductRequest.setFreeMinutes(FREE_MINS);
		createDIDProductRequest.setPerMinuteCharge(PER_MIN_CHARGE);
		createDIDProductRequest.setBusinessPartnerId(BP_2TALK_ID);
		createDIDProductRequest.setSetupCost("1");
		createDIDProductRequest.setMonthlyCharge("2");
		createDIDProductRequest.setCurrencyId(DIDConstants.NZD_CURRENCY_ID);
		createDIDProductRequest.setPricelistVersionId(1000000);

		StandardResponse res = client.createDIDProduct(createDIDProductRequest);
		if (!res.isSuccess())
			fail("Failed to create products for " + didNumber);

		MProduct[] products = DIDUtil.getDIDProducts(getCtx(), didNumber, null);
		if (products.length != 2)
			fail("Failed to load products created for " + didNumber);

		// Test with Trx
		loginRequest = objFactory.createLoginRequest();    	
		loginRequest.setUsername("IntalioUser");
		loginRequest.setPassword("password");
		loginRequest.setType("G-createTrx-Intalio"); 
		loginRequest.setTrxName(""); 

		CreateTrxRequest createTrxRequest = objFactory.createCreateTrxRequest();
		createTrxRequest.setLoginRequest(loginRequest);
		createTrxRequest.setTrxNamePrefix("CreateDIDTest");

		res = client.createTrx(createTrxRequest);
		if (!res.isSuccess())
			fail("Failed to create Trx to test CreateDID with trx");

		String trxName = res.getTrxName();
		loginRequest = objFactory.createLoginRequest();    	
		loginRequest.setUsername("IntalioUser");
		loginRequest.setPassword("password");
		loginRequest.setType("P-createDIDProduct-Intalio"); 
		loginRequest.setTrxName(trxName); 

		didNumber = getRandomDID();

		createDIDProductRequest = objFactory.createCreateDIDProductRequest();
		createDIDProductRequest.setLoginRequest(loginRequest);
		createDIDProductRequest.setNumber(didNumber);
		createDIDProductRequest.setCountryId(COUNTRY_ID);
		createDIDProductRequest.setCountryCode(COUNTRY_CODE);
		createDIDProductRequest.setAreaCode(AREA_CODE);
		createDIDProductRequest.setAreaCodeDescription(AREA_CODE_DESCRIPTION);
		createDIDProductRequest.setFreeMinutes(FREE_MINS);
		createDIDProductRequest.setPerMinuteCharge(PER_MIN_CHARGE);
		createDIDProductRequest.setBusinessPartnerId(BP_2TALK_ID);
		createDIDProductRequest.setSetupCost("1");
		createDIDProductRequest.setMonthlyCharge("2");
		createDIDProductRequest.setCurrencyId(DIDConstants.NZD_CURRENCY_ID);
		createDIDProductRequest.setPricelistVersionId(1000000);

		res = client.createDIDProduct(createDIDProductRequest);
		if (!res.isSuccess())
			fail("Failed to create products for " + didNumber + " using trx");

		products = DIDUtil.getDIDProducts(getCtx(), didNumber, null);
		if (products.length == 2)
			fail("Loaded products created for " + didNumber + " without using trxName");

//		products = DIDUtil.getDIDProducts(getCtx(), didNumber, trxName);
//		if (products.length != 2)
//		fail("Failed to load products created for " + didNumber + " using trxName[" + trxName + "] returned " + products.length + " products");

		loginRequest = objFactory.createLoginRequest();    	
		loginRequest.setUsername("IntalioUser");
		loginRequest.setPassword("password");
		loginRequest.setType("G-commitTrx-Intalio"); 
		loginRequest.setTrxName(trxName); 

		CommitTrxRequest commitTrxRequest = objFactory.createCommitTrxRequest();
		commitTrxRequest.setLoginRequest(loginRequest);

		res = client.commitTrx(commitTrxRequest);
		if (!res.isSuccess())
			fail("Failed to commit products");

		products = DIDUtil.getDIDProducts(getCtx(), didNumber, null);
		if (products.length != 2)
			fail("Failed to load products created for " + didNumber + " when using trx");
	}
	
	public void testUpdateDIDProduct()
	{
		JaxWsProxyFactoryBean factory = getFactory(Provision.class);
		Provision client = (Provision)factory.create();

		ObjectFactory objFactory = new ObjectFactory();

		// Test without Trx
		LoginRequest loginRequest = objFactory.createLoginRequest();    	
		loginRequest.setUsername("IntalioUser");
		loginRequest.setPassword("password");
		loginRequest.setType("P-updateDIDProduct-Intalio"); 
		loginRequest.setTrxName("");
		
		UpdateDIDProductRequest updateDIDProductRequest = objFactory.createUpdateDIDProductRequest();
		updateDIDProductRequest.setLoginRequest(loginRequest);
		updateDIDProductRequest.setNumber("12014784950");
		updateDIDProductRequest.setSubscribed(false);
		
		StandardResponse res = client.updateDIDProduct(updateDIDProductRequest);
		if (!res.isSuccess())
			fail("Failed to update DID product - " + res.getMessage());
	}

	public void testUpdateUserPreferenceValue()
	{
		JaxWsProxyFactoryBean factory = getFactory(Provision.class);
		Provision client = (Provision)factory.create();

		ObjectFactory objFactory = new ObjectFactory();

		// Test without Trx
		LoginRequest loginRequest = objFactory.createLoginRequest();    	
		loginRequest.setUsername("IntalioUser");
		loginRequest.setPassword("password");
		loginRequest.setType("P-updateUserPreference-Intalio"); 
		loginRequest.setTrxName(""); 

		UpdateUserPreferenceValueRequest updateUserPreferenceValueRequest = objFactory.createUpdateUserPreferenceValueRequest();
		updateUserPreferenceValueRequest.setLoginRequest(loginRequest);
		updateUserPreferenceValueRequest.setUuid("1000071");
		updateUserPreferenceValueRequest.setUsername("123456789");
		updateUserPreferenceValueRequest.setDomain("conversant.co.nz");
		updateUserPreferenceValueRequest.setAttribute("42001");
		updateUserPreferenceValueRequest.setValue("true");

		StandardResponse res = client.updateUserPreferenceValue(updateUserPreferenceValueRequest);
		if (!res.isSuccess())
			fail("Failed to update user preference value - " + res.getMessage());
	}

	public void testUpdateUserPreferenceStartDate()
	{
		JaxWsProxyFactoryBean factory = getFactory(Provision.class);
		Provision client = (Provision)factory.create();

		ObjectFactory objFactory = new ObjectFactory();

		// Test without Trx
		LoginRequest loginRequest = objFactory.createLoginRequest();    	
		loginRequest.setUsername("IntalioUser");
		loginRequest.setPassword("password");
		loginRequest.setType("P-updateUserPreference-Intalio"); 
		loginRequest.setTrxName(""); 

		UpdateUserPreferenceStartDateRequest updateUserPreferenceStartDateRequest = objFactory.createUpdateUserPreferenceStartDateRequest();
		updateUserPreferenceStartDateRequest.setLoginRequest(loginRequest);
		updateUserPreferenceStartDateRequest.setUuid("1000071");
		updateUserPreferenceStartDateRequest.setUsername("123456789");
		updateUserPreferenceStartDateRequest.setDomain("conversant.co.nz");
		updateUserPreferenceStartDateRequest.setAttribute("42001");
		updateUserPreferenceStartDateRequest.setStartDate(long2Gregorian(System.currentTimeMillis()));

		StandardResponse res = client.updateUserPreferenceStartDate(updateUserPreferenceStartDateRequest);
		if (!res.isSuccess())
			fail("Failed to update user preference start date");
	}

	public void testUpdateUserPreferenceEndDate()
	{
		JaxWsProxyFactoryBean factory = getFactory(Provision.class);
		Provision client = (Provision)factory.create();

		ObjectFactory objFactory = new ObjectFactory();

		// Test without Trx
		LoginRequest loginRequest = objFactory.createLoginRequest();    	
		loginRequest.setUsername("IntalioUser");
		loginRequest.setPassword("password");
		loginRequest.setType("P-updateUserPreference-Intalio"); 
		loginRequest.setTrxName(""); 

		UpdateUserPreferenceEndDateRequest updateUserPreferenceEndDateRequest = objFactory.createUpdateUserPreferenceEndDateRequest();
		updateUserPreferenceEndDateRequest.setLoginRequest(loginRequest);
		updateUserPreferenceEndDateRequest.setUuid("1000071");
		updateUserPreferenceEndDateRequest.setUsername("123456789");
		updateUserPreferenceEndDateRequest.setDomain("conversant.co.nz");
		updateUserPreferenceEndDateRequest.setAttribute("42001");
		updateUserPreferenceEndDateRequest.setEndDate(long2Gregorian(System.currentTimeMillis()));

		StandardResponse res = client.updateUserPreferenceEndDate(updateUserPreferenceEndDateRequest);
		if (!res.isSuccess())
			fail("Failed to update user preference start date");
	}

	public void testTrx()
	{
		String trxName = Trx.createTrxName();

		ObjectFactory objFactory = new ObjectFactory();

		// Test without Trx
		LoginRequest loginRequest = objFactory.createLoginRequest();    	
		loginRequest.setUsername("IntalioUser");
		loginRequest.setPassword("password");
		loginRequest.setType("P-createDIDProduct-Intalio"); 
		loginRequest.setTrxName(trxName); 

		String didNumber = getRandomDID();
		CreateDIDProductRequest createDIDProductRequest = objFactory.createCreateDIDProductRequest();
		createDIDProductRequest.setLoginRequest(loginRequest);
		createDIDProductRequest.setNumber(didNumber);
		createDIDProductRequest.setCountryId(COUNTRY_ID);
		createDIDProductRequest.setCountryCode(COUNTRY_CODE);
		createDIDProductRequest.setAreaCode(AREA_CODE);
		createDIDProductRequest.setAreaCodeDescription(AREA_CODE_DESCRIPTION);
		createDIDProductRequest.setFreeMinutes(FREE_MINS);
		createDIDProductRequest.setPerMinuteCharge(PER_MIN_CHARGE);
		createDIDProductRequest.setBusinessPartnerId(BP_2TALK_ID);
		createDIDProductRequest.setSetupCost("1");
		createDIDProductRequest.setMonthlyCharge("2");
		createDIDProductRequest.setCurrencyId(DIDConstants.NZD_CURRENCY_ID);
		createDIDProductRequest.setPricelistVersionId(1000000);

		ProvisionImpl provision = new ProvisionImpl();
		StandardResponse res = provision.createDIDProduct(createDIDProductRequest);
		if (res.isSuccess())
		{
			MProduct[] products = DIDUtil.getDIDProducts(getCtx(), didNumber, trxName);
			if (products.length != 2)
				fail("Failed");
		}
		else
			fail("Failed");
	}

	/**
	 * Transform a date in a long to a GregorianCalendar
	 *
	 * @param date
	 * @return
	 */
	public static XMLGregorianCalendar long2Gregorian(long date) 
	{
		DatatypeFactory dataTypeFactory;
		try 
		{
			dataTypeFactory = DatatypeFactory.newInstance();
		} 
		catch (DatatypeConfigurationException e) 
		{
			throw new RuntimeException(e);
		}
		
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTimeInMillis(date);
		
		return dataTypeFactory.newXMLGregorianCalendar(gc);
	} 
}
