package com.bgs.cdc.traccar.listener;

import com.bgs.cdc.traccar.domain.TcDevice;
import com.bgs.cdc.traccar.repository.DeviceRepository;
import com.bgs.cdc.traccar.service.DeviceService;
import com.bgs.cdc.traccar.service.MqttService;
import com.bgs.cdc.traccar.utils.DebeziumRecordUtils;

import io.debezium.config.Configuration;
import io.debezium.data.Envelope;
import io.debezium.embedded.Connect;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.RecordChangeEvent;
import io.debezium.engine.format.ChangeEventFormat;

import lombok.extern.slf4j.Slf4j;

import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.errors.DataException;
import org.apache.kafka.connect.source.SourceRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.Optional;

import static io.debezium.data.Envelope.FieldName.*;
import static io.debezium.data.Envelope.Operation;


@Slf4j
@Component
public class TraccarListener {

    private final Executor executor = Executors.newSingleThreadExecutor();
    private final DebeziumEngine<RecordChangeEvent<SourceRecord>> debeziumEngine;
    private final MqttService mqttService;

    @Autowired
    private DeviceRepository deviceRepository;

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

    public TraccarListener(Configuration traccarConnectorConfiguration,
                           DeviceService deviceService, RedisTemplate<String, Object> redisTemplate, MqttService mqttService) {
        this.debeziumEngine = DebeziumEngine.create(ChangeEventFormat.of(Connect.class))
                .using(traccarConnectorConfiguration.asProperties())
                .notifying(this::handleChangeEvent)
                .build();
        this.deviceService = deviceService;
        this.deviceService = new DeviceService(redisTemplate);
        this.mqttService = mqttService;
    }

    private void handleChangeEvent(RecordChangeEvent<SourceRecord> sourceRecordRecordChangeEvent) {
        //log.debug("handleChangeEvent {}", sourceRecordRecordChangeEvent);
        SourceRecord sourceRecord = sourceRecordRecordChangeEvent.record();
        Struct sourceRecordChangeValue = (Struct) sourceRecord.value();

        if (sourceRecordChangeValue != null) {
            String table = Optional.ofNullable(DebeziumRecordUtils.getRecordStructValue(sourceRecordChangeValue, "source")).map(s -> s.getString("table")).orElse(null);
            try {
                Operation operation = Operation.forCode((String) sourceRecordChangeValue.get(OPERATION));
                if (Objects.nonNull(operation)) {
                    if (Envelope.Operation.CREATE == operation || Envelope.Operation.UPDATE == operation || Envelope.Operation.DELETE == operation) {

                        if (Objects.equals(table, "tc_positions")) {
                            var column = Optional.ofNullable(DebeziumRecordUtils.getRecordStructValue(sourceRecordChangeValue, "after"));
                            Long deviceid = Long.valueOf(column.map(s -> s.getInt32("deviceid")).orElse(0));
                            if (deviceid != 0) {
                                if (deviceService.getDeviceName(String.valueOf(deviceid)) == null) {
                                    Optional<TcDevice> device = this.deviceRepository.findById(deviceid);
                                    device.ifPresent(tcDevice -> deviceService.saveDeviceName(String.valueOf(deviceid), tcDevice.getName().replace(" ", "")));
                                }
                                Float speed = column.map(s -> s.getFloat32("speed")).orElse(0f);
                                boolean isSend = false;
                                if (deviceService.getDeviceSpeed(String.valueOf(deviceid)) == null) {
                                    deviceService.saveDeviceSpeed(String.valueOf(deviceid), Double.valueOf(speed));
                                    isSend = true;
                                } else if (!deviceService.getDeviceSpeed(String.valueOf(deviceid)).equals(Double.valueOf(speed))) {
                                    deviceService.saveDeviceSpeed(String.valueOf(deviceid), Double.valueOf(speed));
                                    isSend = true;
                                }
                                if (isSend) {
                                    String queueName = "obu/speed/" + deviceService.getDeviceName(String.valueOf(deviceid)).replaceAll("\\s+", "");
                                    //log.debug("publishMessage {} - {}", queueName, speed);
                                    this.mqttService.publishMessage(queueName, speed);
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