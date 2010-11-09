package com.conversant.util;

import java.util.HashMap;
import java.util.logging.Level;

import org.compiere.Adempiere;
import org.compiere.model.MProduct;
import org.compiere.model.MSubscription;
import org.compiere.util.CLogMgt;
import org.compiere.util.Env;

import com.conversant.did.DIDConstants;
import com.conversant.did.DIDUtil;

public class Migrate
{
	private static final int TCC_0800_IN = 1000035;
	private static final int TCC_STD_IN = 1000896;
	private static final int TCC_STD_OUT = 1000002;
	
	public static void main(String[] args)
	{
		String number = "6494266777";
		
		startup();
		
//		MProduct setupProduct = getDIDProduct(number, true);
//		MProduct monthlyProduct = getDIDProduct(number, false);
//		
//		MSubscription in0800Sub = get0800InboundSubscription(number);
//		MSubscription inStdSub = getStdInboundSubscription(number);
//		MSubscription outStdSub = getStdOutboundSubscription(number);
//		
//		System.out.println(setupProduct);
//		System.out.println(monthlyProduct);
//		
//		System.out.println(in0800Sub);
//		System.out.println(inStdSub);
//		System.out.println(outStdSub);
		
		MProduct inbound = createDIDCallingProducts(number);
		createDIDCallingSubscription(number, inbound);
	}
	
	private static void startup()
	{
		System.setProperty("PropertyFile", "E:\\workspace\\customization_342\\VancouverAdempiere.properties");
		Adempiere.startupEnvironment(false);
		CLogMgt.setLevel(Level.INFO);
		
		Env.setContext(Env.getCtx(), "#AD_Client_ID", "1000000");
		Env.setContext(Env.getCtx(), "#AD_Org_ID", "1000001");
	}
	
	private static MProduct createDIDCallingProducts(String number)
	{
		HashMap<Integer, Object> attributes = new HashMap<Integer, Object>();
		attributes.put(DIDConstants.ATTRIBUTE_ID_CDR_USERNAME, DIDConstants.ATTRIBUTE_VALUE_CDR_USERNAME.replace(DIDConstants.NUMBER_IDENTIFIER, number).replace(DIDConstants.DOMAIN_IDENTIFIER, "conversant.co.nz"));
		attributes.put(DIDConstants.ATTRIBUTE_ID_CDR_APPLICATION, DIDConstants.ATTRIBUTE_ID_CDR_APPLICATION_VALUE_AUDIO);
		attributes.put(DIDConstants.ATTRIBUTE_ID_CDR_DIRECTION, DIDConstants.ATTRIBUTE_ID_CDR_DIRECTION_VALUE_INBOUND);
		attributes.put(DIDConstants.ATTRIBUTE_ID_CDR_NUMBER, number);
		
		MProduct inbound = DIDUtil.createCallingProduct(Env.getCtx(), attributes, null);
		
		System.out.println(inbound);
		
		return inbound;
	}
	
	private static void createDIDCallingSubscription(String number, MProduct product)
	{
		MSubscription sub = DIDUtil.createCallingSubscription(Env.getCtx(), number, 1000022, 1000023, product.getM_Product_ID(), null);
		
		System.out.println(sub);
	}
	
	private static MSubscription get0800InboundSubscription(String number)
	{
		MSubscription[] subs = MSubscription.getSubscriptions(Env.getCtx(), TCC_0800_IN, null);
		for (MSubscription sub : subs)
		{
			if (sub.getName().equals("+" + number))
				return sub;
		}
		
		return null;
	}
	
	private static MSubscription getStdInboundSubscription(String number)
	{
		MSubscription[] subs = MSubscription.getSubscriptions(Env.getCtx(), TCC_STD_IN, null);
		for (MSubscription sub : subs)
		{
			if (sub.getName().equals("+" + number))
				return sub;
		}
		
		return null;
	}
	
	private static MSubscription getStdOutboundSubscription(String number)
	{
		MSubscription[] subs = MSubscription.getSubscriptions(Env.getCtx(), TCC_STD_OUT, null);
		for (MSubscription sub : subs)
		{
			if (sub.getName().equals("+" + number))
				return sub;
		}
		
		return null;
	}
	
	private static MProduct getDIDProduct(String number, boolean setup)
	{
		MProduct[] products = DIDUtil.getDIDProducts(Env.getCtx(), number, null);
		if (products.length == 2)
			return DIDUtil.getSetupOrMonthlyProduct(Env.getCtx(), products[0], products[1], setup, null);	
		else
			System.err.println(number + " doesn't have both products");
		
		return null;
	}
}
