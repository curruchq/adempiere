package org.compiere.wstore;

import java.util.HashMap;

public class DIDXConstants
{
	/** DIDX.net UserID and Password	 										*/
	protected static final String USERID = "701357";//"708525";
	protected static final String PASSWORD = "0cfe3f456c";//password";
	
	/** DIDX.net SIP and IAX Flags												*/
	protected static final int SIP_FLAG = 1;
//	public static final int DIDX_IAX_FLAG = 2;
	
	/** DIDX.net Method Indicator												*/
	protected static enum DIDXMethod {GET_AVAILABLE_DIDS, GET_DID_COUNTRY, GET_DID_AREA, GET_DID_MINUTES_INFO, BUY_DID, RELEASE_DID, GET_DID_INFO, UPDATE_SIP_ADDRESS};
	
	/** DIDX.net DIDNumber Status Indicator										*/
	protected static enum DID_STATUS {AVAILABLE, RESERVED, SOLD};
	
	/** DIDx.net minimum and maximum Vendor Ratings								*/
	protected static final String MIN_VENDOR_RATING = "3";
	protected static final String MAX_VENDOR_RATING = "9";
	
	/** DIDX.net Method Names													*/
	protected static final String M_GET_AVAILABLE_DIDS = "getAvailableDIDS";
	protected static final String M_GET_DID_COUNTRY = "getDIDCountry";
	protected static final String M_GET_DID_AREA = "getDIDArea";
	protected static final String M_GET_DID_MINUTES_INFO = "getDIDMinutesInfo";
	protected static final String M_GET_DID_INFO = "getDIDInfo";
	protected static final String M_BUY_DID_BY_NUMBER = "BuyDIDByNumber";
	protected static final String M_RELEASE_DID = "ReleaseDID";
	protected static final String M_EDIT_URL = "EditURL";
	
//	/** DIDX.net API URI's 														*/
//	protected static final String DIDX_GET_LIST_URI = "http://didx.net/GetList";
//	protected static final String DIDX_GET_MIN_URI = "http://didx.net/GetMinutes";
//	protected static final String DIDX_GET_DID_INFO = "http://didx.net/GetDIDInfo";
//	protected static final String DIDX_BUY_DID_URI = "http://didx.net/BuyDID";
//	protected static final String DIDX_RELEASE_DID_URI = "http://www.didx.net/Release";
//	protected static final String DIDX_UPDATE_SIP_URI = "http://www.didx.net/Edit";
	
	/** DIDX.net API URN's													*/
	protected static final String URN_PREFIX = "urn:";
	protected static final String URN_GET_AVAILABLE_DIDS = URN_PREFIX + M_GET_AVAILABLE_DIDS;
	protected static final String URN_GET_DID_COUNTRY = URN_PREFIX + M_GET_DID_COUNTRY;
	protected static final String URN_GET_DID_AREA = URN_PREFIX + M_GET_DID_AREA;
	protected static final String URN_GET_DID_MINUTES_INFO = URN_PREFIX + M_GET_DID_MINUTES_INFO;
	protected static final String URN_GET_DID_INFO = URN_PREFIX + M_GET_DID_INFO;
	protected static final String URN_BUY_DID_BY_NUMBER = URN_PREFIX + M_BUY_DID_BY_NUMBER;
	protected static final String URN_RELEASE_DID = URN_PREFIX + M_RELEASE_DID;
	protected static final String URN_EDIT_URL = URN_PREFIX + M_EDIT_URL;
	
	/** DIDX.net API Proxies												*/
	protected static final String P_WEB_GET_LIST_SERVER = "http://api.didx.net/webservice/WebGetListServer.php";
	protected static final String P_WEB_GET_DID_COUNTRIES_SERVER = "http://api.didx.net/webservice/WebGetDIDCountriesServer.php";
	protected static final String P_WEB_GET_DID_AREAS_SERVER = "http://api.didx.net/webservice/WebGetDIDAreasServer.php";
	protected static final String P_WEB_GET_DIDS_MINUTES = "http://api.didx.net/webservice/WebGetDIDSMinutes.php";
	protected static final String P_WEB_GET_DID_INFO = "http://api.didx.net/webservice/WebGetDIDInfo.php";
	protected static final String P_WEB_BUY_DID_SERVER = "http://api.didx.net/webservice/WebBuyDIDServer.php";
	protected static final String P_WEB_RELEASE_DID_SERVER = "http://api.didx.net/webservice/WebReleaseDIDServer.php";
	protected static final String P_WEB_EDIT_URL_SERVER = "http://api.didx.net/webservice/WebEditURLServer.php";
	
//	protected static final String DIDX_EP_WEB_GET_LIST_SERVER = "http://didx.net/cgi-bin/WebGetListServer.cgi";
//	protected static final String DIDX_EP_WEB_GET_DID_COUNTRIES_SERVER = "http://didx.net/cgi-bin/WebGetDIDCountriesServer.cgi";
//	protected static final String DIDX_EP_WEB_GET_DID_AREAS_SERVER = "http://didx.net/cgi-bin/WebGetDIDAreasServer.cgi";
//	protected static final String DIDX_EP_WEB_GET_DID_FREE_MINS = "http://didx.net/cgi-bin/WebGetDIDSMinutes.cgi";
//	protected static final String DIDX_EP_WEB_GET_DID_INFO = "http://didx.net/cgi-bin/WebGetDIDInfo.cgi";
//	protected static final String DIDX_EP_WEB_BUY_DID = "http://didx.net/cgi-bin/WebBuyDIDServer.cgi";
//	protected static final String DIDX_EP_WEB_RELEASE_DID = "http://www.didx.net/cgi-bin/WebReleaseDIDServer.cgi";
//	protected static final String DIDX_EP_WEB_UPDATE_SIP_ADDRESS = "http://www.didx.net/cgi-bin/WebEditURLServer.cgi";
	

	
	/** DIDX.net Method Return Signature (used to check method return results) 	*/
	protected static final String[] FIELD_DESC_GET_AVAILABLE_DIDS = new String[]{"DIDNumber","OurSetupCost","OurMonthlyCharges",
																					   "OurPerMinuteCharges","VendorRating","Country","Area"};
	protected static final String[] FIELD_DESC_GET_DID_COUNTRY = new String[]{"CountryCode","Description","CountryID"};
	protected static final String[] FIELD_DESC_GET_DID_AREA = new String[]{"AreaCode","Description"};
	protected static final String[] FIELD_DESC_GET_DID_FREE_MIN_INFO = new String[]{"DIDNumber","FreeMin","OurPerMinuteCharges","iChannel"};
	protected static final String[] FIELD_DESC_GET_DID_INFO = new String[]{"DIDNumber", "FreeMin", "OurPerMinuteCharges", "iChannel", "CheckStatus", 
																				"OID", "OurSetupCost", "OurMonthlyCharges", "AreaID", "Status", "AlreadyApproved", 
																				"DocumentRequires", "MsgFromVendor", "DocumentType", "VendorRating", "iCallCard", 
																				"iSoldCC", "iCodec", "iNetwork", "iTrigger"};
	
	/** DIDX.net Method Error Codes and Descriptions							*/
	protected static final HashMap<String, String> ERROR_MAP_GET_AVAILABLE_DIDS = new HashMap<String, String>() 
	{
		{
			put("-1", "User ID does not exist");
			put("-2", "Your Password is incorrect");
			put("-3", "There are no DID's for this area in our record");
			put("-4", "The Area code does not exist");
			put("-5", "There are no DID's for this country in our record");
			put("-6", "The Country Code does not exist");
		}
	};
	protected static final HashMap<String, String> ERROR_MAP_GET_DID_COUNTRY = new HashMap<String, String>() 
	{
		{
			put("-1", "User ID does not exist");
			put("-2", "Your Password is incorrect");
			put("-3", "There are no rated DIDs' country available in our record");
		}
	};
	protected static final HashMap<String, String> ERROR_MAP_GET_DID_AREA = new HashMap<String, String>() 
	{
		{
			put("-1", "User ID does not exist");
			put("-2", "Your Password is incorrect");
			put("-3", "There are no rated DIDs' Areas available in our record");
		}
	};
	protected static final HashMap<String, String> ERROR_MAP_GET_DID_FREE_MIN_INFO = new HashMap<String, String>() 
	{
		{
			put("-1", "User ID does not exist");
			put("-2", "Your Password is incorrect");
			put("-3", "There are no DID in our Database");
		}
	};
	protected static final HashMap<String, String> ERROR_MAP_BUY_DID = new HashMap<String, String>() 
	{
		{
			put("-1", "User ID does not exist");
			put("-2", "Your Password is incorrect");
			put("-3", "This DID Number is already sold");
			put("-4", "This DID Number is already reserved");
			put("-5", "DID Number doesn't exist");
			put("-6", "The Country Code doesn't exist");
			put("-20", "Account not active");
			put("-21", "Undocumented error"); // not documented but is returned 
			put("-22", "Due not cleared");
			put("-23", "Customer documents required");
			put("-24", "This DID is reserved for another customer");
			put("-30", "Invalid account type to buy DID");
			put("-31", "Rating of this DID is less than the one you have been allowed");
		}
	};
	protected static final HashMap<String, String> ERROR_MAP_RELEASE_DID = new HashMap<String, String>() 
	{
		{
			put("-1", "User ID does not exist");
			put("-2", "Your Password is incorrect");
			put("-3", "The DID is not in your ownership");
			put("-4", "No such DID Number exists");
		}
	};
	protected static final HashMap<String, String> ERROR_MAP_GET_DID_INFO = new HashMap<String, String>() 
	{
		{
			put("-2", "Invalid User ID or Password");
			put("-3", "No matching DID in database");
		}
	};
	protected static final HashMap<String, String> ERROR_MAP_UPDATE_SIP_ADDRESS = new HashMap<String, String>() 
	{
		{
			put("-1", "User ID does not exist");
			put("-2", "Your Password is incorrect");
			put("-3", "The DID is not in your ownership");
			put("-4", "No such DID Number exists");
			put("-5", "Please provide a valid value for the fifth parameter");
		}
	};
	
	// countryId, description
	protected static final HashMap<String, String> DIDX_COUNTRY_LIST = new HashMap<String, String>()
	{
		{
			put("9", "Argentina");
			put("13", "Australia");
			put("223", "Austria");
			put("17", "Bahrain");
			put("21", "Belgium");
			put("29", "Brazil");
			put("32", "Bulgaria");
			put("37", "Canada");
			put("42", "Chile");
			put("43", "China");
			put("46", "Colombia");
			put("53", "Cyprus");
			put("54", "Czech Republic");
			put("56", "Denmark");
			put("224", "Dominican Republic");
			put("68", "Finland");
			put("69", "France");
			put("74", "Georgia");
			put("75", "Germany");
			put("78", "Greece");
			put("83", "Guatemala");
			put("89", "Honduras");
			put("90", "Hong Kong");
			put("99", "Iran");
			put("101", "Ireland");
			put("102", "Israel");
			put("103", "Italy");
			put("104", "Jamaica");
			put("105", "Japan");
			put("114", "Latvia");
			put("121", "Luxembourg");
			put("126", "Malaysia");
			put("134", "Mexico");
			put("144", "Netherlands");
			put("147", "New Zealand");
			put("153", "Norway");
			put("155", "Pakistan");
			put("157", "Panama");
			put("160", "Peru");
			put("162", "Poland");
			put("167", "Romania");
			put("168", "Russia");
			put("181", "Singapore");
			put("185", "South Africa");
			put("110", "South Korea");
			put("186", "Spain");
			put("191", "Sweden");
			put("192", "Switzerland");
			put("198", "Thailand");
			put("203", "Turkey");
			put("208", "Ukraine");
			put("210", "United Kingdom");
			put("211", "USA");
			put("215", "Venezuela");

		}
	};
}
