# Getting Started
This project aims to show how to use Debezium as database monitor to help performing tasks

## REFERENCES

[Debezium](https://debezium.io/) is an open source distributed platform for __Change Data Capture__ (CDC).
[MariaDB](https://www.mariadb.com/) database used in this project

<br>

## OVERVIEW
In this project is showed how to implement Debezium programmatically and monitor the tables tc_positions, tc_devices, tc_events, and tc_geofences. 

<br>

## ATENTTION
Each database has your on rules to enable/use CDC, check for your database. For MariadDB:

| Property | Description                                                                                                                                                                                            |
| :------- |:-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| server-id | __The value for the server-id must be unique__ for each server and replication client in the MySQL cluster. During MySQL connector set up, Debezium assigns a unique server ID to the connector.       |
| log_bin | The value of log_bin is the base name of the sequence of binlog files.                                                                                                                                 |
| binlog_format | The binlog-format must be set to ROW or row.                                                                                                                                                           |
| binlog_row_image | The binlog_row_image must be set to FULL or full.                                                                                                                                                      |
| expire_logs_days | This is the number of days for automatic binlog file removal. The default is 0, which means no automatic removal. Set the value to match the needs of your environment. See MySQL purges binlog files. |

For more MySQL connector details check here [link](https://debezium.io/documentation/reference/stable/connectors/mysql.html#:~:text=Descriptions%20of%20MySQL%20binlog%20configuration%20properties).

## TESTING 
### Source

```
docker run -d --name mariadb1  -p 3306:3306 -e MARIADB_ROOT_PASSWORD=root -e MARIADB_REPLICATION_USER=data-cdc -e MARIADB_REPLICATION_PASSWORD=SzQA9fG0Wkn6Ogxz2Iei97N4  mariadb:10.5 --server-id=1 --log-bin --log-basename=mariadb1 --binlog-format=row --performance-schema=ON


GRANT SELECT, RELOAD, SHOW DATABASES, REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'data-cdc'@'%';

FLUSH PRIVILEGES;

CREATE DATABASE source_tc;

USE source_tc;

CREATE TABLE `tc_positions` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `protocol` varchar(128) DEFAULT NULL,
  `deviceid` int(11) NOT NULL,
  `servertime` datetime NOT NULL DEFAULT current_timestamp(),
  `devicetime` timestamp NULL DEFAULT NULL,
  `fixtime` timestamp NULL DEFAULT NULL,
  `valid` bit(1) NOT NULL,
  `latitude` double NOT NULL,
  `longitude` double NOT NULL,
  `altitude` float NOT NULL,
  `speed` float NOT NULL,
  `course` float NOT NULL,
  `address` varchar(512) DEFAULT NULL,
  `attributes` varchar(4000) DEFAULT NULL,
  `accuracy` double NOT NULL DEFAULT 0,
  `network` varchar(4000) DEFAULT NULL,
  `geofenceids` varchar(128) DEFAULT NULL,
  PRIMARY KEY (`id`,`servertime`,`deviceid`),
  KEY `position_deviceid_fixtime` (`deviceid`,`fixtime`),
  KEY `idx_tc_positions_deviceid` (`deviceid`),
  KEY `idx_tc_positions_devicetime` (`devicetime`),
  KEY `idx_tc_positions_deviceid_devicetime` (`deviceid`,`devicetime`)
) ENGINE=InnoDB;

  CREATE TABLE `tc_events` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `type` varchar(128) NOT NULL,
  `eventtime` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `deviceid` int(11) DEFAULT NULL,
  `positionid` int(11) DEFAULT NULL,
  `geofenceid` int(11) DEFAULT NULL,
  `attributes` varchar(4000) DEFAULT NULL,
  `maintenanceid` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`,`eventtime`),
  KEY `event_deviceid_servertime` (`deviceid`,`eventtime`),
  KEY `tc_events_idx` (`eventtime`)
) ENGINE=InnoDB;

CREATE TABLE `tc_devices` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(128) NOT NULL,
  `uniqueid` varchar(128) NOT NULL,
  `lastupdate` timestamp NULL DEFAULT NULL,
  `positionid` int(11) DEFAULT NULL,
  `groupid` int(11) DEFAULT NULL,
  `attributes` varchar(4000) DEFAULT NULL,
  `phone` varchar(128) DEFAULT NULL,
  `model` varchar(128) DEFAULT NULL,
  `contact` varchar(512) DEFAULT NULL,
  `category` varchar(128) DEFAULT NULL,
  `disabled` bit(1) DEFAULT b'0',
  `status` char(8) DEFAULT NULL,
  `geofenceids` varchar(128) DEFAULT NULL,
  `expirationtime` timestamp NULL DEFAULT NULL,
  `motionstate` bit(1) DEFAULT b'0',
  `motiontime` timestamp NULL DEFAULT NULL,
  `motiondistance` double DEFAULT 0,
  `overspeedstate` bit(1) DEFAULT b'0',
  `overspeedtime` timestamp NULL DEFAULT NULL,
  `overspeedgeofenceid` int(11) DEFAULT 0,
  `motionstreak` bit(1) DEFAULT b'0',
  `calendarid` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniqueid` (`uniqueid`),
  KEY `fk_devices_groupid` (`groupid`),
  KEY `idx_devices_uniqueid` (`uniqueid`),
  KEY `fk_devices_calendarid` (`calendarid`)
) ENGINE=InnoDB;

 CREATE TABLE `tc_geofences` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(128) NOT NULL,
  `description` varchar(128) DEFAULT NULL,
  `area` varchar(4096) NOT NULL,
  `attributes` varchar(4000) DEFAULT NULL,
  `calendarid` int(11) DEFAULT NULL,
  `geotype` varchar(255) DEFAULT 'ROAD',
  `group_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;
```

### Target
```
docker run -d --name mariadb2  -p 3307:3306 -e MARIADB_ROOT_PASSWORD=root -e MARIADB_USER=adminpanel_tracking -e MARIADB_PASSWORD=Minergo@2022! mariadb:11.5

CREATE DATABASE target_tc;

USE target_tc;

CREATE TABLE `tc_positions` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `protocol` varchar(128) DEFAULT NULL,
  `deviceid` int(11) NOT NULL,
  `servertime` datetime NOT NULL DEFAULT current_timestamp(),
  `devicetime` timestamp NULL DEFAULT NULL,
  `fixtime` timestamp NULL DEFAULT NULL,
  `valid` bit(1) NOT NULL,
  `latitude` double NOT NULL,
  `longitude` double NOT NULL,
  `altitude` float NOT NULL,
  `speed` float NOT NULL,
  `course` float NOT NULL,
  `address` varchar(512) DEFAULT NULL,
  `attributes` varchar(4000) DEFAULT NULL,
  `accuracy` double NOT NULL DEFAULT 0,
  `network` varchar(4000) DEFAULT NULL,
  `geofenceids` varchar(128) DEFAULT NULL,
  PRIMARY KEY (`id`,`servertime`,`deviceid`),
  KEY `position_deviceid_fixtime` (`deviceid`,`fixtime`),
  KEY `idx_tc_positions_deviceid` (`deviceid`),
  KEY `idx_tc_positions_devicetime` (`devicetime`),
  KEY `idx_tc_positions_deviceid_devicetime` (`deviceid`,`devicetime`)
) ENGINE=InnoDB;

CREATE TABLE `tc_events` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `type` varchar(128) NOT NULL,
  `eventtime` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `deviceid` int(11) DEFAULT NULL,
  `positionid` int(11) DEFAULT NULL,
  `geofenceid` int(11) DEFAULT NULL,
  `attributes` varchar(4000) DEFAULT NULL,
  `maintenanceid` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`,`eventtime`),
  KEY `event_deviceid_servertime` (`deviceid`,`eventtime`),
  KEY `tc_events_idx` (`eventtime`)
) ENGINE=InnoDB;

CREATE TABLE `tc_devices` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(128) NOT NULL,
  `uniqueid` varchar(128) NOT NULL,
  `lastupdate` timestamp NULL DEFAULT NULL,
  `positionid` int(11) DEFAULT NULL,
  `groupid` int(11) DEFAULT NULL,
  `attributes` varchar(4000) DEFAULT NULL,
  `phone` varchar(128) DEFAULT NULL,
  `model` varchar(128) DEFAULT NULL,
  `contact` varchar(512) DEFAULT NULL,
  `category` varchar(128) DEFAULT NULL,
  `disabled` bit(1) DEFAULT b'0',
  `status` char(8) DEFAULT NULL,
  `geofenceids` varchar(128) DEFAULT NULL,
  `expirationtime` timestamp NULL DEFAULT NULL,
  `motionstate` bit(1) DEFAULT b'0',
  `motiontime` timestamp NULL DEFAULT NULL,
  `motiondistance` double DEFAULT 0,
  `overspeedstate` bit(1) DEFAULT b'0',
  `overspeedtime` timestamp NULL DEFAULT NULL,
  `overspeedgeofenceid` int(11) DEFAULT 0,
  `motionstreak` bit(1) DEFAULT b'0',
  `calendarid` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniqueid` (`uniqueid`),
  KEY `fk_devices_groupid` (`groupid`),
  KEY `idx_devices_uniqueid` (`uniqueid`),
  KEY `fk_devices_calendarid` (`calendarid`)
) ENGINE=InnoDB;

 CREATE TABLE `tc_geofences` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(128) NOT NULL,
  `description` varchar(128) DEFAULT NULL,
  `area` varchar(4096) NOT NULL,
  `attributes` varchar(4000) DEFAULT NULL,
  `calendarid` int(11) DEFAULT NULL,
  `geotype` varchar(255) DEFAULT 'ROAD',
  `group_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;


 CREATE TABLE `tc_events_ritase` (
  `id` int(11) NOT NULL DEFAULT 0,
  `eventtime` datetime DEFAULT NULL,
  `deviceid` int(11) DEFAULT NULL,
  `positionid` int(11) DEFAULT NULL,
  `geofenceid` int(11) DEFAULT NULL
) ENGINE=InnoDB;
```

### Testing Insert
```
INSERT INTO `tc_positions` SELECT * FROM db_traccar.tc_positions limit 1;

INSERT INTO `tc_events` SELECT * FROM db_traccar.tc_events limit 1;

```