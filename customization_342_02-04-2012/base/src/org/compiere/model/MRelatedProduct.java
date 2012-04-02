package org.compiere.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;

import org.compiere.util.CLogger;
import org.compiere.util.DB;

public class MRelatedProduct extends X_M_RelatedProduct
{
	/** Static Logger					*/
	private static CLogger s_log = CLogger.getCLogger(MRelatedProduct.class);
	
	public MRelatedProduct(Properties ctx, int M_RelatedProduct_ID, String trxName) 
	{
		super(ctx, M_RelatedProduct_ID, trxName);
/*		
		if (M_RelatedProduct_ID == 0)
		{
			setM_Product_ID (0);
			setName (null);
			setRelatedProductType (null);
			setRelatedProduct_ID (0);
		}
*/		
	}
	
	public MRelatedProduct(Properties ctx, ResultSet rs, String trxName) 
	{
		super(ctx, rs, trxName);
	}

	public static MRelatedProduct[] getOfProduct (Properties ctx, int M_Product_ID, String trxName)
	{
		ArrayList<MRelatedProduct> list = new ArrayList<MRelatedProduct>();
		String sql = "SELECT * FROM M_RelatedProduct "
			+ "WHERE M_Product_ID=? AND IsActive='Y' ";
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement (sql, trxName);
			pstmt.setInt (1, M_Product_ID);
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next())
				list.add(new MRelatedProduct (ctx, rs, trxName));
			rs.close ();
			pstmt.close ();
			pstmt = null;
		}
		catch (SQLException ex)
		{
			s_log.log(Level.SEVERE, sql, ex);
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
		//
		MRelatedProduct[] retValue = new MRelatedProduct[list.size()];
		list.toArray(retValue);
		return retValue;
	}	//	getOfProduct

}
