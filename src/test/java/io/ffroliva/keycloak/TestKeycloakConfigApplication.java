package io.ffroliva.keycloak;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration(proxyBeanMethods = false)
public class TestKeycloakConfigApplication {

	public static void main(String[] args) {
		SpringApplication.from(KeycloakConfigApplication::main).with(TestKeycloakConfigApplication.class).run(args);
	}

}
