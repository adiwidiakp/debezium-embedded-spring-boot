package com.cdc.traccar.config;

import org.apache.kafka.connect.json.JsonConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TcEventsConnectorConfig {

    @Value("${tcEvents.datasource.host}")
    private String tcEventsDBHost;

    @Value("${tcEvents.datasource.databasename}")
    private String tcEventsDBName;

    @Value("${tcEvents.datasource.port}")
    private String tcEventsDBPort;

    @Value("${tcEvents.datasource.username}")
    private String tcEventsDBUserName;

    @Value("${tcEvents.datasource.password}")
    private String tcEventsDBPassword;

    private String TC_POSITION_TABLE_NAME = "tc_events";

    @Bean
    public io.debezium.config.Configuration tcEventsConnector() {
        return io.debezium.config.Configuration.create()
                .with("connector.class", "io.debezium.connector.mysql.MySqlConnector")
                .with("offset.storage",  "org.apache.kafka.connect.storage.FileOffsetBackingStore")
                .with("offset.storage.file.filename", "traccar-offset.dat")
                .with("offset.flush.interval.ms", 600)
                .with("database.serverTimezone", "UTC")
                .with("heartbeat.interval.ms", 1000)
                .with("autoReconnect", true)
//				.with("plugin.name", "pgoutput")
                .with("name", "tcEvents-mysql-connector")
                .with("database.server.name", tcEventsDBHost+"-"+tcEventsDBName)
                .with("database.server.id", 184054)
                .with("database.hostname", tcEventsDBHost)
                .with("database.port", tcEventsDBPort)
                .with("database.user", tcEventsDBUserName)
                .with("database.password", tcEventsDBPassword)
                .with("database.whitelist", tcEventsDBName)
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
