package com.itasca.spoofing.controller;

import com.itasca.spoofing.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@Tag(name = "User Profile", description = "APIs for user profile operations")
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserProfileController {

    @Autowired
    private UserService userService;

    @GetMapping("/assigned-profiles")
    @Operation(summary = "Get assigned profiles for current user", description = "Retrieves group profiles assigned to the currently logged-in user")
    public ResponseEntity<?> getAssignedProfiles() {
        try {
            java.util.List<com.itasca.spoofing.model.GroupProfileDto> assignedProfiles = userService.getCurrentUserAssignedProfiles();
            return ResponseEntity.ok(assignedProfiles);
        } catch (Exception e) {
            log.error("Error retrieving assigned profiles for current user: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/group/{groupId}/member-profiles")
    @Operation(summary = "Get member profiles of a group", description = "Retrieves all member profiles within a specific group")
    public ResponseEntity<?> getGroupMemberProfiles(@PathVariable String groupId) {
        try {
            java.util.List<com.itasca.spoofing.model.SingleProfileDto> memberProfiles = userService.getGroupMemberProfiles(groupId);
            return ResponseEntity.ok(memberProfiles);
        } catch (Exception e) {
            log.error("Error retrieving member profiles for group {}: {}", groupId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}