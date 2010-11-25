package com.conversant.did;


/**
 *  DID Constants
 *
 *  @author Josh Hill
 *  @version  $Id: DIDConstants.java,v 1.0 2008/03/13 14:53:21 jhill Exp $
 */
public class DIDConstants
{
	/* 
	 * TODO: Add a new table to compiere to store DIDX info (userid, password, urls & class constants)
	 */
	
	/** */
	public static final String ADDRESS_IDENTIFIER = "##ADDRESS##";
	public static final String DOMAIN_IDENTIFIER = "##DOMAIN##";
	public static final String NUMBER_IDENTIFIER = "##NUMBER##";
	public static final String DID_DESCRIPTION_IDENTIFIER = "##DIDDESCRIPTION##";
	
	public static final int ATTRIBUTE_ID_DID_ISSETUP = 1000008;
	public static final int ATTRIBUTE_ID_DID_FREEMINS = 1000009;
	public static final int ATTRIBUTE_ID_DID_VENDORRATING = 1000010;
	public static final int ATTRIBUTE_ID_DID_COUNTRYCODE = 1000011;
	public static final int ATTRIBUTE_ID_DID_AREACODE = 1000012;
	public static final int ATTRIBUTE_ID_DID_PERMINCHARGES = 1000013;
	public static final int ATTRIBUTE_ID_DID_DESCRIPTION = 1000014;
	public static final int ATTRIBUTE_ID_DID_NUMBER = 1000015;
	public static final int ATTRIBUTE_ID_DID_SUBSCRIBED = 1000016;
	public static final int ATTRIBUTE_ID_DID_COUNTRYID = 1000019;
	public static final int ATTRIBUTE_ID_DID_FAX_ISFAX = 1000030;
	public static final int ATTRIBUTE_ID_DID_FAX_TOEMAIL = 1000031;
	public static final int ATTRIBUTE_ID_DID_FAX_FROMEMAIL = 1000032;
	
	public static final int ATTRIBUTE_ID_SIP_ADDRESS = 1000017;
	public static final int ATTRIBUTE_ID_SIP_DOMAIN = 1000018;
	
	public static final int ATTRIBUTE_ID_VM_MAILBOX_NUMBER = 1000020;
	public static final int ATTRIBUTE_ID_VM_CONTEXT = 1000021;
	public static final int ATTRIBUTE_ID_VM_MACRO_NAME = 1000022;
	
	public static final String ATTRIBUTE_VALUE_INBOUND_CDR_USERNAME = "+" + NUMBER_IDENTIFIER + "@" + "inbound." + DOMAIN_IDENTIFIER;
	public static final String ATTRIBUTE_VALUE_OUTBOUND_CDR_USERNAME = NUMBER_IDENTIFIER + "@" + DOMAIN_IDENTIFIER;
	public static final int ATTRIBUTE_ID_CDR_USERNAME = 1000033;
	public static final int ATTRIBUTE_ID_CDR_APPLICATION = 1000034;
	public static final int ATTRIBUTE_ID_CDR_NUMBER = 1000035;
	public static final int ATTRIBUTE_ID_CDR_DIRECTION = 1000036;
	
	public static final int ATTRIBUTE_ID_CDR_APPLICATION_VALUE_AUDIO = 1000002;
	public static final int ATTRIBUTE_ID_CDR_DIRECTION_VALUE_INBOUND = 1000003;
	public static final int ATTRIBUTE_ID_CDR_DIRECTION_VALUE_OUTBOUND = 1000004;
	
	public static final String ERROR_MSG_GENERIC_KEY = "*";
	
	public static final int C_SUBSCRIPTIONTYPE_ID_MONTH_1 = 1000004;
	public static final int C_SUBSCRIPTIONTYPE_ID_ONE_OFF = 1000005;
	
	/** Window, Menu & Tab IDs					*/
//	public static final int AD_WINDOW_ID_M_PRODUCT = 140;
//	public static final int AD_MENU_ID_M_PRODUCT = 126;
//	public static final int M_PRODUCT_PRODUCT_TAB_ID = 0;
	
	/** Name for invalid product				 */
	public static final String INVALID_PRODUCT_NAME = "INVALID PRODUCT";
	public static final String INVALID_RELATION_NAME = "INVALID RELATION";
	
	/** */
	public static final String DID_SETUP_PRODUCT_SEARCH_KEY = "DIDSU-" + NUMBER_IDENTIFIER;
	public static final String DID_MONTHLY_PRODUCT_SEARCH_KEY = "DID-" + NUMBER_IDENTIFIER;
	public static final String DID_SETUP_PRODUCT_NAME = "DID/DDI setup fee. " + NUMBER_IDENTIFIER;
	public static final String DID_MONTHLY_PRODUCT_NAME = "DID/DDI monthly charge. " + NUMBER_IDENTIFIER;
	public static final String DID_SETUP_PRODUCT_DESCRIPTION = "DID/DDI setup fee. " + DID_DESCRIPTION_IDENTIFIER;
	public static final String DID_MONTHLY_PRODUCT_DESCRIPTION = "DID/DDI monthly charge. " + DID_DESCRIPTION_IDENTIFIER;
	
	/** */
	public static final String SIP_PRODUCT_SEARCH_KEY = "CVC-" + NUMBER_IDENTIFIER;
	public static final String SIP_PRODUCT_NAME = "C-Voice SIP Account. " + NUMBER_IDENTIFIER;
	public static final String SIP_PRODUCT_DESCRIPTION = "C-Voice SIP Account. User ID: " + NUMBER_IDENTIFIER;
	
	/** */
	public static final String VOICEMAIL_PRODUCT_SEARCH_KEY = "VMS-" + NUMBER_IDENTIFIER;
	public static final String VOICEMAIL_PRODUCT_NAME = "Voicemail standard. " + NUMBER_IDENTIFIER;
	public static final String VOICEMAIL_PRODUCT_DESC = "Voicemail standard. " + NUMBER_IDENTIFIER;
	public static final String VOICEMAIL_SUBSCRIBER_NAME = "VM-" + NUMBER_IDENTIFIER;
	
	/** */
	public static final String CALL_OUT_PRODUCT_SEARCH_KEY = "CALL-OUT-" + NUMBER_IDENTIFIER;
	public static final String CALL_IN_PRODUCT_SEARCH_KEY = "CALL-IN-" + NUMBER_IDENTIFIER;
	public static final String CALL_OUT_PRODUCT_NAME = "+" + NUMBER_IDENTIFIER + " outbound calls";
	public static final String CALL_IN_PRODUCT_NAME = "+" + NUMBER_IDENTIFIER + " inbound calls";
	public static final String CALL_OUT_PRODUCT_DESCRIPTION = "+" + NUMBER_IDENTIFIER + " outbound calls";
	public static final String CALL_IN_PRODUCT_DESCRIPTION = "+" + NUMBER_IDENTIFIER + " inbound calls";
	
	/** */
	public static final String RELATED_PRODUCT_NAME = "Setup Product";
	public static final String RELATED_PRODUCT_DESC = "Related DID Setup Product";
	
	public static final String PRODUCT_PO_INVALID_VENDOR_PRODUCT_NO = "(invalid)";
	public static final String PRODUCT_PO_SETUP_VENDOR_PRODUCT_NO = "DIDSU-" + NUMBER_IDENTIFIER;
	public static final String PRODUCT_PO_MONTHLY_VENDOR_PRODUCT_NO = "DID-" + NUMBER_IDENTIFIER;
	
	/** */
	public static final String VOICE_SERVICES_CATEGORY_ID = "1000001";
	public static final String VOICE_SERVICES_RECUR_NONCALL_CATEGORY_ID = "1000009";
	public static final String VOICE_SERVICES_RECUR_CALL_CATEGORY_ID = "1000010";
	public static final String VOICE_SERVICES_NONRECUR_NONCALL_CATEGORY_ID = "1000012";
	public static final String STANDARD_125_TAX_CATEGORY = "1000000";
	public static final String STANDARD_15_TAX_CATEGORY = "1000017";
	public static final String PRODUCT_TYPE_SERVICE = "S";
	public static final String UOM_EACH = "100";
	public static final String UOM_MONTH_8DEC = "1000000";
	public static final String DID_ATTRIBUTE_SET_ID = "1000002";
	public static final String SIP_ATTRIBUTE_SET_ID = "1000004";
	public static final String VOICEMAIL_ATTRIBUTE_SET_ID = "1000005";
	public static final String CDR_ATTRIBUTE_SET_ID = "1000007";
	public static final String NOT_SELF_SERVICE = "N";
	public static final int BP_SUPER_TECH_INC_ID = 1000023;
	public static final String RELATED_PRODUCT_TYPE_SETUP = "O";

	/** Currency Constants 																	*/	
	public static final int USD_CURRENCY_ID = 100;
	public static final int NZD_CURRENCY_ID = 121;
	public static final int DEFAULT_CURRENCY_ID = USD_CURRENCY_ID;
	public static final int CUSTOM_CONV_TYPE_ID = 1000000;
	public static final int FALLBACK_USD_TO_NZD_RATE = 2;
	public static final int PRICELIST_VERSION_ID_STANDARD = 1000000;
	
	/** Default SIP domain 																	*/
	public static final String DEFAULT_SIP_DOMAIN = "conversant.co.nz";
	
	/** Default SIP account password														*/
	public static final String DEFAULT_SIP_PASSWORD = "password";
	
	/** Default SIP timezone 																*/
	public static final String DEFAULT_SIP_TIMEZONE = "Pacific/Auckland";
	
	/**	Subscription names																	*/ 										
	public static final String DID_SUBSCRIPTION_NAME = "+" + NUMBER_IDENTIFIER;
	public static final String SIP_SUBSCRIPTION_NAME = ADDRESS_IDENTIFIER + "@" + DOMAIN_IDENTIFIER; 
	public static final String VOICEMAIL_SUBSCRIPTION_NAME = "VM-" + NUMBER_IDENTIFIER;
	public static final String CALL_SUBSCRIPTION_NAME = "+" + NUMBER_IDENTIFIER;
	
	/**	User preference attributes															*/
	public static final String SER_USER_PREFERENCE_ATTRIBUTE_CONVERSEVOICE  = "ConverseVoice";
	public static final String SER_USER_PREFERENCE_ATTRIBUTE_COUT = "42001";
	public static final String SER_USER_PREFERENCE_ATTRIBUTE_VALUE_ACTIVE = "Active";
	public static final String SER_USER_PREFERENCE_ATTRIBUTE_VALUE_INACTIVE = "Inactive";
	
	/** User preference value for attribute type text e.g ConverseVoice						*/
	public static final String SER_USER_PREFERENCE_ATTRIBUTE_TYPE_TEXT = "0";
	
	/** User preference value for attribute type numberic e.g 42001 (out bound calling) 	*/
	public static final String SER_USER_PREFERENCE_ATTRIBUTE_TYPE_NUMERIC = "2";

}
