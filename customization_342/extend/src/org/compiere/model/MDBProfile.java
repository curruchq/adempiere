package org.compiere.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;
import java.util.logging.Level;

import org.compiere.util.CLogger;
import org.compiere.util.DB;

public class MDBProfile extends X_MOD_DB_Profile 
{
	/** Logger						*/
	private static CLogger log = CLogger.getCLogger(MDBProfile.class);
	
	public MDBProfile(Properties ctx, int MOD_DB_Profile_ID, String trxName)
	{
		super(ctx, MOD_DB_Profile_ID, trxName);
		if (MOD_DB_Profile_ID == 0)
		{
			// TODO: Load defaults which aren't already defined at table level
		}
	}
	
    public MDBProfile(Properties ctx, ResultSet rs, String trxName)
    {
      super (ctx, rs, trxName);
    }
    
    /**
     * Get DB Profile by Schema
     * 
     * @param ctx
     * @param schema 
     * @param trxName
     * @return DB Profile
     */
	public static MDBProfile getBySchema(Properties ctx, String schema, String trxName)
	{
		MDBProfile profile = null;

		// Validate
		if (schema == null || schema.length() < 1)
		{
			log.warning("Invalid schema name");
			return null;
		}
		
		String sql = "SELECT * FROM " + Table_Name + " WHERE " + COLUMNNAME_DB_SCHEMA + " LIKE ? AND IsActive='Y'";
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement(sql, trxName);
			pstmt.setString(1, schema);
			
			rs = pstmt.executeQuery();
			if (rs.next())
				profile = new MDBProfile(ctx, rs, trxName);
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

		return profile;
	}
}
