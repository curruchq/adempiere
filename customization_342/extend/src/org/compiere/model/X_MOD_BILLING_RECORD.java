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

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Properties;
import org.compiere.util.Env;

/** Generated Model for MOD_BILLING_RECORD
 *  @author Adempiere (generated) 
 *  @version Release 3.4.0s - $Id$ */
public class X_MOD_BILLING_RECORD extends PO implements I_MOD_BILLING_RECORD, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

    /** Standard Constructor */
    public X_MOD_BILLING_RECORD (Properties ctx, int MOD_BILLING_RECORD_ID, String trxName)
    {
      super (ctx, MOD_BILLING_RECORD_ID, trxName);
      /** if (MOD_BILLING_RECORD_ID == 0)
        {
			setMOD_BILLING_RECORD_ID (0);
			setRADACCTID (0);
        } */
    }

    /** Load Constructor */
    public X_MOD_BILLING_RECORD (Properties ctx, ResultSet rs, String trxName)
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
      StringBuffer sb = new StringBuffer ("X_MOD_BILLING_RECORD[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	/** Set ACCTAUTHENTIC.
		@param ACCTAUTHENTIC ACCTAUTHENTIC	  */
	public void setACCTAUTHENTIC (String ACCTAUTHENTIC)
	{

		if (ACCTAUTHENTIC != null && ACCTAUTHENTIC.length() > 200)
		{
			log.warning("Length > 200 - truncated");
			ACCTAUTHENTIC = ACCTAUTHENTIC.substring(0, 200);
		}
		set_Value (COLUMNNAME_ACCTAUTHENTIC, ACCTAUTHENTIC);
	}

	/** Get ACCTAUTHENTIC.
		@return ACCTAUTHENTIC	  */
	public String getACCTAUTHENTIC () 
	{
		return (String)get_Value(COLUMNNAME_ACCTAUTHENTIC);
	}

	/** Set ACCTINPUTOCTETS.
		@param ACCTINPUTOCTETS ACCTINPUTOCTETS	  */
	public void setACCTINPUTOCTETS (String ACCTINPUTOCTETS)
	{

		if (ACCTINPUTOCTETS != null && ACCTINPUTOCTETS.length() > 200)
		{
			log.warning("Length > 200 - truncated");
			ACCTINPUTOCTETS = ACCTINPUTOCTETS.substring(0, 200);
		}
		set_Value (COLUMNNAME_ACCTINPUTOCTETS, ACCTINPUTOCTETS);
	}

	/** Get ACCTINPUTOCTETS.
		@return ACCTINPUTOCTETS	  */
	public String getACCTINPUTOCTETS () 
	{
		return (String)get_Value(COLUMNNAME_ACCTINPUTOCTETS);
	}

	/** Set ACCTOUTPUTOCTETS.
		@param ACCTOUTPUTOCTETS ACCTOUTPUTOCTETS	  */
	public void setACCTOUTPUTOCTETS (String ACCTOUTPUTOCTETS)
	{

		if (ACCTOUTPUTOCTETS != null && ACCTOUTPUTOCTETS.length() > 200)
		{
			log.warning("Length > 200 - truncated");
			ACCTOUTPUTOCTETS = ACCTOUTPUTOCTETS.substring(0, 200);
		}
		set_Value (COLUMNNAME_ACCTOUTPUTOCTETS, ACCTOUTPUTOCTETS);
	}

	/** Get ACCTOUTPUTOCTETS.
		@return ACCTOUTPUTOCTETS	  */
	public String getACCTOUTPUTOCTETS () 
	{
		return (String)get_Value(COLUMNNAME_ACCTOUTPUTOCTETS);
	}

	/** Set ACCTSESSIONID.
		@param ACCTSESSIONID ACCTSESSIONID	  */
	public void setACCTSESSIONID (String ACCTSESSIONID)
	{

		if (ACCTSESSIONID != null && ACCTSESSIONID.length() > 200)
		{
			log.warning("Length > 200 - truncated");
			ACCTSESSIONID = ACCTSESSIONID.substring(0, 200);
		}
		set_Value (COLUMNNAME_ACCTSESSIONID, ACCTSESSIONID);
	}

	/** Get ACCTSESSIONID.
		@return ACCTSESSIONID	  */
	public String getACCTSESSIONID () 
	{
		return (String)get_Value(COLUMNNAME_ACCTSESSIONID);
	}

	/** Set ACCTSESSIONTIME.
		@param ACCTSESSIONTIME ACCTSESSIONTIME	  */
	public void setACCTSESSIONTIME (BigDecimal ACCTSESSIONTIME)
	{
		set_Value (COLUMNNAME_ACCTSESSIONTIME, ACCTSESSIONTIME);
	}

	/** Get ACCTSESSIONTIME.
		@return ACCTSESSIONTIME	  */
	public BigDecimal getACCTSESSIONTIME () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_ACCTSESSIONTIME);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set ACCTSTARTDELAY.
		@param ACCTSTARTDELAY ACCTSTARTDELAY	  */
	public void setACCTSTARTDELAY (String ACCTSTARTDELAY)
	{

		if (ACCTSTARTDELAY != null && ACCTSTARTDELAY.length() > 200)
		{
			log.warning("Length > 200 - truncated");
			ACCTSTARTDELAY = ACCTSTARTDELAY.substring(0, 200);
		}
		set_Value (COLUMNNAME_ACCTSTARTDELAY, ACCTSTARTDELAY);
	}

	/** Get ACCTSTARTDELAY.
		@return ACCTSTARTDELAY	  */
	public String getACCTSTARTDELAY () 
	{
		return (String)get_Value(COLUMNNAME_ACCTSTARTDELAY);
	}

	/** Set ACCTSTARTTIME.
		@param ACCTSTARTTIME ACCTSTARTTIME	  */
	public void setACCTSTARTTIME (Timestamp ACCTSTARTTIME)
	{
		set_Value (COLUMNNAME_ACCTSTARTTIME, ACCTSTARTTIME);
	}

	/** Get ACCTSTARTTIME.
		@return ACCTSTARTTIME	  */
	public Timestamp getACCTSTARTTIME () 
	{
		return (Timestamp)get_Value(COLUMNNAME_ACCTSTARTTIME);
	}

	/** Set ACCTSTOPDELAY.
		@param ACCTSTOPDELAY ACCTSTOPDELAY	  */
	public void setACCTSTOPDELAY (String ACCTSTOPDELAY)
	{

		if (ACCTSTOPDELAY != null && ACCTSTOPDELAY.length() > 200)
		{
			log.warning("Length > 200 - truncated");
			ACCTSTOPDELAY = ACCTSTOPDELAY.substring(0, 200);
		}
		set_Value (COLUMNNAME_ACCTSTOPDELAY, ACCTSTOPDELAY);
	}

	/** Get ACCTSTOPDELAY.
		@return ACCTSTOPDELAY	  */
	public String getACCTSTOPDELAY () 
	{
		return (String)get_Value(COLUMNNAME_ACCTSTOPDELAY);
	}

	/** Set ACCTSTOPTIME.
		@param ACCTSTOPTIME ACCTSTOPTIME	  */
	public void setACCTSTOPTIME (Timestamp ACCTSTOPTIME)
	{
		set_Value (COLUMNNAME_ACCTSTOPTIME, ACCTSTOPTIME);
	}

	/** Get ACCTSTOPTIME.
		@return ACCTSTOPTIME	  */
	public Timestamp getACCTSTOPTIME () 
	{
		return (Timestamp)get_Value(COLUMNNAME_ACCTSTOPTIME);
	}

	/** Set ACCTTERMINATECAUSE.
		@param ACCTTERMINATECAUSE ACCTTERMINATECAUSE	  */
	public void setACCTTERMINATECAUSE (String ACCTTERMINATECAUSE)
	{

		if (ACCTTERMINATECAUSE != null && ACCTTERMINATECAUSE.length() > 200)
		{
			log.warning("Length > 200 - truncated");
			ACCTTERMINATECAUSE = ACCTTERMINATECAUSE.substring(0, 200);
		}
		set_Value (COLUMNNAME_ACCTTERMINATECAUSE, ACCTTERMINATECAUSE);
	}

	/** Get ACCTTERMINATECAUSE.
		@return ACCTTERMINATECAUSE	  */
	public String getACCTTERMINATECAUSE () 
	{
		return (String)get_Value(COLUMNNAME_ACCTTERMINATECAUSE);
	}

	/** Set ACCTUNIQUEID.
		@param ACCTUNIQUEID ACCTUNIQUEID	  */
	public void setACCTUNIQUEID (String ACCTUNIQUEID)
	{

		if (ACCTUNIQUEID != null && ACCTUNIQUEID.length() > 200)
		{
			log.warning("Length > 200 - truncated");
			ACCTUNIQUEID = ACCTUNIQUEID.substring(0, 200);
		}
		set_Value (COLUMNNAME_ACCTUNIQUEID, ACCTUNIQUEID);
	}

	/** Get ACCTUNIQUEID.
		@return ACCTUNIQUEID	  */
	public String getACCTUNIQUEID () 
	{
		return (String)get_Value(COLUMNNAME_ACCTUNIQUEID);
	}

	/** Set BILLINGID.
		@param BILLINGID BILLINGID	  */
	public void setBILLINGID (String BILLINGID)
	{

		if (BILLINGID != null && BILLINGID.length() > 200)
		{
			log.warning("Length > 200 - truncated");
			BILLINGID = BILLINGID.substring(0, 200);
		}
		set_Value (COLUMNNAME_BILLINGID, BILLINGID);
	}

	/** Get BILLINGID.
		@return BILLINGID	  */
	public String getBILLINGID () 
	{
		return (String)get_Value(COLUMNNAME_BILLINGID);
	}

	/** Set CALLEDSTATIONID.
		@param CALLEDSTATIONID CALLEDSTATIONID	  */
	public void setCALLEDSTATIONID (String CALLEDSTATIONID)
	{

		if (CALLEDSTATIONID != null && CALLEDSTATIONID.length() > 200)
		{
			log.warning("Length > 200 - truncated");
			CALLEDSTATIONID = CALLEDSTATIONID.substring(0, 200);
		}
		set_Value (COLUMNNAME_CALLEDSTATIONID, CALLEDSTATIONID);
	}

	/** Get CALLEDSTATIONID.
		@return CALLEDSTATIONID	  */
	public String getCALLEDSTATIONID () 
	{
		return (String)get_Value(COLUMNNAME_CALLEDSTATIONID);
	}

	/** Set CALLINGSTATIONID.
		@param CALLINGSTATIONID CALLINGSTATIONID	  */
	public void setCALLINGSTATIONID (String CALLINGSTATIONID)
	{

		if (CALLINGSTATIONID != null && CALLINGSTATIONID.length() > 200)
		{
			log.warning("Length > 200 - truncated");
			CALLINGSTATIONID = CALLINGSTATIONID.substring(0, 200);
		}
		set_Value (COLUMNNAME_CALLINGSTATIONID, CALLINGSTATIONID);
	}

	/** Get CALLINGSTATIONID.
		@return CALLINGSTATIONID	  */
	public String getCALLINGSTATIONID () 
	{
		return (String)get_Value(COLUMNNAME_CALLINGSTATIONID);
	}

	/** Set CANONICALURI.
		@param CANONICALURI CANONICALURI	  */
	public void setCANONICALURI (String CANONICALURI)
	{

		if (CANONICALURI != null && CANONICALURI.length() > 200)
		{
			log.warning("Length > 200 - truncated");
			CANONICALURI = CANONICALURI.substring(0, 200);
		}
		set_Value (COLUMNNAME_CANONICALURI, CANONICALURI);
	}

	/** Get CANONICALURI.
		@return CANONICALURI	  */
	public String getCANONICALURI () 
	{
		return (String)get_Value(COLUMNNAME_CANONICALURI);
	}

	/** Set CONNECTINFO_START.
		@param CONNECTINFO_START CONNECTINFO_START	  */
	public void setCONNECTINFO_START (String CONNECTINFO_START)
	{

		if (CONNECTINFO_START != null && CONNECTINFO_START.length() > 30)
		{
			log.warning("Length > 30 - truncated");
			CONNECTINFO_START = CONNECTINFO_START.substring(0, 30);
		}
		set_Value (COLUMNNAME_CONNECTINFO_START, CONNECTINFO_START);
	}

	/** Get CONNECTINFO_START.
		@return CONNECTINFO_START	  */
	public String getCONNECTINFO_START () 
	{
		return (String)get_Value(COLUMNNAME_CONNECTINFO_START);
	}

	/** Set CONNECTINFO_STOP.
		@param CONNECTINFO_STOP CONNECTINFO_STOP	  */
	public void setCONNECTINFO_STOP (String CONNECTINFO_STOP)
	{

		if (CONNECTINFO_STOP != null && CONNECTINFO_STOP.length() > 30)
		{
			log.warning("Length > 30 - truncated");
			CONNECTINFO_STOP = CONNECTINFO_STOP.substring(0, 30);
		}
		set_Value (COLUMNNAME_CONNECTINFO_STOP, CONNECTINFO_STOP);
	}

	/** Get CONNECTINFO_STOP.
		@return CONNECTINFO_STOP	  */
	public String getCONNECTINFO_STOP () 
	{
		return (String)get_Value(COLUMNNAME_CONNECTINFO_STOP);
	}

	/** Set CONTACT.
		@param CONTACT CONTACT	  */
	public void setCONTACT (String CONTACT)
	{

		if (CONTACT != null && CONTACT.length() > 200)
		{
			log.warning("Length > 200 - truncated");
			CONTACT = CONTACT.substring(0, 200);
		}
		set_Value (COLUMNNAME_CONTACT, CONTACT);
	}

	/** Get CONTACT.
		@return CONTACT	  */
	public String getCONTACT () 
	{
		return (String)get_Value(COLUMNNAME_CONTACT);
	}

	/** Set DELAYTIME.
		@param DELAYTIME DELAYTIME	  */
	public void setDELAYTIME (String DELAYTIME)
	{

		if (DELAYTIME != null && DELAYTIME.length() > 200)
		{
			log.warning("Length > 200 - truncated");
			DELAYTIME = DELAYTIME.substring(0, 200);
		}
		set_Value (COLUMNNAME_DELAYTIME, DELAYTIME);
	}

	/** Get DELAYTIME.
		@return DELAYTIME	  */
	public String getDELAYTIME () 
	{
		return (String)get_Value(COLUMNNAME_DELAYTIME);
	}

	/** Set DESTINATIONID.
		@param DESTINATIONID DESTINATIONID	  */
	public void setDESTINATIONID (String DESTINATIONID)
	{

		if (DESTINATIONID != null && DESTINATIONID.length() > 200)
		{
			log.warning("Length > 200 - truncated");
			DESTINATIONID = DESTINATIONID.substring(0, 200);
		}
		set_Value (COLUMNNAME_DESTINATIONID, DESTINATIONID);
	}

	/** Get DESTINATIONID.
		@return DESTINATIONID	  */
	public String getDESTINATIONID () 
	{
		return (String)get_Value(COLUMNNAME_DESTINATIONID);
	}

	/** Set FRAMEDIPADDRESS.
		@param FRAMEDIPADDRESS FRAMEDIPADDRESS	  */
	public void setFRAMEDIPADDRESS (String FRAMEDIPADDRESS)
	{

		if (FRAMEDIPADDRESS != null && FRAMEDIPADDRESS.length() > 200)
		{
			log.warning("Length > 200 - truncated");
			FRAMEDIPADDRESS = FRAMEDIPADDRESS.substring(0, 200);
		}
		set_Value (COLUMNNAME_FRAMEDIPADDRESS, FRAMEDIPADDRESS);
	}

	/** Get FRAMEDIPADDRESS.
		@return FRAMEDIPADDRESS	  */
	public String getFRAMEDIPADDRESS () 
	{
		return (String)get_Value(COLUMNNAME_FRAMEDIPADDRESS);
	}

	/** Set FRAMEDPROTOCOL.
		@param FRAMEDPROTOCOL FRAMEDPROTOCOL	  */
	public void setFRAMEDPROTOCOL (String FRAMEDPROTOCOL)
	{

		if (FRAMEDPROTOCOL != null && FRAMEDPROTOCOL.length() > 200)
		{
			log.warning("Length > 200 - truncated");
			FRAMEDPROTOCOL = FRAMEDPROTOCOL.substring(0, 200);
		}
		set_Value (COLUMNNAME_FRAMEDPROTOCOL, FRAMEDPROTOCOL);
	}

	/** Get FRAMEDPROTOCOL.
		@return FRAMEDPROTOCOL	  */
	public String getFRAMEDPROTOCOL () 
	{
		return (String)get_Value(COLUMNNAME_FRAMEDPROTOCOL);
	}

	/** Set FROMHEADER.
		@param FROMHEADER FROMHEADER	  */
	public void setFROMHEADER (String FROMHEADER)
	{

		if (FROMHEADER != null && FROMHEADER.length() > 200)
		{
			log.warning("Length > 200 - truncated");
			FROMHEADER = FROMHEADER.substring(0, 200);
		}
		set_Value (COLUMNNAME_FROMHEADER, FROMHEADER);
	}

	/** Get FROMHEADER.
		@return FROMHEADER	  */
	public String getFROMHEADER () 
	{
		return (String)get_Value(COLUMNNAME_FROMHEADER);
	}

	/** Set LINEDESC.
		@param LINEDESC LINEDESC	  */
	public void setLINEDESC (String LINEDESC)
	{

		if (LINEDESC != null && LINEDESC.length() > 100)
		{
			log.warning("Length > 100 - truncated");
			LINEDESC = LINEDESC.substring(0, 100);
		}
		set_Value (COLUMNNAME_LINEDESC, LINEDESC);
	}

	/** Get LINEDESC.
		@return LINEDESC	  */
	public String getLINEDESC () 
	{
		return (String)get_Value(COLUMNNAME_LINEDESC);
	}

	/** Set MEDIAINFO.
		@param MEDIAINFO MEDIAINFO	  */
	public void setMEDIAINFO (String MEDIAINFO)
	{

		if (MEDIAINFO != null && MEDIAINFO.length() > 200)
		{
			log.warning("Length > 200 - truncated");
			MEDIAINFO = MEDIAINFO.substring(0, 200);
		}
		set_Value (COLUMNNAME_MEDIAINFO, MEDIAINFO);
	}

	/** Get MEDIAINFO.
		@return MEDIAINFO	  */
	public String getMEDIAINFO () 
	{
		return (String)get_Value(COLUMNNAME_MEDIAINFO);
	}

	/** Set MOD Billing Record.
		@param MOD_BILLING_RECORD_ID MOD Billing Record	  */
	public void setMOD_BILLING_RECORD_ID (int MOD_BILLING_RECORD_ID)
	{
		if (MOD_BILLING_RECORD_ID < 1)
			 throw new IllegalArgumentException ("MOD_BILLING_RECORD_ID is mandatory.");
		set_ValueNoCheck (COLUMNNAME_MOD_BILLING_RECORD_ID, Integer.valueOf(MOD_BILLING_RECORD_ID));
	}

	/** Get MOD Billing Record.
		@return MOD Billing Record	  */
	public int getMOD_BILLING_RECORD_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_MOD_BILLING_RECORD_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set NASIPADDRESS.
		@param NASIPADDRESS NASIPADDRESS	  */
	public void setNASIPADDRESS (String NASIPADDRESS)
	{

		if (NASIPADDRESS != null && NASIPADDRESS.length() > 200)
		{
			log.warning("Length > 200 - truncated");
			NASIPADDRESS = NASIPADDRESS.substring(0, 200);
		}
		set_Value (COLUMNNAME_NASIPADDRESS, NASIPADDRESS);
	}

	/** Get NASIPADDRESS.
		@return NASIPADDRESS	  */
	public String getNASIPADDRESS () 
	{
		return (String)get_Value(COLUMNNAME_NASIPADDRESS);
	}

	/** Set NASPORTID.
		@param NASPORTID NASPORTID	  */
	public void setNASPORTID (String NASPORTID)
	{

		if (NASPORTID != null && NASPORTID.length() > 200)
		{
			log.warning("Length > 200 - truncated");
			NASPORTID = NASPORTID.substring(0, 200);
		}
		set_Value (COLUMNNAME_NASPORTID, NASPORTID);
	}

	/** Get NASPORTID.
		@return NASPORTID	  */
	public String getNASPORTID () 
	{
		return (String)get_Value(COLUMNNAME_NASPORTID);
	}

	/** Set NASPORTTYPE.
		@param NASPORTTYPE NASPORTTYPE	  */
	public void setNASPORTTYPE (String NASPORTTYPE)
	{

		if (NASPORTTYPE != null && NASPORTTYPE.length() > 200)
		{
			log.warning("Length > 200 - truncated");
			NASPORTTYPE = NASPORTTYPE.substring(0, 200);
		}
		set_Value (COLUMNNAME_NASPORTTYPE, NASPORTTYPE);
	}

	/** Get NASPORTTYPE.
		@return NASPORTTYPE	  */
	public String getNASPORTTYPE () 
	{
		return (String)get_Value(COLUMNNAME_NASPORTTYPE);
	}

	/** Set NORMALIZED.
		@param NORMALIZED NORMALIZED	  */
	public void setNORMALIZED (String NORMALIZED)
	{

		if (NORMALIZED != null && NORMALIZED.length() > 200)
		{
			log.warning("Length > 200 - truncated");
			NORMALIZED = NORMALIZED.substring(0, 200);
		}
		set_Value (COLUMNNAME_NORMALIZED, NORMALIZED);
	}

	/** Get NORMALIZED.
		@return NORMALIZED	  */
	public String getNORMALIZED () 
	{
		return (String)get_Value(COLUMNNAME_NORMALIZED);
	}

	/** Set Price.
		@param Price 
		Price
	  */
	public void setPrice (BigDecimal Price)
	{
		set_Value (COLUMNNAME_Price, Price);
	}

	/** Get Price.
		@return Price
	  */
	public BigDecimal getPrice () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_Price);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set RADACCTID.
		@param RADACCTID RADACCTID	  */
	public void setRADACCTID (int RADACCTID)
	{
		set_Value (COLUMNNAME_RADACCTID, Integer.valueOf(RADACCTID));
	}

	/** Get RADACCTID.
		@return RADACCTID	  */
	public int getRADACCTID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_RADACCTID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set REALM.
		@param REALM REALM	  */
	public void setREALM (String REALM)
	{

		if (REALM != null && REALM.length() > 200)
		{
			log.warning("Length > 200 - truncated");
			REALM = REALM.substring(0, 200);
		}
		set_Value (COLUMNNAME_REALM, REALM);
	}

	/** Get REALM.
		@return REALM	  */
	public String getREALM () 
	{
		return (String)get_Value(COLUMNNAME_REALM);
	}

	/** Set RECORDSTATUS.
		@param RECORDSTATUS RECORDSTATUS	  */
	public void setRECORDSTATUS (String RECORDSTATUS)
	{

		if (RECORDSTATUS != null && RECORDSTATUS.length() > 10)
		{
			log.warning("Length > 10 - truncated");
			RECORDSTATUS = RECORDSTATUS.substring(0, 10);
		}
		set_Value (COLUMNNAME_RECORDSTATUS, RECORDSTATUS);
	}

	/** Get RECORDSTATUS.
		@return RECORDSTATUS	  */
	public String getRECORDSTATUS () 
	{
		return (String)get_Value(COLUMNNAME_RECORDSTATUS);
	}

	/** Set RTPSTATISTICS.
		@param RTPSTATISTICS RTPSTATISTICS	  */
	public void setRTPSTATISTICS (String RTPSTATISTICS)
	{

		if (RTPSTATISTICS != null && RTPSTATISTICS.length() > 200)
		{
			log.warning("Length > 200 - truncated");
			RTPSTATISTICS = RTPSTATISTICS.substring(0, 200);
		}
		set_Value (COLUMNNAME_RTPSTATISTICS, RTPSTATISTICS);
	}

	/** Get RTPSTATISTICS.
		@return RTPSTATISTICS	  */
	public String getRTPSTATISTICS () 
	{
		return (String)get_Value(COLUMNNAME_RTPSTATISTICS);
	}

	/** Set Rate.
		@param Rate 
		Rate or Tax or Exchange
	  */
	public void setRate (String Rate)
	{

		if (Rate != null && Rate.length() > 1000)
		{
			log.warning("Length > 1000 - truncated");
			Rate = Rate.substring(0, 1000);
		}
		set_Value (COLUMNNAME_Rate, Rate);
	}

	/** Get Rate.
		@return Rate or Tax or Exchange
	  */
	public String getRate () 
	{
		return (String)get_Value(COLUMNNAME_Rate);
	}

	/** Set SERVICETYPE.
		@param SERVICETYPE SERVICETYPE	  */
	public void setSERVICETYPE (String SERVICETYPE)
	{

		if (SERVICETYPE != null && SERVICETYPE.length() > 200)
		{
			log.warning("Length > 200 - truncated");
			SERVICETYPE = SERVICETYPE.substring(0, 200);
		}
		set_Value (COLUMNNAME_SERVICETYPE, SERVICETYPE);
	}

	/** Get SERVICETYPE.
		@return SERVICETYPE	  */
	public String getSERVICETYPE () 
	{
		return (String)get_Value(COLUMNNAME_SERVICETYPE);
	}

	/** Set SIPAPPLICATIONTYPE.
		@param SIPAPPLICATIONTYPE SIPAPPLICATIONTYPE	  */
	public void setSIPAPPLICATIONTYPE (String SIPAPPLICATIONTYPE)
	{

		if (SIPAPPLICATIONTYPE != null && SIPAPPLICATIONTYPE.length() > 200)
		{
			log.warning("Length > 200 - truncated");
			SIPAPPLICATIONTYPE = SIPAPPLICATIONTYPE.substring(0, 200);
		}
		set_Value (COLUMNNAME_SIPAPPLICATIONTYPE, SIPAPPLICATIONTYPE);
	}

	/** Get SIPAPPLICATIONTYPE.
		@return SIPAPPLICATIONTYPE	  */
	public String getSIPAPPLICATIONTYPE () 
	{
		return (String)get_Value(COLUMNNAME_SIPAPPLICATIONTYPE);
	}

	/** Set SIPCODECS.
		@param SIPCODECS SIPCODECS	  */
	public void setSIPCODECS (String SIPCODECS)
	{

		if (SIPCODECS != null && SIPCODECS.length() > 200)
		{
			log.warning("Length > 200 - truncated");
			SIPCODECS = SIPCODECS.substring(0, 200);
		}
		set_Value (COLUMNNAME_SIPCODECS, SIPCODECS);
	}

	/** Get SIPCODECS.
		@return SIPCODECS	  */
	public String getSIPCODECS () 
	{
		return (String)get_Value(COLUMNNAME_SIPCODECS);
	}

	/** Set SIPFROMTAG.
		@param SIPFROMTAG SIPFROMTAG	  */
	public void setSIPFROMTAG (String SIPFROMTAG)
	{

		if (SIPFROMTAG != null && SIPFROMTAG.length() > 200)
		{
			log.warning("Length > 200 - truncated");
			SIPFROMTAG = SIPFROMTAG.substring(0, 200);
		}
		set_Value (COLUMNNAME_SIPFROMTAG, SIPFROMTAG);
	}

	/** Get SIPFROMTAG.
		@return SIPFROMTAG	  */
	public String getSIPFROMTAG () 
	{
		return (String)get_Value(COLUMNNAME_SIPFROMTAG);
	}

	/** Set SIPMETHOD.
		@param SIPMETHOD SIPMETHOD	  */
	public void setSIPMETHOD (String SIPMETHOD)
	{

		if (SIPMETHOD != null && SIPMETHOD.length() > 200)
		{
			log.warning("Length > 200 - truncated");
			SIPMETHOD = SIPMETHOD.substring(0, 200);
		}
		set_Value (COLUMNNAME_SIPMETHOD, SIPMETHOD);
	}

	/** Get SIPMETHOD.
		@return SIPMETHOD	  */
	public String getSIPMETHOD () 
	{
		return (String)get_Value(COLUMNNAME_SIPMETHOD);
	}

	/** Set SIPRESPONSECODE.
		@param SIPRESPONSECODE SIPRESPONSECODE	  */
	public void setSIPRESPONSECODE (String SIPRESPONSECODE)
	{

		if (SIPRESPONSECODE != null && SIPRESPONSECODE.length() > 200)
		{
			log.warning("Length > 200 - truncated");
			SIPRESPONSECODE = SIPRESPONSECODE.substring(0, 200);
		}
		set_Value (COLUMNNAME_SIPRESPONSECODE, SIPRESPONSECODE);
	}

	/** Get SIPRESPONSECODE.
		@return SIPRESPONSECODE	  */
	public String getSIPRESPONSECODE () 
	{
		return (String)get_Value(COLUMNNAME_SIPRESPONSECODE);
	}

	/** Set SIPRPID.
		@param SIPRPID SIPRPID	  */
	public void setSIPRPID (String SIPRPID)
	{

		if (SIPRPID != null && SIPRPID.length() > 200)
		{
			log.warning("Length > 200 - truncated");
			SIPRPID = SIPRPID.substring(0, 200);
		}
		set_Value (COLUMNNAME_SIPRPID, SIPRPID);
	}

	/** Get SIPRPID.
		@return SIPRPID	  */
	public String getSIPRPID () 
	{
		return (String)get_Value(COLUMNNAME_SIPRPID);
	}

	/** Set SIPRPIDHEADER.
		@param SIPRPIDHEADER SIPRPIDHEADER	  */
	public void setSIPRPIDHEADER (String SIPRPIDHEADER)
	{

		if (SIPRPIDHEADER != null && SIPRPIDHEADER.length() > 200)
		{
			log.warning("Length > 200 - truncated");
			SIPRPIDHEADER = SIPRPIDHEADER.substring(0, 200);
		}
		set_Value (COLUMNNAME_SIPRPIDHEADER, SIPRPIDHEADER);
	}

	/** Get SIPRPIDHEADER.
		@return SIPRPIDHEADER	  */
	public String getSIPRPIDHEADER () 
	{
		return (String)get_Value(COLUMNNAME_SIPRPIDHEADER);
	}

	/** Set SIPTOTAG.
		@param SIPTOTAG SIPTOTAG	  */
	public void setSIPTOTAG (String SIPTOTAG)
	{

		if (SIPTOTAG != null && SIPTOTAG.length() > 200)
		{
			log.warning("Length > 200 - truncated");
			SIPTOTAG = SIPTOTAG.substring(0, 200);
		}
		set_Value (COLUMNNAME_SIPTOTAG, SIPTOTAG);
	}

	/** Get SIPTOTAG.
		@return SIPTOTAG	  */
	public String getSIPTOTAG () 
	{
		return (String)get_Value(COLUMNNAME_SIPTOTAG);
	}

	/** Set SIPTRANSLATEDREQUESTURI.
		@param SIPTRANSLATEDREQUESTURI SIPTRANSLATEDREQUESTURI	  */
	public void setSIPTRANSLATEDREQUESTURI (String SIPTRANSLATEDREQUESTURI)
	{

		if (SIPTRANSLATEDREQUESTURI != null && SIPTRANSLATEDREQUESTURI.length() > 200)
		{
			log.warning("Length > 200 - truncated");
			SIPTRANSLATEDREQUESTURI = SIPTRANSLATEDREQUESTURI.substring(0, 200);
		}
		set_Value (COLUMNNAME_SIPTRANSLATEDREQUESTURI, SIPTRANSLATEDREQUESTURI);
	}

	/** Get SIPTRANSLATEDREQUESTURI.
		@return SIPTRANSLATEDREQUESTURI	  */
	public String getSIPTRANSLATEDREQUESTURI () 
	{
		return (String)get_Value(COLUMNNAME_SIPTRANSLATEDREQUESTURI);
	}

	/** Set SIPUSERAGENTS.
		@param SIPUSERAGENTS SIPUSERAGENTS	  */
	public void setSIPUSERAGENTS (String SIPUSERAGENTS)
	{

		if (SIPUSERAGENTS != null && SIPUSERAGENTS.length() > 200)
		{
			log.warning("Length > 200 - truncated");
			SIPUSERAGENTS = SIPUSERAGENTS.substring(0, 200);
		}
		set_Value (COLUMNNAME_SIPUSERAGENTS, SIPUSERAGENTS);
	}

	/** Get SIPUSERAGENTS.
		@return SIPUSERAGENTS	  */
	public String getSIPUSERAGENTS () 
	{
		return (String)get_Value(COLUMNNAME_SIPUSERAGENTS);
	}

	/** Set SOURCEIP.
		@param SOURCEIP SOURCEIP	  */
	public void setSOURCEIP (String SOURCEIP)
	{

		if (SOURCEIP != null && SOURCEIP.length() > 200)
		{
			log.warning("Length > 200 - truncated");
			SOURCEIP = SOURCEIP.substring(0, 200);
		}
		set_Value (COLUMNNAME_SOURCEIP, SOURCEIP);
	}

	/** Get SOURCEIP.
		@return SOURCEIP	  */
	public String getSOURCEIP () 
	{
		return (String)get_Value(COLUMNNAME_SOURCEIP);
	}

	/** Set SOURCEPORT.
		@param SOURCEPORT SOURCEPORT	  */
	public void setSOURCEPORT (String SOURCEPORT)
	{

		if (SOURCEPORT != null && SOURCEPORT.length() > 200)
		{
			log.warning("Length > 200 - truncated");
			SOURCEPORT = SOURCEPORT.substring(0, 200);
		}
		set_Value (COLUMNNAME_SOURCEPORT, SOURCEPORT);
	}

	/** Get SOURCEPORT.
		@return SOURCEPORT	  */
	public String getSOURCEPORT () 
	{
		return (String)get_Value(COLUMNNAME_SOURCEPORT);
	}

	/** Set STATUSDATE.
		@param STATUSDATE STATUSDATE	  */
	public void setSTATUSDATE (Timestamp STATUSDATE)
	{
		set_Value (COLUMNNAME_STATUSDATE, STATUSDATE);
	}

	/** Get STATUSDATE.
		@return STATUSDATE	  */
	public Timestamp getSTATUSDATE () 
	{
		return (Timestamp)get_Value(COLUMNNAME_STATUSDATE);
	}

	/** Set SYSTEMID.
		@param SYSTEMID SYSTEMID	  */
	public void setSYSTEMID (BigDecimal SYSTEMID)
	{
		set_Value (COLUMNNAME_SYSTEMID, SYSTEMID);
	}

	/** Get SYSTEMID.
		@return SYSTEMID	  */
	public BigDecimal getSYSTEMID () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_SYSTEMID);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set TIMESTAMP.
		@param TIMESTAMP TIMESTAMP	  */
	public void setTIMESTAMP (String TIMESTAMP)
	{

		if (TIMESTAMP != null && TIMESTAMP.length() > 200)
		{
			log.warning("Length > 200 - truncated");
			TIMESTAMP = TIMESTAMP.substring(0, 200);
		}
		set_Value (COLUMNNAME_TIMESTAMP, TIMESTAMP);
	}

	/** Get TIMESTAMP.
		@return TIMESTAMP	  */
	public String getTIMESTAMP () 
	{
		return (String)get_Value(COLUMNNAME_TIMESTAMP);
	}

	/** Set User Agent.
		@param UserAgent 
		Browser Used
	  */
	public void setUserAgent (String UserAgent)
	{

		if (UserAgent != null && UserAgent.length() > 200)
		{
			log.warning("Length > 200 - truncated");
			UserAgent = UserAgent.substring(0, 200);
		}
		set_Value (COLUMNNAME_UserAgent, UserAgent);
	}

	/** Get User Agent.
		@return Browser Used
	  */
	public String getUserAgent () 
	{
		return (String)get_Value(COLUMNNAME_UserAgent);
	}

	/** Set Registered EMail.
		@param UserName 
		Email of the responsible for the System
	  */
	public void setUserName (String UserName)
	{

		if (UserName != null && UserName.length() > 200)
		{
			log.warning("Length > 200 - truncated");
			UserName = UserName.substring(0, 200);
		}
		set_Value (COLUMNNAME_UserName, UserName);
	}

	/** Get Registered EMail.
		@return Email of the responsible for the System
	  */
	public String getUserName () 
	{
		return (String)get_Value(COLUMNNAME_UserName);
	}
}