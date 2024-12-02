package com.bgs.cdc.traccar.listener;

//import com.bgs.cdc.traccar.App;
import com.bgs.cdc.traccar.service.TraccarService;
import com.bgs.cdc.traccar.utils.DebeziumRecordUtils;

import io.debezium.config.Configuration;
import io.debezium.data.Envelope;
import io.debezium.embedded.Connect;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.RecordChangeEvent;
import io.debezium.engine.format.ChangeEventFormat;

import lombok.extern.slf4j.Slf4j;

import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.errors.DataException;
import org.apache.kafka.connect.source.SourceRecord;
//import org.springframework.boot.SpringApplication;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.Optional;

import static java.util.stream.Collectors.toMap;
import static io.debezium.data.Envelope.FieldName.*;
import static io.debezium.data.Envelope.Operation;

import org.apache.commons.lang3.tuple.Pair;


@Slf4j
@Component
public class TraccarListener {

    private final Executor executor = Executors.newSingleThreadExecutor();    
    private final TraccarService traccarService;
    private final DebeziumEngine<RecordChangeEvent<SourceRecord>> debeziumEngine;

    @PostConstruct
    private void start() {
        log.info("Starting debezium engine.");
        this.executor.execute(debeziumEngine);
    }

    @PreDestroy
    private void stop() throws IOException {
        if (Objects.nonNull(this.debeziumEngine)) {
            log.warn("Stopping debezium engine.");
            this.debeziumEngine.close();
            //log.warn("Stopping Spring boot.");
            //SpringApplication.exit(App.ctx, () -> 1);
        }
    }

    public TraccarListener(Configuration traccarConnectorConfiguration, TraccarService traccarService) {
        this.traccarService = traccarService;
        this.debeziumEngine = DebeziumEngine.create(ChangeEventFormat.of(Connect.class))
            .using(traccarConnectorConfiguration.asProperties())
            .notifying(this::handleChangeEvent)
            .build();
    }

    private void handleChangeEvent(RecordChangeEvent<SourceRecord> sourceRecordRecordChangeEvent) {
        SourceRecord sourceRecord = sourceRecordRecordChangeEvent.record();
        Struct sourceRecordChangeValue= (Struct) sourceRecord.value();

        if (sourceRecordChangeValue != null) {
            String table = Optional.ofNullable(DebeziumRecordUtils.getRecordStructValue(sourceRecordChangeValue, "source")).map(s->s.getString("table")).orElse(null);

            try {
                Operation operation = Operation.forCode((String) sourceRecordChangeValue.get(OPERATION));

                if (Envelope.Operation.CREATE == operation || Envelope.Operation.UPDATE == operation || Envelope.Operation.DELETE == operation) {
                    if (Objects.nonNull(operation)) {
                        String record = operation == Operation.DELETE ? BEFORE : AFTER; 

                        Struct struct = (Struct) sourceRecordChangeValue.get(record);
                        Map<String, Object> payload = struct.schema().fields().stream()
                            .map(Field::name)
                            .filter(fieldName -> struct.get(fieldName) != null)
                            .map(fieldName -> Pair.of(fieldName, struct.get(fieldName)))
                            .collect(toMap(Pair::getKey, Pair::getValue));
                        //log.trace("{} - {} => {}", table, operation.name(), payload);        
                        this.traccarService.replicateData(table, payload, operation);
                        return;
                    }
                }
            } catch (DataException de) {
                log.warn("DataException - sourceRecordChangeValue {} - {} => '{}'",  table, de.getMessage(), sourceRecordChangeValue);
                //SpringApplication.exit(App.ctx, () -> 1);
            } catch (Exception e) {
                log.error("Exception - sourceRecordChangeValue {} - {} => '{}'",  table, e.getMessage(), sourceRecordChangeValue);
                //SpringApplication.exit(App.ctx, () -> 1);
            }
        }
    }

}