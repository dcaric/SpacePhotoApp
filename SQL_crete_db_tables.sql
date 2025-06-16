-- 1. Create the database first
CREATE DATABASE Photo;
GO

-- 2. Switch context to the new database
USE Photo;
GO

-- 3. Create the required table
CREATE TABLE NewsRelease (
    id INT PRIMARY KEY IDENTITY,
    title NVARCHAR(255),
    description NVARCHAR(MAX),
    link NVARCHAR(512),
    guid NVARCHAR(255),
    pubDate NVARCHAR(100),
    imageUrl NVARCHAR(512),
    localImagePath NVARCHAR(512),
    CONSTRAINT UQ_NewsGuid UNIQUE (guid)
);
GO

-- 4. Create login at the server level (if not already exists)
CREATE LOGIN photo_user WITH PASSWORD = 'photo123!';
GO

-- 5. Create user for the login inside the Photo database
CREATE USER photo_user FOR LOGIN photo_user;
GO

-- 6. Grant database role (e.g., db_owner) to the user
EXEC sp_addrolemember 'db_owner', 'photo_user';
GO

-- 7. (Optional) Reset the password for the login if needed (not strictly required here)
-- ALTER LOGIN photo_user WITH PASSWORD = 'photo123!';
-- GO

-- 8. Create the stored procedure
CREATE PROCEDURE InsertNewsRelease
    @title NVARCHAR(MAX),
    @description NVARCHAR(MAX),
    @link NVARCHAR(500),
    @guid NVARCHAR(255),
    @pubDate NVARCHAR(100),
    @imageUrl NVARCHAR(1000),
    @localImagePath NVARCHAR(500)
AS
BEGIN
    INSERT INTO NewsRelease (title, description, link, guid, pubDate, imageUrl, localImagePath)
    VALUES (@title, @description, @link, @guid, @pubDate, @imageUrl, @localImagePath);
END;
GO

-- 9. Test (Optional)
SELECT * FROM NewsRelease;

-- 10. Alter table with categoryALTER TABLE NewsRelease
ALTER TABLE NewsRelease
ADD category NVARCHAR(50);

-- 11. update InsertNewsRelease procedure
DROP PROCEDURE IF EXISTS InsertNewsRelease;
GO

CREATE PROCEDURE InsertNewsRelease
    @title NVARCHAR(MAX),
    @description NVARCHAR(MAX),
    @link NVARCHAR(500),
    @guid NVARCHAR(255),
    @pubDate NVARCHAR(100),
    @imageUrl NVARCHAR(1000),
    @localImagePath NVARCHAR(500),
    @category NVARCHAR(50)  -- ✅ new parameter
AS
BEGIN
    INSERT INTO NewsRelease (title, description, link, guid, pubDate, imageUrl, localImagePath, category)
    VALUES (@title, @description, @link, @guid, @pubDate, @imageUrl, @localImagePath, @category);
END;
GO

-- 12. fix db after inserting category
UPDATE NewsRelease
SET category = 'UNKNOWN'
WHERE category IS NULL;