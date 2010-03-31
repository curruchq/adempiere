package com.conversant.process;

import java.util.ArrayList;

import org.compiere.model.MAttribute;
import org.compiere.model.MAttributeInstance;
import org.compiere.model.MProduct;
import org.compiere.model.MProductPO;
import org.compiere.process.SvrProcess;
import org.compiere.util.CLogger;

import com.conversant.wstore.DIDConstants;

public class UpdateProductPO extends SvrProcess
{
	/** Logger										*/
	private static CLogger log = CLogger.getCLogger(UpdateProductPO.class);
	
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	@Override
	protected void prepare()
	{

	}

	/**
	 * 	Process
	 *	@return message
	 *	@throws Exception
	 */
	@Override
	protected String doIt() throws Exception
	{
		ArrayList<MProduct> updatedProducts = new ArrayList<MProduct>();
		ArrayList<MProduct> failedProducts = new ArrayList<MProduct>();
		
		// Static reference to attributes
		MAttribute didIsSetupAttribute = new MAttribute(getCtx(), DIDConstants.ATTRIBUTE_ID_DID_ISSETUP, null);
		MAttribute didNumberAttribute = new MAttribute(getCtx(), DIDConstants.ATTRIBUTE_ID_DID_NUMBER, null); 
		
		MProduct[] allProducts = MProduct.get(getCtx(), "M_AttributeSet_ID = " + DIDConstants.ATTRIBUTESET_ID_DID + " AND UPPER(IsActive) = 'Y'", null);
		
		int validProductPOCount = 0;
		
		for (MProduct product : allProducts)
		{
			MAttributeInstance mai_isSetup = didIsSetupAttribute.getMAttributeInstance(product.getM_AttributeSetInstance_ID());
			MAttributeInstance mai_didNumber = didNumberAttribute.getMAttributeInstance(product.getM_AttributeSetInstance_ID());
			
			// Check values for both attributes exist
			boolean attributeError = false;
			if (mai_isSetup == null || mai_isSetup.getValue() == null || mai_isSetup.getValue().length() < 1)
			{
				log.severe("Failed to load DID_ISSETUP for " + product);
				attributeError = true;
			}
			
			if (mai_didNumber == null || mai_didNumber.getValue() == null || mai_didNumber.getValue().length() < 1)
			{
				log.severe("Failed to load DID_NUMBER for " + product);
				attributeError = true;
			}
			
			if (attributeError)
				continue;
			
			// Get values
			String didNumber = mai_didNumber.getValue().trim();
			boolean isSetup = mai_isSetup.getValue().equals("true");
			
			MProductPO[] productPOs = MProductPO.getOfProduct(getCtx(), product.getM_Product_ID(), null);
			
			if (productPOs.length == 1)
			{
				MProductPO productPO = productPOs[0];
				
				String setupCorrect = DIDConstants.PRODUCT_PO_SETUP_VENDOR_PRODUCT_NO.replace(DIDConstants.NUMBER_IDENTIFIER, didNumber);
				String monthlyCorrect = DIDConstants.PRODUCT_PO_MONTHLY_VENDOR_PRODUCT_NO.replace(DIDConstants.NUMBER_IDENTIFIER, didNumber);
				
				if (!productPO.getVendorProductNo().equals(setupCorrect) && !productPO.getVendorProductNo().equals(monthlyCorrect))
				{
					String vendorProductNo = monthlyCorrect;
					if (isSetup)
						vendorProductNo = setupCorrect;
					
					productPO.setVendorProductNo(vendorProductNo);
					
					if (productPO.save())
						updatedProducts.add(product);
					else
						failedProducts.add(product);
					
				}
				else
					validProductPOCount++;
			}
		}
		
		String responseMsg = "Found " + validProductPOCount + " valid ProductPO(s). Updated " + updatedProducts.size() + " product PO(s).";
		
		if (failedProducts.size() > 0)
		{
			responseMsg += " Failed to update product PO(s) for ";
			for (MProduct product : failedProducts)
			{
				responseMsg += product + ", ";
			}
			responseMsg = responseMsg.substring(0, responseMsg.length() - 2);
		}
		
		return responseMsg;
	}
}
