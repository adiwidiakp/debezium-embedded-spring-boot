package com.cdc.traccar.listener;

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

import com.cdc.traccar.service.TcPositionService;
import com.cdc.traccar.sql.AbstractDebeziumSqlProvider;
import com.cdc.traccar.sql.DebeziumSqlProviderFactory;
import com.cdc.traccar.utils.DebeziumRecordUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Slf4j
@Component
public class TcPositionListener {

    private final Executor executor = Executors.newSingleThreadExecutor();

    private final EmbeddedEngine engine;

    private final TcPositionService tcPositionService;

    private TcPositionListener(Configuration tcPositionConnector, TcPositionService tcPositionService) {
        this.engine = EmbeddedEngine
                .create()
                .using(tcPositionConnector)
                .notifying(this::handleRecord).build();

        this.tcPositionService = tcPositionService;
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

//        // 处理数据DML
        Envelope.Operation operation = DebeziumRecordUtils.getOperation(payload);
        if (Objects.nonNull(operation)) {
            Struct key = (Struct) record.key();
            handleDML(key, payload, table, operation);
            return;
        }
//
//        // 处理结构DDL
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

        log.info("ddl语句 : {}", ddl);
        try {

        } catch (Exception e) {
            log.error("数据库操作DDL语句失败，", e);
        }
    }

    private void handleDML(Struct key, Struct payload, String table, Envelope.Operation operation) {
        AbstractDebeziumSqlProvider provider = DebeziumSqlProviderFactory.getProvider(operation);
        if (Objects.isNull(provider)) {
            log.error("没有找到sql处理器提供者.");
            return;
        }

        String sql = provider.getSql(key, payload, table);
        if (StringUtils.isBlank(sql)) {
            log.error("找不到sql.");
            return;
        }

        try {
            log.info("dml语句 : {}", sql);
            log.info("parm:{}" ,  provider.getSqlParameterMap());
            tcPositionService.maintainReadModel(provider.getSqlParameterMap(), operation);

        } catch (Exception e) {
            log.error("数据库DML操作失败，", e);
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
