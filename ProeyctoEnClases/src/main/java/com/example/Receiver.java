package com.example;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;

@Component
public class Receiver {

    @RabbitListener(queues = "hello")
    public void receiveMessage(String message) {
        try {
            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("HH:mm:ss"));

            System.out.println("[" + timestamp + "] Recibido: '" + message + "'");
        } catch (Exception e) {
            System.err.println("Error al recibir el mensaje: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @RabbitListener(queues = "Hello")
    public void receiveMessage(String message, Message rawMessage, Channel channel) throws Exception {
        try {
            System.out.println(" [x] Recibido '" + message + "'");
            System.out.println("-Content Type: " + rawMessage.getMessageProperties().getContentType());
            System.out.println("-Timestamp: " + rawMessage.getMessageProperties().getTimestamp());

            channel.basicAck(rawMessage.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            System.err.println("Error al recibir el mensaje: " + e.getMessage());
            channel.basicNack(rawMessage.getMessageProperties().getDeliveryTag(), false, true);
            throw e;
        }
    }
}
