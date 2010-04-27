package com.conversant.process;

import java.util.ArrayList;

import org.compiere.model.MAttribute;
import org.compiere.model.MAttributeInstance;
import org.compiere.model.MProduct;
import org.compiere.process.SvrProcess;
import org.compiere.util.CLogger;

import com.conversant.db.BillingConnector;
import com.conversant.did.DIDConstants;
import com.conversant.did.DIDUtil;

public class SubscribedFaxNumberSync  extends SvrProcess
{
	/** Logger										*/
	private static CLogger log = CLogger.getCLogger(SubscribedFaxNumberSync.class);

	/**
	 *  Prepare
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
		int count = 0;
		ArrayList<String> subscribedNumbers = BillingConnector.getSubscribedNumbers();
		ArrayList<String> failedNumbers = new ArrayList<String>();
		
		MAttribute didIsSetupAttribute = new MAttribute(getCtx(), DIDConstants.ATTRIBUTE_ID_DID_ISSETUP, null);
		MAttribute didNumberAttribute = new MAttribute(getCtx(), DIDConstants.ATTRIBUTE_ID_DID_NUMBER, null); 
		MAttribute didFaxIsFaxAttribute = new MAttribute(getCtx(), DIDConstants.ATTRIBUTE_ID_DID_FAX_ISFAX, null); 

		MProduct[] products = DIDUtil.getBySubscription(getCtx(), true);
		for (MProduct product : products)
		{
			MAttributeInstance mai_isSetup = didIsSetupAttribute.getMAttributeInstance(product.getM_AttributeSetInstance_ID());
			MAttributeInstance mai_didNumber = didNumberAttribute.getMAttributeInstance(product.getM_AttributeSetInstance_ID());
			MAttributeInstance mai_faxIsFax = didFaxIsFaxAttribute.getMAttributeInstance(product.getM_AttributeSetInstance_ID());
			
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
			
			if (mai_faxIsFax == null || mai_faxIsFax.getValue() == null || mai_faxIsFax.getValue().length() < 1)
			{
				log.severe("Failed to load DID_FAX_ISFAX for " + product);
				attributeError = true;
			}
			
			if (attributeError)
				continue;
			
			// Get values
			String didNumber = mai_didNumber.getValue().trim();
			boolean isMonthly = mai_isSetup.getValue().equals("false");
			boolean isFax = mai_faxIsFax.getValue().equals("true");
			
			// Add subscribed monthly fax products
			if (isFax && isMonthly)
			{
				// Check doesn't exist
				boolean found = false;
				for (String subscribedNumber : subscribedNumbers)
				{
					if (didNumber.equals(subscribedNumber))
					{
						found = true;						
						break;
					}
				}
				
				if (!found)
				{
					// Add to database and track result
					if (BillingConnector.addSubscribedNumber(didNumber) == -1)
						failedNumbers.add(didNumber);
					else 
						count++;
				}
			}
		}
		
		if (failedNumbers.size() > 0)
		{
			String msg = "@Error@ " + count + " fax numbers synchronised but failed to synchronise the following numbers ";
			
			for (String number : failedNumbers)
				msg = msg + number + ", ";
			
			msg = msg.substring(0, msg.length() - 2);
			
			return msg;
		}
		else
			return "@Success@ " + count + " fax numbers synchronised";
	}
}
