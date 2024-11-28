package com.bgs.cdc.traccar.listener;

import com.bgs.cdc.traccar.domain.TcDevice;
import com.bgs.cdc.traccar.repository.DeviceRepository;
import com.bgs.cdc.traccar.service.RabbitMqService;
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
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final DebeziumEngine<RecordChangeEvent<SourceRecord>> debeziumEngine;
    private final RabbitMqService rabbitMqService;

    private final HashMap<Long, String> deviceName;
    private final HashMap<Long, Float> deviceSpeed;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private AmqpTemplate amqpTemplate;

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

    public TraccarListener(Configuration traccarConnectorConfiguration, RabbitMqService rabbitMqService) {
        this.debeziumEngine = DebeziumEngine.create(ChangeEventFormat.of(Connect.class))
                .using(traccarConnectorConfiguration.asProperties())
                .notifying(this::handleChangeEvent)
                .build();
        this.rabbitMqService = rabbitMqService;
        this.deviceName = new HashMap<>();
        this.deviceSpeed = new HashMap<>();
    }

    private void handleChangeEvent(RecordChangeEvent<SourceRecord> sourceRecordRecordChangeEvent) {
        SourceRecord sourceRecord = sourceRecordRecordChangeEvent.record();
        Struct sourceRecordChangeValue = (Struct) sourceRecord.value();

        if (sourceRecordChangeValue != null) {
            String table = Optional.ofNullable(DebeziumRecordUtils.getRecordStructValue(sourceRecordChangeValue, "source")).map(s -> s.getString("table")).orElse(null);
            try {
                Operation operation = Operation.forCode((String) sourceRecordChangeValue.get(OPERATION));
                if (Objects.nonNull(operation)) {
                    if (Envelope.Operation.CREATE == operation || Envelope.Operation.UPDATE == operation || Envelope.Operation.DELETE == operation) {
                        /*String record = operation == Operation.DELETE ? BEFORE : AFTER;
                        Struct struct = (Struct) sourceRecordChangeValue.get(record);
                        Map<String, Object> payload = struct.schema().fields().stream()
                                .map(Field::name)
                                .filter(fieldName -> struct.get(fieldName) != null)
                                .map(fieldName -> Pair.of(fieldName, struct.get(fieldName)))
                                .collect(toMap(Pair::getKey, Pair::getValue));*/
                        //log.trace("{} - {} => {}", table, operation.name(), payload);
                        //this.traccarService.replicateData(table, payload, operation);

                        if (Objects.equals(table, "tc_positions")) {
                            var column = Optional.ofNullable(DebeziumRecordUtils.getRecordStructValue(sourceRecordChangeValue, "after"));
                            Long deviceid = Long.valueOf(column.map(s -> s.getInt32("deviceid")).orElse(0));
                            if (deviceid != 0) {
                                if (!this.deviceName.containsKey(deviceid)) {
                                    Optional<TcDevice> device = this.deviceRepository.findById(deviceid);
                                    device.ifPresent(tcDevice -> this.deviceName.put(deviceid, tcDevice.getName()));
                                }
                                if (this.deviceName.containsKey(deviceid)) {
                                    Float speed = column.map(s -> s.getFloat32("speed")).orElse(0F);
                                    boolean isSend = false;
                                    if (!this.deviceSpeed.containsKey(deviceid)) {
                                        this.deviceSpeed.put(deviceid, speed);
                                        isSend = true;
                                    }
                                    if (!this.deviceSpeed.get(deviceid).equals(speed)) {
                                        this.deviceSpeed.put(deviceid, speed);
                                        isSend = true;
                                    }
                                    if (isSend) {
                                        String queueName = "obu/speed/" + this.deviceName.get(deviceid).replaceAll("\\s+", "");
                                        this.rabbitMqService.sendMessage(queueName, this.deviceSpeed.get(deviceid));
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (DataException e) {
                log.trace("SourceRecordChangeValue {} - {} => '{}'", table, e.getMessage(), sourceRecordChangeValue);
                return;
            }
        }
    }

}