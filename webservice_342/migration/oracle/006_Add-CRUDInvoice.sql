-- ------------------------------------------------------ accountingWebservice
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','createInvoice',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'createInvoice',50003,53009)
;

INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','readInvoice',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'createInvoice',50003,53010)
;

INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','updateInvoice',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'createInvoice',50003,53011)
;

INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','deleteInvoice',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'createInvoice',50003,53012)
;

INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','readInvoicesByBusinessPartner',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'readInvoicesByBusinessPartner',50003,53013)
;

-- ------------------------------------------------------ AC-createInvoice-Intalio
INSERT INTO WS_WebServiceType (AD_Client_ID,AD_Org_ID,Created,CreatedBy,Description,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID,WS_WebServiceType_ID) VALUES (0,0,TO_DATE('2010-04-19 19:46:34','YYYY-MM-DD HH24:MI:SS'),100,'','Y','AC-createInvoice-Intalio',TO_DATE('2010-04-19 19:46:34','YYYY-MM-DD HH24:MI:SS'),100,'AC-createInvoice-Intalio',50003,53009,50092)
;
INSERT INTO WS_WebService_Para (AD_Client_ID,AD_Org_ID,ConstantValue,Created,CreatedBy,IsActive,ParameterName,ParameterType,Updated,UpdatedBy,WS_WebService_Para_ID,WS_WebServiceType_ID) VALUES (0,0,'1000000',TO_DATE('2010-04-19 19:47:23','YYYY-MM-DD HH24:MI:SS'),100,'Y','AD_Client_ID','C',TO_DATE('2010-04-19 19:47:23','YYYY-MM-DD HH24:MI:SS'),100,50276,50092)
;
INSERT INTO WS_WebService_Para (AD_Client_ID,AD_Org_ID,ConstantValue,Created,CreatedBy,IsActive,ParameterName,ParameterType,Updated,UpdatedBy,WS_WebService_Para_ID,WS_WebServiceType_ID) VALUES (0,0,'1000001',TO_DATE('2010-04-19 19:47:43','YYYY-MM-DD HH24:MI:SS'),100,'Y','AD_Org_ID','C',TO_DATE('2010-04-19 19:47:43','YYYY-MM-DD HH24:MI:SS'),100,50277,50092)
;
INSERT INTO WS_WebService_Para (AD_Client_ID,AD_Org_ID,ConstantValue,Created,CreatedBy,IsActive,ParameterName,ParameterType,Updated,UpdatedBy,WS_WebService_Para_ID,WS_WebServiceType_ID) VALUES (0,0,'50004',TO_DATE('2010-04-19 19:47:51','YYYY-MM-DD HH24:MI:SS'),100,'Y','AD_Role_ID','C',TO_DATE('2010-04-19 19:47:51','YYYY-MM-DD HH24:MI:SS'),100,50278,50092)
;
INSERT INTO WS_WebServiceTypeAccess (AD_Client_ID,AD_Org_ID,AD_Role_ID,Created,CreatedBy,IsActive,IsReadWrite,Updated,UpdatedBy,WS_WebServiceType_ID) VALUES (0,0,50004,TO_DATE('2010-04-19 19:48:12','YYYY-MM-DD HH24:MI:SS'),100,'Y','Y',TO_DATE('2010-04-19 19:48:12','YYYY-MM-DD HH24:MI:SS'),100,50092)
;

-- ------------------------------------------------------ AC-readInvoice-Intalio
INSERT INTO WS_WebServiceType (AD_Client_ID,AD_Org_ID,Created,CreatedBy,Description,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID,WS_WebServiceType_ID) VALUES (0,0,TO_DATE('2010-04-19 19:46:34','YYYY-MM-DD HH24:MI:SS'),100,'','Y','AC-readInvoice-Intalio',TO_DATE('2010-04-19 19:46:34','YYYY-MM-DD HH24:MI:SS'),100,'AC-readInvoice-Intalio',50003,53010,50093)
;
INSERT INTO WS_WebService_Para (AD_Client_ID,AD_Org_ID,ConstantValue,Created,CreatedBy,IsActive,ParameterName,ParameterType,Updated,UpdatedBy,WS_WebService_Para_ID,WS_WebServiceType_ID) VALUES (0,0,'1000000',TO_DATE('2010-04-19 19:47:23','YYYY-MM-DD HH24:MI:SS'),100,'Y','AD_Client_ID','C',TO_DATE('2010-04-19 19:47:23','YYYY-MM-DD HH24:MI:SS'),100,50279,50093)
;
INSERT INTO WS_WebService_Para (AD_Client_ID,AD_Org_ID,ConstantValue,Created,CreatedBy,IsActive,ParameterName,ParameterType,Updated,UpdatedBy,WS_WebService_Para_ID,WS_WebServiceType_ID) VALUES (0,0,'1000001',TO_DATE('2010-04-19 19:47:43','YYYY-MM-DD HH24:MI:SS'),100,'Y','AD_Org_ID','C',TO_DATE('2010-04-19 19:47:43','YYYY-MM-DD HH24:MI:SS'),100,50280,50093)
;
INSERT INTO WS_WebService_Para (AD_Client_ID,AD_Org_ID,ConstantValue,Created,CreatedBy,IsActive,ParameterName,ParameterType,Updated,UpdatedBy,WS_WebService_Para_ID,WS_WebServiceType_ID) VALUES (0,0,'50004',TO_DATE('2010-04-19 19:47:51','YYYY-MM-DD HH24:MI:SS'),100,'Y','AD_Role_ID','C',TO_DATE('2010-04-19 19:47:51','YYYY-MM-DD HH24:MI:SS'),100,50281,50093)
;
INSERT INTO WS_WebServiceTypeAccess (AD_Client_ID,AD_Org_ID,AD_Role_ID,Created,CreatedBy,IsActive,IsReadWrite,Updated,UpdatedBy,WS_WebServiceType_ID) VALUES (0,0,50004,TO_DATE('2010-04-19 19:48:12','YYYY-MM-DD HH24:MI:SS'),100,'Y','Y',TO_DATE('2010-04-19 19:48:12','YYYY-MM-DD HH24:MI:SS'),100,50093)
;

-- ------------------------------------------------------ AC-updateInvoice-Intalio
INSERT INTO WS_WebServiceType (AD_Client_ID,AD_Org_ID,Created,CreatedBy,Description,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID,WS_WebServiceType_ID) VALUES (0,0,TO_DATE('2010-04-19 19:46:34','YYYY-MM-DD HH24:MI:SS'),100,'','Y','AC-updateInvoice-Intalio',TO_DATE('2010-04-19 19:46:34','YYYY-MM-DD HH24:MI:SS'),100,'AC-updateInvoice-Intalio',50003,53011,50094)
;
INSERT INTO WS_WebService_Para (AD_Client_ID,AD_Org_ID,ConstantValue,Created,CreatedBy,IsActive,ParameterName,ParameterType,Updated,UpdatedBy,WS_WebService_Para_ID,WS_WebServiceType_ID) VALUES (0,0,'1000000',TO_DATE('2010-04-19 19:47:23','YYYY-MM-DD HH24:MI:SS'),100,'Y','AD_Client_ID','C',TO_DATE('2010-04-19 19:47:23','YYYY-MM-DD HH24:MI:SS'),100,50282,50094)
;
INSERT INTO WS_WebService_Para (AD_Client_ID,AD_Org_ID,ConstantValue,Created,CreatedBy,IsActive,ParameterName,ParameterType,Updated,UpdatedBy,WS_WebService_Para_ID,WS_WebServiceType_ID) VALUES (0,0,'1000001',TO_DATE('2010-04-19 19:47:43','YYYY-MM-DD HH24:MI:SS'),100,'Y','AD_Org_ID','C',TO_DATE('2010-04-19 19:47:43','YYYY-MM-DD HH24:MI:SS'),100,50283,50094)
;
INSERT INTO WS_WebService_Para (AD_Client_ID,AD_Org_ID,ConstantValue,Created,CreatedBy,IsActive,ParameterName,ParameterType,Updated,UpdatedBy,WS_WebService_Para_ID,WS_WebServiceType_ID) VALUES (0,0,'50004',TO_DATE('2010-04-19 19:47:51','YYYY-MM-DD HH24:MI:SS'),100,'Y','AD_Role_ID','C',TO_DATE('2010-04-19 19:47:51','YYYY-MM-DD HH24:MI:SS'),100,50284,50094)
;
INSERT INTO WS_WebServiceTypeAccess (AD_Client_ID,AD_Org_ID,AD_Role_ID,Created,CreatedBy,IsActive,IsReadWrite,Updated,UpdatedBy,WS_WebServiceType_ID) VALUES (0,0,50004,TO_DATE('2010-04-19 19:48:12','YYYY-MM-DD HH24:MI:SS'),100,'Y','Y',TO_DATE('2010-04-19 19:48:12','YYYY-MM-DD HH24:MI:SS'),100,50094)
;

-- ------------------------------------------------------ AC-deleteInvoice-Intalio
INSERT INTO WS_WebServiceType (AD_Client_ID,AD_Org_ID,Created,CreatedBy,Description,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID,WS_WebServiceType_ID) VALUES (0,0,TO_DATE('2010-04-19 19:46:34','YYYY-MM-DD HH24:MI:SS'),100,'','Y','AC-deleteInvoice-Intalio',TO_DATE('2010-04-19 19:46:34','YYYY-MM-DD HH24:MI:SS'),100,'AC-deleteInvoice-Intalio',50003,53012,50095)
;
INSERT INTO WS_WebService_Para (AD_Client_ID,AD_Org_ID,ConstantValue,Created,CreatedBy,IsActive,ParameterName,ParameterType,Updated,UpdatedBy,WS_WebService_Para_ID,WS_WebServiceType_ID) VALUES (0,0,'1000000',TO_DATE('2010-04-19 19:47:23','YYYY-MM-DD HH24:MI:SS'),100,'Y','AD_Client_ID','C',TO_DATE('2010-04-19 19:47:23','YYYY-MM-DD HH24:MI:SS'),100,50285,50095)
;
INSERT INTO WS_WebService_Para (AD_Client_ID,AD_Org_ID,ConstantValue,Created,CreatedBy,IsActive,ParameterName,ParameterType,Updated,UpdatedBy,WS_WebService_Para_ID,WS_WebServiceType_ID) VALUES (0,0,'1000001',TO_DATE('2010-04-19 19:47:43','YYYY-MM-DD HH24:MI:SS'),100,'Y','AD_Org_ID','C',TO_DATE('2010-04-19 19:47:43','YYYY-MM-DD HH24:MI:SS'),100,50286,50095)
;
INSERT INTO WS_WebService_Para (AD_Client_ID,AD_Org_ID,ConstantValue,Created,CreatedBy,IsActive,ParameterName,ParameterType,Updated,UpdatedBy,WS_WebService_Para_ID,WS_WebServiceType_ID) VALUES (0,0,'50004',TO_DATE('2010-04-19 19:47:51','YYYY-MM-DD HH24:MI:SS'),100,'Y','AD_Role_ID','C',TO_DATE('2010-04-19 19:47:51','YYYY-MM-DD HH24:MI:SS'),100,50287,50095)
;
INSERT INTO WS_WebServiceTypeAccess (AD_Client_ID,AD_Org_ID,AD_Role_ID,Created,CreatedBy,IsActive,IsReadWrite,Updated,UpdatedBy,WS_WebServiceType_ID) VALUES (0,0,50004,TO_DATE('2010-04-19 19:48:12','YYYY-MM-DD HH24:MI:SS'),100,'Y','Y',TO_DATE('2010-04-19 19:48:12','YYYY-MM-DD HH24:MI:SS'),100,50095)
;

-- ------------------------------------------------------ AC-readInvoicesByBusinessPartner-Intalio
INSERT INTO WS_WebServiceType (AD_Client_ID,AD_Org_ID,Created,CreatedBy,Description,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID,WS_WebServiceType_ID) VALUES (0,0,TO_DATE('2010-04-19 19:46:34','YYYY-MM-DD HH24:MI:SS'),100,'','Y','AC-readInvoicesByBusinessPartner-Intalio',TO_DATE('2010-04-19 19:46:34','YYYY-MM-DD HH24:MI:SS'),100,'AC-readInvoicesByBusinessPartner-Intalio',50003,53013,50096)
;
INSERT INTO WS_WebService_Para (AD_Client_ID,AD_Org_ID,ConstantValue,Created,CreatedBy,IsActive,ParameterName,ParameterType,Updated,UpdatedBy,WS_WebService_Para_ID,WS_WebServiceType_ID) VALUES (0,0,'1000000',TO_DATE('2010-04-19 19:47:23','YYYY-MM-DD HH24:MI:SS'),100,'Y','AD_Client_ID','C',TO_DATE('2010-04-19 19:47:23','YYYY-MM-DD HH24:MI:SS'),100,50288,50096)
;
INSERT INTO WS_WebService_Para (AD_Client_ID,AD_Org_ID,ConstantValue,Created,CreatedBy,IsActive,ParameterName,ParameterType,Updated,UpdatedBy,WS_WebService_Para_ID,WS_WebServiceType_ID) VALUES (0,0,'1000001',TO_DATE('2010-04-19 19:47:43','YYYY-MM-DD HH24:MI:SS'),100,'Y','AD_Org_ID','C',TO_DATE('2010-04-19 19:47:43','YYYY-MM-DD HH24:MI:SS'),100,50289,50096)
;
INSERT INTO WS_WebService_Para (AD_Client_ID,AD_Org_ID,ConstantValue,Created,CreatedBy,IsActive,ParameterName,ParameterType,Updated,UpdatedBy,WS_WebService_Para_ID,WS_WebServiceType_ID) VALUES (0,0,'50004',TO_DATE('2010-04-19 19:47:51','YYYY-MM-DD HH24:MI:SS'),100,'Y','AD_Role_ID','C',TO_DATE('2010-04-19 19:47:51','YYYY-MM-DD HH24:MI:SS'),100,50290,50096)
;
INSERT INTO WS_WebServiceTypeAccess (AD_Client_ID,AD_Org_ID,AD_Role_ID,Created,CreatedBy,IsActive,IsReadWrite,Updated,UpdatedBy,WS_WebServiceType_ID) VALUES (0,0,50004,TO_DATE('2010-04-19 19:48:12','YYYY-MM-DD HH24:MI:SS'),100,'Y','Y',TO_DATE('2010-04-19 19:48:12','YYYY-MM-DD HH24:MI:SS'),100,50096)
;

exit;