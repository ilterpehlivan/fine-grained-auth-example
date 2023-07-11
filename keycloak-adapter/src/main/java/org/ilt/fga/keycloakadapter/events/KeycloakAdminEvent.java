package org.ilt.fga.keycloakadapter.events;

import lombok.Data;

import java.util.List;

@Data
public class KeycloakAdminEvent {
  private String id;
  private long time;
  private String realmId;
  private AuthDetails authDetails;
  private ResourceType resourceType;
  private OperationType operationType;
  private String resourcePath;
  private String representation;
  private String error;
  private String resourceTypeAsString;

  @Data
  public static class AuthDetails {
    private String realmId;
    private String clientId;
    private String userId;
    private String ipAddress;
  }

  @Data
  public static class UserRepresentation {
    private String username;
    private boolean enabled;
    private boolean emailVerified;
    private String firstName;
    private String lastName;
    private String email;
    private List<String> requiredActions;
    private List<String> groups;
  }
}
