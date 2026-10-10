# Travel Agency Management System (TAMS)

A desktop-based Travel Agency Management System built with **Java (Swing / AWT)**, **JDBC**, and **Apache Derby**, developed for **Computer Programming 4** (BSIT-T-2B-T) at the **Technological University of the Philippines – Taguig City**.

---

## 📌 Project Overview

The Travel Agency Management System is designed to computerize and streamline the core operations of a travel agency. It provides an offline, centralized management solution for customer accounts, travel destinations, holiday packages, bookings, traveler information, and payment transactions.

### Key Features
* **Role-Based Authentication**: Secure SHA-256 hashed password verification for Administrators, Managers, and Customers.
* **Customer Portal**: Registration, account login, profile viewing, and self-service booking records.
* **Admin Dashboard**: Centralized management hub for:
  * Destination Management
  * Package Management
  * Booking Approvals and Tracking
  * Customer Management
  * Sales & Payment Reporting
  * Admin Account Management
* **Database Normalization & Integrity**: 8 relational tables with primary keys, foreign keys, constraints, and indexes.

---

## 🛠️ Technology Stack

* **Language**: Java 21
* **GUI Toolkit**: Java Swing & AWT
* **Database**: Apache Derby (Network / Embedded)
* **Connectivity**: JDBC (`derbyclient` / `derby`)
* **Build Tool**: Apache Maven
* **IDE**: Apache NetBeans

---

## 📁 Project Structure

```text
Travel-Agency-Management-System/
├── information/              # Course requirements and project documentation PDFs
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── common/       # Database connection, Screen base frame, Session, Seeder
│   │   │   │   ├── DBConnection.java
│   │   │   │   ├── DatabaseSeeder.java
│   │   │   │   ├── Screen.java
│   │   │   │   └── Session.java
│   │   │   ├── dao/          # Data Access Objects (SQL operations)
│   │   │   │   ├── AdminAccountDAO.java
│   │   │   │   ├── CustomerAccountDAO.java
│   │   │   │   └── CustomerDAO.java
│   │   │   ├── model/        # Entity data models
│   │   │   │   ├── AdminAccount.java
│   │   │   │   ├── Booking.java
│   │   │   │   ├── customer.java
│   │   │   │   ├── CustomerAccount.java
│   │   │   │   ├── Destination.java
│   │   │   │   ├── Payment.java
│   │   │   │   ├── Traveler.java
│   │   │   │   └── TravelPackage.java
│   │   │   ├── ui/           # GUI Screens
│   │   │   │   ├── admin/    # Admin screens and management modules
│   │   │   │   ├── customer/ # Customer portal and booking screens
│   │   │   │   ├── AdminLogin.java
│   │   │   │   ├── CustomerLogin.java
│   │   │   │   ├── CustomerRegister.java
│   │   │   │   └── Homepage.java
│   │   │   ├── main/
│   │   │   │   └── Main.java # Application entry point
│   │   │   ├── Database-Script-Apache-Derby.sql
│   │   │   └── Database-Script.sql
│   └── test/
├── pom.xml                   # Maven dependencies and configuration
└── README.md
```

---

## 🚀 Getting Started

### 1. Prerequisites
* **Java Development Kit (JDK) 21** or higher.
* **Apache NetBeans IDE 17+** (or any modern Java IDE with Maven support).
* **Apache Derby Database Server** (port `1527`).

### 2. Database Setup
1. Start your Apache Derby network server in NetBeans (`Services` tab -> `Databases` -> `Java DB` -> `Start Server`).
2. Create a database named `travel_agency`:
   * **JDBC URL**: `jdbc:derby://localhost:1527/travel_agency;create=true`
   * **User**: `root`
   * **Password**: `password`
3. Execute the SQL script located at:
   ```text
   src/main/java/Database-Script-Apache-Derby.sql
   ```
4. Seed starting sample data by running [`common.DatabaseSeeder.java`](src/main/java/common/DatabaseSeeder.java) (Shift + F6 in NetBeans).

---

## 🔑 Default Seed Accounts

After running the `DatabaseSeeder`, the following accounts are ready for testing:

### Administrator Accounts
| Username | Password | Role |
| :--- | :--- | :--- |
| `admin` | `admin123` | Admin |
| `manager` | `manager123` | Manager |

### Customer Accounts
| Username | Password | Full Name |
| :--- | :--- | :--- |
| `juan` | `juan123` | Juan Dela Cruz |
| `maria` | `maria123` | Maria Clara |

---

## 🏃 Running the Application

1. Open the project in **NetBeans**.
2. Right-click the project and select **Clean and Build**.
3. Run the project (F6) or run [`src/main/java/main/Main.java`](src/main/java/main/Main.java).
