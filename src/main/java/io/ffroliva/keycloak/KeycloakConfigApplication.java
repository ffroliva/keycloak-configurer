package io.ffroliva.keycloak;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@ConfigurationPropertiesScan
@SpringBootApplication
public class KeycloakConfigApplication {

	public static void main(String[] args) {
		SpringApplication.run(KeycloakConfigApplication.class, args);
	}

}
