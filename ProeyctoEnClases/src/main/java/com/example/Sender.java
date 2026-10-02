package com.example;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Productor (Sender) - Envía mensajes a la cola 'hello'
 */
@Component
public class Sender {
    private final RabbitTemplate template;

    public Sender(RabbitTemplate template) {
        this.template = template;
    }

    public void sendMessage(String message){
        try{
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

            String fullMessage = String.format(
                "[%s] %s",
                timestamp,
                message
            );

            System.out.println(" [x] Enviando mensaje: '" + fullMessage + "'");
        }   catch (Exception e) {
            System.err.println("Error al enviar el mensaje: " + 
            e.getMessage());
            e.printStackTrace();
            }


        }
    public void sendMessage(String exchange, String routingKey, String message) {
        try {
            template.convertAndSend(exchange, routingKey, message);
            System.out.println("[] Mensaje enviado a exchange '" + exchange + "' con routing key '" + routingKey + "': " + message);
        } catch (Exception e) {
            System.err.println("Error al enviar el mensaje: " + e.getMessage());
            e.printStackTrace();
        }

    }


}
