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