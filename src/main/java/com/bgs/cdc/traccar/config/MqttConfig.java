package com.bgs.cdc.traccar.config;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
public class MqttConfig {
    private static String serverURI;
    private static String clientId;
    private static IMqttClient instance;

    @Autowired
    public MqttConfig(@Value("${mqttv.server.url}") String serverURI, @Value("${mqttv.server.clientId}") String clientId) {
        MqttConfig.serverURI = serverURI;
        MqttConfig.clientId = clientId;
    }

    @Bean
    public static IMqttClient getInstance() {
        try {
            if (instance == null) {
                instance = new MqttClient(serverURI, clientId);
            }

            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);

            if (!instance.isConnected()) {
                instance.connect(options);
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }

        return instance;
    }
}