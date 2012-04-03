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

/** Generated Model for MOD_DIDX_Country
 *  @author Adempiere (generated) 
 *  @version 3.4.2s+P20090827 - $Id$ */
public class X_MOD_DIDX_Country extends PO implements I_MOD_DIDX_Country, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

    /** Standard Constructor */
    public X_MOD_DIDX_Country (Properties ctx, int MOD_DIDX_Country_ID, String trxName)
    {
      super (ctx, MOD_DIDX_Country_ID, trxName);
      /** if (MOD_DIDX_Country_ID == 0)
        {
			setDIDX_COUNTRYID (0);
			setDIDX_COUNTRY_CODE (0);
			setDIDX_COUNTRY_NAME (null);
			setDIDX_SEARCH (false);
			setMOD_DIDX_Account_ID (0);
			setMOD_DIDX_Country_ID (0);
        } */
    }

    /** Load Constructor */
    public X_MOD_DIDX_Country (Properties ctx, ResultSet rs, String trxName)
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
      StringBuffer sb = new StringBuffer ("X_MOD_DIDX_Country[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	/** Set DIDx Country Id.
		@param DIDX_COUNTRYID DIDx Country Id	  */
	public void setDIDX_COUNTRYID (int DIDX_COUNTRYID)
	{
		set_Value (COLUMNNAME_DIDX_COUNTRYID, Integer.valueOf(DIDX_COUNTRYID));
	}

	/** Get DIDx Country Id.
		@return DIDx Country Id	  */
	public int getDIDX_COUNTRYID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_DIDX_COUNTRYID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set DIDx Country Code.
		@param DIDX_COUNTRY_CODE DIDx Country Code	  */
	public void setDIDX_COUNTRY_CODE (int DIDX_COUNTRY_CODE)
	{
		set_Value (COLUMNNAME_DIDX_COUNTRY_CODE, Integer.valueOf(DIDX_COUNTRY_CODE));
	}

	/** Get DIDx Country Code.
		@return DIDx Country Code	  */
	public int getDIDX_COUNTRY_CODE () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_DIDX_COUNTRY_CODE);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set DIDx Country Name.
		@param DIDX_COUNTRY_NAME DIDx Country Name	  */
	public void setDIDX_COUNTRY_NAME (String DIDX_COUNTRY_NAME)
	{
		if (DIDX_COUNTRY_NAME == null)
			throw new IllegalArgumentException ("DIDX_COUNTRY_NAME is mandatory.");

		if (DIDX_COUNTRY_NAME.length() > 100)
		{
			log.warning("Length > 100 - truncated");
			DIDX_COUNTRY_NAME = DIDX_COUNTRY_NAME.substring(0, 100);
		}
		set_Value (COLUMNNAME_DIDX_COUNTRY_NAME, DIDX_COUNTRY_NAME);
	}

	/** Get DIDx Country Name.
		@return DIDx Country Name	  */
	public String getDIDX_COUNTRY_NAME () 
	{
		return (String)get_Value(COLUMNNAME_DIDX_COUNTRY_NAME);
	}

	/** Set DIDx Search.
		@param DIDX_SEARCH DIDx Search	  */
	public void setDIDX_SEARCH (boolean DIDX_SEARCH)
	{
		set_Value (COLUMNNAME_DIDX_SEARCH, Boolean.valueOf(DIDX_SEARCH));
	}

	/** Get DIDx Search.
		@return DIDx Search	  */
	public boolean isDIDX_SEARCH () 
	{
		Object oo = get_Value(COLUMNNAME_DIDX_SEARCH);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** MOD_DIDX_Account_ID AD_Reference_ID=1000000 */
	public static final int MOD_DIDX_ACCOUNT_ID_AD_Reference_ID=1000000;
	/** Set DIDx Account.
		@param MOD_DIDX_Account_ID DIDx Account	  */
	public void setMOD_DIDX_Account_ID (int MOD_DIDX_Account_ID)
	{
		if (MOD_DIDX_Account_ID < 1)
			 throw new IllegalArgumentException ("MOD_DIDX_Account_ID is mandatory.");
		set_Value (COLUMNNAME_MOD_DIDX_Account_ID, Integer.valueOf(MOD_DIDX_Account_ID));
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

	/** Set DIDx Country.
		@param MOD_DIDX_Country_ID DIDx Country	  */
	public void setMOD_DIDX_Country_ID (int MOD_DIDX_Country_ID)
	{
		if (MOD_DIDX_Country_ID < 1)
			 throw new IllegalArgumentException ("MOD_DIDX_Country_ID is mandatory.");
		set_ValueNoCheck (COLUMNNAME_MOD_DIDX_Country_ID, Integer.valueOf(MOD_DIDX_Country_ID));
	}

	/** Get DIDx Country.
		@return DIDx Country	  */
	public int getMOD_DIDX_Country_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_MOD_DIDX_Country_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}
}