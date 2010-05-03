package com.conversant.webservice.test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;

import com.conversant.webservice.CommitTrxRequest;
import com.conversant.webservice.CreateDIDSubscriptionRequest;
import com.conversant.webservice.CreateTrxRequest;
import com.conversant.webservice.LoginRequest;
import com.conversant.webservice.ObjectFactory;
import com.conversant.webservice.Provision;
import com.conversant.webservice.StandardResponse;

public class MainTestCase 
{
	public static void main(String[] args)
	{   	
		testCreateDIDSubscription();
	}
	
	private static void testCreateDIDSubscription()
	{
		JaxWsProxyFactoryBean factory = getFactory(Provision.class);
    	Provision client = (Provision)factory.create();
    	
    	ObjectFactory objFactory = new ObjectFactory();
    	LoginRequest loginRequest = objFactory.createLoginRequest();
    	
    	loginRequest.setUsername("IntalioUser");
    	loginRequest.setPassword("password");
    	loginRequest.setType(""); // TODO: Test when not set as empty string
    	loginRequest.setTrxName(""); // TODO: Test when not set as empty string
    	
    	CreateTrxRequest createTrxRequest = objFactory.createCreateTrxRequest();
    	createTrxRequest.setLoginRequest(loginRequest);
    	createTrxRequest.setTrxNamePrefix("ProvisionDIDProcess");
    	
    	printResponse(client.createTrx(createTrxRequest));
    	
//    	CommitTrxRequest commitTrxRequest = objFactory.createCommitTrxRequest();
//    	RollbackTrxRequest rollbackTrxRequest = objFactory.createRollbackTrxRequest();
//    	
//    	CreateDIDSubscriptionRequest createDIDSubscriptionRequest = objFactory.createCreateDIDSubscriptionRequest();
//    	createDIDSubscriptionRequest.setLoginRequest(loginRequest);
//    	
//    	commitTrxRequest.setLoginRequest(loginRequest);
//    	rollbackTrxRequest.setLoginRequest(loginRequest);
    	
    	
	}
	
	private static JaxWsProxyFactoryBean getFactory(Class clazz)
	{
		JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
    	factory.getInInterceptors().add(new LoggingInInterceptor());
    	factory.getOutInterceptors().add(new LoggingOutInterceptor());
    	factory.setServiceClass(clazz);
    	factory.setAddress("http://localhost/webservice/provision");
    	return factory;
	}
	
	private static void printResponse(StandardResponse response)
	{
		System.out.println("Server said: Success[" + response.isSuccess() + "] - Message[" + response.getMessage() + "] - TrxName[" + response.getTrxName() + "] - Id[" + response.getId() + "]");
	}
	
	private static void session(Provision client)
	{
		ObjectFactory objFactory = new ObjectFactory();
		
		CreateDIDSubscriptionRequest createDIDSubscriptionRequest = objFactory.createCreateDIDSubscriptionRequest();
		CommitTrxRequest commitTrxRequest = objFactory.createCommitTrxRequest();
		
		StandardResponse response = client.createDIDSubscription(createDIDSubscriptionRequest);
		System.out.println("Server said: Id[" + response.getId() + "]");
	
	}
	
	private static void session2(Provision client)
	{
		ObjectFactory objFactory = new ObjectFactory();
		
		CommitTrxRequest commitTrxRequest = objFactory.createCommitTrxRequest();


	}
	
	private static void test(Provision client)
	{
    	ObjectFactory objFactory = new ObjectFactory();

    	LoginRequest loginRequest = objFactory.createLoginRequest();
    	loginRequest.setUsername("IntalioUser");
    	loginRequest.setPassword("password");
    	loginRequest.setType("Provision-addDIDSubscription-Intalio");
    	
    	CreateDIDSubscriptionRequest createDIDSubscriptionRequest = objFactory.createCreateDIDSubscriptionRequest();
    	createDIDSubscriptionRequest.setLoginRequest(loginRequest);
    	createDIDSubscriptionRequest.setNumber("987654321");
    	createDIDSubscriptionRequest.setBusinessPartnerId(1000071);
    	
    	StandardResponse response = client.createDIDSubscription(createDIDSubscriptionRequest);
    	System.out.println("Server said: Success[" + response.isSuccess() + "] - Message[" + response.getMessage() + "] - TrxName[" + response.getTrxName() + "] - Id[" + response.getId() + "]");

    	CommitTrxRequest commitTrxRequest = objFactory.createCommitTrxRequest();
    	commitTrxRequest.setLoginRequest(loginRequest);
    	
    	
    	System.exit(0); 
	}
	
	private static void showXML(ObjectFactory objFactory, LoginRequest loginRequest)
	{
    	try
    	{
			JAXBElement<LoginRequest> jaxbLoginRequest = objFactory.createLoginRequest(loginRequest);
			JAXBContext jc = JAXBContext.newInstance("com.conversant.webservice");
		    Marshaller m = jc.createMarshaller();
		    m.marshal(jaxbLoginRequest, System.out);
    	} 
    	catch(JAXBException jbe)
    	{

    	}
	}
}
