/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2007 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
/** Generated Model - DO NOT CHANGE */
package org.compiere.model;

import java.sql.ResultSet;
import java.util.Properties;
import org.compiere.model.*;

/** Generated Model for MOD_DIDX_Account
 *  @author Adempiere (generated) 
 *  @version 3.4.2s+P20090827 - $Id$ */
public class X_MOD_DIDX_Account extends PO implements I_MOD_DIDX_Account, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

    /** Standard Constructor */
    public X_MOD_DIDX_Account (Properties ctx, int MOD_DIDX_Account_ID, String trxName)
    {
      super (ctx, MOD_DIDX_Account_ID, trxName);
      /** if (MOD_DIDX_Account_ID == 0)
        {
			setDIDX_PASSWORD (null);
			setDIDX_USERID (null);
			setMAX_VENDOR_RATING (0);
			setMIN_VENDOR_RATING (0);
			setMOD_DIDX_Account_ID (0);
			setValue (null);
        } */
    }

    /** Load Constructor */
    public X_MOD_DIDX_Account (Properties ctx, ResultSet rs, String trxName)
    {
      super (ctx, rs, trxName);
    }

    /** AccessLevel
      * @return 3 - Client - Org 
      */
    protected int get_AccessLevel()
    {
      return accessLevel.intValue();
    }

    /** Load Meta Data */
    protected POInfo initPO (Properties ctx)
    {
      POInfo poi = POInfo.getPOInfo (ctx, Table_ID, get_TrxName());
      return poi;
    }

    public String toString()
    {
      StringBuffer sb = new StringBuffer ("X_MOD_DIDX_Account[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	/** Set DIDx Password.
		@param DIDX_PASSWORD DIDx Password	  */
	public void setDIDX_PASSWORD (String DIDX_PASSWORD)
	{
		if (DIDX_PASSWORD == null)
			throw new IllegalArgumentException ("DIDX_PASSWORD is mandatory.");

		if (DIDX_PASSWORD.length() > 100)
		{
			log.warning("Length > 100 - truncated");
			DIDX_PASSWORD = DIDX_PASSWORD.substring(0, 100);
		}
		set_Value (COLUMNNAME_DIDX_PASSWORD, DIDX_PASSWORD);
	}

	/** Get DIDx Password.
		@return DIDx Password	  */
	public String getDIDX_PASSWORD () 
	{
		return (String)get_Value(COLUMNNAME_DIDX_PASSWORD);
	}

	/** Set DIDx User Id.
		@param DIDX_USERID DIDx User Id	  */
	public void setDIDX_USERID (String DIDX_USERID)
	{
		if (DIDX_USERID == null)
			throw new IllegalArgumentException ("DIDX_USERID is mandatory.");

		if (DIDX_USERID.length() > 100)
		{
			log.warning("Length > 100 - truncated");
			DIDX_USERID = DIDX_USERID.substring(0, 100);
		}
		set_Value (COLUMNNAME_DIDX_USERID, DIDX_USERID);
	}

	/** Get DIDx User Id.
		@return DIDx User Id	  */
	public String getDIDX_USERID () 
	{
		return (String)get_Value(COLUMNNAME_DIDX_USERID);
	}

	/** Set Max Vendor Rating.
		@param MAX_VENDOR_RATING Max Vendor Rating	  */
	public void setMAX_VENDOR_RATING (int MAX_VENDOR_RATING)
	{
		set_Value (COLUMNNAME_MAX_VENDOR_RATING, Integer.valueOf(MAX_VENDOR_RATING));
	}

	/** Get Max Vendor Rating.
		@return Max Vendor Rating	  */
	public int getMAX_VENDOR_RATING () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_MAX_VENDOR_RATING);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Min Vendor Rating.
		@param MIN_VENDOR_RATING Min Vendor Rating	  */
	public void setMIN_VENDOR_RATING (int MIN_VENDOR_RATING)
	{
		set_Value (COLUMNNAME_MIN_VENDOR_RATING, Integer.valueOf(MIN_VENDOR_RATING));
	}

	/** Get Min Vendor Rating.
		@return Min Vendor Rating	  */
	public int getMIN_VENDOR_RATING () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_MIN_VENDOR_RATING);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set DIDx Account.
		@param MOD_DIDX_Account_ID DIDx Account	  */
	public void setMOD_DIDX_Account_ID (int MOD_DIDX_Account_ID)
	{
		if (MOD_DIDX_Account_ID < 1)
			 throw new IllegalArgumentException ("MOD_DIDX_Account_ID is mandatory.");
		set_ValueNoCheck (COLUMNNAME_MOD_DIDX_Account_ID, Integer.valueOf(MOD_DIDX_Account_ID));
	}

	/** Get DIDx Account.
		@return DIDx Account	  */
	public int getMOD_DIDX_Account_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_MOD_DIDX_Account_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Search Key.
		@param Value 
		Search key for the record in the format required - must be unique
	  */
	public void setValue (String Value)
	{
		if (Value == null)
			throw new IllegalArgumentException ("Value is mandatory.");

		if (Value.length() > 100)
		{
			log.warning("Length > 100 - truncated");
			Value = Value.substring(0, 100);
		}
		set_Value (COLUMNNAME_Value, Value);
	}

	/** Get Search Key.
		@return Search key for the record in the format required - must be unique
	  */
	public String getValue () 
	{
		return (String)get_Value(COLUMNNAME_Value);
	}
}