-- ------------------------------------------------------ adminWebservice
INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','createUserRole',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'createUserRole',50002,52030)
;

INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','readUserRole',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'readUserRole',50002,52031)
;

INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','updateUserRole',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'updateUserRole',50002,52032)
;

INSERT INTO WS_WebServiceMethod (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID) VALUES (0,0,TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'Y','deleteUserRole',TO_DATE('2010-04-19 19:24:05','YYYY-MM-DD HH24:MI:SS'),100,'deleteUserRole',50002,52033)
;

-- ------------------------------------------------------ AD-createUserRole-Drupal
INSERT INTO WS_WebServiceType (AD_Client_ID,AD_Org_ID,Created,CreatedBy,Description,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID,WS_WebServiceType_ID) VALUES (0,0,TO_DATE('2010-04-19 19:46:34','YYYY-MM-DD HH24:MI:SS'),100,'','Y','AD-createUserRole-Drupal',TO_DATE('2010-04-19 19:46:34','YYYY-MM-DD HH24:MI:SS'),100,'AD-createUserRole-Drupal',50002,52030,50124)
;
INSERT INTO WS_WebService_Para (AD_Client_ID,AD_Org_ID,ConstantValue,Created,CreatedBy,IsActive,ParameterName,ParameterType,Updated,UpdatedBy,WS_WebService_Para_ID,WS_WebServiceType_ID) VALUES (0,0,'1000000',TO_DATE('2010-04-19 19:47:23','YYYY-MM-DD HH24:MI:SS'),100,'Y','AD_Client_ID','C',TO_DATE('2010-04-19 19:47:23','YYYY-MM-DD HH24:MI:SS'),100,50372,50124)
;
INSERT INTO WS_WebService_Para (AD_Client_ID,AD_Org_ID,ConstantValue,Created,CreatedBy,IsActive,ParameterName,ParameterType,Updated,UpdatedBy,WS_WebService_Para_ID,WS_WebServiceType_ID) VALUES (0,0,'1000001',TO_DATE('2010-04-19 19:47:43','YYYY-MM-DD HH24:MI:SS'),100,'Y','AD_Org_ID','C',TO_DATE('2010-04-19 19:47:43','YYYY-MM-DD HH24:MI:SS'),100,50373,50124)
;
INSERT INTO WS_WebService_Para (AD_Client_ID,AD_Org_ID,ConstantValue,Created,CreatedBy,IsActive,ParameterName,ParameterType,Updated,UpdatedBy,WS_WebService_Para_ID,WS_WebServiceType_ID) VALUES (0,0,'50004',TO_DATE('2010-04-19 19:47:51','YYYY-MM-DD HH24:MI:SS'),100,'Y','AD_Role_ID','C',TO_DATE('2010-04-19 19:47:51','YYYY-MM-DD HH24:MI:SS'),100,50374,50124)
;
INSERT INTO WS_WebServiceTypeAccess (AD_Client_ID,AD_Org_ID,AD_Role_ID,Created,CreatedBy,IsActive,IsReadWrite,Updated,UpdatedBy,WS_WebServiceType_ID) VALUES (0,0,50005,TO_DATE('2010-04-19 19:48:12','YYYY-MM-DD HH24:MI:SS'),100,'Y','Y',TO_DATE('2010-04-19 19:48:12','YYYY-MM-DD HH24:MI:SS'),100,50124)
;

-- ------------------------------------------------------ AD-readUserRole-Drupal
INSERT INTO WS_WebServiceType (AD_Client_ID,AD_Org_ID,Created,CreatedBy,Description,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID,WS_WebServiceType_ID) VALUES (0,0,TO_DATE('2010-04-19 19:46:34','YYYY-MM-DD HH24:MI:SS'),100,'','Y','AD-readUserRole-Drupal',TO_DATE('2010-04-19 19:46:34','YYYY-MM-DD HH24:MI:SS'),100,'AD-readUserRole-Drupal',50002,52031,50125)
;
INSERT INTO WS_WebService_Para (AD_Client_ID,AD_Org_ID,ConstantValue,Created,CreatedBy,IsActive,ParameterName,ParameterType,Updated,UpdatedBy,WS_WebService_Para_ID,WS_WebServiceType_ID) VALUES (0,0,'1000000',TO_DATE('2010-04-19 19:47:23','YYYY-MM-DD HH24:MI:SS'),100,'Y','AD_Client_ID','C',TO_DATE('2010-04-19 19:47:23','YYYY-MM-DD HH24:MI:SS'),100,50375,50125)
;
INSERT INTO WS_WebService_Para (AD_Client_ID,AD_Org_ID,ConstantValue,Created,CreatedBy,IsActive,ParameterName,ParameterType,Updated,UpdatedBy,WS_WebService_Para_ID,WS_WebServiceType_ID) VALUES (0,0,'1000001',TO_DATE('2010-04-19 19:47:43','YYYY-MM-DD HH24:MI:SS'),100,'Y','AD_Org_ID','C',TO_DATE('2010-04-19 19:47:43','YYYY-MM-DD HH24:MI:SS'),100,50376,50125)
;
INSERT INTO WS_WebService_Para (AD_Client_ID,AD_Org_ID,ConstantValue,Created,CreatedBy,IsActive,ParameterName,ParameterType,Updated,UpdatedBy,WS_WebService_Para_ID,WS_WebServiceType_ID) VALUES (0,0,'50004',TO_DATE('2010-04-19 19:47:51','YYYY-MM-DD HH24:MI:SS'),100,'Y','AD_Role_ID','C',TO_DATE('2010-04-19 19:47:51','YYYY-MM-DD HH24:MI:SS'),100,50377,50125)
;
INSERT INTO WS_WebServiceTypeAccess (AD_Client_ID,AD_Org_ID,AD_Role_ID,Created,CreatedBy,IsActive,IsReadWrite,Updated,UpdatedBy,WS_WebServiceType_ID) VALUES (0,0,50005,TO_DATE('2010-04-19 19:48:12','YYYY-MM-DD HH24:MI:SS'),100,'Y','Y',TO_DATE('2010-04-19 19:48:12','YYYY-MM-DD HH24:MI:SS'),100,50125)
;

-- ------------------------------------------------------ AD-updateUserRole-Drupal
INSERT INTO WS_WebServiceType (AD_Client_ID,AD_Org_ID,Created,CreatedBy,Description,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID,WS_WebServiceType_ID) VALUES (0,0,TO_DATE('2010-04-19 19:46:34','YYYY-MM-DD HH24:MI:SS'),100,'','Y','AD-updateUserRole-Drupal',TO_DATE('2010-04-19 19:46:34','YYYY-MM-DD HH24:MI:SS'),100,'AD-updateUserRole-Drupal',50002,52032,50126)
;
INSERT INTO WS_WebService_Para (AD_Client_ID,AD_Org_ID,ConstantValue,Created,CreatedBy,IsActive,ParameterName,ParameterType,Updated,UpdatedBy,WS_WebService_Para_ID,WS_WebServiceType_ID) VALUES (0,0,'1000000',TO_DATE('2010-04-19 19:47:23','YYYY-MM-DD HH24:MI:SS'),100,'Y','AD_Client_ID','C',TO_DATE('2010-04-19 19:47:23','YYYY-MM-DD HH24:MI:SS'),100,50378,50126)
;
INSERT INTO WS_WebService_Para (AD_Client_ID,AD_Org_ID,ConstantValue,Created,CreatedBy,IsActive,ParameterName,ParameterType,Updated,UpdatedBy,WS_WebService_Para_ID,WS_WebServiceType_ID) VALUES (0,0,'1000001',TO_DATE('2010-04-19 19:47:43','YYYY-MM-DD HH24:MI:SS'),100,'Y','AD_Org_ID','C',TO_DATE('2010-04-19 19:47:43','YYYY-MM-DD HH24:MI:SS'),100,50379,50126)
;
INSERT INTO WS_WebService_Para (AD_Client_ID,AD_Org_ID,ConstantValue,Created,CreatedBy,IsActive,ParameterName,ParameterType,Updated,UpdatedBy,WS_WebService_Para_ID,WS_WebServiceType_ID) VALUES (0,0,'50004',TO_DATE('2010-04-19 19:47:51','YYYY-MM-DD HH24:MI:SS'),100,'Y','AD_Role_ID','C',TO_DATE('2010-04-19 19:47:51','YYYY-MM-DD HH24:MI:SS'),100,50380,50126)
;
INSERT INTO WS_WebServiceTypeAccess (AD_Client_ID,AD_Org_ID,AD_Role_ID,Created,CreatedBy,IsActive,IsReadWrite,Updated,UpdatedBy,WS_WebServiceType_ID) VALUES (0,0,50005,TO_DATE('2010-04-19 19:48:12','YYYY-MM-DD HH24:MI:SS'),100,'Y','Y',TO_DATE('2010-04-19 19:48:12','YYYY-MM-DD HH24:MI:SS'),100,50126)
;

-- ------------------------------------------------------ AD-deleteUserRole-Drupal
INSERT INTO WS_WebServiceType (AD_Client_ID,AD_Org_ID,Created,CreatedBy,Description,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID,WS_WebServiceType_ID) VALUES (0,0,TO_DATE('2010-04-19 19:46:34','YYYY-MM-DD HH24:MI:SS'),100,'','Y','AD-deleteUserRole-Drupal',TO_DATE('2010-04-19 19:46:34','YYYY-MM-DD HH24:MI:SS'),100,'AD-deleteUserRole-Drupal',50002,52033,50127)
;
INSERT INTO WS_WebService_Para (AD_Client_ID,AD_Org_ID,ConstantValue,Created,CreatedBy,IsActive,ParameterName,ParameterType,Updated,UpdatedBy,WS_WebService_Para_ID,WS_WebServiceType_ID) VALUES (0,0,'1000000',TO_DATE('2010-04-19 19:47:23','YYYY-MM-DD HH24:MI:SS'),100,'Y','AD_Client_ID','C',TO_DATE('2010-04-19 19:47:23','YYYY-MM-DD HH24:MI:SS'),100,50381,50127)
;
INSERT INTO WS_WebService_Para (AD_Client_ID,AD_Org_ID,ConstantValue,Created,CreatedBy,IsActive,ParameterName,ParameterType,Updated,UpdatedBy,WS_WebService_Para_ID,WS_WebServiceType_ID) VALUES (0,0,'1000001',TO_DATE('2010-04-19 19:47:43','YYYY-MM-DD HH24:MI:SS'),100,'Y','AD_Org_ID','C',TO_DATE('2010-04-19 19:47:43','YYYY-MM-DD HH24:MI:SS'),100,50382,50127)
;
INSERT INTO WS_WebService_Para (AD_Client_ID,AD_Org_ID,ConstantValue,Created,CreatedBy,IsActive,ParameterName,ParameterType,Updated,UpdatedBy,WS_WebService_Para_ID,WS_WebServiceType_ID) VALUES (0,0,'50004',TO_DATE('2010-04-19 19:47:51','YYYY-MM-DD HH24:MI:SS'),100,'Y','AD_Role_ID','C',TO_DATE('2010-04-19 19:47:51','YYYY-MM-DD HH24:MI:SS'),100,50383,50127)
;
INSERT INTO WS_WebServiceTypeAccess (AD_Client_ID,AD_Org_ID,AD_Role_ID,Created,CreatedBy,IsActive,IsReadWrite,Updated,UpdatedBy,WS_WebServiceType_ID) VALUES (0,0,50005,TO_DATE('2010-04-19 19:48:12','YYYY-MM-DD HH24:MI:SS'),100,'Y','Y',TO_DATE('2010-04-19 19:48:12','YYYY-MM-DD HH24:MI:SS'),100,50127)
;

exit;