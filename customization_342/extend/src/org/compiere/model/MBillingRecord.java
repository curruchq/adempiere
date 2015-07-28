package org.compiere.model;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.Properties;

import org.compiere.util.CLogger;
import org.compiere.util.Env;

import com.conversant.model.RadiusAccount;

public class MBillingRecord extends X_MOD_Billing_Record
{
	/** Logger						*/
	private static CLogger log = CLogger.getCLogger(MBillingRecord.class);
	
	public MBillingRecord(Properties ctx, int MOD_BILLING_RECORD_ID, String trxName)
	{
		super(ctx, MOD_BILLING_RECORD_ID, trxName);
		if (MOD_BILLING_RECORD_ID == 0)
		{
			// TODO: Load defaults which aren't already defined at table level
		}
	}
	
	public MBillingRecord(Properties ctx, ResultSet rs, String trxName)
    {
      super (ctx, rs, trxName);
    }
	
	public static MBillingRecord createNew(Properties ctx, RadiusAccount account, String trxName)
	{
		if (ctx == null || account == null)
			return null;
		
		MBillingRecord billingRecord = new MBillingRecord(ctx, 0, trxName);	
		
		if (account.getRadAcctId() != null)
		{
			billingRecord.setRadAcctId(account.getRadAcctId()); 
		}
		else
		{
			log.warning("RadiusAccount loaded with RadAcctId = null");
			return null;
		}
		
		String domain = account.getRealm();
		CharSequence cs = "conversant.net.au";
		if(domain.contains(cs))
			Env.setContext(ctx, "#AD_Org_ID", "1000008");
		else
			Env.setContext(ctx, "#AD_Org_ID", "1000001");
		
		billingRecord.setAD_Org_ID(Env.getAD_Org_ID(ctx));
		billingRecord.setAD_Client_ID(Env.getAD_Client_ID(ctx));
		
		billingRecord.setAcctStartTime(account.getAcctStartTime());
		billingRecord.setNormalized(account.getNormalized());
		billingRecord.setUserName(account.getUserName());
		billingRecord.setSipApplicationType(account.getSipApplicationType());
		billingRecord.setRate(account.getRate());
		
		try
		{
			billingRecord.setPrice(new BigDecimal(account.getPrice()));
		}
		catch (Exception ex)
		{
			billingRecord.setPrice(null);
		}
		
		return billingRecord;
	}
}
