package com.conversant.webservice;

import javax.jws.WebService;

import com.conversant.webservice.CreateDIDSubscriptionRequest;
import com.conversant.webservice.StandardResponse;

@WebService()
public interface Provision extends GenericWebService
{
	public StandardResponse createDIDSubscription(CreateDIDSubscriptionRequest addDIDSubscriptionRequest);
	public StandardResponse createSIPProduct(CreateSIPProductRequest createSIPProductRequest);
	public StandardResponse createSIPSubscription(CreateSIPSubscriptionRequest createSIPSubscriptionRequest);
}
