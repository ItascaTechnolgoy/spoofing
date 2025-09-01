package com.itasca.spoofing.repository;


import com.itasca.spoofing.entity.ProfileAuditEntity;
import com.itasca.spoofing.model.ProfileType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProfileAuditRepository extends JpaRepository<ProfileAuditEntity, Long> {

    /**
     * Find audit entries by profile ID
     */
    List<ProfileAuditEntity> findByProfileIdOrderByActionTimestampDesc(String profileId);

    /**
     * Find audit entries by action
     */
    List<ProfileAuditEntity> findByAction(String action);

    /**
     * Find audit entries by user ID
     */
    List<ProfileAuditEntity> findByUserIdOrderByActionTimestampDesc(String userId);

    /**
     * Find audit entries within date range
     */
    List<ProfileAuditEntity> findByActionTimestampBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find recent audit entries for a profile
     */
    @Query("SELECT a FROM ProfileAuditEntity a WHERE a.profileId = :profileId AND a.actionTimestamp >= :since ORDER BY a.actionTimestamp DESC")
    List<ProfileAuditEntity> findRecentAuditEntries(@Param("profileId") String profileId, @Param("since") LocalDateTime since);

    /**
     * Count actions by profile ID and action type
     */
    long countByProfileIdAndAction(String profileId, String action);

    /**
     * Find audit entries by profile type
     */
    List<ProfileAuditEntity> findByProfileType(ProfileType profileType);
}