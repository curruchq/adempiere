package com.conversant.process;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;

import org.compiere.model.MAttribute;
import org.compiere.model.MAttributeInstance;
import org.compiere.model.MProduct;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.compiere.util.Trx;

import com.conversant.did.DIDUtil;

public class DIDProductCheck extends SvrProcess
{
	/** Logger 																	*/
	private static CLogger log = CLogger.getCLogger(DIDProductCheck.class);

	private static final int DID_M_ATTRIBUTESET_ID = 1000002;
	
	private static final int CDR_M_ATTRIBUTE_SET_ID = 1000007;
	
	private static final int DID_ISSETUP_ATTRIBUTE = 1000008;
	
	private static final int DID_NUMBER_ATTRIBUTE = 1000015;
	
	private static final int CDR_NUMBER_ATTRIBUTE = 1000035;
	
	private static final int CDR_DIRECTION_ATTRIBUTE = 1000036;
	
	private static final int ATTRIBUTE_ID_CDR_DIRECTION_VALUE_INBOUND = 1000003;
	
	private static final int ATTRIBUTE_ID_CDR_DIRECTION_VALUE_OUTBOUND = 1000004;
	
	/** Conversant Client														*/
	private int AD_Client_ID = 1000000; // Conversant
	
	/** Conversant Org															*/
	private int AD_Org_ID = 1000001; // Conversant
	
	@Override
	protected void prepare()
	{
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (para[i].getParameter() == null)
				;
			else if (name.equals("AD_Client_ID"))
			{
				AD_Client_ID = ((BigDecimal)para[i].getParameter()).intValue();
			}
			else if (name.equals("AD_Org_ID"))
			{
				AD_Org_ID = ((BigDecimal)para[i].getParameter()).intValue();
			}
			else
			{
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
			}
		}
	}

	@Override
	protected String doIt() throws Exception
	{
		// Set client and org (useful when run via scheduler)
		int originalAD_Client_ID = Env.getAD_Client_ID(getCtx());
		Env.setContext(getCtx(), "#AD_Client_ID", AD_Client_ID);
		
		int originalAD_Org_ID = Env.getAD_Org_ID(getCtx());
		Env.setContext(getCtx(), "#AD_Org_ID", AD_Org_ID);
		
		try
		{	
			return process();
		}
		catch (Exception ex)
		{
			throw ex;
		}
		finally
		{
			// Reset client and org 
			Env.setContext(getCtx(), "#AD_Client_ID", originalAD_Client_ID);
			Env.setContext(getCtx(), "#AD_Org_ID", originalAD_Org_ID);
		}
	}

	private String process()
	{
		productLoad();
		productPairs();
		
		return "@Success@ - Check log messages for results";
	}
	
	public void productLoad()
	{
		String trxName = Trx.createTrxName("productLoad");
		
		try
		{
			MProduct[] didProducts_bySearchKey = MProduct.get(getCtx(), "(value LIKE 'DID-%' OR value LIKE 'DIDSU-%') AND UPPER(IsActive) = 'Y' AND M_Product_ID NOT IN(" + getProductsToSkip() + ")", trxName);	
			MProduct[] didProducts_byAttributeSet = MProduct.get(getCtx(), "M_AttributeSet_ID = " + DID_M_ATTRIBUTESET_ID + " AND UPPER(IsActive) = 'Y' AND M_Product_ID NOT IN(" + getProductsToSkip() + ")", trxName);	
			MProduct[] didProducts_byDIDUtil = DIDUtil.getAllDIDProducts(getCtx(), trxName);
		
			if (didProducts_bySearchKey.length != didProducts_byDIDUtil.length ||
				didProducts_bySearchKey.length != didProducts_byAttributeSet.length || 
				didProducts_byAttributeSet.length != didProducts_byDIDUtil.length)
			{
				print("DID product count mismatch --> " + 
						   "BySearchKey=" + didProducts_bySearchKey.length + " " +
						   "ByAttributeSet=" + didProducts_byAttributeSet.length + " " +
						   "ByDIDUtil=" + didProducts_byDIDUtil.length);
			}
			
			MProduct[] callProducts_bySearchKey = MProduct.get(getCtx(), "(value LIKE 'CALL-IN-%' OR value LIKE 'CALL-OUT-%') AND UPPER(IsActive) = 'Y' AND M_Product_ID NOT IN(" + getProductsToSkip() + ")", trxName);	
			MProduct[] callProducts_byAttributeSet = MProduct.get(getCtx(), "M_AttributeSet_ID = " + CDR_M_ATTRIBUTE_SET_ID + " AND UPPER(IsActive) = 'Y' AND M_Product_ID NOT IN(" + getProductsToSkip() + ")", trxName);	
			MProduct[] callProducts_byDIDUtil = DIDUtil.getAllCallProducts(getCtx(), trxName);
		
			if (callProducts_bySearchKey.length != callProducts_byDIDUtil.length ||
				callProducts_bySearchKey.length != callProducts_byAttributeSet.length || 
				callProducts_byAttributeSet.length != callProducts_byDIDUtil.length)
			{
				print("CALL product count mismatch --> " + 
						   "BySearchKey=" + callProducts_bySearchKey.length + " " +
						   "ByAttributeSet=" + callProducts_byAttributeSet.length + " " +
						   "ByDIDUtil=" + callProducts_byDIDUtil.length);
			}
		}
		catch (Exception ex)
		{
			print(ex.getMessage());
		}
		finally
		{
			Trx trx = Trx.get(trxName, false);
			if (trx != null && trx.isActive())
				trx.commit();
		}		
	}
	
	/**
	 * Checks DID/DIDSU and CALL-IN/CALL-OUT pairs then all four
	 */
	private void productPairs()
	{
		String trxName = Trx.createTrxName("productPairs");
		
		try
		{
			// 
			HashSet<String> allNumbers = new HashSet<String>();
			
			// DID/DIDSU pairs
			MProduct[] didProducts = DIDUtil.getAllDIDProducts(getCtx(), trxName);
			HashMap[] seperatedDIDProducts = seperateDIDProducts(didProducts, trxName);
			HashMap<String, MProduct> setupProducts = seperatedDIDProducts[0];
			HashMap<String, MProduct> monthlyProducts = seperatedDIDProducts[1];
			
			HashSet<String> didNumbers = loadNumbers(didProducts, trxName);
			allNumbers.addAll(didNumbers);

			
			// CALL-IN/CALL-OUT pairs
			MProduct[] callProducts = DIDUtil.getAllCallProducts(getCtx(), trxName);
			HashMap[] seperatedCallProducts = seperateCallProducts(callProducts, trxName);
			HashMap<String, MProduct> callInProducts = seperatedCallProducts[0];
			HashMap<String, MProduct> callOutProducts = seperatedCallProducts[1];
			
			HashSet<String> callNumbers = loadNumbers(callProducts, trxName);
			allNumbers.addAll(callNumbers);
			
			// All four 
			HashSet<String> missingSetupNumbers = new HashSet<String>();
			HashSet<String> missingMonthlyNumbers = new HashSet<String>();
			HashSet<String> missingCallInNumbers = new HashSet<String>();
			HashSet<String> missingCallOutNumbers = new HashSet<String>();
			
			for (String number : allNumbers)
			{
				boolean found = false;
				
				for (String setupNumber : setupProducts.keySet())
				{
					if (number.equals(setupNumber))
					{
						found = true;
						break;
					}
				}
				
				if (!found)
					missingSetupNumbers.add(number);
				else
					found = false;
				
				for (String monthlyNumber : monthlyProducts.keySet())
				{
					if (number.equals(monthlyNumber))
					{
						found = true;
						break;
					}
				}
				
				if (!found)
					missingMonthlyNumbers.add(number);
				else
					found = false;
				
				for (String callInNumber : callInProducts.keySet())
				{
					if (number.equals(callInNumber))
					{
						found = true;
						break;
					}
				}
				
				if (!found)
					missingCallInNumbers.add(number);
				else
					found = false;
				
				for (String callOutNumber : callOutProducts.keySet())
				{
					if (number.equals(callOutNumber))
					{
						found = true;
						break;
					}
				}
				
				if (!found)
					missingCallOutNumbers.add(number);
				else
					found = false;
			}
			
			// Results
			print("Missing DID products: (" + missingMonthlyNumbers.size() + ") " + missingMonthlyNumbers.toString());
			print("Missing DID-SU products: (" + missingSetupNumbers.size() + ") " + missingSetupNumbers.toString());
			print("Missing CALL-IN products: (" + missingCallInNumbers.size() + ") " + missingCallInNumbers.toString());
			print("Missing CALL-OUT products: (" + missingCallOutNumbers.size() + ") " + missingCallOutNumbers.toString());
		}
		catch (Exception ex)
		{
			print(ex.getMessage());
		}
		finally
		{
			Trx trx = Trx.get(trxName, false);
			if (trx != null && trx.isActive())
				trx.commit();
		}
	}
	
	private HashSet<String> loadNumbers(MProduct[] products, String trxName)
	{
		HashSet<String> numbers = new HashSet<String>();
		
		MAttribute didNumberAttribute = new MAttribute(getCtx(), DID_NUMBER_ATTRIBUTE, trxName);
		MAttribute cdrNumberAttribute = new MAttribute(getCtx(), CDR_NUMBER_ATTRIBUTE, trxName);
		
		for (MProduct product : products)
		{
			MAttributeInstance mai_didNumber = didNumberAttribute.getMAttributeInstance(product.getM_AttributeSetInstance_ID());
			MAttributeInstance mai_cdrNumber = cdrNumberAttribute.getMAttributeInstance(product.getM_AttributeSetInstance_ID());
			
			// Check values for both attributes exist
			String number = null;
			if (mai_didNumber != null && mai_didNumber.getValue() != null && mai_didNumber.getValue().length() > 0)
			{
				number = mai_didNumber.getValue().trim();
			}
			
			if (mai_cdrNumber != null && mai_cdrNumber.getValue() != null && mai_cdrNumber.getValue().length() > 0)
			{
				number = mai_cdrNumber.getValue().trim();
			}
			
			if (number == null)
			{
				print("Failed to load number for " + product);
				continue;
			}
						
			numbers.add(number);
		}
		
		return numbers;
	}
	
	private HashMap[] seperateDIDProducts(MProduct[] allDIDProducts, String trxName)
	{
		// Static reference to attributes
		MAttribute didIsSetupAttribute = new MAttribute(getCtx(), DID_ISSETUP_ATTRIBUTE, trxName);
		MAttribute didNumberAttribute = new MAttribute(getCtx(), DID_NUMBER_ATTRIBUTE, trxName); 
		
		// Hashmaps to hold products		
		HashMap<String, MProduct> setupProducts = new HashMap<String, MProduct>();
		HashMap<String, MProduct> monthlyProducts = new HashMap<String, MProduct>();
		
		// Sort products in lists
		for (MProduct product : allDIDProducts)
		{
			MAttributeInstance mai_isSetup = didIsSetupAttribute.getMAttributeInstance(product.getM_AttributeSetInstance_ID());
			MAttributeInstance mai_didNumber = didNumberAttribute.getMAttributeInstance(product.getM_AttributeSetInstance_ID());
			
			// Check values for both attributes exist
			boolean attributeError = false;
			if (mai_isSetup == null || mai_isSetup.getValue() == null || mai_isSetup.getValue().length() < 1)
			{
				print("Failed to load DID_ISSETUP for " + product);
				attributeError = true;
			}
			
			if (mai_didNumber == null || mai_didNumber.getValue() == null || mai_didNumber.getValue().length() < 1)
			{
				print("Failed to load DID_NUMBER for " + product);
				attributeError = true;
			}
			
			if (attributeError)
				continue;
						
			// Load DID number
			String didNumber = mai_didNumber.getValue().trim();
						
			// Put product in either setup or monthly struct
			if (mai_isSetup.getValue().equalsIgnoreCase("true"))
				setupProducts.put(didNumber, product);
			
			else if (mai_isSetup.getValue().equalsIgnoreCase("false"))
				monthlyProducts.put(didNumber, product);
			
			else
				print("Invalid DID_ISSETUP value for " + product + " DID_ISSETUP=" + mai_isSetup.getValue());
		}
		
		return new HashMap[]{setupProducts, monthlyProducts};
	}
	
	private HashMap[] seperateCallProducts(MProduct[] allCallProducts, String trxName)
	{
		// Static reference to attributes
		MAttribute cdrDirectionAttribute = new MAttribute(getCtx(), CDR_DIRECTION_ATTRIBUTE, trxName);
		MAttribute cdrNumberAttribute = new MAttribute(getCtx(), CDR_NUMBER_ATTRIBUTE, trxName); 
		
		// Hashmaps to hold products		
		HashMap<String, MProduct> callInProducts = new HashMap<String, MProduct>();
		HashMap<String, MProduct> callOutProducts = new HashMap<String, MProduct>();
		
		// Sort products in lists
		for (MProduct product : allCallProducts)
		{
			MAttributeInstance mai_direction = cdrDirectionAttribute.getMAttributeInstance(product.getM_AttributeSetInstance_ID());
			MAttributeInstance mai_number = cdrNumberAttribute.getMAttributeInstance(product.getM_AttributeSetInstance_ID());
			
			// Check values for both attributes exist
			boolean attributeError = false;
			if (mai_direction == null || mai_direction.getValue() == null || mai_direction.getValue().length() < 1)
			{
				print("Failed to load CDR_DIRECTION for " + product);
				attributeError = true;
			}
			
			if (mai_number == null || mai_number.getValue() == null || mai_number.getValue().length() < 1)
			{
				print("Failed to load CDR_NUMBER for " + product);
				attributeError = true;
			}
			
			if (attributeError)
				continue;
						
			// Load number
			String number = mai_number.getValue().trim();

			// Put product in either setup or monthly struct
			if (mai_direction.getM_AttributeValue_ID() == ATTRIBUTE_ID_CDR_DIRECTION_VALUE_INBOUND)
				callInProducts.put(number, product);
			
			else if (mai_direction.getM_AttributeValue_ID() == ATTRIBUTE_ID_CDR_DIRECTION_VALUE_OUTBOUND)
				callOutProducts.put(number, product);
			
			else
				print("Invalid CDR_DIRECTION value for " + product + " CDR_DIRECTION=" + mai_direction.getValue());
		}
		
		return new HashMap[]{callInProducts, callOutProducts};
	}
	
	private void print(String msg)
	{
		addLog(getProcessInfo().getAD_Process_ID(), new Timestamp(System.currentTimeMillis()), null, msg);	
	}
	
	private String getProductsToSkip()
	{
		StringBuilder sb = new StringBuilder();
		for (Integer id : DID_PRODUCTS_TO_SKIP.keySet())
		{
			sb.append(id + ",");
		}
		sb.replace(sb.length()-1, sb.length(), ""); // replace last ,
		
		return sb.toString();
	}
	
	// TODO: Check with Cameron if all these products are used
	protected static final HashMap<Integer, String> DID_PRODUCTS_TO_SKIP = new HashMap<Integer, String>() 
	{
		{
			put(1000044, "DIDSU-643-CHC");
			put(1000045, "DIDSU-643-DUN");
			put(1000036, "DIDSU-644-WGT");
			put(1000046, "DIDSU-647-HAM");
			put(1000048, "DIDSU-649-AKL");
			put(1000047, "DIDSU-649-HBC");
			put(1000050, "DIDSU-649-WHG");
			put(1000063, "DID-331706");
			put(1000061, "DID-441902");
			put(1000062, "DID-44208");
			put(1000052, "DID-61427-SMS");
			
			put(1000029, "DID-643-CHC");
			put(1000026, "DID-643-DUN");
			put(1000027, "DID-644-WGT");
			put(1000028, "DID-647-HAM");
			put(1000001, "DID-649-AKL");
			put(1000043, "DID-649-HBC");
			put(1000049, "DID-649-WHG");
			put(1000024, "DID-800");
			put(1000064, "DID-ST-MIN");
			put(1000530, "DID/DDI monthly charge template");
			put(1000531, "DID/DDI setup fee template");
		}
	};
	
	public static void main(String[] args)
	{
		
	}
}
