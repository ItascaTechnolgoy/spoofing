package com.itasca.spoofing.entity;



import lombok.Getter;
import java.util.Collections;
import java.util.Set;

@Getter
public enum UserRole {
    // Basic user - can only use assigned profiles
    USER("Basic user with profile access", 1,
            "VIEW_ASSIGNED_GROUPS", "USE_PROFILES", "VIEW_OWN_STATS"),

    // Profile manager - can create/edit profiles but not manage users
//    PROFILE_MANAGER("Profile manager with creation rights", 2,
//            "VIEW_ASSIGNED_GROUPS", "USE_PROFILES", "VIEW_OWN_STATS",
//            "CREATE_PROFILES", "EDIT_PROFILES", "VIEW_ALL_PROFILES",
//            "CREATE_CUSTOM_GROUPS", "EDIT_GROUPS"),
//
//    // Team lead - can manage team members and their group assignments
//    TEAM_LEAD("Team leader with user assignment rights", 3,
//            "VIEW_ASSIGNED_GROUPS", "USE_PROFILES", "VIEW_OWN_STATS",
//            "CREATE_PROFILES", "EDIT_PROFILES", "VIEW_ALL_PROFILES",
//            "CREATE_CUSTOM_GROUPS", "EDIT_GROUPS", "DELETE_GROUPS",
//            "ASSIGN_GROUPS_TO_USERS", "VIEW_TEAM_USERS", "VIEW_ALL_STATS"),

    // Admin - full system management except super admin functions
    ADMIN("System administrator", 4,
            "VIEW_ASSIGNED_GROUPS", "USE_PROFILES", "VIEW_OWN_STATS",
            "CREATE_PROFILES", "EDIT_PROFILES", "DELETE_PROFILES", "VIEW_ALL_PROFILES",
            "CREATE_CUSTOM_GROUPS", "EDIT_GROUPS", "DELETE_GROUPS",
            "ASSIGN_GROUPS_TO_USERS", "VIEW_TEAM_USERS", "VIEW_ALL_STATS",
            "MANAGE_USERS", "VIEW_ALL_USERS", "ACTIVATE_USERS", "DEACTIVATE_USERS",
            "VIEW_SYSTEM_STATS", "MANAGE_ROLES"),

    // Super admin - complete system control
    SUPER_ADMIN("Super administrator with full control", 5,
            "VIEW_ASSIGNED_GROUPS", "USE_PROFILES", "VIEW_OWN_STATS",
            "CREATE_PROFILES", "EDIT_PROFILES", "DELETE_PROFILES", "VIEW_ALL_PROFILES",
            "CREATE_CUSTOM_GROUPS", "EDIT_GROUPS", "DELETE_GROUPS",
            "ASSIGN_GROUPS_TO_USERS", "VIEW_TEAM_USERS", "VIEW_ALL_STATS",
            "MANAGE_USERS", "VIEW_ALL_USERS", "ACTIVATE_USERS", "DEACTIVATE_USERS",
            "VIEW_SYSTEM_STATS", "MANAGE_ROLES", "DELETE_USERS", "MANAGE_SUPER_ADMINS",
            "SYSTEM_CONFIGURATION", "VIEW_AUDIT_LOGS", "CLEANUP_DATA");

    private final String description;
    private final int level; // Higher level = more permissions
    private final Set<String> permissions;

    UserRole(String description, int level, String... permissions) {
        this.description = description;
        this.level = level;
        this.permissions = Set.of(permissions);
    }

    /**
     * Check if this role has a specific permission
     */
    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }

    /**
     * Check if this role has higher or equal level than another role
     */
    public boolean hasLevelOrHigher(UserRole other) {
        return this.level >= other.level;
    }

    /**
     * Get all permissions for this role
     */
    public Set<String> getAllPermissions() {
        return Collections.unmodifiableSet(permissions);
    }
}