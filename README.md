# SmartCash Pro: A Complete Project in Object-Oriented, A Point of Sale System

## Table of Contents
1.  [Project Overview & Our Unique Collaboration Story](#1-project-overview--our-unique-collaboration-story)
2.  [How to Run the Project on Your PC (Windows)](#2-how-to-run-the-project-on-your-pc-windows)
    *   [Prerequisites](#prerequisites)
    *   [Database Setup (MySQL on Windows)](#database-setup-mysql-on-windows)
    *   [Project Setup in Your Preferred IDE](#project-setup-in-your-preferred-ide)
    *   [Running the Application](#running-the-application)
3.  [SmartCashPro.sql Database Script](#3-smartcashprosql-database-script)
4.  [Project Structure](#4-project-structure)
5.  [Key Features](#5-key-features)
6.  [Team Contributions](#6-team-contributions)
7.  [Future Enhancements](#7-future-enhancements)
8.  [Group Information](#8-group-information)
9.  [License](#9-license)

---

## 1. Project Overview & Our Unique Collaboration Story

Our team of four – Farida, Musaab, Usman, and Ahmad Hassan – embraced a "linear contribution" model. Each team member would be responsible for a distinct, self-contained module, developing it independently and focusing on their strengths. Musaab will integrate and fix or work on the remaining GUI design.

The result is **SmartCash Pro**, a robust desktop Point of Sale (POS) and inventory management system designed to streamline retail operations. 

## 2. How to Run the Project on Your PC (Windows)

This project is packaged as an Eclipse workspace archive, ensuring all necessary libraries are included and configured for a seamless setup.

### Prerequisites
*   **Java Development Kit (JDK) 8 or higher:** You'll need this to compile and run Java applications.
    *   **Windows-Specific Note:** After installation, ensure `JAVA_HOME` is set as an environment variable and the JDK's `bin` directory (e.g., `C:\Program Files\Java\jdk-xx\bin`) is included in your system's PATH. You can verify this by opening `Command Prompt` and typing `java -version`.
*   **MySQL Database Server:** The application uses MySQL for data persistence. Ensure you have MySQL Server installed and running on your Windows machine (e.g., via XAMPP, WAMP, or a standalone MySQL Community Server installation). You'll also need a way to execute SQL commands, such as MySQL Workbench or the `mysql.exe` client.

### Database Setup (MySQL on Windows)

The application relies on a specific database structure and initial data. We've included the complete SQL script, `SmartCashPro.sql`, in the project's root directory for easy setup.

1.  **Configure MySQL User and Password (Crucial!):**
    The Java application's `DatabaseConnector.java` class is configured to connect to your MySQL server using specific credentials:
    *   **Username:** `m`
    *   **Password:** `mmmm`

    You **must** create this user and grant it permissions to the `SmartCashPro` database.

    *   **Open MySQL Client:** Use MySQL Workbench, your command line MySQL client, or any other tool where you can execute SQL commands.
    *   **Execute the following SQL as a root or administrative user (e.g., after `mysql -u root -p`):**

    ```sql
    -- creates db if not there
    CREATE DATABASE IF NOT EXISTS SmartCashPro CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

    -- Create the user 'm' with password 'mmmm'
    -- If user 'm' already exists but with a different password, you might need to drop and recreate or alter it.
    CREATE USER 'm'@'localhost' IDENTIFIED BY 'mmmm';

    -- Grant all necessary privileges to the 'm' user on the SmartCashPro database
    GRANT ALL PRIVILEGES ON SmartCashPro.* TO 'm'@'localhost';

    -- Apply changes to ensure they take effect immediately
    FLUSH PRIVILEGES;
    ```
    **SECURITY NOTE:** For a real-world application, hardcoding credentials like `m`/`mmmm` and `root`/`root` (for initial setup) is strongly discouraged. Always use stronger, unique passwords and consider environment variables or secure configuration files for credentials. If you *must* use different credentials than `m`/`mmmm`, you **must** update the `DB_USER` and `DB_PASSWORD` static final fields in `src/com/smartcashpro/db/DatabaseConnector.java` to match. Since this project was only for me and my teammates learning journey, all of this (probably and quite highly likely) does not follow industrial standards :}

2.  **Import Schema and Sample Data (`SmartCashPro.sql`):**
    This step populates the `SmartCashPro` database with all the necessary tables, triggers, stored procedures, and some initial data (including the `admin` user).

    *   **Using Command Prompt (Recommended for ease):**
        1.  Navigate your `Command Prompt` to the root directory where you unzipped the project (e.g., `cd C:\Users\YourUser\Documents\oop`). This is where `SmartCashPro.sql` is located.
        2.  Execute the following command, which will run the `SmartCashPro.sql` script using the `m` user:
            ```cmd
            mysql -u m -pmmmm SmartCashPro < SmartCashPro.sql
            ```
            (Ensure `m` and `mmmm` match the user and password you just created/verified in Step 1.)
    *   **Using MySQL Workbench:**
        1.  Connect to your `localhost` MySQL instance using a user with sufficient privileges (like `root` or `m`).
        2.  From the top menu, go to `File > Open SQL Script...` and select the `SmartCashPro.sql` file from your unzipped project directory.
        3.  Ensure the `SmartCashPro` database is selected in the Schemas navigator (double-click it in the left panel to set it as default).
        4.  Click the "Execute" button (lightning bolt icon) to run the entire script.

### Project Setup in Your Preferred IDE

1.  **Download Project Zip:**
    Download the provided `.zip` file, which contains the entire Eclipse project. (zip file, along with .sql script can be found in the releases section of github repo)
2.  **Unzip the Project:**
    Extract the contents of the `.zip` file to your desired location on your Windows machine. You can use Windows' built-in "Extract All" feature, or third-party tools like 7-Zip or WinRAR. You should find a folder named `oop` containing `.project`, `.classpath`, and `src` directories.
3.  **Import into Eclipse (Recommended):**
    *   Open Eclipse.
    *   Go to `File > Import...`.
    *   Select `General > Existing Projects into Workspace` and click `Next`.
    *   For "Select root directory:", click `Browse...` and navigate to the unzipped `oop` folder.
    *   Ensure "SmartCashPro" (or "oop") is checked under "Projects:" and click `Finish`.
    *   Eclipse should automatically detect and configure the included `mysql-connector-java-x.x.x.jar` (and `jcalendar-x.x.x.jar` if present) from the `lib` folder.
4.  **Import into Other IDEs (e.g., IntelliJ IDEA, NetBeans):**
    *   Open your IDE.
    *   Select `File > Open` or `File > Import Project`.
    *   Navigate to the unzipped `oop` folder and open it as a project.
    *   Your IDE should typically detect it as a standard Java project. You might need to manually add the `mysql-connector-java-x.x.x.jar` and `jcalendar-x.x.x.jar` files (located in the `lib` folder within the project) to your project's dependencies/build path. Most modern IDEs can often do this automatically or guide you.

### Running the Application

1.  **Locate Main Class:**
    In your IDE's project explorer, navigate to `src/com/smartcashpro/SmartCashProApp.java`.
2.  **Run:**
    Right-click `SmartCashProApp.java` and select `Run As > Java Application`.

The application should start and display the login page.
*   **Default Login:** After running `SmartCashPro.sql`, a default admin user is created. You can log in with:
    *   **Username:** `admin`
    *   **Password:** `adminpass`

## 3. SmartCashPro.sql Database Script

This is the complete SQL script used to set up the `SmartCashPro` database, including all tables, relationships, views, triggers, stored procedures, and initial sample data. You will have run this script during the [Database Setup](#database-setup-mysql-on-windows) phase.

```sql
-- =============================================================================
-- SmartCashPro Database Creation Script
-- Based on EERD discussed.
-- =============================================================================

-- Drop database if it exists to ensure a clean setup
DROP DATABASE IF EXISTS SmartCashPro;

-- Create the database with UTF8 character set for broader compatibility
CREATE DATABASE SmartCashPro CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Use the newly created database
USE SmartCashPro;

-- =============================================================================
-- Table Creation
-- Create tables in an order that respects foreign key dependencies
-- =============================================================================

-- USER Table (For system logins - Employees link to this)
CREATE TABLE USER (
    UserID INT AUTO_INCREMENT PRIMARY KEY,
    Username VARCHAR(50) NOT NULL UNIQUE,
    PasswordHash VARCHAR(255) NOT NULL, -- Store hashed passwords only!
    Role ENUM('Cashier', 'Manager', 'Admin') NOT NULL DEFAULT 'Cashier',
    IsActive BOOLEAN NOT NULL DEFAULT TRUE, -- TINYINT(1)
    CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- CATEGORY Table (For organizing products)
CREATE TABLE CATEGORY (
    CategoryID INT AUTO_INCREMENT PRIMARY KEY,
    CategoryName VARCHAR(100) NOT NULL UNIQUE,
    Description TEXT NULL
);

-- SUPPLIER Table
CREATE TABLE SUPPLIER (
    SupplierID INT AUTO_INCREMENT PRIMARY KEY,
    SupplierName VARCHAR(150) NOT NULL,
    ContactPerson VARCHAR(100) NULL,
    ContactInfo VARCHAR(255) NULL, -- Phone, Email etc.
    Address TEXT NULL
);

-- SHIFT Table (Represents work shifts)
CREATE TABLE SHIFT (
    ShiftID INT AUTO_INCREMENT PRIMARY KEY,
    StartTime DATETIME NOT NULL,
    EndTime DATETIME NULL,
    Status ENUM('Open', 'Closed', 'Reconciled') NOT NULL DEFAULT 'Open',
    StartUserID INT NOT NULL, -- User who started the shift
    EndUserID INT NULL,     -- User who ended the shift
    StartingFloat DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    CashSalesAmount DECIMAL(10, 2) NULL DEFAULT 0.00, -- Calculated at end
    CardSalesAmount DECIMAL(10, 2) NULL DEFAULT 0.00, -- Calculated at end
    OtherSalesAmount DECIMAL(10, 2) NULL DEFAULT 0.00, -- Calculated at end
    CashRemoved DECIMAL(10, 2) NULL DEFAULT 0.00,   -- Cash taken out during/at end
    EndingFloat DECIMAL(10, 2) NULL,                 -- Actual cash count at end
    CashDiscrepancy DECIMAL(10, 2) NULL,             -- Calculated difference
    FOREIGN KEY (StartUserID) REFERENCES USER(UserID) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (EndUserID) REFERENCES USER(UserID) ON DELETE RESTRICT ON UPDATE CASCADE
);

-- EMPLOYEE Table (Links to USER for login)
CREATE TABLE EMPLOYEE (
    EmployeeID INT AUTO_INCREMENT PRIMARY KEY,
    FirstName VARCHAR(100) NOT NULL,
    LastName VARCHAR(100) NOT NULL,
    ContactInfo VARCHAR(255) NULL,
    Address TEXT NULL,
    DateOfBirth DATE NULL,
    HireDate DATE NOT NULL,
    TerminationDate DATE NULL,
    Position VARCHAR(100) NULL,
    PayRate DECIMAL(10, 2) NULL DEFAULT 0.00,
    EmploymentStatus ENUM('Active', 'Inactive', 'Terminated') NOT NULL DEFAULT 'Active',
    UserID INT NOT NULL UNIQUE, -- Each employee MUST have a unique user account (1:1 link)
    FOREIGN KEY (UserID) REFERENCES USER(UserID) ON DELETE RESTRICT ON UPDATE CASCADE -- If user deleted, need to handle employee? Restrict safer.
);

-- CUSTOMER Table
CREATE TABLE CUSTOMER (
    CustomerID INT AUTO_INCREMENT PRIMARY KEY,
    Name VARCHAR(150) NOT NULL,
    ContactInfo VARCHAR(255) NULL, -- Phone, Email etc.
    LoyaltyPoints INT DEFAULT 0,
    CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- PROMOTION Table
CREATE TABLE PROMOTION (
    PromotionID INT AUTO_INCREMENT PRIMARY KEY,
    Name VARCHAR(150) NOT NULL,
    Description TEXT NULL,
    DiscountType ENUM('Percentage', 'FixedAmount') NOT NULL,
    DiscountValue DECIMAL(10, 2) NOT NULL,
    StartDate DATETIME NULL,
    EndDate DATETIME NULL,
    IsActive BOOLEAN NOT NULL DEFAULT TRUE -- TINYINT(1)
);

-- PRODUCT Table (Superclass for EER)
CREATE TABLE PRODUCT (
    ProductID INT AUTO_INCREMENT PRIMARY KEY,
    SKU VARCHAR(100) NOT NULL UNIQUE, -- Stock Keeping Unit
    Name VARCHAR(150) NOT NULL,
    Description TEXT NULL,
    UnitPrice DECIMAL(10, 2) NOT NULL DEFAULT 0.00, -- Selling price
    QuantityInStock INT NOT NULL DEFAULT 0,
    CurrentCostPrice DECIMAL(10, 2) NOT NULL DEFAULT 0.00, -- Buying price
    ReorderLevel INT NULL DEFAULT 0,
    CategoryID INT NULL,
    SupplierID INT NULL, -- Primary Supplier
    LastReceivedDate DATETIME NULL,
    CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (CategoryID) REFERENCES CATEGORY(CategoryID) ON DELETE SET NULL ON UPDATE CASCADE, -- Can exist without category? SET NULL
    FOREIGN KEY (SupplierID) REFERENCES SUPPLIER(SupplierID) ON DELETE SET NULL ON UPDATE CASCADE -- Can exist without supplier? SET NULL
);

-- PERISHABLE_PRODUCT Table (Subclass of PRODUCT)
CREATE TABLE PERISHABLE_PRODUCT (
    ProductID INT PRIMARY KEY, -- Same PK as PRODUCT, also FK
    StorageTempRequirement VARCHAR(100) NULL,
    FOREIGN KEY (ProductID) REFERENCES PRODUCT(ProductID) ON DELETE CASCADE ON UPDATE CASCADE -- If base product deleted, this goes too
);

-- NONPERISHABLE_PRODUCT Table (Subclass of PRODUCT)
CREATE TABLE NONPERISHABLE_PRODUCT (
    ProductID INT PRIMARY KEY, -- Same PK as PRODUCT, also FK
    FOREIGN KEY (ProductID) REFERENCES PRODUCT(ProductID) ON DELETE CASCADE ON UPDATE CASCADE -- If base product deleted, this goes too
);


-- PURCHASE_ORDER Table
CREATE TABLE PURCHASE_ORDER (
    PurchaseOrderID INT AUTO_INCREMENT PRIMARY KEY,
    PODate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    Status ENUM('Pending', 'Ordered', 'PartiallyReceived', 'Received', 'Cancelled') NOT NULL DEFAULT 'Pending',
    ExpectedDeliveryDate DATE NULL,
    ActualDeliveryDate DATE NULL,
    SupplierID INT NOT NULL,
    PlacedByUserID INT NOT NULL,
    TotalCost DECIMAL(10, 2) NULL DEFAULT 0.00, -- Calculated from items
    FOREIGN KEY (SupplierID) REFERENCES SUPPLIER(SupplierID) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (PlacedByUserID) REFERENCES USER(UserID) ON DELETE RESTRICT ON UPDATE CASCADE
);

-- PURCHASE_ORDER_ITEM Table (Linking table for PurchaseOrder and Product)
CREATE TABLE PURCHASE_ORDER_ITEM (
    PurchaseOrderItemID INT AUTO_INCREMENT PRIMARY KEY,
    PurchaseOrderID INT NOT NULL,
    ProductID INT NOT NULL,
    QuantityOrdered INT NOT NULL DEFAULT 1,
    CostPricePerUnit DECIMAL(10, 2) NOT NULL, -- Cost at the time of ordering
    QuantityReceived INT NULL DEFAULT 0,
    FOREIGN KEY (PurchaseOrderID) REFERENCES PURCHASE_ORDER(PurchaseOrderID) ON DELETE CASCADE ON UPDATE CASCADE, -- If PO deleted, items go too
    FOREIGN KEY (ProductID) REFERENCES PRODUCT(ProductID) ON DELETE RESTRICT ON UPDATE CASCADE -- Don't delete product if it's on a PO
);

-- ORDER Table (Sales Order)
CREATE TABLE `ORDER` ( -- Using backticks because ORDER is a reserved keyword
    OrderID INT AUTO_INCREMENT PRIMARY KEY,
    OrderDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    TotalAmount DECIMAL(10, 2) NOT NULL DEFAULT 0.00, -- Calculated from items/payment
    CustomerID INT NULL, -- Optional link to customer
    UserID INT NOT NULL, -- User who processed the order
    ShiftID INT NOT NULL, -- Shift during which order occurred
    OrderStatus ENUM('Completed', 'Pending', 'Cancelled', 'Returned') NOT NULL DEFAULT 'Completed',
    FOREIGN KEY (CustomerID) REFERENCES CUSTOMER(CustomerID) ON DELETE SET NULL ON UPDATE CASCADE, -- If customer deleted, order remains anonymous
    FOREIGN KEY (UserID) REFERENCES USER(UserID) ON DELETE RESTRICT ON UPDATE CASCADE, -- Don't delete user if they processed orders
    FOREIGN KEY (ShiftID) REFERENCES SHIFT(ShiftID) ON DELETE RESTRICT ON UPDATE CASCADE -- Don't delete shift if orders exist in it
);

-- ORDER_ITEM Table (Linking table for Order and Product)
CREATE TABLE ORDER_ITEM (
    OrderItemID INT AUTO_INCREMENT PRIMARY KEY,
    OrderID INT NOT NULL,
    ProductID INT NOT NULL,
    Quantity INT NOT NULL DEFAULT 1,
    UnitPriceAtSale DECIMAL(10, 2) NOT NULL, -- Price at the time of sale
    CostPriceAtSale DECIMAL(10, 2) NOT NULL, -- Cost at the time of sale (for profit tracking)
    AppliedPromotionID INT NULL, -- Optional link to promotion
    FOREIGN KEY (OrderID) REFERENCES `ORDER`(OrderID) ON DELETE CASCADE ON UPDATE CASCADE, -- If order deleted, items go too
    FOREIGN KEY (ProductID) REFERENCES PRODUCT(ProductID) ON DELETE RESTRICT ON UPDATE CASCADE, -- Don't delete product if sold
    FOREIGN KEY (AppliedPromotionID) REFERENCES PROMOTION(PromotionID) ON DELETE SET NULL ON UPDATE CASCADE -- If promo deleted, link is removed
);


-- PAYMENT Table (Superclass for EER)
CREATE TABLE PAYMENT (
    PaymentID INT AUTO_INCREMENT PRIMARY KEY,
    PaymentDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    Amount DECIMAL(10, 2) NOT NULL,
    OrderID INT NOT NULL UNIQUE, -- Each order has exactly one payment summary record (1:1)
    FOREIGN KEY (OrderID) REFERENCES `ORDER`(OrderID) ON DELETE CASCADE ON UPDATE CASCADE -- If order deleted, payment goes too
);

-- CASH_PAYMENT Table (Subclass of PAYMENT)
CREATE TABLE CASH_PAYMENT (
    PaymentID INT PRIMARY KEY, -- Same PK as PAYMENT, also FK
    AmountTendered DECIMAL(10, 2) NOT NULL,
    ChangeGiven DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (PaymentID) REFERENCES PAYMENT(PaymentID) ON DELETE CASCADE ON UPDATE CASCADE
);

-- CARD_PAYMENT Table (Subclass of PAYMENT)
CREATE TABLE CARD_PAYMENT (
    PaymentID INT PRIMARY KEY, -- Same PK as PAYMENT, also FK
    CardType VARCHAR(50) NULL, -- Visa, Mastercard etc.
    Last4Digits VARCHAR(4) NULL,
    AuthCode VARCHAR(50) NULL, -- Authorization code from terminal
    -- Add other card related fields if needed
    FOREIGN KEY (PaymentID) REFERENCES PAYMENT(PaymentID) ON DELETE CASCADE ON UPDATE CASCADE
);

-- OTHER_PAYMENT Table (Subclass of PAYMENT)
CREATE TABLE OTHER_PAYMENT (
    PaymentID INT PRIMARY KEY, -- Same PK as PAYMENT, also FK
    PaymentProvider VARCHAR(100) NOT NULL, -- e.g., PayPal, Google Pay, Store Credit
    TransactionReference VARCHAR(255) NULL, -- Reference ID from provider
    FOREIGN KEY (PaymentID) REFERENCES PAYMENT(PaymentID) ON DELETE CASCADE ON UPDATE CASCADE
);


-- STOCK_ADJUSTMENT Table
CREATE TABLE STOCK_ADJUSTMENT (
    AdjustmentID INT AUTO_INCREMENT PRIMARY KEY,
    AdjustmentDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ProductID INT NOT NULL,
    UserID INT NOT NULL, -- User who made the adjustment
    ShiftID INT NOT NULL, -- Shift during which adjustment occurred
    QuantityChange INT NOT NULL, -- Can be positive or negative
    Reason VARCHAR(255) NOT NULL, -- e.g., Damage, Spoilage, Count Correction, Theft
    Notes TEXT NULL,
    FOREIGN KEY (ProductID) REFERENCES PRODUCT(ProductID) ON DELETE RESTRICT ON UPDATE CASCADE, -- Don't delete product if adjustments exist
    FOREIGN KEY (UserID) REFERENCES USER(UserID) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (ShiftID) REFERENCES SHIFT(ShiftID) ON DELETE RESTRICT ON UPDATE CASCADE
);

-- SALES_RETURN Table
CREATE TABLE SALES_RETURN (
    ReturnID INT AUTO_INCREMENT PRIMARY KEY,
    ReturnDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    OriginalOrderItemID INT NOT NULL UNIQUE, -- Link to the specific item that was sold and returned (Ensures item returned only once)
    QuantityReturned INT NOT NULL DEFAULT 1,
    Reason VARCHAR(255) NULL,
    RestockFlag BOOLEAN NOT NULL DEFAULT FALSE, -- TINYINT(1) - Should it go back into stock?
    RefundAmount DECIMAL(10, 2) NOT NULL,
    ProcessedByUserID INT NOT NULL,
    ShiftID INT NOT NULL,
    FOREIGN KEY (OriginalOrderItemID) REFERENCES ORDER_ITEM(OrderItemID) ON DELETE RESTRICT ON UPDATE CASCADE, -- Don't delete OrderItem if returned
    FOREIGN KEY (ProcessedByUserID) REFERENCES USER(UserID) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (ShiftID) REFERENCES SHIFT(ShiftID) ON DELETE RESTRICT ON UPDATE CASCADE
);


-- TIMESHEET Table (Links Employee and Shift for M:N)
CREATE TABLE TIMESHEET (
    TimesheetID INT AUTO_INCREMENT PRIMARY KEY,
    EmployeeID INT NOT NULL,
    ShiftID INT NOT NULL,
    TimeIn DATETIME NOT NULL,
    TimeOut DATETIME NULL,
    HoursWorked DECIMAL(5, 2) NULL, -- Can be calculated
    FOREIGN KEY (EmployeeID) REFERENCES EMPLOYEE(EmployeeID) ON DELETE CASCADE ON UPDATE CASCADE, -- If employee deleted, timesheets go? Or restrict? Cascade for cleanup.
    FOREIGN KEY (ShiftID) REFERENCES SHIFT(ShiftID) ON DELETE RESTRICT ON UPDATE CASCADE -- Don't delete shift if timesheets exist
);

-- PAYROLL_RECORD Table
CREATE TABLE PAYROLL_RECORD (
    PayrollRecordID INT AUTO_INCREMENT PRIMARY KEY,
    EmployeeID INT NOT NULL,
    PayPeriodStartDate DATE NOT NULL,
    PayPeriodEndDate DATE NOT NULL,
    TotalHours DECIMAL(7, 2) NOT NULL DEFAULT 0.00, -- Summed from Timesheets for period
    GrossPay DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    Deductions DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    NetPay DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    PaymentDate DATE NOT NULL, -- Date payroll was issued
    FOREIGN KEY (EmployeeID) REFERENCES EMPLOYEE(EmployeeID) ON DELETE RESTRICT ON UPDATE CASCADE -- Don't delete employee if payroll exists
);

-- AUDIT Table for Product Price Changes
CREATE TABLE PRODUCT_PRICE_AUDIT (
    AuditID INT AUTO_INCREMENT PRIMARY KEY,
    ProductID INT NOT NULL,
    OldUnitPrice DECIMAL(10,2) NOT NULL,
    NewUnitPrice DECIMAL(10,2) NOT NULL,
    ChangedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ChangedByUserID INT NULL, -- Optional: To be populated if app sets a session var or passes it
    FOREIGN KEY (ProductID) REFERENCES PRODUCT(ProductID) ON DELETE CASCADE,
    FOREIGN KEY (ChangedByUserID) REFERENCES USER(UserID) ON DELETE SET NULL -- User might be deleted later
);


-- =============================================================================
-- VIEWS
-- =============================================================================

-- View for Low Stock Products (Relevant to InventoryPanel's LowStockRenderer)
CREATE VIEW vw_LowStockProducts AS
SELECT
    ProductID,
    SKU,
    Name,
    QuantityInStock,
    ReorderLevel,
    CategoryID,
    SupplierID
FROM PRODUCT
WHERE QuantityInStock <= ReorderLevel AND ReorderLevel > 0;

-- View for Order Summary with Details (Relevant to OrderHistoryPanel and OrderDAO)
CREATE VIEW vw_OrderSummaryWithDetails AS
SELECT
    o.OrderID,
    o.OrderDate,
    o.TotalAmount,
    o.CustomerID,
    cust.Name AS CustomerName,
    o.UserID,
    usr.Username AS CashierName,
    o.ShiftID,
    o.OrderStatus,
    pay.PaymentID,
    CASE
        WHEN cp.PaymentID IS NOT NULL THEN 'Cash'
        WHEN cdp.PaymentID IS NOT NULL THEN 'Card'
        WHEN op.PaymentID IS NOT NULL THEN 'Other'
        ELSE 'Unknown'
    END AS PaymentType,
    cp.AmountTendered AS CashAmountTendered,
    cp.ChangeGiven AS CashChangeGiven,
    cdp.CardType,
    cdp.Last4Digits AS CardLast4Digits,
    op.PaymentProvider AS OtherPaymentProvider
FROM `ORDER` o
LEFT JOIN CUSTOMER cust ON o.CustomerID = cust.CustomerID
JOIN USER usr ON o.UserID = usr.UserID
LEFT JOIN PAYMENT pay ON o.OrderID = pay.OrderID
LEFT JOIN CASH_PAYMENT cp ON pay.PaymentID = cp.PaymentID
LEFT JOIN CARD_PAYMENT cdp ON pay.PaymentID = cdp.PaymentID
LEFT JOIN OTHER_PAYMENT op ON pay.PaymentID = op.PaymentID;

-- =============================================================================
-- TRIGGERS
-- =============================================================================

DELIMITER //

-- Trigger to Log Product Price Changes (Relevant to InventoryPanel product editing)
CREATE TRIGGER trg_LogProductPriceChange
AFTER UPDATE ON PRODUCT
FOR EACH ROW
BEGIN
    -- Only log if UnitPrice actually changed
    IF OLD.UnitPrice <> NEW.UnitPrice THEN
        -- ChangedByUserID would ideally be set by the application via a session variable
        -- or passed if the update was done through a stored procedure.
        -- For this example, it will be NULL unless a mechanism is added.
        INSERT INTO PRODUCT_PRICE_AUDIT (ProductID, OldUnitPrice, NewUnitPrice, ChangedByUserID)
        VALUES (OLD.ProductID, OLD.UnitPrice, NEW.UnitPrice, NULL); -- THIS LINE IS NOW CORRECT
    END IF;
END //

-- Trigger to Prevent Negative Stock on Product Update (General data integrity)
CREATE TRIGGER trg_PreventNegativeStockOnProductUpdate
BEFORE UPDATE ON PRODUCT
FOR EACH ROW
BEGIN
    IF NEW.QuantityInStock < 0 THEN
        SIGNAL SQLSTATE '45000' -- '45000' is a generic user-defined error state
        SET MESSAGE_TEXT = 'Product quantity cannot be updated to a negative value.';
    END IF;
END //

DELIMITER ;

-- =============================================================================
-- STORED PROCEDURES
-- =============================================================================

DELIMITER //

-- Stored Procedure to Get Full Product Details (including type)
-- Relevant to ProductDAO.findProductById and InventoryPanel editing
CREATE PROCEDURE sp_GetProductFullDetails(IN p_product_id INT)
BEGIN
    SELECT
        p.ProductID, p.SKU, p.Name, p.Description, p.UnitPrice, p.QuantityInStock,
        p.CurrentCostPrice, p.ReorderLevel,
        cat.CategoryName, sup.SupplierName, p.LastReceivedDate,
        CASE
            WHEN pp.ProductID IS NOT NULL THEN 'Perishable'
            WHEN np.ProductID IS NOT NULL THEN 'Non-Perishable'
        END AS ProductType,
        pp.StorageTempRequirement
    FROM PRODUCT p
    LEFT JOIN CATEGORY cat ON p.CategoryID = cat.CategoryID
    LEFT JOIN SUPPLIER sup ON p.SupplierID = sup.SupplierID
    LEFT JOIN PERISHABLE_PRODUCT pp ON p.ProductID = pp.ProductID
    LEFT JOIN NONPERISHABLE_PRODUCT np ON p.ProductID = np.ProductID
    WHERE p.ProductID = p_product_id;
END //

-- Stored Procedure to Get Orders by Customer ID
-- Relevant if displaying customer-specific order history
CREATE PROCEDURE sp_GetOrdersByCustomer(IN p_customer_id INT)
BEGIN
    SELECT
        o.OrderID,
        o.OrderDate,
        o.TotalAmount,
        o.OrderStatus,
        u.Username as CashierName
    FROM `ORDER` o
    JOIN USER u ON o.UserID = u.UserID
    WHERE o.CustomerID = p_customer_id
    ORDER BY o.OrderDate DESC;
END //

DELIMITER ;

-- =============================================================================
-- Basic Sample Data Insertion
-- Insert data respecting foreign key constraints
-- =============================================================================

-- Users (Password hashes are placeholders - use proper hashing in application!)
INSERT INTO USER (Username, PasswordHash, Role, IsActive) VALUES
('cashier1', 'cashier1pass', 'Cashier', TRUE),    -- Changed to plain for UserDAO
('manager1', 'manager1pass', 'Manager', TRUE),    -- Changed to plain for UserDAO
('admin', 'adminpass', 'Admin', TRUE),        -- Added Admin User
('inactive_user', 'inactivepass', 'Cashier', FALSE); -- Changed to plain

-- Categories
INSERT INTO CATEGORY (CategoryName, Description) VALUES
('Beverages', 'Soft drinks, water, juices'),
('Snacks', 'Chips, candy, nuts'),
('Dairy', 'Milk, cheese, yogurt'),
('Grocery', 'Basic packaged goods');

-- Suppliers
INSERT INTO SUPPLIER (SupplierName, ContactPerson, ContactInfo, Address) VALUES
('Global Beverages Inc.', 'Sarah Chen', 'sarah.c@globalbev.com', '123 Drink St'),
('Snack Masters Ltd.', 'Tom Hardy', '555-SNACK', '456 Treat Ave'),
('Farm Fresh Dairy Co.', 'Maria Garcia', 'maria@farmfresh.co', '789 Milk Rd');

-- Shifts (Assume UserID 2 is manager, UserID 1 is cashier)
INSERT INTO SHIFT (StartTime, EndTime, Status, StartUserID, EndUserID, StartingFloat, EndingFloat, CashRemoved, CashSalesAmount, CardSalesAmount, CashDiscrepancy) VALUES
('2024-05-20 08:00:00', '2024-05-20 16:05:00', 'Reconciled', 2, 2, 100.00, 550.50, 200.00, 650.00, 300.00, 0.50), -- Example completed shift
('2024-05-21 07:55:00', NULL, 'Open', 1, NULL, 100.00, NULL, NULL, NULL, NULL, NULL); -- Current open shift

-- Employees (Link to Users)
INSERT INTO EMPLOYEE (FirstName, LastName, HireDate, Position, PayRate, UserID) VALUES
('Alice', 'Smith', '2023-01-15', 'Cashier', 15.50, 1),
('Bob', 'Jones', '2022-08-01', 'Store Manager', 25.00, 2),
('Charlie', 'Brown', '2023-05-10', 'Cashier', 15.00, 4); -- Linked to inactive user

-- Customers
INSERT INTO CUSTOMER (Name, ContactInfo, LoyaltyPoints) VALUES
('Jane Doe', 'jane.d@email.com', 150),
('John Smith', '555-1234', 25);

-- Promotions
INSERT INTO PROMOTION (Name, Description, DiscountType, DiscountValue, StartDate, EndDate, IsActive) VALUES
('Soda Discount', 'Save 10% on Beverages', 'Percentage', 10.00, '2024-05-01 00:00:00', '2024-05-31 23:59:59', TRUE),
('Snack Deal', '$1 off chips', 'FixedAmount', 1.00, '2024-05-15 00:00:00', NULL, TRUE); -- Ongoing

-- Products
INSERT INTO PRODUCT (SKU, Name, UnitPrice, QuantityInStock, CurrentCostPrice, ReorderLevel, CategoryID, SupplierID) VALUES
('BEV001', 'Cola Can 355ml', 1.50, 100, 0.50, 10, 1, 1),
('SNK001', 'Potato Chips Regular', 2.00, 5, 0.75, 5, 2, 2), -- Made low stock
('DRY001', 'Milk 1L', 3.50, 20, 1.50, 8, 3, 3),
('GRC001', 'Instant Noodles', 1.00, 75, 0.30, 15, 4, 2);

-- Product Specialization (Link to ProductID)
INSERT INTO NONPERISHABLE_PRODUCT (ProductID) VALUES (1), (2), (4); -- Cola, Chips, Noodles
INSERT INTO PERISHABLE_PRODUCT (ProductID, StorageTempRequirement) VALUES (3, 'Refrigerate 2-4 C'); -- Milk

-- Purchase Orders
INSERT INTO PURCHASE_ORDER (Status, SupplierID, PlacedByUserID, TotalCost, PODate, ExpectedDeliveryDate) VALUES
('Received', 1, 2, 23.04, '2024-05-10 10:00:00', '2024-05-15'), -- PO #1
('Ordered', 2, 2, 16.80, '2024-05-18 11:00:00', '2024-05-25');  -- PO #2

-- Purchase Order Items
INSERT INTO PURCHASE_ORDER_ITEM (PurchaseOrderID, ProductID, QuantityOrdered, CostPricePerUnit, QuantityReceived) VALUES
(1, 1, 48, 0.48, 48), -- PO 1, Cola
(2, 2, 24, 0.70, 0);  -- PO 2, Chips

-- Orders (Assume Order 1 in Shift 1, Order 2 in Shift 2)
INSERT INTO `ORDER` (TotalAmount, CustomerID, UserID, ShiftID, OrderStatus) VALUES
(3.50, 1, 1, 1, 'Completed'), -- Order #1
(5.50, NULL, 1, 2, 'Completed'); -- Order #2

-- Order Items (Link OrderID, ProductID)
INSERT INTO ORDER_ITEM (OrderID, ProductID, Quantity, UnitPriceAtSale, CostPriceAtSale, AppliedPromotionID) VALUES
(1, 1, 1, 1.50, 0.50, 1), -- Order 1, Cola, Promo Applied
(1, 2, 1, 2.00, 0.75, NULL), -- Order 1, Chips
(2, 3, 1, 3.50, 1.50, NULL), -- Order 2, Milk
(2, 4, 2, 1.00, 0.30, NULL); -- Order 2, Noodles (Qty 2)

-- Payments (Link OrderID)
INSERT INTO PAYMENT (Amount, OrderID) VALUES
(3.50, 1), -- Payment #1 for Order #1
(5.50, 2); -- Payment #2 for Order #2

-- Payment Specialization (Link PaymentID)
INSERT INTO CARD_PAYMENT (PaymentID, CardType, Last4Digits) VALUES (1, 'Visa', '1234'); -- Payment 1 via Card
INSERT INTO CASH_PAYMENT (PaymentID, AmountTendered, ChangeGiven) VALUES (2, 10.00, 4.50); -- Payment 2 via Cash

-- Stock Adjustments (Assume in Shift 1 by Manager UserID 2)
INSERT INTO STOCK_ADJUSTMENT (ProductID, UserID, ShiftID, QuantityChange, Reason) VALUES
(2, 2, 1, -1, 'Damage'); -- 1 bag of chips damaged

-- Sales Returns (Assume Item #2 from Order #1 is returned in Shift 2 by User 1)
-- Note: OrderItemID 2 corresponds to the chips in Order #1
INSERT INTO SALES_RETURN (OriginalOrderItemID, QuantityReturned, RefundAmount, ProcessedByUserID, ShiftID, Reason, RestockFlag) VALUES
(2, 1, 2.00, 1, 2, 'Customer changed mind', TRUE); -- Chips returned, can be restocked

-- Timesheets (Employee 1 working Shift 1 & 2, Employee 2 worked Shift 1)
INSERT INTO TIMESHEET (EmployeeID, ShiftID, TimeIn, TimeOut, HoursWorked) VALUES
(1, 1, '2024-05-20 08:00:00', '2024-05-20 16:00:00', 8.00),
(2, 1, '2024-05-20 07:50:00', '2024-05-20 16:10:00', 8.33),
(1, 2, '2024-05-21 07:55:00', NULL, NULL); -- Clocked in for current shift

-- Payroll Records (Example for Employee 1 for mid-May period)
INSERT INTO PAYROLL_RECORD (EmployeeID, PayPeriodStartDate, PayPeriodEndDate, TotalHours, GrossPay, Deductions, NetPay, PaymentDate) VALUES
(1, '2024-05-01', '2024-05-15', 80.00, 1240.00, 200.00, 1040.00, '2024-05-20');


-- =============================================================================
-- End of Script
-- =============================================================================
```

## 4. Project Structure

The project follows a standard Java package structure for clarity and modularity:

```
oop/
├── .classpath                  # Eclipse project classpath configuration
├── .project                    # Eclipse project definition file
├── src/
│   └── com/
│       └── smartcashpro/
│           ├── SmartCashProApp.java              # Main entry point of the application
│           ├── db/                             # Database Access Objects (DAOs)
│           │   ├── CustomerDAO.java
│           │   ├── DatabaseConnector.java      # Handles MySQL connection
│           │   ├── IProductDAO.java
│           │   ├── OrderDAO.java
│           │   ├── ProductDAO.java
│           │   ├── PurchaseOrderDAO.java
│           │   ├── ShiftDAO.java
│           │   ├── StockAdjustmentDAO.java
│           │   └── UserDAO.java
│           ├── model/                          # Data Model classes (POJOs)
│           │   ├── Customer.java
│           │   ├── NonPerishableProduct.java
│           │   ├── Order.java
│           │   ├── OrderItem.java
│           │   ├── PerishableProduct.java
│           │   ├── Product.java
│           │   ├── PurchaseOrder.java
│           │   ├── PurchaseOrderItem.java
│           │   ├── Shift.java
│           │   ├── Supplier.java
│           │   └── User.java
│           └── ui/                             # User Interface (Swing GUI) classes
│               ├── CreatePODialog.java
│               ├── InventoryPanel.java
│               ├── LoginDialog.java
│               ├── LoginPageCashierSystem.java # Main login window
│               ├── MainFrame.java              # Main application window
│               ├── OrderHistoryPanel.java
│               ├── POSPanel.java
│               ├── ShiftPanel.java
│               ├── UserPanel.java
│               └── ViewPOHistoryDialog.java
├── lib/                                # External libraries (e.g., MySQL Connector/J, JCalendar)
└── SmartCashPro.sql                    # Database schema and initial data (Crucial for setup!)
└── README.md
```

## 5. Key Features

SmartCash Pro offers a comprehensive set of features for small to medium-sized retail businesses:

*   **Secure User Authentication:** Login system with user roles (Cashier, Manager/Admin).
*   **Point of Sale (POS) Interface:**
    *   Add products to cart by SKU.
    *   Real-time calculation of total.
    *   Cash and Card payment processing (simulated).
    *   Customer search and association with sales.
    *   Return processing for individual order items.
*   **Inventory Management:**
    *   View all products with details (SKU, Name, Price, Cost, Quantity, Reorder Level, Type).
    *   Add new products (perishable/non-perishable).
    *   Edit existing product details.
    *   Stock adjustment for reasons like damage, spoilage, or count corrections.
    *   Highlighting products below reorder level.
    *   Purchase Order (PO) creation for stock replenishment.
    *   Receive stock against outstanding purchase orders.
    *   View Purchase Order history.
*   **Shift Management:**
    *   Start and End shifts with initial cash float.
    *   Track active shift duration.
    *   Generate end-of-shift reconciliation reports (cash sales, card sales, expected cash, discrepancy).
*   **Order History & Reporting:**
    *   View a comprehensive list of all past orders.
    *   Search/filter orders by date range, customer, and payment type.
    *   View detailed information for individual orders (items sold, prices, cashier).
    *   (Simulated) receipt printing.
*   **User Management (Manager/Admin Role):**
    *   View all system users.
    *   Add new users with specified roles and initial passwords.
    *   Edit existing user roles, activate/deactivate accounts, and reset passwords.
*   **Robust Database Integration:** Utilizes MySQL for reliable data storage, implementing DAO (Data Access Object) pattern for clean separation of concerns and transactional integrity where crucial (e.g., `OrderDAO`, `PurchaseOrderDAO`, `StockAdjustmentDAO`).

## 6. Team Contributions

Our "linear contribution" model ensured each team member brought their unique skills to the forefront, resulting in a cohesive and functional system:

*   **Farida Mohammadi:** Primarily responsible for the **Login GUI state** and its visual aspects. Her efforts ensured a clean, intuitive, and robust initial user experience for authentication.
*   **Ahmed Hassan Raza:** Dedicated his expertise to the **Inventory Panel**. This included designing the UI for product listings, implementation of add/edit product functionalities, and the integration of features like creating new Purchase Orders and handling stock adjustments.
*   **Muhammad Usman Amjad:** Focused heavily on the **Reports and Order History** aspects, ensuring that users could effectively query and view past transactions. He was also a key contributor to brainstorming and suggesting **ideas for improvement** throughout the project's development.
*   **Muhammad Musaab Ul Haq:** The central figure for **integration** and completing the overall product. Musaab took the individual modules from his teammates, built the overarching **main GUI (`MainFrame`)**, implemented the core **Point of Sale (POS) logic**, the comprehensive **Shift Management** system, and meticulously ensured all existing code (especially Farida's login and Ahmed Hassan's inventory) was seamlessly integrated into a single, working application.

## 7. Future Enhancements

While SmartCash Pro provides a solid foundation, several areas could be enhanced:

*   **Advanced Reporting:** More detailed sales analytics, profit/loss reports, popular item reports, and custom report generation.
*   **Barcode Scanning Integration:** Direct integration with barcode scanners for faster item entry at POS.
*   **Supplier Management:** Full CRUD (Create, Read, Update, Delete) operations for suppliers within the application.
*   **User Role Permissions:** More granular permissions for different user roles (e.g., restrict cashiers from certain inventory actions).
*   **Actual Receipt Printing:** Implement integration with thermal printers for physical receipts.
*   **Customer Loyalty Program Enhancements:** More features around loyalty points (e.g., redeeming points for discounts).
*   **Enhanced Error Handling & Logging:** More sophisticated error reporting and a dedicated logging system for easier debugging in production environments.
*   **Unit and Integration Tests:** Implement a comprehensive suite of tests to ensure stability and prevent regressions.
*   **Modern UI Framework:** Migrate from Swing to a more modern GUI framework (e.g., JavaFX) for a richer user experience.

## 8. Group Information

*   **Group Members:**
    *   Muhammad Musaab Ul Haq (501739)
    *   Ahmed Hassan Raza (511263)
    *   Muhammad Usman Amjad (516261)
    *   Farida Mohammadi (534534)
*   **Instructor:** Dr. Mehwish Awan
*   **Course:** CS212 Object Oriented Programming
*   **Degree Program:** Bachelors of Science in Computer Science
*   **Institution:** NUST, H12 Campus

## 9. License

This project is under MIT licence. There is nothing really new that this projects implement that others have not implemented. So if need be, you should clearly use some other software as a POS, not this project done by newbies in the field :)
