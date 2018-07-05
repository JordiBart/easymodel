-- phpMyAdmin SQL Dump
-- version 4.7.4
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1:3306
-- Generation Time: Jul 05, 2018 at 11:15 AM
-- Server version: 5.7.19
-- PHP Version: 5.6.31

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET AUTOCOMMIT = 0;
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `easymodel`
--

-- --------------------------------------------------------

--
-- Table structure for table `formula`
--

DROP TABLE IF EXISTS `formula`;
CREATE TABLE IF NOT EXISTS `formula` (
  `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `id_user` int(10) UNSIGNED DEFAULT NULL,
  `name` varchar(200) DEFAULT NULL,
  `formula` varchar(500) NOT NULL,
  `onesubstrateonly` tinyint(1) NOT NULL DEFAULT '0',
  `noproducts` tinyint(1) NOT NULL DEFAULT '0',
  `onemodifieronly` tinyint(1) NOT NULL DEFAULT '0',
  `formulatype` int(1) NOT NULL,
  `repositorytype` int(1) NOT NULL,
  `modified` date NOT NULL,
  PRIMARY KEY (`id`),
  KEY `id_user` (`id_user`)
) ENGINE=InnoDB AUTO_INCREMENT=451 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `formula`
--

INSERT INTO `formula` (`id`, `id_user`, `name`, `formula`, `onesubstrateonly`, `noproducts`, `onemodifieronly`, `formulatype`, `repositorytype`, `modified`) VALUES
(357, NULL, 'Power Laws', 'a*Product[X[[i]]^g[[i]],{i,1,Length[X]}]*Product[M[[i]]^g[[i+Length[X]]],{i,1,Length[M]}]', 0, 0, 0, 0, 0, '2018-07-05'),
(358, NULL, 'Saturating Cooperative', '(v*Product[X[[i]]^g[[i]],{i,1,Length[X]}]*Product[M[[i]]^g[[i+Length[X]]],{i,1,Length[M]}])/(Product[k[[i]]+X[[i]]^g[[i]],{i,1,Length[X]}]*Product[k[[i+Length[X]]]+M[[i]]^g[[i+Length[X]]],{i,1,Length[M]}])', 0, 0, 0, 0, 0, '2018-07-05'),
(359, NULL, 'Saturating', '(v*Product[X[[i]],{i,1,Length[X]}]*Product[M[[i]],{i,1,Length[M]}])/(Product[k[[i]]+X[[i]],{i,1,Length[X]}]*Product[k[[i+Length[X]]]+M[[i]],{i,1,Length[M]}])', 0, 0, 0, 0, 0, '2018-07-05'),
(360, NULL, 'Mass action', 'a*Product[X[[i]]^A[[i]],{i,1,Length[X]}]', 0, 0, 0, 0, 0, '2018-07-05'),
(361, NULL, 'Henri-Michaelis menten', '(v*XF)/(k+XF)', 1, 0, 0, 0, 0, '2018-07-05'),
(362, NULL, 'Hill Cooperativity', '(v*(XF^n))/(k^n+XF^n)', 1, 0, 0, 0, 0, '2018-07-05'),
(363, NULL, 'Catalytic activation', '(v*XF*MF)/((k+XF)*(k2+MF))', 1, 1, 1, 0, 0, '2018-07-05'),
(364, NULL, 'Competititve inhibition', '(v*XF*MF)/((k+XF)+(k+MF/k2))', 1, 1, 1, 0, 0, '2018-07-05'),
(404, 1, 'Test_Gai_K1', 'k1*X0^g10*(X3)^g13', 0, 0, 0, 1, 0, '2018-07-05'),
(405, 1, 'Test_Gai_K2', 'k2*(X1)^g21', 0, 0, 0, 1, 0, '2018-07-05'),
(406, 1, 'Test_Gai_K3', 'k3*(X2)^g32', 0, 0, 0, 1, 0, '2018-07-05'),
(407, 1, 'Test_Gai_K4', 'k4*(X3)^g43', 0, 0, 0, 1, 0, '2018-07-05'),
(408, 1, 'Test_Dyn_K1', 'k0', 0, 0, 0, 1, 0, '2018-07-05'),
(409, 1, 'Test_Dyn_K2', 'k4*X2', 0, 0, 0, 1, 0, '2018-07-05'),
(410, 1, 'Test_Dyn_K3', 'k1*X1^2', 0, 0, 0, 1, 0, '2018-07-05'),
(411, 1, 'Test_Dyn_K4', 'k2*X2', 0, 0, 0, 1, 0, '2018-07-05'),
(412, 1, 'Test_Dyn_K5', 'k3*X3', 0, 0, 0, 1, 0, '2018-07-05'),
(413, 1, 'Peroxida_K1', 'k1*NADH*O2', 0, 0, 0, 1, 0, '2018-07-05'),
(414, 1, 'Peroxida_K2', 'k2*H2O2*per3', 0, 0, 0, 1, 0, '2018-07-05'),
(415, 1, 'Peroxida_K3', 'k3*coI*ArH', 0, 0, 0, 1, 0, '2018-07-05'),
(416, 1, 'Peroxida_K4', 'k4*coII*ArH', 0, 0, 0, 1, 0, '2018-07-05'),
(417, 1, 'Peroxida_K5', 'k5*NADrad*O2', 0, 0, 0, 1, 0, '2018-07-05'),
(418, 1, 'Peroxida_K6', 'k6*super*per3', 0, 0, 0, 1, 0, '2018-07-05'),
(419, 1, 'Peroxida_K7', 'k7*super*super', 0, 0, 0, 1, 0, '2018-07-05'),
(420, 1, 'Peroxida_K8', 'k8*coIII*NADrad', 0, 0, 0, 1, 0, '2018-07-05'),
(421, 1, 'Peroxida_K9', 'k9*NADrad*NADrad', 0, 0, 0, 1, 0, '2018-07-05'),
(422, 1, 'Peroxida_K10', 'k10*per3*NADrad', 0, 0, 0, 1, 0, '2018-07-05'),
(423, 1, 'Peroxida_K11', 'k11*per2*O2', 0, 0, 0, 1, 0, '2018-07-05'),
(424, 1, 'Peroxida_K12', 'k12', 0, 0, 0, 1, 0, '2018-07-05'),
(425, 1, 'Peroxida_K13', 'k13f*O2g', 0, 0, 0, 1, 0, '2018-07-05'),
(426, 1, 'Peroxida_K14', 'k13b*O2', 0, 0, 0, 1, 0, '2018-07-05'),
(427, 1, 'Peroxida_K15', 'k14*Ar*NADH', 0, 0, 0, 1, 0, '2018-07-05'),
(428, 1, 'Peroxida_K16', 'k1', 0, 0, 0, 1, 0, '2018-07-05'),
(429, 1, 'Peroxida_K17', 'k2', 0, 0, 0, 1, 0, '2018-07-05'),
(430, 1, 'Peroxida_K18', 'k3', 0, 0, 0, 1, 0, '2018-07-05'),
(431, 1, 'Peroxida_K19', 'k4', 0, 0, 0, 1, 0, '2018-07-05'),
(432, 1, 'Peroxida_K20', 'k5', 0, 0, 0, 1, 0, '2018-07-05'),
(433, 1, 'Peroxida_K21', 'k6', 0, 0, 0, 1, 0, '2018-07-05'),
(434, 1, 'Peroxida_K22', 'k7', 0, 0, 0, 1, 0, '2018-07-05'),
(435, 1, 'Peroxida_K23', 'k8', 0, 0, 0, 1, 0, '2018-07-05'),
(436, 1, 'Peroxida_K24', 'k9', 0, 0, 0, 1, 0, '2018-07-05'),
(437, 1, 'Peroxida_K25', 'k10', 0, 0, 0, 1, 0, '2018-07-05'),
(438, 1, 'Peroxida_K26', 'k11', 0, 0, 0, 1, 0, '2018-07-05'),
(439, 1, 'Peroxida_K27', 'k12', 0, 0, 0, 1, 0, '2018-07-05'),
(440, 1, 'Peroxida_K28', 'k13f', 0, 0, 0, 1, 0, '2018-07-05'),
(441, 1, 'Peroxida_K29', 'k13b', 0, 0, 0, 1, 0, '2018-07-05'),
(442, 1, 'Peroxida_K30', 'k14', 0, 0, 0, 1, 0, '2018-07-05'),
(446, 1, 'Overall__K1', 'alpha1*X0^g10*X3^g13', 1, 0, 1, 1, 0, '2018-07-05'),
(449, 1, 'Alanine__K1', 'kcat*Enzyme*Substrate/(Km+Substrate)', 0, 0, 0, 1, 1, '2018-07-05');

-- --------------------------------------------------------

--
-- Table structure for table `formulagenparam`
--

DROP TABLE IF EXISTS `formulagenparam`;
CREATE TABLE IF NOT EXISTS `formulagenparam` (
  `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `id_formula` int(11) UNSIGNED NOT NULL,
  `genparam` varchar(100) NOT NULL,
  `formulavaluetype` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `id_formula` (`id_formula`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `formulagenparam`
--

INSERT INTO `formulagenparam` (`id`, `id_formula`, `genparam`, `formulavaluetype`) VALUES
(1, 357, 'a', 0),
(2, 358, 'v', 0),
(3, 359, 'v', 0),
(4, 360, 'a', 0),
(5, 361, 'v', 0),
(6, 361, 'k', 0),
(7, 362, 'v', 0),
(8, 362, 'k', 0),
(9, 362, 'n', 0),
(10, 363, 'v', 0),
(11, 363, 'k', 0),
(12, 363, 'k2', 0),
(13, 364, 'v', 0),
(14, 364, 'k', 0),
(15, 364, 'k2', 0);

-- --------------------------------------------------------

--
-- Table structure for table `formulamodifiersarray`
--

DROP TABLE IF EXISTS `formulamodifiersarray`;
CREATE TABLE IF NOT EXISTS `formulamodifiersarray` (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  `id_reaction` int(10) UNSIGNED NOT NULL,
  `constant` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `id_reaction` (`id_reaction`)
) ENGINE=InnoDB AUTO_INCREMENT=62 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `formulamodifiersarray`
--

INSERT INTO `formulamodifiersarray` (`id`, `id_reaction`, `constant`) VALUES
(4, 261, 'g'),
(5, 262, 'g'),
(6, 263, 'g'),
(16, 282, 'g'),
(17, 284, 'g'),
(18, 286, 'g');

-- --------------------------------------------------------

--
-- Table structure for table `formulamodifiersarrayvalue`
--

DROP TABLE IF EXISTS `formulamodifiersarrayvalue`;
CREATE TABLE IF NOT EXISTS `formulamodifiersarrayvalue` (
  `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `id_formulamodifiersarray` int(11) UNSIGNED NOT NULL,
  `modifier` varchar(100) NOT NULL,
  `value` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `id_formulamodifiersarray` (`id_formulamodifiersarray`)
) ENGINE=InnoDB AUTO_INCREMENT=60 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `formulamodifiersarrayvalue`
--

INSERT INTO `formulamodifiersarrayvalue` (`id`, `id_formulamodifiersarray`, `modifier`, `value`) VALUES
(13, 16, 'X3', '-1'),
(14, 16, 'X4', '-1'),
(15, 17, 'X4', '0.5'),
(16, 18, 'X3', '0.5');

-- --------------------------------------------------------

--
-- Table structure for table `formulasubstratesarray`
--

DROP TABLE IF EXISTS `formulasubstratesarray`;
CREATE TABLE IF NOT EXISTS `formulasubstratesarray` (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  `id_reaction` int(10) UNSIGNED NOT NULL,
  `constant` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `id_reaction` (`id_reaction`)
) ENGINE=InnoDB AUTO_INCREMENT=62 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `formulasubstratesarray`
--

INSERT INTO `formulasubstratesarray` (`id`, `id_reaction`, `constant`) VALUES
(4, 261, 'g'),
(5, 262, 'g'),
(6, 263, 'g'),
(16, 282, 'g'),
(17, 284, 'g'),
(18, 286, 'g');

-- --------------------------------------------------------

--
-- Table structure for table `formulasubstratesarrayvalue`
--

DROP TABLE IF EXISTS `formulasubstratesarrayvalue`;
CREATE TABLE IF NOT EXISTS `formulasubstratesarrayvalue` (
  `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `id_formulasubstratesarray` int(11) UNSIGNED NOT NULL,
  `species` varchar(100) NOT NULL,
  `value` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `id_formulasubstratesarray` (`id_formulasubstratesarray`)
) ENGINE=InnoDB AUTO_INCREMENT=62 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `formulasubstratesarrayvalue`
--

INSERT INTO `formulasubstratesarrayvalue` (`id`, `id_formulasubstratesarray`, `species`, `value`) VALUES
(4, 4, 'Prephenate', '1'),
(5, 5, 'KetoPhenylpyruvate', '1'),
(6, 6, 'Phenylalanine', '1'),
(16, 16, 'X0', '1'),
(17, 17, 'X2', '1'),
(18, 18, 'X2', '1');

-- --------------------------------------------------------

--
-- Table structure for table `formulavalue`
--

DROP TABLE IF EXISTS `formulavalue`;
CREATE TABLE IF NOT EXISTS `formulavalue` (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  `id_reaction` int(10) UNSIGNED NOT NULL,
  `constant` varchar(100) NOT NULL,
  `formulavaluetype` int(11) NOT NULL,
  `constantvalue` varchar(100) DEFAULT NULL,
  `substratevalue` varchar(100) DEFAULT NULL,
  `modifiervalue` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `id_reaction` (`id_reaction`)
) ENGINE=InnoDB AUTO_INCREMENT=5524 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `formulavalue`
--

INSERT INTO `formulavalue` (`id`, `id_reaction`, `constant`, `formulavaluetype`, `constantvalue`, `substratevalue`, `modifiervalue`) VALUES
(391, 201, 'a', 0, '1', NULL, NULL),
(392, 202, 'a', 0, '1', NULL, NULL),
(393, 203, 'a', 0, '1', NULL, NULL),
(394, 204, 'a', 0, '1', NULL, NULL),
(412, 212, 'NADH', 1, NULL, 'NADH', NULL),
(413, 212, 'O2', 1, NULL, 'O2', NULL),
(414, 212, 'k1', 0, '0.000003', NULL, NULL),
(415, 213, 'H2O2', 1, NULL, 'H2O2', NULL),
(416, 213, 'k2', 0, '18', NULL, NULL),
(417, 213, 'per3', 1, NULL, 'per3', NULL),
(418, 214, 'ArH', 1, NULL, 'ArH', NULL),
(419, 214, 'coI', 1, NULL, 'coI', NULL),
(420, 214, 'k3', 0, '0.15', NULL, NULL),
(421, 215, 'ArH', 1, NULL, 'ArH', NULL),
(422, 215, 'coII', 1, NULL, 'coII', NULL),
(423, 215, 'k4', 0, '0.0052', NULL, NULL),
(424, 216, 'NADrad', 1, NULL, 'NADrad', NULL),
(425, 216, 'O2', 1, NULL, 'O2', NULL),
(426, 216, 'k5', 0, '20', NULL, NULL),
(427, 217, 'k6', 0, '17', NULL, NULL),
(428, 217, 'per3', 1, NULL, 'per3', NULL),
(429, 217, 'super', 1, NULL, 'super', NULL),
(430, 218, 'k7', 0, '20', NULL, NULL),
(431, 218, 'super', 1, NULL, 'super', NULL),
(432, 219, 'NADrad', 1, NULL, 'NADrad', NULL),
(433, 219, 'coIII', 1, NULL, 'coIII', NULL),
(434, 219, 'k8', 0, '40', NULL, NULL),
(435, 220, 'NADrad', 1, NULL, 'NADrad', NULL),
(436, 220, 'k9', 0, '60', NULL, NULL),
(437, 221, 'NADrad', 1, NULL, 'NADrad', NULL),
(438, 221, 'k10', 0, '1.8', NULL, NULL),
(439, 221, 'per3', 1, NULL, 'per3', NULL),
(440, 222, 'O2', 1, NULL, 'O2', NULL),
(441, 222, 'k11', 0, '0.1', NULL, NULL),
(442, 222, 'per2', 1, NULL, 'per2', NULL),
(443, 223, 'k12', 0, '0.08', NULL, NULL),
(444, 224, 'O2g', 1, NULL, 'O2g', NULL),
(445, 224, 'k13f', 0, '0.006', NULL, NULL),
(446, 225, 'O2', 1, NULL, 'O2', NULL),
(447, 225, 'k13b', 0, '0.006', NULL, NULL),
(448, 226, 'Ar', 1, NULL, 'Ar', NULL),
(449, 226, 'NADH', 1, NULL, 'NADH', NULL),
(450, 226, 'k14', 0, '0.7', NULL, NULL),
(451, 227, 'k1', 0, '0', NULL, NULL),
(452, 228, 'k2', 0, '0', NULL, NULL),
(453, 229, 'k3', 0, '0', NULL, NULL),
(454, 230, 'k4', 0, '0', NULL, NULL),
(455, 231, 'k5', 0, '0', NULL, NULL),
(456, 232, 'k6', 0, '0', NULL, NULL),
(457, 233, 'k7', 0, '0', NULL, NULL),
(458, 234, 'k8', 0, '0', NULL, NULL),
(459, 235, 'k9', 0, '0', NULL, NULL),
(460, 236, 'k10', 0, '0', NULL, NULL),
(461, 237, 'k11', 0, '0', NULL, NULL),
(462, 238, 'k12', 0, '0', NULL, NULL),
(463, 239, 'k13f', 0, '0', NULL, NULL),
(464, 240, 'k13b', 0, '0', NULL, NULL),
(465, 241, 'k14', 0, '0', NULL, NULL),
(466, 242, 'X0', 1, NULL, 'X0', NULL),
(467, 242, 'X3', 2, NULL, NULL, 'X3'),
(468, 242, 'g10', 0, '1', NULL, NULL),
(469, 242, 'g13', 0, '-1.5', NULL, NULL),
(470, 242, 'k1', 0, '1.5', NULL, NULL),
(471, 243, 'X1', 1, NULL, 'X1', NULL),
(472, 243, 'g21', 0, '1', NULL, NULL),
(473, 243, 'k2', 0, '1', NULL, NULL),
(474, 244, 'X2', 1, NULL, 'X2', NULL),
(475, 244, 'g32', 0, '1', NULL, NULL),
(476, 244, 'k3', 0, '1', NULL, NULL),
(477, 245, 'X3', 1, NULL, 'X3', NULL),
(478, 245, 'g43', 0, '1', NULL, NULL),
(479, 245, 'k4', 0, '1', NULL, NULL),
(489, 251, 'k0', 0, '1', NULL, NULL),
(490, 252, 'X2', 1, NULL, 'X2', NULL),
(491, 252, 'k4', 0, '1', NULL, NULL),
(492, 253, 'X1', 1, NULL, 'X1', NULL),
(493, 253, 'k1', 0, '1', NULL, NULL),
(494, 254, 'X2', 1, NULL, 'X2', NULL),
(495, 254, 'k2', 0, '1', NULL, NULL),
(496, 255, 'X3', 1, NULL, 'X3', NULL),
(497, 255, 'k3', 0, '1', NULL, NULL),
(506, 260, 'X0', 1, NULL, 'Chorismate', NULL),
(507, 260, 'X3', 2, NULL, NULL, 'Phenylalanine'),
(508, 260, 'alpha1', 0, '1', NULL, NULL),
(509, 260, 'g10', 0, '1', NULL, NULL),
(510, 260, 'g13', 0, '-27', NULL, NULL),
(511, 261, 'a', 0, '1', NULL, NULL),
(512, 262, 'a', 0, '1', NULL, NULL),
(513, 263, 'a', 0, '1', NULL, NULL),
(538, 282, 'a', 0, '1', NULL, NULL),
(539, 283, 'a', 0, '1', NULL, NULL),
(540, 284, 'a', 0, '0.6', NULL, NULL),
(541, 285, 'k', 0, '2', NULL, NULL),
(542, 285, 'v', 0, '0.6', NULL, NULL),
(543, 286, 'a', 0, '0.4', NULL, NULL),
(544, 287, 'k', 0, '2', NULL, NULL),
(545, 287, 'v', 0, '1', NULL, NULL),
(5452, 1583, 'Enzyme', 2, NULL, NULL, 'E1'),
(5453, 1583, 'Km', 0, '12', NULL, NULL),
(5454, 1583, 'Substrate', 1, NULL, 'Glutamate', NULL),
(5455, 1583, 'kcat', 0, '0.071', NULL, NULL),
(5456, 1584, 'Enzyme', 2, NULL, NULL, 'E2'),
(5457, 1584, 'Km', 0, '0.00045', NULL, NULL),
(5458, 1584, 'Substrate', 1, NULL, 'LGlutamylP', NULL),
(5459, 1584, 'kcat', 0, '1.32335', NULL, NULL),
(5460, 1585, 'Enzyme', 2, NULL, NULL, 'E3'),
(5461, 1585, 'Km', 0, '15.7', NULL, NULL),
(5462, 1585, 'Substrate', 1, NULL, 'Arginine', NULL),
(5463, 1585, 'kcat', 0, '10', NULL, NULL),
(5464, 1586, 'Enzyme', 2, NULL, NULL, 'E4'),
(5465, 1586, 'Km', 0, '3.95', NULL, NULL),
(5466, 1586, 'Substrate', 1, NULL, 'Ornithine', NULL),
(5467, 1586, 'kcat', 0, '2', NULL, NULL),
(5468, 1587, 'Enzyme', 2, NULL, NULL, 'E4'),
(5469, 1587, 'Km', 0, '25', NULL, NULL),
(5470, 1587, 'Substrate', 1, NULL, 'LGlutamate5semialdehyde', NULL),
(5471, 1587, 'kcat', 0, '2.5', NULL, NULL),
(5472, 1588, 'Enzyme', 2, NULL, NULL, 'E5'),
(5473, 1588, 'Km', 0, '0.21', NULL, NULL),
(5474, 1588, 'Substrate', 1, NULL, 'Ornithine', NULL),
(5475, 1588, 'kcat', 0, '315', NULL, NULL),
(5476, 1589, 'Enzyme', 2, NULL, NULL, 'E6'),
(5477, 1589, 'Km', 0, '0.02', NULL, NULL),
(5478, 1589, 'Substrate', 1, NULL, 'Putrescine', NULL),
(5479, 1589, 'kcat', 0, '5.28', NULL, NULL),
(5480, 1590, 'Enzyme', 2, NULL, NULL, 'E7'),
(5481, 1590, 'Km', 0, '0.274', NULL, NULL),
(5482, 1590, 'Substrate', 1, NULL, 'spermidine', NULL),
(5483, 1590, 'kcat', 0, '203.856', NULL, NULL),
(5484, 1591, 'Enzyme', 2, NULL, NULL, 'E6'),
(5485, 1591, 'Km', 0, '0.02', NULL, NULL),
(5486, 1591, 'Substrate', 1, NULL, 'spermidine', NULL),
(5487, 1591, 'kcat', 0, '5.28', NULL, NULL),
(5488, 1592, 'Enzyme', 2, NULL, NULL, 'E7'),
(5489, 1592, 'Km', 0, '0.188', NULL, NULL),
(5490, 1592, 'Substrate', 1, NULL, 'spermine', NULL),
(5491, 1592, 'kcat', 0, '3722.4', NULL, NULL),
(5492, 1593, 'Enzyme', 2, NULL, NULL, 'E7'),
(5493, 1593, 'Km', 0, '0.274', NULL, NULL),
(5494, 1593, 'Substrate', 1, NULL, 'spermidine', NULL),
(5495, 1593, 'kcat', 0, '203.68', NULL, NULL),
(5496, 1594, 'Enzyme', 2, NULL, NULL, 'E10'),
(5497, 1594, 'Km', 0, '0.013', NULL, NULL),
(5498, 1594, 'Substrate', 1, NULL, 'baminopropionaldehyde', NULL),
(5499, 1594, 'kcat', 0, '34.64', NULL, NULL),
(5500, 1595, 'Enzyme', 2, NULL, NULL, 'E11'),
(5501, 1595, 'Km', 0, '0.46', NULL, NULL),
(5502, 1595, 'Substrate', 1, NULL, 'baminopropionaldehyde', NULL),
(5503, 1595, 'kcat', 0, '828', NULL, NULL),
(5504, 1596, 'Enzyme', 2, NULL, NULL, 'E12'),
(5505, 1596, 'Km', 0, '2.32', NULL, NULL),
(5506, 1596, 'Substrate', 1, NULL, 'Aspartate', NULL),
(5507, 1596, 'kcat', 0, '310', NULL, NULL),
(5508, 1597, 'Enzyme', 2, NULL, NULL, 'E13'),
(5509, 1597, 'Km', 0, '0.25', NULL, NULL),
(5510, 1597, 'Substrate', 1, NULL, 'balanine', NULL),
(5511, 1597, 'kcat', 0, '9.6', NULL, NULL),
(5512, 1598, 'Enzyme', 2, NULL, NULL, 'E14'),
(5513, 1598, 'Km', 0, '2.4', NULL, NULL),
(5514, 1598, 'Substrate', 1, NULL, 'balanine', NULL),
(5515, 1598, 'kcat', 0, '921.6', NULL, NULL),
(5516, 1599, 'Enzyme', 2, NULL, NULL, 'E15'),
(5517, 1599, 'Km', 0, '0.2', NULL, NULL),
(5518, 1599, 'Substrate', 1, NULL, 'spermidine', NULL),
(5519, 1599, 'kcat', 0, '1920', NULL, NULL),
(5520, 1600, 'Enzyme', 2, NULL, NULL, 'E7'),
(5521, 1600, 'Km', 0, '0.188', NULL, NULL),
(5522, 1600, 'Substrate', 1, NULL, 'spermine', NULL),
(5523, 1600, 'kcat', 0, '3722.4', NULL, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `model`
--

DROP TABLE IF EXISTS `model`;
CREATE TABLE IF NOT EXISTS `model` (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  `id_user` int(10) UNSIGNED DEFAULT NULL,
  `name` varchar(100) NOT NULL,
  `description` varchar(300) DEFAULT NULL,
  `repositorytype` int(1) NOT NULL,
  `modified` date NOT NULL,
  PRIMARY KEY (`id`),
  KEY `id_user` (`id_user`)
) ENGINE=InnoDB AUTO_INCREMENT=159 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `model`
--

INSERT INTO `model` (`id`, `id_user`, `name`, `description`, `repositorytype`, `modified`) VALUES
(32, 1, 'Brusselator', 'Based on the COPASI example', 0, '2018-07-03'),
(37, 1, 'Peroxidase', 'Based on the COPASI example', 0, '2018-07-03'),
(38, 1, 'Test Gains+Sensitivities', 'Test Gains + Sensitivities + Stability', 0, '2018-07-03'),
(40, 1, 'Test Dynamic+Steady State', 'Test dynamic and steady state simulations', 0, '2018-07-03'),
(42, 1, 'Overall Feedback', 'Unbranched biosynthetic pathway', 0, '2018-07-03'),
(46, 1, 'Branched pathway', '', 0, '2018-07-03'),
(158, 1, 'Alanine - Proline - Arginine', '', 1, '2018-07-03');

-- --------------------------------------------------------

--
-- Table structure for table `reaction`
--

DROP TABLE IF EXISTS `reaction`;
CREATE TABLE IF NOT EXISTS `reaction` (
  `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `id_model` int(10) UNSIGNED NOT NULL,
  `id_formula` int(11) UNSIGNED DEFAULT NULL,
  `reaction` varchar(300) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `id_model` (`id_model`),
  KEY `reaction_ibfk_2` (`id_formula`)
) ENGINE=InnoDB AUTO_INCREMENT=1601 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `reaction`
--

INSERT INTO `reaction` (`id`, `id_model`, `id_formula`, `reaction`) VALUES
(201, 32, 360, 'AA -> XX'),
(202, 32, 360, '2 * XX + Y -> 3 * XX'),
(203, 32, 360, 'XX + B -> Y + DD'),
(204, 32, 360, 'XX -> F'),
(212, 37, 413, 'NADH+O2->H2O2+NAD'),
(213, 37, 414, 'per3+H2O2->coI'),
(214, 37, 415, 'ArH+coI->Ar+coII'),
(215, 37, 416, 'coII+ArH->per3+Ar'),
(216, 37, 417, 'NADrad+O2->NAD+super'),
(217, 37, 418, 'per3+super->coIII'),
(218, 37, 419, '2*super->H2O2+O2'),
(219, 37, 420, 'NADrad+coIII->NAD+coI'),
(220, 37, 421, '2*NADrad->NAD2'),
(221, 37, 422, 'per3+NADrad->per2+NAD'),
(222, 37, 423, 'per2+O2->coIII'),
(223, 37, 424, 'NADHres->NADH'),
(224, 37, 425, 'O2g->O2'),
(225, 37, 426, 'O2->O2g'),
(226, 37, 427, 'NADH+Ar->NADrad+ArH'),
(227, 37, 428, 'H2O2+NAD->NADH+O2'),
(228, 37, 429, 'coI->per3+H2O2'),
(229, 37, 430, 'Ar+coII->ArH+coI'),
(230, 37, 431, 'per3+Ar->coII+ArH'),
(231, 37, 432, 'NAD+super->NADrad+O2'),
(232, 37, 433, 'coIII->per3+super'),
(233, 37, 434, 'H2O2+O2->2*super'),
(234, 37, 435, 'NAD+coI->NADrad+coIII'),
(235, 37, 436, 'NAD2->2*NADrad'),
(236, 37, 437, 'per2+NAD->per3+NADrad'),
(237, 37, 438, 'coIII->per2+O2'),
(238, 37, 439, 'NADH->NADHres'),
(239, 37, 440, 'O2->O2g'),
(240, 37, 441, 'O2g->O2'),
(241, 37, 442, 'NADrad+ArH->NADH+Ar'),
(242, 38, 404, 'X0 -> X1;-X3'),
(243, 38, 405, 'X1 -> X2'),
(244, 38, 406, 'X2 -> X3'),
(245, 38, 407, 'X3 ->'),
(251, 40, 408, '->X1'),
(252, 40, 409, 'X2->'),
(253, 40, 410, '2 X1 -> X2'),
(254, 40, 411, 'X2 -> X3'),
(255, 40, 412, 'X3 -> 2 X1'),
(260, 42, 446, 'Chorismate -> Prephenate;Phenylalanine'),
(261, 42, 357, 'Prephenate -> KetoPhenylpyruvate'),
(262, 42, 357, 'KetoPhenylpyruvate -> Phenylalanine'),
(263, 42, 357, 'Phenylalanine ->'),
(282, 46, 357, 'X0 -> X1;X3;X4'),
(283, 46, 360, 'X1 -> X2'),
(284, 46, 357, 'X2 -> X3;X4'),
(285, 46, 361, 'X3 ->'),
(286, 46, 357, 'X2 -> X4;X3'),
(287, 46, 361, 'X4 ->'),
(1583, 158, 449, 'Glutamate -> LGlutamylP; E1'),
(1584, 158, 449, 'LGlutamylP -> LGlutamate5semialdehyde; E2'),
(1585, 158, 449, 'Arginine -> Ornithine; E3'),
(1586, 158, 449, 'Ornithine -> LGlutamate5semialdehyde; E4'),
(1587, 158, 449, 'LGlutamate5semialdehyde -> Ornithine; E4'),
(1588, 158, 449, 'Ornithine -> Putrescine; E5'),
(1589, 158, 449, 'Putrescine   ->  spermidine; E6'),
(1590, 158, 449, 'spermidine -> Putrescine; E7'),
(1591, 158, 449, 'spermidine   -> spermine; E6'),
(1592, 158, 449, 'spermine -> spermidine; E7'),
(1593, 158, 449, 'spermidine -> baminopropionaldehyde; E7'),
(1594, 158, 449, 'baminopropionaldehyde -> balanine; E10'),
(1595, 158, 449, 'baminopropionaldehyde -> balanine; E11'),
(1596, 158, 449, 'Aspartate -> balanine; E12'),
(1597, 158, 449, 'balanine -> ; E13'),
(1598, 158, 449, 'balanine -> ; E14'),
(1599, 158, 449, 'spermidine  -> spermine; E15'),
(1600, 158, 449, 'spermine -> baminopropionaldehyde; E7');

-- --------------------------------------------------------

--
-- Table structure for table `species`
--

DROP TABLE IF EXISTS `species`;
CREATE TABLE IF NOT EXISTS `species` (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  `id_model` int(10) UNSIGNED NOT NULL,
  `species` varchar(100) NOT NULL,
  `concentration` varchar(100) NOT NULL,
  `vartype` int(1) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `id_model` (`id_model`)
) ENGINE=InnoDB AUTO_INCREMENT=1977 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `species`
--

INSERT INTO `species` (`id`, `id_model`, `species`, `concentration`, `vartype`) VALUES
(166, 32, 'AA', '0.4999998755', 1),
(167, 32, 'B', '2.999995932', 1),
(168, 32, 'DD', '0', 1),
(169, 32, 'F', '0', 1),
(170, 32, 'XX', '2.999995932', 0),
(171, 32, 'Y', '2.999995932', 0),
(182, 37, 'Ar', '0', 0),
(183, 37, 'ArH', '500', 0),
(184, 37, 'H2O2', '0', 0),
(185, 37, 'NAD', '0', 1),
(186, 37, 'NAD2', '0', 1),
(187, 37, 'NADH', '0', 0),
(188, 37, 'NADHres', '0', 1),
(189, 37, 'NADrad', '0', 0),
(190, 37, 'O2', '0', 0),
(191, 37, 'O2g', '12', 1),
(192, 37, 'coI', '0', 0),
(193, 37, 'coII', '0', 0),
(194, 37, 'coIII', '0', 0),
(195, 37, 'per2', '0', 0),
(196, 37, 'per3', '1.4', 0),
(197, 37, 'super', '0', 0),
(198, 38, 'X0', '1', 1),
(199, 38, 'X1', '0.5', 0),
(200, 38, 'X2', '0.5', 0),
(201, 38, 'X3', '0.5', 0),
(205, 40, 'X1', '0.2', 0),
(206, 40, 'X2', '0.4', 0),
(207, 40, 'X3', '0.4', 0),
(212, 42, 'Chorismate', '1', 1),
(213, 42, 'KetoPhenylpyruvate', '0.99', 0),
(214, 42, 'Phenylalanine', '0.99', 0),
(215, 42, 'Prephenate', '0.99', 0),
(231, 46, 'X0', '1', 1),
(232, 46, 'X1', '0.5', 0),
(233, 46, 'X2', '0.5', 0),
(234, 46, 'X3', '0.5', 0),
(235, 46, 'X4', '0.5', 0),
(1953, 158, 'Arginine', '18000', 1),
(1954, 158, 'Aspartate', '8000', 1),
(1955, 158, 'E1', '0.000103649', 1),
(1956, 158, 'E10', '0.000460280', 1),
(1957, 158, 'E11', '0.000184686', 1),
(1958, 158, 'E12', '0.000080496', 1),
(1959, 158, 'E13', '0.000098391', 1),
(1960, 158, 'E14', '0.000086710', 1),
(1961, 158, 'E15', '0.000086710', 1),
(1962, 158, 'E2', '0.000381883', 1),
(1963, 158, 'E3', '0.000216215', 1),
(1964, 158, 'E4', '0.000500975', 1),
(1965, 158, 'E5', '0.000014299', 1),
(1966, 158, 'E6', '0.001657184', 1),
(1967, 158, 'E7', '0.000006859', 1),
(1968, 158, 'Glutamate', '25000', 1),
(1969, 158, 'LGlutamate5semialdehyde', '1.7693', 0),
(1970, 158, 'LGlutamylP', '6.64646E-6', 0),
(1971, 158, 'Ornithine', '0.194816', 0),
(1972, 158, 'Putrescine', '0.00665851', 0),
(1973, 158, 'balanine', '1.17967', 0),
(1974, 158, 'baminopropionaldehyde', '0.00658895', 0),
(1975, 158, 'spermidine', '0.00361888', 0),
(1976, 158, 'spermine', '0.0172848', 0);

-- --------------------------------------------------------

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
CREATE TABLE IF NOT EXISTS `user` (
  `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `password` varchar(200) NOT NULL,
  `usertype` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `user`
--

INSERT INTO `user` (`id`, `name`, `password`, `usertype`) VALUES
(1, 'test', '$2a$06$MSNKfSIyimNX5jXUstHLIeY.3qoMskYq/.K18S390Trd3bvrmcAou', 0),
(2, 'admin', '$2a$06$g8Cu5zzcRE479WlkvIhzJuua42uNufbGkeNhlMXQ/9KB2s8AMbz0a', 1),
(5, 'guest', '', 2);

--
-- Constraints for dumped tables
--

--
-- Constraints for table `formula`
--
ALTER TABLE `formula`
  ADD CONSTRAINT `formula_ibfk_1` FOREIGN KEY (`id_user`) REFERENCES `user` (`id`) ON DELETE SET NULL ON UPDATE SET NULL;

--
-- Constraints for table `formulagenparam`
--
ALTER TABLE `formulagenparam`
  ADD CONSTRAINT `formulagenparam_ibfk_1` FOREIGN KEY (`id_formula`) REFERENCES `formula` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `formulamodifiersarray`
--
ALTER TABLE `formulamodifiersarray`
  ADD CONSTRAINT `formulamodifiersarray_ibfk_1` FOREIGN KEY (`id_reaction`) REFERENCES `reaction` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `formulamodifiersarrayvalue`
--
ALTER TABLE `formulamodifiersarrayvalue`
  ADD CONSTRAINT `formulamodifiersarrayvalue_ibfk_1` FOREIGN KEY (`id_formulamodifiersarray`) REFERENCES `formulamodifiersarray` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `formulasubstratesarray`
--
ALTER TABLE `formulasubstratesarray`
  ADD CONSTRAINT `formulasubstratesarray_ibfk_1` FOREIGN KEY (`id_reaction`) REFERENCES `reaction` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `formulasubstratesarrayvalue`
--
ALTER TABLE `formulasubstratesarrayvalue`
  ADD CONSTRAINT `formulasubstratesarrayvalue_ibfk_1` FOREIGN KEY (`id_formulasubstratesarray`) REFERENCES `formulasubstratesarray` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `formulavalue`
--
ALTER TABLE `formulavalue`
  ADD CONSTRAINT `formulavalue_ibfk_1` FOREIGN KEY (`id_reaction`) REFERENCES `reaction` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `model`
--
ALTER TABLE `model`
  ADD CONSTRAINT `model_ibfk_1` FOREIGN KEY (`id_user`) REFERENCES `user` (`id`) ON DELETE SET NULL ON UPDATE SET NULL;

--
-- Constraints for table `reaction`
--
ALTER TABLE `reaction`
  ADD CONSTRAINT `reaction_ibfk_1` FOREIGN KEY (`id_model`) REFERENCES `model` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `reaction_ibfk_2` FOREIGN KEY (`id_formula`) REFERENCES `formula` (`id`) ON DELETE SET NULL ON UPDATE SET NULL;

--
-- Constraints for table `species`
--
ALTER TABLE `species`
  ADD CONSTRAINT `species_ibfk_1` FOREIGN KEY (`id_model`) REFERENCES `model` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
