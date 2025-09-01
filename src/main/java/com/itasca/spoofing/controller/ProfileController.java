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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/profiles")
@Tag(name = "Profile Management", description = "APIs for managing browser profiles")
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private ProfileAuditService auditService;

    @Autowired
    private ProfileStatsService statsService;

    @PostMapping("/single/test")
    public ResponseEntity<Map<String, Object>> testEndpoint(@RequestBody Map<String, Object> payload) {
        log.info("Test endpoint received payload: {}", payload);
        Map<String, Object> response = Map.of(
            "status", "success",
            "received", payload,
            "message", "Payload received successfully"
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/single")
    @Operation(summary = "Create a new single profile", description = "Creates a new browser profile with specified configuration")
    public ResponseEntity<?> createProfile(@RequestBody Map<String, Object> payload) {
        log.info("Received payload: {}", payload);
        
        try {
            // Map Python JSON to SingleProfileDto
            SingleProfileDto profileDto = SingleProfileDto.builder()
                .name((String) payload.get("name"))
                .description((String) payload.getOrDefault("description", ""))
                .operatingSystem((String) payload.getOrDefault("operating_system", "Windows"))
                .userAgent((String) payload.getOrDefault("user_agent", ""))
                .screenResolution((String) payload.getOrDefault("screen_resolution", "1920x1080")).webglVendor((String) payload.getOrDefault("webgl_vendor", "Google Inc."))
                .webglRenderer((String) payload.getOrDefault("webgl_renderer", ""))
                .hardwareConcurrency((Integer) payload.getOrDefault("hardware_concurrency", 8))
                .deviceMemory((Integer) payload.getOrDefault("device_memory", 8))
                .canvasFingerprint((Boolean) payload.getOrDefault("canvas_fingerprint", true))
                .webrtcEnabled((Boolean) payload.getOrDefault("webrtc_enabled", false))
                .javascriptEnabled((Boolean) payload.getOrDefault("javascript_enabled", true))
                .cookiesEnabled((Boolean) payload.getOrDefault("cookies_enabled", true))
                .geolocationEnabled((Boolean) payload.getOrDefault("geolocation_enabled", false))
                .doNotTrack((Boolean) payload.getOrDefault("do_not_track", true))
                .status((String) payload.getOrDefault("status", "Active"))
                .created((String) payload.getOrDefault("created", ""))
                .lastUsed((String) payload.getOrDefault("last_used", "Never"))
                .build();
            
            log.info("Mapped profile: {}", profileDto);
            
            SingleProfileDto createdProfile = profileService.createSingleProfile(profileDto);
            ProfileResponseDto response = ProfileResponseDto.success(createdProfile);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error creating single profile: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage(), "received", payload));
        }
    }

    @GetMapping("/single/{id}")
    @Operation(summary = "Get single profile by ID", description = "Retrieves a specific browser profile by its ID")
    public ResponseEntity<ProfileResponseDto> getProfile(
            @Parameter(description = "Profile ID") @PathVariable String id) {
        log.debug("Retrieving single profile: {}", id);

        return profileService.getSingleProfile(id)
                .map(profile -> ResponseEntity.ok(ProfileResponseDto.success(profile)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ProfileResponseDto.error("Profile not found with ID: " + id)));
    }

    @GetMapping("/single")
    @Operation(summary = "Get all single profiles", description = "Retrieves all browser profiles")
    public ResponseEntity<List<SingleProfileDto>> getAllProfiles() {
        log.debug("Retrieving all single profiles");

        List<SingleProfileDto> profiles = profileService.getAllSingleProfiles();
        return ResponseEntity.ok(profiles);
    }

    @GetMapping("/single/paged")
    @Operation(summary = "Get all single profiles with pagination", description = "Retrieves all browser profiles with pagination support")
    public ResponseEntity<Page<SingleProfileDto>> getAllProfilesPaged(
            @PageableDefault(size = 20) Pageable pageable) {
        log.debug("Retrieving single profiles with pagination: {}", pageable);

        Page<SingleProfileDto> profiles = profileService.getSingleProfiles(pageable);
        return ResponseEntity.ok(profiles);
    }

    @PutMapping("/single/{id}")
    @Operation(summary = "Update single profile", description = "Updates an existing browser profile")
    public ResponseEntity<ProfileResponseDto> updateProfile(
            @Parameter(description = "Profile ID") @PathVariable String id,
            @RequestBody SingleProfileDto profileDto) {
        log.info("Updating single profile: {}", id);

        try {
            SingleProfileDto updatedProfile = profileService.updateSingleProfile(id, profileDto);
            ProfileResponseDto response = ProfileResponseDto.success(updatedProfile);
            return ResponseEntity.ok(response);
        } catch (ProfileNotFoundException e) {
            ProfileResponseDto errorResponse = ProfileResponseDto.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            log.error("Error updating single profile {}: {}", id, e.getMessage(), e);
            ProfileResponseDto errorResponse = ProfileResponseDto.error("Failed to update profile: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @DeleteMapping("/single/{id}")
    @Operation(summary = "Delete single profile", description = "Deletes a browser profile permanently")
    public ResponseEntity<ProfileResponseDto> deleteProfile(
            @Parameter(description = "Profile ID") @PathVariable String id) {
        log.info("Deleting single profile: {}", id);

        try {
            profileService.deleteSingleProfile(id);
            ProfileResponseDto response = ProfileResponseDto.builder()
                    .status("success")
                    .message("Profile deleted successfully")
                    .id(id)
                    .build();
            return ResponseEntity.ok(response);
        } catch (ProfileNotFoundException e) {
            ProfileResponseDto errorResponse = ProfileResponseDto.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            log.error("Error deleting single profile {}: {}", id, e.getMessage(), e);
            ProfileResponseDto errorResponse = ProfileResponseDto.error("Failed to delete profile: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/single/search")
    @Operation(summary = "Search single profiles", description = "Search profiles by name")
    public ResponseEntity<List<SingleProfileDto>> searchProfiles(
            @Parameter(description = "Search term") @RequestParam String q) {
        log.debug("Searching single profiles with term: {}", q);

        List<SingleProfileDto> profiles = profileService.searchSingleProfiles(q);
        return ResponseEntity.ok(profiles);
    }

    @GetMapping("/single/filter/status/{status}")
    @Operation(summary = "Filter profiles by status", description = "Get profiles filtered by status")
    public ResponseEntity<List<SingleProfileDto>> getProfilesByStatus(
            @Parameter(description = "Profile status") @PathVariable String status) {
        log.debug("Filtering single profiles by status: {}", status);

        List<SingleProfileDto> profiles = profileService.getSingleProfilesByStatus(status);
        return ResponseEntity.ok(profiles);
    }

    @GetMapping("/filter/os/{os}")
    @Operation(summary = "Filter profiles by OS", description = "Get profiles filtered by operating system")
    public ResponseEntity<List<SingleProfileDto>> getProfilesByOS(
            @Parameter(description = "Operating system") @PathVariable String os) {
        log.debug("Filtering single profiles by OS: {}", os);

        List<SingleProfileDto> profiles = profileService.getSingleProfilesByOS(os);
        return ResponseEntity.ok(profiles);
    }

    @PostMapping("/{id}/use")
    @Operation(summary = "Use profile", description = "Mark a profile as used and update last used timestamp")
    public ResponseEntity<ProfileResponseDto> useProfile(
            @Parameter(description = "Profile ID") @PathVariable String id) {
        log.info("Using single profile: {}", id);

        try {
            SingleProfileDto usedProfile = profileService.useProfile(id);
            ProfileResponseDto response = ProfileResponseDto.success(usedProfile);
            return ResponseEntity.ok(response);
        } catch (ProfileNotFoundException e) {
            ProfileResponseDto errorResponse = ProfileResponseDto.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            log.error("Error using single profile {}: {}", id, e.getMessage(), e);
            ProfileResponseDto errorResponse = ProfileResponseDto.error("Failed to use profile: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/{id}/activate")
    @Operation(summary = "Activate profile", description = "Activate a profile")
    public ResponseEntity<ProfileResponseDto> activateProfile(
            @Parameter(description = "Profile ID") @PathVariable String id) {
        log.info("Activating single profile: {}", id);

        try {
            profileService.activateProfile(id, ProfileType.SINGLE);
            ProfileResponseDto response = ProfileResponseDto.builder()
                    .status("success")
                    .message("Profile activated successfully")
                    .id(id)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error activating single profile {}: {}", id, e.getMessage(), e);
            ProfileResponseDto errorResponse = ProfileResponseDto.error("Failed to activate profile: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate profile", description = "Deactivate a profile")
    public ResponseEntity<ProfileResponseDto> deactivateProfile(
            @Parameter(description = "Profile ID") @PathVariable @NotBlank String id) {
        log.info("Deactivating single profile: {}", id);

        try {
            profileService.deactivateProfile(id, ProfileType.SINGLE);
            ProfileResponseDto response = ProfileResponseDto.builder()
                    .status("success")
                    .message("Profile deactivated successfully")
                    .id(id)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error deactivating single profile {}: {}", id, e.getMessage(), e);
            ProfileResponseDto errorResponse = ProfileResponseDto.error("Failed to deactivate profile: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/{id}/stats")
    @Operation(summary = "Get profile statistics", description = "Get usage statistics for a profile")
    public ResponseEntity<Map<String, Object>> getProfileStats(
            @Parameter(description = "Profile ID") @PathVariable @NotBlank String id) {
        log.debug("Retrieving statistics for single profile: {}", id);

        try {
            Map<String, Object> stats = statsService.getProfilePerformanceReport(id);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error retrieving stats for profile {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve statistics: " + e.getMessage()));
        }
    }

    @PostMapping("/bulk")
    @Operation(summary = "Create multiple profiles", description = "Create multiple profiles in bulk")
    public ResponseEntity<List<ProfileResponseDto>> createBulkProfiles(
            @Valid @RequestBody List<SingleProfileDto> profiles) {
        log.info("Creating {} single profiles in bulk", profiles.size());

        try {
            List<SingleProfileDto> createdProfiles = profileService.createSingleProfiles(profiles);
            List<ProfileResponseDto> responses = createdProfiles.stream()
                    .map(ProfileResponseDto::success)
                    .toList();
            return ResponseEntity.status(HttpStatus.CREATED).body(responses);
        } catch (Exception e) {
            log.error("Error creating bulk profiles: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    List.of(ProfileResponseDto.error("Failed to create bulk profiles: " + e.getMessage()))
            );
        }
    }

    @DeleteMapping("/bulk")
    @Operation(summary = "Delete multiple profiles", description = "Delete multiple profiles in bulk")
    public ResponseEntity<ProfileResponseDto> deleteBulkProfiles(@RequestBody List<String> profileIds) {
        log.info("Deleting {} single profiles in bulk", profileIds.size());

        try {
            profileService.deleteMultipleProfiles(profileIds, ProfileType.SINGLE);
            ProfileResponseDto response = ProfileResponseDto.builder()
                    .status("success")
                    .message(profileIds.size() + " profiles deleted successfully")
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error deleting bulk profiles: {}", e.getMessage(), e);
            ProfileResponseDto errorResponse = ProfileResponseDto.error("Failed to delete bulk profiles: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}