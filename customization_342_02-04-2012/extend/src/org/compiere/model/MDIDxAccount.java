package org.compiere.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;
import java.util.logging.Level;

import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;

/**
 *  DIDx Account
 *
 *  @author Josh Hill
 *  @version  $Id: MDIDxAccount.java,v 1.0 2009/09/14 14:53:21 jhill Exp $
 */
public class MDIDxAccount extends X_MOD_DIDX_Account
{
	/** Logger						*/
	private static CLogger log = CLogger.getCLogger(MDIDxAccount.class);
	
	public MDIDxAccount(Properties ctx, int MOD_DIDX_Account_ID, String trxName)
	{
		super(ctx, MOD_DIDX_Account_ID, trxName);
		if (MOD_DIDX_Account_ID == 0)
		{
			// TODO: Load defaults which aren't already defined at table level
		}
	}
	
    public MDIDxAccount(Properties ctx, ResultSet rs, String trxName)
    {
      super (ctx, rs, trxName);
    }
	
    /**
     * Get DIDx Account by Search Key
     * 
     * @param ctx
     * @param searchKey if null returns first record
     * @param trxName
     * @return DIDx Account
     */
	public static MDIDxAccount getBySearchKey(Properties ctx, String searchKey, String trxName)
	{
		MDIDxAccount acct = null;
		int AD_Client_ID = Env.getAD_Client_ID(ctx);
		int AD_Org_ID = Env.getAD_Org_ID(ctx);

		// Get all records
		if (searchKey == null || searchKey.length() < 1)
			searchKey = "%";
		
		String sql = "SELECT * FROM " + Table_Name + " WHERE AD_Client_ID=? AND AD_Org_ID=? AND Value LIKE ? AND IsActive='Y'";
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement(sql, trxName);
			pstmt.setInt(1, AD_Client_ID);
			pstmt.setInt(2, AD_Org_ID);
			pstmt.setString(3, searchKey);
			
			rs = pstmt.executeQuery();
			if (rs.next())
				acct = new MDIDxAccount(ctx, rs, trxName);
		} 
		catch (Exception ex)
		{
			log.log(Level.SEVERE, sql, ex);
		}
		finally
		{
			DB.close(rs, pstmt);
			rs = null;
			pstmt = null;
		}

		return acct;
	}
}
