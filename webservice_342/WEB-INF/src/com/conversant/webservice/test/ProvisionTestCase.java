package com.conversant.webservice.test;

import java.util.Random;

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
import com.conversant.webservice.CreateTrxRequest;
import com.conversant.webservice.LoginRequest;
import com.conversant.webservice.ObjectFactory;
import com.conversant.webservice.Provision;
import com.conversant.webservice.ProvisionImpl;
import com.conversant.webservice.StandardResponse;

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
    	loginRequest.setType("P-createTrx-Intalio"); 
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
//			fail("Failed to load products created for " + didNumber + " using trxName[" + trxName + "] returned " + products.length + " products");
		
		loginRequest = objFactory.createLoginRequest();    	
    	loginRequest.setUsername("IntalioUser");
    	loginRequest.setPassword("password");
    	loginRequest.setType("P-commitTrx-Intalio"); 
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
		
		
	}
}
