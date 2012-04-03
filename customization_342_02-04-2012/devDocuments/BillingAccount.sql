DROP TABLE IF EXISTS `billing`.`billingaccount`;
CREATE TABLE  `billing`.`billingaccount` (
  `billingAccountId` int(10) NOT NULL,
  `login` varchar(255) NOT NULL,
  `username` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  PRIMARY KEY  (`billingAccountId`)
) ENGINE=InnoDB;

INSERT INTO `billing`.`billingaccount` (billingAccountId, login, username, password) VALUES (1, "028891398", "10104115", "l70kw62z");
INSERT INTO `billing`.`billingaccount` (billingAccountId, login, username, password) VALUES (2, "028892520", "10159615", "g57tr26k");
INSERT INTO `billing`.`billingaccount` (billingAccountId, login, username, password) VALUES (3, "02825503272", "10667064", "s60nq27u");

ALTER TABLE `billing`.`billingrecord` ADD billingAccountId int(10) NOT NULL DEFAULT 1;
ALTER TABLE `billing`.`billingrecord` ADD FOREIGN KEY(billingAccountId) REFERENCES billingaccount(billingAccountId);
ALTER TABLE `billing`.`billingrecord` ALTER billingAccountId DROP DEFAULT;

INSERT INTO billing.billingrecord (twoTalkId, billingGroup, originNumber, destinationNumber, description, `status`, `terminated`, `date`, `time`, `dateTime`, callLength, callCost, smartCode, smartCodeDescription, `type`, subType, mp3, billingAccountId) VALUES (137897029, 'DELETE ME', '', '', '', '', '', '2010-12-16 00:00:00', '1900-01-01 20:20:27', '2010-12-16 20:20:27', '', '', '', '', '', '', b'0', 2);

INSERT INTO billing.billingrecord (twoTalkId, billingGroup, originNumber, destinationNumber, description, `status`, `terminated`, `date`, `time`, `dateTime`, callLength, callCost, smartCode, smartCodeDescription, `type`, subType, mp3, billingAccountId) VALUES (142365952, 'DELETE ME', '', '', '', '', '', '2010-12-16 00:00:00', '1900-01-01 20:20:27', '2010-12-16 20:20:27', '', '', '', '', '', '', b'0', 3);

GRANT ALL ON `billing`.* TO 'erp_local'@'%';
FLUSH PRIVILEGES;