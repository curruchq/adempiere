package com.conversant.webservice;

import javax.jws.WebService;

@WebService()
public interface Accounting extends GenericWebService
{
	public StandardResponse createPayment(CreatePaymentRequest createPaymentRequest);
	public StandardResponse readPayment(ReadPaymentRequest readPaymentRequest);
	public StandardResponse updatePayment(UpdatePaymentRequest updatePaymentRequest);
	public StandardResponse deletePayment(DeletePaymentRequest deletePaymentRequest);

	public StandardResponse processPayment(ProcessPaymentRequest processPaymentRequest);
	
	public StandardResponse createBPBankAccount(CreateBPBankAccountRequest createBPBankAccountRequest);
	public StandardResponse readBPBankAccount(ReadBPBankAccountRequest readBPBankAccountRequest);
	public StandardResponse updateBPBankAccount(UpdateBPBankAccountRequest updateBPBankAccountRequest);
	public StandardResponse deleteBPBankAccount(DeleteBPBankAccountRequest deleteBPBankAccountRequest);
}