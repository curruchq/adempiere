package com.conversant.model;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.logging.Level;

import org.compiere.util.CLogger;

public class RadiusAccountInvoice
{
	/** Logger						*/
	private static CLogger log = CLogger.getCLogger(RadiusAccountInvoice.class);
	
	private Integer radAcctId = null;
	private Integer invoiceId = null;
	private Integer invoiceLineId = null;
	private Boolean active = null;
	
	private boolean valid = false;
	
	public static RadiusAccountInvoice get(Object[] dbRow)
	{
		RadiusAccountInvoice radiusAccountInvoice = new RadiusAccountInvoice(dbRow);
		return radiusAccountInvoice.isValid() ? radiusAccountInvoice : null;
	}
	
	private RadiusAccountInvoice(Object[] dbRow)
	{
		try
		{
			radAcctId = ((Long)dbRow[0]).intValue();
			invoiceId = ((Integer)dbRow[1]);
			invoiceLineId = ((Integer)dbRow[2]);
			active = ((Boolean)dbRow[3]);

			valid = true; // to indicate that no class cast exceptions were raised
		}
		catch (ClassCastException ex)
		{
			valid = false;
			log.log(Level.SEVERE, "Error casting from db result to attribute, table may have changed", ex);
		}
	}

	public Integer getRadAcctId()
	{
		return radAcctId;
	}

	public void setRadAcctId(Integer radAcctId)
	{
		this.radAcctId = radAcctId;
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

	public boolean isValid()
	{
		return valid;
	}

	public void setValid(boolean valid)
	{
		this.valid = valid;
	}

	public void setActive(Boolean active)
	{
		this.active = active;
	}
}
