package org.compiere.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;

import org.compiere.util.CLogger;
import org.compiere.util.DB;

public class MUserEx extends MUser
{
	/**	Static Logger			*/
	private static CLogger	s_log	= CLogger.getCLogger(MUserEx.class);
	
	public MUserEx(Properties ctx, int AD_User_ID, String trxName)
	{
		super (ctx, AD_User_ID, trxName);	
	}
	
	/**
	 * Get actives users matching a specific email (JH)
	 * @param ctx context
	 * @param email of user
	 * @return array of users
	 */
	public static MUser[] getUsersByEmail(Properties ctx, String email)
	{
		if (email == null || email.length() < 1)
			return new MUser[]{};
		
		ArrayList<MUser> list = new ArrayList<MUser>();
		String sql = "SELECT * FROM AD_User WHERE UPPER(Email) LIKE UPPER(?) AND IsActive='Y'";
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement (sql, null);
			pstmt.setString (1, email);
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next ())
				list.add(new MUser(ctx, rs, null));
			rs.close ();
			pstmt.close ();
			pstmt = null;
		} 
		catch (Exception e)
		{
			s_log.log(Level.SEVERE, sql, e);
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
		
		MUser[] retValue = new MUser[list.size()];
		list.toArray (retValue);
		return retValue;
	} 	//  getUsers
	
	/**
	 * Get actives users matching a specific name (JH)
	 * @param ctx context
	 * @param name of user
	 * @return array of users
	 */
	public static MUser[] getUsersByName(Properties ctx, String name)
	{
		if (name == null)
			name = "";
		
		ArrayList<MUser> list = new ArrayList<MUser>();
		String sql = "SELECT * FROM AD_User WHERE UPPER(Name) LIKE UPPER(?) AND IsActive='Y'";
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement (sql, null);
			pstmt.setString (1, "%" + name + "%");
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next ())
				list.add(new MUser(ctx, rs, null));
			rs.close ();
			pstmt.close ();
			pstmt = null;
		} 
		catch (Exception e)
		{
			s_log.log(Level.SEVERE, sql, e);
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
		
		MUser[] retValue = new MUser[list.size ()];
		list.toArray (retValue);
		return retValue;
	} 	//  getUsersByName
	
	/**
	 * 	Get User (cached)
	 * 	Also loads Admninistrator (0)
	 *	@param ctx context
	 *	@param AD_User_ID id
	 *  @param cache use cache
	 *	@return user
	 */
	public static MUser getIgnoreCache (Properties ctx, int AD_User_ID)
	{
		MUser retValue = new MUser (ctx, AD_User_ID, null);
		if (AD_User_ID == 0)
		{
			String trxName = null;
			retValue.load(trxName);	//	load System Record
		}
		
		return retValue;
	}	//	get
}
