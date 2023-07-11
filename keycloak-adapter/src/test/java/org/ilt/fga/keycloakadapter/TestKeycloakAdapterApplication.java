package org.ilt.fga.keycloakadapter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration(proxyBeanMethods = false)
class TestKeycloakAdapterApplication {

	public static void main(String[] args) {
		SpringApplication.from(KeycloakAdapterApplication::main).with(TestKeycloakAdapterApplication.class).run(args);
	}

}
