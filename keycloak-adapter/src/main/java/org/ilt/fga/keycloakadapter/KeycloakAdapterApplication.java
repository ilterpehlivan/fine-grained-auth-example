package org.ilt.fga.keycloakadapter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;

@SpringBootApplication
public class KeycloakAdapterApplication {

	public static void main(String[] args) {
		SpringApplication.run(KeycloakAdapterApplication.class, args);
	}

	@Bean
	public StringJsonMessageConverter jsonConverter() {
		return new StringJsonMessageConverter();
	}

}
