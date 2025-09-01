package com.itasca.spoofing.model;


import com.itasca.spoofing.entity.UserRole;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import jakarta.validation.constraints.NotNull;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class RoleChangeRequestDto {

    @NotNull(message = "Target user ID is required")
    @JsonProperty("target_user_id")
    private Long targetUserId;

    @JsonProperty("new_primary_role")
    private UserRole newPrimaryRole;

    @JsonProperty("additional_roles_to_add")
    private Set<UserRole> additionalRolesToAdd;

    @JsonProperty("additional_roles_to_remove")
    private Set<UserRole> additionalRolesToRemove;

    @JsonProperty("department")
    private String department;

    @JsonProperty("team")
    private String team;

    @JsonProperty("manager_id")
    private Long managerId;

    @JsonProperty("reason")
    private String reason;
}