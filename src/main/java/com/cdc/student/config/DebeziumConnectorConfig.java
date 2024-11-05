package com.cdc.student.config;

import org.apache.kafka.connect.json.JsonConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class DebeziumConnectorConfig {

    
    @Value("${source.datasource.host}")
    private String studentDBHost;

    @Value("${source.datasource.databasename}")
    private String studentDBName;

    @Value("${source.datasource.port}")
    private String studentDBPort;

    @Value("${source.datasource.username}")
    private String studentDBUserName;

    @Value("${source.datasource.password}")
    private String studentDBPassword;

    private String STUDENT_TABLE_NAME = "student";

    /**
     * Student database connector.
     *
     * @return Configuration.
     */
    @Bean
    public io.debezium.config.Configuration studentConnector() {
        return io.debezium.config.Configuration.create()
                .with("connector.class", "io.debezium.connector.mysql.MySqlConnector")
                .with("offset.storage",  "org.apache.kafka.connect.storage.FileOffsetBackingStore")
                .with("offset.storage.file.filename", "student-offset.dat")
                .with("offset.flush.interval.ms", 600)
				.with("database.serverTimezone", "UTC")
                .with("heartbeat.interval.ms", 1000)
                .with("autoReconnect", true)
//				.with("plugin.name", "pgoutput")
                .with("name", "student-mysql-connector")
                .with("database.server.name", studentDBHost+"-"+studentDBName)
                .with("database.server.id", 184054)
                .with("database.hostname", studentDBHost)
                .with("database.port", studentDBPort)
                .with("database.user", studentDBUserName)
                .with("database.password", studentDBPassword)
//                .with("database.dbname", studentDBName)
                .with("database.whitelist", studentDBName)
//                .with("table.whitelist", STUDENT_TABLE_NAME)
                .with("database.history",
                        "io.debezium.relational.history.FileDatabaseHistory")
                  .with("database.history.file.filename",
                        "dbhistory.dat")
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
