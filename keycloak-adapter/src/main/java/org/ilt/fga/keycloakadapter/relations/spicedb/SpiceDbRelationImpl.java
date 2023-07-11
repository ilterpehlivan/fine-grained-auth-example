package org.ilt.fga.keycloakadapter.relations.spicedb;

import com.authzed.api.v1.Core;
import com.authzed.api.v1.PermissionService;
import com.authzed.api.v1.PermissionsServiceGrpc;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.ilt.fga.keycloakadapter.relations.AuthRelationService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Slf4j
public class SpiceDbRelationImpl implements AuthRelationService {
  @GrpcClient("permissionClient")
  private PermissionsServiceGrpc.PermissionsServiceBlockingStub permissionService;

  @Override
  public void addManager2Branch(String managerId, String branchId) {
    PermissionService.WriteRelationshipsRequest relationshipsRequest =
        PermissionService.WriteRelationshipsRequest.newBuilder()
            .addUpdates(
                Core.RelationshipUpdate.newBuilder()
                    .setOperation(Core.RelationshipUpdate.Operation.OPERATION_CREATE)
                    .setRelationship(
                        Core.Relationship.newBuilder()
                            .setResource(
                                Core.ObjectReference.newBuilder()
                                    .setObjectType("branch")
                                    .setObjectId(branchId)
                                    .build())
                            .setRelation("manager")
                            .setSubject(
                                Core.SubjectReference.newBuilder()
                                    .setObject(
                                        Core.ObjectReference.newBuilder()
                                            .setObjectType("user")
                                            .setObjectId(managerId)
                                            .build())
                                    .build())
                            .build())
                    .build())
            .build();

    PermissionService.WriteRelationshipsResponse response = null;
    try {
      response = permissionService.writeRelationships(relationshipsRequest);
    } catch (Exception e) {
      log.warn("RPC failed {}", e.getMessage());
      return;
    }
    log.info("Relationship Response: {}", response.toString());
  }

  @Override
  public void addAgent2Group(String groupId, String agentId, String expiryDate) {
    // group_agent:g1#member@user:agent1
    log.info(
        "relationship:adding agent {} to group {} with if expirydate {}",
        agentId,
        groupId,
        expiryDate);
    Core.Relationship.Builder relationshipBuilder =
        Core.Relationship.newBuilder()
            .setResource(
                Core.ObjectReference.newBuilder()
                    .setObjectType("group_agent")
                    .setObjectId(groupId)
                    .build())
            .setRelation("member")
            .setSubject(
                Core.SubjectReference.newBuilder()
                    .setObject(
                        Core.ObjectReference.newBuilder()
                            .setObjectType("user")
                            .setObjectId(agentId)
                            .build())
                    .build());

    if (StringUtils.hasLength(expiryDate)) {
      // add caveat
      relationshipBuilder.setOptionalCaveat(
          Core.ContextualizedCaveat.newBuilder()
              .setCaveatName("is_not_expired")
              .setContext(
                  Struct.newBuilder()
                      .putFields(
                          "expiration", Value.newBuilder().setStringValue(expiryDate).build())
                      .build())
              .build());
    }
    PermissionService.WriteRelationshipsRequest relationshipsRequest =
        PermissionService.WriteRelationshipsRequest.newBuilder()
            .addUpdates(
                Core.RelationshipUpdate.newBuilder()
                    .setOperation(Core.RelationshipUpdate.Operation.OPERATION_CREATE)
                    .setRelationship(relationshipBuilder.build())
                    .build())
            .build();

    PermissionService.WriteRelationshipsResponse response = null;
    try {
      response = permissionService.writeRelationships(relationshipsRequest);
    } catch (Exception e) {
      log.warn("RPC failed {}", e.getMessage());
      return;
    }
    log.info("Relationship Response: {}", response.toString());
  }

  @Override
  public void addAgent2Branch(String branchId, String agentId) {
    // branch:1#agent@user:agent1
    log.info("relationship:adding agent {} to branch {}", agentId, branchId);
    PermissionService.WriteRelationshipsRequest relationshipsRequest =
        PermissionService.WriteRelationshipsRequest.newBuilder()
            .addUpdates(
                Core.RelationshipUpdate.newBuilder()
                    .setOperation(Core.RelationshipUpdate.Operation.OPERATION_CREATE)
                    .setRelationship(
                        Core.Relationship.newBuilder()
                            .setResource(
                                Core.ObjectReference.newBuilder()
                                    .setObjectType("branch")
                                    .setObjectId(branchId)
                                    .build())
                            .setRelation("agent")
                            .setSubject(
                                Core.SubjectReference.newBuilder()
                                    .setObject(
                                        Core.ObjectReference.newBuilder()
                                            .setObjectType("user")
                                            .setObjectId(agentId)
                                            .build())
                                    .build())
                            .build())
                    .build())
            .build();

    PermissionService.WriteRelationshipsResponse response = null;
    try {
      response = permissionService.writeRelationships(relationshipsRequest);
    } catch (Exception e) {
      log.warn("RPC failed {}", e.getMessage());
      return;
    }
    log.info("Relationship Response: {}", response.toString());
  }

  @Override
  public void addBranch2Group(String branchId, String groupId) {
    // group_agent:g1#branch@branch:1
    log.info("relationship:adding branch {} to group {}", branchId, groupId);
    PermissionService.WriteRelationshipsRequest relationshipsRequest =
        PermissionService.WriteRelationshipsRequest.newBuilder()
            .addUpdates(
                Core.RelationshipUpdate.newBuilder()
                    .setOperation(Core.RelationshipUpdate.Operation.OPERATION_CREATE)
                    .setRelationship(
                        Core.Relationship.newBuilder()
                            .setResource(
                                Core.ObjectReference.newBuilder()
                                    .setObjectType("group_agent")
                                    .setObjectId(groupId)
                                    .build())
                            .setRelation("branch")
                            .setSubject(
                                Core.SubjectReference.newBuilder()
                                    .setObject(
                                        Core.ObjectReference.newBuilder()
                                            .setObjectType("branch")
                                            .setObjectId(branchId)
                                            .build())
                                    .build())
                            .build())
                    .build())
            .build();

    PermissionService.WriteRelationshipsResponse response = null;
    try {
      response = permissionService.writeRelationships(relationshipsRequest);
    } catch (Exception e) {
      log.warn("RPC failed {}", e.getMessage());
      return;
    }
    log.info("Relationship Response: {}", response.toString());
  }
}
