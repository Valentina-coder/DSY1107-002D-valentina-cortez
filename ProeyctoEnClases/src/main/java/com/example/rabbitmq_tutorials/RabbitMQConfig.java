package com.example.rabbitmq_tutorials;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {/**
* Define la cola "hello"
*
* Esta cola es idempotente:
* - Si no existe, la crea
* - Si ya existe, la reutiliza
*
* durable=false: se borra si RabbitMQ se reinicia
* (En producción, normalmente usarías durable=true)
*/
@Bean
public Queue helloQueue() {
return new Queue("hello", false);
}

@Bean
public Queue manualAckQueue() {
return new Queue("Hello", false);
}
}