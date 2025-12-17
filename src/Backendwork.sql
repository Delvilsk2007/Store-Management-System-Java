/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/SQLTemplate.sql to edit this template
 */
/**
 * Author:  Sk
 * Created: Dec 17, 2025
 */
--
-- Database: `store_management_system`
--

CREATE DATABASE IF NOT EXISTS `store_management_system`
DEFAULT CHARACTER SET latin1 COLLATE latin1_swedish_ci;
USE `store_management_system`;

-- --------------------------------------------------------
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
CREATE TABLE IF NOT EXISTS `user` (
  `Id` varchar(20) NOT NULL,
  `pin` int(11) DEFAULT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

INSERT INTO `user` VALUES
('sk', 123),
('user', 123);

-- --------------------------------------------------------
-- Table structure for table `product`
--

DROP TABLE IF EXISTS `product`;
CREATE TABLE IF NOT EXISTS `product` (
  `S_no` int NOT NULL AUTO_INCREMENT,
  `P_Name` varchar(20) NOT NULL,
  `Price` float(9,2) DEFAULT NULL,
  `Category` varchar(25) DEFAULT NULL,
  `Quantity` int DEFAULT NULL,
  PRIMARY KEY (`S_no`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

INSERT INTO `product` (P_Name, Price, Category, Quantity) VALUES
('Soap', 45.00, 'Daily Use', 100),
('Shampoo', 120.00, 'Daily Use', 60),
('Rice', 55.00, 'Grocery', 200),
('Sugar', 42.00, 'Grocery', 150),
('Oil', 160.00, 'Grocery', 80);

-- --------------------------------------------------------
-- Table structure for table `customer`
--

DROP TABLE IF EXISTS `customer`;
CREATE TABLE IF NOT EXISTS `customer` (
  `Id` int NOT NULL AUTO_INCREMENT,
  `Name` varchar(30) NOT NULL,
  `Email` varchar(255) DEFAULT NULL,
  `Mobile_no` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`Id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

INSERT INTO `customer` (Name, Email, Mobile_no) VALUES
('Rahul Kumar', 'rahul@gmail.com', '9876543210'),
('Neha Sharma', 'neha@gmail.com', '9123456789');

-- --------------------------------------------------------
-- Table structure for table `cart`
--

DROP TABLE IF EXISTS `cart`;
CREATE TABLE IF NOT EXISTS `cart` (
  `Cart_id` int NOT NULL AUTO_INCREMENT,
  `Customer` varchar(100) NOT NULL,
  `Product_name` varchar(100) DEFAULT NULL,
  `Quantity` int DEFAULT NULL,
  `Category` varchar(100) DEFAULT NULL,
  `Total_price` double DEFAULT NULL,
  PRIMARY KEY (`Cart_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

INSERT INTO `cart` (Customer, Product_name, Quantity, Category, Total_price) VALUES
('Rahul Kumar', 'Soap', 2, 'Daily Use', 90.00),
('Neha Sharma', 'Rice', 5, 'Grocery', 275.00);

-- --------------------------------------------------------
-- Table structure for table `emp`
--

DROP TABLE IF EXISTS `emp`;
CREATE TABLE IF NOT EXISTS `emp` (
  `S_NO` int NOT NULL AUTO_INCREMENT,
  `Emp_Name` varchar(30) NOT NULL,
  `Gender` varchar(10) DEFAULT NULL,
  `Status` varchar(20) NOT NULL,
  `Salary` float(10,2) DEFAULT NULL,
  PRIMARY KEY (`S_NO`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

INSERT INTO `emp` (Emp_Name, Gender, Status, Salary) VALUES
('Amit', 'Male', 'Active', 12000.00),
('Pooja', 'Female', 'Active', 11000.00);

-- --------------------------------------------------------
-- Table structure for table `emp_personal`
--

DROP TABLE IF EXISTS `emp_personal`;
CREATE TABLE IF NOT EXISTS `emp_personal` (
  `S_NO` int NOT NULL,
  `Address` varchar(100) DEFAULT NULL,
  `Mobile_No` varchar(100) DEFAULT NULL,
  `Email` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`S_NO`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

INSERT INTO `emp_personal` VALUES
(1, 'Faridabad', '9870001111', 'amit@gmail.com'),
(2, 'Delhi', '9870002222', 'pooja@gmail.com');

COMMIT;

