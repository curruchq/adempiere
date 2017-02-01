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
	public ReadBPBankAccountResponse readBPBankAccount(ReadBPBankAccountRequest readBPBankAccountRequest);
	public StandardResponse updateBPBankAccount(UpdateBPBankAccountRequest updateBPBankAccountRequest);
	public StandardResponse deleteBPBankAccount(DeleteBPBankAccountRequest deleteBPBankAccountRequest);
	
	public StandardResponse createInvoice(CreateInvoiceRequest createInvoiceRequest);
	public ReadInvoiceResponse readInvoice(ReadInvoiceRequest readInvoiceRequest);
	public StandardResponse updateInvoice(UpdateInvoiceRequest updateInvoiceRequest);
	public StandardResponse deleteInvoice(DeleteInvoiceRequest deleteInvoiceRequest);
	
	public ReadInvoicesByBusinessPartnerResponse readInvoicesByBusinessPartner(ReadInvoicesByBusinessPartnerRequest readInvoicesByBusinessPartnerRequest);
	public ReadInvoiceLinesResponse readInvoiceLines(ReadInvoiceLinesRequest readInvoiceLinesRequest);
	
	public ReadInvoiceTaxLinesResponse readInvoiceTaxLines(ReadInvoiceTaxLinesRequest readInvoiceTaxLinesRequest);
	
	public ReadPaymentsByBusinessPartnerResponse readPaymentByBusinessPartner(ReadPaymentsByBusinessPartnerRequest readPaymentsByBusinessPartnerRequest);
	
	public StandardResponse createOneOffPayment(CreateOneOffPaymentRequest createOneOffPaymentRequest);
}
