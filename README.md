# Getting Started
This project aims to show how to use Debezium as database monitor to help performing tasks

## REFERENCES

[Debezium](https://debezium.io/) is an open source distributed platform for __Change Data Capture__ (CDC).
[MariaDB](https://www.mariadb.com/) database used in this project.
[RabbitMQ](https://www.rabbitmq.com/) is an open source message broker.

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
```

### Running RabbitMQ
```
docker run -d --name rabbitmq -p 5672:5672 rabbitmq:3.13.7-alpine
```

### Running REDIS
```
docker run -d --name redis -p 6379:6379 redis:6.2.7-alpine
```

### Testing Insert
```
INSERT INTO `tc_positions` SELECT * FROM db_traccar.tc_positions limit 1;

INSERT INTO `tc_events` SELECT * FROM db_traccar.tc_events limit 1;

```