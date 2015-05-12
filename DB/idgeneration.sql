
--
-- Database: `idgeneration`
--
CREATE SCHEMA `idgeneration` ;
-- --------------------------------------------------------

--
-- Table structure for table `id_generated`
--
USE idgeneration;

CREATE TABLE IF NOT EXISTS `id_generated` (
`Id` bigint(20) unsigned NOT NULL,
  `service` varchar(250) DEFAULT NULL,
  `hashValue` binary(250) DEFAULT NULL,
  `time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=10001 ;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `id_generated`
--
ALTER TABLE `id_generated`
 ADD PRIMARY KEY (`Id`), ADD UNIQUE KEY `hashValue` (`hashValue`);

--
-- AUTO_INCREMENT for dumped tables
--
-- AUTO_INCREMENT for table `id_generated`
--
ALTER TABLE `id_generated`
MODIFY `Id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=10001;
