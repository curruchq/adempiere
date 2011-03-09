package org.compiere.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;

import org.compiere.util.CLogger;
import org.compiere.util.DB;

public class MInvoiceEx
{
	/**	Logger									*/
	private static CLogger log = CLogger.getCLogger (MInvoiceEx.class);
	
	/**
	 * 	Get Payments Of BPartner
	 *	@param ctx context
	 *	@param C_BPartner_ID id
	 *	@param trxName transaction
	 *	@return array
	 */
	public static MInvoice[] getOfBPartnerOrdered(Properties ctx, int C_BPartner_ID, String trxName)
	{
		ArrayList<MInvoice> list = new ArrayList<MInvoice>();
		String sql = "SELECT * FROM C_Invoice WHERE C_BPartner_ID=? ORDER BY DocumentNo ASC";
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(sql, trxName);
			pstmt.setInt(1, C_BPartner_ID);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
				list.add(new MInvoice(ctx,rs, trxName));
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, sql, e);
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
		MInvoice[] retValue = new MInvoice[list.size()];
		list.toArray(retValue);
		return retValue;
	}	//	getOfBPartner
}
