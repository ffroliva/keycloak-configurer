package io.ffroliva.keycloak.token.exchange;

public record OriginalClient(String name) implements ClientId {
    @Override
    public String getId() {
        return name;
    }
}
