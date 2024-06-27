package io.ffroliva.keycloak.token.exchange;

import lombok.AllArgsConstructor;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmsResource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@AllArgsConstructor
@Configuration
public class KeycloakConfig {

    private final KeycloakConfigurationProperties properties;

    @Bean
    @ConditionalOnMissingBean
    public Keycloak keycloak() {
        return KeycloakBuilder.builder()
                .serverUrl(properties.getAuthUrl())
                .username("admin")
                .password("admin")
                .grantType(OAuth2Constants.PASSWORD)
                .clientId("admin-cli")
                .realm("master")

                .build();
    }

    @Bean
    public Keycloak securityAdminConsoleKeycloak() {
        return KeycloakBuilder.builder()
                .serverUrl(properties.getAuthUrl())
                .username("admin")
                .password("admin")
                .grantType(OAuth2Constants.PASSWORD)
                .clientId("security-admin-console")
                .realm("master")

                .build();
    }

    @Bean
    RealmsResource realmsResource(Keycloak keycloak) {
        return keycloak.realms();
    }

    @Bean
    @ConditionalOnMissingBean
    public RestClient restClient(KeycloakConfigurationProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.getAuthUrl())
                .build();
    }

    @Bean
    public KeycloakClient keycloakClient(RestClient restClient) {
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build();
        return factory.createClient(KeycloakClient.class);
    }
}
