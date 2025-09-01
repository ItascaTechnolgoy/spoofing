package com.itasca.spoofing.controller;

import com.itasca.spoofing.model.*;
import com.itasca.spoofing.service.GroupManagementService;
import com.itasca.spoofing.exception.UnauthorizedAccessException;
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
@RequestMapping("/api/groups")
@Validated
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class GroupManagementController {

    @Autowired
    private GroupManagementService groupManagementService;

    @PostMapping("/custom")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<GroupProfileDto> createCustomGroup(@Valid @RequestBody GroupProfileDto groupDto) {
        log.info("Creating custom group: {}", groupDto.getName());
        
        try {
            GroupProfileDto createdGroup = groupManagementService.createCustomGroup(groupDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdGroup);
        } catch (Exception e) {
            log.error("Error creating custom group: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping
    public ResponseEntity<Page<GroupProfileDto>> getAllGroups(@PageableDefault(size = 20) Pageable pageable) {
        log.debug("Retrieving all groups with pagination: {}", pageable);
        
        Page<GroupProfileDto> groups = groupManagementService.getAllGroups(pageable);
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GroupProfileDto> getGroup(@PathVariable @NotBlank String id) {
        log.debug("Retrieving group: {}", id);
        
        return groupManagementService.getGroup(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<GroupProfileDto> updateGroup(
            @PathVariable @NotBlank String id,
            @Valid @RequestBody GroupProfileDto groupDto) {
        log.info("Updating group: {}", id);
        
        try {
            GroupProfileDto updatedGroup = groupManagementService.updateGroup(id, groupDto);
            return ResponseEntity.ok(updatedGroup);
        } catch (Exception e) {
            log.error("Error updating group {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteGroup(@PathVariable @NotBlank String id) {
        log.info("Deleting group: {}", id);
        
        try {
            groupManagementService.deleteGroup(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error deleting group {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}