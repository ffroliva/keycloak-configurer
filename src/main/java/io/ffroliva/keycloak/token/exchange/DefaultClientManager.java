package io.ffroliva.keycloak.token.exchange;

import org.keycloak.admin.client.Keycloak;
import org.springframework.stereotype.Service;

public class DefaultClientManager extends AbstractClientManager {

    protected DefaultClientManager(Keycloak keycloak, RealmName realmName, ClientId clientId) {
        super(keycloak, realmName, clientId);
    }
}
