package com.conversant.webservice;

import javax.jws.WebService;

@WebService()
public interface Provision extends GenericWebService
{
	public StandardResponse createDIDProduct(CreateDIDProductRequest createDIDProductRequest);
	public StandardResponse createSIPProduct(CreateSIPProductRequest createSIPProductRequest);
	public StandardResponse createVoicemailProduct(CreateVoicemailProductRequest createVoicemailProductRequest);
	
	public StandardResponse createDIDSubscription(CreateDIDSubscriptionRequest addDIDSubscriptionRequest);
	public StandardResponse createSIPSubscription(CreateSIPSubscriptionRequest createSIPSubscriptionRequest);
	public StandardResponse createVoicemailSubscription(CreateVoicemailSubscriptionRequest createVoicemailSubscriptionRequest);
	
	public StandardResponse createSERSubscriber(CreateSERSubscriberRequest createSERSubscriberRequest);
	public StandardResponse createSERUserPreference(CreateSERUserPreferenceRequest createSERUserPreferenceRequest);
}
