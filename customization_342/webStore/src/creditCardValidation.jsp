<%@ include file="/WEB-INF/jspf/page.jspf" %>
<%@page import="org.compiere.wstore.*"%>
<c:if test='${empty webUser || !webUser.loggedIn}'>
  	<c:redirect url='loginServlet?ForwardTo=creditCardValidation.jsp'/>
</c:if>
<c:if test='${empty webUser.BPBankAccount}'>
 	<c:set var="infoMsg" value="No credit card details found, click the button below to add a credit card."/>
</c:if>
<c:if test='${not empty webUser.BPBankAccount && webUser.BPBankAccount.BP_CC_Validated}'>
 	<c:set var="infoMsg" value="Your credit card has been validated."/>
</c:if>
<html>
<!--
  - Author:  Josh Hill
  - Version: $Id: creditCardValidation.jsp,v 1.2 2008/07/19 11:41:33 jhill Exp $
  - Compiere ERP & CRM Smart Business Solution - Copyright (c) 1999-2003 Jorg Janke
  - - -
  - Web Store - Credit Card Validation
  -->
<head>
	<%@ include file="/WEB-INF/jspf/head.jspf" %>
	<title><c:out value='${ctx.name}'/> - Credit Card Validation</title>
	
	<c:set var="REQUIRED_FIELD_NAMES" value="<%= ValidationServlet.REQUIRED_FIELD_NAMES %>"/>
	<c:set var="ALL_FIELDS" value="<%= ValidationServlet.ALL_FIELDS %>"/>
	<c:set var="SUCCESS_FORWARD_URL" value="<%= ValidationServlet.SUCCESS_FORWARD_URL %>"/>
	<c:set var="REFERER_NAME" value="<%= ValidationServlet.REFERER_NAME %>"/>
	
	<c:set var="THIS_JSP" value="<%= CreditCardValidationServlet.DEFAULT_JSP %>"/>
	<c:set var="SERVLET_NAME" value="<%= CreditCardValidationServlet.SERVLET_NAME %>"/>
	<c:set var="PARAM_ACTION" value="<%= CreditCardValidationServlet.PARAM_ACTION %>"/>
	<c:set var="ACTION_VALIDATE_AMOUNT" value="<%= CreditCardValidationServlet.ACTION_VALIDATE_AMOUNT %>"/>
	<c:set var="ACTION_CHARGE_ACCOUNT" value="<%= CreditCardValidationServlet.ACTION_CHARGE_ACCOUNT %>"/>
	<c:set var="ACTION_NO_BANK_ACCOUNT" value="<%= CreditCardValidationServlet.ACTION_NO_BANK_ACCOUNT %>"/>
	
</head>
<body>
	<div id="page">
		<%@ include file="/WEB-INF/jspf/header.jspf" %>
		<div id="main">
			<%@ include file="/WEB-INF/jspf/menu.jspf" %>
			<div id="content">				
				
				<!-- Display message if set -->
				<c:if test='${not empty infoMsg}'>
					<div id="infoMsg">
						<c:out value="${infoMsg}" />
					</div>
				</c:if>
				
				<!-- Shows a button when user has no BankAccount -->
				<c:if test='${empty webUser.BPBankAccount}'>
					<form action="/<c:out value='${SERVLET_NAME}' />" method="POST">	
						<div class="fieldDiv">
							<input type="submit" name="submit" id="submit" value="Add Creditcard" />
						</div>
						<input 
							type="hidden" 
							name="<c:out value='${PARAM_ACTION}' />" 
							id="<c:out value='${PARAM_ACTION}' />" 
							value="<c:out value='${ACTION_NO_BANK_ACCOUNT}' />"
						/>
					</form>
				</c:if>
				
				<!-- Checks user has a BankAccount, that the BankAccount hasn't been validated yet -->
				<c:if test='${not empty webUser.BPBankAccount && !webUser.BPBankAccount.BP_CC_Validated}'>
				
					<fieldset>
						<legend>Credit Card Validation</legend>	
						
						<!-- Check that an amount HASN'T been charged to their account -->
						<c:if test='${empty webUser.BPBankAccount.BP_CC_ValidationAmount || webUser.BPBankAccount.BP_CC_ValidationAmount == 0}'>						 		
						 	<form action="/<c:out value='${SERVLET_NAME}' />" method="POST">	
						 	
						 		<div class="fieldDiv">																	
									<span>
										By clicking on the "Charge Account" button below you agree that Conversant Ltd  
										will charge a random amount (less the $2.00) to your credit card. Please confirm  
										the amount on your credit card statement and then return to this page to complete the credit card 
										validation process. The amount charged will be credited to your 
										Conversant account and will be offset against future charges. If you do not 
										wish to continue please click the "Cancel" button.
										<br />
										<br />
										Note: You will not be able to make outbound calls to the telephone network (PSTN) 
										until your credit card has been validated.								
									</span>															
								</div>
								
								<div class="fieldDiv">
									<input 
										type="hidden" 
										name="<c:out value='${PARAM_ACTION}' />" 
										id="<c:out value='${PARAM_ACTION}' />" 
										value="<c:out value='${ACTION_CHARGE_ACCOUNT}' />"
									/>
															
									<input type="submit" name="submit" id="submit" value="Charge Account" />
									<input type="button" name="cancel" id="cancel" value="Cancel" onClick="window.location.href='/index.jsp'"/>
						 		</div>
						 	</form>			 	
						</c:if>
						
						<!-- Check that an amount HAS been charged to their account -->
						<c:if test='${not empty webUser.BPBankAccount.BP_CC_ValidationAmount && webUser.BPBankAccount.BP_CC_ValidationAmount != 0}'>
							<form action="/validationServlet" method="POST">				
								
								<div class="fieldDiv">
									<label>Card</label>
									<span>
										<c:out value='${webUser.creditCardDescription}'></c:out>
									</span>
								</div>
		
								<c:if test='${not empty webUser.BPBankAccount.BP_CC_ValidationDateCharged}'>
									<div class="fieldDiv">
										<label for="type">Date Charged</label>
										<span>
											<c:out value='${webUser.BPBankAccount.BP_CC_ValidationDateCharged}'></c:out>
										</span>
									</div>
								</c:if>
								
								<div class="fieldDiv">
									<label for="form.amount">Amount</label>
									<input 
										class="mandatory" 
										type="text" 
										name="form.amount" 
										id="form.amount" 
										value=""
									/>
									<% 
										java.util.ArrayList invalidFields = (java.util.ArrayList)request.getAttribute(org.compiere.wstore.ValidationServlet.INVALID_FIELDS);
										if (invalidFields != null && invalidFields.contains("form.amount")) 
											out.println("<span class=\"errorIcon\"></span>");
									%>
									<span><i> (From your statement e.g. $1.77)</i></span>
								</div>
		
								<div class="fieldDiv">									
									<input 
										type="hidden" 
										name="<c:out value='${REQUIRED_FIELD_NAMES}' />" 
										id="<c:out value='${REQUIRED_FIELD_NAMES}' />" 
										value="form.amount"
									/>
									<input 
										type="hidden" 
										name="<c:out value='${REFERER_NAME}' />" 
										id="<c:out value='${REFERER_NAME}' />" 
										value="<c:out value='${THIS_JSP}' />"
									/>
									<input 
										type="hidden" 
										name="<c:out value='${SUCCESS_FORWARD_URL}' />" 
										id="<c:out value='${SUCCESS_FORWARD_URL}' />" 
										value="<c:out value='${SERVLET_NAME}' />"
									/>
									<input 
										type="hidden" 
										name="<c:out value='${PARAM_ACTION}' />" 
										id="<c:out value='${PARAM_ACTION}' />" 
										value="<c:out value='${ACTION_VALIDATE_AMOUNT}' />"
									/>
									<br/>
									<label for="submit">&nbsp;</label>	
									<input type="submit" name="submit" id="submit" value="Validate" />
									<input type="reset" name="Reset" value="Clear" />
								</div>					
							</form>	
						</c:if>
					</fieldset>
				</c:if>
			</div>
		</div>
		<%@ include file="/WEB-INF/jspf/footer.jspf" %>
	</div>
</body>
</html>
