package com.conversant.webservice;

import java.util.Properties;

import javax.jws.WebService;

import org.compiere.model.MBPartnerLocation;
import org.compiere.model.MWebServiceType;
import org.compiere.model.PO;
import org.compiere.model.X_WS_WebService_Para;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;
import org.compiere.util.Login;
import org.compiere.util.Trx;

import com.conversant.webservice.util.WebServiceConstants;
import com.conversant.webservice.util.WebServiceUtil;


@WebService(endpointInterface = "com.conversant.webservice.GenericWebService")
public class GenericWebServiceImpl implements GenericWebService
{
	private static CLogger log = CLogger.getCLogger(GenericWebServiceImpl.class);
	
	public GenericWebServiceImpl()
	{
		WebServiceUtil.startADempiere();
	}
	
	public StandardResponse createTrx(CreateTrxRequest createTrxRequest)
	{
		Properties ctx = Env.getCtx();
		
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("GENERIC_WEBSERVICE"), WebServiceConstants.GENERIC_WEBSERVICE_METHODS.get("CREATE_TRX_METHOD_ID"), createTrxRequest.getLoginRequest(), null);
		
		if (error != null)	
			return getErrorStandardResponse(error, null);
		
		// Load specified prefix or use current time (in milliseconds)
		String trxNamePrefix = createTrxRequest.getTrxNamePrefix();
		if (trxNamePrefix == null || trxNamePrefix.length() < 1)
			trxNamePrefix = String.valueOf(System.currentTimeMillis());
		
		String trxName = Trx.createTrxName(trxNamePrefix);
		Trx trx = Trx.get(trxName, true);
		
		if (trx != null)
			return getStandardResponse(true, "Transaction has been created", trxName, null);
		else
			return getErrorStandardResponse("Failed to create transaction", null);
	}
	
	public StandardResponse commitTrx(CommitTrxRequest commitTrxRequest)
	{
		Properties ctx = Env.getCtx();		
		String trxName = getTrxName(commitTrxRequest.getLoginRequest());
		
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("GENERIC_WEBSERVICE"), WebServiceConstants.GENERIC_WEBSERVICE_METHODS.get("COMMIT_TRX_METHOD_ID"), commitTrxRequest.getLoginRequest(), trxName);
		
		if (error != null)	
			return getErrorStandardResponse(error, trxName);

		Trx trx = null;
		try
		{
			trx = Trx.get(trxName, false);	
			if (trx != null)
			{
				if (trx.commit())
				{
					trx.close();
					return getStandardResponse(true, "Success", trxName, WebServiceConstants.STANDARD_RESPONSE_DEFAULT_ID);
				}
				else
					return getErrorStandardResponse("Failed to commit transaction Trx[" + trxName + "]", trxName);
			}
		}
		catch (Exception ex)
		{
			// Catches Trx.get() IllegalArgumentExceptions
		}
		finally
		{
			if (trx != null)
				trx.close();
		}
		
		return getErrorStandardResponse("Failed to load Trx[" + trxName + "]", trxName);
	}
	
	public StandardResponse rollbackTrx(RollbackTrxRequest rollbackTrxRequest)
	{
		Properties ctx = Env.getCtx();		
		String trxName = getTrxName(rollbackTrxRequest.getLoginRequest());
		
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("GENERIC_WEBSERVICE"), WebServiceConstants.GENERIC_WEBSERVICE_METHODS.get("ROLLBACK_TRX_METHOD_ID"), rollbackTrxRequest.getLoginRequest(), trxName);
		
		if (error != null)	
			return getErrorStandardResponse(error, trxName);
		
		Trx trx = null;
		try
		{
			trx = Trx.get(trxName, false);	
			if (trx != null)
			{
				if (trx.rollback())
				{
					trx.close();
					return getStandardResponse(true, "Success", trxName, WebServiceConstants.STANDARD_RESPONSE_DEFAULT_ID);
				}
				else
					return getErrorStandardResponse("Failed to rollback transaction Trx[" + trxName + "]", trxName);
			}
		}
		catch (Exception ex)
		{
			// Catches Trx.get() IllegalArgumentException's
		}
		finally
		{
			if (trx != null)
				trx.close();
		}
		
		return getErrorStandardResponse("Failed to load Trx[" + trxName + "]", trxName);
	}
	
	/**
	 * Login to ADempiere
	 * 
	 * @param ctx
	 * @param WS_WebService_ID
	 * @param WS_WebServiceMethod_ID
	 * @param loginRequest
	 * @param trxName
	 * @return String error or NULL if successful
	 */
	public String login(Properties ctx, int WS_WebService_ID, int WS_WebServiceMethod_ID, LoginRequest loginRequest, String trxName)
	{		
		if (loginRequest == null)
			return "Login Failed - Missing LoginRequest";
			
		Login login = new Login(ctx);
		
		// Validate WebServiceType value
		String webServiceTypeValue = loginRequest.getType();
		if (webServiceTypeValue == null || webServiceTypeValue.length() < 1 || 
			webServiceTypeValue.contains("%") || webServiceTypeValue.contains(";") || webServiceTypeValue.contains("_"))
			return "Login Failed - Invalid WebServiceType";
		
		KeyNamePair[] userRoles = login.getRoles(loginRequest.getUsername(), loginRequest.getPassword());
		MWebServiceType webServiceType = MWebServiceType.get(ctx, WS_WebService_ID, WS_WebServiceMethod_ID, webServiceTypeValue, trxName);
		
		if (webServiceType == null)
		{
			log.severe("Failed to load MWebServiceType[" + WS_WebService_ID + "-" + WS_WebServiceMethod_ID + "-" + webServiceTypeValue + "]");
		}
		else if (webServiceType.validRole(userRoles))
		{
			int AD_Client_ID = -1;
			int AD_Org_ID = -1;
			int AD_Role_ID = -1;
			
			X_WS_WebService_Para[] parameters = webServiceType.getParameters(true);
			for (X_WS_WebService_Para parameter : parameters)
			{
				if (parameter.getParameterName().equals("AD_Client_ID"))
					AD_Client_ID = Integer.parseInt(parameter.getConstantValue());
				else if (parameter.getParameterName().equals("AD_Org_ID"))
					AD_Org_ID = Integer.parseInt(parameter.getConstantValue());
				else if (parameter.getParameterName().equals("AD_Role_ID"))
					AD_Role_ID = Integer.parseInt(parameter.getConstantValue());
			}
			
			// Vaildate login parameters
			if (AD_Client_ID < 0 || AD_Org_ID < 0 || AD_Role_ID < 0)
			{
				log.severe("Failed to load required values from " + webServiceType + " AD_Client_ID=" + AD_Client_ID + ", AD_Org_ID=" + AD_Org_ID + ", AD_Role_ID=" + AD_Role_ID);
				return "Login Failed - WebServiceType config";
			}
			
			// Get full KeyNamePair with name if possible
			KeyNamePair roleLogin = new KeyNamePair(AD_Role_ID, "");
			for (KeyNamePair role : userRoles)
			{
				if (role.getKey() == AD_Role_ID)
					roleLogin = role;
			}
			
			KeyNamePair[] clients = login.getClients(roleLogin);
			
			// Get full KeyNamePair with name if possible
			KeyNamePair clientLogin = new KeyNamePair(AD_Client_ID, "");
			for (KeyNamePair client : clients)
			{
				if (client.getKey() == AD_Client_ID)
					clientLogin = client;
			}
			
			KeyNamePair[] orgs = login.getOrgs(clientLogin);
			
			// Get full KeyNamePair with name if possible
			KeyNamePair orgLogin = new KeyNamePair(AD_Org_ID, "");
			for (KeyNamePair org : orgs)
			{
				if (org.getKey() == AD_Org_ID)
					orgLogin = org;					
			}
			
			String error = login.validateLogin(orgLogin);
			if (error != null && error.length() > 0)
				return error;
			
			return null; // Success
		}
		else
			return "Login Failed - Permission denied";
		
		return "Login Failed";
	}
	
	/**
	 * Gets Business Partner Location ID from Business Partner
	 * 
	 * @param ctx
	 * @param C_BPartner_ID
	 * @return Valid C_BPartner_Location_ID or NULL
	 */
	protected Integer getBusinessPartnerLocationId(Properties ctx, Integer C_BPartner_ID)
	{
		return validateBusinessPartnerLocationId(ctx, C_BPartner_ID, null);
	}
	
	/**
	 * Validates Business Partner Location ID 
	 * - Checks if given C_BPartner_Location_ID exists
	 * - If doesn't exist or not specified will load default location from Business Partner
	 * 
	 * @param ctx
	 * @param C_BPartner_ID
	 * @param C_BPartner_Location_ID (Optional)
	 * @return Valid C_BPartner_Location_ID or NULL
	 */
	protected Integer validateBusinessPartnerLocationId(Properties ctx, Integer C_BPartner_ID, Integer C_BPartner_Location_ID)
	{
		MBPartnerLocation[] businessPartnerLocations = MBPartnerLocation.getForBPartner(ctx, C_BPartner_ID);
		if (C_BPartner_Location_ID != null && C_BPartner_Location_ID > 0)
		{	
			boolean locationFound = false;
			
			for (MBPartnerLocation businessPartnerLocation : businessPartnerLocations)
			{
				if (C_BPartner_Location_ID == businessPartnerLocation.getC_BPartner_Location_ID())
				{
					locationFound = true;
					break;
				}
			}
			
			if (!locationFound)
				C_BPartner_Location_ID = null;
		}
		
		// Use first returned location as default
		if (C_BPartner_Location_ID == null || C_BPartner_Location_ID < 1)
		{
			if (businessPartnerLocations.length > 0 && businessPartnerLocations[0] != null)
				C_BPartner_Location_ID = businessPartnerLocations[0].getC_BPartner_Location_ID();
			else 
				C_BPartner_Location_ID = null;
		}
		
		return C_BPartner_Location_ID;
	}
	
	public boolean validateADId(String tableName, int id, String trxName)
	{
		int[] actualIds = PO.getAllIDs(tableName, "UPPER(IsActive)='Y'", trxName); 
		if (actualIds == null)
		{
			log.severe("Failed to load all Table Ids for " + tableName);
			return false;
		}
		
		for (int actualId : actualIds)
		{
			if (id == actualId)
				return true;
		}
		
		return false;
	}
	
	/**
	 * Vaildates a string
	 * 
	 * @param s string to validate
	 * @return true if valid
	 */
	public boolean validateString(String s)
	{
		if (s == null || s.trim().length() < 1)
			return false;
		else
			return true;
	}
	
	/**
	 * Gets Trx name, if empty string will set to NULL
	 * 
	 * @param loginRequest
	 * @return trxName or NULL
	 */
	protected String getTrxName(LoginRequest loginRequest)
	{
		if (loginRequest == null)
			return null;
			
		String trxName = loginRequest.getTrxName();
		if (trxName != null && trxName.length() < 1)
			trxName = null;
		
		return trxName;
	}
	
	/**
	 * Get StandardResponse for error
	 * 
	 * @param message
	 * @param trxName
	 * @return StandardResponse
	 */
	protected StandardResponse getErrorStandardResponse(String message, String trxName)
	{
		return getStandardResponse(false, message, trxName, null);
	}
	
	/**
	 * Get StandardResponse 
	 * 
	 * @param success
	 * @param message
	 * @param trxName
	 * @param id
	 * @return StandardResponse
	 */
	protected StandardResponse getStandardResponse(boolean success, String message, String trxName, Integer id)
	{
		if (message == null)
			message = "";
		
		if (trxName == null)
			trxName =  "";
		
		if (id == null)
			id = WebServiceConstants.STANDARD_RESPONSE_DEFAULT_ID;
		
		ObjectFactory objectFactory = new ObjectFactory();
		StandardResponse standardResponse = objectFactory.createStandardResponse();
		standardResponse.setSuccess(success);
		standardResponse.setMessage(message);
		standardResponse.setTrxName(trxName);
		standardResponse.setId(id);
		
		return standardResponse;
	}
}
