package org.compiere.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.compiere.model.MInvoice;
import org.compiere.model.MPayment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class Flo2CashClient {

	private static int MAX_CONNECTION_ATTEMPTS = 3;
	/**	Logger							*/
	protected CLogger			log = CLogger.getCLogger (getClass());

	public String establishConnectionFlo2Cash(String PlanId, String Amount,String DueDate, String Reference, MInvoice invoice) {
		String paymentStatus = null;
		log.info("Connecting to Flo2Cash");
		try {
			final SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
			final SOAPConnection soapConnection = soapConnectionFactory.createConnection();
			// Next, create the actual message
			MessageFactory messageFactory = MessageFactory.newInstance();
			SOAPMessage message = messageFactory.createMessage();
			String soapActionStr = Flo2CashConstants.FLO2CASHWEBSERVICES.get("SOAP_ACTION");

			// Create objects for the message parts
			SOAPPart soapPart = message.getSOAPPart();
			SOAPEnvelope envelope = soapPart.getEnvelope();
			envelope.addNamespaceDeclaration("ver",Flo2CashConstants.FLO2CASHWEBSERVICES.get("VERSION"));

			MimeHeaders mimeHeader = message.getMimeHeaders();
			// change header's attribute
			mimeHeader.setHeader("SOAPAction", soapActionStr);

			SOAPBody body = envelope.getBody();
			// Populate the body
			// Create the main element and namespace
			SOAPElement bodyElement = body.addChildElement(envelope.createName(Flo2CashConstants.FLO2CASHWEBSERVICES.get("PAYMENT_METHOD"), "ver", ""));

			// Add content
			bodyElement.addChildElement(Flo2CashConstants.FLO2CASHWEBSERVICES.get("REQUEST_USERNAME"), "ver").addTextNode("100950");
			bodyElement.addChildElement(Flo2CashConstants.FLO2CASHWEBSERVICES.get("REQUEST_PASSWORD"), "ver").addTextNode("dbi33jfg");
			SOAPElement sperline = bodyElement.addChildElement(Flo2CashConstants.FLO2CASHWEBSERVICES.get("REQUEST_PLANDETAILSNODE"), "ver");
			sperline.addChildElement(Flo2CashConstants.FLO2CASHWEBSERVICES.get("REQUEST_PLANID"), "ver").addTextNode(PlanId);
			sperline.addChildElement(Flo2CashConstants.FLO2CASHWEBSERVICES.get("REQUEST_AMOUNT"), "ver").addTextNode(invoice.getGrandTotal().toString());
			sperline.addChildElement(Flo2CashConstants.FLO2CASHWEBSERVICES.get("REQUEST_PAYMENTDATE"), "ver").addTextNode(DueDate);
			sperline.addChildElement(Flo2CashConstants.FLO2CASHWEBSERVICES.get("REQUEST_REFERENCE"), "ver").addTextNode(invoice.getDocumentNo());
			sperline.addChildElement(Flo2CashConstants.FLO2CASHWEBSERVICES.get("REQUEST_PARTICULAR"), "ver").addTextNode("Conversant");
			// Save the message
			message.saveChanges();

			// Check the input
			log.info("\nREQUEST:\n"+message.toString());
			message.writeTo(System.out);
		
			String destination = Flo2CashConstants.FLO2CASHWEBSERVICES.get("DEMO_DESTINATION_WEBSERVICE");
			// Send the message
			SOAPMessage reply = soapConnection.call(message, destination);
			paymentStatus = processFlo2CashResponse(reply, invoice);
			soapConnection.close();
		} catch (SOAPException e) {
			log.severe("Failed to Connect to Flo2Cash : "+e.getMessage());
		} catch (IOException e) {
			log.severe("Failed to Connect to Flo2Cash : "+e.getMessage());
		}

		return paymentStatus;
	}

	public String processFlo2CashResponse(SOAPMessage reply, MInvoice invoice) {
		log.info("Process response from Flo2Cash");
		// Create the transformer
		try {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			// Extract the content of the reply
			Source sourceContent = reply.getSOAPPart().getContent();
			// Set the output for the transformation
			Writer out = new StringWriter();
			StreamResult result = new StreamResult(out);
			transformer.transform(sourceContent, result);
			
			StringWriter sw = (StringWriter) result.getWriter();

			StringBuffer sb = sw.getBuffer();
			String finalstring = sb.toString();
			log.info("Response string from Flo2Cash : "+finalstring);

			// Payment related changes to Adempiere create new method
			// processResponse()
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(new ByteArrayInputStream(finalstring.getBytes("UTF-8")));
			if (reply.getSOAPBody().hasFault()) {
				SOAPFault fault = reply.getSOAPBody().getFault();
				String string = fault.getFaultString();
				log.warning("Fault String from Flo2Cash : "+string);
				return string;
			} else 
			{
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
						MPayment payment = new MPayment(invoice.getCtx(), 0,invoice.get_TrxName());

						payment.setDocumentNo(paymentid.getTextContent());
						payment.setDescription("Flo2Cash DD Transaction Number :"+ ddtrnxid.getTextContent());
						payment.setDateAcct(formatDate(paymentDate.getTextContent()));
						payment.setDateTrx(formatDate(paymentDate.getTextContent()));
						payment.setPayAmt(new BigDecimal(amount.getTextContent()));
						payment.setC_Currency_ID(invoice.getC_Currency_ID());
						payment.setC_BPartner_ID(invoice.getC_BPartner_ID());
						payment.setC_Invoice_ID(invoice.getC_Invoice_ID());
						payment.setC_BankAccount_ID(1000000);
						payment.setTenderType("D");
						payment.setIsReceipt(true);
						payment.setC_DocType_ID(true);
						payment.setProcessed(true);
						//payment.
						if (!payment.save(invoice.get_TrxName()))
							log.warning("Automatic payment creation failure - payment not saved");
				        
						payment.completeIt();
					}
				}
			}// no soap fault
		}// try
		catch (SOAPException e) {
			log.severe("Failed to read SOAP Body : "+e.getMessage());
		} catch (TransformerException e) {
			log.severe("Unable to parse the response : "+e.getMessage());
		} catch (IOException e) {
			log.severe("IO Exception : "+e.getMessage());
		} catch (ParserConfigurationException e) {
			log.severe("Unable to parse the response : "+e.getMessage());
		} catch (SAXException e) {
			log.severe("Unable to parse the response : "+e.getMessage());			
		}
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
	
	// changes by lavanya end
}
