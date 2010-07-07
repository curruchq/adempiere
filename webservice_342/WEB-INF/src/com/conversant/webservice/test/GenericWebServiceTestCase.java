package com.conversant.webservice.test;

import java.util.HashMap;

import org.compiere.model.MCurrency;
import org.compiere.model.MProduct;
import org.compiere.util.CLogger;
import org.compiere.util.Trx;
import org.compiere.wstore.DIDController;

import com.conversant.test.AdempiereTestCase;
import com.conversant.webservice.GenericWebServiceImpl;
import com.conversant.webservice.LoginRequest;
import com.conversant.webservice.ObjectFactory;
import com.conversant.webservice.util.WebServiceConstants;

public class GenericWebServiceTestCase extends AdempiereTestCase 
{
	private static CLogger log = CLogger.getCLogger(GenericWebServiceTestCase.class);	
	private static boolean SHOW_TIMING = true;

	@Override
	protected void setUp() throws Exception 
	{
		super.setUp();
		log.info("Done setting up GenericWebServiceTestCase");
	}
	
	@Override
	protected void tearDown() throws Exception 
	{
		super.tearDown();
		log.info("Done tearing down GenericWebServiceTestCase");
	}
	
	private LoginRequest getLoginRequest(String username, String password, String type, String trxName)
	{
		ObjectFactory objFactory = new ObjectFactory();
    	
    	LoginRequest loginRequest = objFactory.createLoginRequest();    	
    	loginRequest.setUsername(username);
    	loginRequest.setPassword(password);
    	loginRequest.setType(type); 
    	loginRequest.setTrxName(trxName); 
    	
    	return loginRequest;
	}
	
	public void testLogin()
	{
		GenericWebServiceImpl gws = new GenericWebServiceImpl();		
		
		// Valid login
		String error = gws.login(getCtx(), WebServiceConstants.WEBSERVICES.get("PROVISION_WEBSERVICE"), WebServiceConstants.PROVISION_WEBSERVICE_METHODS.get("CREATE_DID_PRODUCT_METHOD_ID"), 
								 getLoginRequest("IntalioUser", "password", "P-createDIDProduct-Intalio", ""), null);
		if (error != null)
			fail("Failed valid login - " + error);
		
		// Invalid WS_WebService_ID
		error = gws.login(getCtx(), -1, WebServiceConstants.PROVISION_WEBSERVICE_METHODS.get("CREATE_DID_PRODUCT_METHOD_ID"), getLoginRequest("IntalioUser", "password", "P-createDIDProduct-Intalio", ""), null);
		if (error == null)
			fail("Logged in with invalid WS_WebService_ID");
		
		// Invalid WS_WebServiceMethod_ID
		error = gws.login(getCtx(), WebServiceConstants.WEBSERVICES.get("PROVISION_WEBSERVICE"), -1, getLoginRequest("IntalioUser", "password", "P-createDIDProduct-Intalio", ""), null);
		if (error == null)
			fail("Logged in with invalid WS_WebServiceMethod_ID");
		
		// Invalid WS_WebService_ID & WS_WebServiceMethod_ID
		error = gws.login(getCtx(), -1, -1, getLoginRequest("IntalioUser", "password", "P-createDIDProduct-Intalio", ""), null);
		if (error == null)
			fail("Logged in with invalid WS_WebService_ID & WS_WebServiceMethod_ID");
		
		// Valid login (null trxName)
		error = gws.login(getCtx(), WebServiceConstants.WEBSERVICES.get("PROVISION_WEBSERVICE"), WebServiceConstants.PROVISION_WEBSERVICE_METHODS.get("CREATE_DID_PRODUCT_METHOD_ID"), 
				 getLoginRequest("IntalioUser", "password", "P-createDIDProduct-Intalio", null), null);
		if (error != null)
			fail("Failed valid login - " + error);
		
		// Invalid login (null LoginRequest)
		error = gws.login(getCtx(), WebServiceConstants.WEBSERVICES.get("PROVISION_WEBSERVICE"), WebServiceConstants.PROVISION_WEBSERVICE_METHODS.get("CREATE_DID_PRODUCT_METHOD_ID"), null, null);
		if (error == null)
			fail("Logged in with empty username");
		
		// Invalid login (all null values in LoginRequest)
		error = gws.login(getCtx(), WebServiceConstants.WEBSERVICES.get("PROVISION_WEBSERVICE"), WebServiceConstants.PROVISION_WEBSERVICE_METHODS.get("CREATE_DID_PRODUCT_METHOD_ID"), getLoginRequest(null, null, null, null), null);
		if (error == null)
			fail("Logged in with all null values in LoginRequest");
		
		// Invalid login (empty username)
		error = gws.login(getCtx(), WebServiceConstants.WEBSERVICES.get("PROVISION_WEBSERVICE"), WebServiceConstants.PROVISION_WEBSERVICE_METHODS.get("CREATE_DID_PRODUCT_METHOD_ID"), 
				 getLoginRequest("", "password", "P-createDIDProduct-Intalio", ""), null);
		if (error == null)
			fail("Logged in with empty username");
		
		// Invalid login (null username)
		error = gws.login(getCtx(), WebServiceConstants.WEBSERVICES.get("PROVISION_WEBSERVICE"), WebServiceConstants.PROVISION_WEBSERVICE_METHODS.get("CREATE_DID_PRODUCT_METHOD_ID"), 
				 getLoginRequest(null, "password", "P-createDIDProduct-Intalio", ""), null);
		if (error == null)
			fail("Logged in with null username");
		
		// Invalid login (empty password)
		error = gws.login(getCtx(), WebServiceConstants.WEBSERVICES.get("PROVISION_WEBSERVICE"), WebServiceConstants.PROVISION_WEBSERVICE_METHODS.get("CREATE_DID_PRODUCT_METHOD_ID"), 
				 getLoginRequest("IntalioUser", "", "P-createDIDProduct-Intalio", ""), null);
		if (error == null)
			fail("Logged in with empty password");
		
		// Invalid login (null password)
		error = gws.login(getCtx(), WebServiceConstants.WEBSERVICES.get("PROVISION_WEBSERVICE"), WebServiceConstants.PROVISION_WEBSERVICE_METHODS.get("CREATE_DID_PRODUCT_METHOD_ID"), 
				 getLoginRequest("IntalioUser", null, "P-createDIDProduct-Intalio", ""), null);
		if (error == null)
			fail("Logged in with null password");

		// Invalid login (empty type)
		error = gws.login(getCtx(), WebServiceConstants.WEBSERVICES.get("PROVISION_WEBSERVICE"), WebServiceConstants.PROVISION_WEBSERVICE_METHODS.get("CREATE_DID_PRODUCT_METHOD_ID"), 
				 getLoginRequest("IntalioUser", "password", "", ""), null);
		if (error == null)
			fail("Logged in with empty type");
		
		// Invalid login (null type)
		error = gws.login(getCtx(), WebServiceConstants.WEBSERVICES.get("PROVISION_WEBSERVICE"), WebServiceConstants.PROVISION_WEBSERVICE_METHODS.get("CREATE_DID_PRODUCT_METHOD_ID"), 
				 getLoginRequest("IntalioUser", "password", null, ""), null);
		if (error == null)
			fail("Logged in with null type");
	}
	
	public void testValidatedADId()
	{
		GenericWebServiceImpl gws = new GenericWebServiceImpl();
		
		if (gws.validateADId("InvalidTableName", 100, null))
			fail("Validated an ID for an invalid TableName");
		
		if (gws.validateADId(MCurrency.Table_Name, 122, null))
			fail("Validated an invalid Currency ID");
		
		if (!gws.validateADId(MCurrency.Table_Name, 121, null))
			fail("Didn't validate valid Currency ID");
		
		String trxName = Trx.createTrxName("testValidatedADId");
		
		// Create product
		MProduct product = new MProduct(getCtx(), 0, trxName);		
		HashMap<String, String> fields = DIDController.getDIDSetupFields("1137894123", "Test Area Code");
		product.setValue(fields.get("Value"));
		product.setName(fields.get("Name"));
		product.setDescription(fields.get("Description"));
		product.setM_Product_Category_ID(Integer.parseInt(fields.get("M_Product_Category_ID")));
		product.setC_TaxCategory_ID(Integer.parseInt(fields.get("C_TaxCategory_ID")));
		product.setC_UOM_ID(Integer.parseInt(fields.get("C_UOM_ID")));
		product.setM_AttributeSet_ID(Integer.parseInt(fields.get("M_AttributeSet_ID")));
		product.setProductType(fields.get("ProductType"));
		product.setIsSelfService(fields.get("IsSelfService").equals("Y"));		
		product.save();
		
		try
		{
			if (!gws.validateADId(MProduct.Table_Name, product.getM_Product_ID(), trxName))
				throw new Exception("Didn't validate newly created MProduct ID");
			
			product.setIsActive(false);
			product.save();
			
			if (gws.validateADId(MProduct.Table_Name, product.getM_Product_ID(), trxName))
				throw new Exception("Validated inactive MProduct ID");
		}
		catch (Exception ex)
		{
			fail(ex.getMessage());
		}
		finally
		{
			Trx trx = Trx.get(trxName, false);
			if (trx != null)
			{
				trx.rollback();
				trx.close();
			}
		}
	}
}
