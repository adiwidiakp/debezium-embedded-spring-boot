package com.bgs.cdc.traccar.listener;

import com.bgs.cdc.traccar.domain.TcDevice;
import com.bgs.cdc.traccar.service.TraccarService;
import com.bgs.cdc.traccar.utils.DebeziumRecordUtils;

import com.bgs.cdc.traccar.utils.DebeziumUtils;
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
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.HashMap;
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

    HashMap<Object,Object> deviceName = new HashMap<Object,Object>();
    HashMap<Object,Object> deviceSpeed = new HashMap<Object,Object>();

    @PostConstruct
    private void start() {
        this.executor.execute(debeziumEngine);
    }

    @PreDestroy
    private void stop() throws IOException {
        if (Objects.nonNull(this.debeziumEngine)) {
            this.debeziumEngine.close();
        }
    }

    public TraccarListener(Configuration traccarConnectorConfiguration, TraccarService traccarService) {
        this.debeziumEngine = DebeziumEngine.create(ChangeEventFormat.of(Connect.class))
            .using(traccarConnectorConfiguration.asProperties())
            .notifying(this::handleChangeEvent)
            .build();
        this.traccarService = traccarService;
    }

    private void handleChangeEvent(RecordChangeEvent<SourceRecord> sourceRecordRecordChangeEvent) {
        SourceRecord sourceRecord = sourceRecordRecordChangeEvent.record();
        Struct sourceRecordChangeValue= (Struct) sourceRecord.value();
        String table = Optional.ofNullable(DebeziumRecordUtils.getRecordStructValue(sourceRecordChangeValue, "source")).map(s->s.getString("table")).orElse(null);

        if (sourceRecordChangeValue != null) {
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
//                        log.trace("{} - {} => {}", table, operation.name(), payload);
                        if(table != null && table.equals("tc_positions") && payload.get("speed")!=null && payload.get("deviceid")!=null) {
                            Integer key = (Integer) payload.get("deviceid");
                            float speed = (float) payload.get("speed");
                            if( deviceName.get( key ) == null ){
                                String nameOfDevice = this.traccarService.getNameTcDeviceById(Long.valueOf(key));
                                deviceName.put(key,nameOfDevice);
                                log.info("added new key value deviceName {} - {}",key, nameOfDevice);
                            } else {
                                log.info("key value deviceName existed {} - {}",key, deviceName.get(key));
                                if( deviceSpeed.get(key) != null ) {
                                    deviceSpeed.replace(key, speed);
                                    log.info("replaced key value deviceSpeed {} - {}",key, speed);
                                } else {
                                    deviceSpeed.put(key,speed);
                                    log.info("added new key value deviceSpeed {} - {}",key, speed);
                                }
                            }
                        }
                        log.info("payload {}", DebeziumUtils.toJson(payload));
                        // TODO: proses replicate ke db_target diganti dengan nulis ke queue
//                        this.traccarService.replicateData(table, payload, operation);
                        return;
                    }
                }
            } catch (DataException e){
                log.trace("SourceRecordChangeValue {} - {} => '{}'",  table, e.getMessage(), sourceRecordChangeValue);
            }
        }
    }

}