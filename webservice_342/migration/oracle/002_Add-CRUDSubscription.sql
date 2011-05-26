-- ------------------------------------------------------ adminWebservice
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','createSubscription',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'createSubscription',50002,52017)
;

INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','readSubscription',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'readSubscription',50002,52018)
;

INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','updateSubscription',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'updateSubscription',50002,52019)
;

INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','deleteSubscription',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'deleteSubscription',50002,52020)
;

INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','readSubscriptions',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'readSubscriptions',50002,52021)
;

-- ------------------------------------------------------ AD-createSubscription-Intalio
INSERT INTO WS_WebServiceType (AD_Client_ID,AD_Org_ID,Created,CreatedBy,Description,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID,WS_WebServiceType_ID) VALUES (0,0,TO_DATE('2010-04-19 19:46:34','YYYY-MM-DD HH24:MI:SS'),100,'','Y','AD-createSubscription-Intalio',TO_DATE('2010-04-19 19:46:34','YYYY-MM-DD HH24:MI:SS'),100,'AD-createSubscription-Intalio',50002,52017,50074)
;
INSERT INTO WS_WebService_Para (AD_Client_ID,AD_Org_ID,ConstantValue,Created,CreatedBy,IsActive,ParameterName,ParameterType,Updated,UpdatedBy,WS_WebService_Para_ID,WS_WebServiceType_ID) VALUES (0,0,'1000000',TO_DATE('2010-04-19 19:47:23','YYYY-MM-DD HH24:MI:SS'),100,'Y','AD_Client_ID','C',TO_DATE('2010-04-19 19:47:23','YYYY-MM-DD HH24:MI:SS'),100,50222,50074)
;
INSERT INTO WS_WebService_Para (AD_Client_ID,AD_Org_ID,ConstantValue,Created,CreatedBy,IsActive,ParameterName,ParameterType,Updated,UpdatedBy,WS_WebService_Para_ID,WS_WebServiceType_ID) VALUES (0,0,'1000001',TO_DATE('2010-04-19 19:47:43','YYYY-MM-DD HH24:MI:SS'),100,'Y','AD_Org_ID','C',TO_DATE('2010-04-19 19:47:43','YYYY-MM-DD HH24:MI:SS'),100,50223,50074)
;
INSERT INTO WS_WebService_Para (AD_Client_ID,AD_Org_ID,ConstantValue,Created,CreatedBy,IsActive,ParameterName,ParameterType,Updated,UpdatedBy,WS_WebService_Para_ID,WS_WebServiceType_ID) VALUES (0,0,'50004',TO_DATE('2010-04-19 19:47:51','YYYY-MM-DD HH24:MI:SS'),100,'Y','AD_Role_ID','C',TO_DATE('2010-04-19 19:47:51','YYYY-MM-DD HH24:MI:SS'),100,50224,50074)
;
INSERT INTO WS_WebServiceTypeAccess (AD_Client_ID,AD_Org_ID,AD_Role_ID,Created,CreatedBy,IsActive,IsReadWrite,Updated,UpdatedBy,WS_WebServiceType_ID) VALUES (0,0,50004,TO_DATE('2010-04-19 19:48:12','YYYY-MM-DD HH24:MI:SS'),100,'Y','Y',TO_DATE('2010-04-19 19:48:12','YYYY-MM-DD HH24:MI:SS'),100,50074)
;

-- ------------------------------------------------------ AD-readSubscription-Intalio
INSERT INTO WS_WebServiceType (AD_Client_ID,AD_Org_ID,Created,CreatedBy,Description,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID,WS_WebServiceType_ID) VALUES (0,0,TO_DATE('2010-04-19 19:46:34','YYYY-MM-DD HH24:MI:SS'),100,'','Y','AD-readSubscription-Intalio',TO_DATE('2010-04-19 19:46:34','YYYY-MM-DD HH24:MI:SS'),100,'AD-readSubscription-Intalio',50002,52018,50075)
;
INSERT INTO WS_WebService_Para (AD_Client_ID,AD_Org_ID,ConstantValue,Created,CreatedBy,IsActive,ParameterName,ParameterType,Updated,UpdatedBy,WS_WebService_Para_ID,WS_WebServiceType_ID) VALUES (0,0,'1000000',TO_DATE('2010-04-19 19:47:23','YYYY-MM-DD HH24:MI:SS'),100,'Y','AD_Client_ID','C',TO_DATE('2010-04-19 19:47:23','YYYY-MM-DD HH24:MI:SS'),100,50225,50075)
;
INSERT INTO WS_WebService_Para (AD_Client_ID,AD_Org_ID,ConstantValue,Created,CreatedBy,IsActive,ParameterName,ParameterType,Updated,UpdatedBy,WS_WebService_Para_ID,WS_WebServiceType_ID) VALUES (0,0,'1000001',TO_DATE('2010-04-19 19:47:43','YYYY-MM-DD HH24:MI:SS'),100,'Y','AD_Org_ID','C',TO_DATE('2010-04-19 19:47:43','YYYY-MM-DD HH24:MI:SS'),100,50226,50075)
;
INSERT INTO WS_WebService_Para (AD_Client_ID,AD_Org_ID,ConstantValue,Created,CreatedBy,IsActive,ParameterName,ParameterType,Updated,UpdatedBy,WS_WebService_Para_ID,WS_WebServiceType_ID) VALUES (0,0,'50004',TO_DATE('2010-04-19 19:47:51','YYYY-MM-DD HH24:MI:SS'),100,'Y','AD_Role_ID','C',TO_DATE('2010-04-19 19:47:51','YYYY-MM-DD HH24:MI:SS'),100,50227,50075)
;
INSERT INTO WS_WebServiceTypeAccess (AD_Client_ID,AD_Org_ID,AD_Role_ID,Created,CreatedBy,IsActive,IsReadWrite,Updated,UpdatedBy,WS_WebServiceType_ID) VALUES (0,0,50004,TO_DATE('2010-04-19 19:48:12','YYYY-MM-DD HH24:MI:SS'),100,'Y','Y',TO_DATE('2010-04-19 19:48:12','YYYY-MM-DD HH24:MI:SS'),100,50075)
;

-- ------------------------------------------------------ AD-updateSubscription-Intalio
INSERT INTO WS_WebServiceType (AD_Client_ID,AD_Org_ID,Created,CreatedBy,Description,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID,WS_WebServiceType_ID) VALUES (0,0,TO_DATE('2010-04-19 19:46:34','YYYY-MM-DD HH24:MI:SS'),100,'','Y','AD-updateSubscription-Intalio',TO_DATE('2010-04-19 19:46:34','YYYY-MM-DD HH24:MI:SS'),100,'AD-updateSubscription-Intalio',50002,52019,50076)
;
INSERT INTO WS_WebService_Para (AD_Client_ID,AD_Org_ID,ConstantValue,Created,CreatedBy,IsActive,ParameterName,ParameterType,Updated,UpdatedBy,WS_WebService_Para_ID,WS_WebServiceType_ID) VALUES (0,0,'1000000',TO_DATE('2010-04-19 19:47:23','YYYY-MM-DD HH24:MI:SS'),100,'Y','AD_Client_ID','C',TO_DATE('2010-04-19 19:47:23','YYYY-MM-DD HH24:MI:SS'),100,50228,50076)
;
INSERT INTO WS_WebService_Para (AD_Client_ID,AD_Org_ID,ConstantValue,Created,CreatedBy,IsActive,ParameterName,ParameterType,Updated,UpdatedBy,WS_WebService_Para_ID,WS_WebServiceType_ID) VALUES (0,0,'1000001',TO_DATE('2010-04-19 19:47:43','YYYY-MM-DD HH24:MI:SS'),100,'Y','AD_Org_ID','C',TO_DATE('2010-04-19 19:47:43','YYYY-MM-DD HH24:MI:SS'),100,50229,50076)
;
INSERT INTO WS_WebService_Para (AD_Client_ID,AD_Org_ID,ConstantValue,Created,CreatedBy,IsActive,ParameterName,ParameterType,Updated,UpdatedBy,WS_WebService_Para_ID,WS_WebServiceType_ID) VALUES (0,0,'50004',TO_DATE('2010-04-19 19:47:51','YYYY-MM-DD HH24:MI:SS'),100,'Y','AD_Role_ID','C',TO_DATE('2010-04-19 19:47:51','YYYY-MM-DD HH24:MI:SS'),100,50230,50076)
;
INSERT INTO WS_WebServiceTypeAccess (AD_Client_ID,AD_Org_ID,AD_Role_ID,Created,CreatedBy,IsActive,IsReadWrite,Updated,UpdatedBy,WS_WebServiceType_ID) VALUES (0,0,50004,TO_DATE('2010-04-19 19:48:12','YYYY-MM-DD HH24:MI:SS'),100,'Y','Y',TO_DATE('2010-04-19 19:48:12','YYYY-MM-DD HH24:MI:SS'),100,50076)
;

-- ------------------------------------------------------ AD-deleteSubscription-Intalio
INSERT INTO WS_WebServiceType (AD_Client_ID,AD_Org_ID,Created,CreatedBy,Description,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID,WS_WebServiceType_ID) VALUES (0,0,TO_DATE('2010-04-19 19:46:34','YYYY-MM-DD HH24:MI:SS'),100,'','Y','AD-deleteSubscription-Intalio',TO_DATE('2010-04-19 19:46:34','YYYY-MM-DD HH24:MI:SS'),100,'AD-deleteSubscription-Intalio',50002,52020,50077)
;
INSERT INTO WS_WebService_Para (AD_Client_ID,AD_Org_ID,ConstantValue,Created,CreatedBy,IsActive,ParameterName,ParameterType,Updated,UpdatedBy,WS_WebService_Para_ID,WS_WebServiceType_ID) VALUES (0,0,'1000000',TO_DATE('2010-04-19 19:47:23','YYYY-MM-DD HH24:MI:SS'),100,'Y','AD_Client_ID','C',TO_DATE('2010-04-19 19:47:23','YYYY-MM-DD HH24:MI:SS'),100,50231,50077)
;
INSERT INTO WS_WebService_Para (AD_Client_ID,AD_Org_ID,ConstantValue,Created,CreatedBy,IsActive,ParameterName,ParameterType,Updated,UpdatedBy,WS_WebService_Para_ID,WS_WebServiceType_ID) VALUES (0,0,'1000001',TO_DATE('2010-04-19 19:47:43','YYYY-MM-DD HH24:MI:SS'),100,'Y','AD_Org_ID','C',TO_DATE('2010-04-19 19:47:43','YYYY-MM-DD HH24:MI:SS'),100,50232,50077)
;
INSERT INTO WS_WebService_Para (AD_Client_ID,AD_Org_ID,ConstantValue,Created,CreatedBy,IsActive,ParameterName,ParameterType,Updated,UpdatedBy,WS_WebService_Para_ID,WS_WebServiceType_ID) VALUES (0,0,'50004',TO_DATE('2010-04-19 19:47:51','YYYY-MM-DD HH24:MI:SS'),100,'Y','AD_Role_ID','C',TO_DATE('2010-04-19 19:47:51','YYYY-MM-DD HH24:MI:SS'),100,50233,50077)
;
INSERT INTO WS_WebServiceTypeAccess (AD_Client_ID,AD_Org_ID,AD_Role_ID,Created,CreatedBy,IsActive,IsReadWrite,Updated,UpdatedBy,WS_WebServiceType_ID) VALUES (0,0,50004,TO_DATE('2010-04-19 19:48:12','YYYY-MM-DD HH24:MI:SS'),100,'Y','Y',TO_DATE('2010-04-19 19:48:12','YYYY-MM-DD HH24:MI:SS'),100,50077)
;

-- ------------------------------------------------------ AD-readSubscriptions-Intalio
INSERT INTO WS_WebServiceType (AD_Client_ID,AD_Org_ID,Created,CreatedBy,Description,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID,WS_WebServiceType_ID) VALUES (0,0,TO_DATE('2010-04-19 19:46:34','YYYY-MM-DD HH24:MI:SS'),100,'','Y','AD-readSubscriptions-Intalio',TO_DATE('2010-04-19 19:46:34','YYYY-MM-DD HH24:MI:SS'),100,'AD-readSubscriptions-Intalio',50002,52021,50078)
;
INSERT INTO WS_WebService_Para (AD_Client_ID,AD_Org_ID,ConstantValue,Created,CreatedBy,IsActive,ParameterName,ParameterType,Updated,UpdatedBy,WS_WebService_Para_ID,WS_WebServiceType_ID) VALUES (0,0,'1000000',TO_DATE('2010-04-19 19:47:23','YYYY-MM-DD HH24:MI:SS'),100,'Y','AD_Client_ID','C',TO_DATE('2010-04-19 19:47:23','YYYY-MM-DD HH24:MI:SS'),100,50234,50078)
;
INSERT INTO WS_WebService_Para (AD_Client_ID,AD_Org_ID,ConstantValue,Created,CreatedBy,IsActive,ParameterName,ParameterType,Updated,UpdatedBy,WS_WebService_Para_ID,WS_WebServiceType_ID) VALUES (0,0,'1000001',TO_DATE('2010-04-19 19:47:43','YYYY-MM-DD HH24:MI:SS'),100,'Y','AD_Org_ID','C',TO_DATE('2010-04-19 19:47:43','YYYY-MM-DD HH24:MI:SS'),100,50235,50078)
;
INSERT INTO WS_WebService_Para (AD_Client_ID,AD_Org_ID,ConstantValue,Created,CreatedBy,IsActive,ParameterName,ParameterType,Updated,UpdatedBy,WS_WebService_Para_ID,WS_WebServiceType_ID) VALUES (0,0,'50004',TO_DATE('2010-04-19 19:47:51','YYYY-MM-DD HH24:MI:SS'),100,'Y','AD_Role_ID','C',TO_DATE('2010-04-19 19:47:51','YYYY-MM-DD HH24:MI:SS'),100,50236,50078)
;
INSERT INTO WS_WebServiceTypeAccess (AD_Client_ID,AD_Org_ID,AD_Role_ID,Created,CreatedBy,IsActive,IsReadWrite,Updated,UpdatedBy,WS_WebServiceType_ID) VALUES (0,0,50004,TO_DATE('2010-04-19 19:48:12','YYYY-MM-DD HH24:MI:SS'),100,'Y','Y',TO_DATE('2010-04-19 19:48:12','YYYY-MM-DD HH24:MI:SS'),100,50078)
;

exit;