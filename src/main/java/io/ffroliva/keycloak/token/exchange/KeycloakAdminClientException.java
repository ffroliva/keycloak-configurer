package io.ffroliva.keycloak.token.exchange;

public class KeycloakAdminClientException extends RuntimeException {

    KeycloakAdminClientException(String msg) {
        this(msg, null);
    }

    KeycloakAdminClientException(String msg, Throwable e) {
        super(msg, e);
    }
}
