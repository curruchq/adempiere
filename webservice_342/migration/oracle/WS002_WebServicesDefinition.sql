INSERT INTO WS_WebService (AD_Client_ID,AD_Org_ID,Created,CreatedBy,Description,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'A web service used for provisioning','Y','Provisioning Service',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Provision',50000)
;

INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','createDIDSubscription',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'addDIDSubscription',50000,50000)
;

INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','createSIPProduct',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'createSIPProduct',50000,50001)
;

INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','createSIPSubscription',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'createSIPSubscription',50000,50002)
;