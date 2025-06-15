create database Photo

use Photo


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
select * from NewsRelease

drop table NewsRelease


CREATE LOGIN photo_user WITH PASSWORD = 'photo123!';
CREATE USER photo_user FOR LOGIN photo_user;
USE Photo;
EXEC sp_addrolemember 'db_owner', 'photo_user';
ALTER LOGIN photo_user WITH PASSWORD = 'photo123!';

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
END