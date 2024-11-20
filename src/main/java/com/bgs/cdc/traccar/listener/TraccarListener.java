package com.bgs.cdc.traccar.listener;

import com.bgs.cdc.traccar.domain.TcDevice;
import com.bgs.cdc.traccar.repository.DeviceRepository;
import com.bgs.cdc.traccar.service.DeviceService;
import com.bgs.cdc.traccar.service.RedisService;
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
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
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

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private DeviceService deviceService;

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

    public TraccarListener(Configuration traccarConnectorConfiguration, TraccarService traccarService,
                           DeviceService deviceService, RedisTemplate<String, Object> redisTemplate) {
        this.debeziumEngine = DebeziumEngine.create(ChangeEventFormat.of(Connect.class))
                .using(traccarConnectorConfiguration.asProperties())
                .notifying(this::handleChangeEvent)
                .build();
        this.traccarService = traccarService;
        this.deviceService = deviceService;
        this.deviceService = new DeviceService(redisTemplate);
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
                        String record = operation == Operation.DELETE ? BEFORE : AFTER;
                        Struct struct = (Struct) sourceRecordChangeValue.get(record);
                        Map<String, Object> payload = struct.schema().fields().stream()
                                .map(Field::name)
                                .filter(fieldName -> struct.get(fieldName) != null)
                                .map(fieldName -> Pair.of(fieldName, struct.get(fieldName)))
                                .collect(toMap(Pair::getKey, Pair::getValue));
                        //log.trace("{} - {} => {}", table, operation.name(), payload);
                        this.traccarService.replicateData(table, payload, operation);

                        if (Objects.equals(table, "tc_positions")) {
                            var column = Optional.ofNullable(DebeziumRecordUtils.getRecordStructValue(sourceRecordChangeValue, "after"));
                            Long deviceid = Long.valueOf(column.map(s -> s.getInt32("deviceid")).orElse(0));
                            Float speed = column.map(s -> s.getFloat32("speed")).orElse(0F);
                            if (deviceid != 0) {
                                if (deviceService.getDeviceName(String.valueOf(deviceid))==null) {
                                    Optional<TcDevice> device = this.deviceRepository.findById(deviceid);
                                    if (device.isPresent()) {
//                                        this.deviceName.put(deviceid, device.get().getName());
                                        deviceService.saveDeviceName(String.valueOf(deviceid), device.get().getName());
                                    }
                                }
                                if (deviceService.getDeviceName(String.valueOf(deviceid))!=null) {
                                    if (deviceService.getDeviceSpeed(String.valueOf(deviceid))!=null) {
                                        deviceService.saveDeviceSpeed(String.valueOf(deviceid), speed);
                                    }
                                    if(deviceService.getDeviceSpeed(String.valueOf(deviceid))!=null) {
                                        if (!deviceService.getDeviceSpeed(String.valueOf(deviceid)).equals(speed)) {
                                            deviceService.saveDeviceSpeed(String.valueOf(deviceid), speed);
                                        }
                                        this.amqpTemplate.convertAndSend("cdc-traccar-rabbit.exchange", "cdc-traccar-rabbit.routingkey", deviceService.getDeviceSpeed(String.valueOf(deviceid)));
                                    }

                                }
                            }
                        }
                    }
                }
            } catch (DataException e) {
                log.trace("SourceRecordChangeValue {} - {} => '{}'", table, e.getMessage(), sourceRecordChangeValue);
            }
        }
    }

}