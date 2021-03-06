package org.compiere.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

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
	
	/**
	 * Get orders by DocumentNo (JH)
	 * @param ctx
	 * @param DocumentNo
	 * @return array of orders
	 */
	public static MOrder getOrder(Properties ctx, String documentNo, String trxName)
	{
		MOrder order = null;
		String sql = "SELECT * FROM C_Order WHERE AD_Client_ID = ? AND AD_Org_ID = ? AND DocumentNo LIKE ? AND IsActive = 'Y'";

		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(sql, trxName);
			pstmt.setInt(1, Env.getAD_Client_ID(ctx));
			pstmt.setInt(2, Env.getAD_Org_ID(ctx));
			pstmt.setString(3, documentNo);
			
			ResultSet rs = pstmt.executeQuery();
			
			if (rs.next())
				order = new MOrder(ctx, rs, trxName);
			
			rs.close();
			pstmt.close();
			pstmt = null;
		} 
		catch (Exception ex)
		{
			// TODO: add logging
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close();
				pstmt = null;
			}
			catch(SQLException ex)
			{
				pstmt = null;
			}
		}
		
		return order;
	}
	

	/**
	 * 	Document Status is Complete or Closed
	 *	@return true if CO, CL or RE
	 */
	public boolean isComplete()
	{
		String ds = getDocStatus();
		return DOCSTATUS_Completed.equals(ds) 
			|| DOCSTATUS_Closed.equals(ds)
			|| DOCSTATUS_Reversed.equals(ds);
	}	//	isComplete
	
	/**************************************************************************
	 * 	Get Lines of Order based on product category
	 * 	@param whereClause where clause or null (starting with AND)
	 * 	@param orderClause order clause
	 * 	@return lines
	 */
	public static List<MOrderLine> getLinesByProductCategory (Properties ctx,int m_C_Order_ID, int m_M_Product_Category_ID,String trxName)
	{
		ArrayList<MOrderLine> list = new ArrayList<MOrderLine> ();
		StringBuffer sql = new StringBuffer("SELECT * FROM C_OrderLine OL INNER JOIN M_Product P ON (OL.M_Product_ID = P.M_Product_ID) WHERE OL.C_Order_ID=? AND P.M_Product_Category_ID= ?");
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement(sql.toString(), trxName);
			pstmt.setInt(1, m_C_Order_ID);
			pstmt.setInt(2,m_M_Product_Category_ID );
			rs = pstmt.executeQuery();
			while (rs.next())
			{
				MOrderLine ol = new MOrderLine(ctx, rs, trxName);
				//ol.setHeaderInfo (this);
				list.add(ol);
			}
 		}
		catch (Exception e)
		{
			
		}
		finally
		{
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}
		//
		
		return list;
	}	//	getLines
	
	/**
	 * 	Get Orders Of BPartner
	 *	@param ctx context
	 *	@param C_BPartner_ID id
	 *	@param trxName transaction
	 *	@return array
	 */
	public static MOrder[] getOfBPartner (Properties ctx, int C_BPartner_ID, String trxName)
	{
		ArrayList<MOrder> list = new ArrayList<MOrder>();
		String sql = "SELECT * FROM C_Order WHERE C_BPartner_ID=?";
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(sql, trxName);
			pstmt.setInt(1, C_BPartner_ID);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
				list.add(new MOrder(ctx,rs, trxName));
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			
		}
		try
		{
			if (pstmt != null)
				pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			pstmt = null;
		}

		//
		MOrder[] retValue = new MOrder[list.size()];
		list.toArray(retValue);
		return retValue;
	}	//	getOfBPartner
	
	/**
	 * Get orders by DocumentNo and DocType(Lavanya)
	 * @param ctx
	 * @param DocumentNo
	 * @return array of orders
	 */
	public static MOrder getOrder(Properties ctx, String documentNo, int docType,String trxName)
	{
		MOrder order = null;
		String sql = "SELECT * FROM C_Order WHERE AD_Client_ID = ? AND C_DOCTYPETARGET_ID = ? AND DocumentNo LIKE ? AND IsActive = 'Y'";

		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(sql, trxName);
			pstmt.setInt(1, Env.getAD_Client_ID(ctx));
			pstmt.setInt(2, docType);
			pstmt.setString(3, documentNo);
			
			ResultSet rs = pstmt.executeQuery();
			
			if (rs.next())
				order = new MOrder(ctx, rs, trxName);
			
			rs.close();
			pstmt.close();
			pstmt = null;
		} 
		catch (Exception ex)
		{
			// TODO: add logging
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close();
				pstmt = null;
			}
			catch(SQLException ex)
			{
				pstmt = null;
			}
		}
		
		return order;
	}
}
