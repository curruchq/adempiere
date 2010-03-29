package org.compiere.wstore;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;

import org.apache.axis.AxisFault;
import org.apache.axis.client.Call;
import org.compiere.Adempiere;
import org.compiere.model.MConversionRate;
import org.compiere.model.MDIDxAccount;
import org.compiere.model.MDIDxCountry;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.compiere.util.Trx;

import com.conversant.model.DID;
import com.conversant.model.DIDAreaCode;
import com.conversant.model.DIDCountry;
import com.conversant.model.DIDInfo;

/**
 *  DIDX.net Service
 *
 *  @author Josh Hill
 *  @version  $Id: DIDXService.java,v 1.0 2008/03/13 14:53:21 jhill Exp $
 */
public class DIDXService
{
	/** Logger 																	*/
	protected static CLogger log = CLogger.getCLogger(DIDXService.class);

	private DIDXService(){}
	
	/**
	 * Get available DIDs
	 * <br>
	 * @param ctx
	 * @param countryCode
	 * @param countryId
	 * @param areaCode
	 * @param countryDescription
	 * @return DIDCountry
	 */
	public static DIDCountry getAvailableDIDS(Properties ctx, String countryCode, String countryId, String areaCode, String countryDescription)
	{
		if (countryCode == null)
			countryCode = "";
		if (countryId == null)
			countryId = "";
		if (areaCode == null || areaCode.length() < 1)
			areaCode = "-1";
		if (countryDescription == null)
			countryDescription = "";
		
		log.fine("Getting available DIDs from DIDx.net: CountryCode=" + countryCode + ", CountryId=" + countryId + 
				 ", AreaCode=" + areaCode + ", Description=" + countryDescription);
		
		Object[] vendorRatings = getVendorRatings(ctx);		
		Object[] params = concat(getCredentials(ctx), new Object[]{countryCode, areaCode, "", vendorRatings[0], 
			vendorRatings[1], countryId});		
  		Object result = invokeSOAPCall(DIDXConstants.P_WEB_GET_LIST_SERVER,
  			DIDXConstants.URN_GET_AVAILABLE_DIDS, DIDXConstants.M_GET_AVAILABLE_DIDS, params);
  		
		DIDCountry country = new DIDCountry();
		country.setCountryCode(countryCode);
		country.setCountryId(countryId);
		country.setDescription(countryDescription);
		country.addAreaCode(areaCode, "");
		DIDAreaCode oAreaCode = country.getAreaCode(areaCode);
  		
  		if (!isErrorCode(DIDXConstants.DIDXMethod.GET_AVAILABLE_DIDS, result))
		{
  			try
  			{
	  			Object fieldDescriptions = ((Object[])result)[0];
				if (validateFieldDescriptions(DIDXConstants.DIDXMethod.GET_AVAILABLE_DIDS, fieldDescriptions))
				{
					Object[] data = (Object[])result;
					for (int i=1;i<data.length;i++)
					{
						try
						{
							Object[] row = (Object[])data[i];
								
							String didNumber = parseField(row[0]);
							String setupCost = parseField(row[1]);
							String monthlyCharges = parseField(row[2]);
							String perMinCharges = parseField(row[3]);
							String vendorRating = parseField(row[4]);
							String countryName = parseField(row[5]);
							String description = row[6] instanceof String ? parseField(row[6]) : ""; // Only allow string descriptions
							
							// Convert prices from USD to NZD
							BigDecimal setupCostNZD = MConversionRate.convert(ctx,  new BigDecimal(setupCost), DIDConstants.USD_CURRENCY_ID, DIDConstants.NZD_CURRENCY_ID, null, DIDConstants.CUSTOM_CONV_TYPE_ID, Env.getAD_Client_ID(ctx), Env.getAD_Org_ID(ctx));
							BigDecimal monthlyChargesNZD = MConversionRate.convert(ctx, new BigDecimal(monthlyCharges), DIDConstants.USD_CURRENCY_ID, DIDConstants.NZD_CURRENCY_ID, null, DIDConstants.CUSTOM_CONV_TYPE_ID, Env.getAD_Client_ID(ctx), Env.getAD_Org_ID(ctx));
							BigDecimal perMinChargesNZD = MConversionRate.convert(ctx, new BigDecimal(perMinCharges), DIDConstants.USD_CURRENCY_ID, DIDConstants.NZD_CURRENCY_ID, null, DIDConstants.CUSTOM_CONV_TYPE_ID, Env.getAD_Client_ID(ctx), Env.getAD_Org_ID(ctx));
							
							// If rate not found use fall back constant
							if (setupCostNZD == null || monthlyChargesNZD == null || perMinChargesNZD == null) 
							{
								setupCostNZD = new BigDecimal(setupCost).multiply(new BigDecimal(DIDConstants.FALLBACK_USD_TO_NZD_RATE));
								monthlyChargesNZD = new BigDecimal(monthlyCharges).multiply(new BigDecimal(DIDConstants.FALLBACK_USD_TO_NZD_RATE));
								perMinChargesNZD = new BigDecimal(perMinCharges).multiply((new BigDecimal(DIDConstants.FALLBACK_USD_TO_NZD_RATE)));
								log.severe("Could not find a rate in CurrencyRate table, using fallback USD to NZD rate of " + DIDConstants.FALLBACK_USD_TO_NZD_RATE + "NZD = 1USD");
							}
							
							oAreaCode.addDID(new DID(didNumber, setupCostNZD.toString(), monthlyChargesNZD.toString(), perMinChargesNZD.toString(), vendorRating, countryName, description));
						}
						catch (ClassCastException ex)
						{
							// continue in loop
						}
					}
					
					oAreaCode.sortDIDsByNumber(true);
				}
  			}
  			catch (ClassCastException ex)
  			{
  				log.log(Level.SEVERE, "DIDx.net getAvailableDIDS did not return an Object[]", ex);
  			}
		}
  		
  		return country;
	}	// getAvailableDIDS
	
	/**
	 * Get available DIDs
	 * <br>
	 * @param ctx
	 * @param countryCode
	 * @param areaCode
	 * @param vendorId
	 * @param vendorRatingFrom
	 * @param vendorRatingTo
	 * @param countryId
	 * @param countryDescription
	 * @return DIDCountry
	 *
	public static DIDCountry getAvailableDIDS(Properties ctx, String countryCode, String areaCode, 
			String vendorId, String vendorRatingFrom, String vendorRatingTo,
			String countryId, String countryDescription)
	{
		// process params
		if (countryCode == null)
			countryCode = "";
		if (areaCode == null || countryCode.equals(""))
			areaCode = "-1";
		if (vendorId == null)
			vendorId = "";
		if (vendorRatingFrom == null)
			vendorRatingFrom = DIDConstants.DIDX_MIN_VENDOR_RATING;
		if (vendorRatingTo == null)
			vendorRatingTo = DIDConstants.DIDX_MAX_VENDOR_RATING;
		if (countryId == null)
			countryId = "";
		if (countryDescription == null)
			countryDescription = "";
		
		log.fine("Getting available DIDs from DIDx.net, CountryCode=" + countryCode + ", AreaCode=" + areaCode + ", CountryId=" + countryId);

		Object[] params = concat(getCredentials(), new Object[]
		{ countryCode, areaCode, vendorId, vendorRatingFrom, vendorRatingTo,
				countryId });
		Object result = invokeSOAPCall(DIDConstants.DIDX_EP_WEB_GET_LIST_SERVER,
				DIDConstants.DIDX_GET_LIST_URI, DIDConstants.DIDX_METHOD_GET_AVAILABLE_DIDS, params);

		// set up return obj
		DIDCountry country = new DIDCountry();
		country.setCountryCode(countryCode);
		country.setCountryId(countryId);
		country.setDescription(countryDescription);
		country.addAreaCode(areaCode, "");
		DIDAreaCode oAreaCode = country.getAreaCode(areaCode);
		
		if (!isErrorCode(DIDConstants.DIDXMethod.GET_AVAILABLE_DIDS, result) && result instanceof ArrayList)
		{
			ArrayList listOfArrayLists = (ArrayList)result;
			
			// validate the field descriptions (hardcoded check to make sure DIDX.net API hasn't changed)
			if (validateFieldDescriptions(DIDConstants.DIDXMethod.GET_AVAILABLE_DIDS, listOfArrayLists.remove(0)))
			{
				// loop through all countries
				for (Object child : listOfArrayLists)
				{
					try
					{
						Object[] fields = (Object[])child;
						
						String didNumber = parseField(fields[0]);
						String setupCost = parseField(fields[1]);
						String monthlyCharges = parseField(fields[2]);
						String perMinCharges = parseField(fields[3]);
						String vendorRating = parseField(fields[4]);
						String countryName = parseField(fields[5]);
						String description = fields[6] instanceof String ? parseField(fields[6]) : ""; // Only allow string descriptions
						
						// Convert prices from USD to NZD
						BigDecimal setupCostNZD = MConversionRate.convert(ctx,  new BigDecimal(setupCost), DIDConstants.USD_CURRENCY_ID, DIDConstants.NZD_CURRENCY_ID, null, DIDConstants.CUSTOM_CONV_TYPE_ID, Env.getAD_Client_ID(ctx), Env.getAD_Org_ID(ctx));
						BigDecimal monthlyChargesNZD = MConversionRate.convert(ctx, new BigDecimal(monthlyCharges), DIDConstants.USD_CURRENCY_ID, DIDConstants.NZD_CURRENCY_ID, null, DIDConstants.CUSTOM_CONV_TYPE_ID, Env.getAD_Client_ID(ctx), Env.getAD_Org_ID(ctx));
						BigDecimal perMinChargesNZD = MConversionRate.convert(ctx, new BigDecimal(perMinCharges), DIDConstants.USD_CURRENCY_ID, DIDConstants.NZD_CURRENCY_ID, null, DIDConstants.CUSTOM_CONV_TYPE_ID, Env.getAD_Client_ID(ctx), Env.getAD_Org_ID(ctx));
						
						// If rate not found use fall back constant
						if (setupCostNZD == null || monthlyChargesNZD == null || perMinChargesNZD == null) 
						{
							setupCostNZD = new BigDecimal(setupCost).multiply(new BigDecimal(DIDConstants.FALLBACK_USD_TO_NZD_RATE));
							monthlyChargesNZD = new BigDecimal(monthlyCharges).multiply(new BigDecimal(DIDConstants.FALLBACK_USD_TO_NZD_RATE));
							perMinChargesNZD = new BigDecimal(perMinCharges).multiply((new BigDecimal(DIDConstants.FALLBACK_USD_TO_NZD_RATE)));
							log.severe("Could not find a rate in CurrencyRate table, using fallback USD to NZD rate of " + DIDConstants.FALLBACK_USD_TO_NZD_RATE + "NZD = 1USD");
						}
						
						oAreaCode.addDID(new DID(didNumber, setupCostNZD.toString(), monthlyChargesNZD.toString(), perMinChargesNZD.toString(), vendorRating, countryName, description));
					}
					catch (ClassCastException ex)
					{
						// do nothing (continue in loop)
					}
				}

				oAreaCode.sortDIDsByNumber(true);
			}
		}
		else
		{
			log.fine("DIDXService.getAvailableDIDS(): DIDX.net did not return an ArrayList");
		}

		return country;
	}	// getAvailableDIDS
	*/
	
	/**
	 * Get list of countries
	 * <br>
	 * @return ArrayList of countries
	 */
	public static ArrayList<DIDCountry> getCountries(Properties ctx)
	{
		log.fine("Getting list of countries from DIDx.net ...");
		
		// set up arraylist to return
		ArrayList<DIDCountry> allCountries = new ArrayList<DIDCountry>();
		
		Object[] params = concat(getCredentials(ctx), new Object[]{Integer.toString(DIDXConstants.MIN_VENDOR_RATING_RANGE), 
			Integer.toString(DIDXConstants.MAX_VENDOR_RATING_RANGE)});		
  		Object result = invokeSOAPCall(DIDXConstants.P_WEB_GET_DID_COUNTRIES_SERVER,
  				DIDXConstants.URN_GET_DID_COUNTRY, DIDXConstants.M_GET_DID_COUNTRY, params);
  		
  		if (!isErrorCode(DIDXConstants.DIDXMethod.GET_DID_COUNTRY, result))
		{
			try
			{
				Object fieldDescriptions = ((Object[])result)[0];
				if (validateFieldDescriptions(DIDXConstants.DIDXMethod.GET_DID_COUNTRY, fieldDescriptions))
				{					
					Object[] data = (Object[])result;
					for (int i=1;i<data.length;i++)
					{
						try
						{
							Object[] row = (Object[])data[i];
						
							String countryCode = parseField(row[0]);
							String description = parseField(row[1]);						
							String countryId = parseField(row[2]);
							
							// filter out empty first row
							if (!countryCode.equals("") || !description.equals("") || !countryId.equals(""))
								allCountries.add(new DIDCountry(description, countryCode, countryId));
						}
						catch (ClassCastException ex)
						{
							// continue in loop
						}
					}
				}
			}
			catch (ClassCastException ex)
			{
				log.log(Level.SEVERE, "DIDx.net getDIDCountries did not return an Object[]", ex);
			}
		}
  		
  		return allCountries;
	}	// getDIDCountries

	/**
	 * Get DID Countries
	 * <br>
	 * @param vendorRatingFrom
	 * @param vendorRatingTo
	 * @return ArrayList of DIDCountry's
	 *
	public static ArrayList<DIDCountry> getDIDCountries(String vendorRatingFrom,
			String vendorRatingTo)
	{
		// process params
		if (vendorRatingFrom == null)
			vendorRatingFrom = "";
		if (vendorRatingTo == null)
			vendorRatingTo = "";
		
		log.fine("Getting DID Countries from DIDx.net");

		// set up arraylist to return
		ArrayList<DIDCountry> allCountries = new ArrayList<DIDCountry>();
		
		Object[] params = concat(getCredentials(), new Object[]
		{ vendorRatingFrom, vendorRatingTo });
		Object result = invokeSOAPCall(
				DIDConstants.DIDX_EP_WEB_GET_DID_COUNTRIES_SERVER, DIDConstants.DIDX_GET_LIST_URI,
				DIDConstants.DIDX_METHOD_GET_DID_COUNTRY, params);

		if (!isErrorCode(DIDConstants.DIDXMethod.GET_DID_COUNTRY, result) && result instanceof ArrayList)
		{
			ArrayList listOfArrayLists = (ArrayList)result;
			
			// validate the field descriptions (hardcoded check to make sure DIDX.net API hasn't changed)
			if (validateFieldDescriptions(DIDConstants.DIDXMethod.GET_DID_COUNTRY, listOfArrayLists.remove(0)))
			{
				// loop through all countries
				for (Object child : listOfArrayLists)
				{
					try
					{
						Object[] fields = (Object[])child;
						
						String code = parseField(fields[0]);
						String desc = parseField(fields[1]);						
						String id = parseField(fields[2]);
						
						// filter out empty first row
						if (!desc.equals("") || !code.equals("") || !id.equals(""))
							allCountries.add(new DIDCountry(desc, code, id));
					}
					catch (ClassCastException ex)
					{
						// do nothing (continue in loop)
					}
				}
			}
		}
		else
		{
			log.fine("DIDXService.getDIDCountries(): DIDX.net did not return an ArrayList");
		}

		return allCountries;
	}	// getDIDCountries
	*/
	
	/**
	 * Get DID Areas
	 * <br>
	 * @param countryDescription
	 * @param countryCode
	 * @param countryId
	 * @return DIDCountry
	 */
	public static DIDCountry getDIDAreas(Properties ctx, String countryDescription, String countryCode, String countryId)
	{
		return getDIDAreas(ctx, countryDescription, countryCode, null, null, null, null, null, countryId);
	}	// getDIDAreas

	/**
	 * Get DID Areas
	 * <br>
	 * @param countryDescription
	 * @param countryCode
	 * @param vendorRatingFrom
	 * @param vendorRatingTo
	 * @param vendor
	 * @param monthlyFrom
	 * @param monthlyTo
	 * @param countryId
	 * @return DIDCountry
	 */
	public static DIDCountry getDIDAreas(Properties ctx, String countryDescription, String countryCode, String vendorRatingFrom,
			String vendorRatingTo, String vendor, String monthlyFrom,
			String monthlyTo, String countryId)
	{
		Object[] vendorRatings = getVendorRatings(ctx);	
		
		// process params
		if (countryCode == null)
			countryCode = "";
		if (vendorRatingFrom == null)
			vendorRatingFrom = (String)vendorRatings[0];
		if (vendorRatingTo == null)
			vendorRatingTo = (String)vendorRatings[1];
		if (vendor == null)
			vendor = "";
		if (monthlyFrom == null)
			monthlyFrom = "";
		if (monthlyTo == null)
			monthlyTo = "";
		if (countryId == null)
			countryId = "";

		log.fine("Getting DID Areas from DIDx.net, CountryCode=" + countryCode + ", CountryId=" + countryId + ", Description=" + countryDescription);
		
		Object[] params = concat(getCredentials(ctx), new Object[]
		{ countryCode, vendorRatingFrom, vendorRatingTo, vendor, monthlyFrom, monthlyTo, countryId });

		Object result = invokeSOAPCall(DIDXConstants.P_WEB_GET_DID_AREAS_SERVER,
				DIDXConstants.URN_GET_DID_AREA, DIDXConstants.M_GET_DID_AREA, params);
		
		// set up return object
		DIDCountry country = new DIDCountry(countryDescription, countryCode, countryId);
		
		if (!isErrorCode(DIDXConstants.DIDXMethod.GET_DID_AREA, result))
		{
			try
			{
				Object fieldDescriptions = ((Object[])result)[0];
				if (validateFieldDescriptions(DIDXConstants.DIDXMethod.GET_DID_AREA, fieldDescriptions))
				{					
					Object[] data = (Object[])result;
					for (int i=1;i<data.length;i++)
					{
						try
						{
							Object[] row = (Object[])data[i];
						
							String areaCode = parseField(row[0]);
							String areaCodeDescription = parseField(row[1]);						
							
							country.addAreaCode(areaCode, areaCodeDescription);
						}
						catch (ClassCastException ex)
						{
							// continue in loop
						}
					}
				}
			}
			catch (ClassCastException ex)
			{
				log.log(Level.SEVERE, "DIDx.net getDIDArea did not return an Object[]", ex);
			}
		}

		return country;
	}	// getDIDAreas
	
	/**
	 * Get DID Free Min Info
	 * <br>
	 * @param didNumber
	 * @return
	 */
	public static String getDIDFreeMinInfo(Properties ctx, String didNumber)
	{
		log.fine("Getting DID Free Min Info from DIDx.net, DIDNumber=" + didNumber);
		
		String defaultReturn = "0";
		
		if (didNumber == null || didNumber.equals(""))
			return defaultReturn;
		
		
		Object[] params = concat(getCredentials(ctx), new Object[]{didNumber});
		Object result = invokeSOAPCall(DIDXConstants.P_WEB_GET_DIDS_MINUTES, 
				DIDXConstants.URN_GET_DID_MINUTES_INFO, 
				DIDXConstants.M_GET_DID_MINUTES_INFO, params);

		if (!isErrorCode(DIDXConstants.DIDXMethod.GET_DID_MINUTES_INFO, result))
		{
			try
			{
				Object fieldDescriptions = ((Object[])result)[0];
				if (validateFieldDescriptions(DIDXConstants.DIDXMethod.GET_DID_MINUTES_INFO, fieldDescriptions))
				{
					Object[] data = (Object[])result;
					for (int i=1;i<data.length;i++)
					{
						try
						{
							Object[] row = (Object[])data[i];
						
//							String didNumber = parseField(row[0]);
							String freeMin = parseField(row[1]);
//							String OurPerMinuteCharges = parseField(row[2]);
//							String iChannel = parseField(row[3]);						
							
							return freeMin.equals("") ? defaultReturn : freeMin; 
						}
						catch (ClassCastException ex)
						{
							// continue in loop
						}
					}
				}
			}
			catch (ClassCastException ex)
			{
				log.log(Level.SEVERE, "DIDx.net getDIDMinutesInfo did not return an Object[]", ex);
			}
		}
		
		return defaultReturn;
	}	// getDIDFreeMinInfo
	
	/**
	 * 
	 * @param didNumber
	 * @return
	 */
	public static DIDInfo getDIDInfo(Properties ctx, String didNumber)
	{
		log.fine("Getting DID info from DIDx.net, DIDNumber=" + didNumber);
		
		DIDInfo info = null;
		
		if (didNumber == null || didNumber.equals(""))
			return info;
		
		Object[] params = concat(getCredentials(ctx), new Object[]{didNumber});
		Object result = invokeSOAPCall(DIDXConstants.P_WEB_GET_DID_INFO, 
				DIDXConstants.URN_GET_DID_INFO, 
				DIDXConstants.M_GET_DID_INFO, params);
		
		if (!isErrorCode(DIDXConstants.DIDXMethod.GET_DID_INFO, result))
		{
			try
			{
				Object fieldDescriptions = ((Object[])result)[0];
				if (validateFieldDescriptions(DIDXConstants.DIDXMethod.GET_DID_INFO, fieldDescriptions))
				{
					try
					{
						Object[] data = (Object[])result;
						Object[] row = (Object[])data[1]; // row 0 are the field descriptions
						info = DIDInfo.get(row);
					}
					catch (ClassCastException ex)
					{
						log.warning("Could not cast returned value to Object[]");
					}
					
				}
			}
			catch (ClassCastException ex)
			{
				log.log(Level.SEVERE, "DIDx.net getDIDInfo did not return an Object[]", ex);
			}
		}
		
		return info;
	}	// getDIDInfo
	
	public static boolean isDIDAvailable(Properties ctx, String didNumber)
	{
		return getDIDStatus(ctx, didNumber).equals(DIDXConstants.DID_STATUS.AVAILABLE);
	}
	
	private static DIDXConstants.DID_STATUS getDIDStatus(Properties ctx, String didNumber)
	{
		DIDInfo didInfo = getDIDInfo(ctx, didNumber);
		
		if (didInfo != null)
		{
			switch (didInfo.getStatus())
			{
				case 0 : 
					return DIDXConstants.DID_STATUS.AVAILABLE;
				case 1 :
					return DIDXConstants.DID_STATUS.RESERVED;
				case 2 : 
					return DIDXConstants.DID_STATUS.SOLD;
				default :
					log.severe("Value returned from DIDX.net for DID status does match existing values, please check if these have changed and modify code to address the issue. (Status has been set to SOLD)");
					return DIDXConstants.DID_STATUS.SOLD;
			}
		}
		else
			log.severe("DIDInfo object was not created, debug at once!");
		
		return DIDXConstants.DID_STATUS.SOLD;
	}
	
	/**
	 * Buy a SIP DID Number
	 * <br>
	 * @param didNumber
	 * @return true if bought
	 */
	public static boolean buyDID(Properties ctx, String didNumber)
	{
		return buyDID(ctx, didNumber, null);
	}	// buySIPDID
	
	/**
	 * Buy a SIP DID Number
	 * <br>
	 * @param didNumber
	 * @param vendorId
	 * @return true if bought
	 */
	public static boolean buyDID(Properties ctx, String didNumber, String vendorId)
	{
		log.fine("Buying SIP DID from DIDx.net, DIDNumber=" + didNumber);
		
		if (vendorId == null || vendorId.equals(""))
			vendorId = "-1";
		
		if (didNumber == null || didNumber.equals(""))
		{
			log.warning("Buy unsuccessful, " + didNumber == null ? "DIDNumber is null" : "DIDNumber is empty");
			return false;
		}

		String SIPAddress = didNumber + "@" + DIDConstants.DEFAULT_SIP_DOMAIN;
		
		Object[] params = concat(getCredentials(ctx), new Object[]{didNumber, SIPAddress, DIDXConstants.SIP_FLAG, vendorId});
		Object result = invokeSOAPCall(DIDXConstants.P_WEB_BUY_DID_SERVER, 
				DIDXConstants.URN_BUY_DID_BY_NUMBER, 
				DIDXConstants.M_BUY_DID_BY_NUMBER, params);

		if (!isErrorCode(DIDXConstants.DIDXMethod.BUY_DID, result) && result instanceof String)
		{
			if (didNumber.compareTo((String)result) == 0)
			{
				return true;
			}
			else
			{
				log.severe("Result is not an error code and does not match the DID number, debug at once!");
			}
//			return Long.decode(didNumber).compareTo((Long)result) == 0 ? true : false;
		}
		
		return false;
	}	// buySIPDID
	
	/**
	 * Release a DID Number (within 36 hours of purchase free of charge)
	 * <br>
	 * @param didNumber
	 * @return true if released
	 */
	public static boolean releaseDID(Properties ctx, String didNumber)
	{
		log.fine("Releasing DID from DIDx.net, DIDNumber=" + didNumber);
		
		if (didNumber == null || didNumber.equals(""))
		{
			log.warning("Release unsuccessful, " + didNumber == null ? "DIDNumber is null" : "DIDNumber is empty");
			return false;
		}
		
		Object[] params = concat(getCredentials(ctx), new Object[]{didNumber});
		Object result = invokeSOAPCall(DIDXConstants.P_WEB_RELEASE_DID_SERVER, DIDXConstants.URN_RELEASE_DID, 
				DIDXConstants.M_RELEASE_DID, params);

		if (!isErrorCode(DIDXConstants.DIDXMethod.RELEASE_DID, result) && result instanceof String)
		{
			if (didNumber.compareTo((String)result) == 0)
			{
				return true;
			}
			else
			{
				log.severe("Result is not an error code and does not match the DID number, debug at once!");
			}
//			return Long.decode(DIDNumber).compareTo((Long)result) == 0 ? true : false;
		}
		
		return false;
	}	// releaseDID
	
	public static boolean updateSIPAddress(Properties ctx, String didNumber, String address)
	{
		log.fine("Updating SIP Address, DIDNumber=" + didNumber);
		
		if (didNumber == null || didNumber.length() < 1)
		{
			log.warning("Update unsuccessful, " + didNumber == null ? "DIDNumber is null" : "DIDNumber is empty");
			return false;
		}
		
		Object[] params = concat(getCredentials(ctx), new Object[]{didNumber, address, DIDXConstants.SIP_FLAG});
		Object result = invokeSOAPCall(DIDXConstants.P_WEB_EDIT_URL_SERVER, DIDXConstants.URN_EDIT_URL, 
				DIDXConstants.M_EDIT_URL, params);

		if (!isErrorCode(DIDXConstants.DIDXMethod.UPDATE_SIP_ADDRESS, result) && result instanceof String)
		{
			if (didNumber.compareTo((String)result) == 0)
			{
				return true;
			}
			else
			{
				log.severe("Result is not an error code and does not match the DID number, debug at once!");
			}
//			return Long.decode(didNumber).compareTo((Long)result) == 0 ? true : false;
		}
		
		return false;
	}

	/**
	 * Invoke soap call
	 * <br>
	 * @param endPoint
	 * @param namespaceURI
	 * @param methodName
	 * @param params
	 * @return Object returned from SOAP call, null if unsuccessful
	 */
	private static Object invokeSOAPCall(String endPoint, String namespaceURI,
			String methodName, Object[] params)
	{
		Call call = null;

		try
		{
			call = new Call(endPoint);
			Object o = call.invoke(namespaceURI, methodName, params);
			return o;
		}
		catch (MalformedURLException muEx)
		{
			log.log(Level.SEVERE, "DIDXService.invokeSOAPCall() raised MalformedURLException", muEx);
		}
		catch (AxisFault afEx)
		{
			log.log(Level.SEVERE, "DIDXService.invokeSOAPCall() raised AxisFault", afEx);
		}

		return null;
	}	// invokeSOAPCall

	

	public static MDIDxCountry getDIDxCountry(Properties ctx, int countryId)
	{
		ArrayList<MDIDxCountry> countries = MDIDxCountry.getCountries(ctx);
		for (MDIDxCountry country : countries)
		{
			if (country.getDIDX_COUNTRYID() == countryId)
				return country;
		}
		
		return null;
	}
	
	private static Object[] getCredentials(Properties ctx)
	{
		String userId = "";
		String password = "";
		
		MDIDxAccount acct = MDIDxAccount.getBySearchKey(ctx, null, null);
		if (acct != null)
		{
			userId = acct.getDIDX_USERID();
			password = acct.getDIDX_PASSWORD();
		}
		else
			log.severe("Failed to load DIDx Account");
		
		return new Object[]{userId, password};
	}	// getCredentials
	
	private static Object[] getVendorRatings(Properties ctx)
	{
		String min = DIDXConstants.DEFAULT_MIN_VENDOR_RATING;
		String max = DIDXConstants.DEFAULT_MAX_VENDOR_RATING;
		
		MDIDxAccount acct = MDIDxAccount.getBySearchKey(ctx, null, null);
		if (acct != null)
		{
			if (acct.getMIN_VENDOR_RATING() >= DIDXConstants.MIN_VENDOR_RATING_RANGE)
				min = Integer.toString(acct.getMIN_VENDOR_RATING());
			
			if (acct.getMAX_VENDOR_RATING() <= DIDXConstants.MAX_VENDOR_RATING_RANGE)
				max = Integer.toString(acct.getMAX_VENDOR_RATING());
		}
		else
			log.severe("Failed to load DIDx Account");
		
		return new Object[]{min, max};
	}	// getVendorRatings
	
	/**
	 * Concatenate two arrays
	 * <br>
	 * @param A array one
	 * @param B array two
	 * @return single array created from concatenating A and B
	 */
	private static Object[] concat(Object[] A, Object[] B)
	{
		Object[] C = new Object[A.length + B.length];
		System.arraycopy(A, 0, C, 0, A.length);
		System.arraycopy(B, 0, C, A.length, B.length);
		return C;
	}	// concat
	
	/**
	 * Parse field from Object to String
	 * <br>
	 * @param field Object to parse
	 * @return String value of field, empty if couldn't parse
	 */
	protected static String parseField(Object field)
	{
		if (field == null)
		{
			return "";
		}
		else if (field instanceof String)
		{
			return (String)field;
		}
		else if (field instanceof Integer)
		{
			return Integer.toString((Integer)field);
		}
		else if (field instanceof Float)
		{
			return Float.toString((Float)field);
		}
		else if (field instanceof Long)
		{
			return Long.toString((Long)field);
		}
		else if (field instanceof Double)
		{
			return Double.toString((Double)field);
		}
		else if (field instanceof Short)
		{
			return Short.toString((Short)field);
		}
		else if (field instanceof Timestamp)
		{
			return ((Timestamp)field).toString();
		}
		else if (field instanceof Boolean)
		{
			return Boolean.toString((Boolean)field);
		}
		else if (field instanceof Object[])
		{
			return Arrays.toString((Object[])field);
		}
		// If not instance of Object[] and isArray=true must be primitive array
		else if (field.getClass().isArray())
		{
			Object firstElement = Array.get(field, 0);
			if (firstElement != null)
			{
				StringBuilder sb = new StringBuilder("");
				if (firstElement instanceof Integer)
				{
					for (int i=0; i < Array.getLength(field); i++)
					{
						if (i > 0) sb.append(",");
						sb.append(Array.getInt(field, i));
					}
				}
				else if (firstElement instanceof Double)
				{
					for (int i=0; i < Array.getLength(field); i++)
					{
						if (i > 0) sb.append(",");
						sb.append(Array.getDouble(field, i));
					}
				}
				else if (firstElement instanceof Float)
				{
					for (int i=0; i < Array.getLength(field); i++)
					{
						if (i > 0) sb.append(",");
						sb.append(Array.getFloat(field, i));
					}
				}
				else if (firstElement instanceof Long)
				{
					for (int i=0; i < Array.getLength(field); i++)
					{
						if (i > 0) sb.append(",");
						sb.append(Array.getLong(field, i));
					}
				}
				else if (firstElement instanceof Short)
				{
					for (int i=0; i < Array.getLength(field); i++)
					{
						if (i > 0) sb.append(",");
						sb.append(Array.getShort(field, i));
					}
				}
				else if (firstElement instanceof Boolean)
				{
					for (int i=0; i < Array.getLength(field); i++)
					{
						if (i > 0) sb.append(",");
						sb.append(Array.getBoolean(field, i));
					}
				}
				else if (firstElement instanceof Byte)
				{
					for (int i=0; i < Array.getLength(field); i++)
					{
						if (i > 0) sb.append(",");
						sb.append(Array.getByte(field, i));
					}
				}
				else if (firstElement instanceof Character)
				{
					for (int i=0; i < Array.getLength(field); i++)
					{
						if (i > 0) sb.append(",");
						sb.append(Array.getChar(field, i));
					}
				}
				
				return sb.toString();
			}
			return "";
		}
		else
		{
			log.warning("DIDXService.parseField() could not parse field of type -> " + field.getClass() + ", please check the DIDX.net API's returned data types.");
			return "";
		}
	}	// parseField
		
	/**
	 * Checks if result is an error code, if it is log the error (level=SEVERE)
	 * <br>
	 * @param method Method which placed DIDX.net request
	 * @param result 
	 * @return true if error
	 */
	private static boolean isErrorCode(DIDXConstants.DIDXMethod method, Object result)
	{
		if (result == null)
			return true;
		
		try
		{
			String errorCode = "";
			
			if (result instanceof String)
			{
				errorCode = (String)result;
			}
			else
			{
				Object data = ((Object[])result)[0];
				errorCode = ((String[])data)[0];
			}
			
			if (errorCode.startsWith("-"))
			{
				HashMap<String, String> errorCodeDescriptions = null;
				
				switch (method)
				{
					case GET_DID_COUNTRY :
						errorCodeDescriptions = DIDXConstants.ERROR_MAP_GET_DID_COUNTRY;
						break;
					case GET_DID_AREA :
						errorCodeDescriptions = DIDXConstants.ERROR_MAP_GET_DID_AREA;
						break;
					case GET_AVAILABLE_DIDS :
						errorCodeDescriptions = DIDXConstants.ERROR_MAP_GET_AVAILABLE_DIDS;
						break;
					case GET_DID_MINUTES_INFO :
						errorCodeDescriptions = DIDXConstants.ERROR_MAP_GET_DID_FREE_MIN_INFO;
						break;
					case BUY_DID :
						errorCodeDescriptions = DIDXConstants.ERROR_MAP_BUY_DID;
						break;
					case RELEASE_DID :
						errorCodeDescriptions = DIDXConstants.ERROR_MAP_RELEASE_DID;
						break;
					case GET_DID_INFO :
						errorCodeDescriptions = DIDXConstants.ERROR_MAP_GET_DID_INFO;
						break;
					case UPDATE_SIP_ADDRESS :
						errorCodeDescriptions = DIDXConstants.ERROR_MAP_UPDATE_SIP_ADDRESS;
						break;
					default :
						log.severe("DIDXService.isErrorCode(DIDXMethod method, Object result) does not implement a case for " + method.toString() + ", this should be fixed at once.");
						return true;
				}
				
				String errorDesc = errorCodeDescriptions.get(errorCode);
				log.warning("DIDX.net returned an error -> Method=" + method.toString() + ", Code=" + errorCode + (errorDesc == null ? "" : ", Desc=" + errorDesc));
				
				return true;
			}
			else
				return false;
		}
		catch (ClassCastException ex)
		{
			
		}
		
		return true;
	}	// isErrorCode
	
	/**
	 * Validate Field Descriptions (make sure API hasn't changed)
	 * <br>
	 * @param method
	 * @param descriptions
	 * @return
	 */
	private static boolean validateFieldDescriptions(DIDXConstants.DIDXMethod method, Object descriptions)
	{
		if (descriptions instanceof String[])
		{
			String[] fieldDescriptions = (String[])descriptions;
			String[] validationFieldDescriptions = null;
			
			switch (method)
			{
				case GET_DID_COUNTRY :
					validationFieldDescriptions = DIDXConstants.FIELD_DESC_GET_DID_COUNTRY;
					break;
				case GET_DID_AREA :
					validationFieldDescriptions = DIDXConstants.FIELD_DESC_GET_DID_AREA;
					break;
				case GET_AVAILABLE_DIDS :
					validationFieldDescriptions = DIDXConstants.FIELD_DESC_GET_AVAILABLE_DIDS;
					break;
				case GET_DID_MINUTES_INFO :
					validationFieldDescriptions = DIDXConstants.FIELD_DESC_GET_DID_FREE_MIN_INFO;
					break;
				case GET_DID_INFO :
					validationFieldDescriptions = DIDXConstants.FIELD_DESC_GET_DID_INFO;
					break;
				default :
					log.log(Level.SEVERE, "DIDXService.validateFieldDescriptions(DIDXMethod method, Object descriptions) does not implement a case for " + method.toString() + ", this should be fixed at once.");
					return false;
			}
			
			for (int i = 0; i < validationFieldDescriptions.length; i++)
			{
				if (!validationFieldDescriptions[i].equalsIgnoreCase(fieldDescriptions[i]))
				{
					log.log(Level.SEVERE, "DIDXService." + method.toString() + ": Validation of the field descriptions has FAILED (Check if DIDX.net's API has changed)");
					return false;
				}
			}
			
			return true;
		}
		else
		{
			log.log(Level.SEVERE, "DIDXService." + method.toString() + " passed in an invalid header object != String[]");
		}
		return false;
	}	// validateFieldDescriptions
	
	public static void main (String[] args)
	{	
//		buySIPDID("15672446044");
//		updateSIPAddress("15672446044", "15672446044@test.com");
//		releaseDID("15672446044");
		
		Adempiere.startup(false);
		Properties ctx = Env.getCtx();
		ctx.put("#AD_Client_ID", "1000000");
		ctx.put("#AD_Org_ID", "1000001");
		String trxName = Trx.createTrxName("DIDXService-CreateDIDxCountry");
		
		MDIDxAccount acct = MDIDxAccount.getBySearchKey(ctx, null, null);
		
		ArrayList<DIDCountry> countries = getCountries(ctx);
		DIDCountry.sortCountriesByDescription(countries, true);
		boolean error = false;
		int count = 1000000;
		for (DIDCountry country : countries)
		{
//			System.out.println("put(\"" + country.getCountryId() + "\", \"" + country.getDescription() + "\");");
//			System.out.println("countries.add(new DIDCountry(\"" + country.getDescription() + "\", \"" + country.getCountryCode() + "\", \"" + country.getCountryId() + "\"));");
		
			System.out.println("countries.add(new MDIDxCountry(ctx, \"" + country.getDescription() + "\", " + country.getCountryCode() + ", " + country.getCountryId() + ", true));");
			
//			System.out.println("\nINSERT INTO MOD_DIDX_COUNTRY \n(AD_CLIENT_ID, AD_ORG_ID, ISACTIVE, CREATED, CREATEDBY, UPDATED, UPDATEDBY, MOD_DIDX_COUNTRY_ID, MOD_DIDX_ACCOUNT_ID, DIDX_COUNTRY_NAME, DIDX_COUNTRY_CODE, DIDX_COUNTRYID, DIDX_SEARCH)"
//							 + "\n VALUES \n" + 
//							 "(1000000, 1000001, 'Y', TO_DATE('09/16/2009 12:22:03', 'MM/DD/YYYY HH24:MI:SS'), 0, TO_DATE('09/16/2009 12:22:03', 'MM/DD/YYYY HH24:MI:SS'), 0, "
//							 + count + ", 1000000, '" + country.getDescription() + "', " + country.getCountryCode() + ", " + country.getCountryId() + ", 'Y');");
//			count++;
			
//			MDIDxCountry didxCountry = new MDIDxCountry(ctx, 0, trxName);
//			didxCountry.setMOD_DIDX_Account_ID(acct.getMOD_DIDX_Account_ID());
//			didxCountry.setDIDX_COUNTRY_NAME(country.getDescription());
//			didxCountry.setDIDX_COUNTRY_CODE(Integer.parseInt(country.getCountryCode()));
//			didxCountry.setDIDX_COUNTRYID(Integer.parseInt(country.getCountryId()));
//			
//			if (!didxCountry.save())
//			{
//				System.err.println("Failed to save record for " + country.getDescription() + " id=" + country.getCountryId() + " code=" + country.getCountryCode());
//				error = true;
//			}
//			else
//				System.out.println("Saved MDIDxCountry[" + didxCountry.get_ID() + "-" + didxCountry.getDIDX_COUNTRY_NAME() + "]");
		}
		
//		Trx trx = Trx.get(trxName, false);		
//		if (!error)
//		{
//			if (trx != null)
//				trx.commit();
//			System.out.println("Commited");
//		}
//		else
//		{
//			if (trx != null)
//				trx.rollback();
//			System.out.println("Rolled back");
//		}
	}
}
