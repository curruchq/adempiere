package com.conversant.model;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class DIDCountry implements Serializable
{
	private String description;
	private String countryCode;
	private String countryId;
	
	private ArrayList<DIDAreaCode> areaCodes = new ArrayList<DIDAreaCode>();
	
	public DIDCountry()
	{
		this("","","");
	}
	
	public DIDCountry(String description, String countryCode, String countryId)
	{
		this.description = description;
		this.countryCode = countryCode;
		this.countryId = countryId;
	}
	
	public String toString()
	{
		return "Description=" + description + ", CountryCode=" + countryCode + ", CountryId=" + countryId;
	}
	
	public DIDAreaCode getAreaCode(String code)
	{
		for (DIDAreaCode areaCode : this.areaCodes)
		{
			if (areaCode.getCode().equals(code))
				return areaCode;
		}
		return null;
	}
	
	public boolean addAreaCode(String code, String desc)
	{
		if (this.getAreaCode(code) == null)
		{
			return this.areaCodes.add(new DIDAreaCode(code, desc));
		}
		return false;
	}
	
	public boolean removeAreaCode(String code)
	{
		DIDAreaCode areaCode = this.getAreaCode(code);
		if (areaCode != null)
		{
			return this.areaCodes.remove(areaCode);
		}
		return false;
	}
	
	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getCountryCode()
	{
		return countryCode;
	}

	public void setCountryCode(String countryCode)
	{
		this.countryCode = countryCode;
	}

	public String getCountryId()
	{
		return countryId;
	}

	public void setCountryId(String countryId)
	{
		this.countryId = countryId;
	}

	public ArrayList<DIDAreaCode> getAreaCodes()
	{
		return areaCodes;
	}

	public void setAreaCodes(ArrayList<DIDAreaCode> areaCodes)
	{
		this.areaCodes = areaCodes;
	}
	
	public void sortAreaCodesByDescription()
	{
		Collections.sort(areaCodes, AREACODE_ASC);
	}
	
	private static final Comparator<DIDAreaCode> AREACODE_ASC = new Comparator<DIDAreaCode>() 
    {
		public int compare(DIDAreaCode ac1, DIDAreaCode ac2) 
		{
			String ac1Desc = ac1.getDesc() != null ? ac1.getDesc().toLowerCase() : "";
			String ac2Desc = ac2.getDesc() != null ? ac2.getDesc().toLowerCase() : "";
			
			return ac1Desc.compareTo(ac2Desc);
		}
    };
	
	private static final Comparator<DIDCountry> DESCRIPTION_ASC = new Comparator<DIDCountry>() 
    {
		public int compare(DIDCountry country1, DIDCountry country2) 
		{
			String country1Desc = country1.getDescription() != null ? country1.getDescription().toLowerCase() : "";
			String country2Desc = country2.getDescription() != null ? country2.getDescription().toLowerCase() : "";
			
			return country1Desc.compareTo(country2Desc);
		}
    };
    
    private static final Comparator<DIDCountry> DESCRIPTION_DESC = new Comparator<DIDCountry>() 
    {
		public int compare(DIDCountry country1, DIDCountry country2) 
		{
			String country1Desc = country1.getDescription() != null ? country1.getDescription().toLowerCase() : "";
			String country2Desc = country2.getDescription() != null ? country2.getDescription().toLowerCase() : "";
			
			return country2Desc.compareTo(country1Desc);
		}
    };
	
    private static final Comparator<DIDCountry> CODE_ASC = new Comparator<DIDCountry>() 
    {
		public int compare(DIDCountry country1, DIDCountry country2) 
		{	
			return country1.getCountryCode().compareTo(country2.getCountryCode());
		}
    };
    
    private static final Comparator<DIDCountry> CODE_DESC = new Comparator<DIDCountry>() 
    {
		public int compare(DIDCountry country1, DIDCountry country2) 
		{	
			return country2.getCountryCode().compareTo(country1.getCountryCode());
		}
    };
    
    public static void sortCountriesByCode(ArrayList<DIDCountry> countries, boolean asc)
    {
    	if (asc)
			Collections.sort(countries, CODE_ASC);
		else
			Collections.sort(countries, CODE_DESC);
    }
    
	public static void sortCountriesByDescription(ArrayList<DIDCountry> countries, boolean asc)
	{
		if (asc)
			Collections.sort(countries, DESCRIPTION_ASC);
		else
			Collections.sort(countries, DESCRIPTION_DESC);
	}
}
