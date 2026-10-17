-- =====================================================
-- travel_db for Apache Derby
-- Run with the ij tool:
--   connect 'jdbc:derby:travel_db;create=true';
--   run 'travel_db_derby.sql';
-- (Derby has no CREATE DATABASE; the connect line above creates it.)
-- =====================================================

-- ---------- CUSTOMER ----------
CREATE TABLE customer (
  customerID INT NOT NULL GENERATED ALWAYS AS IDENTITY,
  firstName  VARCHAR(45) NOT NULL,
  lastName   VARCHAR(45) NOT NULL,
  middleName VARCHAR(45),
  email      VARCHAR(45) NOT NULL,
  sex        VARCHAR(10),
  age        INT,
  CONSTRAINT pk_customer PRIMARY KEY (customerID),
  CONSTRAINT uq_customer_email UNIQUE (email),
  CONSTRAINT chk_customer_sex CHECK (sex IN ('Male','Female'))
);

-- ---------- CUSTOMER ACCOUNT ----------
CREATE TABLE customerAccount (
  customerAccountID INT NOT NULL GENERATED ALWAYS AS IDENTITY,
  customerID INT NOT NULL,
  username   VARCHAR(45) NOT NULL,
  password   VARCHAR(255) NOT NULL,
  status     VARCHAR(10) NOT NULL DEFAULT 'Active',
  createdAt  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT pk_customerAccount PRIMARY KEY (customerAccountID),
  CONSTRAINT uq_customerAccount_username UNIQUE (username),
  CONSTRAINT chk_customerAccount_status CHECK (status IN ('Active','Inactive','Suspended')),
  CONSTRAINT fk_customerAccount_customer
    FOREIGN KEY (customerID) REFERENCES customer (customerID)
    ON DELETE CASCADE
);

-- ---------- DESTINATION ----------
CREATE TABLE destination (
  destinationID INT NOT NULL GENERATED ALWAYS AS IDENTITY,
  name        VARCHAR(45) NOT NULL,
  country     VARCHAR(45) NOT NULL,
  description CLOB,
  imagePath   VARCHAR(255),
  status      VARCHAR(45) NOT NULL DEFAULT 'Active',
  CONSTRAINT pk_destination PRIMARY KEY (destinationID)
);

-- ---------- PACKAGE ----------
CREATE TABLE package (
  packageID INT NOT NULL GENERATED ALWAYS AS IDENTITY,
  destinationID INT NOT NULL,
  name        VARCHAR(45) NOT NULL,
  description CLOB,
  imagePath   VARCHAR(255),
  pricePerTraveller DECIMAL(10,2) NOT NULL,
  status      VARCHAR(10) NOT NULL DEFAULT 'Active',
  minTraveller INT NOT NULL DEFAULT 1,
  maxTraveller INT NOT NULL,
  CONSTRAINT pk_package PRIMARY KEY (packageID),
  CONSTRAINT chk_package_status CHECK (status IN ('Active','Inactive')),
  CONSTRAINT chk_package_traveller_range CHECK (minTraveller <= maxTraveller),
  CONSTRAINT fk_package_destination
    FOREIGN KEY (destinationID) REFERENCES destination (destinationID)
    ON DELETE CASCADE
);

-- ---------- BOOKING ----------
CREATE TABLE booking (
  bookingID INT NOT NULL GENERATED ALWAYS AS IDENTITY,
  customerAccountID INT NOT NULL,
  packageID INT NOT NULL,
  status VARCHAR(10) NOT NULL DEFAULT 'Pending',
  bookingDate TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  travelDate  TIMESTAMP NOT NULL,
  CONSTRAINT pk_booking PRIMARY KEY (bookingID),
  CONSTRAINT chk_booking_status CHECK (status IN ('Pending','Confirmed','Cancelled','Completed')),
  CONSTRAINT fk_booking_customerAccount
    FOREIGN KEY (customerAccountID) REFERENCES customerAccount (customerAccountID)
    ON DELETE CASCADE,
  CONSTRAINT fk_booking_package
    FOREIGN KEY (packageID) REFERENCES package (packageID)
    ON DELETE RESTRICT
);

-- ---------- TRAVELER ----------
CREATE TABLE traveler (
  travelerID    INT NOT NULL GENERATED ALWAYS AS IDENTITY,
  bookingID     INT NOT NULL,
  firstName     VARCHAR(45) NOT NULL,
  lastName      VARCHAR(45) NOT NULL,
  dateOfBirth   DATE NOT NULL,
  contactNumber VARCHAR(20),
  CONSTRAINT pk_traveler PRIMARY KEY (travelerID),
  CONSTRAINT fk_traveler_booking
    FOREIGN KEY (bookingID) REFERENCES booking (bookingID)
    ON DELETE CASCADE
);

-- ---------- PAYMENT ----------
CREATE TABLE payment (
  paymentID INT NOT NULL GENERATED ALWAYS AS IDENTITY,
  bookingID INT NOT NULL,
  paymentDate TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  amount DECIMAL(10,2) NOT NULL,
  method VARCHAR(20) NOT NULL,
  source VARCHAR(45),
  CONSTRAINT pk_payment PRIMARY KEY (paymentID),
  CONSTRAINT chk_payment_method CHECK (method IN ('Cash','Credit Card','Debit Card','GCash','Bank Transfer')),
  CONSTRAINT fk_payment_booking
    FOREIGN KEY (bookingID) REFERENCES booking (bookingID)
    ON DELETE CASCADE
);

-- ---------- ADMIN ACCOUNT ----------
CREATE TABLE adminAccount (
  adminAccountID INT NOT NULL GENERATED ALWAYS AS IDENTITY,
  username VARCHAR(45) NOT NULL,
  password VARCHAR(255) NOT NULL,
  role     VARCHAR(45) NOT NULL,
  CONSTRAINT pk_adminAccount PRIMARY KEY (adminAccountID),
  CONSTRAINT uq_adminAccount_username UNIQUE (username)
);

-- ---------- PACKAGE PRICE LOCK ----------
-- Derby has no SIGNAL, so the trigger forces an error (divide by zero)
-- whenever the price is changed. Requires Derby 10.11+ (WHEN clause).
CREATE TRIGGER trg_package_lock_price
  NO CASCADE BEFORE UPDATE OF pricePerTraveller ON package
  REFERENCING OLD AS o NEW AS n
  FOR EACH ROW
  WHEN (n.pricePerTraveller <> o.pricePerTraveller)
  VALUES (n.pricePerTraveller / 0);

-- ---------- CHANGING A PACKAGE PRICE ----------
-- Derby procedures need Java, so do this as two statements
-- (in one transaction if you do it from JDBC). Example for package 1, new price 15000:
--
-- UPDATE package SET status = 'Inactive' WHERE packageID = 1;
--
-- INSERT INTO package
--   (destinationID, name, description, imagePath, pricePerTraveller, status, minTraveller, maxTraveller)
-- SELECT destinationID, name, description, imagePath, 15000.00, 'Active', minTraveller, maxTraveller
-- FROM package
-- WHERE packageID = 1;
