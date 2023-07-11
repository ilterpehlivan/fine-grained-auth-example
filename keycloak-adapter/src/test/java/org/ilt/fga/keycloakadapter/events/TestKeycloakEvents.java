package org.ilt.fga.keycloakadapter.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

public class TestKeycloakEvents {

  @Test
  void testGroupAttributesParsing() throws JsonProcessingException {
    String groupUpdateRepresentation =
        """
      {
        "id": "b0031b1f-cd70-4e62-a721-ac75150c54a5",
        "name": "group-1",
        "path": "/branch-1/group-1",
        "attributes": {
          "expiry_2e62c6b6-0bbe-48fa-b31b-6856f1d95916": [
            "2023-06-24T00:00:00Z"
          ]
        },
        "realmRoles": [],
        "clientRoles": {},
        "subGroups": []
      }
    """;

    Object attributes = getObjectFromJson(groupUpdateRepresentation, "attributes", false);
    Map<String, List<String>> attrMap = new ObjectMapper().convertValue(attributes, Map.class);

    assertThat(attrMap.size()).isEqualTo(1);
    attrMap.forEach(
        (k, v) -> {
          assertThat(k).contains("expiry");
          assertThat(v.size()).isEqualTo(1);
        });
  }

  @Test
  void shouldReadSampleEventAndConvertToEventObject() throws IOException {
    // given read sample event from resources
    Resource resource = new ClassPathResource("sample-keycloak-event-createUser.json");
    try (InputStream inputStream = resource.getInputStream()) {
      KeycloakAdminEvent keycloakAdminEvent =
          new ObjectMapper().readValue(inputStream, KeycloakAdminEvent.class);
      assertThat(keycloakAdminEvent.getId()).isNotEmpty();
      assertThat(keycloakAdminEvent.getRealmId()).isEqualTo("cdafecc5-5657-4065-844a-6a5600e25afd");
      assertThat(keycloakAdminEvent.getOperationType()).isEqualTo(OperationType.CREATE);
      assertThat(keycloakAdminEvent.getResourceType()).isEqualTo(ResourceType.USER);
      System.out.println(keycloakAdminEvent.getRepresentation());
      KeycloakAdminEvent.UserRepresentation userRepresentation =
          new ObjectMapper()
              .readValue(
                  keycloakAdminEvent.getRepresentation(),
                  KeycloakAdminEvent.UserRepresentation.class);
      System.out.println(userRepresentation);
    }
  }

  @Test
  void testBranchPatternMatchin() {
    String groupRepresentation =
            """
          {
            "id": "b0031b1f-cd70-4e62-a721-ac75150c54a5",
            "name": "group-1",
            "path": "/branch-1/group-1",
            "attributes": {
              "expiry_2e62c6b6-0bbe-48fa-b31b-6856f1d95916": [
                "2023-06-24T00:00:00Z"
              ]
            },
            "realmRoles": [],
            "clientRoles": {},
            "subGroups": []
          }
        """;
    Pattern groupPathPattern = Pattern.compile("/(branch.*)/(.+)");
    Matcher groupPathMatcher =
            groupPathPattern.matcher(getValueFromJson(groupRepresentation, "path", false));
    if (groupPathMatcher.find()) {
      String group1 = groupPathMatcher.group(1);
      String group2 = groupPathMatcher.group(2);
      System.out.println(group1);
      System.out.println(group2);
    }

  }

  private String getValueFromJson(String json, String field, boolean isArray) {
    try {
      JsonNode jsonNode = new ObjectMapper().readTree(json);
      if (isArray) {
        return jsonNode.elements().next().get(field).asText();
      }

      return jsonNode.get(field).asText();

    } catch (JsonProcessingException e) {
      return null;
    }
  }

  private Object getObjectFromJson(String json, String field, boolean isArray) {
    try {
      JsonNode jsonNode = new ObjectMapper().readTree(json);
      if (isArray) {
        return jsonNode.elements().next().get(field);
      }

      return jsonNode.get(field);

    } catch (JsonProcessingException e) {
      return null;
    }
  }
}
