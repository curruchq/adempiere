<%@ include file="/WEB-INF/jspf/page.jspf" %>
<%@page import="org.compiere.wstore.*"%>
<html>
<!--
  - Author:  Josh Hill
  - Version: $Id: didSearch.jsp,v 1.0 2007/02/14 14:24:14 Exp $
  - Compiere ERP & CRM Smart Business Solution - Copyright (c) 1999-2003 Jorg Janke
  - - -
  - Web Store DID Search
  -->
<head>
<%@ include file="/WEB-INF/jspf/head.jspf" %>  
<title><c:out value='${ctx.name}'/> - DID Search</title>

<c:set var="COUNTRY_SELECT_NAME" value="<%= DIDCountryListTag.COUNTRY_SELECT_NAME %>"/>
<c:set var="AREA_CODE_SELECT_NAME" value="<%= DIDAreaCodeListTag.AREA_CODE_SELECT_NAME %>"/>
<c:set var="ACTION_GET_AREA_CODES" value="<%= DIDServlet.ACTION_GET_AREA_CODES %>"/>
<c:set var="XML_AREA_CODE_PARENT" value="<%= DIDServlet.XML_AREA_CODE_PARENT %>"/>
<c:set var="PARAM_ACTION" value="<%= DIDServlet.PARAM_ACTION %>"/>
<c:set var="PARAM_COUNTRY" value="<%= DIDServlet.PARAM_COUNTRY %>"/>

<style>
.formDiv {
	margin:4px;
}
.loading {
	display:none;
	font-family:arial;
	font-size:95%;
	font-size-adjust:none;
	font-stretch:normal;
	font-style:italic;
	font-variant:normal;
	font-weight:bold;
	line-height:normal;
}
.loading img {
	vertical-align:middle;
}
</style>
<script type="text/javascript">
function updateAreaCodes()
{
    /**
     * Calls DIDServlet
     *
     * params: 
     *		action = 'getAreaCodes'
     *		countryParams = <countryCode>, <countryId>, <description>
     *
     * returns:
     * 		<areacodes>
	 *   		<areacode id='-1'>All</areacode>
	 *   		<areacode id='100'>(<code>) <desc></areacode>
	 *   		<areacode id='201'>(<code>) <desc></areacode>
	 * 		</areacodes>
     */

	isLoading(1);

 	var countrySelect = document.getElementById("<c:out value='${COUNTRY_SELECT_NAME}'/>");
    var countryOption = countrySelect.options[countrySelect.selectedIndex];
    var country = countryOption.attributes.getNamedItem('value').value;

	var params = new Array();
	params["<c:out value='${PARAM_ACTION}'/>"]="<c:out value='${ACTION_GET_AREA_CODES}'/>";
	params["<c:out value='${PARAM_COUNTRY}'/>"]=country;
	var loader = new AJAX.AjaxLoader("DIDServlet", updateAreaCodeListCallback, updateAreaCodeListOnError, "GET", params);
}
function updateAreaCodeListCallback()
{
	isLoading(0); 
	updateSelect(this, "<c:out value='${XML_AREA_CODE_PARENT}'/>", "<c:out value='${AREA_CODE_SELECT_NAME}'/>");
}
function updateAreaCodeListOnError()
{
	isLoading(0);
	
}
function isLoading(loading)
{
	var searchBtn = document.getElementById("form.input.submit");
	var areaCodeSelect = document.getElementById("<c:out value='${AREA_CODE_SELECT_NAME}'/>");
	if (loading == 1)
	{
		AJAX.showLoading();
		resetOptions("<c:out value='${AREA_CODE_SELECT_NAME}'/>", "Loading...", ""); 
		searchBtn.disabled = true;
		areaCodeSelect.disabled = true;
	}
	else
	{
		AJAX.hideLoading();
		searchBtn.disabled = false;
		areaCodeSelect.disabled = false;
	}
}
function resetOptions(selectId, optionText, optionValue)
{
	optionText = typeof(optionText) != 'undefined' ? optionText : 'None';
	optionValue = typeof(optionValue) != 'undefined' ? optionValue : '';
	
	var htmlSelect = document.getElementById(selectId);
	while(htmlSelect.options.length > 0)
	{
		htmlSelect.removeChild(htmlSelect.options[0]);
	}
	
	var htmlOption = document.createElement("option");
	htmlOption.setAttribute('value', optionValue)
	htmlOption.appendChild(document.createTextNode(optionText));
    htmlSelect.appendChild(htmlOption);
}
function addCountrySelectOnChange()
{
	var countrySelect = document.getElementById("<c:out value='${COUNTRY_SELECT_NAME}'/>");
	countrySelect.onchange = updateAreaCodes;
}
function showAreaCodeDiv()
{
	var areacodeDiv = document.getElementById("form.div.areacodes");
	areacodeDiv.style.display = ("");
}
</script>
</head>
<body>
<div id="page">
<%@ include file="/WEB-INF/jspf/header.jspf" %>
<div id="main">
	<%@ include file="/WEB-INF/jspf/menu.jspf" %>
	<div id="content"> 
	    <form action="DIDServlet" method="get" enctype="application/x-www-form-urlencoded">
	    	<cws:priceList/>
	    	<input type="hidden" name="<c:out value='${PARAM_ACTION}'/>" id="<c:out value='${PARAM_ACTION}'/>" value="<%=DIDServlet.ACTION_SEARCH %>"/>
	    	<input type="hidden" name="M_PriceList_ID" id="M_PriceList_ID" value="<c:out value='${priceList.priceList_ID}'/>">
		    <input type="hidden" name="M_PriceList_Version_ID" id="M_PriceList_Version_ID" value="<c:out value='${priceList.priceList_Version_ID}'/>">
	    	<fieldset>
	        	<legend>Search</legend>
	    		<div id="form.div.countries" class="formDiv">
	    			<cws:countryList/>
	    			<script type="text/javascript">
	    				addCountrySelectOnChange();
	    			</script>
	    		</div>
	    		
				<c:set var="AC_STYLE" value=""/>
				<c:if test='${empty areaCodeXML}'>
					<c:set var="AC_STYLE" value="display:none"/>
				</c:if>	    	
				<div id="form.div.areacodes" class="formDiv" style="<c:out value='${AC_STYLE}'/>">	
	    			<cws:areacodeList xml="${areaCodeXML}"/>
	    			<div id="loading" class="loading"><img src="images/loading.gif"/></div>
	    		</div>
	    		<script type="text/javascript">
	    			showAreaCodeDiv();
	    		</script>
	    		<div id="form.div.submit" class="formDiv">
	            	<label for="form.input.submit">&nbsp;</label>
	            	<input id="form.input.submit" type="submit" value="Search"/>
	            </div>
		    </fieldset>
	    </form>
	    <br/> 
	    <c:if test='${not empty didCountry}'>
			<c:if test='${not empty infoMsg}'>
				<div id="infoMsg">
					<c:out value="${infoMsg}"/>
				</div>
			</c:if>			
			<form action="DIDServlet" method="post" enctype="application/x-www-form-urlencoded">
				<cws:priceList priceList_ID="0"/>
				<input type="hidden" name="<c:out value='${PARAM_ACTION}'/>" id="<c:out value='${PARAM_ACTION}'/>" value="<%=DIDServlet.ACTION_ADD_DID_TO_WB %>"/>
				<input type="hidden" name="M_PriceList_ID" id="M_PriceList_ID" value="<c:out value='${priceList.priceList_ID}'/>">
		      	<input type="hidden" name="M_PriceList_Version_ID" id="M_PriceList_Version_ID" value="<c:out value='${priceList.priceList_Version_ID}'/>">
				<table class="contentTable">
					<tr> 
						<!-- <th>Country Code</th>
						<th>Area Code</th> -->
				    	<th>DID Number</th>
				    	<th>Setup Cost</th>
				    	<th>Monthly Charges</th>
				    	<!-- <th>Per Min Charges</th> -->
				    	<th colspan=2>Description</th>
				  	</tr>
				  	
					<c:if test='${empty didCountry.areaCodes}'>
				  		<tr>
				  			<td colspan="8">
				  				<i>No area code(s) found</i>
				  			</td>
				  		</tr>
				  	</c:if>
						
				  	<c:forEach items='${didCountry.areaCodes}' var='areaCode'>
				  		
						<c:if test='${empty areaCode.allDIDs}'>
							<tr>
								<td colspan="8">
									<i>No DIDs found for area code <c:out value='${areaCode.code}'/></i>
								</td>
							</tr>
						</c:if>
							
				  		<c:forEach items='${areaCode.allDIDs}' var='didObj' varStatus='statusCountTwo'>
					  		<jsp:useBean id="statusCountTwo" type="javax.servlet.jsp.jstl.core.LoopTagStatus" />
					  		<c:choose>
						  		<c:when test="<%= statusCountTwo.getCount() %2 == 0 %>">
						   			<c:set var="rowClass" value="evenRow"/>
						  		</c:when>
						  		<c:otherwise>
						   			<c:set var="rowClass" value="oddRow"/>
						  		</c:otherwise>
				  			</c:choose>
						  		
						   	<tr> 
						    	<!-- <td class="<c:out value='${rowClass}' />"><c:out value='${didCountry.countryCode}'/></td> -->
						     	<!-- <td class="<c:out value='${rowClass}' />"><c:out value='${areaCode.code}'/></td> -->
								<td class="<c:out value='${rowClass}' />"><c:out value='${didObj.number}'/></td>
								<td class="<c:out value='${rowClass}' />"><fmt:formatNumber value='${didObj.setupCost}' type="currency" currencySymbol=""/></td>
								<td class="<c:out value='${rowClass}' />"><fmt:formatNumber value='${didObj.monthlyCharges}' type="currency" currencySymbol=""/></td>
								<!-- <td class="<c:out value='${rowClass}' />"><c:out value='${didObj.perMinCharges}'/></td> -->
								<td class="<c:out value='${rowClass}' />"><c:out value='${didObj.description}'/></td>
								<td class="<c:out value='${rowClass}' />">
									<input type="submit" name="Add_<c:out value='${didObj.number}'/>" id="Add_<c:out value='${didObj.number}'/>" value="Add"/>
									<input type="hidden" name="countryId" value="<c:out value='${didCountry.countryId}'/>"/>
									<input type="hidden" name="countryCode" value="<c:out value='${didCountry.countryCode}'/>"/>
									<input type="hidden" name="areaCode" value="<c:out value='${areaCode.code}'/>"/>
									<input type="hidden" name="didNumber" value="<c:out value='${didObj.number}'/>"/>
									<input type="hidden" name="setupCost" value="<c:out value='${didObj.setupCost}'/>"/>
									<input type="hidden" name="monthlyCharge" value="<c:out value='${didObj.monthlyCharges}'/>"/>
									<input type="hidden" name="perMinCharges" value="<c:out value='${didObj.perMinCharges}'/>"/>
									<input type="hidden" name="vendorRating" value="<c:out value='${didObj.vendorRating}'/>"/>
									<input type="hidden" name="description" value="<c:out value='${didObj.description}'/>"/>
								</td>
						   	</tr>
					   	</c:forEach>
				    </c:forEach>
				</table>
			</form>
		</c:if>
    </div>
</div>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
</div>
</body>
</html>