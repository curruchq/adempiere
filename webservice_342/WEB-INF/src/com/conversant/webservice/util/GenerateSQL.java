package com.conversant.webservice.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class GenerateSQL
{
	public static void main(String[] args)
	{		
		generateWebServicesDefinition();
		generateWebServicesConfig();
	}
	
	private static void generateWebServicesDefinition()
	{
		try
		{
			FileWriter fstream = new FileWriter("E:\\workspace\\webservice_342\\migration\\oracle\\WS002_WebServicesDefinition_NEW.sql");
		    BufferedWriter out = new BufferedWriter(fstream);

		    printWS_WebService(out);
		    printWS_WebServiceMethod(out);
		    
		    out.write("exit;");		    
			out.close();
		}
		catch (IOException ex)
		{
			System.err.println("Error: " + ex.getMessage());
		}
	}
	
	private static void generateWebServicesConfig()
	{
		try
		{
			FileWriter fstream = new FileWriter("E:\\workspace\\webservice_342\\migration\\oracle\\WS003_WebServicesConfig_NEW.sql");
		    BufferedWriter out = new BufferedWriter(fstream);

		    printAD_Config(out);
		    printWS_WebServiceType(out);
		    
		    out.write("exit;");	
			out.close();
		}
		catch (IOException ex)
		{
			System.err.println("Error: " + ex.getMessage());
		}
	}
	
	private static void printWS_WebService(BufferedWriter out) throws IOException
	{
		out.write("-- ------------------------------------------------------ Web Services Definition\n");
		out.write("INSERT INTO WS_WebService (AD_Client_ID,AD_Org_ID,Created,CreatedBy,Description,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'A generic web service','Y','Generic Web Service',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Generic'," + WebServiceConstants.WEBSERVICES.get("GENERIC_WEBSERVICE") + ")\n;\n");
		out.write("INSERT INTO WS_WebService (AD_Client_ID,AD_Org_ID,Created,CreatedBy,Description,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'A web service used for provisioning','Y','Provisioning Web Service',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Provision'," + WebServiceConstants.WEBSERVICES.get("PROVISION_WEBSERVICE") + ")\n;\n");
		out.write("INSERT INTO WS_WebService (AD_Client_ID,AD_Org_ID,Created,CreatedBy,Description,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'A web service used for administration','Y','Administration Web Service',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Admin'," + WebServiceConstants.WEBSERVICES.get("ADMIN_WEBSERVICE") + ")\n;\n");
		out.write("INSERT INTO WS_WebService (AD_Client_ID,AD_Org_ID,Created,CreatedBy,Description,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'A web service used for accounting','Y','Accounting Web Service',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Accounting'," + WebServiceConstants.WEBSERVICES.get("ACCOUNTING_WEBSERVICE") + ")\n;\n");
		out.write("\n");
	}
	
	private static void printWS_WebServiceMethod(BufferedWriter out) throws IOException
	{
		Iterator<String> webServiceIterator = WebServiceConstants.WEBSERVICES.keySet().iterator();
		while (webServiceIterator.hasNext())
		{
			String webServiceName = webServiceIterator.next();
			Integer webServiceId = WebServiceConstants.WEBSERVICES.get(webServiceName);
			
			out.write("-- ------------------------------------------------------ " + constantToCamelCase(webServiceName) + "\n");
			
			Iterator<String> webServiceMethodIterator = WebServiceConstants.WEBSERVICE_METHODS.get(webServiceName).keySet().iterator();
			while (webServiceMethodIterator.hasNext())
			{
				String webServiceMethodName = webServiceMethodIterator.next();
				String webServiceMethodNameFormatted = constantToCamelCase(webServiceMethodName.substring(0, webServiceMethodName.lastIndexOf("_METHOD_ID")));
				Integer webServiceMethodId = WebServiceConstants.WEBSERVICE_METHODS.get(webServiceName).get(webServiceMethodName);
				
				
				
				out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','" + webServiceMethodNameFormatted + "',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'" + webServiceMethodNameFormatted + "'," + webServiceId + "," + webServiceMethodId + ")\n;\n");				
			}
			
			out.write("\n");
		}
		
//		out.write("-- ------------------------------------------------------ Generic Web Service\n");
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','createTrx',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'createTrx'," + WebServiceConstants.GENERIC_WEBSERVICE_ID + "," + WebServiceConstants.CREATE_TRX_METHOD_ID + ")\n;\n");
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','commitTrx',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'commitTrx'," + WebServiceConstants.GENERIC_WEBSERVICE_ID + "," + WebServiceConstants.COMMIT_TRX_METHOD_ID + ")\n;\n");
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','rollbackTrx',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'rollbackTrx'," + WebServiceConstants.GENERIC_WEBSERVICE_ID + "," + WebServiceConstants.ROLLBACK_TRX_METHOD_ID + ")\n;\n");
//		out.write("\n");
//		
//		out.write("-- ------------------------------------------------------ Provision Web Service\n");
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','createDIDProduct',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'createDIDProduct'," + WebServiceConstants.PROVISION_WEBSERVICE_ID + "," + WebServiceConstants.CREATE_DID_PRODUCT_METHOD_ID + ")\n;\n");
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','readDIDProduct',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'readDIDProduct'," + WebServiceConstants.PROVISION_WEBSERVICE_ID + "," + WebServiceConstants.READ_DID_PRODUCT_METHOD_ID + ")\n;\n");
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','updateDIDProduct',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'updateDIDProduct'," + WebServiceConstants.PROVISION_WEBSERVICE_ID + "," + WebServiceConstants.UPDATE_DID_PRODUCT_METHOD_ID + ")\n;\n");
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','deleteDIDProduct',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'deleteDIDProduct'," + WebServiceConstants.PROVISION_WEBSERVICE_ID + "," + WebServiceConstants.DELETE_DID_PRODUCT_METHOD_ID + ")\n;\n");
//		
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','createSIPProduct',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'createSIPProduct'," + WebServiceConstants.PROVISION_WEBSERVICE_ID + "," + WebServiceConstants.CREATE_SIP_PRODUCT_METHOD_ID + ")\n;\n");
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','readSIPProduct',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'readSIPProduct'," + WebServiceConstants.PROVISION_WEBSERVICE_ID + "," + WebServiceConstants.READ_SIP_PRODUCT_METHOD_ID + ")\n;\n");
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','updateSIPProduct',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'updateSIPProduct'," + WebServiceConstants.PROVISION_WEBSERVICE_ID + "," + WebServiceConstants.UPDATE_SIP_PRODUCT_METHOD_ID + ")\n;\n");
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','deleteSIPProduct',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'deleteSIPProduct'," + WebServiceConstants.PROVISION_WEBSERVICE_ID + "," + WebServiceConstants.DELETE_SIP_PRODUCT_METHOD_ID + ")\n;\n");
//		
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','createVoicemailProduct',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'createVoicemailProduct'," + WebServiceConstants.PROVISION_WEBSERVICE_ID + "," + WebServiceConstants.CREATE_VOICEMAIL_PRODUCT_METHOD_ID + ")\n;\n");
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','readVoicemailProduct',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'readVoicemailProduct'," + WebServiceConstants.PROVISION_WEBSERVICE_ID + "," + WebServiceConstants.READ_VOICEMAIL_PRODUCT_METHOD_ID + ")\n;\n");
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','updateVoicemailProduct',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'updateVoicemailProduct'," + WebServiceConstants.PROVISION_WEBSERVICE_ID + "," + WebServiceConstants.UPDATE_VOICEMAIL_PRODUCT_METHOD_ID + ")\n;\n");
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','deleteVoicemailProduct',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'deleteVoicemailProduct'," + WebServiceConstants.PROVISION_WEBSERVICE_ID + "," + WebServiceConstants.DELETE_VOICEMAIL_PRODUCT_METHOD_ID + ")\n;\n");
//		
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','createDIDSubscription',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'createDIDSubscription'," + WebServiceConstants.PROVISION_WEBSERVICE_ID + "," + WebServiceConstants.CREATE_DID_SUBSCRIPTION_METHOD_ID + ")\n;\n");
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','readDIDSubscription',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'readDIDSubscription'," + WebServiceConstants.PROVISION_WEBSERVICE_ID + "," + WebServiceConstants.READ_DID_SUBSCRIPTION_METHOD_ID + ")\n;\n");
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','updateDIDSubscription',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'updateDIDSubscription'," + WebServiceConstants.PROVISION_WEBSERVICE_ID + "," + WebServiceConstants.UPDATE_DID_SUBSCRIPTION_METHOD_ID + ")\n;\n");
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','deleteDIDSubscription',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'deleteDIDSubscription'," + WebServiceConstants.PROVISION_WEBSERVICE_ID + "," + WebServiceConstants.DELETE_DID_SUBSCRIPTION_METHOD_ID + ")\n;\n");
//		
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','createSIPSubscription',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'createSIPSubscription'," + WebServiceConstants.PROVISION_WEBSERVICE_ID + "," + WebServiceConstants.CREATE_SIP_SUBSCRIPTION_METHOD_ID + ")\n;\n");
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','readSIPSubscription',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'readSIPSubscription'," + WebServiceConstants.PROVISION_WEBSERVICE_ID + "," + WebServiceConstants.READ_SIP_SUBSCRIPTION_METHOD_ID + ")\n;\n");
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','updateSIPSubscription',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'updateSIPSubscription'," + WebServiceConstants.PROVISION_WEBSERVICE_ID + "," + WebServiceConstants.UPDATE_SIP_SUBSCRIPTION_METHOD_ID + ")\n;\n");
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','deleteSIPSubscription',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'deleteSIPSubscription'," + WebServiceConstants.PROVISION_WEBSERVICE_ID + "," + WebServiceConstants.DELETE_SIP_SUBSCRIPTION_METHOD_ID + ")\n;\n");
//		
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','createVoicemailSubscription',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'createVoicemailSubscription'," + WebServiceConstants.PROVISION_WEBSERVICE_ID + "," + WebServiceConstants.CREATE_VOICEMAIL_SUBSCRIPTION_METHOD_ID + ")\n;\n");
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','readVoicemailSubscription',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'readVoicemailSubscription'," + WebServiceConstants.PROVISION_WEBSERVICE_ID + "," + WebServiceConstants.READ_VOICEMAIL_SUBSCRIPTION_METHOD_ID + ")\n;\n");
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','updateVoicemailSubscription',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'updateVoicemailSubscription'," + WebServiceConstants.PROVISION_WEBSERVICE_ID + "," + WebServiceConstants.UPDATE_VOICEMAIL_SUBSCRIPTION_METHOD_ID + ")\n;\n");
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','deleteVoicemailSubscription',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'deleteVoicemailSubscription'," + WebServiceConstants.PROVISION_WEBSERVICE_ID + "," + WebServiceConstants.DELETE_VOICEMAIL_SUBSCRIPTION_METHOD_ID + ")\n;\n");
//		
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','createSubscriber',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'createSubscriber'," + WebServiceConstants.PROVISION_WEBSERVICE_ID + "," + WebServiceConstants.CREATE_SUBSCRIBER_METHOD_ID + ")\n;\n");
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','readSubscriber',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'readSubscriber'," + WebServiceConstants.PROVISION_WEBSERVICE_ID + "," + WebServiceConstants.READ_SUBSCRIBER_METHOD_ID + ")\n;\n");
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','updateSubscriber',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'updateSubscriber'," + WebServiceConstants.PROVISION_WEBSERVICE_ID + "," + WebServiceConstants.UPDATE_SUBSCRIBER_METHOD_ID + ")\n;\n");
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','deleteSubscriber',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'deleteSubscriber'," + WebServiceConstants.PROVISION_WEBSERVICE_ID + "," + WebServiceConstants.DELETE_SUBSCRIBER_METHOD_ID + ")\n;\n");
//		
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','createUserPreference',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'createUserPreference'," + WebServiceConstants.PROVISION_WEBSERVICE_ID + "," + WebServiceConstants.CREATE_USER_PREFERENCE_METHOD_ID + ")\n;\n");
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','readUserPreference',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'readUserPreference'," + WebServiceConstants.PROVISION_WEBSERVICE_ID + "," + WebServiceConstants.READ_USER_PREFERENCE_METHOD_ID + ")\n;\n");
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','updateUserPreference',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'updateUserPreference'," + WebServiceConstants.PROVISION_WEBSERVICE_ID + "," + WebServiceConstants.UPDATE_USER_PREFERENCE_METHOD_ID + ")\n;\n");
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','deleteUserPreference',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'deleteUserPreference'," + WebServiceConstants.PROVISION_WEBSERVICE_ID + "," + WebServiceConstants.DELETE_USER_PREFERENCE_METHOD_ID + ")\n;\n");
//		
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','createVoicemailUser',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'createVoicemailUser'," + WebServiceConstants.PROVISION_WEBSERVICE_ID + "," + WebServiceConstants.CREATE_VOICEMAIL_USER_METHOD_ID + ")\n;\n");
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','readVoicemailUser',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'readVoicemailUser'," + WebServiceConstants.PROVISION_WEBSERVICE_ID + "," + WebServiceConstants.READ_VOICEMAIL_USER_METHOD_ID + ")\n;\n");
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','updateVoicemailUser',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'updateVoicemailUser'," + WebServiceConstants.PROVISION_WEBSERVICE_ID + "," + WebServiceConstants.UPDATE_VOICEMAIL_USER_METHOD_ID + ")\n;\n");
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','deleteVoicemailUser',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'deleteVoicemailUser'," + WebServiceConstants.PROVISION_WEBSERVICE_ID + "," + WebServiceConstants.DELETE_VOICEMAIL_USER_METHOD_ID + ")\n;\n");
//		
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','createVoicemailUserPreferences',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'createVoicemailUserPreferences'," + WebServiceConstants.PROVISION_WEBSERVICE_ID + "," + WebServiceConstants.CREATE_VOICEMAIL_USER_PREFERENCES_METHOD_ID + ")\n;\n");
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','readVoicemailUserPreferences',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'readVoicemailUserPreferences'," + WebServiceConstants.PROVISION_WEBSERVICE_ID + "," + WebServiceConstants.READ_VOICEMAIL_USER_PREFERENCES_METHOD_ID + ")\n;\n");
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','updateVoicemailUserPreferences',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'updateVoicemailUserPreferences'," + WebServiceConstants.PROVISION_WEBSERVICE_ID + "," + WebServiceConstants.UPDATE_VOICEMAIL_USER_PREFERENCES_METHOD_ID + ")\n;\n");
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','deleteVoicemailUserPreferences',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'deleteVoicemailUserPreferences'," + WebServiceConstants.PROVISION_WEBSERVICE_ID + "," + WebServiceConstants.DELETE_VOICEMAIL_USER_PREFERENCES_METHOD_ID + ")\n;\n");
//		
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','createVoicemailDialPlan',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'createVoicemailDialPlan'," + WebServiceConstants.PROVISION_WEBSERVICE_ID + "," + WebServiceConstants.CREATE_VOICEMAIL_DIALPLAN_METHOD_ID + ")\n;\n");
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','readVoicemailDialPlan',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'readVoicemailDialPlan'," + WebServiceConstants.PROVISION_WEBSERVICE_ID + "," + WebServiceConstants.READ_VOICEMAIL_DIALPLAN_METHOD_ID + ")\n;\n");
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','updateVoicemailDialPlan',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'updateVoicemailDialPlan'," + WebServiceConstants.PROVISION_WEBSERVICE_ID + "," + WebServiceConstants.UPDATE_VOICEMAIL_DIALPLAN_METHOD_ID + ")\n;\n");
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','deleteVoicemailDialPlan',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'deleteVoicemailDialPlan'," + WebServiceConstants.PROVISION_WEBSERVICE_ID + "," + WebServiceConstants.DELETE_VOICEMAIL_DIALPLAN_METHOD_ID + ")\n;\n");		
//		
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','validateProvisionDIDParameters',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'validateProvisionDIDParameters'," + WebServiceConstants.PROVISION_WEBSERVICE_ID + "," + WebServiceConstants.VALIDATE_PROVISION_DID_PARAMETERS_METHOD_ID + ")\n;\n");
//		out.write("\n");
//		
//		out.write("-- ------------------------------------------------------ Admin Web Service\n");
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','createBusinessPartner',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'createBusinessPartner'," + WebServiceConstants.ADMIN_WEBSERVICE_ID + "," + WebServiceConstants.CREATE_BUSINESS_PARTNER_METHOD_ID + ")\n;\n");
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','readBusinessPartner',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'readBusinessPartner'," + WebServiceConstants.ADMIN_WEBSERVICE_ID + "," + WebServiceConstants.READ_BUSINESS_PARTNER_METHOD_ID + ")\n;\n");
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','updateBusinessPartner',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'updateBusinessPartner'," + WebServiceConstants.ADMIN_WEBSERVICE_ID + "," + WebServiceConstants.UPDATE_BUSINESS_PARTNER_METHOD_ID + ")\n;\n");
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','deleteBusinessPartner',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'deleteBusinessPartner'," + WebServiceConstants.ADMIN_WEBSERVICE_ID + "," + WebServiceConstants.DELETE_BUSINESS_PARTNER_METHOD_ID + ")\n;\n");
//		
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','createBusinessPartnerLocation',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'createBusinessPartnerLocation'," + WebServiceConstants.ADMIN_WEBSERVICE_ID + "," + WebServiceConstants.CREATE_BUSINESS_PARTNER_LOCATION_METHOD_ID + ")\n;\n");
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','readBusinessPartnerLocation',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'readBusinessPartnerLocation'," + WebServiceConstants.ADMIN_WEBSERVICE_ID + "," + WebServiceConstants.READ_BUSINESS_PARTNER_LOCATION_METHOD_ID + ")\n;\n");
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','updateBusinessPartnerLocation',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'updateBusinessPartnerLocation'," + WebServiceConstants.ADMIN_WEBSERVICE_ID + "," + WebServiceConstants.UPDATE_BUSINESS_PARTNER_LOCATION_METHOD_ID + ")\n;\n");
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','deleteBusinessPartnerLocation',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'deleteBusinessPartnerLocation'," + WebServiceConstants.ADMIN_WEBSERVICE_ID + "," + WebServiceConstants.DELETE_BUSINESS_PARTNER_LOCATION_METHOD_ID + ")\n;\n");
//				
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','createLocation',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'createLocation'," + WebServiceConstants.ADMIN_WEBSERVICE_ID + "," + WebServiceConstants.CREATE_LOCATION_METHOD_ID + ")\n;\n");
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','readLocation',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'readLocation'," + WebServiceConstants.ADMIN_WEBSERVICE_ID + "," + WebServiceConstants.READ_LOCATION_METHOD_ID + ")\n;\n");
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','updateLocation',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'updateLocation'," + WebServiceConstants.ADMIN_WEBSERVICE_ID + "," + WebServiceConstants.UPDATE_LOCATION_METHOD_ID + ")\n;\n");
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','deleteLocation',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'deleteLocation'," + WebServiceConstants.ADMIN_WEBSERVICE_ID + "," + WebServiceConstants.DELETE_LOCATION_METHOD_ID + ")\n;\n");
//				
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','createUser',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'createUser'," + WebServiceConstants.ADMIN_WEBSERVICE_ID + "," + WebServiceConstants.CREATE_USER_METHOD_ID + ")\n;\n");
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','readUser',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'readUser'," + WebServiceConstants.ADMIN_WEBSERVICE_ID + "," + WebServiceConstants.READ_USER_METHOD_ID + ")\n;\n");
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','updateUser',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'updateUser'," + WebServiceConstants.ADMIN_WEBSERVICE_ID + "," + WebServiceConstants.UPDATE_USER_METHOD_ID + ")\n;\n");
//		out.write("INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','deleteUser',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'deleteUser'," + WebServiceConstants.ADMIN_WEBSERVICE_ID + "," + WebServiceConstants.DELETE_USER_METHOD_ID + ")\n;\n");
//		out.write("\n");
	}
	
	private static void printAD_Config(BufferedWriter out) throws IOException
	{
		out.write("-- ------------------------------------------------------ Add access for Conversant Admin Role\n");
		out.write("INSERT INTO AD_Window_Access (AD_Client_ID,AD_Org_ID,AD_Role_ID,AD_Window_ID,Created,CreatedBy,IsActive,Updated,UpdatedBy) VALUES (0,0,1000000,53068,TO_DATE('2009-01-30 17:57:08','YYYY-MM-DD HH24:MI:SS'),100,'Y',TO_DATE('2009-01-30 17:57:08','YYYY-MM-DD HH24:MI:SS'),100)\n;\n");
		out.write("\n");
		
		out.write("-- ------------------------------------------------------ Config role, user, access, web service type, parameters and access\n");
		out.write("INSERT INTO AD_Role (AD_Client_ID,AD_Org_ID,AD_Role_ID,Allow_Info_Account,Allow_Info_Asset,Allow_Info_BPartner,Allow_Info_CashJournal,Allow_Info_InOut,Allow_Info_Invoice,Allow_Info_Order,Allow_Info_Payment,Allow_Info_Product,Allow_Info_Resource,Allow_Info_Schedule,AmtApproval,C_Currency_ID,ConfirmQueryRecords,Created,CreatedBy,IsAccessAllOrgs,IsActive,IsCanApproveOwnDoc,IsCanExport,IsCanReport,IsChangeLog,IsManual,IsPersonalAccess,IsPersonalLock,IsShowAcct,IsUseUserOrgAccess,MaxQueryRecords,Name,OverwritePriceLimit,PreferenceType,Supervisor_ID,Updated,UpdatedBy,UserDiscount,UserLevel) VALUES (1000000,0,50004,'N','N','N','N','N','N','N','N','N','N','N',0,100,0,TO_DATE('2010-04-19 19:44:19','YYYY-MM-DD HH24:MI:SS'),100,'N','Y','N','N','N','Y','Y','N','N','N','N',0,'Web Service - Intalio Role','N','N',1000000,TO_DATE('2010-04-19 19:44:19','YYYY-MM-DD HH24:MI:SS'),100,0.00,' CO')\n;\n");
		out.write("INSERT INTO AD_User_Roles (AD_Client_ID,AD_Org_ID,AD_Role_ID,AD_User_ID,Created,CreatedBy,IsActive,Updated,UpdatedBy) VALUES (1000000,0,50004,100,TO_DATE('2010-04-19 19:44:19','YYYY-MM-DD HH24:MI:SS'),100,'Y',TO_DATE('2010-04-19 19:44:19','YYYY-MM-DD HH24:MI:SS'),100)\n;\n");
		out.write("INSERT INTO AD_User_Roles (AD_Client_ID,AD_Org_ID,AD_Role_ID,AD_User_ID,Created,CreatedBy,IsActive,Updated,UpdatedBy) VALUES (1000000,0,50004,0,TO_DATE('2010-04-19 19:44:19','YYYY-MM-DD HH24:MI:SS'),100,'Y',TO_DATE('2010-04-19 19:44:19','YYYY-MM-DD HH24:MI:SS'),100)\n;\n");
		out.write("INSERT INTO AD_Role_OrgAccess (AD_Client_ID,AD_Org_ID,AD_Role_ID,Created,CreatedBy,IsActive,IsReadOnly,Updated,UpdatedBy) VALUES (1000000,0,50004,TO_DATE('2010-04-19 19:44:19','YYYY-MM-DD HH24:MI:SS'),100,'Y','N',TO_DATE('2010-04-19 19:44:19','YYYY-MM-DD HH24:MI:SS'),100)\n;\n");
		out.write("INSERT INTO AD_Role_OrgAccess (AD_Client_ID,AD_Org_ID,AD_Role_ID,Created,CreatedBy,IsActive,IsReadOnly,Updated,UpdatedBy) VALUES (1000000,1000001,50004,TO_DATE('2010-04-19 19:44:19','YYYY-MM-DD HH24:MI:SS'),100,'Y','N',TO_DATE('2010-04-19 19:44:19','YYYY-MM-DD HH24:MI:SS'),100)\n;\n");
		out.write("INSERT INTO AD_User (AD_Client_ID,AD_Org_ID,AD_User_ID,Created,CreatedBy,IsActive,IsFullBPAccess,Name,NotificationType,Password,Processing,Updated,UpdatedBy,Value) VALUES (1000000,0,50001,TO_DATE('2010-04-19 19:44:19','YYYY-MM-DD HH24:MI:SS'),100,'Y','Y','IntalioUser','X','password','N',TO_DATE('2010-04-19 19:44:19','YYYY-MM-DD HH24:MI:SS'),100,'intaliouser')\n;\n");
		out.write("INSERT INTO AD_User_Roles (AD_Client_ID,AD_Org_ID,AD_Role_ID,AD_User_ID,Created,CreatedBy,IsActive,Updated,UpdatedBy) VALUES (1000000,0,50004,50001,TO_DATE('2010-04-19 19:44:19','YYYY-MM-DD HH24:MI:SS'),100,'Y',TO_DATE('2010-04-19 19:44:19','YYYY-MM-DD HH24:MI:SS'),100)\n;\n");
		out.write("\n");
	}
	
	private static void printWS_WebServiceType(BufferedWriter out) throws IOException
	{
		int webServiceTypeId = 50000;
		int webServiceParaId = 50000;
		
		Iterator<String> webServiceIterator = WebServiceConstants.WEBSERVICES.keySet().iterator();
		while (webServiceIterator.hasNext())
		{
			String webServiceName = webServiceIterator.next();
			Integer webServiceId = WebServiceConstants.WEBSERVICES.get(webServiceName);
			
			Iterator<String> webServiceMethodIterator = WebServiceConstants.WEBSERVICE_METHODS.get(webServiceName).keySet().iterator();
			while (webServiceMethodIterator.hasNext())
			{
				String webServiceMethodName = webServiceMethodIterator.next();
				Integer webServiceMethodId = WebServiceConstants.WEBSERVICE_METHODS.get(webServiceName).get(webServiceMethodName);
				
				String webServiceNameFirstLetterUpperCase = getWebServiceNameAbbreviation(webServiceName);
				String webServiceMethodNameFormatted = constantToCamelCase(webServiceMethodName.substring(0, webServiceMethodName.lastIndexOf("_METHOD_ID")));				
				String webServiceTypeName = webServiceNameFirstLetterUpperCase + "-" + webServiceMethodNameFormatted + "-" + "Intalio";
				if (webServiceTypeName.length() > 40)
					System.out.println(webServiceTypeName);
					
				out.write("-- ------------------------------------------------------ " + webServiceTypeName + "\n");
				out.write("INSERT INTO WS_WebServiceType (AD_Client_ID,AD_Org_ID,Created,CreatedBy,Description,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID,WS_WebServiceType_ID) VALUES (0,0,TO_DATE('2010-04-19 19:46:34','YYYY-MM-DD HH24:MI:SS'),100,'','Y','" + webServiceTypeName + "',TO_DATE('2010-04-19 19:46:34','YYYY-MM-DD HH24:MI:SS'),100,'" + webServiceTypeName + "'," + webServiceId + "," + webServiceMethodId + "," + webServiceTypeId + ")\n;\n");
				out.write("INSERT INTO WS_WebService_Para (AD_Client_ID,AD_Org_ID,ConstantValue,Created,CreatedBy,IsActive,ParameterName,ParameterType,Updated,UpdatedBy,WS_WebService_Para_ID,WS_WebServiceType_ID) VALUES (0,0,'1000000',TO_DATE('2010-04-19 19:47:23','YYYY-MM-DD HH24:MI:SS'),100,'Y','AD_Client_ID','C',TO_DATE('2010-04-19 19:47:23','YYYY-MM-DD HH24:MI:SS'),100," + webServiceParaId + "," + webServiceTypeId + ")\n;\n");
				out.write("INSERT INTO WS_WebService_Para (AD_Client_ID,AD_Org_ID,ConstantValue,Created,CreatedBy,IsActive,ParameterName,ParameterType,Updated,UpdatedBy,WS_WebService_Para_ID,WS_WebServiceType_ID) VALUES (0,0,'1000001',TO_DATE('2010-04-19 19:47:43','YYYY-MM-DD HH24:MI:SS'),100,'Y','AD_Org_ID','C',TO_DATE('2010-04-19 19:47:43','YYYY-MM-DD HH24:MI:SS'),100," + (webServiceParaId + 1) + "," + webServiceTypeId + ")\n;\n");
				out.write("INSERT INTO WS_WebService_Para (AD_Client_ID,AD_Org_ID,ConstantValue,Created,CreatedBy,IsActive,ParameterName,ParameterType,Updated,UpdatedBy,WS_WebService_Para_ID,WS_WebServiceType_ID) VALUES (0,0,'50004',TO_DATE('2010-04-19 19:47:51','YYYY-MM-DD HH24:MI:SS'),100,'Y','AD_Role_ID','C',TO_DATE('2010-04-19 19:47:51','YYYY-MM-DD HH24:MI:SS'),100," + (webServiceParaId + 2) + "," + webServiceTypeId + ")\n;\n");
				out.write("INSERT INTO WS_WebServiceTypeAccess (AD_Client_ID,AD_Org_ID,AD_Role_ID,Created,CreatedBy,IsActive,IsReadWrite,Updated,UpdatedBy,WS_WebServiceType_ID) VALUES (0,0,50004,TO_DATE('2010-04-19 19:48:12','YYYY-MM-DD HH24:MI:SS'),100,'Y','Y',TO_DATE('2010-04-19 19:48:12','YYYY-MM-DD HH24:MI:SS'),100," + webServiceTypeId + ")\n;\n");
				out.write("\n");
				
				webServiceTypeId++;			
				webServiceParaId = webServiceParaId + 3;
			}
		}
	}
	
	private static String getWebServiceNameAbbreviation(String webServiceNameToAbbreviate)
	{
		if (webServiceNameToAbbreviate.equalsIgnoreCase("ADMIN_WEBSERVICE"))
			return "AD";
		else if (webServiceNameToAbbreviate.equalsIgnoreCase("ACCOUNTING_WEBSERVICE"))
			return "AC";
		else
			return webServiceNameToAbbreviate.substring(0, 1).toUpperCase();
		
//		String abbreviatedName = webServiceNameToAbbreviate.substring(0, 1).toUpperCase();
//		
//		Iterator<String> webServiceIterator = WebServiceConstants.WEBSERVICES.keySet().iterator();
//		while (webServiceIterator.hasNext())
//		{
//			String webServiceName = webServiceIterator.next();
//							 
//			if (webServiceNameToAbbreviate.equalsIgnoreCase(webServiceName))
//			{
//				// do nothing
//			}				
//			else 
//			{
//				for (int i=1; i<=webServiceNameToAbbreviate.length(); i++)
//				{
//					if (!webServiceNameToAbbreviate.substring(0, i).equalsIgnoreCase(webServiceName.substring(0, i)))
//					{
//						String newAbbreviatedName = webServiceNameToAbbreviate.substring(0, i).toUpperCase();						
//						if (newAbbreviatedName.length() > abbreviatedName.length())
//							abbreviatedName = newAbbreviatedName;
//					}
//				}
//			}			
//		}
//		
//		return abbreviatedName;
	}
	
	private static String constantToCamelCase(String constant)
	{
		String lowerCaseConstant = constant.toLowerCase();
		
		char prevChar = ' ';
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<lowerCaseConstant.length(); i++)
		{		
			char currentChar = lowerCaseConstant.charAt(i);
			
			if (currentChar == '_')
				;// do nothing
			else if (prevChar == '_')
				sb.append(String.valueOf(currentChar).toUpperCase());
			else
				sb.append(currentChar);
			
			// Convert Did or Sip to uppercase
			if (sb.lastIndexOf("Did") > -1 || sb.lastIndexOf("Sip") > -1)
			{
				String sub = sb.substring(sb.length() - 3, sb.length());
				sub = sub.toUpperCase();
				sb = sb.delete(sb.length() - 3, sb.length());
				sb.append(sub);				
			}
			
			prevChar = currentChar;
		}
		
		return sb.toString();
	}
}
