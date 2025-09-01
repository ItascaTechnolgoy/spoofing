package com.itasca.spoofing.model;




import com.itasca.spoofing.entity.UserRole;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"password"})
@EqualsAndHashCode(exclude = {"password"})
public class UserDto {

    private Long id;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 255, message = "Email cannot exceed 255 characters")
    private String email;

    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String password;

    @JsonProperty("first_name")
    @Size(max = 100, message = "First name cannot exceed 100 characters")
    private String firstName;

    @JsonProperty("last_name")
    @Size(max = 100, message = "Last name cannot exceed 100 characters")
    private String lastName;

    @JsonProperty("full_name")
    private String fullName;

    // Role information
    @JsonProperty("primary_role")
    @Builder.Default
    private UserRole primaryRole = UserRole.USER;

    @JsonProperty("additional_roles")
    @Builder.Default
    private Set<UserRole> additionalRoles = new HashSet<>();

    @JsonProperty("all_roles")
    @Builder.Default
    private Set<UserRole> allRoles = new HashSet<>();

    @JsonProperty("permissions")
    @Builder.Default
    private Set<String> permissions = new HashSet<>();

    // Organizational structure
    private String department;
    private String team;

    @JsonProperty("manager_id")
    private Long managerId;

    @JsonProperty("manager_name")
    private String managerName;

    @JsonProperty("direct_reports_count")
    private Integer directReportsCount;

    @Builder.Default
    private String status = "ACTIVE";

    @JsonProperty("last_login")
    private LocalDateTime lastLogin;

    @JsonProperty("email_verified")
    @Builder.Default
    private Boolean emailVerified = false;

    @JsonProperty("assigned_group_ids")
    @Builder.Default
    private Set<String> assignedGroupIds = new HashSet<>();

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    // Role change tracking
    @JsonProperty("role_changed_at")
    private LocalDateTime roleChangedAt;

    @JsonProperty("role_changed_by")
    private String roleChangedBy;

    // Helper methods
    public boolean hasRole(UserRole role) {
        return allRoles.contains(role);
    }

    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }

    public boolean isActive() {
        return "ACTIVE".equals(status);
    }

    public boolean isAdmin() {
        return hasRole(UserRole.ADMIN) || hasRole(UserRole.SUPER_ADMIN);
    }

    public boolean canManageUsers() {
        return hasPermission("MANAGE_USERS");
    }

    public boolean canManageRoles() {
        return hasPermission("MANAGE_ROLES");
    }

    public String getRoleLevel() {
        return primaryRole.getDescription();
    }
}