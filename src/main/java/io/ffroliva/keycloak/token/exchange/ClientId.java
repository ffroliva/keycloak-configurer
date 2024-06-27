package io.ffroliva.keycloak.token.exchange;

public interface ClientId {

    String getId();

    static ClientId of(String id) {
        return () -> id;
    }
}
