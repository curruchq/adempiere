package com.conversant.model;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.logging.Level;

import org.compiere.util.CLogger;

public class RadiusAccount
{
	/** Logger						*/
	private static CLogger log = CLogger.getCLogger(RadiusAccount.class);
	
	private Integer RadAcctId = null;
	private String AcctSessionId = null;
	private String AcctUniqueId = null;
	private String UserName = null;
	private String Realm = null;
	private String NASIPAddress = null;
	private String NASPortId = null;
	private String NASPortType = null;
	private Timestamp AcctStartTime = null;
	private Timestamp AcctStopTime = null;
	private Integer AcctSessionTime = null;
	private String AcctAuthentic = null;
	private String ConnectInfo_start = null;
	private String ConnectInfo_stop = null;
	private Long AcctInputOctets = null;
	private Long AcctOutputOctets = null;
	private String CalledStationId = null;
	private String CallingStationId = null;
	private String AcctTerminateCause = null;
	private String ServiceType = null;
	private String FramedProtocol = null;
	private String FramedIPAddress = null;
	private Integer AcctStartDelay = null;
	private Integer AcctStopDelay = null;
	private String SipMethod = null;
	private Integer SipResponseCode = null;
	private String SipToTag = null;
	private String SipFromTag = null;
	private String SipTranslatedRequestURI = null;
	private String SipUserAgents = null;
	private String SipApplicationType = null;
	private String SipCodecs = null;
	private String SipRPID = null;
	private String SipRPIDHeader = null;
	private String SourceIP = null;
	private String SourcePort = null;
	private String CanonicalURI = null;
	private String DelayTime = null;
	private BigInteger Timestamp = null;
	private String DestinationId = null;
	private String Price = null;
	private String Rate = null;
	private String Normalized = null;
	private String BillingId = null;
	private String MediaInfo = null;
	private String RTPStatistics = null;
	private String FromHeader = null;
	private String UserAgent = null;
	private String Contact = null;
	
	/** James extra fields */
//	private String System_ID = null; // NUMBER
//	private String Status = null;//		               VARCHAR2(10)	,
//	private String Status_Date	= null;//	           DATE,
//	private String Line_Descr = null;//               VARCHAR2(100));

	private boolean valid = false;
	
	public static RadiusAccount get(Object[] dbRow)
	{
		RadiusAccount radiusAccount = new RadiusAccount(dbRow);
		return radiusAccount.isValid() ? radiusAccount : null;
	}
	
	private RadiusAccount(Object[] dbRow)
	{
		try
		{
			RadAcctId = ((Long)dbRow[0]).intValue();
			AcctSessionId = (String)dbRow[1];
			AcctUniqueId = (String)dbRow[2];
			UserName = (String)dbRow[3];
			Realm = (String)dbRow[4];
			NASIPAddress = (String)dbRow[5];
			NASPortId = (String)dbRow[6];
			NASPortType = (String)dbRow[7];
			AcctStartTime = (Timestamp)dbRow[8];
			AcctStopTime = (Timestamp)dbRow[9];
			AcctSessionTime = (Integer)dbRow[10];
			AcctAuthentic = (String)dbRow[11];
			ConnectInfo_start = (String)dbRow[12];
			ConnectInfo_stop = (String)dbRow[13];
			AcctInputOctets = (Long)dbRow[14];
			AcctOutputOctets = (Long)dbRow[15];
			CalledStationId = (String)dbRow[16];
			CallingStationId = (String)dbRow[17];
			AcctTerminateCause = (String)dbRow[18];
			ServiceType = (String)dbRow[19];
			FramedProtocol = (String)dbRow[20];
			FramedIPAddress = (String)dbRow[21];
			AcctStartDelay = (Integer)dbRow[22];
			AcctStopDelay = (Integer)dbRow[23];
			SipMethod = (String)dbRow[24];
			SipResponseCode = (Integer)dbRow[25];
			SipToTag = (String)dbRow[26];
			SipFromTag = (String)dbRow[27];
			SipTranslatedRequestURI = (String)dbRow[28];
			SipUserAgents = (String)dbRow[29];
			SipApplicationType = (String)dbRow[30];
			SipCodecs = (String)dbRow[31];
			SipRPID = (String)dbRow[32];
			SipRPIDHeader = (String)dbRow[33];
			SourceIP = (String)dbRow[34];
			SourcePort = (String)dbRow[35];
			CanonicalURI = (String)dbRow[36];
			DelayTime = (String)dbRow[37];
			Timestamp = (BigInteger)dbRow[38];
			DestinationId = (String)dbRow[39];
			Price = (String)dbRow[40];
			Rate = (String)dbRow[41];
			Normalized = (String)dbRow[42];
			BillingId = (String)dbRow[43];
			MediaInfo = (String)dbRow[44];
			RTPStatistics = (String)dbRow[45];
			FromHeader = (String)dbRow[46];
			UserAgent = (String)dbRow[47];
			Contact = (String)dbRow[48];

			valid = true; // to indicate that no class cast exceptions were raised
		}
		catch (ClassCastException ex)
		{
			valid = false;
			log.log(Level.SEVERE, "Error casting from db result to attribute, table may have changed", ex);
		}
	}

	@Override
	public String toString()
	{
		return "RadiusAccount[" + getRadAcctId() + "]";
	}
	
	public static CLogger getLog()
	{
		return log;
	}

	public static void setLog(CLogger log)
	{
		RadiusAccount.log = log;
	}

	public Integer getRadAcctId()
	{
		return RadAcctId;
	}

	public void setRadAcctId(Integer radAcctId)
	{
		RadAcctId = radAcctId;
	}

	public String getAcctSessionId()
	{
		return AcctSessionId;
	}

	public void setAcctSessionId(String acctSessionId)
	{
		AcctSessionId = acctSessionId;
	}

	public String getAcctUniqueId()
	{
		return AcctUniqueId;
	}

	public void setAcctUniqueId(String acctUniqueId)
	{
		AcctUniqueId = acctUniqueId;
	}

	public String getUserName()
	{
		return UserName;
	}

	public void setUserName(String userName)
	{
		UserName = userName;
	}

	public String getRealm()
	{
		return Realm;
	}

	public void setRealm(String realm)
	{
		Realm = realm;
	}

	public String getNASIPAddress()
	{
		return NASIPAddress;
	}

	public void setNASIPAddress(String address)
	{
		NASIPAddress = address;
	}

	public String getNASPortId()
	{
		return NASPortId;
	}

	public void setNASPortId(String portId)
	{
		NASPortId = portId;
	}

	public String getNASPortType()
	{
		return NASPortType;
	}

	public void setNASPortType(String portType)
	{
		NASPortType = portType;
	}

	public Timestamp getAcctStartTime()
	{
		return AcctStartTime;
	}

	public void setAcctStartTime(Timestamp acctStartTime)
	{
		AcctStartTime = acctStartTime;
	}

	public Timestamp getAcctStopTime()
	{
		return AcctStopTime;
	}

	public void setAcctStopTime(Timestamp acctStopTime)
	{
		AcctStopTime = acctStopTime;
	}

	public Integer getAcctSessionTime()
	{
		return AcctSessionTime;
	}

	public void setAcctSessionTime(Integer acctSessionTime)
	{
		AcctSessionTime = acctSessionTime;
	}

	public String getAcctAuthentic()
	{
		return AcctAuthentic;
	}

	public void setAcctAuthentic(String acctAuthentic)
	{
		AcctAuthentic = acctAuthentic;
	}

	public String getConnectInfo_start()
	{
		return ConnectInfo_start;
	}

	public void setConnectInfo_start(String connectInfo_start)
	{
		ConnectInfo_start = connectInfo_start;
	}

	public String getConnectInfo_stop()
	{
		return ConnectInfo_stop;
	}

	public void setConnectInfo_stop(String connectInfo_stop)
	{
		ConnectInfo_stop = connectInfo_stop;
	}

	public Long getAcctInputOctets()
	{
		return AcctInputOctets;
	}

	public void setAcctInputOctets(Long acctInputOctets)
	{
		AcctInputOctets = acctInputOctets;
	}

	public Long getAcctOutputOctets()
	{
		return AcctOutputOctets;
	}

	public void setAcctOutputOctets(Long acctOutputOctets)
	{
		AcctOutputOctets = acctOutputOctets;
	}

	public String getCalledStationId()
	{
		return CalledStationId;
	}

	public void setCalledStationId(String calledStationId)
	{
		CalledStationId = calledStationId;
	}

	public String getCallingStationId()
	{
		return CallingStationId;
	}

	public void setCallingStationId(String callingStationId)
	{
		CallingStationId = callingStationId;
	}

	public String getAcctTerminateCause()
	{
		return AcctTerminateCause;
	}

	public void setAcctTerminateCause(String acctTerminateCause)
	{
		AcctTerminateCause = acctTerminateCause;
	}

	public String getServiceType()
	{
		return ServiceType;
	}

	public void setServiceType(String serviceType)
	{
		ServiceType = serviceType;
	}

	public String getFramedProtocol()
	{
		return FramedProtocol;
	}

	public void setFramedProtocol(String framedProtocol)
	{
		FramedProtocol = framedProtocol;
	}

	public String getFramedIPAddress()
	{
		return FramedIPAddress;
	}

	public void setFramedIPAddress(String framedIPAddress)
	{
		FramedIPAddress = framedIPAddress;
	}

	public Integer getAcctStartDelay()
	{
		return AcctStartDelay;
	}

	public void setAcctStartDelay(Integer acctStartDelay)
	{
		AcctStartDelay = acctStartDelay;
	}

	public Integer getAcctStopDelay()
	{
		return AcctStopDelay;
	}

	public void setAcctStopDelay(Integer acctStopDelay)
	{
		AcctStopDelay = acctStopDelay;
	}

	public String getSipMethod()
	{
		return SipMethod;
	}

	public void setSipMethod(String sipMethod)
	{
		SipMethod = sipMethod;
	}

	public Integer getSipResponseCode()
	{
		return SipResponseCode;
	}

	public void setSipResponseCode(Integer sipResponseCode)
	{
		SipResponseCode = sipResponseCode;
	}

	public String getSipToTag()
	{
		return SipToTag;
	}

	public void setSipToTag(String sipToTag)
	{
		SipToTag = sipToTag;
	}

	public String getSipFromTag()
	{
		return SipFromTag;
	}

	public void setSipFromTag(String sipFromTag)
	{
		SipFromTag = sipFromTag;
	}

	public String getSipTranslatedRequestURI()
	{
		return SipTranslatedRequestURI;
	}

	public void setSipTranslatedRequestURI(String sipTranslatedRequestURI)
	{
		SipTranslatedRequestURI = sipTranslatedRequestURI;
	}

	public String getSipUserAgents()
	{
		return SipUserAgents;
	}

	public void setSipUserAgents(String sipUserAgents)
	{
		SipUserAgents = sipUserAgents;
	}

	public String getSipApplicationType()
	{
		return SipApplicationType;
	}

	public void setSipApplicationType(String sipApplicationType)
	{
		SipApplicationType = sipApplicationType;
	}

	public String getSipCodecs()
	{
		return SipCodecs;
	}

	public void setSipCodecs(String sipCodecs)
	{
		SipCodecs = sipCodecs;
	}

	public String getSipRPID()
	{
		return SipRPID;
	}

	public void setSipRPID(String sipRPID)
	{
		SipRPID = sipRPID;
	}

	public String getSipRPIDHeader()
	{
		return SipRPIDHeader;
	}

	public void setSipRPIDHeader(String sipRPIDHeader)
	{
		SipRPIDHeader = sipRPIDHeader;
	}

	public String getSourceIP()
	{
		return SourceIP;
	}

	public void setSourceIP(String sourceIP)
	{
		SourceIP = sourceIP;
	}

	public String getSourcePort()
	{
		return SourcePort;
	}

	public void setSourcePort(String sourcePort)
	{
		SourcePort = sourcePort;
	}

	public String getCanonicalURI()
	{
		return CanonicalURI;
	}

	public void setCanonicalURI(String canonicalURI)
	{
		CanonicalURI = canonicalURI;
	}

	public String getDelayTime()
	{
		return DelayTime;
	}

	public void setDelayTime(String delayTime)
	{
		DelayTime = delayTime;
	}

	public BigInteger getTimestamp()
	{
		return Timestamp;
	}

	public void setTimestamp(BigInteger timestamp)
	{
		Timestamp = timestamp;
	}

	public String getDestinationId()
	{
		return DestinationId;
	}

	public void setDestinationId(String destinationId)
	{
		DestinationId = destinationId;
	}

	public String getPrice()
	{
		return Price;
	}

	public void setPrice(String price)
	{
		Price = price;
	}

	public String getRate()
	{
		return Rate;
	}

	public void setRate(String rate)
	{
		Rate = rate;
	}

	public String getNormalized()
	{
		return Normalized;
	}

	public void setNormalized(String normalized)
	{
		Normalized = normalized;
	}

	public String getBillingId()
	{
		return BillingId;
	}

	public void setBillingId(String billingId)
	{
		BillingId = billingId;
	}

	public String getMediaInfo()
	{
		return MediaInfo;
	}

	public void setMediaInfo(String mediaInfo)
	{
		MediaInfo = mediaInfo;
	}

	public String getRTPStatistics()
	{
		return RTPStatistics;
	}

	public void setRTPStatistics(String statistics)
	{
		RTPStatistics = statistics;
	}

	public String getFromHeader()
	{
		return FromHeader;
	}

	public void setFromHeader(String fromHeader)
	{
		FromHeader = fromHeader;
	}

	public String getUserAgent()
	{
		return UserAgent;
	}

	public void setUserAgent(String userAgent)
	{
		UserAgent = userAgent;
	}

	public String getContact()
	{
		return Contact;
	}

	public void setContact(String contact)
	{
		Contact = contact;
	}

	public boolean isValid()
	{
		return valid;
	}

	public void setValid(boolean valid)
	{
		this.valid = valid;
	}
	
}
