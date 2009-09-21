package org.compiere.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
	
	public MDIDxCountry(Properties ctx, String countryName, int countryCode, int countryId, boolean search)
	{
		super(ctx, 0, null);
		setDIDX_COUNTRY_NAME(countryName);
		setDIDX_COUNTRY_CODE(countryCode);
		setDIDX_COUNTRYID(countryId);
		setDIDX_SEARCH(search);
	}
	
    public MDIDxCountry(Properties ctx, ResultSet rs, String trxName)
    {
      super (ctx, rs, trxName);
    }

   /* ******************************************************************** */
    
	public static ArrayList<MDIDxCountry> getCountries(Properties ctx)
	{
		ArrayList<MDIDxCountry> countries = new ArrayList<MDIDxCountry>();		
		
		MDIDxAccount acct = MDIDxAccount.getBySearchKey(ctx, null, null);
		if (acct != null)
			countries = loadDIDxCountries(ctx, acct.getMOD_DIDX_Account_ID(), null);
		
		// Load static list if failed to load from db
		if (countries == null || countries.size() < 1)
		{
			log.warning("Couldn't load DIDx Account and/or DIDx Country list from DB, using static list instead");
			countries = loadStaticDIDxCountries(ctx);
		}

		return countries;
	}

    /**
     * Load DIDx Countries from DB
     * 
     * @param ctx
     * @param MDIDxAccount_ID
     * @param trxName
     * @return ArrayList of DIDx Countries
     */
	private static ArrayList<MDIDxCountry> loadDIDxCountries(Properties ctx, int MDIDxAccount_ID, String trxName)
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
		
		return countries;
	}
	
	private static ArrayList<MDIDxCountry> loadStaticDIDxCountries(Properties ctx)
	{
		ArrayList<MDIDxCountry> countries = new ArrayList<MDIDxCountry>();	
		
		countries.add(new MDIDxCountry(ctx, "Argentina", 54, 9, true));
		countries.add(new MDIDxCountry(ctx, "Australia", 61, 13, true));
		countries.add(new MDIDxCountry(ctx, "Austria", 43, 223, true));
		countries.add(new MDIDxCountry(ctx, "Bahrain", 973, 17, true));
		countries.add(new MDIDxCountry(ctx, "Belgium", 32, 21, true));
		countries.add(new MDIDxCountry(ctx, "Brazil", 55, 29, true));
		countries.add(new MDIDxCountry(ctx, "Bulgaria", 359, 32, true));
		countries.add(new MDIDxCountry(ctx, "Canada", 1, 37, true));
		countries.add(new MDIDxCountry(ctx, "Chile", 56, 42, true));
		countries.add(new MDIDxCountry(ctx, "China", 86, 43, true));
		countries.add(new MDIDxCountry(ctx, "Colombia", 57, 46, true));
		countries.add(new MDIDxCountry(ctx, "Croatia", 385, 51, true));
		countries.add(new MDIDxCountry(ctx, "Cyprus", 357, 53, true));
		countries.add(new MDIDxCountry(ctx, "Czech Republic", 420, 54, true));
		countries.add(new MDIDxCountry(ctx, "Denmark", 45, 56, true));
		countries.add(new MDIDxCountry(ctx, "Dominican Republic", 1, 224, true));
		countries.add(new MDIDxCountry(ctx, "Estonia", 372, 64, true));
		countries.add(new MDIDxCountry(ctx, "Finland", 358, 68, true));
		countries.add(new MDIDxCountry(ctx, "France", 33, 69, true));
		countries.add(new MDIDxCountry(ctx, "Georgia", 995, 74, true));
		countries.add(new MDIDxCountry(ctx, "Germany", 49, 75, true));
		countries.add(new MDIDxCountry(ctx, "Greece", 30, 78, true));
		countries.add(new MDIDxCountry(ctx, "Guatemala", 502, 83, true));
		countries.add(new MDIDxCountry(ctx, "Hong Kong", 852, 90, true));
		countries.add(new MDIDxCountry(ctx, "Hungary", 36, 91, true));
		countries.add(new MDIDxCountry(ctx, "Iceland", 354, 92, true));
		countries.add(new MDIDxCountry(ctx, "Ireland", 353, 101, true));
		countries.add(new MDIDxCountry(ctx, "Israel", 972, 102, true));
		countries.add(new MDIDxCountry(ctx, "Italy", 39, 103, true));
		countries.add(new MDIDxCountry(ctx, "Jamaica", 1, 104, true));
		countries.add(new MDIDxCountry(ctx, "Japan", 81, 105, true));
		countries.add(new MDIDxCountry(ctx, "Jordan", 962, 106, true));
		countries.add(new MDIDxCountry(ctx, "Kenya", 254, 108, true));
		countries.add(new MDIDxCountry(ctx, "Latvia", 371, 114, true));
		countries.add(new MDIDxCountry(ctx, "Lithuania", 370, 120, true));
		countries.add(new MDIDxCountry(ctx, "Luxembourg", 352, 121, true));
		countries.add(new MDIDxCountry(ctx, "Malaysia", 60, 126, true));
		countries.add(new MDIDxCountry(ctx, "Mexico", 52, 134, true));
		countries.add(new MDIDxCountry(ctx, "Netherlands", 31, 144, true));
		countries.add(new MDIDxCountry(ctx, "New Zealand", 64, 147, true));
		countries.add(new MDIDxCountry(ctx, "Norway", 47, 153, true));
		countries.add(new MDIDxCountry(ctx, "Pakistan", 92, 155, true));
		countries.add(new MDIDxCountry(ctx, "Panama", 507, 157, true));
		countries.add(new MDIDxCountry(ctx, "Peru", 51, 160, true));
		countries.add(new MDIDxCountry(ctx, "Philippines", 63, 161, true));
		countries.add(new MDIDxCountry(ctx, "Poland", 48, 162, true));
		countries.add(new MDIDxCountry(ctx, "Romania", 40, 167, true));
		countries.add(new MDIDxCountry(ctx, "Russia", 7, 168, true));
		countries.add(new MDIDxCountry(ctx, "Singapore", 65, 181, true));
		countries.add(new MDIDxCountry(ctx, "Slovak Republic", 421, 182, true));
		countries.add(new MDIDxCountry(ctx, "Slovenia", 386, 230, true));
		countries.add(new MDIDxCountry(ctx, "South Africa", 27, 185, true));
		countries.add(new MDIDxCountry(ctx, "South Korea", 82, 110, true));
		countries.add(new MDIDxCountry(ctx, "Spain", 34, 186, true));
		countries.add(new MDIDxCountry(ctx, "Sweden", 46, 191, true));
		countries.add(new MDIDxCountry(ctx, "Switzerland", 41, 192, true));
		countries.add(new MDIDxCountry(ctx, "Thailand", 66, 198, true));
		countries.add(new MDIDxCountry(ctx, "Turkey", 90, 203, true));
		countries.add(new MDIDxCountry(ctx, "Ukraine", 380, 208, true));
		countries.add(new MDIDxCountry(ctx, "United Kingdom", 44, 210, true));
		countries.add(new MDIDxCountry(ctx, "USA", 1, 211, true));
		countries.add(new MDIDxCountry(ctx, "Venezuela", 58, 215, true));

		return countries;
	}
	
	public static void sortByCode(ArrayList<MDIDxCountry> countries, boolean asc)
    {
    	if (asc)
			Collections.sort(countries, CODE_ASC);
		else
			Collections.sort(countries, CODE_DESC);
    }
	
    private static final Comparator<MDIDxCountry> CODE_ASC = new Comparator<MDIDxCountry>() 
    {
		public int compare(MDIDxCountry country1, MDIDxCountry country2) 
		{	
			return new Integer(country1.getDIDX_COUNTRY_CODE()).compareTo(country2.getDIDX_COUNTRY_CODE());
		}
    };
    
    private static final Comparator<MDIDxCountry> CODE_DESC = new Comparator<MDIDxCountry>() 
    {
		public int compare(MDIDxCountry country1, MDIDxCountry country2) 
		{	
			return new Integer(country2.getDIDX_COUNTRY_CODE()).compareTo(country1.getDIDX_COUNTRY_CODE());
		}
    };
}
