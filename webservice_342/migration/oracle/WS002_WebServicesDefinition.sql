-- ------------------------------------------------------ Web Services Definition
INSERT INTO WS_WebService (AD_Client_ID,AD_Org_ID,Created,CreatedBy,Description,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'A generic web service','Y','Generic Web Service',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Generic',50000)
;
INSERT INTO WS_WebService (AD_Client_ID,AD_Org_ID,Created,CreatedBy,Description,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'A web service used for provisioning','Y','Provisioning Web Service',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Provision',50001)
;
INSERT INTO WS_WebService (AD_Client_ID,AD_Org_ID,Created,CreatedBy,Description,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'A web service used for administration','Y','Administration Web Service',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Admin',50002)
;

-- ------------------------------------------------------ genericWebservice
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','createTrx',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'createTrx',50000,50000)
;
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','commitTrx',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'commitTrx',50000,50001)
;
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','rollbackTrx',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'rollbackTrx',50000,50002)
;

-- ------------------------------------------------------ provisionWebservice
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','createDIDProduct',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'createDIDProduct',50001,51000)
;
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','readDIDProduct',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'readDIDProduct',50001,51001)
;
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','updateDIDProduct',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'updateDIDProduct',50001,51002)
;
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','deleteDIDProduct',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'deleteDIDProduct',50001,51003)
;
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','createSIPProduct',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'createSIPProduct',50001,51004)
;
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','readSIPProduct',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'readSIPProduct',50001,51005)
;
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','updateSIPProduct',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'updateSIPProduct',50001,51006)
;
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','deleteSIPProduct',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'deleteSIPProduct',50001,51007)
;
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','createVoicemailProduct',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'createVoicemailProduct',50001,51008)
;
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','readVoicemailProduct',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'readVoicemailProduct',50001,51009)
;
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','updateVoicemailProduct',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'updateVoicemailProduct',50001,51010)
;
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','deleteVoicemailProduct',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'deleteVoicemailProduct',50001,51011)
;
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','createDIDSubscription',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'createDIDSubscription',50001,51012)
;
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','readDIDSubscription',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'readDIDSubscription',50001,51013)
;
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','updateDIDSubscription',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'updateDIDSubscription',50001,51014)
;
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','deleteDIDSubscription',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'deleteDIDSubscription',50001,51015)
;
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','createSIPSubscription',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'createSIPSubscription',50001,51016)
;
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','readSIPSubscription',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'readSIPSubscription',50001,51017)
;
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','updateSIPSubscription',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'updateSIPSubscription',50001,51018)
;
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','deleteSIPSubscription',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'deleteSIPSubscription',50001,51019)
;
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','createVoicemailSubscription',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'createVoicemailSubscription',50001,51020)
;
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','readVoicemailSubscription',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'readVoicemailSubscription',50001,51021)
;
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','updateVoicemailSubscription',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'updateVoicemailSubscription',50001,51022)
;
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','deleteVoicemailSubscription',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'deleteVoicemailSubscription',50001,51023)
;
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','createSubscriber',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'createSubscriber',50001,51024)
;
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','readSubscriber',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'readSubscriber',50001,51025)
;
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','updateSubscriber',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'updateSubscriber',50001,51026)
;
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','deleteSubscriber',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'deleteSubscriber',50001,51027)
;
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','createUserPreference',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'createUserPreference',50001,51028)
;
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','readUserPreference',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'readUserPreference',50001,51029)
;
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','updateUserPreference',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'updateUserPreference',50001,51030)
;
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','deleteUserPreference',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'deleteUserPreference',50001,51031)
;
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','createVoicemailUser',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'createVoicemailUser',50001,51032)
;
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','readVoicemailUser',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'readVoicemailUser',50001,51033)
;
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','updateVoicemailUser',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'updateVoicemailUser',50001,51034)
;
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','deleteVoicemailUser',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'deleteVoicemailUser',50001,51035)
;
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','createVoicemailUserPreferences',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'createVoicemailUserPreferences',50001,51036)
;
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','readVoicemailUserPreferences',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'readVoicemailUserPreferences',50001,51037)
;
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','updateVoicemailUserPreferences',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'updateVoicemailUserPreferences',50001,51038)
;
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','deleteVoicemailUserPreferences',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'deleteVoicemailUserPreferences',50001,51039)
;
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','createVoicemailDialplan',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'createVoicemailDialplan',50001,51040)
;
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','readVoicemailDialplan',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'readVoicemailDialplan',50001,51041)
;
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','updateVoicemailDialplan',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'updateVoicemailDialplan',50001,51042)
;
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','deleteVoicemailDialplan',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'deleteVoicemailDialplan',50001,51043)
;
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','validateProvisionDIDParameters',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'validateProvisionDIDParameters',50001,51044)
;

-- ------------------------------------------------------ adminWebservice
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','createBusinessPartner',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'createBusinessPartner',50002,52000)
;
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','readBusinessPartner',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'readBusinessPartner',50002,52001)
;
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','updateBusinessPartner',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'updateBusinessPartner',50002,52002)
;
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','deleteBusinessPartner',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'deleteBusinessPartner',50002,52003)
;
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','createBusinessPartnerLocation',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'createBusinessPartnerLocation',50002,52004)
;
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','readBusinessPartnerLocation',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'readBusinessPartnerLocation',50002,52005)
;
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','updateBusinessPartnerLocation',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'updateBusinessPartnerLocation',50002,52006)
;
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','deleteBusinessPartnerLocation',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'deleteBusinessPartnerLocation',50002,52007)
;
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','createLocation',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'createLocation',50002,52008)
;
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','readLocation',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'readLocation',50002,52009)
;
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','updateLocation',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'updateLocation',50002,52010)
;
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','deleteLocation',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'deleteLocation',50002,52011)
;
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','createUser',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'createUser',50002,52012)
;
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','readUser',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'readUser',50002,52013)
;
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','updateUser',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'updateUser',50002,52014)
;
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','deleteUser',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'deleteUser',50002,52015)
;

exit;