package com.itasca.spoofing.controller;

import com.itasca.spoofing.exception.ProfileNotFoundException;
import com.itasca.spoofing.model.*;
import com.itasca.spoofing.service.ProfileService;
import com.itasca.spoofing.service.ProfileAuditService;
import com.itasca.spoofing.service.ProfileStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashSet;

@RestController
@RequestMapping("/api/profiles")
@Tag(name = "Group Profile Management", description = "APIs for managing group browser profiles")
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class GroupProfileController {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private ProfileAuditService auditService;

    @Autowired
    private ProfileStatsService statsService;

    @PostMapping("/group")
    @Operation(summary = "Create a new group profile", description = "Creates a new group profile with member profiles")
    public ResponseEntity<?> createGroupProfile(@RequestBody Map<String, Object> payload) {
        log.info("Received group profile payload: {}", payload);
        
        try {
            GroupProfileDto profileDto = GroupProfileDto.builder()
                .name((String) payload.get("name"))
                .description((String) payload.getOrDefault("description", ""))
                .selectionMode((String) payload.getOrDefault("selection_mode", "sequential"))
                .currentProfileIndex((Integer) payload.getOrDefault("current_profile_index", 0))
                .urlGroupId(payload.get("url_group_id") != null ? Long.valueOf(payload.get("url_group_id").toString()) : null)
                .status((String) payload.getOrDefault("status", "Active"))
                .timezone((String) payload.getOrDefault("timezone", "America/New_York"))
                .language((String) payload.getOrDefault("language", "en-US,en;q=0.9"))
                .created((String) payload.getOrDefault("created", ""))
                .lastUsed((String) payload.getOrDefault("last_used", "Never"))
                .build();
            
            // Handle proxy config if provided
            Map<String, Object> proxyConfigData = (Map<String, Object>) payload.get("proxy_config");
            if (proxyConfigData != null) {
                Integer endPort = (Integer) proxyConfigData.get("port_end");
                
                ProxyConfig proxyConfig = ProxyConfig.builder()
                    .proxyType((String) proxyConfigData.getOrDefault("proxy_type", "None"))
                    .host((String) proxyConfigData.getOrDefault("host", ""))
                    .port((Integer) proxyConfigData.getOrDefault("port", 8080))
                    .endPort(endPort)
                    .username((String) proxyConfigData.getOrDefault("username", ""))
                    .password((String) proxyConfigData.getOrDefault("password", ""))
                    .ipType(proxyConfigData.get("ip_type") != null ? IPType.fromValue((String) proxyConfigData.get("ip_type")) : null)
                    .fixedIp((String) proxyConfigData.get("fixed_ip"))
                    .country((String) proxyConfigData.getOrDefault("country", "US"))
                    .build();
                profileDto.setProxyConfig(proxyConfig);
            }
            
            List<String> memberIds = (List<String>) payload.get("member_profile_ids");
            if (memberIds != null) {
                profileDto.setMemberProfileIds(new HashSet<>(memberIds));
            }
            
            GroupProfileDto createdProfile = profileService.createGroupProfile(profileDto);
            ProfileResponseDto response = ProfileResponseDto.success(createdProfile);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error creating group profile: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage(), "received", payload));
        }
    }

    @GetMapping("/group/{id}")
    @Operation(summary = "Get group profile by ID", description = "Retrieves a specific group profile by its ID")
    public ResponseEntity<ProfileResponseDto> getProfile(
            @Parameter(description = "Group Profile ID") @PathVariable String id) {
        log.debug("Retrieving group profile: {}", id);

        return profileService.getGroupProfile(id)
                .map(profile -> ResponseEntity.ok(ProfileResponseDto.success(profile)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ProfileResponseDto.error("Group profile not found with ID: " + id)));
    }

    @GetMapping("/group")
    @Operation(summary = "Get all group profiles", description = "Retrieves all group profiles")
    public ResponseEntity<List<GroupProfileDto>> getAllGroupProfiles() {
        log.debug("Retrieving all group profiles");

        List<GroupProfileDto> profiles = profileService.getAllGroupProfiles();
        return ResponseEntity.ok(profiles);
    }

    @PutMapping("/group/{id}")
    @Operation(summary = "Update group profile", description = "Updates an existing group profile")
    public ResponseEntity<?> updateProfile(
            @Parameter(description = "Group Profile ID") @PathVariable String id,
            @RequestBody Map<String, Object> payload) {
        log.info("Updating group profile {}: {}", id, payload);

        try {
            GroupProfileDto profileDto = GroupProfileDto.builder()
                .id(id)
                .name((String) payload.get("name"))
                .description((String) payload.getOrDefault("description", ""))
                .selectionMode((String) payload.getOrDefault("selection_mode", "sequential"))
                .urlGroupId(payload.get("url_group_id") != null ? Long.valueOf(payload.get("url_group_id").toString()) : null)
                .timezone((String) payload.getOrDefault("timezone", "America/New_York"))
                .language((String) payload.getOrDefault("language", "en-US,en;q=0.9"))
                .status((String) payload.getOrDefault("status", "Active"))
                .build();
            
            // Handle proxy config if provided
            Map<String, Object> proxyConfigData = (Map<String, Object>) payload.get("proxy_config");
            if (proxyConfigData != null) {
                Integer endPort = (Integer) proxyConfigData.get("port_end");
                
                ProxyConfig proxyConfig = ProxyConfig.builder()
                    .proxyType((String) proxyConfigData.getOrDefault("proxy_type", "None"))
                    .host((String) proxyConfigData.getOrDefault("host", ""))
                    .port((Integer) proxyConfigData.getOrDefault("port", 8080))
                    .endPort(endPort)
                    .username((String) proxyConfigData.getOrDefault("username", ""))
                    .password((String) proxyConfigData.getOrDefault("password", ""))
                    .ipType(proxyConfigData.get("ip_type") != null ? IPType.fromValue((String) proxyConfigData.get("ip_type")) : null)
                    .fixedIp((String) proxyConfigData.get("fixed_ip"))
                    .country((String) proxyConfigData.getOrDefault("country", "US"))
                    .build();
                profileDto.setProxyConfig(proxyConfig);
            }
            
            List<String> memberIds = (List<String>) payload.get("member_profile_ids");
            if (memberIds != null) {
                profileDto.setMemberProfileIds(new HashSet<>(memberIds));
            }
            
            GroupProfileDto updatedProfile = profileService.updateGroupProfile(id, profileDto);
            ProfileResponseDto response = ProfileResponseDto.success(updatedProfile);
            return ResponseEntity.ok(response);
        } catch (ProfileNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating group profile {}: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage(), "received", payload));
        }
    }

    @DeleteMapping("/group/{id}")
    @Operation(summary = "Delete group profile", description = "Deletes a group profile permanently")
    public ResponseEntity<ProfileResponseDto> deleteProfile(
            @Parameter(description = "Group Profile ID") @PathVariable String id) {
        log.info("Deleting group profile: {}", id);

        try {
            profileService.deleteGroupProfile(id);
            ProfileResponseDto response = ProfileResponseDto.builder()
                    .status("success")
                    .message("Group profile deleted successfully")
                    .id(id)
                    .build();
            return ResponseEntity.ok(response);
        } catch (ProfileNotFoundException e) {
            ProfileResponseDto errorResponse = ProfileResponseDto.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            log.error("Error deleting group profile {}: {}", id, e.getMessage(), e);
            ProfileResponseDto errorResponse = ProfileResponseDto.error("Failed to delete group profile: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/group/{id}/next-profile")
    @Operation(summary = "Get next profile from group", description = "Gets the next profile ID from group based on selection mode")
    public ResponseEntity<Map<String, String>> getNextProfile(
            @Parameter(description = "Group Profile ID") @PathVariable String id) {
        log.info("Getting next profile from group: {}", id);

        try {
            String nextProfileId = profileService.getNextProfileFromGroup(id);
            if (nextProfileId != null) {
                return ResponseEntity.ok(Map.of(
                        "groupId", id,
                        "nextProfileId", nextProfileId,
                        "status", "success"
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                        "groupId", id,
                        "message", "No profiles available in group",
                        "status", "warning"
                ));
            }
        } catch (ProfileNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage(), "status", "error"));
        } catch (Exception e) {
            log.error("Error getting next profile from group {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get next profile: " + e.getMessage(), "status", "error"));
        }
    }
}