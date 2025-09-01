package com.itasca.spoofing.model;


import lombok.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class SingleProfileDto {

    // Basic info
    private String id;
    private String name;
    
    @Builder.Default
    private String description = "";

    @JsonProperty("profile_type")
    @Builder.Default
    private ProfileType profileType = ProfileType.SINGLE;

    // Browser settings
    @JsonProperty("operating_system")
    @Builder.Default
    private String operatingSystem = "Windows";

    @JsonProperty("user_agent")
    @Builder.Default
    private String userAgent = "";

    @JsonProperty("screen_resolution")
    @Builder.Default
    private String screenResolution = "1920x1080";

    // Location settings moved to GroupProfile

    // Hardware fingerprinting
    @JsonProperty("webgl_vendor")
    @Builder.Default
    private String webglVendor = "Google Inc.";

    @JsonProperty("webgl_renderer")
    @Builder.Default
    private String webglRenderer = "";

    @JsonProperty("hardware_concurrency")
    @Builder.Default
    private Integer hardwareConcurrency = 8;

    @JsonProperty("device_memory")
    @Builder.Default
    private Integer deviceMemory = 8;

    // Privacy settings
    @JsonProperty("canvas_fingerprint")
    @Builder.Default
    private Boolean canvasFingerprint = true;

    @JsonProperty("webrtc_enabled")
    @Builder.Default
    private Boolean webrtcEnabled = false;

    @JsonProperty("javascript_enabled")
    @Builder.Default
    private Boolean javascriptEnabled = true;

    @JsonProperty("cookies_enabled")
    @Builder.Default
    private Boolean cookiesEnabled = true;

    @JsonProperty("geolocation_enabled")
    @Builder.Default
    private Boolean geolocationEnabled = false;

    @JsonProperty("do_not_track")
    @Builder.Default
    private Boolean doNotTrack = true;

    // URL management
    @JsonProperty("url_groups")
    @Builder.Default
    private List<URLGroupDto> urlGroups = new ArrayList<>();

    @JsonProperty("default_url_group")
    private String defaultUrlGroup;

    // Metadata
    @Builder.Default
    private String status = "Active";

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Builder.Default
    private String created = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

    @JsonProperty("last_used")
    @Builder.Default
    private String lastUsed = "Never";

    @JsonProperty("generated_fingerprint")
    private Map<String, Object> generatedFingerprint;

    // Utility methods



    /**
     * Get URL group by name
     */
    public URLGroupDto getUrlGroupByName(String name) {
        return urlGroups.stream()
                .filter(group -> group.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    /**
     * Check if profile has URL groups
     */
    public boolean hasUrlGroups() {
        return urlGroups != null && !urlGroups.isEmpty();
    }

    /**
     * Get default URL group object
     */
    public URLGroupDto getDefaultUrlGroupObject() {
        if (defaultUrlGroup != null) {
            return getUrlGroupByName(defaultUrlGroup);
        }
        return null;
    }

    /**
     * Update last used timestamp
     */
    public void updateLastUsed() {
        this.lastUsed = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    /**
     * Check if profile is active
     */
    public boolean isActive() {
        return "Active".equals(status);
    }
}