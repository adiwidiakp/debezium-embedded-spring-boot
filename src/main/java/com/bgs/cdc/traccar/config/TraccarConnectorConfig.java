package com.bgs.cdc.traccar.config;

import org.apache.kafka.connect.json.JsonConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


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
    private int serverId;

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
    public io.debezium.config.Configuration tcPositionConnector() {
        return io.debezium.config.Configuration.create()
                .with("connector.class", "io.debezium.connector.mysql.MySqlConnector")
                .with("offset.storage",  "org.apache.kafka.connect.storage.FileOffsetBackingStore")
                .with("offset.storage.file.filename", offsetFile)
                .with("offset.flush.interval.ms", 500)
                .with("database.serverTimezone", serverTimezone)
                .with("heartbeat.interval.ms", 1000)
                .with("autoReconnect", true)
                .with("name", traccarName)
                .with("database.server.name", traccarDBHost+"-"+traccarDBName)
                .with("database.server.id", serverId)
                .with("database.hostname", traccarDBHost)
                .with("database.port", traccarDBPort)
                .with("database.user", traccarDBUserName)
                .with("database.password", traccarDBPassword)
                .with("database.whitelist", traccarDBName)
                .with("table.include.list", tableIncludeList)
                .with("include.schema.changes", false)
                .with("database.history", "io.debezium.relational.history.FileDatabaseHistory")
                .with("database.history.file.filename", historyFile)
                //.with("message.key.columns", traccarDBName+".tc_positions:id,servertime;"+traccarDBName+".tc_events:id,eventtime;"+traccarDBName+".tc_devices:id;"+traccarDBName+".tc_geofences:id")
                .with("message.key.columns", keyColumns)
                .build();
    }

    @Bean
    public JsonConverter keyConverter(io.debezium.config.Configuration embeddedConfig) {
        JsonConverter converter = new JsonConverter();
        converter.configure(embeddedConfig.asMap(), true);
        return converter;
    }

    @Bean
    public JsonConverter valueConverter(io.debezium.config.Configuration embeddedConfig) {
        JsonConverter converter = new JsonConverter();
        converter.configure(embeddedConfig.asMap(), false);
        return converter;
    }

}
