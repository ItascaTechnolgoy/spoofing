package com.itasca.spoofing.repository;


import com.itasca.spoofing.entity.ProfileStatsEntity;
import com.itasca.spoofing.model.ProfileType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProfileStatsRepository extends JpaRepository<ProfileStatsEntity, Long> {

    Optional<ProfileStatsEntity> findByProfileIdAndDate(String profileId, LocalDate date);
    List<ProfileStatsEntity> findByProfileIdAndDateBetween(String profileId, LocalDate startDate, LocalDate endDate);
    List<ProfileStatsEntity> findByDateAfter(LocalDate date);
    void deleteByDateBefore(LocalDate cutoffDate);
    List<ProfileStatsEntity> findByProfileIdAndDateBetweenOrderByDateDesc(String profileId, LocalDate startDate, LocalDate endDate);
    List<ProfileStatsEntity> findByDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT s FROM ProfileStatsEntity s WHERE s.date >= :since ORDER BY s.usageCount DESC")
    List<ProfileStatsEntity> findTopProfilesByUsage(@Param("since") LocalDate since);

    @Query("SELECT s FROM ProfileStatsEntity s WHERE s.date >= :since AND (s.failureCount * 100.0 / NULLIF(s.successCount + s.failureCount, 0)) > :failureThreshold")
    List<ProfileStatsEntity> findProfilesWithHighFailureRate(@Param("since") LocalDate since, @Param("failureThreshold") double failureThreshold);

    @Query("SELECT SUM(s.usageCount), SUM(s.successCount), SUM(s.failureCount) FROM ProfileStatsEntity s WHERE s.date BETWEEN :startDate AND :endDate")
    Object[] getTotalStatistics(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    default Optional<ProfileStatsEntity> findTodayStats(String profileId) {
        return findByProfileIdAndDate(profileId, LocalDate.now());
    }

    default List<ProfileStatsEntity> findMonthStats(String profileId) {
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        return findByProfileIdAndDateBetweenOrderByDateDesc(profileId, startOfMonth, now);
    }
}