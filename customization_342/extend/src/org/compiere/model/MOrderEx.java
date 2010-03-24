package org.compiere.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Properties;

import org.compiere.util.DB;
import org.compiere.util.Env;

public class MOrderEx extends MOrder
{
	public MOrderEx(Properties ctx, int C_Order_ID, String trxName)
	{
		super (ctx, C_Order_ID, trxName);	
	}
	
	/**
	 * Get orders belonging to a user (used by Web Store - JH)
	 * @param ctx
	 * @param AD_User_ID id
	 * @return array of orders
	 */
	public static MOrder[] getOrders(Properties ctx, int AD_User_ID, String docStatus)
	{
		ArrayList<MOrder> list = new ArrayList<MOrder>();
		String sql = "SELECT * FROM C_Order WHERE AD_User_ID = ? AND AD_Client_ID = ? AND AD_Org_ID = ? AND IsActive = 'Y'";
		
		if (docStatus != null)
			sql = sql + " AND DocStatus = ?";
		
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement (sql, null);
			pstmt.setInt (1, AD_User_ID);
			pstmt.setInt (2, Env.getAD_Client_ID(ctx));
			pstmt.setInt (3, Env.getAD_Org_ID(ctx));
			
			if (docStatus != null)
				pstmt.setString(2, docStatus);
			
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next ())
				list.add(new MOrder(ctx, rs, null));
			rs.close ();
			pstmt.close ();
			pstmt = null;
		} 
		catch (Exception e)
		{
			// TODO: Add logging
//			s_log.log(Level.SEVERE, sql, e);
		}
		try
		{
			if (pstmt != null)
				pstmt.close ();
			pstmt = null;
		} 
		catch (Exception e)
		{
			pstmt = null;
		}
		
		MOrder[] retValue = new MOrder[list.size ()];
		list.toArray (retValue);
		return retValue;
	}
}
