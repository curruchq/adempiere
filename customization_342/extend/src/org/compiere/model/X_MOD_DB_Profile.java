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

/** Generated Model for MOD_DB_Profile
 *  @author Adempiere (generated) 
 *  @version 3.4.2s+P20090916 - $Id$ */
public class X_MOD_DB_Profile extends PO implements I_MOD_DB_Profile, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

    /** Standard Constructor */
    public X_MOD_DB_Profile (Properties ctx, int MOD_DB_Profile_ID, String trxName)
    {
      super (ctx, MOD_DB_Profile_ID, trxName);
      /** if (MOD_DB_Profile_ID == 0)
        {
			setDB_HOST (null);
			setDB_PASSWORD (null);
			setDB_PORT (0);
			setDB_SCHEMA (null);
			setDB_USERNAME (null);
			setMOD_DB_Profile_ID (0);
			setValue (null);
        } */
    }

    /** Load Constructor */
    public X_MOD_DB_Profile (Properties ctx, ResultSet rs, String trxName)
    {
      super (ctx, rs, trxName);
    }

    /** AccessLevel
      * @return 4 - System 
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
      StringBuffer sb = new StringBuffer ("X_MOD_DB_Profile[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	/** Set DB Host.
		@param DB_HOST DB Host	  */
	public void setDB_HOST (String DB_HOST)
	{
		if (DB_HOST == null)
			throw new IllegalArgumentException ("DB_HOST is mandatory.");

		if (DB_HOST.length() > 100)
		{
			log.warning("Length > 100 - truncated");
			DB_HOST = DB_HOST.substring(0, 100);
		}
		set_Value (COLUMNNAME_DB_HOST, DB_HOST);
	}

	/** Get DB Host.
		@return DB Host	  */
	public String getDB_HOST () 
	{
		return (String)get_Value(COLUMNNAME_DB_HOST);
	}

	/** Set DB Password.
		@param DB_PASSWORD DB Password	  */
	public void setDB_PASSWORD (String DB_PASSWORD)
	{
		if (DB_PASSWORD == null)
			throw new IllegalArgumentException ("DB_PASSWORD is mandatory.");

		if (DB_PASSWORD.length() > 100)
		{
			log.warning("Length > 100 - truncated");
			DB_PASSWORD = DB_PASSWORD.substring(0, 100);
		}
		set_Value (COLUMNNAME_DB_PASSWORD, DB_PASSWORD);
	}

	/** Get DB Password.
		@return DB Password	  */
	public String getDB_PASSWORD () 
	{
		return (String)get_Value(COLUMNNAME_DB_PASSWORD);
	}

	/** Set DB Port.
		@param DB_PORT DB Port	  */
	public void setDB_PORT (int DB_PORT)
	{
		set_Value (COLUMNNAME_DB_PORT, Integer.valueOf(DB_PORT));
	}

	/** Get DB Port.
		@return DB Port	  */
	public int getDB_PORT () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_DB_PORT);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set DB Schema.
		@param DB_SCHEMA DB Schema	  */
	public void setDB_SCHEMA (String DB_SCHEMA)
	{
		if (DB_SCHEMA == null)
			throw new IllegalArgumentException ("DB_SCHEMA is mandatory.");

		if (DB_SCHEMA.length() > 100)
		{
			log.warning("Length > 100 - truncated");
			DB_SCHEMA = DB_SCHEMA.substring(0, 100);
		}
		set_Value (COLUMNNAME_DB_SCHEMA, DB_SCHEMA);
	}

	/** Get DB Schema.
		@return DB Schema	  */
	public String getDB_SCHEMA () 
	{
		return (String)get_Value(COLUMNNAME_DB_SCHEMA);
	}

	/** Set DB Username.
		@param DB_USERNAME DB Username	  */
	public void setDB_USERNAME (String DB_USERNAME)
	{
		if (DB_USERNAME == null)
			throw new IllegalArgumentException ("DB_USERNAME is mandatory.");

		if (DB_USERNAME.length() > 100)
		{
			log.warning("Length > 100 - truncated");
			DB_USERNAME = DB_USERNAME.substring(0, 100);
		}
		set_Value (COLUMNNAME_DB_USERNAME, DB_USERNAME);
	}

	/** Get DB Username.
		@return DB Username	  */
	public String getDB_USERNAME () 
	{
		return (String)get_Value(COLUMNNAME_DB_USERNAME);
	}

	/** Set DB Profile.
		@param MOD_DB_Profile_ID DB Profile	  */
	public void setMOD_DB_Profile_ID (int MOD_DB_Profile_ID)
	{
		if (MOD_DB_Profile_ID < 1)
			 throw new IllegalArgumentException ("MOD_DB_Profile_ID is mandatory.");
		set_ValueNoCheck (COLUMNNAME_MOD_DB_Profile_ID, Integer.valueOf(MOD_DB_Profile_ID));
	}

	/** Get DB Profile.
		@return DB Profile	  */
	public int getMOD_DB_Profile_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_MOD_DB_Profile_ID);
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