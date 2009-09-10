package com.conversant.model;

import java.util.Date;

/**
 * 	WebUser Contract
 * 
 *	@author Josh Hill
 * 	@version $Id: WebUserContract.java,v 1.0 2008/10/31 14:58:54 jhill Exp $
 */
public class WebUserContract
{
	private String name;
	private String ip;
	private String filename;
	private Date date;
	
	public WebUserContract()
	{
		
	}
	
	public WebUserContract(String name, String ip, String filename, Date date)
	{
		this.name = name;
		this.ip = ip;
		this.filename = filename;
		this.date = date;
	}
	
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	public String getIp()
	{
		return ip;
	}
	public void setIp(String ip)
	{
		this.ip = ip;
	}
	public Date getDate()
	{
		return date;
	}
	public void setDate(Date date)
	{
		this.date = date;
	}

	public String getFilename()
	{
		return filename;
	}

	public void setFilename(String filename)
	{
		this.filename = filename;
	}
}
