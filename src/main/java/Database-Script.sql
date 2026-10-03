CREATE DATABASE IF NOT EXISTS travel_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;
USE travel_db;

-- ---------- CUSTOMER ----------
CREATE TABLE customer (
  customerID INT NOT NULL AUTO_INCREMENT,
  firstName  VARCHAR(45) NOT NULL,
  lastName   VARCHAR(45) NOT NULL,
  middleName VARCHAR(45) NULL,
  email      VARCHAR(45) NOT NULL,
  sex        ENUM('Male','Female') NULL,
  age        INT NULL,
  PRIMARY KEY (customerID),
  UNIQUE KEY uq_customer_email (email)
) ENGINE=InnoDB;

-- ---------- CUSTOMER ACCOUNT ----------
CREATE TABLE customerAccount (
  customerAccountID INT NOT NULL AUTO_INCREMENT,
  customerID INT NOT NULL,
  username   VARCHAR(45) NOT NULL,
  password   VARCHAR(255) NOT NULL,
  status     ENUM('Active','Inactive','Suspended') NOT NULL DEFAULT 'Active',
  createdAt  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (customerAccountID),
  UNIQUE KEY uq_customerAccount_username (username),
  KEY idx_customerAccount_customer (customerID),
  CONSTRAINT fk_customerAccount_customer
    FOREIGN KEY (customerID) REFERENCES customer (customerID)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ---------- DESTINATION ----------
CREATE TABLE destination (
  destinationID INT NOT NULL AUTO_INCREMENT,
  name        VARCHAR(45) NOT NULL,
  country     VARCHAR(45) NOT NULL,
  description LONGTEXT NULL,
  status      VARCHAR(45) NOT NULL DEFAULT 'Active',
  PRIMARY KEY (destinationID)
) ENGINE=InnoDB;

-- ---------- PACKAGE ----------
CREATE TABLE package (
  packageID INT NOT NULL AUTO_INCREMENT,
  destinationID INT NOT NULL,
  name        VARCHAR(45) NOT NULL,
  description LONGTEXT NULL,
  pricePerTraveller DECIMAL(10,2) NOT NULL,
  status      ENUM('Active','Inactive') NOT NULL DEFAULT 'Active',
  minTraveller INT NOT NULL DEFAULT 1,
  maxTraveller INT NOT NULL,
  PRIMARY KEY (packageID),
  KEY idx_package_destination (destinationID),
  CONSTRAINT fk_package_destination
    FOREIGN KEY (destinationID) REFERENCES destination (destinationID)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ---------- BOOKING ----------
CREATE TABLE booking (
  bookingID INT NOT NULL AUTO_INCREMENT,
  customerAccountID INT NOT NULL,
  packageID INT NOT NULL,
  status ENUM('Pending','Confirmed','Cancelled','Completed') NOT NULL DEFAULT 'Pending',
  bookingDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  travelDate  DATETIME NOT NULL,
  PRIMARY KEY (bookingID),
  KEY idx_booking_customerAccount (customerAccountID),
  KEY idx_booking_package (packageID),
  CONSTRAINT fk_booking_customerAccount
    FOREIGN KEY (customerAccountID) REFERENCES customerAccount (customerAccountID)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_booking_package
    FOREIGN KEY (packageID) REFERENCES package (packageID)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ---------- TRAVELER ----------
CREATE TABLE traveler (
  travelerID    INT NOT NULL AUTO_INCREMENT,
  bookingID     INT NOT NULL,
  firstName     VARCHAR(45) NOT NULL,
  lastName      VARCHAR(45) NOT NULL,
  dateOfBirth   DATE NOT NULL,
  contactNumber VARCHAR(20) NULL,
  PRIMARY KEY (travelerID),
  KEY idx_traveler_booking (bookingID),
  CONSTRAINT fk_traveler_booking
    FOREIGN KEY (bookingID) REFERENCES booking (bookingID)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ---------- PAYMENT ----------
CREATE TABLE payment (
  paymentID INT NOT NULL AUTO_INCREMENT,
  bookingID INT NOT NULL,
  date   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  amount DECIMAL(10,2) NOT NULL,
  method ENUM('Cash','Credit Card','Debit Card','GCash','Bank Transfer') NOT NULL,
  source VARCHAR(45) NULL,
  PRIMARY KEY (paymentID),
  KEY idx_payment_booking (bookingID),
  CONSTRAINT fk_payment_booking
    FOREIGN KEY (bookingID) REFERENCES booking (bookingID)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ---------- ADMIN ACCOUNT ----------
CREATE TABLE adminAccount (
  adminAccountID INT NOT NULL AUTO_INCREMENT,
  username VARCHAR(45) NOT NULL,
  password VARCHAR(255) NOT NULL,
  role     VARCHAR(45) NOT NULL,
  PRIMARY KEY (adminAccountID),
  UNIQUE KEY uq_adminAccount_username (username)
) ENGINE=InnoDB;

-- ---------- PACKAGE PRICE LOCK ----------
-- Price can never be changed. To change a price, set the package to
-- Inactive and create a new Active package (see changePackagePrice).
DROP TRIGGER IF EXISTS trg_package_lock_price;
DELIMITER $$
CREATE TRIGGER trg_package_lock_price
BEFORE UPDATE ON package
FOR EACH ROW
BEGIN
  IF NEW.pricePerTraveller <> OLD.pricePerTraveller THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Package price cannot be changed. Set the package to Inactive and create a new package instead.';
  END IF;
END$$
DELIMITER ;

-- Helper: deactivate the old package + create a new Active copy with the new price
DROP PROCEDURE IF EXISTS changePackagePrice;
DELIMITER $$
CREATE PROCEDURE changePackagePrice(IN p_packageID INT, IN p_newPrice DECIMAL(10,2))
BEGIN
  DECLARE EXIT HANDLER FOR SQLEXCEPTION
  BEGIN
    ROLLBACK;
    RESIGNAL;
  END;

  START TRANSACTION;

  UPDATE package SET status = 'Inactive' WHERE packageID = p_packageID;

  INSERT INTO package
    (destinationID, name, description, pricePerTraveller, status, minTraveller, maxTraveller)
  SELECT destinationID, name, description, p_newPrice, 'Active', minTraveller, maxTraveller
  FROM package
  WHERE packageID = p_packageID;

  COMMIT;

  SELECT LAST_INSERT_ID() AS newPackageID;
END$$
DELIMITER ;