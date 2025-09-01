package com.itasca.spoofing.entity;

import lombok.*;
import com.itasca.spoofing.model.ProfileType;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "profile_stats")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileStatsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "profile_id", nullable = false)
    private String profileId;

    @Enumerated(EnumType.STRING)
    @Column(name = "profile_type")
    private ProfileType profileType;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "usage_count")
    @Builder.Default
    private Integer usageCount = 0;

    @Column(name = "success_count")
    @Builder.Default
    private Integer successCount = 0;

    @Column(name = "failure_count")
    @Builder.Default
    private Integer failureCount = 0;

    public static ProfileStatsEntity createStats(String profileId, ProfileType profileType) {
        return ProfileStatsEntity.builder()
                .profileId(profileId)
                .profileType(profileType)
                .date(LocalDate.now())
                .build();


    }
}