package com.itasca.spoofing.entity;

import lombok.*;
import com.itasca.spoofing.model.ProfileType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "profile_audit")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileAuditEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "profile_id", nullable = false)
    private String profileId;

    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "action_timestamp", nullable = false)
    @Builder.Default
    private LocalDateTime actionTimestamp = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "profile_type")
    private ProfileType profileType;

    @Column(name = "changes", columnDefinition = "TEXT")
    private String changes;

    public static ProfileAuditEntity createAudit(String profileId, ProfileType profileType, String userId) {
        return ProfileAuditEntity.builder()
                .profileId(profileId)
                .profileType(profileType)
                .userId(userId)
                .action("CREATE")
                .build();
    }

    public static ProfileAuditEntity updateAudit(String profileId, ProfileType profileType, String userId, String changes) {
        return ProfileAuditEntity.builder()
                .profileId(profileId)
                .profileType(profileType)
                .userId(userId)
                .action("UPDATE")
                .changes(changes)
                .build();
    }
}