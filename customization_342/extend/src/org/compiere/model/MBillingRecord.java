package org.compiere.model;

import java.math.BigDecimal;
import java.util.Properties;

import org.compiere.util.CLogger;
import org.compiere.util.Env;

import com.conversant.model.RadiusAccount;

public class MBillingRecord extends X_MOD_BILLING_RECORD
{
	/** Logger						*/
	private static CLogger log = CLogger.getCLogger(MBillingRecord.class);
	
	public MBillingRecord(Properties ctx, int MOD_BILLING_RECORD_ID, String trxName)
	{
		super(ctx, MOD_BILLING_RECORD_ID, trxName);
		if (MOD_BILLING_RECORD_ID == 0)
		{
			// TODO: Load defaults which aren't already defined at table level
			setLINEDESC(null);
			setRECORDSTATUS(null); // was STATUS
			setSTATUSDATE(null);
			setSYSTEMID(null);
		}
	}
	
	public static MBillingRecord createNew(Properties ctx, RadiusAccount account)
	{
		if (ctx == null || account == null)
			return null;
		
		MBillingRecord billingRecord = new MBillingRecord(ctx, 0, null);	
		
		if (account.getRadAcctId() != null)
		{
			billingRecord.setRADACCTID(account.getRadAcctId()); 
		}
		else
		{
			log.warning("RadiusAccount loaded with RadAcctId = null");
			return null;
		}
		
		billingRecord.setAD_Org_ID(Env.getAD_Org_ID(ctx));
		billingRecord.setAD_Client_ID(Env.getAD_Client_ID(ctx));
		
		billingRecord.setACCTAUTHENTIC(account.getAcctAuthentic());
		billingRecord.setACCTSESSIONID(account.getAcctSessionId());
		billingRecord.setACCTSTARTTIME(account.getAcctStartTime());
		billingRecord.setACCTSTOPTIME(account.getAcctStopTime());
		billingRecord.setACCTTERMINATECAUSE(account.getAcctTerminateCause());
		billingRecord.setACCTUNIQUEID(account.getAcctUniqueId());
		billingRecord.setBILLINGID(account.getBillingId());
		billingRecord.setCALLEDSTATIONID(account.getCalledStationId());
		billingRecord.setCALLINGSTATIONID(account.getCallingStationId());
		billingRecord.setCANONICALURI(account.getCanonicalURI());
		billingRecord.setCONNECTINFO_START(account.getConnectInfo_start());
		billingRecord.setCONNECTINFO_STOP(account.getConnectInfo_stop());
		billingRecord.setCONTACT(account.getContact());
		billingRecord.setDELAYTIME(account.getDelayTime());
		billingRecord.setDESTINATIONID(account.getDestinationId());
		billingRecord.setFRAMEDIPADDRESS(account.getFramedIPAddress());
		billingRecord.setFRAMEDPROTOCOL(account.getFramedProtocol());
		billingRecord.setFROMHEADER(account.getFromHeader());
		billingRecord.setMEDIAINFO(account.getMediaInfo());
		billingRecord.setNASIPADDRESS(account.getNASIPAddress());
		billingRecord.setNASPORTID(account.getNASPortId());
		billingRecord.setNASPORTTYPE(account.getNASPortType());
		billingRecord.setNORMALIZED(account.getNormalized());
		billingRecord.setRate(account.getRate());
		billingRecord.setREALM(account.getRealm());
		billingRecord.setRTPSTATISTICS(account.getRTPStatistics());
		billingRecord.setSERVICETYPE(account.getServiceType());
		billingRecord.setSIPAPPLICATIONTYPE(account.getSipApplicationType());
		billingRecord.setSIPCODECS(account.getSipCodecs());
		billingRecord.setSIPFROMTAG(account.getSipFromTag());
		billingRecord.setSIPMETHOD(account.getSipMethod());
		billingRecord.setSIPRPID(account.getSipRPID());
		billingRecord.setSIPRPIDHEADER(account.getSipRPIDHeader());
		billingRecord.setSIPTOTAG(account.getSipToTag());
		billingRecord.setSIPTRANSLATEDREQUESTURI(account.getSipTranslatedRequestURI());
		billingRecord.setSIPUSERAGENTS(account.getSipUserAgents());
		billingRecord.setSOURCEIP(account.getSourceIP());
		billingRecord.setSOURCEPORT(account.getSourcePort());
		billingRecord.setTIMESTAMP(account.getTimestamp().toString());
		billingRecord.setUserAgent(account.getUserAgent());
		billingRecord.setUserName(account.getUserName());
		
		try
		{
			billingRecord.setACCTINPUTOCTETS(Long.toString(account.getAcctInputOctets()));
		}
		catch (Exception ex)
		{
			billingRecord.setACCTINPUTOCTETS(null);
		}
		
		try
		{
			billingRecord.setACCTOUTPUTOCTETS(Long.toString(account.getAcctOutputOctets()));
		}
		catch (Exception ex)
		{
			billingRecord.setACCTOUTPUTOCTETS(null);
		}
		
		try
		{
			billingRecord.setACCTSESSIONTIME(new BigDecimal(account.getAcctSessionTime()));
		}
		catch (Exception ex)
		{
			billingRecord.setACCTSESSIONTIME(null);
		}
		
		try
		{
			billingRecord.setACCTSTARTDELAY(Integer.toString(account.getAcctStartDelay()));
		}
		catch (Exception ex)
		{
			billingRecord.setACCTSTARTDELAY(null);
		}
		
		try
		{
			billingRecord.setACCTSTOPDELAY(Integer.toString(account.getAcctStopDelay()));
		}
		catch (Exception ex)
		{
			billingRecord.setACCTSTOPDELAY(null);
		}
		
		try
		{
			billingRecord.setPrice(new BigDecimal(account.getPrice()));
		}
		catch (Exception ex)
		{
			billingRecord.setPrice(null);
		}
		
		try
		{
			billingRecord.setSIPRESPONSECODE(Integer.toString(account.getSipResponseCode()));
		}
		catch (Exception ex)
		{
			billingRecord.setSIPRESPONSECODE(null);
		}
		
		return billingRecord;
	}
}
