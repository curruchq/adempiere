DELETE FROM AD_PINSTANCE_LOG WHERE AD_PINSTANCE_ID NOT IN (SELECT AD_PINSTANCE_ID FROM AD_PINSTANCE);
ALTER TABLE AD_PINSTANCE_LOG MODIFY CONSTRAINT ADPINSTANCE_PILOG ENABLE;

DELETE FROM AD_PINSTANCE_PARA WHERE AD_PINSTANCE_ID NOT IN (SELECT AD_PINSTANCE_ID FROM AD_PINSTANCE);
ALTER TABLE AD_PINSTANCE_PARA MODIFY CONSTRAINT ADPINSTANCE_ADPINSTANCEPARA ENABLE;

DELETE FROM AD_PRINTFORMATITEM WHERE AD_PRINTFORMAT_ID NOT IN (SELECT AD_PRINTFORMAT_ID FROM AD_PRINTFORMAT);
ALTER TABLE AD_PRINTFORMATITEM MODIFY CONSTRAINT ADPRINTFORMAT_PRINTFORMATITEM ENABLE;

UPDATE C_BPARTNER SET M_PRICELIST_ID = NULL WHERE M_PRICELIST_ID NOT IN (SELECT M_PRICELIST_ID FROM M_PRICELIST);
ALTER TABLE C_BPARTNER MODIFY CONSTRAINT MPRICELIST_CBPARTNER ENABLE;


/* Need to check DELETE rule for these constraints */
DELETE FROM C_DOCTYPE WHERE DOCNOSEQUENCE_ID IN (SELECT AD_SEQUENCE_ID FROM AD_SEQUENCE WHERE AD_CLIENT_ID NOT IN (SELECT AD_CLIENT_ID FROM AD_CLIENT));
DELETE FROM AD_SEQUENCE WHERE AD_CLIENT_ID NOT IN (SELECT AD_CLIENT_ID FROM AD_CLIENT);