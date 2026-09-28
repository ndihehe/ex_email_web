CREATE DATABASE MurachDB;
GO

USE MurachDB;
GO

CREATE TABLE Users (
    UserID INT IDENTITY(1,1) PRIMARY KEY,
    Email NVARCHAR(100) NOT NULL UNIQUE,
    FirstName NVARCHAR(50) NOT NULL,
    LastName NVARCHAR(50) NOT NULL
);
GO

INSERT INTO Users (Email, FirstName, LastName)
VALUES 
('jsmith@gmail.com', 'John', 'Smith'),
('andrea@yahoo.com', 'Andrea', 'Boehm'),
('joelmurach@yahoo.com', 'Joel', 'Murach');
GO
