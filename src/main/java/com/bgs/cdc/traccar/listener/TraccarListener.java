package com.bgs.cdc.traccar.listener;

import io.debezium.config.Configuration;
import io.debezium.data.Envelope;
import io.debezium.embedded.EmbeddedEngine;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.json.JsonConverter;
import org.apache.kafka.connect.source.SourceRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bgs.cdc.traccar.service.TraccarService;
import com.bgs.cdc.traccar.sql.AbstractDebeziumSqlProvider;
import com.bgs.cdc.traccar.sql.DebeziumSqlProviderFactory;
import com.bgs.cdc.traccar.utils.DebeziumRecordUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Slf4j
@Component
public class TraccarListener {

    private final Executor executor = Executors.newSingleThreadExecutor();

    private final EmbeddedEngine engine;

    private final TraccarService traccarService;

    private TraccarListener(Configuration tcPositionConnector, TraccarService traccarService) {
        this.engine = EmbeddedEngine
                .create()
                .using(tcPositionConnector)
                .notifying(this::handleRecord).build();

        this.traccarService = traccarService;
    }

    @PostConstruct
    private void start() {
        this.executor.execute(engine);
    }

    @PreDestroy
    private void stop() {
        if (this.engine != null) {
            this.engine.stop();
        }
    }

    private void handleRecord(SourceRecord record) {
        logRecord(record);

        Struct payload = (Struct) record.value();
        if (Objects.isNull(payload)) {
            return;
        }
        String table = Optional.ofNullable(DebeziumRecordUtils.getRecordStructValue(payload, "source"))
                .map(s->s.getString("table")).orElse(null);

        Envelope.Operation operation = DebeziumRecordUtils.getOperation(payload);
        if (Objects.nonNull(operation)) {
            Struct key = (Struct) record.key();
            handleDML(key, payload, table, operation);
            return;
        }
        String ddl = getDDL(payload);
        if (StringUtils.isNotBlank(ddl)) {
            handleDDL(ddl);
        }
    }

    private String getDDL(Struct payload) {
        String ddl = DebeziumRecordUtils.getDDL(payload);
        log.info("ddl:{}" ,ddl);
        if (StringUtils.isBlank(ddl)) {
            return null;
        }
        String db = DebeziumRecordUtils.getDatabaseName(payload);
        if (StringUtils.isBlank(db)) {
            log.info("db:{}" ,db);
        }
        ddl = ddl.replace(db + ".", "");
        ddl = ddl.replace("`" + db + "`.", "");
        return ddl;
    }

    private void handleDDL(String ddl) {

        log.info("DDL Statement : {}", ddl);
        try {

        } catch (Exception e) {
            log.error("Database operation DDL statement failed", e);
        }
    }

    private void handleDML(Struct key, Struct payload, String table, Envelope.Operation operation) {
        AbstractDebeziumSqlProvider provider = DebeziumSqlProviderFactory.getProvider(operation);
        if (Objects.isNull(provider)) {
            log.error("No sql processor provider found.");
            return;
        }

        String sql = provider.getSql(key, payload, table);
        if (StringUtils.isBlank(sql)) {
            log.error("SQL not found.");
            return;
        }

        try {
            log.info("DML statement: {}", sql);
            log.info("parm:{}" ,  provider.getSqlParameterMap());
            traccarService.maintainReadModel(provider.getSqlParameterMap(), table, operation);

        } catch (Exception e) {
            log.error("Database DML operation failed.", e);
        }
    }


    @Autowired
    private JsonConverter keyConverter;

    @Autowired
    private JsonConverter valueConverter;

    private void logRecord(SourceRecord record) {
        final byte[] payload = valueConverter.fromConnectData("dummy", record.valueSchema(), record.value());
        final byte[] key = keyConverter.fromConnectData("dummy", record.keySchema(), record.key());
        log.info("Publishing Topic --> {}", record.topic());
        log.info("Key --> {}", new String(key));
        log.info("Payload --> {}", new String(payload));
    }
}
