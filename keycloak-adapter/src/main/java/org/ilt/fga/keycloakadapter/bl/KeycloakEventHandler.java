package org.ilt.fga.keycloakadapter.bl;

import org.ilt.fga.keycloakadapter.events.KeycloakAdminEvent;

public interface KeycloakEventHandler {
    void process(KeycloakAdminEvent event);
}
