package io.ffroliva.keycloak.token.exchange;

public record RealmName(String name) {

    @Override
    public String toString() {
        return name;
    }
}
