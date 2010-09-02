package com.conversant.webservice.test;

import java.util.List;

import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.compiere.model.MBPartner;
import org.compiere.model.MLocation;
import org.compiere.util.CLogger;

import com.conversant.test.AdempiereTestCase;
import com.conversant.webservice.Admin;
import com.conversant.webservice.BusinessPartner;
import com.conversant.webservice.CommitTrxRequest;
import com.conversant.webservice.CreateBusinessPartnerLocationRequest;
import com.conversant.webservice.CreateBusinessPartnerRequest;
import com.conversant.webservice.CreateLocationRequest;
import com.conversant.webservice.CreateTrxRequest;
import com.conversant.webservice.LoginRequest;
import com.conversant.webservice.ObjectFactory;
import com.conversant.webservice.ReadBusinessPartnersByGroupRequest;
import com.conversant.webservice.ReadBusinessPartnersResponse;
import com.conversant.webservice.ReadSubscriptionsRequest;
import com.conversant.webservice.ReadSubscriptionsResponse;
import com.conversant.webservice.StandardResponse;
import com.conversant.webservice.Subscription;

public class AdminTestCase extends AdempiereTestCase 
{
	private static CLogger log = CLogger.getCLogger(AdminTestCase.class);	

	private static String URL = "http://localhost/webservice/admin";
	
	@Override
	protected void setUp() throws Exception 
	{
		super.setUp();
		log.info("Done setting up AdminTestCase");
	}
	
	@Override
	protected void tearDown() throws Exception 
	{
		super.tearDown();
		log.info("Done tearing down AdminTestCase");
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
	
	public void testCreateBusinessPartner()
	{
		JaxWsProxyFactoryBean factory = getFactory(Admin.class);
    	Admin client = (Admin)factory.create();

    	ObjectFactory objFactory = new ObjectFactory();
    	
    	// Test without Trx
    	LoginRequest loginRequest = objFactory.createLoginRequest();    	
    	loginRequest.setUsername("IntalioUser");
    	loginRequest.setPassword("password");
    	loginRequest.setType("AD-createBusinessPartner-Intalio"); 
    	loginRequest.setTrxName(""); 
    	
    	CreateBusinessPartnerRequest createBusinessPartnerRequest = objFactory.createCreateBusinessPartnerRequest();
    	createBusinessPartnerRequest.setLoginRequest(loginRequest);
    	createBusinessPartnerRequest.setSearchKey("testSearchKey");
    	createBusinessPartnerRequest.setName("TestBP");
    	createBusinessPartnerRequest.setBusinessPartnerGroupId(1000000);
    	
    	StandardResponse res = client.createBusinessPartner(createBusinessPartnerRequest);
    	if (!res.isSuccess())
    		fail("Failed to create Business Partner - " + res.getMessage());
    	
    	MBPartner businessPartner = MBPartner.get(getCtx(), res.getId());
    	if (businessPartner == null)
    		fail("Failed to load newly create Business Partner");
    	else
    		businessPartner.delete(true);
    	
    	// Test without Search Key
    	createBusinessPartnerRequest = objFactory.createCreateBusinessPartnerRequest();
    	createBusinessPartnerRequest.setLoginRequest(loginRequest);
    	createBusinessPartnerRequest.setName("TestBPwithoutSearchKey");
    	createBusinessPartnerRequest.setBusinessPartnerGroupId(1000000);
    	
    	res = client.createBusinessPartner(createBusinessPartnerRequest);
    	if (!res.isSuccess())
    		fail("Failed to create Business Partner - " + res.getMessage());
    	
    	businessPartner = MBPartner.get(getCtx(), res.getId());
    	if (businessPartner == null)
    		fail("Failed to load newly create Business Partner");
    	else
    		businessPartner.delete(true);
    	
    	// Test with empty Search Key
    	createBusinessPartnerRequest = objFactory.createCreateBusinessPartnerRequest();
    	createBusinessPartnerRequest.setLoginRequest(loginRequest);
    	createBusinessPartnerRequest.setSearchKey("");
    	createBusinessPartnerRequest.setName("TestBPwithEmptySearchKey");
    	createBusinessPartnerRequest.setBusinessPartnerGroupId(1000000);
    	
    	res = client.createBusinessPartner(createBusinessPartnerRequest);
    	if (!res.isSuccess())
    		fail("Failed to create Business Partner - " + res.getMessage());
    	
    	businessPartner = MBPartner.get(getCtx(), res.getId());
    	if (businessPartner == null)
    		fail("Failed to load newly create Business Partner");
    	else
    		businessPartner.delete(true);
    	
    	// Test with NULL Search Key
    	createBusinessPartnerRequest = objFactory.createCreateBusinessPartnerRequest();
    	createBusinessPartnerRequest.setLoginRequest(loginRequest);
    	createBusinessPartnerRequest.setSearchKey(null);
    	createBusinessPartnerRequest.setName("TestBPwithNULLSearchKey");
    	createBusinessPartnerRequest.setBusinessPartnerGroupId(1000000);
    	
    	res = client.createBusinessPartner(createBusinessPartnerRequest);
    	if (!res.isSuccess())
    		fail("Failed to create Business Partner - " + res.getMessage());
    	
    	businessPartner = MBPartner.get(getCtx(), res.getId());
    	if (businessPartner == null)
    		fail("Failed to load newly create Business Partner");
    	else
    		businessPartner.delete(true);
    
    	// Test with Trx
    	loginRequest = objFactory.createLoginRequest();    	
    	loginRequest.setUsername("IntalioUser");
    	loginRequest.setPassword("password");
    	loginRequest.setType("G-createTrx-Intalio"); 
    	loginRequest.setTrxName(""); 
    	
    	CreateTrxRequest createTrxRequest = objFactory.createCreateTrxRequest();
    	createTrxRequest.setLoginRequest(loginRequest);
    	
    	res = client.createTrx(createTrxRequest);
    	if (!res.isSuccess())
    		fail("Failed to create trx - " + res.getMessage());
    	
    	String trxName = res.getTrxName();
    	
    	loginRequest = objFactory.createLoginRequest();    	
    	loginRequest.setUsername("IntalioUser");
    	loginRequest.setPassword("password");
    	loginRequest.setType("AD-createBusinessPartner-Intalio"); 
    	loginRequest.setTrxName(res.getTrxName()); 
    	
    	createBusinessPartnerRequest = objFactory.createCreateBusinessPartnerRequest();
    	createBusinessPartnerRequest.setLoginRequest(loginRequest);
    	createBusinessPartnerRequest.setName("TestBPwithTrx");
    	createBusinessPartnerRequest.setBusinessPartnerGroupId(1000000);
    	
    	res = client.createBusinessPartner(createBusinessPartnerRequest);
    	if (!res.isSuccess())
    		fail("Failed to create Business Partner - " + res.getMessage());
    	
    	int trxCreateBP_ID = res.getId();
    	
    	businessPartner = MBPartner.get(getCtx(), trxCreateBP_ID);
    	if (businessPartner != null)
    		fail("Loaded newly create Business Partner before commit");
    	
    	loginRequest = objFactory.createLoginRequest();    	
    	loginRequest.setUsername("IntalioUser");
    	loginRequest.setPassword("password");
    	loginRequest.setType("G-commitTrx-Intalio"); 
    	loginRequest.setTrxName(trxName); 
    	
    	CommitTrxRequest commitTrxRequest = objFactory.createCommitTrxRequest();
    	commitTrxRequest.setLoginRequest(loginRequest);
    	
    	res = client.commitTrx(commitTrxRequest);
    	if (!res.isSuccess())
    		fail("Failed to commit - " + res.getMessage());
    	
    	businessPartner = MBPartner.get(getCtx(), trxCreateBP_ID);
    	if (businessPartner == null)
    		fail("Failed to load newly create Business Partner");
    	else
    		businessPartner.delete(true);
	}
	
	public void testReadBusinessPartnersByGroup()
	{
		JaxWsProxyFactoryBean factory = getFactory(Admin.class);
    	Admin client = (Admin)factory.create();

    	ObjectFactory objFactory = new ObjectFactory();
    	
    	LoginRequest loginRequest = objFactory.createLoginRequest();    	
    	loginRequest.setUsername("IntalioUser");
    	loginRequest.setPassword("password");
    	loginRequest.setType("AD-readBusinessPartnersByGroup-Intalio"); 
    	loginRequest.setTrxName(""); 
    	
    	int businessPartnerGroupId = 1000004;
    	
    	ReadBusinessPartnersByGroupRequest readBusinessPartnersByGroupRequest = objFactory.createReadBusinessPartnersByGroupRequest();
    	readBusinessPartnersByGroupRequest.setLoginRequest(loginRequest);
    	readBusinessPartnersByGroupRequest.setBusinessPartnerGroupId(businessPartnerGroupId);
    	
    	ReadBusinessPartnersResponse res = client.readBusinessPartnersByGroup(readBusinessPartnersByGroupRequest);
    	StandardResponse stdRes = res.getStandardResponse();
    	if (!stdRes.isSuccess())
    		fail("Failed to read Business Partners by Group - " + stdRes.getMessage());
    	
    	List<BusinessPartner> businessPartners = res.getBusinessPartners();
    	System.out.println("Business Partners in MBPGroup[" + businessPartnerGroupId + "]");
    	for (BusinessPartner businessPartner : businessPartners)
    	{
    		System.out.println(businessPartner.getBusinessPartnerId() + "-" + businessPartner.getSearchKey() + "-" + businessPartner.getName());
    	}
    	
    	// TODO: Create a MBPGroup to test on
    	// Test group without any business partners
//    	businessPartnerGroupId = 1000000;
//    	readBusinessPartnersByGroupRequest = objFactory.createReadBusinessPartnersByGroupRequest();
//    	readBusinessPartnersByGroupRequest.setLoginRequest(loginRequest);
//    	readBusinessPartnersByGroupRequest.setBusinessPartnerGroupId(businessPartnerGroupId);
//    	
//    	res = client.readBusinessPartnersByGroup(readBusinessPartnersByGroupRequest);
//    	stdRes = res.getStandardResponse();
//    	if (!stdRes.isSuccess())
//    		fail("Failed to read Business Partners by Group - " + stdRes.getMessage());
//    	
//    	businessPartners = res.getBusinessPartners();
//    	System.out.println("Business Partners in MBPGroup[" + businessPartnerGroupId + "]");
//    	for (BusinessPartner businessPartner : businessPartners)
//    	{
//    		System.out.println(businessPartner.getBusinessPartnerId() + "-" + businessPartner.getSearchKey() + "-" + businessPartner.getName());
//    	}
	}
	
	public void testCreateBusinessPartnerLocation()
	{
		JaxWsProxyFactoryBean factory = getFactory(Admin.class);
    	Admin client = (Admin)factory.create();

    	ObjectFactory objFactory = new ObjectFactory();
    	
    	MBPartner businessPartner = null;
    	MLocation location = null;
    	
    	try
    	{
	    	// Create BP to use
	    	businessPartner = new MBPartner(getCtx(), 0, null);	
			businessPartner.setName("TestBP");
			
			if (!businessPartner.save())
				throw new Exception("Failed to create BP for tests");
			
			// Create Location to use
			location = new MLocation(getCtx(), 0, null);
			location.setAddress1("123 Test St");
			location.setCity("Auckland Test");
			location.setC_Country_ID(262); // NZ
			
			if (!location.save())
				throw new Exception("Failed to create Location for tests");
	
	    	// Test without Trx
	    	LoginRequest loginRequest = objFactory.createLoginRequest();    	
	    	loginRequest.setUsername("IntalioUser");
	    	loginRequest.setPassword("password");
	    	loginRequest.setType("AD-createBusinessPartnerLocation-Intalio"); 
	    	loginRequest.setTrxName(""); 
	    	
	    	CreateBusinessPartnerLocationRequest createBusinessPartnerLocationRequest = objFactory.createCreateBusinessPartnerLocationRequest();
	    	createBusinessPartnerLocationRequest.setLoginRequest(loginRequest);
	    	createBusinessPartnerLocationRequest.setName("TestLocation");
	    	createBusinessPartnerLocationRequest.setBusinessPartnerId(businessPartner.getC_BPartner_ID());
	    	createBusinessPartnerLocationRequest.setLocationId(location.getC_Location_ID());
	    	
	    	StandardResponse res = client.createBusinessPartnerLocation(createBusinessPartnerLocationRequest);
	    	if (!res.isSuccess())
	    		throw new Exception("Failed to create Business Partner Location - " + res.getMessage());
    	}
    	catch (Exception ex)
    	{
    		fail(ex.getMessage());
    	}
    	finally
    	{
    		if (businessPartner != null)
    			businessPartner.delete(true);
    		
    		if (location != null)
    			location.delete(true);
    	}
	}
	
	public void testCreateLocation()
	{
		JaxWsProxyFactoryBean factory = getFactory(Admin.class);
    	Admin client = (Admin)factory.create();

    	ObjectFactory objFactory = new ObjectFactory();
    	
    	try
    	{
	    	// Test without Trx
	    	LoginRequest loginRequest = objFactory.createLoginRequest();    	
	    	loginRequest.setUsername("IntalioUser");
	    	loginRequest.setPassword("password");
	    	loginRequest.setType("AD-createLocation-Intalio"); 
	    	loginRequest.setTrxName(""); 
	    	
	    	CreateLocationRequest createLocationRequest = objFactory.createCreateLocationRequest();
	    	createLocationRequest.setLoginRequest(loginRequest);
	    	createLocationRequest.setAddress1("123 Test St");
	    	createLocationRequest.setAddress2("Testland");
	    	createLocationRequest.setCity("TestCity");
	    	createLocationRequest.setCountryId(262); // NZ
	    	
	    	StandardResponse res = client.createLocation(createLocationRequest);
	    	if (!res.isSuccess())
	    		throw new Exception("Failed to create Location - " + res.getMessage());
    	}
    	catch (Exception ex)
    	{
    		fail(ex.getMessage());
    	}
    	finally
    	{

    	}
	}
	
//	public void testCreateUser()
//	{
//		JaxWsProxyFactoryBean factory = getFactory(Admin.class);
//    	Admin client = (Admin)factory.create();
//
//    	ObjectFactory objFactory = new ObjectFactory();
//    	
//    	try
//    	{
// // ************************************************************************************
//    		
//	    	// Test without Trx
//	    	LoginRequest loginRequest = objFactory.createLoginRequest();    	
//	    	loginRequest.setUsername("IntalioUser");
//	    	loginRequest.setPassword("password");
//	    	loginRequest.setType("AD-createUser-Intalio"); 
//	    	loginRequest.setTrxName(""); 
//	    	
//	    	CreateUserRequest createUserRequest = objFactory.createCreateUserRequest();
//	    	createUserRequest.setLoginRequest(loginRequest);
//	    	createUserRequest.setName("TestUser");
//	    	createUserRequest.setEmail("test.user@test.com");
//	    	createUserRequest.setPassword("password");
//
//	    	StandardResponse res = client.createUser(createUserRequest);
//	    	if (!res.isSuccess())
//	    		throw new Exception("Failed to create User - " + res.getMessage());
//	    	
//	    	// Check user can be loaded
//	    	MUser user = MUser.get(getCtx(), res.getId());
//	    	if (user == null)
//	    		throw new Exception("Failed to load created User");
//	    	
//	    	// Remove the created user
//	    	if (!user.delete(true))
//	    		throw new Exception("Failed to delete created User");
//	    	
//// ************************************************************************************
//	    	
//	    	// Test with Trx
//	    	loginRequest = objFactory.createLoginRequest();    	
//	    	loginRequest.setUsername("IntalioUser");
//	    	loginRequest.setPassword("password");
//	    	loginRequest.setType("G-createTrx-Intalio"); 
//	    	loginRequest.setTrxName(""); 
//	    	
//	    	CreateTrxRequest createTrxRequest = objFactory.createCreateTrxRequest();
//	    	createTrxRequest.setLoginRequest(loginRequest);
//	    	createTrxRequest.setTrxNamePrefix("AdminTestCase");
//	    	
//	    	res = client.createTrx(createTrxRequest);
//	    	if (!res.isSuccess())
//	    		throw new Exception("Failed to create Trx - " + res.getMessage());
//	    	
//	    	String trxName = res.getTrxName();
//	    	
//	    	loginRequest = objFactory.createLoginRequest();    	
//	    	loginRequest.setUsername("IntalioUser");
//	    	loginRequest.setPassword("password");
//	    	loginRequest.setType("AD-createUser-Intalio"); 
//	    	loginRequest.setTrxName(trxName); 
//	    	
//	    	createUserRequest = objFactory.createCreateUserRequest();
//	    	createUserRequest.setLoginRequest(loginRequest);
//	    	createUserRequest.setName("TestUser");
//	    	createUserRequest.setEmail("test.user@test.com");
//	    	createUserRequest.setPassword("password");
//	    	
//	    	res = client.createUser(createUserRequest);
//	    	if (!res.isSuccess())
//	    		throw new Exception("Failed to create User (with trx) - " + res.getMessage());
//	    	
////	    	user = MUser.get(getCtx(), res.getId());
////	    	if (user != null)
////	    		throw new Exception("Loaded user without using Trx");
//	    	
//	    	loginRequest = objFactory.createLoginRequest();    	
//	    	loginRequest.setUsername("IntalioUser");
//	    	loginRequest.setPassword("password");
//	    	loginRequest.setType("G-commitTrx-Intalio"); 
//	    	loginRequest.setTrxName(trxName); 
//	    	
//	    	CommitTrxRequest commitTrxRequest = objFactory.createCommitTrxRequest();
//	    	commitTrxRequest.setLoginRequest(loginRequest);
//	    	
//	    	res = client.commitTrx(commitTrxRequest);
//	    	if (!res.isSuccess())
//	    		throw new Exception("Failed to commit Trx for User creation - " + res.getMessage());
//	    	
//	    	// Check user can be loaded
//	    	user = MUser.get(getCtx(), res.getId());
//	    	if (user == null)
//	    		throw new Exception("Loaded user when using Trx");
//	    	
//	    	// Remove created user
//	    	if (!user.delete(true))
//	    		throw new Exception("Failed to delete created User");
//	    		
//    	}
//    	catch (Exception ex)
//    	{
//    		fail(ex.getMessage());
//    	}
//    	finally
//    	{
//
//    	}
//	}
	
	public void testReadSubscriptions()
	{
		JaxWsProxyFactoryBean factory = getFactory(Admin.class);
    	Admin client = (Admin)factory.create();

    	ObjectFactory objFactory = new ObjectFactory();
    	
    	LoginRequest loginRequest = objFactory.createLoginRequest();    	
    	loginRequest.setUsername("IntalioUser");
    	loginRequest.setPassword("password");
    	loginRequest.setType("AD-readSubscriptions-Intalio"); 
    	loginRequest.setTrxName(""); 
    	
    	int businessPartnerId = 1000011;
    	
    	ReadSubscriptionsRequest readSubscriptionsRequest = objFactory.createReadSubscriptionsRequest();
    	readSubscriptionsRequest.setLoginRequest(loginRequest);
    	readSubscriptionsRequest.setBusinessPartnerId(businessPartnerId);
    	
    	ReadSubscriptionsResponse res = client.readSubscriptions(readSubscriptionsRequest);
    	StandardResponse stdRes = res.getStandardResponse();
    	if (!stdRes.isSuccess())
    		fail("Failed to read Subscriptions - " + stdRes.getMessage());
    	
    	List<Subscription> subscriptions = res.getSubscriptions();
    	System.out.println("Subscriptions for MBPartner[" + businessPartnerId + "]");
    	for (Subscription subscription : subscriptions)
    	{
    		System.out.println(subscription.getSubscriptionId() + "-" + subscription.getName());
    	}
    	
    	// Test bp without subscriptions
    	businessPartnerId = 1000000;
    	readSubscriptionsRequest = objFactory.createReadSubscriptionsRequest();
    	readSubscriptionsRequest.setLoginRequest(loginRequest);
    	readSubscriptionsRequest.setBusinessPartnerId(businessPartnerId);
    	
    	res = client.readSubscriptions(readSubscriptionsRequest);
    	stdRes = res.getStandardResponse();
    	if (!stdRes.isSuccess())
    		fail("Failed to read Subscriptions - " + stdRes.getMessage());
    	
    	subscriptions = res.getSubscriptions();
    	System.out.println("Subscriptions for MBPartner[" + businessPartnerId + "]");
    	for (Subscription subscription : subscriptions)
    	{
    		System.out.println(subscription.getSubscriptionId() + "-" + subscription.getName());
    	}
    	
	}
}
