package org.compiere.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;

import org.compiere.util.CLogger;
import org.compiere.util.DB;

public class MSubscription extends X_C_Subscription
{
	/** Logger					*/
	private static CLogger log = CLogger.getCLogger(MSubscription.class);
	
    /** Column name BillInAdvance */
    public static final String COLUMNNAME_BillInAdvance = "Bill_In_Advance";
	
	public MSubscription(Properties ctx, int C_Subscription_ID, String trxName)
	{
		super(ctx, C_Subscription_ID, trxName);
/*
		if (C_Subscription_ID == 0)
		{
			setC_BPartner_ID(0);
			setC_SubscriptionType_ID(0);
			setC_Subscription_ID(0);
			setIsDue(false);
			setM_Product_ID(0);
			setName(null);
			setPaidUntilDate(new Timestamp(System.currentTimeMillis()));
			setRenewalDate(new Timestamp(System.currentTimeMillis()));
			setStartDate(new Timestamp(System.currentTimeMillis()));
		}
*/
	}
	
	public MSubscription(Properties ctx, ResultSet rs, String trxName) 
	{
		super(ctx, rs, trxName);
	}
	
	/** Set BillInAdvance.
		@param IsBillInAdvance 
		Subscription bill in advance
	  */
	public void setBillInAdvance (boolean IsBillInAdvance)
	{
		set_Value (COLUMNNAME_BillInAdvance, Boolean.valueOf(IsBillInAdvance));
	}
	
	/** Get BillInAdvance.
		@return Subscription bill in advance
	  */
	public boolean isBillInAdvance () 
	{
		Object oo = get_Value(COLUMNNAME_BillInAdvance);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}
	
	public static MSubscription[] getSubscriptions(Properties ctx, Integer M_Product_ID, String trxName)
	{
		ArrayList<MSubscription> list = new ArrayList<MSubscription>();
		String sql = "SELECT * FROM C_Subscription "
			+ "WHERE M_Product_ID=? AND IsActive='Y' ";
		
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(sql, trxName);
			pstmt.setInt(1, M_Product_ID);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
				list.add(new MSubscription(ctx, rs, trxName));
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (SQLException ex)
		{
			log.log(Level.SEVERE, sql, ex);
		}
		try
		{
			if (pstmt != null)
				pstmt.close ();
		}
		catch (SQLException ex1)
		{
		}
		pstmt = null;
		
		MSubscription[] retValue = new MSubscription[list.size()];
		list.toArray(retValue);
		return retValue;
	}
	
	public static MSubscription[] getSubscriptions(Properties ctx, Integer M_Product_ID, int C_BPartner_ID, String trxName)
	{
		ArrayList<MSubscription> list = new ArrayList<MSubscription>();
		String sql = "SELECT * FROM C_Subscription "
			+ "WHERE C_BPartner_ID=? AND IsActive='Y' ";
		
		if (M_Product_ID != null)
			sql = sql + "AND M_Product_ID=? ";
		
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(sql, trxName);
			pstmt.setInt(1, C_BPartner_ID);
			if (M_Product_ID != null)
				pstmt.setInt(2, M_Product_ID);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
				list.add(new MSubscription(ctx, rs, trxName));
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (SQLException ex)
		{
			log.log(Level.SEVERE, sql, ex);
		}
		try
		{
			if (pstmt != null)
				pstmt.close ();
		}
		catch (SQLException ex1)
		{
		}
		pstmt = null;
		
		MSubscription[] retValue = new MSubscription[list.size()];
		list.toArray(retValue);
		return retValue;
	}	//	getOfProduct
}
