package com.bgs.cdc.traccar.service;

import com.bgs.cdc.traccar.model.RabbitMqModel;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.stereotype.Service;

@Service
public class RabbitMqService {
    private final RabbitAdmin rabbitAdmin;
    private final RabbitTemplate rabbitTemplate;
    private final RabbitMqModel rabbitMqModel;
    private final RabbitListenerEndpointRegistry rabbitListenerEndpointRegistry;

    public RabbitMqService(RabbitAdmin rabbitAdmin, RabbitTemplate rabbitTemplate, RabbitMqModel rabbitMqModel, RabbitListenerEndpointRegistry rabbitListenerEndpointRegistry) {
        this.rabbitAdmin = rabbitAdmin;
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitMqModel = rabbitMqModel;
        this.rabbitListenerEndpointRegistry = rabbitListenerEndpointRegistry;
    }

    public void createQueue(String queueName) {
        Queue queue = new Queue(queueName, true, false, false);
        Binding binding = new Binding(
                queueName,
                Binding.DestinationType.QUEUE,
                rabbitMqModel.getExchange(),
                queueName,
                null
        );
        rabbitAdmin.declareQueue(queue);
        rabbitAdmin.declareBinding(binding);
        if (!this.checkQueueExist(rabbitMqModel.getExchange(), queueName)) {
            var msgListener = this.getMessageListener(rabbitMqModel.getExchange());
            if (msgListener != null) {
                msgListener.addQueueNames(queueName);
            }
        }
    }

    public void sendMessage(String queueName, Object message) {
        this.createQueue(queueName);
        this.rabbitTemplate.convertAndSend(this.rabbitMqModel.getExchange(), queueName, message);
    }

    public Boolean checkQueueExist(String exchangeName, String queueName) {
        try {
            var msgListener = this.getMessageListener(exchangeName);
            if (msgListener != null) {
                String[] queueNames = msgListener.getQueueNames();
                if (queueNames.length > 0) {
                    for (String name : queueNames) {
                        if (name.equals(queueName)) {
                            return Boolean.TRUE;
                        }
                    }
                }
            }
            return Boolean.FALSE;
        } catch (Exception e) {
            return Boolean.FALSE;
        }
    }

    private AbstractMessageListenerContainer getMessageListener(String exchangeName) {
        return ((AbstractMessageListenerContainer) this.rabbitListenerEndpointRegistry.getListenerContainer(exchangeName));
    }
}
