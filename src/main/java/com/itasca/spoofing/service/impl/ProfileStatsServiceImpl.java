package com.itasca.spoofing.service.impl;


import com.itasca.spoofing.model.ProfileType;
import com.itasca.spoofing.entity.ProfileStatsEntity;
import com.itasca.spoofing.repository.ProfileStatsRepository;
import com.itasca.spoofing.service.ProfileStatsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class ProfileStatsServiceImpl implements ProfileStatsService {

    @Autowired
    private ProfileStatsRepository statsRepository;

    @Override
    public void recordProfileUsage(String profileId, ProfileType profileType) {
        ProfileStatsEntity stats = getOrCreateTodayStats(profileId, profileType);
        stats.setUsageCount(stats.getUsageCount() + 1);
        statsRepository.save(stats);
        log.debug("Recorded usage for profile: {} ({})", profileId, profileType);
    }

    @Override
    public void recordSuccessfulSession(String profileId, ProfileType profileType, int durationMinutes, int urlsVisited) {
        ProfileStatsEntity stats = getOrCreateTodayStats(profileId, profileType);
        stats.setSuccessCount(stats.getSuccessCount() + 1);
        statsRepository.save(stats);
        log.debug("Recorded successful session for profile: {} (duration: {}min, URLs: {})",
                profileId, durationMinutes, urlsVisited);
    }

    @Override
    public void recordFailedSession(String profileId, ProfileType profileType) {
        ProfileStatsEntity stats = getOrCreateTodayStats(profileId, profileType);
        stats.setFailureCount(stats.getFailureCount() + 1);
        statsRepository.save(stats);
        log.debug("Recorded failed session for profile: {} ({})", profileId, profileType);
    }

    @Override
    public void recordProxyFailure(String profileId, ProfileType profileType) {
        ProfileStatsEntity stats = getOrCreateTodayStats(profileId, profileType);
        stats.setFailureCount(stats.getFailureCount() + 1);
        statsRepository.save(stats);
        log.debug("Recorded proxy failure for profile: {} ({})", profileId, profileType);
    }

    @Override
    public void recordFingerprintDetection(String profileId, ProfileType profileType) {
        ProfileStatsEntity stats = getOrCreateTodayStats(profileId, profileType);
        stats.setFailureCount(stats.getFailureCount() + 1);
        statsRepository.save(stats);
        log.debug("Recorded fingerprint detection for profile: {} ({})", profileId, profileType);
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileStatsEntity getTodayStats(String profileId) {
        return statsRepository.findByProfileIdAndDate(profileId, LocalDate.now()).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfileStatsEntity> getMonthStats(String profileId) {
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        return statsRepository.findByProfileIdAndDateBetween(profileId, startOfMonth, LocalDate.now());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfileStatsEntity> getDateRangeStats(String profileId, LocalDate startDate, LocalDate endDate) {
        return statsRepository.findByProfileIdAndDateBetween(profileId, startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getProfilePerformanceReport(String profileId) {
        List<ProfileStatsEntity> monthStats = getMonthStats(profileId);

        Map<String, Object> report = new HashMap<>();

        if (monthStats.isEmpty()) {
            report.put("totalUsage", 0);
            report.put("totalSessions", 0);
            report.put("successRate", 0.0);
            report.put("averageSessionDuration", 0.0);
            report.put("totalUrlsVisited", 0);
            report.put("proxyFailures", 0);
            report.put("fingerprintDetections", 0);
            return report;
        }

        int totalUsage = monthStats.stream().mapToInt(ProfileStatsEntity::getUsageCount).sum();
        int totalSuccessful = monthStats.stream().mapToInt(ProfileStatsEntity::getSuccessCount).sum();
        int totalFailed = monthStats.stream().mapToInt(ProfileStatsEntity::getFailureCount).sum();
        int totalSessions = totalSuccessful + totalFailed;
        int totalDuration = 0; // Simplified
        int totalUrls = 0; // Simplified
        int totalProxyFailures = totalFailed;
        int totalFingerprintDetections = 0; // Simplified

        double successRate = totalSessions > 0 ? (double) totalSuccessful / totalSessions * 100 : 0.0;
        double avgSessionDuration = totalSuccessful > 0 ? (double) totalDuration / totalSuccessful : 0.0;

        report.put("totalUsage", totalUsage);
        report.put("totalSessions", totalSessions);
        report.put("successfulSessions", totalSuccessful);
        report.put("failedSessions", totalFailed);
        report.put("successRate", Math.round(successRate * 100.0) / 100.0);
        report.put("averageSessionDuration", Math.round(avgSessionDuration * 100.0) / 100.0);
        report.put("totalUrlsVisited", totalUrls);
        report.put("proxyFailures", totalProxyFailures);
        report.put("fingerprintDetections", totalFingerprintDetections);

        return report;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfileStatsEntity> getTopPerformingProfiles(LocalDate since, int limit) {
        return statsRepository.findByDateAfter(since).stream().limit(limit).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfileStatsEntity> getProblemsProfiles(LocalDate since, double failureThreshold) {
        return statsRepository.findByDateAfter(since).stream()
                .filter(stats -> stats.getFailureCount() > stats.getSuccessCount() * failureThreshold)
                .collect(Collectors.toList());
    }

    @Override
    public void cleanupOldStats(int daysToKeep) {
        LocalDate cutoffDate = LocalDate.now().minusDays(daysToKeep);
        statsRepository.deleteByDateBefore(cutoffDate);
        log.info("Cleaned up statistics older than {} days", daysToKeep);
    }

    private ProfileStatsEntity getOrCreateTodayStats(String profileId, ProfileType profileType) {
        Optional<ProfileStatsEntity> existingStats = statsRepository.findByProfileIdAndDate(profileId, LocalDate.now());

        if (existingStats.isPresent()) {
            return existingStats.get();
        }

        ProfileStatsEntity newStats = ProfileStatsEntity.createStats(profileId, profileType);
        return statsRepository.save(newStats);
    }
}