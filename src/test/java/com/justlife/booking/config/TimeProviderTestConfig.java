package com.justlife.booking.config;

import com.justlife.booking.time.TimeProvider;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TimeProviderTestConfig {

    @Bean
    @Primary
    TimeProvider timeProvider() {
        return Mockito.mock(TimeProvider.class);
    }
}