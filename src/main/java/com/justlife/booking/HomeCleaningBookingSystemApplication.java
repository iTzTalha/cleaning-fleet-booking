package com.justlife.booking;

import com.justlife.booking.config.SchedulingConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableConfigurationProperties(SchedulingConfig.class)
@EnableJpaAuditing
@SpringBootApplication
public class HomeCleaningBookingSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(HomeCleaningBookingSystemApplication.class, args);
	}

}
