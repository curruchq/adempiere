package org.compiere.wstore;

import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Level;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.compiere.util.CLogger;
import org.compiere.util.Util;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.conversant.model.DIDAreaCode;
import com.conversant.model.DIDCountry;

public class DIDAreaCodeListTag  extends TagSupport
{
	public static final String AREA_CODE_SELECT_NAME = "form.select.areacode";
	
	/**	Logging								*/
	private static CLogger log = CLogger.getCLogger(DIDAreaCodeListTag.class);

	/** DIDCountry to get area codes from	*/
	private String areaCodeXML = "";
	
	public void setXml(String areaCodeXML)
	{
		this.areaCodeXML = areaCodeXML;
	}
	
	public String getXml()
	{
		return areaCodeXML;
	}
	
	/**
	 *  Start Tag
	 *  @return SKIP_BODY
	 * 	@throws JspException
	 */
	public int doStartTag() throws JspException
	{	
		try
		{
			// Write
			JspWriter out = pageContext.getOut();
			out.print(getHtml(areaCodeXML));
		}
		catch (IOException ex)
		{
			log.log(Level.WARNING, "Error writing DIDAreaCodeListTag html to page", ex);
		}
		
		return (SKIP_BODY);
	}	// doStartTag
	
	/**
	 * 	End Tag - NOP
	 * 	@return EVAL_PAGE
	 * 	@throws JspException
	 */
	public int doEndTag() throws JspException
	{
		return EVAL_PAGE;
	}	//	doEndTag
	
	/**
	 * Gets HTML code on single line
	 * 
	 * @param areaCodeXML
	 * @return
	 */
	public static String getHtml(String areaCodeXML)
	{
		StringBuilder html = new StringBuilder();
		
		html.append("<label for=\"" + AREA_CODE_SELECT_NAME + "\">Area Code:</label>" + DIDServlet.NEWLINE);
		html.append("<select id=\"" + AREA_CODE_SELECT_NAME + "\" name=\"" + AREA_CODE_SELECT_NAME + "\">" + DIDServlet.NEWLINE);
		html.append(getAreaCodes(areaCodeXML));
		html.append("</select>" + DIDServlet.NEWLINE);

		return html.toString();
	}
	
	/**
	 * Gets area codes
	 * 
	 * @param XML
	 * @return 
	 */
	private static String getAreaCodes(String XML)
	{	
		if (XML != null && XML.length() > 0)
		{
			try 
			{
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = factory.newDocumentBuilder();
				
				InputSource inStream = new InputSource();
				inStream.setCharacterStream(new StringReader(XML));
				
				Document doc = db.parse(inStream);
				doc.getDocumentElement().normalize();
				
				NodeList nodeLst = doc.getDocumentElement().getChildNodes();
				if (nodeLst.getLength() > 0)
				{
					StringBuilder options = new StringBuilder();
					
					for (int i = 0; i < nodeLst.getLength(); i++) 
					{
						Node firstNode = nodeLst.item(i);
						if (firstNode.getNodeType() == Node.ELEMENT_NODE) 
						{
							boolean optionSelected = false;
							
							Element firstElement = (Element)firstNode;
							String id = firstElement.getAttribute("id");
							String selected = firstElement.getAttribute("selected");
							String desc = firstElement.getTextContent();
							options.append("<option value=\"" + id + "\"");
							if (!optionSelected && selected != null && selected.length() > 0)
							{
								optionSelected = true;
								options.append(" selected=\"selected\"");
							}
							options.append(">" + Util.maskHTML(desc) + "</option>" + DIDServlet.NEWLINE);
						}
					}
					
					return options.toString();
				}
			} 
			catch (Exception ex) 
			{
				log.log(Level.WARNING, "Error parsing XML", ex);
			}
		}
		return "<option value=\"\">None</option>" + DIDServlet.NEWLINE;
	}

//	private static String getTestXML()
//	{
//		String XML_AREA_CODE_PARENT = "areacodes";
//		String xmlAreaCodeChild = XML_AREA_CODE_PARENT.substring(0, XML_AREA_CODE_PARENT.length()-1);
//		
//		StringBuilder out = new StringBuilder("");
//
//		DIDCountry country = DIDXService.getDIDAreas("USA", "1", "211");	
//		if (country != null)
//		{			
//	        out.append("<" + XML_AREA_CODE_PARENT + ">");
//	        out.append("<" + xmlAreaCodeChild + " id='-1'>Select area code..</" + xmlAreaCodeChild + ">");
//	      
//	        for (DIDAreaCode areacode : country.getAreaCodes())
//	        {		
//	        	String value = "(" + areacode.getCode() + ") " + areacode.getDesc();
//	        	out.append("<" + xmlAreaCodeChild + " id='" + areacode.getCode() + "'>" + value + "</" + xmlAreaCodeChild + ">");			        	
//	        }
//	        
//	        out.append("</" + XML_AREA_CODE_PARENT + ">");			
//		}
//		else
//		{
//			out.append("<" + XML_AREA_CODE_PARENT + ">");
//	        out.append("<" + xmlAreaCodeChild + " id='-1'>None</" + xmlAreaCodeChild + ">");
//	        out.append("</" + XML_AREA_CODE_PARENT + ">");
//		}
//		
//		return out.toString();
//	}
	
//	public static void main(String[] args)
//	{
////		System.out.println(getHtml(getTestXML()));
//		String xml = getTestXML();
//		while (xml.contains("&"))
//    	{
//			int index = xml.indexOf("&");
//    		System.out.println(index + " ---> " + xml.substring(index-20, index+20));
//    		xml = xml.replaceFirst("&", "");
//    	}
//	}
}
