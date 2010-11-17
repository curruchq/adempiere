package com.conversant.webservice;

import javax.jws.WebService;

@WebService()
public interface Provision extends GenericWebService
{	
	public StandardResponse createDIDProduct(CreateDIDProductRequest createDIDProductRequest);
	public StandardResponse readDIDProduct(ReadDIDProductRequest readDIDProductRequest);
	public StandardResponse updateDIDProduct(UpdateDIDProductRequest updateDIDProductRequest);
	public StandardResponse deleteDIDProduct(DeleteDIDProductRequest deleteDIDProductRequest);
	
	public StandardResponse createSIPProduct(CreateSIPProductRequest createSIPProductRequest);
	public StandardResponse createVoicemailProduct(CreateVoicemailProductRequest createVoicemailProductRequest);
	
	public StandardResponse createDIDSubscription(CreateDIDSubscriptionRequest createDIDSubscriptionRequest);
	public StandardResponse createSIPSubscription(CreateSIPSubscriptionRequest createSIPSubscriptionRequest);
	public StandardResponse createVoicemailSubscription(CreateVoicemailSubscriptionRequest createVoicemailSubscriptionRequest);
	
	public StandardResponse createSubscriber(CreateSubscriberRequest createSubscriberRequest);
	public StandardResponse readSubscriber(ReadSubscriberRequest readSubscriberRequest);
	public StandardResponse updateSubscriber(UpdateSubscriberRequest updateSubscriberRequest);
	public StandardResponse deleteSubscriber(DeleteSubscriberRequest deleteSubscriberRequest);
	
	public StandardResponse createUserPreference(CreateUserPreferenceRequest createUserPreferenceRequest);
	public StandardResponse readUserPreference(ReadUserPreferenceRequest readUserPreferenceRequest);
	public StandardResponse updateUserPreferenceValue(UpdateUserPreferenceValueRequest updateUserPreferenceValueRequest);
	public StandardResponse updateUserPreferenceStartDate(UpdateUserPreferenceStartDateRequest updateUserPreferenceStartDateRequest);
	public StandardResponse updateUserPreferenceEndDate(UpdateUserPreferenceEndDateRequest updateUserPreferenceEndDateRequest);
	public StandardResponse deleteUserPreference(DeleteUserPreferenceRequest deleteUserPreferenceRequest);
	
	public StandardResponse createVoicemailUser(CreateVoicemailUserRequest createVoicemailUserRequest);
	public StandardResponse deleteVoicemailUser(DeleteVoicemailUserRequest deleteVoicemailUserRequest);
	
	public StandardResponse createVoicemailUserPreferences(CreateVoicemailUserPreferencesRequest createVoicemailUserPreferencesRequest);
	public StandardResponse deleteVoicemailUserPreferences(DeleteVoicemailUserPreferencesRequest deleteVoicemailUserPreferencesRequest);
	
	public StandardResponse createVoicemailDialPlan(CreateVoicemailDialPlanRequest createVoicemailDialPlanRequest);
	public StandardResponse deleteVoicemailDialPlan(DeleteVoicemailDialPlanRequest deleteVoicemailDialPlanRequest);
	
	public StandardResponse validateProvisionDIDParameters(ValidateProvisionDIDParametersRequest validateProvisionDIDParametersRequest);
	
	public StandardResponse createCallProduct(CreateCallProductRequest createCallProductRequest);
	public StandardResponse createCallSubscription(CreateCallSubscriptionRequest createCallSubscriptionRequest);
}
