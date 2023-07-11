package org.ilt.fga.keycloakadapter.relations;



//Example:
 //         <objectType>:<objectId>#<permission>@<subjectType>:<subjectId>
//          group_agent:g1#member@user:agent1
public record Relation(String objectType,String objectId,String subjectType,String subjectId,String permission) {}
