package com.mts.notification.configs;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.messaging.handler.annotation.support.MessageHandlerMethodFactory;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitConfig implements RabbitListenerConfigurer {
    @Bean
    public ConnectionFactory connectionFactory() throws NoSuchAlgorithmException, KeyManagementException {
        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setHost("puffin.rmq2.cloudamqp.com");
        factory.setPort(5671);
        factory.setUsername("hwfrgjdo");
        factory.setPassword("JRfAaIVATjJumNgua1dg6wl_2HKMmk8m");
        factory.setVirtualHost("hwfrgjdo");

        factory.getRabbitConnectionFactory().useSslProtocol();

        RetryPolicy retryPolicy = RetryPolicy.builder()
                .maxRetries(2)
                .delay(Duration.ofSeconds(2))
                .multiplier(2)
                .build();

        new RetryTemplate(retryPolicy).invoke(() -> {
            factory.createConnection().close();
            System.out.println("Successfully connected to RabbitMQ!");
        });

        return factory;
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        JacksonJsonMessageConverter converter = new JacksonJsonMessageConverter();

        DefaultClassMapper classMapper = new DefaultClassMapper();
        Map<String, Class<?>> idClassMapping = new HashMap<>();
        idClassMapping.put(
                "com.mts.tenant.DTOs.notificationDTOs.RegistrationPayload",
                com.mts.notification.DTOs.RegistrationPayload.class
        );
        idClassMapping.put(
                "com.mts.tenant.DTOs.notificationDTOs.InvitationPayload",
                com.mts.notification.DTOs.InvitationPayload.class
        );
        classMapper.setIdClassMapping(idClassMapping);
        classMapper.setTrustedPackages("*");

        converter.setClassMapper(classMapper);
        return converter;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setConcurrentConsumers(5);
        factory.setMaxConcurrentConsumers(10);
        factory.setPrefetchCount(20);
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);
        factory.setAdviceChain(
                RetryInterceptorBuilder.stateless()
                        .maxRetries(3)
                        .backOffOptions(1000, 2, 10000)
                        .recoverer(messageRecoverer())
                        .build()
        );

        return factory;
    }

    @Bean
    public MessageRecoverer messageRecoverer() {
        return (message, error) -> {
            if (error.getCause() instanceof MethodArgumentNotValidException validationEx) {
                System.err.println("Validation failed: " + validationEx.getMessage());
            } else {
                System.err.println("Listener error: " + error.getMessage());
            }
        };
    }

    @Override
    public void configureRabbitListeners(RabbitListenerEndpointRegistrar registrar) {
        registrar.setMessageHandlerMethodFactory(messageHandlerMethodFactory());
    }

    @Bean
    public MessageHandlerMethodFactory messageHandlerMethodFactory() {
        DefaultMessageHandlerMethodFactory factory = new DefaultMessageHandlerMethodFactory();
        factory.setValidator(validator());

        return factory;
    }

    @Bean
    public Validator validator() {
        return new LocalValidatorFactoryBean();
    }
}
