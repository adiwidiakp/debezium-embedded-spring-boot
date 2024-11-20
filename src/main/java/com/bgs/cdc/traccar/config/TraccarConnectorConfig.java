package com.bgs.cdc.traccar.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
//import org.springframework.core.env.Environment;

@Configuration
public class TraccarConnectorConfig {
    @Value("${traccar.name}")
    private String traccarName;

    @Value("${traccar.datasource.host}")
    private String traccarDBHost;

    @Value("${traccar.datasource.databasename}")
    private String traccarDBName;

    @Value("${traccar.datasource.port}")
    private String traccarDBPort;

    @Value("${traccar.datasource.username}")
    private String traccarDBUserName;

    @Value("${traccar.datasource.password}")
    private String traccarDBPassword;

    @Value("${traccar.database.server.id}")
    private String serverId;

    @Value("${traccar.offset.file}")
    private String offsetFile;

    @Value("${traccar.history.file}")
    private String historyFile;

    @Value("${traccar.database.serverTimezone}")
    private String serverTimezone;

    @Value("${traccar.table.include.list}")
    private String tableIncludeList;

    @Value("${traccar.message.key.columns}")
    private String keyColumns;

    @Bean
    public io.debezium.config.Configuration traccarConnector() throws IOException {
        return io.debezium.config.Configuration.create()
                .with("name", traccarName)
                .with("topic.prefix", "traccar")
                .with("connector.class", "io.debezium.connector.mysql.MySqlConnector")
                .with("tasks.max", "1")
                .with("offset.storage", "org.apache.kafka.connect.storage.FileOffsetBackingStore")
                .with("offset.storage.file.filename", offsetFile)
                .with("offset.flush.interval.ms", 500)
                .with("include.schema.changes", false)
                .with("schema.history.internal", "io.debezium.storage.file.history.FileSchemaHistory")
                .with("schema.history.internal.file.filename", historyFile)
                //.with("database.serverTimezone", serverTimezone)
                .with("heartbeat.interval.ms", 15000)
                //.with("errors.retry.delay.initial.ms", 300)
                //.with("errors.retry.delay.max.ms", 10000)
                //.with("database.server.name", traccarDBHost+"-"+traccarDBName)
                .with("database.server.id", serverId)
                .with("database.hostname", traccarDBHost)
                .with("database.port", traccarDBPort)
                .with("database.user", traccarDBUserName)
                .with("database.password", traccarDBPassword)
                .with("database.whitelist", traccarDBName)
                .with("table.include.list", tableIncludeList)
                .with("message.key.columns", keyColumns)
                .build();
    }
}
