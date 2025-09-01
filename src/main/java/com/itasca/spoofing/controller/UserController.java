package com.itasca.spoofing.controller;

import com.itasca.spoofing.model.UserDto;
import com.itasca.spoofing.entity.UserRole;
import com.itasca.spoofing.service.UserService;
import com.itasca.spoofing.service.UserRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "APIs for managing users")
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRoleService userRoleService;

    @PostMapping
    @Operation(summary = "Create user", description = "Creates a new user")
    public ResponseEntity<?> createUser(@RequestBody Map<String, Object> payload) {
        try {
            UserDto userDto = UserDto.builder()
                    .username((String) payload.get("username"))
                    .email((String) payload.get("email"))
                    .password((String) payload.get("password"))
                    .firstName((String) payload.get("first_name"))
                    .lastName((String) payload.get("last_name"))
                    .status((String) payload.getOrDefault("status", "active"))
                    .build();
            
            UserDto created = userService.createUser(userDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

        @GetMapping
        @Operation(summary = "Get all users", description = "Retrieves all users with pagination")
        public ResponseEntity<?> getUsers(
                @RequestParam(defaultValue = "1") int page,
                @RequestParam(defaultValue = "20") int size) {
            try {
                // Convert 1-based to 0-based pagination, ensure minimum page is 1
                int adjustedPage = Math.max(1, page) - 1;

                Pageable pageable = org.springframework.data.domain.PageRequest.of(adjustedPage, size);
                Page<UserDto> users = userService.getUsers(pageable);

                return ResponseEntity.ok(Map.of(
                    "users", users.getContent(),
                    "totalElements", users.getTotalElements(),
                    "totalPages", users.getTotalPages(),
                    "currentPage", users.getNumber() + 1,
                    "size", users.getSize(),
                    "hasNext", users.hasNext(),
                    "hasPrevious", users.hasPrevious()
                ));
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
            }
        }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieves a user by ID")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(user -> ResponseEntity.ok(user))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Updates an existing user")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        try {
            log.info("Updating user {}: {}", id, payload);
            
            // Handle assigned_profiles if present
            if (payload.containsKey("assignedProfiles")) {
                java.util.List<String> profileList = (java.util.List<String>) payload.get("assigned_profiles");
                Set<String> profileIds = new HashSet<>(profileList);
                UserDto updated = userService.assignProfilesToUser(id, profileIds);
                return ResponseEntity.ok(updated);
            }
            
            // Handle regular user updates
            UserDto userDto = UserDto.builder()
                    .username((String) payload.get("username"))
                    .email((String) payload.get("email"))
                    .firstName((String) payload.get("first_name"))
                    .lastName((String) payload.get("last_name"))
                    .status((String) payload.get("status"))
                    .build();
            
            UserDto updated = userService.updateUser(id, userDto);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error updating user {}: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Deletes a user")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/assign-profiles")
    @Operation(summary = "Assign profiles to user", description = "Assigns profile groups to a user")
    public ResponseEntity<?> assignProfiles(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        try {
            log.info("Assigning profiles to user {}: {}", id, payload);

            if (payload.containsKey("profileIds")) {
                java.util.List<String> profileList = (java.util.List<String>) payload.get("profileIds");
                Set<String> profileIds = new HashSet<>(profileList);
                UserDto updated = userService.assignProfilesToUser(id, profileIds);
                return ResponseEntity.ok(updated);
            }

            return ResponseEntity.ok(Map.of(
                "message", "Profiles assigned successfully",
                "userId", id,
                "assignedProfiles", payload.get("assignedProfiles")
            ));
        } catch (Exception e) {
            log.error("Error assigning profiles to user {}: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/eligible-for-profiles")
    @Operation(summary = "Get users eligible for profiles", description = "Retrieves users with USER role only")
    public ResponseEntity<?> getEligibleUsers() {
        try {
            java.util.List<UserDto> eligibleUsers = userRoleService.getUsersByRole(UserRole.USER);
            log.info("Eligible users response: {}", eligibleUsers);
            return ResponseEntity.ok(eligibleUsers);
        } catch (Exception e) {
            log.error("Error retrieving eligible users: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{user_id}/assigned-profiles")
    @Operation(summary = "Get assigned profiles for user", description = "Retrieves group profiles assigned to a specific user")
    public ResponseEntity<?> getAssignedProfiles(@PathVariable("user_id") Long userId) {
        try {
            java.util.List<com.itasca.spoofing.model.GroupProfileDto> assignedProfiles = userService.getAssignedProfiles(userId);
            return ResponseEntity.ok(assignedProfiles);
        } catch (Exception e) {
            log.error("Error retrieving assigned profiles for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{user_id}/unassigned-profiles")
    @Operation(summary = "Get unassigned profiles for user", description = "Retrieves group profiles not assigned to a specific user")
    public ResponseEntity<?> getUnassignedProfiles(@PathVariable("user_id") Long userId) {
        try {
            java.util.List<com.itasca.spoofing.model.GroupProfileDto> unassignedProfiles = userService.getUnassignedProfiles(userId);
            return ResponseEntity.ok(unassignedProfiles);
        } catch (Exception e) {
            log.error("Error retrieving unassigned profiles for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}