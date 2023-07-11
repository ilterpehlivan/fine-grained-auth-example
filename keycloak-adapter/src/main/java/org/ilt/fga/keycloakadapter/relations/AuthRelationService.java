package org.ilt.fga.keycloakadapter.relations;

public interface AuthRelationService {
    void addManager2Branch(String managerId,String branchId);

    void addAgent2Group(String groupId, String agentId, String expiryDate);

    void addAgent2Branch(String branchId,String agentId);

    void addBranch2Group(String branchId,String groupId);
}
