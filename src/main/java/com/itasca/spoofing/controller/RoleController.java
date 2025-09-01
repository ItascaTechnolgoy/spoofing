package com.itasca.spoofing.controller;

import com.itasca.spoofing.entity.UserRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/roles")
@Tag(name = "Role Management", description = "APIs for managing user roles")
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class RoleController {

    @GetMapping
    @Operation(summary = "Get all available roles", description = "Retrieves all system roles")
    public ResponseEntity<List<Map<String, Object>>> getAllRoles() {
        log.debug("Retrieving all available roles");
        
        List<Map<String, Object>> roles = Arrays.stream(UserRole.values())
                .map(role -> {
                    Map<String, Object> roleMap = new HashMap<>();
                    roleMap.put("name", role.name());
                    roleMap.put("displayName", role.name().replace("_", " "));
                    roleMap.put("description", getRoleDescription(role));
                    return roleMap;
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/names")
    @Operation(summary = "Get role names only", description = "Retrieves just the role names as strings")
    public ResponseEntity<List<String>> getRoleNames() {
        log.debug("Retrieving role names");
        
        List<String> roleNames = Arrays.stream(UserRole.values())
                .map(UserRole::name)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(roleNames);
    }

    private String getRoleDescription(UserRole role) {
        return role.getDescription();
    }
}