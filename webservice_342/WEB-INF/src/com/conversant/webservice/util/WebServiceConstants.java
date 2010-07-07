package com.conversant.webservice.util;

import java.util.LinkedHashMap;

public class WebServiceConstants
{	
// **********************************************************************************

	public static final int STANDARD_RESPONSE_DEFAULT_ID = -1;
	
//// **********************************************************************************
//	
//	public static final int GENERIC_WEBSERVICE_ID = 50000;	
//	public static final int PROVISION_WEBSERVICE_ID = 50001;	
//	public static final int ADMIN_WEBSERVICE_ID = 50002;	
//
//// **********************************************************************************
//	
//	public static final int CREATE_TRX_METHOD_ID = 50000;
//	public static final int COMMIT_TRX_METHOD_ID = 50001;
//	public static final int ROLLBACK_TRX_METHOD_ID = 50002;
//
//// **********************************************************************************
//	
//	public static final int CREATE_DID_PRODUCT_METHOD_ID = 51000;
//	public static final int READ_DID_PRODUCT_METHOD_ID = 51001;
//	public static final int UPDATE_DID_PRODUCT_METHOD_ID = 51002;
//	public static final int DELETE_DID_PRODUCT_METHOD_ID = 51003;
//	
//	public static final int CREATE_SIP_PRODUCT_METHOD_ID = 51004;
//	public static final int READ_SIP_PRODUCT_METHOD_ID = 51005;
//	public static final int UPDATE_SIP_PRODUCT_METHOD_ID = 51006;
//	public static final int DELETE_SIP_PRODUCT_METHOD_ID = 51007;
//	
//	public static final int CREATE_VOICEMAIL_PRODUCT_METHOD_ID = 51008;
//	public static final int READ_VOICEMAIL_PRODUCT_METHOD_ID = 51009;
//	public static final int UPDATE_VOICEMAIL_PRODUCT_METHOD_ID = 51010;
//	public static final int DELETE_VOICEMAIL_PRODUCT_METHOD_ID = 51010;
//	
//	public static final int CREATE_DID_SUBSCRIPTION_METHOD_ID = 51012;
//	public static final int READ_DID_SUBSCRIPTION_METHOD_ID = 51013;
//	public static final int UPDATE_DID_SUBSCRIPTION_METHOD_ID = 51014;
//	public static final int DELETE_DID_SUBSCRIPTION_METHOD_ID = 51015;
//	
//	public static final int CREATE_SIP_SUBSCRIPTION_METHOD_ID = 51016;
//	public static final int READ_SIP_SUBSCRIPTION_METHOD_ID = 51017;
//	public static final int UPDATE_SIP_SUBSCRIPTION_METHOD_ID = 51018;
//	public static final int DELETE_SIP_SUBSCRIPTION_METHOD_ID = 51019;
//	
//	public static final int CREATE_VOICEMAIL_SUBSCRIPTION_METHOD_ID = 51020;
//	public static final int READ_VOICEMAIL_SUBSCRIPTION_METHOD_ID = 51021;
//	public static final int UPDATE_VOICEMAIL_SUBSCRIPTION_METHOD_ID = 51022;
//	public static final int DELETE_VOICEMAIL_SUBSCRIPTION_METHOD_ID = 51023;
//	
//	public static final int CREATE_SUBSCRIBER_METHOD_ID = 51024;
//	public static final int READ_SUBSCRIBER_METHOD_ID = 51025;
//	public static final int UPDATE_SUBSCRIBER_METHOD_ID = 51026;
//	public static final int DELETE_SUBSCRIBER_METHOD_ID = 51027;
//	
//	public static final int CREATE_USER_PREFERENCE_METHOD_ID = 51028;
//	public static final int READ_USER_PREFERENCE_METHOD_ID = 51029;
//	public static final int UPDATE_USER_PREFERENCE_METHOD_ID = 51030;
//	public static final int DELETE_USER_PREFERENCE_METHOD_ID = 51031;
//	
//	public static final int CREATE_VOICEMAIL_USER_METHOD_ID = 51032;
//	public static final int READ_VOICEMAIL_USER_METHOD_ID = 51033;
//	public static final int UPDATE_VOICEMAIL_USER_METHOD_ID = 51034;
//	public static final int DELETE_VOICEMAIL_USER_METHOD_ID = 51035;
//	
//	public static final int CREATE_VOICEMAIL_USER_PREFERENCES_METHOD_ID = 51036;
//	public static final int READ_VOICEMAIL_USER_PREFERENCES_METHOD_ID = 51037;
//	public static final int UPDATE_VOICEMAIL_USER_PREFERENCES_METHOD_ID = 51038;
//	public static final int DELETE_VOICEMAIL_USER_PREFERENCES_METHOD_ID = 51039;
//	
//	public static final int CREATE_VOICEMAIL_DIALPLAN_METHOD_ID = 51040;
//	public static final int READ_VOICEMAIL_DIALPLAN_METHOD_ID = 51041;
//	public static final int UPDATE_VOICEMAIL_DIALPLAN_METHOD_ID = 51042;
//	public static final int DELETE_VOICEMAIL_DIALPLAN_METHOD_ID = 51043;
//	
//	public static final int VALIDATE_PROVISION_DID_PARAMETERS_METHOD_ID = 51044;
//	
//// **********************************************************************************
//	
//	public static final int CREATE_BUSINESS_PARTNER_METHOD_ID = 52000;
//	public static final int READ_BUSINESS_PARTNER_METHOD_ID = 52001;
//	public static final int UPDATE_BUSINESS_PARTNER_METHOD_ID = 52002;
//	public static final int DELETE_BUSINESS_PARTNER_METHOD_ID = 52003;
//	
//	public static final int CREATE_BUSINESS_PARTNER_LOCATION_METHOD_ID = 52004;
//	public static final int READ_BUSINESS_PARTNER_LOCATION_METHOD_ID = 52005;
//	public static final int UPDATE_BUSINESS_PARTNER_LOCATION_METHOD_ID = 52006;
//	public static final int DELETE_BUSINESS_PARTNER_LOCATION_METHOD_ID = 52007;
//	
//	public static final int CREATE_LOCATION_METHOD_ID = 52008;
//	public static final int READ_LOCATION_METHOD_ID = 52009;
//	public static final int UPDATE_LOCATION_METHOD_ID = 52010;
//	public static final int DELETE_LOCATION_METHOD_ID = 52011;
//	
//	public static final int CREATE_USER_METHOD_ID = 52012;
//	public static final int READ_USER_METHOD_ID = 52013;
//	public static final int UPDATE_USER_METHOD_ID = 52014;
//	public static final int DELETE_USER_METHOD_ID = 52015;

// **********************************************************************************
	
	public static final LinkedHashMap<String, Integer> WEBSERVICES = new LinkedHashMap<String, Integer>() 
	{
		{
			put("GENERIC_WEBSERVICE", 50000);			
			put("PROVISION_WEBSERVICE", 50001);
			put("ADMIN_WEBSERVICE", 50002);
		}
	};

	public static final LinkedHashMap<String, Integer> GENERIC_WEBSERVICE_METHODS = new LinkedHashMap<String, Integer>() 
	{
		{
			put("CREATE_TRX_METHOD_ID", 50000);
			put("COMMIT_TRX_METHOD_ID", 50001);
			put("ROLLBACK_TRX_METHOD_ID", 50002);
		}
	};
	
	public static final LinkedHashMap<String, Integer> PROVISION_WEBSERVICE_METHODS = new LinkedHashMap<String, Integer>() 
	{
		{
			put("CREATE_DID_PRODUCT_METHOD_ID", 51000);
			put("READ_DID_PRODUCT_METHOD_ID", 51001);
			put("UPDATE_DID_PRODUCT_METHOD_ID", 51002);
			put("DELETE_DID_PRODUCT_METHOD_ID", 51003);
			
			put("CREATE_SIP_PRODUCT_METHOD_ID", 51004);
			put("READ_SIP_PRODUCT_METHOD_ID", 51005);
			put("UPDATE_SIP_PRODUCT_METHOD_ID", 51006);
			put("DELETE_SIP_PRODUCT_METHOD_ID", 51007);
			
			put("CREATE_VOICEMAIL_PRODUCT_METHOD_ID", 51008);
			put("READ_VOICEMAIL_PRODUCT_METHOD_ID", 51009);
			put("UPDATE_VOICEMAIL_PRODUCT_METHOD_ID", 51010);
			put("DELETE_VOICEMAIL_PRODUCT_METHOD_ID", 51011);
			
			put("CREATE_DID_SUBSCRIPTION_METHOD_ID", 51012);
			put("READ_DID_SUBSCRIPTION_METHOD_ID", 51013);
			put("UPDATE_DID_SUBSCRIPTION_METHOD_ID", 51014);
			put("DELETE_DID_SUBSCRIPTION_METHOD_ID", 51015);
			
			put("CREATE_SIP_SUBSCRIPTION_METHOD_ID", 51016);
			put("READ_SIP_SUBSCRIPTION_METHOD_ID", 51017);
			put("UPDATE_SIP_SUBSCRIPTION_METHOD_ID", 51018);
			put("DELETE_SIP_SUBSCRIPTION_METHOD_ID", 51019);
			
			put("CREATE_VOICEMAIL_SUBSCRIPTION_METHOD_ID", 51020);
			put("READ_VOICEMAIL_SUBSCRIPTION_METHOD_ID", 51021);
			put("UPDATE_VOICEMAIL_SUBSCRIPTION_METHOD_ID", 51022);
			put("DELETE_VOICEMAIL_SUBSCRIPTION_METHOD_ID", 51023);
			
			put("CREATE_SUBSCRIBER_METHOD_ID", 51024);
			put("READ_SUBSCRIBER_METHOD_ID", 51025);
			put("UPDATE_SUBSCRIBER_METHOD_ID", 51026);
			put("DELETE_SUBSCRIBER_METHOD_ID", 51027);
			
			put("CREATE_USER_PREFERENCE_METHOD_ID", 51028);
			put("READ_USER_PREFERENCE_METHOD_ID", 51029);
			put("UPDATE_USER_PREFERENCE_METHOD_ID", 51030);
			put("DELETE_USER_PREFERENCE_METHOD_ID", 51031);
			
			put("CREATE_VOICEMAIL_USER_METHOD_ID", 51032);
			put("READ_VOICEMAIL_USER_METHOD_ID", 51033);
			put("UPDATE_VOICEMAIL_USER_METHOD_ID", 51034);
			put("DELETE_VOICEMAIL_USER_METHOD_ID", 51035);
			
			put("CREATE_VOICEMAIL_USER_PREFERENCES_METHOD_ID", 51036);
			put("READ_VOICEMAIL_USER_PREFERENCES_METHOD_ID", 51037);
			put("UPDATE_VOICEMAIL_USER_PREFERENCES_METHOD_ID", 51038);
			put("DELETE_VOICEMAIL_USER_PREFERENCES_METHOD_ID", 51039);
			
			put("CREATE_VOICEMAIL_DIALPLAN_METHOD_ID", 51040);
			put("READ_VOICEMAIL_DIALPLAN_METHOD_ID", 51041);
			put("UPDATE_VOICEMAIL_DIALPLAN_METHOD_ID", 51042);
			put("DELETE_VOICEMAIL_DIALPLAN_METHOD_ID", 51043);
			
			put("VALIDATE_PROVISION_DID_PARAMETERS_METHOD_ID", 51044);
		}
	};
	
	public static final LinkedHashMap<String, Integer> ADMIN_WEBSERVICE_METHODS = new LinkedHashMap<String, Integer>() 
	{
		{
			put("CREATE_BUSINESS_PARTNER_METHOD_ID", 52000);
			put("READ_BUSINESS_PARTNER_METHOD_ID", 52001);
			put("UPDATE_BUSINESS_PARTNER_METHOD_ID", 52002);
			put("DELETE_BUSINESS_PARTNER_METHOD_ID", 52003);
			
			put("CREATE_BUSINESS_PARTNER_LOCATION_METHOD_ID", 52004);
			put("READ_BUSINESS_PARTNER_LOCATION_METHOD_ID", 52005);
			put("UPDATE_BUSINESS_PARTNER_LOCATION_METHOD_ID", 52006);
			put("DELETE_BUSINESS_PARTNER_LOCATION_METHOD_ID", 52007);
			
			put("CREATE_LOCATION_METHOD_ID", 52008);
			put("READ_LOCATION_METHOD_ID", 52009);
			put("UPDATE_LOCATION_METHOD_ID", 52010);
			put("DELETE_LOCATION_METHOD_ID", 52011);
			
			put("CREATE_USER_METHOD_ID", 52012);
			put("READ_USER_METHOD_ID", 52013);
			put("UPDATE_USER_METHOD_ID", 52014);
			put("DELETE_USER_METHOD_ID", 52015);
		}
	};
	
	public static final LinkedHashMap<String, LinkedHashMap<String, Integer>> WEBSERVICE_METHODS = new LinkedHashMap<String, LinkedHashMap<String, Integer>>() 
	{
		{
			put("GENERIC_WEBSERVICE", GENERIC_WEBSERVICE_METHODS);
			put("PROVISION_WEBSERVICE", PROVISION_WEBSERVICE_METHODS);
			put("ADMIN_WEBSERVICE", ADMIN_WEBSERVICE_METHODS);
		}
	};
	
// **********************************************************************************
}
