/* *********************************
 *         创建表collection         *
************************************/
DROP TABLE if exists collection;
CREATE TABLE collection
(
-- ID自动递增
dataId int NOT NULL primary key auto_increment,
username varchar(50),
Time TIMESTAMP,
Longitude double(11,7),
Latitude double(11,7),
Light int,
Noise int
)ENGINE=INNODB;


/* *********************************
 *         创建表POI         *
************************************/
DROP TABLE if exists POI;
CREATE TABLE POI
(
poi char(8) NOT NULL primary key,
min_level int,
max_level int,
Longitude double(11,7),
Latitude double(11,7)

)ENGINE=INNODB;


/* *********************************
 *         创建表multimedia         *
************************************/
DROP TABLE if exists multimedia;
CREATE TABLE multimedia
(
-- ID自动递增
dataId int NOT NULL primary key auto_increment,
username varchar(50),
Time TIMESTAMP,
Longitude double(11,7),
Latitude double(11,7),
fpm int,
path varchar(100),
content MediumBlob,
poi	char(8),
CONSTRAINT multimedia_POI FOREIGN KEY (poi) REFERENCES POI(poi)

)ENGINE=INNODB;


