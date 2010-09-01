DROP TABLE IF EXISTS `radius`.`radacctinvoice`;
CREATE TABLE  `radius`.`radacctinvoice` (
  `RadAcctId` bigint(21) NOT NULL,
  `invoiceId` int(10) NOT NULL default '0',
  `invoiceLineId` int(10) NOT NULL default '0',
  `isActive` bit(1) NOT NULL default b'1',
  PRIMARY KEY (`RadAcctId`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;