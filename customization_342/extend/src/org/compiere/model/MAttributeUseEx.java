package org.compiere.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;

public class MAttributeUseEx
{
	/** Logger										*/
	private static CLogger log = CLogger.getCLogger(MAttributeUseEx.class);
			
	private static final int SEQ_NO_GAP = 10;
	
	public static int getNextSeqNo(Properties ctx, int M_AttributeSet_ID)
	{
		int maxSeqNo = 0;
		String sql = "SELECT MAX(SEQNO) FROM M_AttributeUse WHERE M_AttributeSet_ID = ? AND AD_Client_ID = ? AND AD_Org_ID = ? AND IsActive='Y'";		
		
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement (sql, null);
			pstmt.setInt (1, M_AttributeSet_ID);
			pstmt.setInt (2, Env.getAD_Client_ID(ctx));
			pstmt.setInt (3, Env.getAD_Org_ID(ctx));
			
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next())
				maxSeqNo = rs.getInt(1);
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
		
		// Increment sequence numbering
		maxSeqNo += SEQ_NO_GAP;
		
		return maxSeqNo;
	}
}
