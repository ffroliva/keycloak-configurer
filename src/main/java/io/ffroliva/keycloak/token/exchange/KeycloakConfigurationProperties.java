package io.ffroliva.keycloak.token.exchange;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@Getter
@Setter
@ConfigurationProperties(prefix = "keycloak")
public class KeycloakConfigurationProperties {

    private String authUrl;
    private String realm;
    private Map<String, ClientConfig> clients;
    private Map<String, User> users;

    @Getter
    @Setter
    public static class ClientConfig {
        private String clientId;
        private String clientSecret;
    }

    @Getter
    @Setter
    public static class User {
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String password;
    }

    public ClientConfig getClientConfig(String clientId) {
        return clients.get(clientId);
    }

    public User getUserByUsername(String username) {
        return users.get(username);
    }

}
