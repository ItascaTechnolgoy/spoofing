package com.itasca.spoofing.controller;

import com.itasca.spoofing.model.URLDto;
import com.itasca.spoofing.service.URLService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/url/urls")
@CrossOrigin(origins = "*", maxAge = 3600)
public class URLController {

    @Autowired
    private URLService urlService;

    @GetMapping
    public ResponseEntity<List<URLDto>> getAllUrls() {
        List<URLDto> urls = urlService.getAllUrls();
        return ResponseEntity.ok(urls);
    }

    @GetMapping("/available")
    public ResponseEntity<List<URLDto>> getAvailableUrls() {
        List<URLDto> availableUrls = urlService.getAvailableUrls();
        return ResponseEntity.ok(availableUrls);
    }

    @PostMapping
    public ResponseEntity<URLDto> createUrl(@Valid @RequestBody URLDto urlDto) {
        URLDto createdUrl = urlService.createUrl(urlDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUrl);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUrl(@PathVariable Long id) {
        urlService.deleteUrl(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/default")
    public ResponseEntity<URLDto> getDefaultUrl() {
        try {
            URLDto defaultUrl = urlService.getDefaultUrl();
            return ResponseEntity.ok(defaultUrl);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}