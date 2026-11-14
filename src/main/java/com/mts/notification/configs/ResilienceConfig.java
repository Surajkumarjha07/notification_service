package com.mts.notification.configs;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.common.circuitbreaker.configuration.CircuitBreakerConfigCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.MailParseException;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.MailSendException;

import java.io.IOException;
import java.time.Duration;

@Configuration
public class ResilienceConfig {

    private static CircuitBreakerConfig.Builder baseline(CircuitBreakerConfig.Builder b) {
            return b.slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                    .slidingWindowSize(10)
                    .minimumNumberOfCalls(5)
                    .failureRateThreshold(50)
                    .waitDurationInOpenState(Duration.ofSeconds(30))
                    .permittedNumberOfCallsInHalfOpenState(3)
                    .automaticTransitionFromOpenToHalfOpenEnabled(true);
    }

    @Bean
    public CircuitBreakerConfigCustomizer emailProviderCustomizer() {
        return CircuitBreakerConfigCustomizer.of("emailProvider", b ->
            baseline(b)
                    .recordExceptions(MailSendException.class, IOException.class)
                    .ignoreExceptions(
                            IllegalArgumentException.class,
                            MailParseException.class,
                            MailPreparationException.class
                    )
        );
    }
}
