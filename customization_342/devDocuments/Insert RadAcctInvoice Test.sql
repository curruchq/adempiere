INSERT INTO radius.radacctinvoice (RadAcctId) SELECT RadAcctId FROM radius.radacct LIMIT 10;
UPDATE radius.radacctinvoice SET invoiceId=1000000;
INSERT INTO radius.radacctinvoice (RadAcctId) SELECT RadAcctId FROM radius.radacct ORDER BY RadAcctId DESC LIMIT 10;
UPDATE radius.radacctinvoice SET invoiceId=1000001 WHERE invoiceId != 1000000;