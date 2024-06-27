package io.ffroliva.keycloak.token.exchange;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.ManagementPermissionRepresentation;

import static io.ffroliva.keycloak.token.exchange.TokenExchangeConstants.REALM_MANAGEMENT_CLIENT;

/**
 * This class is a specialized ClientManager that manages keycloak 'realm-manager' client
 */
public class RealmManagementClientManager extends AbstractClientManager {

    public RealmManagementClientManager(Keycloak keycloak, RealmName realmName) {
        super(keycloak, realmName, ClientId.of(REALM_MANAGEMENT_CLIENT));

    }


}
