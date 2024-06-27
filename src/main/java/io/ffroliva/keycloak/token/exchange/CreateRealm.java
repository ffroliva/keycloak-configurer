package io.ffroliva.keycloak.token.exchange;

import lombok.AllArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmsResource;
import org.keycloak.representations.idm.RealmRepresentation;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class CreateRealm {

    private final RealmsResource realmsResource;

    void execute(String realmName) {
        RealmRepresentation newRealm = new RealmRepresentation();
        newRealm.setRealm(realmName);
        realmsResource.create(newRealm);
    }
}
