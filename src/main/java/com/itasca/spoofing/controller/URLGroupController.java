package com.itasca.spoofing.controller;

import com.itasca.spoofing.model.URLGroupDto;
import com.itasca.spoofing.model.URLDto;
import com.itasca.spoofing.service.URLGroupService;
import com.itasca.spoofing.service.URLService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/url/url-groups")
@CrossOrigin(origins = "*", maxAge = 3600)
public class URLGroupController {

    @Autowired
    private URLGroupService urlGroupService;
    
    @Autowired
    private URLService urlService;

    @GetMapping
    public ResponseEntity<List<URLGroupDto>> getAllURLGroups() {
        List<URLGroupDto> urlGroups = urlGroupService.getAllURLGroups();
        return ResponseEntity.ok(urlGroups);
    }

    @GetMapping("/{id}")
    public ResponseEntity<URLGroupDto> getURLGroupById(@PathVariable Long id) {
        return urlGroupService.getURLGroupById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }



    @GetMapping("/{id}/available")
    public ResponseEntity<List<URLDto>> getAvailableUrls(@PathVariable Long id) {
        try {
            List<URLDto> availableUrls = urlGroupService.getAvailableUrlsForGroup(id);
            return ResponseEntity.ok(availableUrls);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<URLGroupDto> createURLGroup(@Valid @RequestBody URLGroupDto urlGroupDto) {
        URLGroupDto createdGroup = urlGroupService.createURLGroup(urlGroupDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdGroup);
    }

    @PutMapping("/{id}")
    public ResponseEntity<URLGroupDto> updateURLGroup(@PathVariable Long id, @Valid @RequestBody URLGroupDto urlGroupDto) {
        try {
            URLGroupDto updatedGroup = urlGroupService.updateURLGroup(id, urlGroupDto);
            return ResponseEntity.ok(updatedGroup);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteURLGroup(@PathVariable Long id) {
        urlGroupService.deleteURLGroup(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/assign-urls")
    public ResponseEntity<URLGroupDto> assignUrlsToGroup(@PathVariable Long id, @RequestBody Map<String, List<Long>> request) {
        try {
            List<Long> urlIds = request.get("url_ids");
            List<URLDto> urls = urlService.getAllUrls().stream()
                    .filter(url -> urlIds.contains(url.getId()))
                    .collect(Collectors.toList());
            URLGroupDto updatedGroup = urlGroupService.assignUrlsToGroup(id, urls);
            return ResponseEntity.ok(updatedGroup);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/add-url")
    public ResponseEntity<URLGroupDto> addUrlToGroup(@PathVariable Long id, @RequestBody URLDto url) {
        try {
            URLGroupDto updatedGroup = urlGroupService.addUrlToGroup(id, url);
            return ResponseEntity.ok(updatedGroup);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/remove-url")
    public ResponseEntity<URLGroupDto> removeUrlFromGroup(@PathVariable Long id, @RequestBody URLDto url) {
        try {
            URLGroupDto updatedGroup = urlGroupService.removeUrlFromGroup(id, url);
            return ResponseEntity.ok(updatedGroup);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}/urls/{urlId}")
    public ResponseEntity<URLGroupDto> removeUrlById(@PathVariable Long id, @PathVariable Long urlId) {
        try {
            URLDto url = urlService.getAllUrls().stream()
                    .filter(u -> u.getId().equals(urlId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("URL not found"));
            URLGroupDto updatedGroup = urlGroupService.removeUrlFromGroup(id, url);
            return ResponseEntity.ok(updatedGroup);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/default")
    public ResponseEntity<URLGroupDto> getDefaultUrlGroup() {
        try {
            URLGroupDto defaultGroup = urlGroupService.getDefaultUrlGroup();
            return ResponseEntity.ok(defaultGroup);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}