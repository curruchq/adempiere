package org.compiere.wstore;

public class DIDDescription 
{
	private String countryCode = "";
	private String areaCode = "";
	private String perMinCharges = "";
	private String freeMins = "";
	
	public DIDDescription(String countryCode, String areaCode, String perMinCharges, String freeMins)
	{
		setCountryCode(countryCode);
		setAreaCode(areaCode);
		setPerMinCharges(perMinCharges);
		setFreeMins(freeMins);
	}
	
	public String getCountryCode() {
		return countryCode;
	}
	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}
	public String getAreaCode() {
		return areaCode;
	}
	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}
	public String getPerMinCharges() {
		return perMinCharges;
	}
	public void setPerMinCharges(String perMinCharges) {
		this.perMinCharges = perMinCharges;
	}
	public String getFreeMins() {
		return freeMins;
	}
	public void setFreeMins(String freeMins) {
		this.freeMins = freeMins;
	}
}
