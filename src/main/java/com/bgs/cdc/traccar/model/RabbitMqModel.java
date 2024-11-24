package com.bgs.cdc.traccar.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Getter
@Setter
@Configuration
public class RabbitMqModel {
    @org.springframework.beans.factory.annotation.Value("${props.rabbitmq.exchange}")
    private String exchange;
    @org.springframework.beans.factory.annotation.Value("${props.rabbitmq.queue}")
    private String queue;
    @org.springframework.beans.factory.annotation.Value("${props.rabbitmq.routing-key}")
    private String routingKey;
}
