package com.conversant.webservice;

import javax.jws.WebService;

@WebService()
public interface Admin extends GenericWebService
{
	public StandardResponse createBusinessPartner(CreateBusinessPartnerRequest createBusinessPartnerRequest);
	public StandardResponse readBusinessPartner(ReadBusinessPartnerRequest readBusinessPartnerRequest);
	public StandardResponse updateBusinessPartner(UpdateBusinessPartnerRequest updateBusinessPartnerRequest);
	public StandardResponse deleteBusinessPartner(DeleteBusinessPartnerRequest deleteBusinessPartnerRequest);
	
	public ReadBusinessPartnersResponse readBusinessPartnersByGroup(ReadBusinessPartnersByGroupRequest readBusinessPartnersByGroupRequest);
	
	public StandardResponse createBusinessPartnerLocation(CreateBusinessPartnerLocationRequest createBusinessLocationPartnerRequest);
	public StandardResponse createLocation(CreateLocationRequest createLocationRequest);
	public StandardResponse createUser(CreateUserRequest createUserRequest);
	
	public StandardResponse createSubscription(CreateSubscriptionRequest createSubscriptionRequest);
	public StandardResponse readSubscription(ReadSubscriptionRequest readSubscriptionRequest);
	public StandardResponse updateSubscription(UpdateSubscriptionRequest updateSubscriptionRequest);
	public StandardResponse deleteSubscription(DeleteSubscriptionRequest deleteSubscriptionRequest);
	
	public ReadSubscriptionsResponse readSubscriptions(ReadSubscriptionsRequest readSubscriptionsRequest);
}
