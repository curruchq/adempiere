package com.conversant.model;

import org.compiere.util.CLogger;

public class RadiusAccountInvoice
{
	/** Logger						*/
	private static CLogger log = CLogger.getCLogger(RadiusAccountInvoice.class);
	
	private RadiusAccount radAcct = null;
	private Integer invoiceId = null;
	private Integer invoiceLineId = null;
	private Boolean active = null;

	public RadiusAccountInvoice(RadiusAccount radAcct, Integer invoiceId, Integer invoiceLineId, Boolean active)
	{
		setRadAcct(radAcct);
		setInvoiceId(invoiceId);
		setInvoiceLineId(invoiceLineId);
		setActive(active);
	}

	public RadiusAccount getRadAcct()
	{
		return radAcct;
	}

	public void setRadAcct(RadiusAccount radAcct)
	{
		this.radAcct = radAcct;
	}

	public Integer getInvoiceId()
	{
		return invoiceId;
	}

	public void setInvoiceId(Integer invoiceId)
	{
		this.invoiceId = invoiceId;
	}

	public Integer getInvoiceLineId()
	{
		return invoiceLineId;
	}

	public void setInvoiceLineId(Integer invoiceLineId)
	{
		this.invoiceLineId = invoiceLineId;
	}

	public Boolean getActive()
	{
		return active;
	}

	public void setActive(Boolean active)
	{
		this.active = active;
	}
}
