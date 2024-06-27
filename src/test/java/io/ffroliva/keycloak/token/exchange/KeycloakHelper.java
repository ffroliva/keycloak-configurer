package io.ffroliva.keycloak.token.exchange;

import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RealmsResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.text.MessageFormat;
import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
public class KeycloakHelper {

    public static RealmResource getRealmOrElseCreate(RealmsResource realms, RealmName realmName) {
        RealmRepresentation realmRepresentation = realms.findAll().stream()
                .filter(r -> r.getRealm().equals(realmName.name()))
                .findFirst()
                .orElseGet(() -> createRealm(realms, realmName));
        return realms.realm(realmRepresentation.getRealm());

    }

    public static RealmRepresentation createRealm(RealmsResource realms, RealmName realmName) {
        final RealmRepresentation realmRepresentation = new RealmRepresentation();
        realmRepresentation.setRealm(realmName.name());
        realmRepresentation.setEnabled(true);
        realms.create(realmRepresentation);

        return realms.realm(realmName.name()).toRepresentation();
    }


    // --- Client code
    public static ClientRepresentation createClient(Keycloak keycloak, RealmName realmName, ClientId clientId, ClientSecret clientSecret) {
        RealmResource realmResource = keycloak.realm(realmName.name());
        ClientRepresentation clientRepresentation = new ClientRepresentation();
        clientRepresentation.setClientId(clientId.getId());
        clientRepresentation.setStandardFlowEnabled(true);
        clientRepresentation.setDirectAccessGrantsEnabled(true);
        clientRepresentation.setAuthorizationServicesEnabled(true);
        clientRepresentation.setServiceAccountsEnabled(true); // if not enable it throws 500 server error: java.lang.RuntimeException: Client does not have a service account.
        clientRepresentation.setSecret(clientSecret.secret());
        clientRepresentation.setEnabled(true);

        try (var response = realmResource.clients().create(clientRepresentation)) {
            if (response.getStatus() == 201) {
                return getClientRepresentation(realmResource, clientId);
            }
            throw new KeycloakAdminClientException(MessageFormat.format("Error creating client: [clientId: {}]", clientId.getId()));
        }
    }

    public static ClientRepresentation getClientResourceByClientIdOrElseCreate(Keycloak keycloak, RealmName realmName, ClientId clientId, ClientSecret clientSecret) {
        return keycloak.realm(realmName.name()).clients().findByClientId(clientId.getId()).stream()
                .findFirst()
                .orElseGet(() -> createClient(keycloak, realmName, clientId, clientSecret));
    }

    public static ClientRepresentation getClientRepresentation(RealmResource realmResource, ClientId clientId) {
        return realmResource.clients().findByClientId(clientId.getId()).stream().findFirst().orElseThrow();

    }

    public static ClientResource getClientResource(RealmResource realmResource, ClientId clientId) {
        return realmResource.clients().get(getClientRepresentation(realmResource, clientId).getId());

    }

    // User

    static Response createUser(Keycloak keycloak, RealmName realmName, KeycloakConfigurationProperties.User user) {

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
            log.info(ex.getMessage());
            throw new KeycloakAdminClientException(MessageFormat.format("Unable to create user: {}", user.getUsername()));
        }
    }
}
