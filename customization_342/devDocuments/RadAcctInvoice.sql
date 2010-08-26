DROP TABLE IF EXISTS `radius`.`radacctinvoice`;
CREATE TABLE  `radius`.`radacctinvoice` (
  `radAcctId` bigint(21) NOT NULL,
  `invoiceId` int(10) NOT NULL,
  `invoiceLineId` int(10) NOT NULL,
  PRIMARY KEY (`radAcctId`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;