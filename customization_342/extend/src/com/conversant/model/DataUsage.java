package com.conversant.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.compiere.util.CLogger;

import com.conversant.db.BillingConnector;

public class DataUsage
{

	/** Logger						*/
	private static CLogger log = CLogger.getCLogger(DataUsage.class);
	
	/** Data Usage attributes	*/
	private long id = 0;
	private String billingGroup = "";
	private String loginName = "";
	private String description = "";
	private String date = null;
	private String time = null;
	private String dateTime = null;
	private String cost = "";
	private String usage = "" ;
	private String accountCode = "";
	private String billDate = null;
	
	/** For JSP date display	 	*/
	private String formattedDateTime = null;
	
	/** Succes indicator			*/
	private boolean valid = false;
	
	/** Date format 				*/
	private static final String DATE_FORMAT = "dd/MM/yyyy hh:mm:ss aa";
	//private static final String DATE_FORMAT = "yyyy-MM-dd hh:mm:ss aa";
	
	/**	Date/Time pattern			*/
	private static final String DATE_TIME_PATTERN = "^([0-9]{1,2})/([0-9]{1,2})/([0-9]{4})\\s+([0-9]{1,2}):([0-9]{1,2}):([0-9]{1,2})\\s+(.{2,4})";
	
	public DataUsage()
	{
		
	}
	
	public static DataUsage createFromDataUsageFeed(String[] dataUsageFeedRow)
	{
		DataUsage duRecord = new DataUsage(dataUsageFeedRow);
		return duRecord.isValid() ? duRecord : null;
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public String getBillingGroup() {
		return billingGroup;
	}

	public void setBillingGroup(String billingGroup) {
		this.billingGroup = billingGroup;
	}
	
	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getDateTime() {
		return dateTime;
	}

	public void setDateTime(String dateTime) {
		this.dateTime = dateTime;
	}
	
	public String getCost()
	{
		return cost;
	}

	public void setCost(String cost)
	{
		this.cost = cost;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getLoginName() {
		return loginName;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}
	
	public String getUsage() {
		return usage;
	}

	public void setUsage(String usage) {
		this.usage = usage;
	}
	
	public String getBillDate() {
		return billDate;
	}

	public void setBillDate(String billDate) {
		this.billDate = billDate;
	}
	
	public String getAccountCode() {
		return accountCode;
	}

	public void setAccountCode(String accountCode) {
		this.accountCode = accountCode;
	}
	
	// TODO: Handle update saves, currently only saves new records
	public boolean save()
	{
		long id = BillingConnector.addDataUsageRecord(this);		
		if (id > 0)
		{
			setId(id);
			return true;
		}
		else
			log.severe("Failed to save " + toString());
		
		return false;
	}
	
	public String toString() 
	{
		return "Data Usage Record[" + getId() + "," + getBillDate() + "]";
	}
	
	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}
	
	private DataUsage(String[] dataUsageFeedRow)
	{
		try
		{
			id = Long.valueOf(dataUsageFeedRow[0]).longValue(); ; // New
			billingGroup = dataUsageFeedRow[1];
			loginName = dataUsageFeedRow[2];
			description = dataUsageFeedRow[3];
			date = dataUsageFeedRow[4];
			time = dataUsageFeedRow[5];
			dateTime = dataUsageFeedRow[6];
			usage = dataUsageFeedRow[7];
			cost = dataUsageFeedRow[8];
			accountCode = dataUsageFeedRow[9];
			billDate = dataUsageFeedRow[10];
			
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
	
	
}
