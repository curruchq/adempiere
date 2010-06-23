package com.conversant.webservice;

import javax.jws.WebService;

@WebService()
public interface Admin extends GenericWebService
{
	public StandardResponse createBusinessPartner(CreateBusinessPartnerRequest createBusinessPartnerRequest);
	public StandardResponse createBusinessPartnerLocation(CreateBusinessPartnerLocationRequest createBusinessLocationPartnerRequest);
	public StandardResponse createLocation(CreateLocationRequest createLocationRequest);
	public StandardResponse createUser(CreateUserRequest createUserRequest);
}
