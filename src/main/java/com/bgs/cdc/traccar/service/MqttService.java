package com.bgs.cdc.traccar.service;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import com.bgs.cdc.traccar.config.MqttConfig;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

@Service
@Slf4j
public class MqttService {

    private IMqttClient client;

    public MqttService() {
        this.client = MqttConfig.getInstance();
    }

    public void publishMessage(String topic, Object message) {
        try {
            MqttMessage mqttMessage = new MqttMessage(message.toString().getBytes());
            mqttMessage.setQos(1);
            mqttMessage.setRetained(true);
            //log.debug("client.publish {} - {}", topic, message);
            client.publish(topic, mqttMessage);
        } catch (MqttException e) {
            log.error("publishMessage error", e);
        }
    }

}
