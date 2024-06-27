package io.ffroliva.keycloak.token.exchange;

public record TargetClient (String clientId) implements ClientId {

    @Override
    public String getId() {
        return clientId;
    }
}
