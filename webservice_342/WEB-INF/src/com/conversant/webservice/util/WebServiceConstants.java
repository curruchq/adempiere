package com.conversant.webservice.util;

import java.util.LinkedHashMap;

public class WebServiceConstants
{	
// **********************************************************************************

	public static final int STANDARD_RESPONSE_DEFAULT_ID = -1;
	
// **********************************************************************************
	
	public static final LinkedHashMap<String, Integer> WEBSERVICES = new LinkedHashMap<String, Integer>() 
	{
		{
			put("GENERIC_WEBSERVICE", 50000);			
			put("PROVISION_WEBSERVICE", 50001);
			put("ADMIN_WEBSERVICE", 50002);
			put("ACCOUNTING_WEBSERVICE", 50003);
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
			
			put("CREATE_CALL_PRODUCT_METHOD_ID", 51045);
			put("READ_CALL_PRODUCT_METHOD_ID", 51046);
			put("UPDATE_CALL_PRODUCT_METHOD_ID", 51047);
			put("DELETE_CALL_PRODUCT_METHOD_ID", 51048);
			
			put("CREATE_CALL_SUBSCRIPTION_METHOD_ID", 51049);
			put("READ_CALL_SUBSCRIPTION_METHOD_ID", 51050);
			put("UPDATE_CALL_SUBSCRIPTION_METHOD_ID", 51051);
			put("DELETE_CALL_SUBSCRIPTION_METHOD_ID", 51052);
			
			put("READ_RADIUS_ACCOUNTS_BY_INVOICE", 51053); 
			
			put("CREATE_NUMBER_PORT_SUBSCRIPTION_METHOD_ID", 51054); 
			put("READ_NUMBER_PORT_SUBSCRIPTION_METHOD_ID", 51055); 
			put("UPDATE_NUMBER_PORT_SUBSCRIPTION_METHOD_ID", 51056); 
			put("DELETE_NUMBER_PORT_SUBSCRIPTION_METHOD_ID", 51057); 
			
			put("READ_RADIUS_ACCOUNTS_SEARCH", 51058);
			
			put("END_DID_SUBSCRIPTION", 51059);
			put("END_SIP_SUBSCRIPTION", 51060);
			put("END_VOICEMAIL_SUBSCRIPTION", 51061);
			put("END_CALL_SUBSCRIPTION", 51062);
			
			put("END_VOICEMAIL_USER_PREFERENCES_METHOD_ID", 51063);
			
			put("CREATE_CVOX_USER_METHOD_ID", 51064);
			put("UPDATE_CVOX_USER_METHOD_ID", 51065);
			
			put("CREATE_CALL_PRODUCT2_METHOD_ID", 51066);
			
			put("CREATE_CALL_SUBSCRIPTION2_METHOD_ID",51067);//latest
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

			put("READ_BUSINESS_PARTNERS_BY_GROUP_METHOD_ID", 52016);
			put("READ_BUSINESS_PARTNER_BY_SEARCHKEY_METHOD_ID",52037);
			
			put("CREATE_SUBSCRIPTION_METHOD_ID", 52017);
			put("READ_SUBSCRIPTION_METHOD_ID", 52018);
			put("UPDATE_SUBSCRIPTION_METHOD_ID", 52019);
			put("DELETE_SUBSCRIPTION_METHOD_ID", 52020);
			
			put("READ_SUBSCRIPTIONS_METHOD_ID", 52021);
			
			put("CREATE_ORDER_METHOD_ID", 52022);
			put("READ_ORDER_METHOD_ID", 52023);
			put("UPDATE_ORDER_METHOD_ID", 52024);
			put("DELETE_ORDER_METHOD_ID", 52025);
						
			put("READ_ORDER_DIDS_METHOD_ID", 52026);
			
			put("READ_USERS_BY_EMAIL_METHOD_ID", 52027);
			
			put("READ_ORDER_NUMBER_PORTS_METHOD_ID", 52028);
			
			put("READ_SUBSCRIBED_NUMBERS_METHOD_ID", 52029);
			
			put("CREATE_USER_ROLE_METHOD_ID", 52030);
			put("READ_USER_ROLE_METHOD_ID", 52031);
			put("UPDATE_USER_ROLE_METHOD_ID", 52032);
			put("DELETE_USER_ROLE_METHOD_ID", 52033);
			
			put("READ_ROLES_METHOD_ID", 52034);
			
			put("READ_USERS_BY_BUSINESS_PARTNER_METHOD_ID", 52035);
			
			put("READ_CALL_RECORDING_METHOD_ID", 52036);
			
			put("READ_PRODUCT_BP_PRICE_METHOD_ID",52038); 
			
			put("READ_PRODUCT_METHOD_ID",52039);
			
			put("READ_ORGANIZATION_METHOD_ID",52040); 
			
			put("READ_ORDER_LINES_METHOD_ID",52041); 
			
			put("CREATE_ORDER_LINE_METHOD_ID" ,52042); 
			put("UPDATE_ORDER_LINE_METHOD_ID",52043); 
			put("READ_ORDERS_BY_BUSINESS_PARTNER_SEARCHKEY_METHOD_ID",52044);
			
			put("READ_USER_BY_SEARCH_KEY_METHOD_ID" ,52045); 
			
			put("READ_PRODUCT_PRICING_METHOD_ID" ,52046); //latest
		}
	};
	
	public static final LinkedHashMap<String, Integer> ACCOUNTING_WEBSERVICE_METHODS = new LinkedHashMap<String, Integer>() 
	{
		{
			put("CREATE_PAYMENT_METHOD_ID", 53000);
			put("READ_PAYMENT_METHOD_ID", 53001);
			put("UPDATE_PAYMENT_METHOD_ID", 53002);
			put("DELETE_PAYMENT_METHOD_ID", 53003);
			
			put("PROCESS_PAYMENT_METHOD_ID", 53004);
			
			put("CREATE_BP_BANK_ACCOUNT_METHOD_ID", 53005);
			put("READ_BP_BANK_ACCOUNT_METHOD_ID", 53006);
			put("UPDATE_BP_BANK_ACCOUNT_METHOD_ID", 53007);
			put("DELETE_BP_BANK_ACCOUNT_METHOD_ID", 53008);
			
			put("CREATE_INVOICE_METHOD_ID", 53009);
			put("READ_INVOICE_METHOD_ID", 53010);
			put("UPDATE_INVOICE_METHOD_ID", 53011);
			put("DELETE_INVOICE_METHOD_ID", 53012);
			
			put("READ_INVOICES_BY_BUSINESS_PARTNER_METHOD_ID", 53013);
			
			put("READ_INVOICE_LINES_METHOD_ID",53014);
			
			put("READ_INVOICE_TAX_LINES_METHOD_ID" , 53015);
			
			put("READ_PAYMENTS_BY_BUSINESS_PARTNER_METHOD_ID" , 53016);
			
			put("CREATE_ONE_OFF_PAYMENT_METHOD_ID" , 53017);
		}
	};
	
	public static final LinkedHashMap<String, LinkedHashMap<String, Integer>> WEBSERVICE_METHODS = new LinkedHashMap<String, LinkedHashMap<String, Integer>>() 
	{
		{
			put("GENERIC_WEBSERVICE", GENERIC_WEBSERVICE_METHODS);
			put("PROVISION_WEBSERVICE", PROVISION_WEBSERVICE_METHODS);
			put("ADMIN_WEBSERVICE", ADMIN_WEBSERVICE_METHODS);
			put("ACCOUNTING_WEBSERVICE", ACCOUNTING_WEBSERVICE_METHODS);
		}
	};
	
// **********************************************************************************
}
