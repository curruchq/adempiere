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
	
	public StandardResponse createSubscriber(CreateSubscriberRequest createSubscriberRequest);
	public StandardResponse deleteSubscriber(DeleteSubscriberRequest deleteSubscriberRequest);
	
	public StandardResponse createUserPreference(CreateUserPreferenceRequest createUserPreferenceRequest);
	public StandardResponse deleteUserPreference(DeleteUserPreferenceRequest deleteUserPreferenceRequest);
	
	public StandardResponse createVoicemailUser(CreateVoicemailUserRequest createVoicemailUserRequest);
	public StandardResponse deleteVoicemailUser(DeleteVoicemailUserRequest deleteVoicemailUserRequest);
	
	public StandardResponse createRTExtension(CreateRTExtensionRequest createRTExtensionRequest);
	public StandardResponse deleteRTExtension(DeleteRTExtensionRequest deleteRTExtensionRequest);
}
