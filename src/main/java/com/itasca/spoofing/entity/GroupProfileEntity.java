package com.itasca.spoofing.entity;


import lombok.*;
import com.itasca.spoofing.model.ProfileType;

import jakarta.persistence.*;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Entity
@Table(
        name = "group_profiles",
        indexes = {
                @Index(name = "idx_group_profile_name", columnList = "name"),
                @Index(name = "idx_group_profile_status", columnList = "status"),
                @Index(name = "idx_group_profile_created", columnList = "created_at"),
                @Index(name = "idx_group_profile_type", columnList = "group_type"),
                @Index(name = "idx_group_profile_system", columnList = "is_system_generated")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"urlGroup", "memberProfiles", "assignedUsers", "defaultForProfile"})
@EqualsAndHashCode(exclude = {"urlGroup", "memberProfiles", "assignedUsers", "defaultForProfile"}, callSuper = true)
public class GroupProfileEntity extends BaseEntity {

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
    private ProfileType profileType = ProfileType.GROUP;

    @Column(name = "group_type", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private GroupType groupType = GroupType.CUSTOM;

    @Column(name = "is_system_generated", nullable = false)
    @Builder.Default
    private Boolean isSystemGenerated = false;

    // Profile selection settings
    @Column(name = "selection_mode", length = 20, nullable = false)
    @Builder.Default
    private String selectionMode = "random";

    @Column(name = "current_profile_index", nullable = false)
    @Builder.Default
    private Integer currentProfileIndex = 0;

    // Many-to-Many: Group contains multiple profiles, profiles can be in multiple groups
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "group_profile_members",
            joinColumns = @JoinColumn(name = "group_profile_id", foreignKey = @ForeignKey(name = "fk_group_members_group_id")),
            inverseJoinColumns = @JoinColumn(name = "single_profile_id", foreignKey = @ForeignKey(name = "fk_group_members_profile_id"))
    )
    @Builder.Default
    private Set<SingleProfileEntity> memberProfiles = new HashSet<>();

    // Proxy/IP configuration for the group
    @Embedded
    @Builder.Default
    private ProxyConfigEntity proxyConfig = new ProxyConfigEntity();

    // Location-based settings (determined by proxy location)
    @Column(name = "timezone", length = 50, nullable = false)
    @Builder.Default
    private String timezone = "America/New_York";

    @Column(name = "language", length = 100, nullable = false)
    @Builder.Default
    private String language = "en-US,en;q=0.9";

    // URL group (single URL group per profile)
    @OneToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true
    )
    @JoinColumn(name = "url_group_id")
    private URLGroupEntity urlGroup;

    @Column(name = "url_group_id", insertable = false, updatable = false)
    private Long urlGroupId;

    // Admin assigns groups to users
    @ManyToMany(mappedBy = "assignedGroups", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<UserEntity> assignedUsers = new HashSet<>();

    // Back reference to profile if this is a default group
    @OneToOne(mappedBy = "defaultGroup", fetch = FetchType.LAZY)
    private SingleProfileEntity defaultForProfile;

    // Metadata
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private String status = "Active";

    @Column(name = "last_used", length = 50)
    @Builder.Default
    private String lastUsed = "Never";

    @Column(name = "max_concurrent_usage", nullable = false)
    @Builder.Default
    private Integer maxConcurrentUsage = 1;

    @Column(name = "current_active_sessions", nullable = false)
    @Builder.Default
    private Integer currentActiveSessions = 0;

    // Helper methods for managing relationships

    /**
     * Set URL group for profile
     */
    public void setUrlGroup(URLGroupEntity urlGroup) {
        this.urlGroup = urlGroup;
        if (urlGroup != null) {
            this.urlGroupId = urlGroup.getId();
        }
    }

    /**
     * Add member profile to group
     */
    public void addMemberProfile(SingleProfileEntity profile) {
        memberProfiles.add(profile);
        profile.getMemberOfGroups().add(this);
    }

    /**
     * Remove member profile from group
     */
    public void removeMemberProfile(SingleProfileEntity profile) {
        memberProfiles.remove(profile);
        profile.getMemberOfGroups().remove(this);
    }

    // Business logic methods for profile selection

    /**
     * Get next profile based on selection mode
     */
    public SingleProfileEntity getNextProfile() {
        if (memberProfiles.isEmpty()) {
            return null;
        }

        List<SingleProfileEntity> activeProfiles = memberProfiles.stream()
                .filter(profile -> "Active".equals(profile.getStatus()))
                .collect(Collectors.toList());

        if (activeProfiles.isEmpty()) {
            return null;
        }

        switch (selectionMode.toLowerCase()) {
            case "sequential":
                return getSequentialProfile(activeProfiles);
            case "weighted":
                // For weighted selection, you might need additional weight data
                // For now, fall back to random
                return getRandomProfile(activeProfiles);
            case "random":
            default:
                return getRandomProfile(activeProfiles);
        }
    }

    /**
     * Get random profile from active profiles
     */
    private SingleProfileEntity getRandomProfile(List<SingleProfileEntity> activeProfiles) {
        Random random = new Random();
        int index = random.nextInt(activeProfiles.size());
        return activeProfiles.get(index);
    }

    /**
     * Get sequential profile from active profiles
     */
    private SingleProfileEntity getSequentialProfile(List<SingleProfileEntity> activeProfiles) {
        SingleProfileEntity profile = activeProfiles.get(currentProfileIndex % activeProfiles.size());
        currentProfileIndex = (currentProfileIndex + 1) % activeProfiles.size();
        return profile;
    }

    /**
     * Reset profile selection index
     */
    public void resetProfileSelection() {
        currentProfileIndex = 0;
    }

    /**
     * Get URL group
     */
    public URLGroupEntity getUrlGroup() {
        return urlGroup;
    }

    /**
     * Check if group is active
     */
    public boolean isActive() {
        return "Active".equals(status);
    }

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
     * Check if user has access to this group
     */
    public boolean isAccessibleByUser(UserEntity user) {
        return assignedUsers.contains(user);
    }

    /**
     * Check if group can handle more concurrent sessions
     */
    public boolean canAcceptNewSession() {
        return currentActiveSessions < maxConcurrentUsage;
    }

    /**
     * Increment active session count
     */
    public void incrementActiveSession() {
        if (canAcceptNewSession()) {
            currentActiveSessions++;
        }
    }

    /**
     * Decrement active session count
     */
    public void decrementActiveSession() {
        if (currentActiveSessions > 0) {
            currentActiveSessions--;
        }
    }
}
