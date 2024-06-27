package io.ffroliva.keycloak.token.exchange;

public record RealmManagementClient(String clientId) implements ClientId {
    @Override
    public String getId() {
        return clientId;
    }
}
