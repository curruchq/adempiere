package org.compiere.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;

import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;

import com.conversant.model.DIDCountry;

public class MDIDxCountry extends X_MOD_DIDX_Country 
{
	/** Logger						*/
	private static CLogger log = CLogger.getCLogger(MDIDxCountry.class);
	
	public MDIDxCountry(Properties ctx, int MOD_DIDX_Country_ID, String trxName)
	{
		super(ctx, MOD_DIDX_Country_ID, trxName);
		if (MOD_DIDX_Country_ID == 0)
		{
			// TODO: Load defaults which aren't already defined at table level
		}
	}
	
    public MDIDxCountry(Properties ctx, ResultSet rs, String trxName)
    {
      super (ctx, rs, trxName);
    }
	
    /**
     * Get active DIDx Countries
     * 
     * @param ctx
     * @param trxName
     * @return ArrayList of DID Countries
     */
	public static ArrayList<DIDCountry> getCountries(Properties ctx, int MDIDxAccount_ID, String trxName)
	{
		ArrayList<MDIDxCountry> countries = new ArrayList<MDIDxCountry>();
		int AD_Client_ID = Env.getAD_Client_ID(ctx);
		int AD_Org_ID = Env.getAD_Org_ID(ctx);
		
		String sql = "SELECT * FROM " + Table_Name + " WHERE AD_Client_ID=? AND AD_Org_ID=? AND MOD_DIDX_Account_ID=? AND IsActive='Y'";
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement(sql, trxName);
			pstmt.setInt(1, AD_Client_ID);
			pstmt.setInt(2, AD_Org_ID);
			pstmt.setInt(3, MDIDxAccount_ID);
			
			rs = pstmt.executeQuery();
			while (rs.next())
				countries.add(new MDIDxCountry(ctx, rs, trxName));
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

		ArrayList<DIDCountry> didCountries = new ArrayList<DIDCountry>();
		for (MDIDxCountry country : countries)
		{
			didCountries.add(new DIDCountry(country.getDIDX_COUNTRY_NAME(), Integer.toString(country.getDIDX_COUNTRY_CODE()), Integer.toString(country.getDIDX_COUNTRYID())));
		}
		
		DIDCountry.sortCountriesByCode(didCountries, true);
		
		return didCountries;
	}
}
