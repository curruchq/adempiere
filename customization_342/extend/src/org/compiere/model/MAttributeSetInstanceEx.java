package org.compiere.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Properties;

import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;

public class MAttributeSetInstanceEx 
{
	/** Logger										*/
	private static CLogger log = CLogger.getCLogger(MAttributeSetInstanceEx.class);
	
	public static MAttributeSetInstance[] getAttributeSetInstances(Properties ctx, int M_AttributeSet_ID, String trxName)
	{
		ArrayList<MAttributeSetInstance> allAttributeSetInstances = new ArrayList<MAttributeSetInstance>();
		
		String sql = "SELECT * FROM M_AttributeSetInstance WHERE M_AttributeSet_ID = ? AND AD_Client_ID = ? AND AD_Org_ID = ? AND IsActive='Y'";		
		
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement (sql, null);
			pstmt.setInt (1, M_AttributeSet_ID);
			pstmt.setInt (2, Env.getAD_Client_ID(ctx));
			pstmt.setInt (3, Env.getAD_Org_ID(ctx));
			
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next())
			{
				MAttributeSetInstance attributeSetInstace = new MAttributeSetInstance(ctx, rs, trxName);
				allAttributeSetInstances.add(attributeSetInstace);
			}
			rs.close ();
			pstmt.close ();
			pstmt = null;
		} 
		catch (Exception ex)
		{
			log.severe(ex.toString());
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
		
		MAttributeSetInstance[] retValue = new MAttributeSetInstance[allAttributeSetInstances.size()];
		allAttributeSetInstances.toArray(retValue);
		
		return retValue;
	}
	
}
