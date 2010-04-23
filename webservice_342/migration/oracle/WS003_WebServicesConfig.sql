/* Add access for Conversant Admin Role */
INSERT INTO AD_Window_Access (AD_Client_ID,AD_Org_ID,AD_Role_ID,AD_Window_ID,Created,CreatedBy,IsActive,Updated,UpdatedBy) VALUES (0,0,1000000,53068,TO_DATE('2009-01-30 17:57:08','YYYY-MM-DD HH24:MI:SS'),100,'Y',TO_DATE('2009-01-30 17:57:08','YYYY-MM-DD HH24:MI:SS'),100)
;

/* Config role, user, access, web service type, parameters and access */
INSERT INTO AD_Role (AD_Client_ID,AD_Org_ID,AD_Role_ID,Allow_Info_Account,Allow_Info_Asset,Allow_Info_BPartner,Allow_Info_CashJournal,Allow_Info_InOut,Allow_Info_Invoice,Allow_Info_Order,Allow_Info_Payment,Allow_Info_Product,Allow_Info_Resource,Allow_Info_Schedule,AmtApproval,C_Currency_ID,ConfirmQueryRecords,Created,CreatedBy,IsAccessAllOrgs,IsActive,IsCanApproveOwnDoc,IsCanExport,IsCanReport,IsChangeLog,IsManual,IsPersonalAccess,IsPersonalLock,IsShowAcct,IsUseUserOrgAccess,MaxQueryRecords,Name,OverwritePriceLimit,PreferenceType,Supervisor_ID,Updated,UpdatedBy,UserDiscount,UserLevel) VALUES (1000000,0,50004,'N','N','N','N','N','N','N','N','N','N','N',0,100,0,TO_DATE('2010-04-19 19:44:19','YYYY-MM-DD HH24:MI:SS'),100,'N','Y','N','N','N','Y','Y','N','N','N','N',0,'Conversant Web Service - Intalio','N','N',1000000,TO_DATE('2010-04-19 19:44:19','YYYY-MM-DD HH24:MI:SS'),100,0.00,' CO')
;

INSERT INTO AD_User_Roles (AD_Client_ID,AD_Org_ID,AD_Role_ID,AD_User_ID,Created,CreatedBy,IsActive,Updated,UpdatedBy) VALUES (1000000,0,50004,100,TO_DATE('2010-04-19 19:44:19','YYYY-MM-DD HH24:MI:SS'),100,'Y',TO_DATE('2010-04-19 19:44:19','YYYY-MM-DD HH24:MI:SS'),100)
;

INSERT INTO AD_User_Roles (AD_Client_ID,AD_Org_ID,AD_Role_ID,AD_User_ID,Created,CreatedBy,IsActive,Updated,UpdatedBy) VALUES (1000000,0,50004,0,TO_DATE('2010-04-19 19:44:19','YYYY-MM-DD HH24:MI:SS'),100,'Y',TO_DATE('2010-04-19 19:44:19','YYYY-MM-DD HH24:MI:SS'),100)
;

INSERT INTO AD_Role_OrgAccess (AD_Client_ID,AD_Org_ID,AD_Role_ID,Created,CreatedBy,IsActive,IsReadOnly,Updated,UpdatedBy) VALUES (1000000,0,50004,TO_DATE('2010-04-19 19:44:19','YYYY-MM-DD HH24:MI:SS'),100,'Y','N',TO_DATE('2010-04-19 19:44:19','YYYY-MM-DD HH24:MI:SS'),100)
;

INSERT INTO AD_Role_OrgAccess (AD_Client_ID,AD_Org_ID,AD_Role_ID,Created,CreatedBy,IsActive,IsReadOnly,Updated,UpdatedBy) VALUES (1000000,1000001,50004,TO_DATE('2010-04-19 19:44:19','YYYY-MM-DD HH24:MI:SS'),100,'Y','N',TO_DATE('2010-04-19 19:44:19','YYYY-MM-DD HH24:MI:SS'),100)
;

INSERT INTO AD_User (AD_Client_ID,AD_Org_ID,AD_User_ID,Created,CreatedBy,IsActive,IsFullBPAccess,Name,NotificationType,Password,Processing,Updated,UpdatedBy,Value) VALUES (1000000,0,50001,TO_DATE('2010-04-19 19:44:19','YYYY-MM-DD HH24:MI:SS'),100,'Y','Y','IntalioUser','X','password','N',TO_DATE('2010-04-19 19:44:19','YYYY-MM-DD HH24:MI:SS'),100,'intaliouser')
;

INSERT INTO AD_User_Roles (AD_Client_ID,AD_Org_ID,AD_Role_ID,AD_User_ID,Created,CreatedBy,IsActive,Updated,UpdatedBy) VALUES (1000000,0,50004,50001,TO_DATE('2010-04-19 19:44:19','YYYY-MM-DD HH24:MI:SS'),100,'Y',TO_DATE('2010-04-19 19:44:19','YYYY-MM-DD HH24:MI:SS'),100)
;

INSERT INTO WS_WebServiceType (AD_Client_ID,AD_Org_ID,Created,CreatedBy,Description,IsActive,Name,Updated,UpdatedBy,Value,WS_WebService_ID,WS_WebServiceMethod_ID,WS_WebServiceType_ID) VALUES (0,0,TO_DATE('2010-04-19 19:46:34','YYYY-MM-DD HH24:MI:SS'),100,'','Y','Provision.addDIDSubscription() - Intalio',TO_DATE('2010-04-19 19:46:34','YYYY-MM-DD HH24:MI:SS'),100,'Provision-addDIDSubscription-Intalio',50000,50000,50000)
;

INSERT INTO WS_WebService_Para (AD_Client_ID,AD_Org_ID,ConstantValue,Created,CreatedBy,IsActive,ParameterName,ParameterType,Updated,UpdatedBy,WS_WebService_Para_ID,WS_WebServiceType_ID) VALUES (0,0,'1000000',TO_DATE('2010-04-19 19:47:23','YYYY-MM-DD HH24:MI:SS'),100,'Y','AD_Client_ID','C',TO_DATE('2010-04-19 19:47:23','YYYY-MM-DD HH24:MI:SS'),100,50000,50000)
;

INSERT INTO WS_WebService_Para (AD_Client_ID,AD_Org_ID,ConstantValue,Created,CreatedBy,IsActive,ParameterName,ParameterType,Updated,UpdatedBy,WS_WebService_Para_ID,WS_WebServiceType_ID) VALUES (0,0,'1000001',TO_DATE('2010-04-19 19:47:43','YYYY-MM-DD HH24:MI:SS'),100,'Y','AD_Org_ID','C',TO_DATE('2010-04-19 19:47:43','YYYY-MM-DD HH24:MI:SS'),100,50001,50000)
;

INSERT INTO WS_WebService_Para (AD_Client_ID,AD_Org_ID,ConstantValue,Created,CreatedBy,IsActive,ParameterName,ParameterType,Updated,UpdatedBy,WS_WebService_Para_ID,WS_WebServiceType_ID) VALUES (0,0,'50004',TO_DATE('2010-04-19 19:47:51','YYYY-MM-DD HH24:MI:SS'),100,'Y','AD_Role_ID','C',TO_DATE('2010-04-19 19:47:51','YYYY-MM-DD HH24:MI:SS'),100,50002,50000)
;

INSERT INTO WS_WebService_Para (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,ParameterName,ParameterType,Updated,UpdatedBy,WS_WebService_Para_ID,WS_WebServiceType_ID) VALUES (0,0,TO_DATE('2010-04-19 19:47:59','YYYY-MM-DD HH24:MI:SS'),100,'Y','didNumber','F',TO_DATE('2010-04-19 19:47:59','YYYY-MM-DD HH24:MI:SS'),100,50003,50000)
;

INSERT INTO WS_WebService_Para (AD_Client_ID,AD_Org_ID,Created,CreatedBy,IsActive,ParameterName,ParameterType,Updated,UpdatedBy,WS_WebService_Para_ID,WS_WebServiceType_ID) VALUES (0,0,TO_DATE('2010-04-19 19:47:59','YYYY-MM-DD HH24:MI:SS'),100,'Y','businessPartnerId','F',TO_DATE('2010-04-19 19:47:59','YYYY-MM-DD HH24:MI:SS'),100,50004,50000)
;

INSERT INTO WS_WebServiceTypeAccess (AD_Client_ID,AD_Org_ID,AD_Role_ID,Created,CreatedBy,IsActive,IsReadWrite,Updated,UpdatedBy,WS_WebServiceType_ID) VALUES (0,0,50004,TO_DATE('2010-04-19 19:48:12','YYYY-MM-DD HH24:MI:SS'),100,'Y','Y',TO_DATE('2010-04-19 19:48:12','YYYY-MM-DD HH24:MI:SS'),100,50000)
;