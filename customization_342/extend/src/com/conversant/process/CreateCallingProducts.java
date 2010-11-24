package com.conversant.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.compiere.model.MProduct;
import org.compiere.process.SvrProcess;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.wstore.DIDController;

import com.conversant.did.DIDConstants;
import com.conversant.did.DIDUtil;

public class CreateCallingProducts extends SvrProcess
{
	/** Logger										*/
	private static CLogger log = CLogger.getCLogger(CreateCallingProducts.class);

	private static String PROCESS_MSG_SUCCESS = "@Success@";
	private static String PROCESS_MSG_ERROR = "@Error@";
	
	private static int BATCH_COUNT = 50;
	
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
		
// ------------ Hack to allow product retrieval ---------------
		
		int AD_Client_ID = Env.getAD_Client_ID(getCtx());
		Env.setContext(getCtx(), "#AD_Client_ID", "1000000");
		
		HashMap<String, MProduct> didProducts = new HashMap<String, MProduct>();
		for (MProduct product : DIDUtil.getAllDIDProducts(Env.getCtx(), null))
		{
			String number = DIDUtil.getDIDNumber(Env.getCtx(), product, null);
			didProducts.put(number, product);
		}
		
		HashMap<String, MProduct> callProducts = new HashMap<String, MProduct>();
		for (MProduct product : DIDUtil.getAllCallProducts(Env.getCtx(), null))
		{
			String number = DIDUtil.getCDRNumber(Env.getCtx(), product, null);
			callProducts.put(number, product);
		}
		
		Env.setContext(getCtx(), "#AD_Client_ID", AD_Client_ID);
	
// ------------------------------------------------------------
		
		ArrayList<String> createdCallProductNumbers = new ArrayList<String>();
		
		int count = 0;
		Iterator<String> didIterator = didProducts.keySet().iterator();
		while(didIterator.hasNext())
		{
			String number = (String)didIterator.next();
			MProduct didProduct = (MProduct)didProducts.get(number);
			
			boolean found = false;
			
			Iterator<String> callIterator = callProducts.keySet().iterator();
			while(callIterator.hasNext())
			{
				String username = (String)callIterator.next();
				MProduct callProduct = (MProduct)callProducts.get(username);
				
				if (number.equalsIgnoreCase(username))
				{
					found = true;
					break;
				}
			}
			
			if (!found)
			{
				if (count >= BATCH_COUNT)
					break;
				else
					count++;
				
				HashMap<Integer, Object> attributes = new HashMap<Integer, Object>();
				attributes.put(DIDConstants.ATTRIBUTE_ID_CDR_USERNAME, DIDConstants.ATTRIBUTE_VALUE_INBOUND_CDR_USERNAME.replace(DIDConstants.NUMBER_IDENTIFIER, number).replace(DIDConstants.DOMAIN_IDENTIFIER, "conversant.co.nz"));
				attributes.put(DIDConstants.ATTRIBUTE_ID_CDR_APPLICATION, DIDConstants.ATTRIBUTE_ID_CDR_APPLICATION_VALUE_AUDIO);
				attributes.put(DIDConstants.ATTRIBUTE_ID_CDR_DIRECTION, DIDConstants.ATTRIBUTE_ID_CDR_DIRECTION_VALUE_INBOUND);
				attributes.put(DIDConstants.ATTRIBUTE_ID_CDR_NUMBER, number);
				
				MProduct inbound = DIDUtil.createCallProduct(Env.getCtx(), attributes, null);
				if (!DIDController.updateProductPrice(Env.getCtx(), 1000000, inbound.getM_Product_ID(), Env.ZERO, null))
					log.severe("Failed to create price for " + inbound);
				
				if (inbound != null)
				{						
					attributes.remove(DIDConstants.ATTRIBUTE_ID_CDR_USERNAME);
					attributes.remove(DIDConstants.ATTRIBUTE_ID_CDR_DIRECTION);
					attributes.put(DIDConstants.ATTRIBUTE_ID_CDR_USERNAME, DIDConstants.ATTRIBUTE_VALUE_OUTBOUND_CDR_USERNAME.replace(DIDConstants.NUMBER_IDENTIFIER, number).replace(DIDConstants.DOMAIN_IDENTIFIER, "conversant.co.nz"));
					attributes.put(DIDConstants.ATTRIBUTE_ID_CDR_DIRECTION, DIDConstants.ATTRIBUTE_ID_CDR_DIRECTION_VALUE_OUTBOUND);
					
					MProduct outbound = DIDUtil.createCallProduct(Env.getCtx(), attributes, null);
					if (!DIDController.updateProductPrice(Env.getCtx(), 1000000, outbound.getM_Product_ID(), Env.ZERO, null))
						log.severe("Failed to create price for " + outbound);
					
					if (outbound != null)
						createdCallProductNumbers.add(number);
					else
						log.severe("Outbound product is NULL for " + number + " either create it or delete " + inbound);
				}
				else
					log.severe("Inbound product is NULL for " + number);		
			}
		}

		return PROCESS_MSG_SUCCESS + "\n\n" + "Created " + createdCallProductNumbers.size() + " products";
	}
}
