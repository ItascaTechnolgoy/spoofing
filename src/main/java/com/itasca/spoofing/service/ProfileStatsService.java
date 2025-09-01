package com.itasca.spoofing.service;


import com.itasca.spoofing.model.ProfileType;
import com.itasca.spoofing.entity.ProfileStatsEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ProfileStatsService {

    void recordProfileUsage(String profileId, ProfileType profileType);
    void recordSuccessfulSession(String profileId, ProfileType profileType, int durationMinutes, int urlsVisited);
    void recordFailedSession(String profileId, ProfileType profileType);
    void recordProxyFailure(String profileId, ProfileType profileType);
    void recordFingerprintDetection(String profileId, ProfileType profileType);

    ProfileStatsEntity getTodayStats(String profileId);
    List<ProfileStatsEntity> getMonthStats(String profileId);
    List<ProfileStatsEntity> getDateRangeStats(String profileId, LocalDate startDate, LocalDate endDate);

    Map<String, Object> getProfilePerformanceReport(String profileId);
    List<ProfileStatsEntity> getTopPerformingProfiles(LocalDate since, int limit);
    List<ProfileStatsEntity> getProblemsProfiles(LocalDate since, double failureThreshold);

    void cleanupOldStats(int daysToKeep);
}
