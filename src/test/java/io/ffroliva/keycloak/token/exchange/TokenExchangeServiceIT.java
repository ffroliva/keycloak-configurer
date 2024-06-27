package io.ffroliva.keycloak.token.exchange;

import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.OAuth2Constants;
import org.keycloak.TokenVerifier;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RealmsResource;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.text.MessageFormat;
import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@SpringBootTest
class TokenExchangeServiceIT extends AbstractKeycloakTestContainer {

    @Autowired
    KeycloakConfigurationProperties properties;

    @Autowired
    KeycloakService keycloakService;

    @Autowired
    KeycloakClient keycloakClient;

    Keycloak securityAdminConsoleKeycloak;

    @BeforeEach
    public void setup() {

        securityAdminConsoleKeycloak = KeycloakBuilder.builder()
                .realm("master")
                .clientId(TokenExchangeConstants.ADMIN_CONSOLE_CLIENT_ID)
                .username("admin")
                .password("admin")
                .grantType(OAuth2Constants.PASSWORD)
                .serverUrl(properties.getAuthUrl())
                .build();

    }

    @Test
    void shouldConfigureInternalToInternalTokenExchange() throws VerificationException {

        // given
        RealmName realmName = new RealmName(properties.getRealm()); // real name defined in application.properties
        OriginalClient originalClient = new OriginalClient("original");
        TargetClient targetClient = new TargetClient("target");
        RealmManagementClient realmManagementClient = new RealmManagementClient("realm-management");
        ClientSecret clientSecret = new ClientSecret("123456");


        var realmResource = KeycloakHelper.getRealmOrElseCreate(keycloak.realms(), realmName);

        // create original and target clients
        var originalClientRepresentation = KeycloakHelper.getClientResourceByClientIdOrElseCreate(keycloak, realmName, originalClient, clientSecret);
        var targetClientRepresentation = KeycloakHelper.getClientResourceByClientIdOrElseCreate(keycloak, realmName, targetClient, clientSecret);

        Assertions.assertThat(originalClientRepresentation).isNotNull();
        Assertions.assertThat(targetClientRepresentation).isNotNull();

        // create users
        properties.getUsers().forEach((username, user) -> KeycloakHelper.createUser(keycloak, realmName, user));

        // when
        TokenExchangeService tokenExchangeService = new TokenExchangeService(keycloak);
        tokenExchangeService.configureInternalToInternalTokenExchange(realmName, originalClient, targetClient);
        // then

        List<KeycloakConfigurationProperties.User> users = List.copyOf(properties.getUsers().values());
        var keycloak = KeycloakBuilder.builder()
                .serverUrl(keycloakContainer.getAuthServerUrl())
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

    @Test
    void shouldExchangeToken() throws VerificationException {

        // given
        RealmName realmName = new RealmName(properties.getRealm());
        OriginalClient originalClient = new OriginalClient("original");
        TargetClient targetClient = new TargetClient("target");
        ClientSecret clientSecret = new ClientSecret("123456");

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

    private RealmResource createRealm(RealmsResource realms) {
        final RealmRepresentation realmRepresentation = new RealmRepresentation();
        realmRepresentation.setRealm("riskcontrol");
        realmRepresentation.setEnabled(true);
        realms.create(realmRepresentation);

        return realms.realm(realmRepresentation.getRealm());
    }

    private Response createClient(Keycloak keycloak, RealmName realmName, String clientId, String clientSecret) {

        ClientRepresentation clientRepresentation = new ClientRepresentation();
        clientRepresentation.setClientId(clientId);
        clientRepresentation.setStandardFlowEnabled(true);
        clientRepresentation.setDirectAccessGrantsEnabled(true);
        clientRepresentation.setAuthorizationServicesEnabled(true);
        clientRepresentation.setServiceAccountsEnabled(true); // if not enable it throws 500 server error: java.lang.RuntimeException: Client does not have a service account.
        clientRepresentation.setSecret(clientSecret);
        clientRepresentation.setEnabled(true);

        try (var response = keycloak.realm(realmName.name()).clients().create(clientRepresentation)) {
            return response;
        } catch (Exception e) {
            log.error("Error creating client: [clientId: {}]", clientId);
            throw new RuntimeException(e.getMessage());
        }
    }

    private Response createUser(RealmName realmName, KeycloakConfigurationProperties.User user) {

        UserRepresentation userRepresentation = new UserRepresentation();

        userRepresentation.setEnabled(true);
        userRepresentation.setEmailVerified(true);
        userRepresentation.setEmail(user.getEmail());
        userRepresentation.setUsername(user.getUsername());
        userRepresentation.setFirstName(user.getFirstName());
        userRepresentation.setLastName(user.getLastName());
        userRepresentation.setCreatedTimestamp(OffsetDateTime.now().toEpochSecond());

        // Create password
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setTemporary(false);
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(user.getPassword());

        userRepresentation.setCredentials(List.of(credential));

        try (var response = keycloak.realm(realmName.name()).users().create(userRepresentation)) {
            return response;
        } catch (Exception ex) {
            throw new RuntimeException();
        }
    }


    private RealmRepresentation getRealmRepresentation(String realmName) {
        return keycloak.realms().findAll()
                .stream()
                .filter(r -> r.getRealm().equals(realmName)).findFirst()
                .orElseThrow(() -> new RuntimeException(MessageFormat.format("Reaml '{0} not found'", realmName)));
    }

    ClientResource getClientResource(RealmResource realmResource, String clientId) {
        var id = realmResource.clients().findByClientId(clientId).get(0).getId();
        return realmResource.clients().get(id);
    }

}