package com.conversant.webservice;

import javax.jws.WebService;

@WebService()
public interface Admin extends GenericWebService
{
	//public StandardResponse createBusinessPartner(CreateBusinessPartnerRequest createBusinessPartnerRequest);
	public CreateBusinessPartnerResponse createBusinessPartner(CreateBusinessPartnerRequest createBusinessPartnerRequest);
	public ReadBusinessPartnerResponse readBusinessPartner(ReadBusinessPartnerRequest readBusinessPartnerRequest);
	public StandardResponse updateBusinessPartner(UpdateBusinessPartnerRequest updateBusinessPartnerRequest);
	public StandardResponse deleteBusinessPartner(DeleteBusinessPartnerRequest deleteBusinessPartnerRequest);
	
	public ReadBusinessPartnersResponse readBusinessPartnersByGroup(ReadBusinessPartnersByGroupRequest readBusinessPartnersByGroupRequest);
	public ReadBusinessPartnerResponse readBusinessPartnerBySearchKey(ReadBusinessPartnerBySearchKeyRequest readBusinessPartnerBySearchKeyRequest);
	public ReadBPLocationResponse readBPLocations(ReadBPLocationRequest readBPLocationRequest);
	
	public StandardResponse createUser(CreateUserRequest createUserRequest);
	public ReadUserResponse readUser(ReadUserRequest readUserRequest);
	public StandardResponse updateUser(UpdateUserRequest updateUserRequest);
	public StandardResponse deleteUser(DeleteUserRequest deleteUserRequest);
	
	public StandardResponse createBusinessPartnerLocation(CreateBusinessPartnerLocationRequest createBusinessLocationPartnerRequest);
	public StandardResponse updateBusinessPartnerLocation(UpdateBusinessPartnerLocationRequest updateBusinessLocationPartnerRequest);
	public StandardResponse createLocation(CreateLocationRequest createLocationRequest);
	
	public StandardResponse createSubscription(CreateSubscriptionRequest createSubscriptionRequest);
	public ReadSubscriptionResponse readSubscription(ReadSubscriptionRequest readSubscriptionRequest);
	public StandardResponse updateSubscription(UpdateSubscriptionRequest updateSubscriptionRequest);
	public StandardResponse deleteSubscription(DeleteSubscriptionRequest deleteSubscriptionRequest);
	
	public ReadSubscriptionsResponse readSubscriptions(ReadSubscriptionsRequest readSubscriptionsRequest);
	
	public StandardResponse createOrder(CreateOrderRequest createOrderRequest);
	public ReadOrderResponse readOrder(ReadOrderRequest readOrderRequest);
	public StandardResponse updateOrder(UpdateOrderRequest updateOrderRequest);
	public StandardResponse deleteOrder(DeleteOrderRequest deleteOrderRequest);
	
	public ReadOrderDIDsResponse readOrderDIDs(ReadOrderDIDsRequest readOrderDIDsRequest);
	
	public ReadUsersByEmailResponse readUsersByEmail(ReadUsersByEmailRequest readUsersByEmailRequest);
	
	public ReadOrderNumberPortsResponse readOrderNumberPorts(ReadOrderNumberPortsRequest readOrderNumberPortsRequest);
	
	public ReadSubscribedNumbersResponse readSubscribedNumbers(ReadSubscribedNumbersRequest readSubscribedNumbersRequest);
	
	public StandardResponse createUserRole(CreateUserRoleRequest createUserRoleRequest);
	public StandardResponse readUserRole(ReadUserRoleRequest readUserRoleRequest);
	public StandardResponse updateUserRole(UpdateUserRoleRequest updateUserRoleRequest);
	public StandardResponse deleteUserRole(DeleteUserRoleRequest deleteUserRoleRequest);
	
	public ReadRolesResponse readRoles(ReadRolesRequest readRolesRequest);
	
	public ReadUsersByBusinessPartnerResponse readUsersByBusinessPartner(ReadUsersByBusinessPartnerRequest readUsersByBusinessPartnerRequest);
	
	public ReadCallRecordingResponse readCallRecording(ReadCallRecordingRequest readCallRecordingRequest);
	
	public ReadProductBPPriceResponse readProductBPPrice(ReadProductBPPriceRequest readProductBPPriceRequest);
	
	public ReadProductResponse readProduct(ReadProductRequest readProductRequest);
	
	public ReadOrganizationResponse readOrganization(ReadOrganizationRequest readOrganizationRequest);
	
	public ReadOrderLinesResponse readOrderLines(ReadOrderLinesRequest readOrderLinesRequest);
}
