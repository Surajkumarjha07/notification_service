package com.mts.notification.configs;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitQueueConfig {
    public static final String EXCHANGE = "notification.exchange";

    public static final String REGISTRATION_QUEUE = "notification.registration.queue";
    public static final String INVITATION_QUEUE = "notification.invitation.queue";

    public static final String REGISTRATION_ROUTING_KEY = "notification.registration";
    public static final String INVITATION_ROUTING_KEY = "notification.invitation";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    // ===================== REGISTRATION QUEUE SETUP =====================

    @Bean
    public Queue registrationQueue() {
        return QueueBuilder.durable(REGISTRATION_QUEUE).build();
    }

    @Bean
    public Binding registrationBinding(@Qualifier("registrationQueue") Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(REGISTRATION_ROUTING_KEY);
    }

    // ===================== INVITATION QUEUE SETUP =====================

    @Bean
    public Queue invitationQueue() {
        return QueueBuilder.durable(INVITATION_QUEUE).build();
    }

    @Bean
    public Binding invitationBinding(@Qualifier("invitationQueue") Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(INVITATION_ROUTING_KEY);
    }
}
