package com.conversant.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.logging.Level;

import org.compiere.util.CLogger;

public class SIPAccount implements Serializable
{
	/** Logger						*/
	private static CLogger log = CLogger.getCLogger(SIPAccount.class);
	
	private Integer id = null;
	private String phplib_id = null;
	private String username = null;
	private String domain = null;
	private String password = null;
	private String first_name = null;
	private String last_name = null;
	private String phone = null;
	private String email_address = null;
	private Timestamp datetime_created = null;
	private Timestamp datetime_modified = null;
	private String confirmation = null;
	private String flag = null;
	private String sendnotification = null;
	private String greeting = null;
	private String ha1 = null;
	private String ha1b = null;
	private String allow_find = null;
	private String timezone = null;
	private String rpid = null;
	private Integer domn = null;
	private String uuid = null;
	private String vmail_password = null;
	private Boolean vmail = null;
	private String ipaddr = null;
	private Long regseconds = null;
	private String port = null;
	
	private boolean valid = false;

	public static SIPAccount get(Object[] dbRow)
	{
		SIPAccount sipAccount = new SIPAccount(dbRow);
		return sipAccount.isValid() ? sipAccount : null;
	}
	
	private SIPAccount(Object[] dbRow)
	{
		try
		{
			id = (Integer)dbRow[0];
			phplib_id = (String)dbRow[1];
			username = (String)dbRow[2];
			domain = (String)dbRow[3];
			password = (String)dbRow[4];
			first_name = (String)dbRow[5];
			last_name = (String)dbRow[6];
			phone = (String)dbRow[7];
			email_address = (String)dbRow[8];
			datetime_created = (Timestamp)dbRow[9];
			datetime_modified = (Timestamp)dbRow[10];
			confirmation = (String)dbRow[11];
			flag = (String)dbRow[12];
			sendnotification = (String)dbRow[13];
			greeting = (String)dbRow[14];
			ha1 = (String)dbRow[15];
			ha1b = (String)dbRow[16];
			allow_find = (String)dbRow[17];
			timezone = (String)dbRow[18];
			rpid = (String)dbRow[19];
			domn = (Integer)dbRow[20];
			uuid = (String)dbRow[21];
			vmail_password = (String)dbRow[22];
			vmail = (Boolean)dbRow[23];
			ipaddr = (String)dbRow[24];
			regseconds = (Long)dbRow[25];
			port = (String)dbRow[26];
			
			valid = true; // to indicate that no class cast exceptions were raised
		}
		catch (ClassCastException ex)
		{
			valid = false;
			log.log(Level.SEVERE, "Error casting from db result to attribute, table may have changed", ex);
		}
	}
	
	public String toString()
	{
		return "SIPAccount[ID = " + this.getId() + ", Username = " + this.username + ", Domain = " + this.getDomain() + 
		", Phone = " + this.getPhone() + ", Email = " + this.getEmail_address() + ", UUID = " + this.getUuid() + "]";
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getPhplib_id() {
		return phplib_id;
	}

	public void setPhplib_id(String phplib_id) {
		this.phplib_id = phplib_id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getFirst_name() {
		return first_name;
	}

	public void setFirst_name(String first_name) {
		this.first_name = first_name;
	}

	public String getLast_name() {
		return last_name;
	}

	public void setLast_name(String last_name) {
		this.last_name = last_name;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail_address() {
		return email_address;
	}

	public void setEmail_address(String email_address) {
		this.email_address = email_address;
	}

	public Timestamp getDatetime_created() {
		return datetime_created;
	}

	public void setDatetime_created(Timestamp datetime_created) {
		this.datetime_created = datetime_created;
	}

	public Timestamp getDatetime_modified() {
		return datetime_modified;
	}

	public void setDatetime_modified(Timestamp datetime_modified) {
		this.datetime_modified = datetime_modified;
	}

	public String getConfirmation() {
		return confirmation;
	}

	public void setConfirmation(String confirmation) {
		this.confirmation = confirmation;
	}

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

	public String getSendnotification() {
		return sendnotification;
	}

	public void setSendnotification(String sendnotification) {
		this.sendnotification = sendnotification;
	}

	public String getGreeting() {
		return greeting;
	}

	public void setGreeting(String greeting) {
		this.greeting = greeting;
	}

	public String getHa1() {
		return ha1;
	}

	public void setHa1(String ha1) {
		this.ha1 = ha1;
	}

	public String getHa1b() {
		return ha1b;
	}

	public void setHa1b(String ha1b) {
		this.ha1b = ha1b;
	}

	public String getAllow_find() {
		return allow_find;
	}

	public void setAllow_find(String allow_find) {
		this.allow_find = allow_find;
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public String getRpid() {
		return rpid;
	}

	public void setRpid(String rpid) {
		this.rpid = rpid;
	}

	public Integer getDomn() {
		return domn;
	}

	public void setDomn(Integer domn) {
		this.domn = domn;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getVmail_password() {
		return vmail_password;
	}

	public void setVmail_password(String vmail_password) {
		this.vmail_password = vmail_password;
	}

	public Boolean getVmail() {
		return vmail;
	}

	public void setVmail(Boolean vmail) {
		this.vmail = vmail;
	}

	public String getIpaddr() {
		return ipaddr;
	}

	public void setIpaddr(String ipaddr) {
		this.ipaddr = ipaddr;
	}

	public Long getRegseconds() {
		return regseconds;
	}

	public void setRegseconds(Long regseconds) {
		this.regseconds = regseconds;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public boolean isValid() {
		return valid;
	}
}
