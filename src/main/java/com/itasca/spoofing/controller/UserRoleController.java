package com.itasca.spoofing.controller;


import com.itasca.spoofing.model.*;
import com.itasca.spoofing.entity.UserRole;
import com.itasca.spoofing.service.UserRoleService;
import com.itasca.spoofing.service.UserService;
import com.itasca.spoofing.exception.UnauthorizedAccessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/user-roles")
@Tag(name = "User Role Management", description = "Role-based user management and hierarchy APIs")
@Validated
@Slf4j
@SecurityRequirement(name = "Bearer Authentication")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserRoleController {

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private UserService userService;

    // ==================== USER MANAGEMENT ====================

    @GetMapping
    @PreAuthorize("hasPermission(authentication.principal.id, 'VIEW_ALL_USERS')")
    @Operation(summary = "Get all users", description = "Get all users with role information (Admin+ only)")
    public ResponseEntity<Page<UserDto>> getAllUsers(@PageableDefault(size = 20) Pageable pageable) {
        log.debug("Retrieving all users with pagination: {}", pageable);

        try {
            Page<UserDto> users = userService.getUsers(pageable);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Error retrieving users: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasPermission(authentication.principal.id, 'VIEW_ALL_USERS') or #id == authentication.principal.id")
    @Operation(summary = "Get user by ID", description = "Get user details by ID")
    public ResponseEntity<UserDto> getUserById(@Parameter(description = "User ID") @PathVariable Long id) {
        log.debug("Retrieving user: {}", id);

        try {
            return userService.getUserById(id)
                    .map(user -> ResponseEntity.ok(user))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error retrieving user {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Get current authenticated user's details")
    public ResponseEntity<UserDto> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long currentUserId = getCurrentUserId(auth);

        log.debug("Retrieving current user: {}", currentUserId);

        try {
            return userService.getUserById(currentUserId)
                    .map(user -> ResponseEntity.ok(user))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error retrieving current user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // ==================== ROLE MANAGEMENT ====================

    @PostMapping("/{id}/role/change")
    @PreAuthorize("hasPermission(authentication.principal.id, 'MANAGE_ROLES')")
    @Operation(summary = "Change user role", description = "Change user's primary role (Admin+ only)")
    public ResponseEntity<Map<String, Object>> changeUserRole(
            @Parameter(description = "User ID") @PathVariable Long id,
            @RequestBody @Valid RoleChangeRequestDto request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String changedBy = auth.getName();

        log.info("Role change request for user {} to {} by {}", id, request.getNewPrimaryRole(), changedBy);

        try {
            boolean success = userRoleService.changeUserRole(id, request.getNewPrimaryRole(), changedBy);
            return ResponseEntity.ok(Map.of(
                    "success", success,
                    "message", "Role changed successfully",
                    "userId", id,
                    "newRole", request.getNewPrimaryRole()
            ));
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error changing role for user {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to change role: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/role/additional/add")
    @PreAuthorize("hasPermission(authentication.principal.id, 'MANAGE_ROLES')")
    @Operation(summary = "Add additional role", description = "Add additional role to user (Admin+ only)")
    public ResponseEntity<Map<String, Object>> addAdditionalRole(
            @Parameter(description = "User ID") @PathVariable Long id,
            @RequestParam UserRole role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String changedBy = auth.getName();

        log.info("Adding additional role {} to user {} by {}", role, id, changedBy);

        try {
            boolean success = userRoleService.addAdditionalRole(id, role, changedBy);
            return ResponseEntity.ok(Map.of(
                    "success", success,
                    "message", "Additional role added successfully",
                    "userId", id,
                    "addedRole", role
            ));
        } catch (Exception e) {
            log.error("Error adding additional role to user {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to add role: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/role/additional/remove")
    @PreAuthorize("hasPermission(authentication.principal.id, 'MANAGE_ROLES')")
    @Operation(summary = "Remove additional role", description = "Remove additional role from user (Admin+ only)")
    public ResponseEntity<Map<String, Object>> removeAdditionalRole(
            @Parameter(description = "User ID") @PathVariable Long id,
            @RequestParam UserRole role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String changedBy = auth.getName();

        log.info("Removing additional role {} from user {} by {}", role, id, changedBy);

        try {
            boolean success = userRoleService.removeAdditionalRole(id, role, changedBy);
            return ResponseEntity.ok(Map.of(
                    "success", success,
                    "message", "Additional role removed successfully",
                    "userId", id,
                    "removedRole", role
            ));
        } catch (Exception e) {
            log.error("Error removing additional role from user {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to remove role: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}/available-roles")
    @PreAuthorize("hasPermission(authentication.principal.id, 'MANAGE_ROLES')")
    @Operation(summary = "Get available roles", description = "Get roles that current user can assign to target user")
    public ResponseEntity<List<UserRole>> getAvailableRoles(@Parameter(description = "User ID") @PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long currentUserId = getCurrentUserId(auth);

        log.debug("Getting available roles for user {} by {}", id, currentUserId);

        try {
            List<UserRole> availableRoles = userRoleService.getAvailableRolesForUser(currentUserId, id);
            return ResponseEntity.ok(availableRoles);
        } catch (Exception e) {
            log.error("Error getting available roles: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // ==================== HIERARCHY MANAGEMENT ====================

    @PostMapping("/{id}/manager/assign")
    @PreAuthorize("hasPermission(authentication.principal.id, 'MANAGE_USERS')")
    @Operation(summary = "Assign manager", description = "Assign a manager to user (Admin+ only)")
    public ResponseEntity<Map<String, Object>> assignManager(
            @Parameter(description = "User ID") @PathVariable Long id,
            @RequestParam Long managerId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String changedBy = auth.getName();

        log.info("Assigning manager {} to user {} by {}", managerId, id, changedBy);

        try {
            boolean success = userRoleService.assignManager(id, managerId, changedBy);
            return ResponseEntity.ok(Map.of(
                    "success", success,
                    "message", "Manager assigned successfully",
                    "userId", id,
                    "managerId", managerId
            ));
        } catch (Exception e) {
            log.error("Error assigning manager to user {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to assign manager: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/manager/remove")
    @PreAuthorize("hasPermission(authentication.principal.id, 'MANAGE_USERS')")
    @Operation(summary = "Remove manager", description = "Remove manager from user (Admin+ only)")
    public ResponseEntity<Map<String, Object>> removeManager(@Parameter(description = "User ID") @PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String changedBy = auth.getName();

        log.info("Removing manager from user {} by {}", id, changedBy);

        try {
            boolean success = userRoleService.removeManager(id, changedBy);
            return ResponseEntity.ok(Map.of(
                    "success", success,
                    "message", "Manager removed successfully",
                    "userId", id
            ));
        } catch (Exception e) {
            log.error("Error removing manager from user {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to remove manager: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}/direct-reports")
    @PreAuthorize("hasPermission(authentication.principal.id, 'VIEW_TEAM_USERS') or #id == authentication.principal.id")
    @Operation(summary = "Get direct reports", description = "Get user's direct reports")
    public ResponseEntity<List<UserDto>> getDirectReports(@Parameter(description = "Manager ID") @PathVariable Long id) {
        log.debug("Getting direct reports for manager: {}", id);

        try {
            List<UserDto> reports = userRoleService.getDirectReports(id);
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            log.error("Error getting direct reports for {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{id}/team-members")
    @PreAuthorize("hasPermission(authentication.principal.id, 'VIEW_TEAM_USERS') or #id == authentication.principal.id")
    @Operation(summary = "Get team members", description = "Get team members for team lead")
    public ResponseEntity<List<UserDto>> getTeamMembers(@Parameter(description = "Team Lead ID") @PathVariable Long id) {
        log.debug("Getting team members for team lead: {}", id);

        try {
            List<UserDto> members = userRoleService.getTeamMembers(id);
            return ResponseEntity.ok(members);
        } catch (Exception e) {
            log.error("Error getting team members for {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // ==================== DEPARTMENT AND TEAM MANAGEMENT ====================

    @PostMapping("/{id}/department/assign")
    @PreAuthorize("hasPermission(authentication.principal.id, 'MANAGE_USERS')")
    @Operation(summary = "Assign department", description = "Assign user to department and team (Admin+ only)")
    public ResponseEntity<Map<String, Object>> assignDepartment(
            @Parameter(description = "User ID") @PathVariable Long id,
            @RequestParam String department,
            @RequestParam(required = false) String team) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String changedBy = auth.getName();

        log.info("Assigning user {} to department {} team {} by {}", id, department, team, changedBy);

        try {
            boolean success = userRoleService.assignUserToDepartment(id, department, team, changedBy);
            return ResponseEntity.ok(Map.of(
                    "success", success,
                    "message", "Department assigned successfully",
                    "userId", id,
                    "department", department,
                    "team", team != null ? team : ""
            ));
        } catch (Exception e) {
            log.error("Error assigning department to user {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to assign department: " + e.getMessage()));
        }
    }

    @GetMapping("/department/{department}")
    @PreAuthorize("hasPermission(authentication.principal.id, 'VIEW_ALL_USERS')")
    @Operation(summary = "Get department users", description = "Get all users in a department (Admin+ only)")
    public ResponseEntity<List<UserDto>> getDepartmentUsers(
            @Parameter(description = "Department name") @PathVariable String department) {
        log.debug("Getting users for department: {}", department);

        try {
            List<UserDto> users = userRoleService.getDepartmentUsers(department);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Error getting department users: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/team/{team}")
    @PreAuthorize("hasPermission(authentication.principal.id, 'VIEW_TEAM_USERS')")
    @Operation(summary = "Get team users", description = "Get all users in a team")
    public ResponseEntity<List<UserDto>> getTeamUsers(@Parameter(description = "Team name") @PathVariable String team) {
        log.debug("Getting users for team: {}", team);

        try {
            List<UserDto> users = userRoleService.getTeamUsers(team);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Error getting team users: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // ==================== ROLE QUERIES AND ANALYTICS ====================

    @GetMapping("/role/{role}")
    @PreAuthorize("hasPermission(authentication.principal.id, 'VIEW_ALL_USERS')")
    @Operation(summary = "Get users by role", description = "Get all users with specific role (Admin+ only)")
    public ResponseEntity<List<UserDto>> getUsersByRole(@Parameter(description = "Role") @PathVariable UserRole role) {
        log.debug("Getting users with role: {}", role);

        try {
            List<UserDto> users = userRoleService.getUsersByRole(role);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Error getting users by role: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/permission/{permission}")
    @PreAuthorize("hasPermission(authentication.principal.id, 'VIEW_ALL_USERS')")
    @Operation(summary = "Get users by permission", description = "Get all users with specific permission (Admin+ only)")
    public ResponseEntity<List<UserDto>> getUsersByPermission(
            @Parameter(description = "Permission") @PathVariable String permission) {
        log.debug("Getting users with permission: {}", permission);

        try {
            List<UserDto> users = userRoleService.getUsersWithPermission(permission);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Error getting users by permission: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/analytics/role-distribution")
    @PreAuthorize("hasPermission(authentication.principal.id, 'VIEW_SYSTEM_STATS')")
    @Operation(summary = "Get role distribution", description = "Get distribution of users across roles (Admin+ only)")
    public ResponseEntity<Map<UserRole, Long>> getRoleDistribution() {
        log.debug("Getting role distribution");

        try {
            Map<UserRole, Long> distribution = userRoleService.getRoleDistribution();
            return ResponseEntity.ok(distribution);
        } catch (Exception e) {
            log.error("Error getting role distribution: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{id}/permissions")
    @PreAuthorize("hasPermission(authentication.principal.id, 'VIEW_ALL_USERS') or #id == authentication.principal.id")
    @Operation(summary = "Get user permissions", description = "Get all permissions for a user")
    public ResponseEntity<Set<String>> getUserPermissions(@Parameter(description = "User ID") @PathVariable Long id) {
        log.debug("Getting permissions for user: {}", id);

        try {
            Set<String> permissions = userRoleService.getUserPermissions(id);
            return ResponseEntity.ok(permissions);
        } catch (Exception e) {
            log.error("Error getting user permissions: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // ==================== BULK OPERATIONS ====================

    @PostMapping("/bulk/role-change")
    @PreAuthorize("hasPermission(authentication.principal.id, 'MANAGE_ROLES')")
    public ResponseEntity<Map<String, Object>> bulkRoleChange(
            @RequestBody @Valid Map<String, Object> request) {
        return ResponseEntity.ok(Map.of("message", "Bulk operation not implemented"));
    }
    
    private Long getCurrentUserId(Authentication auth) {
        return 1L; // Placeholder implementation
    }
}