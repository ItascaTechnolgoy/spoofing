package com.itasca.spoofing.entity;




import lombok.*;
import com.itasca.spoofing.model.ProfileType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Entity
@Table(
        name = "single_profiles",
        indexes = {
                @Index(name = "idx_single_profile_name", columnList = "name"),
                @Index(name = "idx_single_profile_status", columnList = "status"),
                @Index(name = "idx_single_profile_created", columnList = "created_at"),
                @Index(name = "idx_single_profile_default_group", columnList = "default_group_id")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"urlGroups", "memberOfGroups", "defaultGroup"})
@EqualsAndHashCode(exclude = {"urlGroups", "memberOfGroups", "defaultGroup"}, callSuper = true)
public class SingleProfileEntity extends BaseEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", length = 1000)
    @Builder.Default
    private String description = "";

    @Column(name = "profile_type", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ProfileType profileType = ProfileType.SINGLE;

    // Browser settings
    @Column(name = "operating_system", length = 50, nullable = false)
    @Builder.Default
    private String operatingSystem = "Windows";

    @Column(name = "user_agent", length = 2000)
    @Builder.Default
    private String userAgent = "";

    @Column(name = "screen_resolution", length = 20)
    @Builder.Default
    private String screenResolution = "1920x1080";

    // Hardware fingerprinting
    @Column(name = "webgl_vendor", length = 255, nullable = false)
    @Builder.Default
    private String webglVendor = "Google Inc.";

    @Column(name = "webgl_renderer", length = 500)
    @Builder.Default
    private String webglRenderer = "";

    @Column(name = "hardware_concurrency", nullable = false)
    @Builder.Default
    private Integer hardwareConcurrency = 8;

    @Column(name = "device_memory", nullable = false)
    @Builder.Default
    private Integer deviceMemory = 8;

    // Privacy settings
    @Column(name = "canvas_fingerprint", nullable = false)
    @Builder.Default
    private Boolean canvasFingerprint = true;

    @Column(name = "webrtc_enabled", nullable = false)
    @Builder.Default
    private Boolean webrtcEnabled = false;

    @Column(name = "javascript_enabled", nullable = false)
    @Builder.Default
    private Boolean javascriptEnabled = true;

    @Column(name = "cookies_enabled", nullable = false)
    @Builder.Default
    private Boolean cookiesEnabled = true;

    @Column(name = "geolocation_enabled", nullable = false)
    @Builder.Default
    private Boolean geolocationEnabled = false;

    @Column(name = "do_not_track", nullable = false)
    @Builder.Default
    private Boolean doNotTrack = true;

    // URL management
    @OneToMany(
            mappedBy = "singleProfile",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true
    )
    @Builder.Default
    private List<URLGroupEntity> urlGroups = new ArrayList<>();

    @Column(name = "default_url_group", length = 255)
    private String defaultUrlGroup;

    // Default Group - Each profile has its own default group
    @OneToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true
    )
    @JoinColumn(
            name = "default_group_id",
            foreignKey = @ForeignKey(name = "fk_single_profile_default_group")
    )
    private GroupProfileEntity defaultGroup;

    // Many-to-Many: Profile can be member of multiple custom groups
    @ManyToMany(mappedBy = "memberProfiles", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<GroupProfileEntity> memberOfGroups = new HashSet<>();

    // Metadata
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private String status = "Active";

    @Column(name = "last_used", length = 50)
    @Builder.Default
    private String lastUsed = "Never";

    // Generated fingerprint stored as JSON
    @Column(name = "generated_fingerprint", columnDefinition = "TEXT")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> generatedFingerprint;

    // Helper methods for managing relationships

    /**
     * Add URL group to profile
     */
    public void addUrlGroup(URLGroupEntity urlGroup) {
        urlGroups.add(urlGroup);
        urlGroup.setSingleProfile(this);
    }

    /**
     * Remove URL group from profile
     */
    public void removeUrlGroup(URLGroupEntity urlGroup) {
        urlGroups.remove(urlGroup);
        urlGroup.setSingleProfile(null);
    }

    /**
     * Get URL group by name
     */
    public URLGroupEntity getUrlGroupByName(String name) {
        return urlGroups.stream()
                .filter(group -> group.getName().equals(name))
                .findFirst()
                .orElse(null);
    }



    /**
     * Check if profile is active
     */
    public boolean isActive() {
        return "Active".equals(status);
    }

    /**
     * Create default group for this profile
     */
    public GroupProfileEntity createDefaultGroup() {
        if (this.defaultGroup == null) {
            this.defaultGroup = GroupProfileEntity.builder()
                    .id(this.id + "_default")
                    .name(this.name + " - Default Group")
                    .description("Auto-generated default group for profile: " + this.name)
                    .profileType(ProfileType.GROUP)
                    .groupType(GroupType.DEFAULT)
                    .isSystemGenerated(true)
                    .memberProfiles(Set.of(this))
                    .status("Active")
                    .build();

            this.memberOfGroups.add(this.defaultGroup);
        }
        return this.defaultGroup;
    }

    /**
     * Join a custom group
     */
    public void joinGroup(GroupProfileEntity group) {
        if (group.getGroupType() == GroupType.CUSTOM) {
            this.memberOfGroups.add(group);
            group.getMemberProfiles().add(this);
        }
    }

    /**
     * Leave a custom group
     */
    public void leaveGroup(GroupProfileEntity group) {
        if (group.getGroupType() == GroupType.CUSTOM) {
            this.memberOfGroups.remove(group);
            group.getMemberProfiles().remove(this);
        }
    }

    /**
     * Get all groups this profile belongs to (default + custom)
     */
    public Set<GroupProfileEntity> getAllGroups() {
        Set<GroupProfileEntity> allGroups = new HashSet<>(memberOfGroups);
        if (defaultGroup != null) {
            allGroups.add(defaultGroup);
        }
        return allGroups;
    }
}