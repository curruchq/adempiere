package com.conversant.webservice;

import javax.jws.WebService;

@WebService(endpointInterface = "com.conversant.webservice.Admin")
public class AdminImpl extends GenericWebServiceImpl implements Admin
{
	public StandardResponse createBusinessPartner(CreateBusinessPartnerRequest createBusinessPartnerRequest)
	{
		return null;
	}
}
