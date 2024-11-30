package com.bgs.cdc.traccar.listener;

import com.bgs.cdc.traccar.domain.TcDevice;
import com.bgs.cdc.traccar.repository.DeviceRepository;
import com.bgs.cdc.traccar.service.DeviceService;
import com.bgs.cdc.traccar.service.RabbitMqService;
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
import java.util.HashMap;
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
    private final RabbitMqService rabbitMqService;
    private final MqttService mqttService;

    HashMap<String,Object> deviceName = new HashMap<String,Object>();
    HashMap<String,Object> deviceSpeed = new HashMap<String,Object>();

    @Autowired
    private DeviceRepository deviceRepository;

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

    public TraccarListener(Configuration traccarConnectorConfiguration, RabbitMqService rabbitMqService, MqttService mqttService) {
        this.debeziumEngine = DebeziumEngine.create(ChangeEventFormat.of(Connect.class))
                .using(traccarConnectorConfiguration.asProperties())
                .notifying(this::handleChangeEvent)
                .build();
        this.rabbitMqService = rabbitMqService;
        this.mqttService = mqttService;
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

                        if (Objects.equals(table, "tc_positions")) {
                            var column = Optional.ofNullable(DebeziumRecordUtils.getRecordStructValue(sourceRecordChangeValue, "after"));
                            Long deviceid = Long.valueOf(column.map(s -> s.getInt32("deviceid")).orElse(0));
                            if (deviceid != 0) {
                                if (deviceName.get(String.valueOf(deviceid)) == null) {
                                    Optional<TcDevice> device = this.deviceRepository.findById(deviceid);
                                    device.ifPresent(tcDevice -> deviceName.put(String.valueOf(deviceid), tcDevice.getName().replace(" ", "")));
                                }
                                Float speed = column.map(s -> s.getFloat32("speed")).orElse(0f);
                                boolean isSend = false;
                                if (deviceSpeed.get(String.valueOf(deviceid)) == null) {
                                    deviceSpeed.put(String.valueOf(deviceid), Double.valueOf(speed));
                                    isSend = true;
                                } else if (!deviceSpeed.get(String.valueOf(deviceid)).equals(Double.valueOf(speed))) {
                                    deviceSpeed.put(String.valueOf(deviceid), Double.valueOf(speed));
                                    isSend = true;
                                }
                                if (isSend) {
                                    String queueName = "obu/speed/" + deviceName.get(String.valueOf(deviceid)).toString().replaceAll("\\s+", "");
//                                    this.mqttService.publishMessage(queueName, speed);
                                    this.rabbitMqService.sendMessage(queueName, speed);
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