package org.compiere.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;

import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;

public class MBPartnerEx extends MBPartner
{
	/**	Logger				*/
	private static CLogger log = CLogger.getCLogger (MBPartnerEx.class);
	
	/**
	 * 	Default Constructor
	 * 	@param ctx context
	 * 	@param C_BPartner_ID partner or 0 or -1 (load from template)
	 * 	@param trxName transaction
	 */
	public MBPartnerEx (Properties ctx, int C_BPartner_ID, String trxName)
	{
		super(ctx, C_BPartner_ID, trxName);
	}	//	MBPartnerEx
	
	/**
	 * 	Get BPartner using Name (used by Web Store search - JH)
	 *	@param ctx context 
	 *	@param name name
	 *	@return BPartner or null
	 */
	public static ArrayList<MBPartner> getByName(Properties ctx, String name)
	{
		ArrayList<MBPartner> businessPartners = new ArrayList<MBPartner>();
		
		if (name == null || name.length() == 0)
			return businessPartners;
		
		if (!name.startsWith("%"))
			name = "%" + name;
		
		if (!name.endsWith("%"))
			name = name + "%";

		int AD_Client_ID = Env.getAD_Client_ID(ctx);
		String sql = "SELECT * FROM C_BPartner WHERE UPPER(Name) LIKE UPPER(?) AND AD_Client_ID=?";
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement (sql, null);
			pstmt.setString(1, name);
			pstmt.setInt(2, AD_Client_ID);
			ResultSet rs = pstmt.executeQuery ();
			
			while (rs.next())		
				businessPartners.add(new MBPartner(ctx, rs, null));
			
			rs.close ();
			pstmt.close ();
			pstmt = null;
		}
		catch (Exception ex)
		{
			log.log(Level.SEVERE, sql, ex);
		}
		try
		{
			if (pstmt != null)
				pstmt.close ();
			pstmt = null;
		}
		catch (Exception ex)
		{
			pstmt = null;
		}
		return businessPartners;
	}	//	getByName
}
