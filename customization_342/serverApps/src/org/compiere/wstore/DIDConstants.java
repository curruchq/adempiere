package org.compiere.wstore;


/**
 *  DIDXService Constants
 *
 *  @author Josh Hill
 *  @version  $Id: DIDXService.java,v 1.0 2008/03/13 14:53:21 jhill Exp $
 */
public class DIDConstants
{
	/* 
	 * TODO: Add a new table to compiere to store DIDX info (userid, password, urls & class constants)
	 */
	
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
	
	public static final int ATTRIBUTE_ID_SIP_ADDRESS = 1000017;
	public static final int ATTRIBUTE_ID_SIP_DOMAIN = 1000018;
	
	public static final int ATTRIBUTE_ID_VM_MAILBOX_NUMBER = 1000020;
	public static final int ATTRIBUTE_ID_VM_CONTEXT = 1000021;
	public static final int ATTRIBUTE_ID_VM_MACRO_NAME = 1000022;
	
	protected static final String ERROR_MSG_GENERIC_KEY = "*";
	
	protected static final int C_SUBSCRIPTIONTYPE_ID_MONTH_1 = 1000004;
	
	/** Window, Menu & Tab IDs					*/
	protected static final int AD_WINDOW_ID_M_PRODUCT = 140;
	protected static final int AD_MENU_ID_M_PRODUCT = 126;
	protected static final int M_PRODUCT_PRODUCT_TAB_ID = 0;
	
	/** Name for invalid product				 */
	public static final String INVALID_PRODUCT_NAME = "INVALID PRODUCT";
	protected static final String INVALID_RELATION_NAME = "INVALID RELATION";
	
	/** */
	public static final String NUMBER_IDENTIFIER = "##NUMBER##";
	protected static final String DID_AREA_CODE_DESC_IDENTIFIER = "##DIDAREACODEDESC##";
	
	/** */
	protected static final String DID_SETUP_PRODUCT_SEARCH_KEY = "DIDSU-" + NUMBER_IDENTIFIER;
	protected static final String DID_PRODUCT_SEARCH_KEY = "DID-" + NUMBER_IDENTIFIER;
	protected static final String DID_SETUP_PRODUCT_NAME = "DID/DDI setup fee. " + NUMBER_IDENTIFIER;
	protected static final String DID_PRODUCT_NAME = "DID/DDI monthly charge. " + NUMBER_IDENTIFIER;
	protected static final String DID_SETUP_PRODUCT_DESC = "DID/DDI setup fee. " + DID_AREA_CODE_DESC_IDENTIFIER;
	protected static final String DID_PRODUCT_DESC = "DID/DDI monthly charge. " + DID_AREA_CODE_DESC_IDENTIFIER;
	
	/** */
	protected static final String SIP_PRODUCT_SEARCH_KEY = "CVC-" + NUMBER_IDENTIFIER;
	protected static final String SIP_PRODUCT_NAME = "C-Voice SIP Account. " + NUMBER_IDENTIFIER;
	protected static final String SIP_PRODUCT_DESC = "C-Voice SIP Account. User ID: " + NUMBER_IDENTIFIER;
	
	protected static final String VOICEMAIL_PRODUCT_SEARCH_KEY = "VMS-" + NUMBER_IDENTIFIER;
	protected static final String VOICEMAIL_PRODUCT_NAME = "Voicemail standard. " + NUMBER_IDENTIFIER;
	protected static final String VOICEMAIL_PRODUCT_DESC = "Voicemail standard. " + NUMBER_IDENTIFIER;
	protected static final String VOICEMAIL_SUBSCRIBER_NAME = "VM-" + NUMBER_IDENTIFIER;
	
	/** */
	protected static final String RELATED_PRODUCT_NAME = "Setup Product";
	protected static final String RELATED_PRODUCT_DESC = "Related DID Setup Product";
	
	protected static final String PRODUCT_PO_INVALID_VENDOR_PRODUCT_NO = "(invalid)";
	public static final String PRODUCT_PO_SETUP_VENDOR_PRODUCT_NO = "DIDSU-" + NUMBER_IDENTIFIER;
	public static final String PRODUCT_PO_MONTHLY_VENDOR_PRODUCT_NO = "DID-" + NUMBER_IDENTIFIER;
	
	/** */
	protected static final String VOICE_SERVICES_CATEGORY_ID = "1000001";
	protected static final String STANDARD_TAX_CATEGORY = "1000000";
	protected static final String PRODUCT_TYPE_SERVICE = "S";
	protected static final String UOM_EACH = "100";
	protected static final String UOM_MONTH_8DEC = "1000000";
	protected static final String DID_ATTRIBUTE_SET_ID = "1000002";
	protected static final String SIP_ATTRIBUTE_SET_ID = "1000004";
	protected static final String VOICEMAIL_ATTRIBUTE_SET_ID = "1000005";
	protected static final String NOT_SELF_SERVICE = "N";
	public static final int BP_SUPER_TECH_INC_ID = 1000023;
	protected static final String RELATED_PRODUCT_TYPE_SETUP = "O";

	/** Currency Constants 														*/	
	protected static final int USD_CURRENCY_ID = 100;
	public static final int NZD_CURRENCY_ID = 121;
	protected static final int DEFAULT_CURRENCY_ID = USD_CURRENCY_ID;
	protected static final int CUSTOM_CONV_TYPE_ID = 1000000;
	protected static final int FALLBACK_USD_TO_NZD_RATE = 2;
	
	/** Default SIP domain 														*/
	public static final String DEFAULT_SIP_DOMAIN = "conversant.co.nz";
	
	/** Default SIP account password											*/
	public static final String DEFAULT_SIP_PASSWORD = "password";
	
	/** Default SIP timezone 													*/
	public static final String DEFAULT_SIP_TIMEZONE = "Pacific/Auckland";
	
	
}
