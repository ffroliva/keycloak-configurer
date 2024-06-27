package io.ffroliva.keycloak.token.exchange;

import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UserRepresentationBuilder {

    private String id;
    private OffsetDateTime createdTimestamp;
    private String username;
    private Boolean enabled;
    private Boolean emailVerified;
    private String firstName;
    private String lastName;
    private String email;
    private List<CredentialRepresentation> credentials = new ArrayList<>();
    private Map<String, List<String>> attributes;
    private List<String> requiredActions;
    private List<String> reamlRoles = new ArrayList<>();
    private String federationLink;


    public UserRepresentationBuilder() {
        // Initialize fields if necessary
    }

    public UserRepresentationBuilder setId(String id) {
        this.id = id;
        return this;
    }

    public UserRepresentationBuilder setCreatedTimestamp(OffsetDateTime createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
        return this;
    }

    public UserRepresentationBuilder setUsername(String username) {
        this.username = username;
        return this;
    }

    public UserRepresentationBuilder setEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }


    public UserRepresentationBuilder setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
        return this;
    }

    public UserRepresentationBuilder setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public UserRepresentationBuilder setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public UserRepresentationBuilder setEmail(String email) {
        this.email = email;
        return this;
    }

    public UserRepresentationBuilder addCredentials(CredentialRepresentation... credentials) {
        this.credentials.addAll(Set.of(credentials));
        return this;
    }

    public UserRepresentationBuilder addRealRoles(String... realRoles) {
        this.reamlRoles.addAll(Set.of(realRoles));
        return this;
    }

    public UserRepresentationBuilder setAttributes(Map<String, List<String>> attributes) {
        this.attributes = attributes;
        return this;
    }

    public UserRepresentationBuilder setRequiredActions(List<String> requiredActions) {
        this.requiredActions = requiredActions;
        return this;
    }


    public UserRepresentationBuilder setFederationLink(String federationLink) {
        this.federationLink = federationLink;
        return this;
    }


    public UserRepresentation build() {
        UserRepresentation user = new UserRepresentation();
        user.setId(this.id);
        user.setCreatedTimestamp(this.createdTimestamp.toEpochSecond());
        user.setUsername(this.username);
        user.setEnabled(this.enabled);
        user.setEmailVerified(this.emailVerified);
        user.setFirstName(this.firstName);
        user.setLastName(this.lastName);
        user.setEmail(this.email);
        user.setCredentials(this.credentials);
        user.setAttributes(this.attributes);
        user.setRequiredActions(this.requiredActions);
        user.setRealmRoles(this.reamlRoles);
        user.setFederationLink(this.federationLink);
        return user;
    }

    public UserRepresentation create() {
        UserRepresentation user = new UserRepresentation();
        user.setId(this.id);
        user.setCreatedTimestamp(this.createdTimestamp.toEpochSecond());
        user.setUsername(this.username);
        user.setEnabled(this.enabled);
        user.setEmailVerified(this.emailVerified);
        user.setFirstName(this.firstName);
        user.setLastName(this.lastName);
        user.setEmail(this.email);
        user.setCredentials(this.credentials);
        user.setAttributes(this.attributes);
        user.setRequiredActions(this.requiredActions);
        user.setRealmRoles(this.reamlRoles);
        user.setFederationLink(this.federationLink);
        return user;
    }
}



