package org.ilt.fga.keycloakadapter.events;

import lombok.extern.slf4j.Slf4j;
import org.ilt.fga.keycloakadapter.bl.KeycloakEventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KafkaEventListener {

  @Autowired private KeycloakEventHandler eventHandler;

  @KafkaListener(topics = "keycloak-admin-events", groupId = "event-adapter")
  public void handleKeycloakAdminEvent(KeycloakAdminEvent event) {
    log.info("Received admin event {}", event);
    eventHandler.process(event);
  }
}
