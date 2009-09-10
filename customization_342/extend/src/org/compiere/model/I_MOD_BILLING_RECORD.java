/**********************************************************************
 * This file is part of Adempiere ERP Bazaar                          *
 * http://www.adempiere.org                                           *
 *                                                                    *
 * Copyright (C) Trifon Trifonov.                                     *
 * Copyright (C) Contributors                                         *
 *                                                                    *
 * This program is free software;
 you can redistribute it and/or      *
 * modify it under the terms of the GNU General Public License        *
 * as published by the Free Software Foundation;
 either version 2     *
 * of the License, or (at your option) any later version.             *
 *                                                                    *
 * This program is distributed in the hope that it will be useful,    *
 * but WITHOUT ANY WARRANTY;
 without even the implied warranty of     *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the       *
 * GNU General Public License for more details.                       *
 *                                                                    *
 * You should have received a copy of the GNU General Public License  *
 * along with this program;
 if not, write to the Free Software        *
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,         *
 * MA 02110-1301, USA.                                                *
 *                                                                    *
 * Contributors:                                                      *
 * - Trifon Trifonov (trifonnt@users.sourceforge.net)                 *
 *                                                                    *
 * Sponsors:                                                          *
 * - Company (http://www.site.com)                                    *
 **********************************************************************/
package org.compiere.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import org.compiere.util.KeyNamePair;

/** Generated Interface for MOD_BILLING_RECORD
 *  @author Trifon Trifonov (generated) 
 *  @version Release 3.4.0s
 */
public interface I_MOD_BILLING_RECORD 
{

    /** TableName=MOD_BILLING_RECORD */
    public static final String Table_Name = "MOD_BILLING_RECORD";

    /** AD_Table_ID=1000003 */
    public static final int Table_ID = MTable.getTable_ID(Table_Name);

    KeyNamePair Model = new KeyNamePair(Table_ID, Table_Name);

    /** AccessLevel = 3 - Client - Org 
     */
    BigDecimal accessLevel = BigDecimal.valueOf(3);

    /** Load Meta Data */

    /** Column name ACCTAUTHENTIC */
    public static final String COLUMNNAME_ACCTAUTHENTIC = "ACCTAUTHENTIC";

	/** Set ACCTAUTHENTIC	  */
	public void setACCTAUTHENTIC (String ACCTAUTHENTIC);

	/** Get ACCTAUTHENTIC	  */
	public String getACCTAUTHENTIC();

    /** Column name ACCTINPUTOCTETS */
    public static final String COLUMNNAME_ACCTINPUTOCTETS = "ACCTINPUTOCTETS";

	/** Set ACCTINPUTOCTETS	  */
	public void setACCTINPUTOCTETS (String ACCTINPUTOCTETS);

	/** Get ACCTINPUTOCTETS	  */
	public String getACCTINPUTOCTETS();

    /** Column name ACCTOUTPUTOCTETS */
    public static final String COLUMNNAME_ACCTOUTPUTOCTETS = "ACCTOUTPUTOCTETS";

	/** Set ACCTOUTPUTOCTETS	  */
	public void setACCTOUTPUTOCTETS (String ACCTOUTPUTOCTETS);

	/** Get ACCTOUTPUTOCTETS	  */
	public String getACCTOUTPUTOCTETS();

    /** Column name ACCTSESSIONID */
    public static final String COLUMNNAME_ACCTSESSIONID = "ACCTSESSIONID";

	/** Set ACCTSESSIONID	  */
	public void setACCTSESSIONID (String ACCTSESSIONID);

	/** Get ACCTSESSIONID	  */
	public String getACCTSESSIONID();

    /** Column name ACCTSESSIONTIME */
    public static final String COLUMNNAME_ACCTSESSIONTIME = "ACCTSESSIONTIME";

	/** Set ACCTSESSIONTIME	  */
	public void setACCTSESSIONTIME (BigDecimal ACCTSESSIONTIME);

	/** Get ACCTSESSIONTIME	  */
	public BigDecimal getACCTSESSIONTIME();

    /** Column name ACCTSTARTDELAY */
    public static final String COLUMNNAME_ACCTSTARTDELAY = "ACCTSTARTDELAY";

	/** Set ACCTSTARTDELAY	  */
	public void setACCTSTARTDELAY (String ACCTSTARTDELAY);

	/** Get ACCTSTARTDELAY	  */
	public String getACCTSTARTDELAY();

    /** Column name ACCTSTARTTIME */
    public static final String COLUMNNAME_ACCTSTARTTIME = "ACCTSTARTTIME";

	/** Set ACCTSTARTTIME	  */
	public void setACCTSTARTTIME (Timestamp ACCTSTARTTIME);

	/** Get ACCTSTARTTIME	  */
	public Timestamp getACCTSTARTTIME();

    /** Column name ACCTSTOPDELAY */
    public static final String COLUMNNAME_ACCTSTOPDELAY = "ACCTSTOPDELAY";

	/** Set ACCTSTOPDELAY	  */
	public void setACCTSTOPDELAY (String ACCTSTOPDELAY);

	/** Get ACCTSTOPDELAY	  */
	public String getACCTSTOPDELAY();

    /** Column name ACCTSTOPTIME */
    public static final String COLUMNNAME_ACCTSTOPTIME = "ACCTSTOPTIME";

	/** Set ACCTSTOPTIME	  */
	public void setACCTSTOPTIME (Timestamp ACCTSTOPTIME);

	/** Get ACCTSTOPTIME	  */
	public Timestamp getACCTSTOPTIME();

    /** Column name ACCTTERMINATECAUSE */
    public static final String COLUMNNAME_ACCTTERMINATECAUSE = "ACCTTERMINATECAUSE";

	/** Set ACCTTERMINATECAUSE	  */
	public void setACCTTERMINATECAUSE (String ACCTTERMINATECAUSE);

	/** Get ACCTTERMINATECAUSE	  */
	public String getACCTTERMINATECAUSE();

    /** Column name ACCTUNIQUEID */
    public static final String COLUMNNAME_ACCTUNIQUEID = "ACCTUNIQUEID";

	/** Set ACCTUNIQUEID	  */
	public void setACCTUNIQUEID (String ACCTUNIQUEID);

	/** Get ACCTUNIQUEID	  */
	public String getACCTUNIQUEID();

    /** Column name BILLINGID */
    public static final String COLUMNNAME_BILLINGID = "BILLINGID";

	/** Set BILLINGID	  */
	public void setBILLINGID (String BILLINGID);

	/** Get BILLINGID	  */
	public String getBILLINGID();

    /** Column name CALLEDSTATIONID */
    public static final String COLUMNNAME_CALLEDSTATIONID = "CALLEDSTATIONID";

	/** Set CALLEDSTATIONID	  */
	public void setCALLEDSTATIONID (String CALLEDSTATIONID);

	/** Get CALLEDSTATIONID	  */
	public String getCALLEDSTATIONID();

    /** Column name CALLINGSTATIONID */
    public static final String COLUMNNAME_CALLINGSTATIONID = "CALLINGSTATIONID";

	/** Set CALLINGSTATIONID	  */
	public void setCALLINGSTATIONID (String CALLINGSTATIONID);

	/** Get CALLINGSTATIONID	  */
	public String getCALLINGSTATIONID();

    /** Column name CANONICALURI */
    public static final String COLUMNNAME_CANONICALURI = "CANONICALURI";

	/** Set CANONICALURI	  */
	public void setCANONICALURI (String CANONICALURI);

	/** Get CANONICALURI	  */
	public String getCANONICALURI();

    /** Column name CONNECTINFO_START */
    public static final String COLUMNNAME_CONNECTINFO_START = "CONNECTINFO_START";

	/** Set CONNECTINFO_START	  */
	public void setCONNECTINFO_START (String CONNECTINFO_START);

	/** Get CONNECTINFO_START	  */
	public String getCONNECTINFO_START();

    /** Column name CONNECTINFO_STOP */
    public static final String COLUMNNAME_CONNECTINFO_STOP = "CONNECTINFO_STOP";

	/** Set CONNECTINFO_STOP	  */
	public void setCONNECTINFO_STOP (String CONNECTINFO_STOP);

	/** Get CONNECTINFO_STOP	  */
	public String getCONNECTINFO_STOP();

    /** Column name CONTACT */
    public static final String COLUMNNAME_CONTACT = "CONTACT";

	/** Set CONTACT	  */
	public void setCONTACT (String CONTACT);

	/** Get CONTACT	  */
	public String getCONTACT();

    /** Column name DELAYTIME */
    public static final String COLUMNNAME_DELAYTIME = "DELAYTIME";

	/** Set DELAYTIME	  */
	public void setDELAYTIME (String DELAYTIME);

	/** Get DELAYTIME	  */
	public String getDELAYTIME();

    /** Column name DESTINATIONID */
    public static final String COLUMNNAME_DESTINATIONID = "DESTINATIONID";

	/** Set DESTINATIONID	  */
	public void setDESTINATIONID (String DESTINATIONID);

	/** Get DESTINATIONID	  */
	public String getDESTINATIONID();

    /** Column name FRAMEDIPADDRESS */
    public static final String COLUMNNAME_FRAMEDIPADDRESS = "FRAMEDIPADDRESS";

	/** Set FRAMEDIPADDRESS	  */
	public void setFRAMEDIPADDRESS (String FRAMEDIPADDRESS);

	/** Get FRAMEDIPADDRESS	  */
	public String getFRAMEDIPADDRESS();

    /** Column name FRAMEDPROTOCOL */
    public static final String COLUMNNAME_FRAMEDPROTOCOL = "FRAMEDPROTOCOL";

	/** Set FRAMEDPROTOCOL	  */
	public void setFRAMEDPROTOCOL (String FRAMEDPROTOCOL);

	/** Get FRAMEDPROTOCOL	  */
	public String getFRAMEDPROTOCOL();

    /** Column name FROMHEADER */
    public static final String COLUMNNAME_FROMHEADER = "FROMHEADER";

	/** Set FROMHEADER	  */
	public void setFROMHEADER (String FROMHEADER);

	/** Get FROMHEADER	  */
	public String getFROMHEADER();

    /** Column name LINEDESC */
    public static final String COLUMNNAME_LINEDESC = "LINEDESC";

	/** Set LINEDESC	  */
	public void setLINEDESC (String LINEDESC);

	/** Get LINEDESC	  */
	public String getLINEDESC();

    /** Column name MEDIAINFO */
    public static final String COLUMNNAME_MEDIAINFO = "MEDIAINFO";

	/** Set MEDIAINFO	  */
	public void setMEDIAINFO (String MEDIAINFO);

	/** Get MEDIAINFO	  */
	public String getMEDIAINFO();

    /** Column name MOD_BILLING_RECORD_ID */
    public static final String COLUMNNAME_MOD_BILLING_RECORD_ID = "MOD_BILLING_RECORD_ID";

	/** Set MOD Billing Record	  */
	public void setMOD_BILLING_RECORD_ID (int MOD_BILLING_RECORD_ID);

	/** Get MOD Billing Record	  */
	public int getMOD_BILLING_RECORD_ID();

    /** Column name NASIPADDRESS */
    public static final String COLUMNNAME_NASIPADDRESS = "NASIPADDRESS";

	/** Set NASIPADDRESS	  */
	public void setNASIPADDRESS (String NASIPADDRESS);

	/** Get NASIPADDRESS	  */
	public String getNASIPADDRESS();

    /** Column name NASPORTID */
    public static final String COLUMNNAME_NASPORTID = "NASPORTID";

	/** Set NASPORTID	  */
	public void setNASPORTID (String NASPORTID);

	/** Get NASPORTID	  */
	public String getNASPORTID();

    /** Column name NASPORTTYPE */
    public static final String COLUMNNAME_NASPORTTYPE = "NASPORTTYPE";

	/** Set NASPORTTYPE	  */
	public void setNASPORTTYPE (String NASPORTTYPE);

	/** Get NASPORTTYPE	  */
	public String getNASPORTTYPE();

    /** Column name NORMALIZED */
    public static final String COLUMNNAME_NORMALIZED = "NORMALIZED";

	/** Set NORMALIZED	  */
	public void setNORMALIZED (String NORMALIZED);

	/** Get NORMALIZED	  */
	public String getNORMALIZED();

    /** Column name Price */
    public static final String COLUMNNAME_Price = "Price";

	/** Set Price.
	  * Price
	  */
	public void setPrice (BigDecimal Price);

	/** Get Price.
	  * Price
	  */
	public BigDecimal getPrice();

    /** Column name RADACCTID */
    public static final String COLUMNNAME_RADACCTID = "RADACCTID";

	/** Set RADACCTID	  */
	public void setRADACCTID (int RADACCTID);

	/** Get RADACCTID	  */
	public int getRADACCTID();

    /** Column name REALM */
    public static final String COLUMNNAME_REALM = "REALM";

	/** Set REALM	  */
	public void setREALM (String REALM);

	/** Get REALM	  */
	public String getREALM();

    /** Column name RECORDSTATUS */
    public static final String COLUMNNAME_RECORDSTATUS = "RECORDSTATUS";

	/** Set RECORDSTATUS	  */
	public void setRECORDSTATUS (String RECORDSTATUS);

	/** Get RECORDSTATUS	  */
	public String getRECORDSTATUS();

    /** Column name RTPSTATISTICS */
    public static final String COLUMNNAME_RTPSTATISTICS = "RTPSTATISTICS";

	/** Set RTPSTATISTICS	  */
	public void setRTPSTATISTICS (String RTPSTATISTICS);

	/** Get RTPSTATISTICS	  */
	public String getRTPSTATISTICS();

    /** Column name Rate */
    public static final String COLUMNNAME_Rate = "Rate";

	/** Set Rate.
	  * Rate or Tax or Exchange
	  */
	public void setRate (String Rate);

	/** Get Rate.
	  * Rate or Tax or Exchange
	  */
	public String getRate();

    /** Column name SERVICETYPE */
    public static final String COLUMNNAME_SERVICETYPE = "SERVICETYPE";

	/** Set SERVICETYPE	  */
	public void setSERVICETYPE (String SERVICETYPE);

	/** Get SERVICETYPE	  */
	public String getSERVICETYPE();

    /** Column name SIPAPPLICATIONTYPE */
    public static final String COLUMNNAME_SIPAPPLICATIONTYPE = "SIPAPPLICATIONTYPE";

	/** Set SIPAPPLICATIONTYPE	  */
	public void setSIPAPPLICATIONTYPE (String SIPAPPLICATIONTYPE);

	/** Get SIPAPPLICATIONTYPE	  */
	public String getSIPAPPLICATIONTYPE();

    /** Column name SIPCODECS */
    public static final String COLUMNNAME_SIPCODECS = "SIPCODECS";

	/** Set SIPCODECS	  */
	public void setSIPCODECS (String SIPCODECS);

	/** Get SIPCODECS	  */
	public String getSIPCODECS();

    /** Column name SIPFROMTAG */
    public static final String COLUMNNAME_SIPFROMTAG = "SIPFROMTAG";

	/** Set SIPFROMTAG	  */
	public void setSIPFROMTAG (String SIPFROMTAG);

	/** Get SIPFROMTAG	  */
	public String getSIPFROMTAG();

    /** Column name SIPMETHOD */
    public static final String COLUMNNAME_SIPMETHOD = "SIPMETHOD";

	/** Set SIPMETHOD	  */
	public void setSIPMETHOD (String SIPMETHOD);

	/** Get SIPMETHOD	  */
	public String getSIPMETHOD();

    /** Column name SIPRESPONSECODE */
    public static final String COLUMNNAME_SIPRESPONSECODE = "SIPRESPONSECODE";

	/** Set SIPRESPONSECODE	  */
	public void setSIPRESPONSECODE (String SIPRESPONSECODE);

	/** Get SIPRESPONSECODE	  */
	public String getSIPRESPONSECODE();

    /** Column name SIPRPID */
    public static final String COLUMNNAME_SIPRPID = "SIPRPID";

	/** Set SIPRPID	  */
	public void setSIPRPID (String SIPRPID);

	/** Get SIPRPID	  */
	public String getSIPRPID();

    /** Column name SIPRPIDHEADER */
    public static final String COLUMNNAME_SIPRPIDHEADER = "SIPRPIDHEADER";

	/** Set SIPRPIDHEADER	  */
	public void setSIPRPIDHEADER (String SIPRPIDHEADER);

	/** Get SIPRPIDHEADER	  */
	public String getSIPRPIDHEADER();

    /** Column name SIPTOTAG */
    public static final String COLUMNNAME_SIPTOTAG = "SIPTOTAG";

	/** Set SIPTOTAG	  */
	public void setSIPTOTAG (String SIPTOTAG);

	/** Get SIPTOTAG	  */
	public String getSIPTOTAG();

    /** Column name SIPTRANSLATEDREQUESTURI */
    public static final String COLUMNNAME_SIPTRANSLATEDREQUESTURI = "SIPTRANSLATEDREQUESTURI";

	/** Set SIPTRANSLATEDREQUESTURI	  */
	public void setSIPTRANSLATEDREQUESTURI (String SIPTRANSLATEDREQUESTURI);

	/** Get SIPTRANSLATEDREQUESTURI	  */
	public String getSIPTRANSLATEDREQUESTURI();

    /** Column name SIPUSERAGENTS */
    public static final String COLUMNNAME_SIPUSERAGENTS = "SIPUSERAGENTS";

	/** Set SIPUSERAGENTS	  */
	public void setSIPUSERAGENTS (String SIPUSERAGENTS);

	/** Get SIPUSERAGENTS	  */
	public String getSIPUSERAGENTS();

    /** Column name SOURCEIP */
    public static final String COLUMNNAME_SOURCEIP = "SOURCEIP";

	/** Set SOURCEIP	  */
	public void setSOURCEIP (String SOURCEIP);

	/** Get SOURCEIP	  */
	public String getSOURCEIP();

    /** Column name SOURCEPORT */
    public static final String COLUMNNAME_SOURCEPORT = "SOURCEPORT";

	/** Set SOURCEPORT	  */
	public void setSOURCEPORT (String SOURCEPORT);

	/** Get SOURCEPORT	  */
	public String getSOURCEPORT();

    /** Column name STATUSDATE */
    public static final String COLUMNNAME_STATUSDATE = "STATUSDATE";

	/** Set STATUSDATE	  */
	public void setSTATUSDATE (Timestamp STATUSDATE);

	/** Get STATUSDATE	  */
	public Timestamp getSTATUSDATE();

    /** Column name SYSTEMID */
    public static final String COLUMNNAME_SYSTEMID = "SYSTEMID";

	/** Set SYSTEMID	  */
	public void setSYSTEMID (BigDecimal SYSTEMID);

	/** Get SYSTEMID	  */
	public BigDecimal getSYSTEMID();

    /** Column name TIMESTAMP */
    public static final String COLUMNNAME_TIMESTAMP = "TIMESTAMP";

	/** Set TIMESTAMP	  */
	public void setTIMESTAMP (String TIMESTAMP);

	/** Get TIMESTAMP	  */
	public String getTIMESTAMP();

    /** Column name UserAgent */
    public static final String COLUMNNAME_UserAgent = "UserAgent";

	/** Set User Agent.
	  * Browser Used
	  */
	public void setUserAgent (String UserAgent);

	/** Get User Agent.
	  * Browser Used
	  */
	public String getUserAgent();

    /** Column name UserName */
    public static final String COLUMNNAME_UserName = "UserName";

	/** Set Registered EMail.
	  * Email of the responsible for the System
	  */
	public void setUserName (String UserName);

	/** Get Registered EMail.
	  * Email of the responsible for the System
	  */
	public String getUserName();
}
