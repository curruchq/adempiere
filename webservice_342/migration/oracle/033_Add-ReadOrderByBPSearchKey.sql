-- -------------------------------------------------------adminWebservice
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2015-07-24 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','readOrderByBusinessPartnerSearchKey',TO_DATE('2015-07-24 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'readOrderByBusinessPartnerSearchKey',50002,52044)
;

-- ------------------------------------------------------ AD-readOrderByBusinessPartnerSearchKey-Intalio
INSERT INTO WS_WebServiceType (AD_Client_ID,AD_Org_ID,Created,CreatedBy,Description,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID,WS_WebServiceType_ID) VALUES (0,0,TO_DATE('2015-07-24 19:46:34','YYYY-MM-DD HH24:MI:SS'),100,'','Y','AD-readOrderByBusinessPartnerSearchKey-Intalio',TO_DATE('2015-07-24 19:46:34','YYYY-MM-DD HH24:MI:SS'),100,'AD-readOrderByBusinessPartnerSearchKey-Intalio',50002,52044,50151)
;
INSERT INTO WS_WebService_Para (AD_Client_ID,AD_Org_ID,ConstantValue,Created,CreatedBy,IsActive,ParameterName,ParameterType,Updated,UpdatedBy,WS_WebService_Para_ID,WS_WebServiceType_ID) VALUES (0,0,'1000000',TO_DATE('2015-07-24 19:47:23','YYYY-MM-DD HH24:MI:SS'),100,'Y','AD_Client_ID','C',TO_DATE('2015-07-24 19:47:23','YYYY-MM-DD HH24:MI:SS'),100,50453,50151)
;
INSERT INTO WS_WebService_Para (AD_Client_ID,AD_Org_ID,ConstantValue,Created,CreatedBy,IsActive,ParameterName,ParameterType,Updated,UpdatedBy,WS_WebService_Para_ID,WS_WebServiceType_ID) VALUES (0,0,'1000001',TO_DATE('2015-07-24 19:47:43','YYYY-MM-DD HH24:MI:SS'),100,'Y','AD_Org_ID','C',TO_DATE('2015-07-24 19:47:43','YYYY-MM-DD HH24:MI:SS'),100,50454,50151)
;
INSERT INTO WS_WebService_Para (AD_Client_ID,AD_Org_ID,ConstantValue,Created,CreatedBy,IsActive,ParameterName,ParameterType,Updated,UpdatedBy,WS_WebService_Para_ID,WS_WebServiceType_ID) VALUES (0,0,'50004',TO_DATE('2015-07-24 19:47:51','YYYY-MM-DD HH24:MI:SS'),100,'Y','AD_Role_ID','C',TO_DATE('2015-07-24 19:47:51','YYYY-MM-DD HH24:MI:SS'),100,50455,50151)
;
INSERT INTO WS_WebServiceTypeAccess (AD_Client_ID,AD_Org_ID,AD_Role_ID,Created,CreatedBy,IsActive,IsReadWrite,Updated,UpdatedBy,WS_WebServiceType_ID) VALUES (0,0,50004,TO_DATE('2015-07-24 19:48:12','YYYY-MM-DD HH24:MI:SS'),100,'Y','Y',TO_DATE('2015-07-24 19:48:12','YYYY-MM-DD HH24:MI:SS'),100,50151)
;

exit; 
