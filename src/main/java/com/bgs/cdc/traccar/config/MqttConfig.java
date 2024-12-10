package com.bgs.cdc.traccar.config;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Configuration
public class MqttConfig {
    private static String serverURI;
    private static String clientId;
    private static String user;
    private static String password;
    private static IMqttClient instance;

    public MqttConfig(
            @Value("${mqtt.server.url}") String serverURI,
            @Value("${mqtt.server.clientId}") String clientId,
            @Value("${mqtt.server.user}") String user,
            @Value("${mqtt.server.password}") String password
    ) {
        MqttConfig.serverURI = serverURI;
        MqttConfig.clientId = clientId;
        MqttConfig.user = user;
        MqttConfig.password = password;
    }

    @Bean
    public static IMqttClient getInstance() {
        try {
            if (instance == null) {
                instance = new MqttClient(serverURI, clientId);
            }

            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(user);
            options.setPassword(password.toCharArray());
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);

            if (!instance.isConnected()) {
                instance.connect(options);
            }
        } catch (MqttException e) {
            log.error(clientId + " connect error", e);
        }

        return instance;
    }
}