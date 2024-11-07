package com.cdc.traccar.config;

import org.apache.kafka.connect.json.JsonConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class TcPositionConnectorConfig {

    @Value("${tcPosition.datasource.host}")
    private String tcPositionDBHost;

    @Value("${tcPosition.datasource.databasename}")
    private String tcPositionDBName;

    @Value("${tcPosition.datasource.port}")
    private String tcPositionDBPort;

    @Value("${tcPosition.datasource.username}")
    private String tcPositionDBUserName;

    @Value("${tcPosition.datasource.password}")
    private String tcPositionDBPassword;

    private String TC_POSITION_TABLE_NAME = "tc_position";

    @Bean
    public io.debezium.config.Configuration tcPositionConnector() {
        return io.debezium.config.Configuration.create()
                .with("connector.class", "io.debezium.connector.mysql.MySqlConnector")
                .with("offset.storage",  "org.apache.kafka.connect.storage.FileOffsetBackingStore")
                .with("offset.storage.file.filename", "traccar-offset.dat")
                .with("offset.flush.interval.ms", 600)
                .with("database.serverTimezone", "UTC")
                .with("heartbeat.interval.ms", 1000)
                .with("autoReconnect", true)
//				.with("plugin.name", "pgoutput")
                .with("name", "tcPosition-mysql-connector")
                .with("database.server.name", tcPositionDBHost+"-"+tcPositionDBName)
                .with("database.server.id", 184054)
                .with("database.hostname", tcPositionDBHost)
                .with("database.port", tcPositionDBPort)
                .with("database.user", tcPositionDBUserName)
                .with("database.password", tcPositionDBPassword)
                .with("database.whitelist", tcPositionDBName)
                .with("database.history",
                        "io.debezium.relational.history.FileDatabaseHistory")
                .with("database.history.file.filename",
                        "dbhistory.dat")
                .with("message.key.columns", "test.tc_positions:id,servertime")
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
