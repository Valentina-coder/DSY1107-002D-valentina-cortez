package com.example;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:5173") // Permite peticiones desde Vite
public class LogProducerController {

    private final RabbitTemplate rabbitTemplate;

    public LogProducerController(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @PostMapping("/log")
    public String sendLog(@RequestBody LogMessage logMessage) {
        // La routing key es el nivel del log (INFO, ERROR, etc.)
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EXCHANGE_NAME,
            logMessage.getLevel(),
            logMessage.getMessage()
        );
        return "Log enviado: " + logMessage.getMessage();
    }

    // DTO para el cuerpo de la petición
    public static class LogMessage {
        private String level;
        private String message;

        // Getters y Setters
        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}