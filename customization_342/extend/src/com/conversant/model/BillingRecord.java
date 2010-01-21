package com.conversant.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.compiere.util.CLogger;

import com.conversant.db.BillingConnector;

// TODO: Log messages
// TODO: Check handle AM/PM correctly

public class BillingRecord 
{
	/** Logger						*/
	private static CLogger log = CLogger.getCLogger(BillingRecord.class);
	
	/** Billing Record attributes	*/
	private long id = 0;
	private long twoTalkId = 0;
	private String billingGroup = "";
	private String originNumber = "";
	private String destinationNumber = "";
	private String description = "";
	private String status = "";
	private String terminated = "";
	private Date date = null;
	private Date time = null;
	private Date dateTime = null;
	private String callLength = "";
	private String callCost = "";
	private String smartCode = "";
	private String smartCodeDescription = "";
	private String type = "";
	private String subType = "";
	private boolean mp3 = false;
	
	/** Succes indicator			*/
	private boolean valid = false;
	
	/** Date format 				*/
	private static final String DATE_FORMAT = "dd/MM/yyyy hh:mm:ss aa";
	
	/**	Date/Time pattern			*/
	private static final String DATE_TIME_PATTERN = "^([0-9]{1,2})/([0-9]{1,2})/([0-9]{4})\\s+([0-9]{1,2}):([0-9]{1,2}):([0-9]{1,2})\\s+(.{2,4})";
	
	public static BillingRecord createFromBillingFeed(String[] billingFeedRow)
	{
		BillingRecord billingRecord = new BillingRecord(billingFeedRow);
		return billingRecord.isValid() ? billingRecord : null;
	}
	
	public static BillingRecord createFromDB(Object[] dbRow)
	{
		BillingRecord billingRecord = new BillingRecord(dbRow);
		return billingRecord.isValid() ? billingRecord : null;
	}
	
	private BillingRecord(String[] billingFeedRow)
	{
		try
		{
			id = 0; // New
			twoTalkId = Long.parseLong(billingFeedRow[0]);
			billingGroup = billingFeedRow[1];
			originNumber = billingFeedRow[2];
			destinationNumber = billingFeedRow[3];
			description = billingFeedRow[4];
			status = billingFeedRow[5];
			terminated = billingFeedRow[6];
			date = parseDate(billingFeedRow[7]);
			time = parseDate(billingFeedRow[8]);
			dateTime = parseDate(billingFeedRow[9]);
			callLength = billingFeedRow[10];
			callCost = billingFeedRow[11];
			smartCode = billingFeedRow[12];
			smartCodeDescription = billingFeedRow[13];
			type = billingFeedRow[14];
			subType = billingFeedRow[15];
			mp3 = parseIsMP3(billingFeedRow[16]);
			
			setValid(true);
			
			// Check date, time and dateTime aren't null
			if (date == null)
			{
				setValid(false);
				log.warning("Date is NULL " + this.toString());
			}
			
			if (time == null)
			{
				setValid(false);
				log.warning("Time is NULL " + this.toString());
			}
			
			if (dateTime == null)
			{
				setValid(false);
				log.warning("DateTime is NULL " + this.toString());
			}
		}
		catch (Exception ex)
		{
			setValid(false);
			log.log(Level.SEVERE, "Error casting data from billing feed row", ex);
		}
	}
	
	private BillingRecord(Object[] dbRow)
	{
		try
		{
			id = (Long)dbRow[0];
			twoTalkId = (Long)dbRow[1];
			billingGroup = (String)dbRow[2];
			originNumber = (String)dbRow[3];
			destinationNumber = (String)dbRow[4];
			description = (String)dbRow[5];
			status = (String)dbRow[6];
			terminated = (String)dbRow[7];
			date = (Date)dbRow[8];
			time = (Date)dbRow[9];
			dateTime = (Date)dbRow[10];
			callLength = (String)dbRow[11];
			callCost = (String)dbRow[12];
			smartCode = (String)dbRow[13];
			smartCodeDescription = (String)dbRow[14];
			type = (String)dbRow[15];
			subType = (String)dbRow[16];
			mp3 = (Boolean)dbRow[17];
			
			setValid(true);
		}
		catch (Exception ex)
		{
			setValid(false);
			log.log(Level.SEVERE, "Error casting data from DB row", ex);
		}
	}
	
	private Date parseDate(String date)
	{
		// Check not null
		if (date == null)
			return null;
		
		// Create Simple Date Format
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		
		// Apply zeros and remove periods
		date = fixDate(date);
		
		// Parse
		try
		{
			Date parsedDate = sdf.parse(date);
			
			// Make sure date's match (SDF rolls dec 32 over to 01 jan)
		    String sParsedDate = sdf.format(parsedDate);
			if (sParsedDate.equalsIgnoreCase(date))
		    {
		    	return parsedDate;
		    }
		    else
		    	log.severe("Parsed date doesn't match original date ParsedDate[" + sParsedDate + "] OriginalDate[" + date + "]");
		}
		catch (ParseException ex)
	    {
			log.severe("Failed to parse Date[" + date + "]\n" + ex);
		}

		return null;
	}
	
	private static String fixDate(String date)
	{
		Pattern datePattern = Pattern.compile(DATE_TIME_PATTERN); 
	    Matcher dateMatcher = datePattern.matcher(date);
		
	    if (dateMatcher.find())
	    {
			String day = dateMatcher.group(1);
			String month = dateMatcher.group(2);
			String year =dateMatcher.group(3);
			String hour = dateMatcher.group(4);
			String minute = dateMatcher.group(5);
			String second = dateMatcher.group(6);
			String amPmMarker = dateMatcher.group(7);
			
			if (day.length() == 1)
				day = "0" + day;
			
			if (month.length() == 1)
				month = "0" + month;
			
			if (hour.length() == 1)
				hour = "0" + hour;
			
			if (minute.length() == 1)
				minute = "0" + minute;
			
			if (second.length() == 1)
				second = "0" + second;
			
			amPmMarker = amPmMarker.replace(".", "").toUpperCase();
			
			return day + "/" + month + "/" + year + " " + hour + ":" + minute + ":" + second + " " + amPmMarker;			
	    }
	    else
	    	log.severe("Failed to match date against pattern");

		return date;		
	}
	
	private boolean parseIsMP3(String mp3) throws ParseException
	{
		if (mp3 != null && (mp3.equals("0") || mp3.equals("1")))
			return mp3.equals("0") ? false : true;
		else 
			throw new ParseException("Failed to Parse '" + mp3 + "' to a boolean ('0' & '1' are only valid values)", 0);
	}
	
	// TODO: Handle update saves, currently only saves new records
	public boolean save()
	{
		long id = BillingConnector.addBillingRecord(this);		
		if (id > 0)
		{
			setId(id);
			return true;
		}
		else
			log.severe("Failed to save " + toString());
		
		return false;
	}
	
	public boolean delete()
	{
		return BillingConnector.removeBillingRecord(this);
	}
	
	public String toString() 
	{
		return "BillingRecord[" + getTwoTalkId() + "," + getOriginNumber() + "," + getDestinationNumber() + "]";
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getTwoTalkId() {
		return twoTalkId;
	}

	public void setTwoTalkId(long twoTalkId) {
		this.twoTalkId = twoTalkId;
	}

	public String getBillingGroup() {
		return billingGroup;
	}

	public void setBillingGroup(String billingGroup) {
		this.billingGroup = billingGroup;
	}

	public String getOriginNumber() {
		return originNumber;
	}

	public void setOriginNumber(String originNumber) {
		this.originNumber = originNumber;
	}

	public String getDestinationNumber() {
		return destinationNumber;
	}

	public void setDestinationNumber(String destinationNumber) {
		this.destinationNumber = destinationNumber;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getTerminated() {
		return terminated;
	}

	public void setTerminated(String terminated) {
		this.terminated = terminated;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public Date getDateTime() {
		return dateTime;
	}

	public void setDateTime(Date dateTime) {
		this.dateTime = dateTime;
	}

	public String getCallLength() {
		return callLength;
	}

	public void setCallLength(String callLength) {
		this.callLength = callLength;
	}

	public String getCallCost() {
		return callCost;
	}

	public void setCallCost(String callCost) {
		this.callCost = callCost;
	}

	public String getSmartCode() {
		return smartCode;
	}

	public void setSmartCode(String smartCode) {
		this.smartCode = smartCode;
	}

	public String getSmartCodeDescription() {
		return smartCodeDescription;
	}

	public void setSmartCodeDescription(String smartCodeDescription) {
		this.smartCodeDescription = smartCodeDescription;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSubType() {
		return subType;
	}

	public void setSubType(String subType) {
		this.subType = subType;
	}

	public boolean isMp3() {
		return mp3;
	}

	public void setMp3(boolean mp3) {
		this.mp3 = mp3;
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}
}
