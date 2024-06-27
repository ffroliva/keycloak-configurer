package io.ffroliva.keycloak.token.exchange;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.keycloak.OAuth2Constants;
import org.keycloak.TokenVerifier;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.RealmsResource;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.AccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

@Slf4j
@Testcontainers
@SpringBootTest
class TokenExchangeServiceTest {

    @Autowired
    KeycloakConfigurationProperties properties;

    @Autowired
    KeycloakService keycloakService;

    @Autowired
    KeycloakClient keycloakClient;

    static Keycloak keycloakAdminClient;
    static Keycloak keycloakSecurityAdminConsole;


    @Container
    static KeycloakContainer keycloakContainer = new KeycloakContainer("quay.io/keycloak/keycloak:25.0.1")
            .withContextPath("/auth")
            .withAdminPassword("admin")
            .withAdminPassword("admin")
            .withFeaturesEnabled("token-exchange", "admin-fine-grained-authz")
            .withExposedPorts(8080);

    static {
        keycloakContainer.start();
        keycloakAdminClient = keycloakContainer.getKeycloakAdminClient();
        keycloakSecurityAdminConsole = KeycloakBuilder.builder()
                .realm("master")
                .clientId("security-admin-console")
                .username("admin")
                .password("admin")
                .grantType(OAuth2Constants.PASSWORD)
                .serverUrl(keycloakContainer.getAuthServerUrl())
                .build();
    }


    @Test
    void shouldConfigureInternalToInternalTokenExchange() throws VerificationException {
        // given
        RealmName realmName = new RealmName(properties.getRealm());
        OriginalClient originalClient = new OriginalClient("original");
        TargetClient targetClient = new TargetClient("target");
        RealmManagementClient realmManagementClient = new RealmManagementClient("realm-management");
        ClientSecret clientSecret = new ClientSecret("123456");

        final RealmsResource realms = keycloakAdminClient.realms();
        // create realm
        var realmResource = KeycloakHelper.getRealmOrElseCreate(realms, realmName);

        // create original and target clients
        var originalClientRepresentation = KeycloakHelper.getClientResourceByClientIdOrElseCreate(keycloakAdminClient, realmName, originalClient, clientSecret);
        var targetClientRepresentation = KeycloakHelper.getClientResourceByClientIdOrElseCreate(keycloakAdminClient, realmName, targetClient, clientSecret);

        Assertions.assertThat(originalClientRepresentation).isNotNull();
        Assertions.assertThat(originalClientRepresentation).isNotNull();

        // create users
        properties.getUsers().forEach((username, user) -> KeycloakHelper.createUser(keycloakAdminClient, realmName, user));

        // when
        TokenExchangeService tokenExchangeService = new TokenExchangeService(keycloakAdminClient, keycloakClient);
        tokenExchangeService.configureInternalToInternalTokenExchange(realmName, originalClient, targetClient);
        // then

        List<KeycloakConfigurationProperties.User> users = List.copyOf(properties.getUsers().values());
        var keycloak = KeycloakBuilder.builder()
                .serverUrl(properties.getAuthUrl())
                .realm(realmName.name())
                .password(users.get(0).getPassword())
                .grantType("password")
                .username(users.get(0).getUsername())
                .clientId(originalClient.name())
                .clientSecret(clientSecret.secret())
                .build();

        var originalSubjectToken = keycloak.tokenManager().getAccessToken().getToken();
        log.info("Original Subject Token : {}", originalSubjectToken);

        var tokenExchangeRequest = new TokenExchangeRequest(
                originalSubjectToken,
                originalClient.name(),
                clientSecret.secret(),
                targetClient.clientId(),
                realmName.name());
        String tokenExchange = keycloakService.tokenExchange(tokenExchangeRequest);

        AccessToken token = TokenVerifier.create(tokenExchange, AccessToken.class).getToken();
        TokenVerifier.AudienceCheck audienceCheck = new TokenVerifier.AudienceCheck(targetClient.clientId());
        Assertions.assertThat(audienceCheck.test(token)).isTrue();

    }


    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("keycloak.authUrl",
                () -> keycloakContainer.getAuthServerUrl());
        registry.add("spring.datasource.username", () -> "postgres");
        registry.add("spring.datasource.password", () -> "pass");
    }


}