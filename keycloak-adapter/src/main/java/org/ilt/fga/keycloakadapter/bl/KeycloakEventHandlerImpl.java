package org.ilt.fga.keycloakadapter.bl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.ilt.fga.keycloakadapter.events.KeycloakAdminEvent;
import org.ilt.fga.keycloakadapter.events.OperationType;
import org.ilt.fga.keycloakadapter.events.ResourceType;
import org.ilt.fga.keycloakadapter.relations.AuthRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Slf4j
public class KeycloakEventHandlerImpl implements KeycloakEventHandler {

  Map<String, UserProfile> users = new HashMap<>();

  @Autowired AuthRelationService authRelationService;

  @Override
  public void process(KeycloakAdminEvent event) {

    // adding user into a group
    // 1- User Create with group value in it
    // 2- Group_membership create

    // user created
    if (event.getResourceType() == ResourceType.USER
        && event.getOperationType() == OperationType.CREATE) {
      KeycloakAdminEvent.UserRepresentation userRepresentation;
      try {
        userRepresentation =
            new ObjectMapper()
                .readValue(event.getRepresentation(), KeycloakAdminEvent.UserRepresentation.class);
      } catch (JsonProcessingException e) {
        log.warn("exception while parsing the user representation");
        return;
      }
      String userId = event.getResourcePath().split("/")[1];
      log.info("creating new username {} and id {} ", userRepresentation.getUsername(), userId);
      users.put(userId, new UserProfile(userRepresentation.getUsername()));
    }

    // user assigned the role
    if (event.getResourceType() == ResourceType.REALM_ROLE_MAPPING
        && event.getOperationType() == OperationType.CREATE) {
      String userId = event.getResourcePath().split("/")[1];
      Optional.ofNullable(users.get(userId))
          .map(
              u -> {
                u.setRole(getRole(event.getRepresentation()));
                return u;
              });
    }

    // group updated with expiry
    if (event.getResourceType() == ResourceType.GROUP
        && event.getOperationType() == OperationType.UPDATE) {
      ResourcePath resourcePath = getResourcePath(event);
      if (resourcePath == null) return;
      Object attributes = getObjectFromJson(event.getRepresentation(), "attributes", false);
      Map<String, List<String>> attrMap = new ObjectMapper().convertValue(attributes, Map.class);
      String tempUserAgent = null;
      String expiryDate = null;
      if (!attrMap.isEmpty()) {
        for (String key : attrMap.keySet()) {
          if (key.toLowerCase().contains("expiry")) {
            tempUserAgent = key.split("_")[1];
            expiryDate = attrMap.get(key).get(0);
          }
        }
      }

      log.info(
          "adding temp agent {} into group {} with expiry {} ",
          tempUserAgent,
          resourcePath.groupId(),
          expiryDate);

      // group_agent:g1#member@user:agent3[is_not_expired:{"expiration":"2023-06-24T00:00:00Z"}]
      authRelationService.addAgent2Group(resourcePath.groupId(), tempUserAgent, expiryDate);
    }

    // user added to group
    if (event.getResourceType() == ResourceType.GROUP_MEMBERSHIP
        && event.getOperationType() == OperationType.CREATE) {

      // validate
      ResourcePath resourcePath = getResourcePath(event);
      if (resourcePath == null) return;
      UserProfile user = users.get(resourcePath.userId());
      if (user == null) {
        log.warn("user cannot be empty, not created yet! returning from group assignment");
        return;
      }

      GroupType groupType = GroupType.NOT_SCOPED;
      String groupName = getValueFromJson(event.getRepresentation(), "name", false);
      if (StringUtils.hasLength(groupName)) {
        if (groupName.toLowerCase().contains("branch")) {
          groupType = GroupType.BRANCH;
        } else if (resourcePath.isBranchGroup()) {
          groupType = GroupType.AGENT_GROUPS;
        }
      }

      log.info("running logic for group {} and grouptype {}", groupName, groupType);

      // manager must be under branch
      if (groupType == GroupType.BRANCH) {
        user.setBranch(resourcePath.groupId());
        // assign manager to the branch
        if (user.getRole() == Role.MANAGER) {
          log.info(
              "assigning manager {} to branch {} and branch id {}",
              user.getName(),
              groupName,
              resourcePath.groupId());
          // branch:1#manager@user:manager1
          authRelationService.addManager2Branch(resourcePath.userId(), resourcePath.groupId());
        }
      } else if (groupType == GroupType.AGENT_GROUPS) {
        if (user.getRole() == Role.AGENT) {
          log.info(
              "assigning agent {} to sub branch group {},id {} and branch id {}",
              user.getName(),
              groupName,
              resourcePath.groupId(),
              user.getBranch());
          // group_agent:g1#member@user:agent1
          authRelationService.addAgent2Group(resourcePath.groupId(), resourcePath.userId(), null);
          // branch:1#agent@user:agent1
          authRelationService.addAgent2Branch(user.getBranch(), resourcePath.userId());
          // group_agent:g1#branch@branch:1
          authRelationService.addBranch2Group(user.getBranch(), resourcePath.groupId());
        }
      }
    }
  }

  private ResourcePath getResourcePath(KeycloakAdminEvent event) {
    String userId;
    String groupId;
    Pattern pattern = Pattern.compile("users/(.*?)/groups/(.*?)$");
    Matcher matcher = pattern.matcher(event.getResourcePath());
    if (matcher.find()) {
      userId = matcher.group(1); // Get the user ID
      groupId = matcher.group(2); // Get the group ID
    } else {
      log.warn("something wrong with the resource path {}", event.getResourcePath());
      return null;
    }
    // now check the group path if there is branch->sub_group hierarchy
    Pattern groupPathPattern = Pattern.compile("/(branch.*)/(.+)");
    Matcher groupPathMatcher =
        groupPathPattern.matcher(getValueFromJson(event.getRepresentation(), "path", false));
    boolean isBranchGroup = false;
    String branchId = null;
    if (groupPathMatcher.find()) {
      branchId = groupPathMatcher.group(1);
      isBranchGroup = true;
    }

    ResourcePath result = new ResourcePath(userId, groupId, branchId, isBranchGroup);
    return result;
  }

  private record ResourcePath(
      String userId, String groupId, String branchId, boolean isBranchGroup) {}

  private Role getRole(String representation) {
    String roleName = getValueFromJson(representation, "name", true);
    if (roleName == null) return Role.NO_ROLE;
    Role role =
        Arrays.stream(Role.values())
            .filter(r -> r.name().equalsIgnoreCase(roleName))
            .findAny()
            .get();
    log.info("found the role {}", role);
    return role;
  }

  private String getValueFromJson(String json, String field, boolean isArray) {
    try {
      JsonNode jsonNode = new ObjectMapper().readTree(json);
      if (isArray) {
        return jsonNode.elements().next().get(field).asText();
      }

      return jsonNode.get(field).asText();

    } catch (JsonProcessingException e) {
      log.warn("parsing error from json {}", json);
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

  @Data
  private class UserProfile {
    private final String name;
    Role role;
    String branch;
  }

  private enum Role {
    NO_ROLE,
    MANAGER,
    AGENT,
    CLIENT
  }

  private enum GroupType {
    NOT_SCOPED,
    BRANCH,
    AGENT_GROUPS
  }
}
