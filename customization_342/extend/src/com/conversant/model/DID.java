package com.conversant.model;

import java.io.Serializable;
import java.util.Comparator;

public class DID implements Serializable
{
	private String number;
	private String setupCost;
	private String monthlyCharges;
	private String perMinCharges;
	private String vendorRating;
	private String country;
	private String description;
	
	public DID()
	{
		this("","","","","","","");
	}
	
	public DID(String number, String setupCost, String monthlyCharges,
			String perMinCharges, String vendorRating, String country,
			String description)
	{
		this.number = number;
		this.setupCost = setupCost;
		this.monthlyCharges = monthlyCharges;
		this.perMinCharges = perMinCharges;
		this.vendorRating = vendorRating;
		this.country = country;
		this.description = description;
	}

	public String getNumber()
	{
		return number;
	}

	public void setNumber(String number)
	{
		this.number = number;
	}

	public String getSetupCost()
	{
		return setupCost;
	}

	public void setSetupCost(String setupCost)
	{
		this.setupCost = setupCost;
	}

	public String getMonthlyCharges()
	{
		return monthlyCharges;
	}

	public void setMonthlyCharges(String monthlyCharges)
	{
		this.monthlyCharges = monthlyCharges;
	}

	public String getPerMinCharges()
	{
		return perMinCharges;
	}

	public void setPerMinCharges(String perMinCharges)
	{
		this.perMinCharges = perMinCharges;
	}

	public String getVendorRating()
	{
		return vendorRating;
	}

	public void setVendorRating(String vendorRating)
	{
		this.vendorRating = vendorRating;
	}

	public String getCountry()
	{
		return country;
	}

	public void setCountry(String country)
	{
		this.country = country;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}
	
	public static final Comparator<DID> NUMBER_ASC = new Comparator<DID>() 
    {
		public int compare(DID did1, DID did2) 
		{
			return did1.getNumber().compareTo(did2.getNumber());
		}
    };
    
    public static final Comparator<DID> NUMBER_DESC = new Comparator<DID>() 
    {
		public int compare(DID did1, DID did2) 
		{
			return did2.getNumber().compareTo(did1.getNumber());
		}
    };
}
