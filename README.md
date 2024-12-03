# Getting Started
This project aims to show how to use Debezium as database monitor to help performing tasks

## REFERENCES

[Debezium](https://debezium.io/) is an open source distributed platform for __Change Data Capture__ (CDC).
[MariaDB](https://www.mariadb.com/) database used in this project.
[RabbitMQ](https://www.rabbitmq.com/) is an open source message broker.
[Redis](https://www.redis.io/) is an open source cache management.

<br>

## OVERVIEW
In this project is showed how to implement Debezium programmatically and monitor the tables tc_positions and sending speed to mqtt to related device's queue.

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
docker run -d --name mariadb1  -p 3306:3306 -e MARIADB_ROOT_PASSWORD=root  -e MARIADB_REPLICATION_USER=data-cdc  -e MARIADB_REPLICATION_PASSWORD=SzQA9fG0Wkn6Ogxz2Iei97N4  mariadb:10.5 --server-id=1 --log-bin --log-basename=mariadb1 --binlog-format=row --performance-schema=ON


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

```

### Running RabbitMQ
```
docker run -d --rm --name rabbitmq -p 5672:5672 -p 1883:1883 rabbitmq:3.13-alpine
```

### Running REDIS
```
docker run -d --rm --name redis -p 6379:6379 redis:6.2.7-alpine
```

### Testing Insert
```
INSERT INTO `tc_devices` VALUES (52,'RBT 4219','863719065180706','2024-12-02 17:08:08',1042605597,3,'{\"web.reportColor\":\"#FF0000\"}','8115116423','FMC650',NULL,'truck','\0','offline','null',NULL,'','2024-12-02 17:04:35',56779871.9,'\0',NULL,0,'\0',NULL);

INSERT INTO `tc_positions` VALUES (336884,'teltonika',52,'2023-10-31 15:59:10','2023-10-31 15:58:24','2023-10-31 15:58:24','\0',-3.7015451,115.5620886,0,0,0,NULL,'{\"priority\":0,\"sat\":0,\"event\":0,\"io22\":0,\"io71\":4,\"motion\":false,\"rssi\":4,\"io200\":2,\"ignition\":false,\"battery\":8.402000000000001,\"io68\":0,\"pdop\":0.0,\"hdop\":0.0,\"power\":0.0,\"io24\":0,\"distance\":0.0,\"totalDistance\":471069.61}',0,'null','null');

INSERT INTO `tc_positions` SELECT * FROM db_traccar.tc_positions limit 1;

INSERT INTO `tc_events` SELECT * FROM db_traccar.tc_events limit 1;

```