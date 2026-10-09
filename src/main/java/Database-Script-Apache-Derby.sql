-- =====================================================
-- TRAVEL AGENCY MANAGEMENT SYSTEM
-- Database: travel_db
-- DBMS: Apache Derby
-- =====================================================


-- =====================================================
-- 1. CUSTOMER
-- =====================================================

CREATE TABLE customer (
    customerID INT NOT NULL
        GENERATED ALWAYS AS IDENTITY
        (START WITH 1, INCREMENT BY 1),

    firstName VARCHAR(45) NOT NULL,
    lastName VARCHAR(45) NOT NULL,
    middleName VARCHAR(45),
    email VARCHAR(45) NOT NULL,

    sex VARCHAR(10)
        CHECK (sex IN ('Male', 'Female')),

    age INT,

    CONSTRAINT pk_customer
        PRIMARY KEY (customerID),

    CONSTRAINT uq_customer_email
        UNIQUE (email),

    CONSTRAINT chk_customer_age
        CHECK (age IS NULL OR age >= 0)
);


-- =====================================================
-- 2. CUSTOMER ACCOUNT
-- =====================================================

CREATE TABLE customerAccount (
    customerAccountID INT NOT NULL
        GENERATED ALWAYS AS IDENTITY
        (START WITH 1, INCREMENT BY 1),

    customerID INT NOT NULL,
    username VARCHAR(45) NOT NULL,
    password VARCHAR(255) NOT NULL,

    status VARCHAR(10) DEFAULT 'Active' NOT NULL
        CHECK (status IN ('Active', 'Inactive', 'Suspended')),

    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

    CONSTRAINT pk_customerAccount
        PRIMARY KEY (customerAccountID),

    CONSTRAINT uq_customerAccount_username
        UNIQUE (username),

    CONSTRAINT fk_customerAccount_customer
        FOREIGN KEY (customerID)
        REFERENCES customer (customerID)
        ON DELETE CASCADE
);


-- =====================================================
-- 3. DESTINATION
-- =====================================================

CREATE TABLE destination (
    destinationID INT NOT NULL
        GENERATED ALWAYS AS IDENTITY
        (START WITH 1, INCREMENT BY 1),

    name VARCHAR(45) NOT NULL,
    country VARCHAR(45) NOT NULL,
    description CLOB,

    status VARCHAR(10) DEFAULT 'Active' NOT NULL
        CHECK (status IN ('Active', 'Inactive')),

    CONSTRAINT pk_destination
        PRIMARY KEY (destinationID)
);


-- =====================================================
-- 4. PACKAGE
-- =====================================================

CREATE TABLE package (
    packageID INT NOT NULL
        GENERATED ALWAYS AS IDENTITY
        (START WITH 1, INCREMENT BY 1),

    destinationID INT NOT NULL,
    name VARCHAR(45) NOT NULL,
    description CLOB,

    pricePerTraveller DECIMAL(10,2) NOT NULL,

    status VARCHAR(10) DEFAULT 'Active' NOT NULL
        CHECK (status IN ('Active', 'Inactive')),

    minTraveller INT DEFAULT 1 NOT NULL,
    maxTraveller INT NOT NULL,

    CONSTRAINT pk_package
        PRIMARY KEY (packageID),

    CONSTRAINT fk_package_destination
        FOREIGN KEY (destinationID)
        REFERENCES destination (destinationID),

    CONSTRAINT chk_package_price
        CHECK (pricePerTraveller >= 0),

    CONSTRAINT chk_package_travellers
        CHECK (
            minTraveller >= 1
            AND maxTraveller >= minTraveller
        )
);


-- =====================================================
-- 5. BOOKING
-- =====================================================

CREATE TABLE booking (
    bookingID INT NOT NULL
        GENERATED ALWAYS AS IDENTITY
        (START WITH 1, INCREMENT BY 1),

    customerAccountID INT NOT NULL,
    packageID INT NOT NULL,

    status VARCHAR(10) DEFAULT 'Pending' NOT NULL
        CHECK (
            status IN (
                'Pending',
                'Confirmed',
                'Cancelled',
                'Completed'
            )
        ),

    bookingDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    travelDate TIMESTAMP NOT NULL,

    CONSTRAINT pk_booking
        PRIMARY KEY (bookingID),

    CONSTRAINT fk_booking_customerAccount
        FOREIGN KEY (customerAccountID)
        REFERENCES customerAccount (customerAccountID)
        ON DELETE CASCADE,

    CONSTRAINT fk_booking_package
        FOREIGN KEY (packageID)
        REFERENCES package (packageID)
);


-- =====================================================
-- 6. TRAVELER
-- =====================================================

CREATE TABLE traveler (
    travelerID INT NOT NULL
        GENERATED ALWAYS AS IDENTITY
        (START WITH 1, INCREMENT BY 1),

    bookingID INT NOT NULL,
    firstName VARCHAR(45) NOT NULL,
    lastName VARCHAR(45) NOT NULL,
    dateOfBirth DATE NOT NULL,
    contactNumber VARCHAR(20),

    CONSTRAINT pk_traveler
        PRIMARY KEY (travelerID),

    CONSTRAINT fk_traveler_booking
        FOREIGN KEY (bookingID)
        REFERENCES booking (bookingID)
        ON DELETE CASCADE
);


-- =====================================================
-- 7. PAYMENT
-- =====================================================

CREATE TABLE payment (
    paymentID INT NOT NULL
        GENERATED ALWAYS AS IDENTITY
        (START WITH 1, INCREMENT BY 1),

    bookingID INT NOT NULL,
    paymentDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    amount DECIMAL(10,2) NOT NULL,

    method VARCHAR(20) NOT NULL
        CHECK (
            method IN (
                'Cash',
                'Credit Card',
                'Debit Card',
                'GCash',
                'Bank Transfer'
            )
        ),

    source VARCHAR(45),

    CONSTRAINT pk_payment
        PRIMARY KEY (paymentID),

    CONSTRAINT fk_payment_booking
        FOREIGN KEY (bookingID)
        REFERENCES booking (bookingID)
        ON DELETE CASCADE,

    CONSTRAINT chk_payment_amount
        CHECK (amount > 0)
);


-- =====================================================
-- 8. ADMIN ACCOUNT
-- =====================================================

CREATE TABLE adminAccount (
    adminAccountID INT NOT NULL
        GENERATED ALWAYS AS IDENTITY
        (START WITH 1, INCREMENT BY 1),

    username VARCHAR(45) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(45) NOT NULL,

    CONSTRAINT pk_adminAccount
        PRIMARY KEY (adminAccountID),

    CONSTRAINT uq_adminAccount_username
        UNIQUE (username)
);


-- =====================================================
-- INDEXES
-- =====================================================

CREATE INDEX idx_customerAccount_customer
    ON customerAccount (customerID);

CREATE INDEX idx_package_destination
    ON package (destinationID);

CREATE INDEX idx_booking_customerAccount
    ON booking (customerAccountID);

CREATE INDEX idx_booking_package
    ON booking (packageID);

CREATE INDEX idx_traveler_booking
    ON traveler (bookingID);

CREATE INDEX idx_payment_booking
    ON payment (bookingID);