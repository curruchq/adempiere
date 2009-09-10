package com.conversant.model;

import java.io.Serializable;
import java.util.logging.Level;

import org.compiere.util.CLogger;

public class DIDInfo implements Serializable
{
	/** Logger						*/
	private static CLogger log = CLogger.getCLogger(DIDInfo.class);
	
	private Long didNumber = null;
	private Integer freeMin = null;
	private Float ourPerMinuteCharges = null;
	private Integer iChannel = null;
	private Integer checkStatus = null;
	private Integer OID = null;
	private Integer ourSetupCost = null;
	private Float ourMonthlyCharges = null;
	private Integer areaID = null;
	private Integer status = null;
	private Integer alreadyApproved = null;
	private Integer documentRequires = null;
	private String msgFromVendor = null;
	private String documentType = null;
	private Integer vendorRating = null;
	private Integer iCallCard = null;
	private Integer iSoldCC = null;
	private Integer iCodec = null;
	private Integer iNetwork = null;
	private Integer trigger = null;

	private boolean valid = false;

	public static DIDInfo get(Object[] values)
	{
		DIDInfo didInfo = new DIDInfo(values);
		return didInfo.isValid() ? didInfo : null;
	}
	
	private DIDInfo(Object[] values)
	{
		try
		{
			/*
			 * Due to DIDx.net's API returning different DATA TYPES depending on which did is queried i have commented this out
			 * until needed.
			 */
			
//			didNumber = (Long)values[0];
//			freeMin = (Integer)values[1];
//			ourPerMinuteCharges = (Float)values[2];
//			iChannel = (Integer)values[3];
//			checkStatus = (Integer)values[4];
//			OID = (Integer)values[5];
//			ourSetupCost = (Integer)values[6];
//			ourMonthlyCharges = (Float)values[7];
//			areaID = (Integer)values[8];
			status = Integer.parseInt((String)values[9]);
//			alreadyApproved = (Integer)values[10];
//			documentRequires = (Integer)values[11];
//			msgFromVendor = (String)values[12];
//			documentType = (String)values[13];
//			vendorRating = (Integer)values[14];
//			iCallCard = (Integer)values[15];
//			iSoldCC = (Integer)values[16];
//			iCodec = (Integer)values[17];
//			iNetwork = (Integer)values[18];
//			trigger = (Integer)values[19];
			
			valid = true; // to indicate that no class cast exceptions were raised
		}
		catch (ClassCastException ex)
		{
			valid = false;
			log.log(Level.SEVERE, "Error casting from Object[] result to attributes, returned values from DIDX.net may have changed", ex);
		}
	}
	
	public boolean isValid() {
		return valid;
	}
	
	public Long getDidNumber() {
		return didNumber;
	}
	public void setDidNumber(Long didNumber) {
		this.didNumber = didNumber;
	}
	public Integer getFreeMin() {
		return freeMin;
	}
	public void setFreeMin(Integer freeMin) {
		this.freeMin = freeMin;
	}
	public Float getOurPerMinuteCharges() {
		return ourPerMinuteCharges;
	}
	public void setOurPerMinuteCharges(Float ourPerMinuteCharges) {
		this.ourPerMinuteCharges = ourPerMinuteCharges;
	}
	public Integer getIChannel() {
		return iChannel;
	}
	public void setIChannel(Integer channel) {
		iChannel = channel;
	}
	public Integer getCheckStatus() {
		return checkStatus;
	}
	public void setCheckStatus(Integer checkStatus) {
		this.checkStatus = checkStatus;
	}
	public Integer getOID() {
		return OID;
	}
	public void setOID(Integer oid) {
		OID = oid;
	}
	public Integer getOurSetupCost() {
		return ourSetupCost;
	}
	public void setOurSetupCost(Integer ourSetupCost) {
		this.ourSetupCost = ourSetupCost;
	}
	public Float getOurMonthlyCharges() {
		return ourMonthlyCharges;
	}
	public void setOurMonthlyCharges(Float ourMonthlyCharges) {
		this.ourMonthlyCharges = ourMonthlyCharges;
	}
	public Integer getAreaID() {
		return areaID;
	}
	public void setAreaID(Integer areaID) {
		this.areaID = areaID;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Integer getAlreadyApproved() {
		return alreadyApproved;
	}
	public void setAlreadyApproved(Integer alreadyApproved) {
		this.alreadyApproved = alreadyApproved;
	}
	public Integer getDocumentRequires() {
		return documentRequires;
	}
	public void setDocumentRequires(Integer documentRequires) {
		this.documentRequires = documentRequires;
	}
	public String getMsgFromVendor() {
		return msgFromVendor;
	}
	public void setMsgFromVendor(String msgFromVendor) {
		this.msgFromVendor = msgFromVendor;
	}
	public String getDocumentType() {
		return documentType;
	}
	public void setDocumentType(String documentType) {
		this.documentType = documentType;
	}
	public Integer getVendorRating() {
		return vendorRating;
	}
	public void setVendorRating(Integer vendorRating) {
		this.vendorRating = vendorRating;
	}
	public Integer getICallCard() {
		return iCallCard;
	}
	public void setICallCard(Integer callCard) {
		iCallCard = callCard;
	}
	public Integer getISoldCC() {
		return iSoldCC;
	}
	public void setISoldCC(Integer soldCC) {
		iSoldCC = soldCC;
	}
	public Integer getICodec() {
		return iCodec;
	}
	public void setICodec(Integer codec) {
		iCodec = codec;
	}
	public Integer getINetwork() {
		return iNetwork;
	}
	public void setINetwork(Integer network) {
		iNetwork = network;
	}
	public Integer getTrigger() {
		return trigger;
	}
	public void setTrigger(Integer trigger) {
		this.trigger = trigger;
	}
}
