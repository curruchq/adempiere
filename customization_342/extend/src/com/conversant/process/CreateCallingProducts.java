package com.conversant.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.compiere.model.MProduct;
import org.compiere.process.SvrProcess;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.compiere.util.Trx;
import org.compiere.wstore.DIDController;

import com.conversant.did.DIDConstants;
import com.conversant.did.DIDUtil;

public class CreateCallingProducts extends SvrProcess
{
	/** Logger										*/
	private static CLogger log = CLogger.getCLogger(CreateCallingProducts.class);

	private static String PROCESS_MSG_SUCCESS = "@Success@";
	private static String PROCESS_MSG_ERROR = "@Error@";
	
	private static int PRICELIST_VERSION_ID = 1000000;
	
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

//------------------------------------------------------------
		
		String trxName = Trx.createTrxName("createCallProducts");
		
		try
		{	
			HashMap<String, MProduct> didProducts = new HashMap<String, MProduct>();
			for (MProduct product : DIDUtil.getAllDIDProducts(Env.getCtx(), trxName))
			{
				String number = DIDUtil.getDIDNumber(Env.getCtx(), product, trxName);
				didProducts.put(number, product);
			}
			
			HashMap<String, MProduct> callProducts = new HashMap<String, MProduct>();
			for (MProduct product : DIDUtil.getAllCallProducts(Env.getCtx(), trxName))
			{
				String number = DIDUtil.getCDRNumber(Env.getCtx(), product, trxName);
				callProducts.put(number, product);
			}

			ArrayList<String> createdCallProductNumbers = new ArrayList<String>();
			
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
					HashMap<Integer, Object> attributes = new HashMap<Integer, Object>();
					attributes.put(DIDConstants.ATTRIBUTE_ID_CDR_USERNAME, DIDConstants.ATTRIBUTE_VALUE_INBOUND_CDR_USERNAME.replace(DIDConstants.NUMBER_IDENTIFIER, number).replace(DIDConstants.DOMAIN_IDENTIFIER, "conversant.co.nz"));
					attributes.put(DIDConstants.ATTRIBUTE_ID_CDR_APPLICATION, DIDConstants.ATTRIBUTE_ID_CDR_APPLICATION_VALUE_AUDIO);
					attributes.put(DIDConstants.ATTRIBUTE_ID_CDR_DIRECTION, DIDConstants.ATTRIBUTE_ID_CDR_DIRECTION_VALUE_INBOUND);
					attributes.put(DIDConstants.ATTRIBUTE_ID_CDR_NUMBER, number);
					
					MProduct inbound = DIDUtil.createCallProduct(Env.getCtx(), attributes, trxName);
					if (!DIDController.updateProductPrice(Env.getCtx(), PRICELIST_VERSION_ID, inbound.getM_Product_ID(), Env.ZERO, trxName))
						throw new Exception("Failed to create price for " + inbound);
					
					attributes.remove(DIDConstants.ATTRIBUTE_ID_CDR_USERNAME);
					attributes.remove(DIDConstants.ATTRIBUTE_ID_CDR_DIRECTION);
					attributes.put(DIDConstants.ATTRIBUTE_ID_CDR_USERNAME, DIDConstants.ATTRIBUTE_VALUE_OUTBOUND_CDR_USERNAME.replace(DIDConstants.NUMBER_IDENTIFIER, number).replace(DIDConstants.DOMAIN_IDENTIFIER, "conversant.co.nz"));
					attributes.put(DIDConstants.ATTRIBUTE_ID_CDR_DIRECTION, DIDConstants.ATTRIBUTE_ID_CDR_DIRECTION_VALUE_OUTBOUND);
					
					MProduct outbound = DIDUtil.createCallProduct(Env.getCtx(), attributes, trxName);
					if (!DIDController.updateProductPrice(Env.getCtx(), PRICELIST_VERSION_ID, outbound.getM_Product_ID(), Env.ZERO, trxName))
						throw new Exception("Failed to create price for " + outbound);
					
					createdCallProductNumbers.add(number);
				}
			}
			
			Trx trx = null;
			try
			{
				trx = Trx.get(trxName, false);	
				if (trx != null)
				{
					if (!trx.commit())
						throw new Exception("Failed to commit trx");
				}
			}
			catch (Exception ex)
			{
				// Catches Trx.get() IllegalArgumentExceptions
				throw new Exception("Failed to get trx");
			}
			finally
			{
				if (trx != null && trx.isActive())
					trx.close();
			}
	
			return PROCESS_MSG_SUCCESS + "\n\n" + "Created " + createdCallProductNumbers.size() + " CALL products";
		}
		catch (Exception ex)
		{
			log.warning("CreateCallingProducts process failed - " + ex.getMessage());
			return PROCESS_MSG_ERROR + "\n\n" + ex.getMessage();
		}
		finally
		{
			// Rollback trx
			Trx trx = Trx.get(trxName, false);
			if (trx != null && trx.isActive())
			{
				trx.rollback();
				trx.close();
			}		
			
// ------------ Remove Hack -----------------------------------
			
			Env.setContext(getCtx(), "#AD_Client_ID", AD_Client_ID);

// ------------------------------------------------------------
		}
	}
}
