package com.conversant.model;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

public class DIDAreaCode implements Serializable
{
	private String code;
	private String desc;
	
	private ArrayList<DID> allDIDs = new ArrayList<DID>();
	
	public DIDAreaCode()
	{
		this("","");
	}
	
	public DIDAreaCode(String code, String desc)
	{
		this.code = code;
		this.desc = desc;
	}

	public void sortDIDsByNumber(boolean asc)
	{
		if (asc)
			Collections.sort(allDIDs, DID.NUMBER_ASC);
		else
			Collections.sort(allDIDs, DID.NUMBER_DESC);
	}
	
	public DID getDID(String number)
	{
		for (DID did : allDIDs)
		{
			if (did.getNumber().equals(number))
				return did;
		}
		return null;
	}
	
	public boolean addDID(DID did)
	{
		if (this.getDID(did.getNumber()) == null)
		{
			return this.allDIDs.add(did);
		}
		return false;
	}
	
	public boolean removeDID(String number)
	{
		DID did = this.getDID(number);
		if (did != null)
		{
			return this.allDIDs.remove(did);
		}
		return false;
	}
	
	public ArrayList<DID> getAllDIDs()
	{
		return allDIDs;
	}

	public void setAllDIDs(ArrayList<DID> allDIDs)
	{
		this.allDIDs = allDIDs;
	}

	public String getCode()
	{
		return code;
	}

	public void setCode(String code)
	{
		this.code = code;
	}

	public String getDesc()
	{
		return desc;
	}

	public void setDesc(String desc)
	{
		this.desc = desc;
	}
}
