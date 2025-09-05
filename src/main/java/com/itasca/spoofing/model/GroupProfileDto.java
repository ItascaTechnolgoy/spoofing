package com.itasca.spoofing.model;

import com.itasca.spoofing.entity.GroupType;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class GroupProfileDto {

    @NotBlank(message = "Group profile ID is required")
    private String id;

    @NotBlank(message = "Group profile name is required")
    @Size(min = 1, max = 255, message = "Group profile name must be between 1 and 255 characters")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    @Builder.Default
    private String description = "";

    @JsonProperty("profile_type")
    @Builder.Default
    private ProfileType profileType = ProfileType.GROUP;

    @JsonProperty("group_type")
    @Builder.Default
    private GroupType groupType = GroupType.CUSTOM;

    @JsonProperty("is_system_generated")
    @Builder.Default
    private Boolean isSystemGenerated = false;

    // Profile selection settings
    @JsonProperty("selection_mode")
    @Pattern(regexp = "^(random|sequential|weighted)$", message = "Selection mode must be random, sequential, or weighted")
    @Builder.Default
    private String selectionMode = "random";

    @JsonProperty("current_profile_index")
    @Min(value = 0, message = "Current profile index cannot be negative")
    @Builder.Default
    private Integer currentProfileIndex = 0;

    // Member profiles - now using profile objects instead of IDs
    @JsonProperty("member_profiles")
    @Valid
    @Builder.Default
    private Set<SingleProfileDto> memberProfiles = new HashSet<>();

    // Member profile IDs for API compatibility
    @JsonProperty("member_profile_ids")
    @Builder.Default
    private Set<String> memberProfileIds = new HashSet<>();

    // Proxy/IP configuration for the group
    @JsonProperty("proxy_config")
    @Valid
    @Builder.Default
    private ProxyConfig proxyConfig = new ProxyConfig();

    // Location-based settings (determined by proxy location)
    @Builder.Default
    private String timezone = "America/New_York";

    @Builder.Default
    private String language = "en-US,en;q=0.9";

    // URL group (single URL group per profile)
    @JsonProperty("url_group")
    @Valid
    private URLGroupDto urlGroup;

    @JsonProperty("url_group_id")
    private Long urlGroupId;

    // User assignments
    @JsonProperty("assigned_user_ids")
    @Builder.Default
    private Set<Long> assignedUserIds = new HashSet<>();

    @JsonProperty("assigned_users")
    @Builder.Default
    private Set<UserDto> assignedUsers = new HashSet<>();

    // Metadata
    @Pattern(regexp = "^(Active|Inactive|Suspended)$", message = "Status must be Active, Inactive, or Suspended")
    @Builder.Default
    private String status = "Active";

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Builder.Default
    private String created = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

    @JsonProperty("last_used")
    @Builder.Default
    private String lastUsed = "Never";

    @JsonProperty("max_concurrent_usage")
    @Min(value = 1, message = "Max concurrent usage must be at least 1")
    @Builder.Default
    private Integer maxConcurrentUsage = 1;

    @JsonProperty("current_active_sessions")
    @Min(value = 0, message = "Current active sessions cannot be negative")
    @Builder.Default
    private Integer currentActiveSessions = 0;

    @JsonProperty("default_for_profile_id")
    private String defaultForProfileId;

    // Utility methods

    /**
     * Get member profile count
     */
    public int getMemberCount() {
        return memberProfiles != null ? memberProfiles.size() : 0;
    }

    /**
     * Get active member profile count
     */
    public int getActiveMemberCount() {
        return (int) memberProfiles.stream()
                .filter(profile -> "Active".equals(profile.getStatus()))
                .count();
    }

    /**
     * Check if group is active
     */
    public boolean isActive() {
        return "Active".equals(status);
    }

    /**
     * Check if this is a default group
     */
    public boolean isDefaultGroup() {
        return groupType == GroupType.DEFAULT;
    }

    /**
     * Check if this is a custom group
     */
    public boolean isCustomGroup() {
        return groupType == GroupType.CUSTOM;
    }

    /**
     * Check if group can handle more concurrent sessions
     */
    public boolean canAcceptNewSession() {
        return currentActiveSessions < maxConcurrentUsage;
    }

    /**
     * Update last used timestamp
     */
    public void updateLastUsed() {
        this.lastUsed = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    /**
     * Sync member profile IDs from member profiles
     */
    public void syncMemberProfileIds() {
        this.memberProfileIds = memberProfiles.stream()
                .map(SingleProfileDto::getId)
                .collect(Collectors.toSet());
    }

    /**
     * Sync assigned user IDs from assigned users
     */
    public void syncAssignedUserIds() {
        this.assignedUserIds = assignedUsers.stream()
                .map(UserDto::getId)
                .collect(Collectors.toSet());
    }
}
