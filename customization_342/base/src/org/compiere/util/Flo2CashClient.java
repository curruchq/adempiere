package org.compiere.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.compiere.model.MInvoice;
import org.compiere.model.MPayment;
import org.compiere.model.MPaymentProcessor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.rpc.ServiceException;
import javax.xml.transform.stream.StreamSource;
import org.apache.axis.AxisFault;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.soap.MessageFactoryImpl;

public class Flo2CashClient {

	/**	Logger							*/
	protected CLogger			log = CLogger.getCLogger (getClass());
    HashMap<String,String> status=new HashMap<String,String>();
	public HashMap<String,String> establishConnectionFlo2Cash(String PlanId, String Amount,String DueDate, String Reference, MInvoice invoice) {
		
		log.info("Connecting to Flo2Cash");
		Call call = null;
		String destination = Flo2CashConstants.FLO2CASHWEBSERVICES.get("DEMO_DESTINATION_WEBSERVICE");
		try {
			MPaymentProcessor pp=getFlo2CashCredentials(invoice.getCtx());
			// Next, create the actual message
			String soapActionStr = Flo2CashConstants.FLO2CASHWEBSERVICES.get("SOAP_ACTION");

			String SOAP_REQUEST="<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ver=\"http://www.flo2cash.co.nz/webservices/ddwebservice/versionone\"><SOAP-ENV:Header/><SOAP-ENV:Body><ver:SchedulePerInvoicePayment><ver:Username xmlns:ver=\"http://www.flo2cash.co.nz/webservices/ddwebservice/versionone\">"+pp.getUserID()+"</ver:Username><ver:Password xmlns:ver=\"http://www.flo2cash.co.nz/webservices/ddwebservice/versionone\">"+pp.getPassword()+"</ver:Password><ver:SchedulePerInvoicePaymentLineInput xmlns:ver=\"http://www.flo2cash.co.nz/webservices/ddwebservice/versionone\"><ver:PlanId>"+PlanId+"</ver:PlanId><ver:Amount>"+Amount+"</ver:Amount><ver:PaymentDate>"+DueDate+"</ver:PaymentDate><ver:Reference>"+invoice.getDocumentNo()+"</ver:Reference><ver:Particular>Conversant</ver:Particular></ver:SchedulePerInvoicePaymentLineInput></ver:SchedulePerInvoicePayment></SOAP-ENV:Body></SOAP-ENV:Envelope>";
			log.info(SOAP_REQUEST);
			byte[] reqBytes = SOAP_REQUEST.getBytes();
			ByteArrayInputStream bis = new ByteArrayInputStream(reqBytes);
			StreamSource ss = new StreamSource(bis);
			MessageFactoryImpl messageFactory = new MessageFactoryImpl();
			SOAPMessage msg = messageFactory.createMessage();
			SOAPPart soapPart = msg.getSOAPPart();
			soapPart.setContent(ss);
			Service service = new Service();
			call = (Call)service.createCall();
			call.setTargetEndpointAddress(pp.getHostAddress());
			call.setSOAPActionURI(soapActionStr);
			SOAPEnvelope resp = call.invoke(((org.apache.axis.SOAPPart)soapPart).getAsSOAPEnvelope());
			processFlo2CashResponse(resp, invoice);
		} catch (SOAPException e) {
			log.severe("Failed to Connect to Flo2Cash : "+e.getMessage());
			status.put("failure", e.getMessage());
			return status;
		}
		catch (ServiceException serviceexception) {
			log.severe(serviceexception.getMessage());
			status.put("failure", serviceexception.getMessage());
			return status ;
			}
		catch (AxisFault axisfault) {
			log.severe(axisfault.getMessage());
			status.put("failure", axisfault.getMessage());
			return status;
			}

		return status;
	}

	public String processFlo2CashResponse(SOAPEnvelope reply, MInvoice invoice) {
		log.info("Process response from Flo2Cash");
		MPayment payment =null;
		// Create the transformer
		try {
			String finalstring = reply.getBody().toString();
			log.info("Response string from Flo2Cash : "+finalstring);

			// Payment related changes to Adempiere create new method
			// processResponse()
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(new ByteArrayInputStream(finalstring.getBytes("UTF-8")));
			
				NodeList flo2cash = doc.getElementsByTagName(Flo2CashConstants.FLO2CASHWEBSERVICES.get("RESPONSE_PLANDETAILSNODE"));
				for (int i = 0; i < flo2cash.getLength(); i++) {
					Element schedulePayment = (Element) flo2cash.item(i);
					Node planid = schedulePayment.getElementsByTagName(Flo2CashConstants.FLO2CASHWEBSERVICES.get("RESPONSE_PLANID")).item(0);
					Node amount = schedulePayment.getElementsByTagName(Flo2CashConstants.FLO2CASHWEBSERVICES.get("RESPONSE_AMOUNT")).item(0);
					Node paymentDate = schedulePayment.getElementsByTagName(Flo2CashConstants.FLO2CASHWEBSERVICES.get("RESPONSE_PAYMENTDATE")).item(0);
					Node paymentid = schedulePayment.getElementsByTagName(Flo2CashConstants.FLO2CASHWEBSERVICES.get("RESPONSE_PAYMENTID")).item(0);
					Node status = schedulePayment.getElementsByTagName(Flo2CashConstants.FLO2CASHWEBSERVICES.get("RESPONSE_STATUSID")).item(0);
					Node reference = schedulePayment.getElementsByTagName(Flo2CashConstants.FLO2CASHWEBSERVICES.get("RESPONSE_REFERENCE")).item(0);
					Node particular = schedulePayment.getElementsByTagName(Flo2CashConstants.FLO2CASHWEBSERVICES.get("RESPONSE_PARTICULAR")).item(0);
					Node ddtrnxid = schedulePayment.getElementsByTagName(Flo2CashConstants.FLO2CASHWEBSERVICES.get("RESPONSE_DDTRANSACTIONNUMBER")).item(0);
					log.info("Plan ID = "+planid.getTextContent()+" Amount = "+amount.getTextContent()+" Payment Date = "+paymentDate.getTextContent()+" Payment ID = "+paymentid.getTextContent()+" Payment Status = "+status.getTextContent()+" Reference = "+reference.getTextContent()+" Particular = "+particular.getTextContent()+" Direct Debit Transaction Number = "+ddtrnxid.getTextContent());

					if (status.getTextContent().equals("0")) {
						log.info("Creating a payment record");
						payment = new MPayment(invoice.getCtx(), 0,invoice.get_TrxName());

						payment.setDocumentNo(paymentid.getTextContent());
						payment.setDescription("Flo2Cash DD Transaction Number :"+ ddtrnxid.getTextContent());
						payment.setDateAcct(formatDate(paymentDate.getTextContent()));
						payment.setDateTrx(formatDate(paymentDate.getTextContent()));
						payment.setPayAmt(new BigDecimal(amount.getTextContent()));
						payment.setC_Currency_ID(invoice.getC_Currency_ID());
						payment.setC_BPartner_ID(invoice.getC_BPartner_ID());
						//payment.setC_Invoice_ID(invoice.getC_Invoice_ID());
						payment.setC_BankAccount_ID(1000000);
						payment.setTenderType("D");
						payment.setC_DocType_ID(true);
						if (!payment.save())
							log.warning("Automatic payment creation failure - payment not saved");
						//invoice.setIsPaid(true);
				       // payment.processIt("CO");
				        //payment.save(invoice.get_TrxName());
					}
				}
		}// try
		catch (SOAPException e) {
			log.severe("Failed to read SOAP Body : "+e.getMessage());
			status.put("failure", e.getMessage());
		} catch (IOException e) {
			log.severe("IO Exception : "+e.getMessage());
			status.put("failure", e.getMessage());
		} catch (ParserConfigurationException e) {
			log.severe("Unable to parse the response : "+e.getMessage());
			status.put("failure", e.getMessage());
		} catch (SAXException e) {
			log.severe("Unable to parse the response : "+e.getMessage());
			status.put("failure", e.getMessage());
		}
		status.put("success", "Payment No : "+payment.getDocumentNo());
		return "success";
	}

	public Timestamp formatDate(String date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Timestamp newdate = null;
		try {
			java.util.Date d = sdf.parse(date);
			newdate = new Timestamp(d.getTime());
		} catch (ParseException e) {
			log.severe("Unable to parse the date : "+e.getMessage());
		}
		return newdate;
	}
	public MPaymentProcessor getFlo2CashCredentials(Properties props)
	{
		MPaymentProcessor paymentprocessor=null;
		String sql="SELECT * FROM C_PAYMENTPROCESSOR WHERE NAME='Flo2CashCredentials'";
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement (sql, null);
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next ())
			{
				paymentprocessor = new MPaymentProcessor(props, rs, null);
			}
			rs.close ();
			pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, sql, e);
		}
		try
		{
			if (pstmt != null)
				pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			pstmt = null;
		}
		return paymentprocessor;
	}
	// changes by lavanya end
}