ALTER TABLE ADEMPIERE.MOD_BILLING_RECORD DROP PRIMARY KEY CASCADE;
DROP TABLE ADEMPIERE.MOD_BILLING_RECORD CASCADE CONSTRAINTS;

CREATE TABLE ADEMPIERE.MOD_BILLING_RECORD
(
  MOD_BILLING_RECORD_ID    NUMBER(10)           NOT NULL,
  RADACCTID                NUMBER(10)           NOT NULL,
  USERNAME                 VARCHAR2(200 BYTE),
  ACCTSTARTTIME            DATE,
  SIPAPPLICATIONTYPE       VARCHAR2(200 BYTE),
  PRICE                    NUMBER,
  RATE                     VARCHAR2(1000 BYTE),
  NORMALIZED               VARCHAR2(200 BYTE),
  AD_CLIENT_ID             NUMBER(10)           NOT NULL,
  AD_ORG_ID                NUMBER(10)           NOT NULL,
  ISACTIVE                 CHAR(1 BYTE)         DEFAULT 'Y'                   NOT NULL,
  CREATED                  DATE                 DEFAULT SYSDATE               NOT NULL,
  CREATEDBY                NUMBER(10)           NOT NULL,
  UPDATED                  DATE                 DEFAULT SYSDATE               NOT NULL,
  UPDATEDBY                NUMBER(10)           NOT NULL,
  C_INVOICE_ID             NUMBER(10)           DEFAULT 0                     NOT NULL,
  C_INVOICELINE_ID         NUMBER(10)           DEFAULT 0                     NOT NULL,
  PROCESSED                CHAR(1 BYTE)         DEFAULT 'N'                   NOT NULL,
  SYNCRONISED              CHAR(1 BYTE)         DEFAULT 'N'                   NOT NULL
);

ALTER TABLE ADEMPIERE.MOD_BILLING_RECORD ADD PRIMARY KEY (MOD_BILLING_RECORD_ID);
