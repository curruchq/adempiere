package com.conversant.webservice;

import javax.jws.WebService;

@WebService()
public interface Admin extends GenericWebService
{
	public StandardResponse createBusinessPartner(CreateBusinessPartnerRequest createBusinessPartnerRequest);
}
