<%@ include file="/WEB-INF/jspf/page.jspf" %>
<c:if test='${empty webUser || !webUser.loggedIn}'>
  <c:redirect url='loginServlet?ForwardTo=paymentInfo.jsp'/>
</c:if>
<c:if test='${empty payment}'>
  <c:redirect url='index.jsp'/>
</c:if>
<html>
<!--
  - Author:  Jorg Janke
  - Version: $Id: paymentInfo.jsp,v 1.3 2006/05/06 02:13:56 mdeaelfweald Exp $
  - Compiere ERP & CRM Smart Business Solution - Copyright (c) 1999-2003 Jorg Janke
  - - -
  - Web Payment Info
  - Variables: webOrder, webUser, payment
  -->
<head>
<%@ include file="/WEB-INF/jspf/head.jspf" %>
<title><c:out value='${ctx.name}'/> - My Payment Info</title>
<style>
#fieldsetPayment div {
	margin: 2px;
}
#fieldsetPayment .errorBackground {
	background:#FFCE7B;
}
#fieldsetPayment .required {
	background:transparent url("required.gif") no-repeat scroll left center;
	color:White;
	padding:0pt 0pt 0pt 8px;
}
#fieldsetPayment .error {
	background-color:#FFCE7B;
}
</style>
</head>
<body><div id="page">
<%@ include file="/WEB-INF/jspf/header.jspf" %>
<div id="main">
	<%@ include file="/WEB-INF/jspf/menu.jspf" %>
	<div id="content"> 
	  <c:if test='${not empty webOrder}'>
	  <h1>Thank you for your Order</h1>
      <table class="contentTable">
        <tr> 
          <th>Order</th>
          <th>Lines</th>
          <th>Shipping</th>
          <th>Tax</th>
          <th>Total</th>
        </tr>
        <tr> 
          <td class="oddRow">&nbsp;<c:out value='${webOrder.documentNo}'/></td>
          <td class="oddRow amount">&nbsp;<fmt:formatNumber value='${webOrder.totalLines}' type="currency" currencySymbol=""/></td>
          <td class="oddRow amount">&nbsp;<fmt:formatNumber value='${webOrder.freightAmt}' type="currency" currencySymbol=""/></td>
          <td class="oddRow amount">&nbsp;<fmt:formatNumber value='${webOrder.taxAmt}' type="currency" currencySymbol=""/></td>
          <td class="oddRow amount"><b><c:out value='${webOrder.currencyISO}'/>&nbsp;<fmt:formatNumber value='${webOrder.grandTotal}' type="currency" currencySymbol=""/></b></td>
        </tr>
      </table>
	  </c:if>
	  <c:if test='${empty webOrder && empty ccvPayment}'>
	  <h1>Payment of  <c:out value='${payment.currencyISO}'/> <fmt:formatNumber value='${payment.payAmt}' type="currency" currencySymbol=""/></h1>
	  </c:if>
      <h2>Please enter your payment information</h2>
      <c:if test="${not empty payment.r_PnRef}">
        <p><b>Payment Info: <c:out value='${payment.r_PnRef}'/></b></p>
      </c:if>

	  <cws:paymentInfoForm/>
	  
      <div id="validationInfo">
          <div id="visaBox"><img src="images/visaCID.jpg" height="80" width="135"/></div>
          <div id="amexBox"><img src="images/amexCID.jpg" height="80" width="135"/></div>
          <strong>Credit Card Validation Code <br/>
              <font size="-1">(Card ID)<br/></font></strong>
          <font size="-1">Visa and Mastercard: 3 digits - back<br/>
            American Express: 4 digits - front</font>
	      <p>&nbsp;</p>
      </div>
	</div>
</div>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
</div></body>
</html>
