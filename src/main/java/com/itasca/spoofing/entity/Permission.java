package com.itasca.spoofing.entity;

public final class Permission {
    // Profile permissions
    public static final String VIEW_ASSIGNED_GROUPS = "VIEW_ASSIGNED_GROUPS";
    public static final String USE_PROFILES = "USE_PROFILES";
    public static final String CREATE_PROFILES = "CREATE_PROFILES";
    public static final String EDIT_PROFILES = "EDIT_PROFILES";
    public static final String DELETE_PROFILES = "DELETE_PROFILES";
    public static final String VIEW_ALL_PROFILES = "VIEW_ALL_PROFILES";

    // Group permissions
    public static final String CREATE_CUSTOM_GROUPS = "CREATE_CUSTOM_GROUPS";
    public static final String EDIT_GROUPS = "EDIT_GROUPS";
    public static final String DELETE_GROUPS = "DELETE_GROUPS";
    public static final String ASSIGN_GROUPS_TO_USERS = "ASSIGN_GROUPS_TO_USERS";

    // User management permissions
    public static final String MANAGE_USERS = "MANAGE_USERS";
    public static final String VIEW_ALL_USERS = "VIEW_ALL_USERS";
    public static final String VIEW_TEAM_USERS = "VIEW_TEAM_USERS";
    public static final String ACTIVATE_USERS = "ACTIVATE_USERS";
    public static final String DEACTIVATE_USERS = "DEACTIVATE_USERS";
    public static final String DELETE_USERS = "DELETE_USERS";
    public static final String MANAGE_ROLES = "MANAGE_ROLES";
    public static final String MANAGE_SUPER_ADMINS = "MANAGE_SUPER_ADMINS";

    // Statistics permissions
    public static final String VIEW_OWN_STATS = "VIEW_OWN_STATS";
    public static final String VIEW_ALL_STATS = "VIEW_ALL_STATS";
    public static final String VIEW_SYSTEM_STATS = "VIEW_SYSTEM_STATS";
    public static final String VIEW_AUDIT_LOGS = "VIEW_AUDIT_LOGS";

    // System permissions
    public static final String SYSTEM_CONFIGURATION = "SYSTEM_CONFIGURATION";
    public static final String CLEANUP_DATA = "CLEANUP_DATA";

    private Permission() {} // Prevent instantiation
}