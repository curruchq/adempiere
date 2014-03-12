package org.compiere.model;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;

import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;

public class MSubscription extends X_C_Subscription
{
	/** Logger						*/
	private static CLogger log = CLogger.getCLogger(MSubscription.class);
	
    /** Column name BillInAdvance	*/
    public static final String COLUMNNAME_BillInAdvance = "Bill_In_Advance";
    
    /** Column name Qty				*/
    public static final String COLUMNNAME_Qty = "Qty";
    
    /** Column name COLUMNNAME_C_BPartner_Location_ID*/
    public static final String COLUMNNAME_C_BPartner_Location_ID="C_BPartner_Location_ID";
	
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
	
	/**
	 * Set Partner Location.
	 * 
	 * @param C_BPartner_Location_ID
	 *            Identifies the (ship to) address for this Business Partner
	 */
	/*public void setC_BPartner_Location_ID(int C_BPartner_Location_ID)
	{
		if (C_BPartner_Location_ID < 1)
			throw new IllegalArgumentException(
					"C_BPartner_Location_ID is mandatory.");
		set_ValueNoCheck(MBPartnerLocation.COLUMNNAME_C_BPartner_Location_ID, Integer
				.valueOf(C_BPartner_Location_ID));
	}

	*//**
	 * Get Partner Location.
	 * 
	 * @return Identifies the (ship to) address for this Business Partner
	 *//*
	public int getC_BPartner_Location_ID()
	{
		Integer ii = (Integer) get_Value(MBPartnerLocation.COLUMNNAME_C_BPartner_Location_ID);
		if (ii == null)
			return 0;
		return ii.intValue();
	}*/
	
	/**
	 * Set BillInAdvance.
	 * 
	 * @param IsBillInAdvance
	 *            Subscription bill in advance
	 */
	public void setBillInAdvance(boolean IsBillInAdvance)
	{
		set_Value(COLUMNNAME_BillInAdvance, Boolean.valueOf(IsBillInAdvance));
	}

	/**
	 * Get BillInAdvance.
	 * 
	 * @return Subscription bill in advance
	 */
	public boolean isBillInAdvance()
	{
		Object oo = get_Value(COLUMNNAME_BillInAdvance);
		if (oo != null)
		{
			if (oo instanceof Boolean)
				return ((Boolean) oo).booleanValue();
			return "Y".equals(oo);
		}
		return false;
	}
	
	/**
	 * Set Qty.
	 * 
	 * @param qty
	 */
	public void setQty(BigDecimal qty)
	{
		set_Value(COLUMNNAME_Qty, qty);
	}

	/**
	 * Get Qty.
	 * 
	 * @return Qty
	 */
	public BigDecimal getQty()
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_Qty);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}
	
	public static MSubscription[] getSubscriptions(Properties ctx, Integer M_Product_ID, String trxName)
	{
		ArrayList<MSubscription> list = new ArrayList<MSubscription>();
		String sql = "SELECT * FROM " + MSubscription.Table_Name + " WHERE " + MSubscription.COLUMNNAME_M_Product_ID + "=? AND IsActive='Y'";
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try
		{
			pstmt = DB.prepareStatement(sql, trxName);
			pstmt.setInt(1, M_Product_ID);
			rs = pstmt.executeQuery();
			
			while (rs.next())
				list.add(new MSubscription(ctx, rs, trxName));
		}
		catch (SQLException ex)
		{
			log.log(Level.SEVERE, sql, ex);
		}
		finally
		{
			DB.close(rs, pstmt);

			pstmt = null;
			rs = null;
		}
		
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
	
	public I_C_BPartner_Location getC_BPartner_Location() throws Exception 
    {
        Class<?> clazz = MTable.getClass(I_C_BPartner_Location.Table_Name);
        I_C_BPartner_Location result = null;
        try	{
	        Constructor<?> constructor = null;
	    	constructor = clazz.getDeclaredConstructor(new Class[]{Properties.class, int.class, String.class});
    	    result = (I_C_BPartner_Location)constructor.newInstance(new Object[] {getCtx(), new Integer(getC_BPartner_Location_ID()), get_TrxName()});
        } catch (Exception e) {
	        log.log(Level.SEVERE, "(id) - Table=" + Table_Name + ",Class=" + clazz, e);
	        log.saveError("Error", "Table=" + Table_Name + ",Class=" + clazz);
           throw e;
        }
        return result;
    }

	/** Set Partner Location.
		@param C_BPartner_Location_ID 
		Identifies the (ship to) address for this Business Partner
	  */
	public void setC_BPartner_Location_ID (int C_BPartner_Location_ID)
	{
		if (C_BPartner_Location_ID < 1)
			 throw new IllegalArgumentException ("C_BPartner_Location_ID is mandatory.");
		set_Value (COLUMNNAME_C_BPartner_Location_ID, Integer.valueOf(C_BPartner_Location_ID));
	}

	/** Get Partner Location.
		@return Identifies the (ship to) address for this Business Partner
	  */
	public int getC_BPartner_Location_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_BPartner_Location_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}
}
