package com.itasca.spoofing.service;

import com.itasca.spoofing.model.ProfileType;
import com.itasca.spoofing.entity.ProfileAuditEntity;

import java.time.LocalDateTime;
import java.util.List;

public interface ProfileAuditService {

    void logProfileCreation(String profileId, ProfileType profileType);
    void logProfileUpdate(String profileId, ProfileType profileType);
    void logProfileDeletion(String profileId, ProfileType profileType);
    void logProfileUsage(String profileId, ProfileType profileType);
    void logProfileActivation(String profileId, ProfileType profileType);
    void logProfileDeactivation(String profileId, ProfileType profileType);

    List<ProfileAuditEntity> getProfileAuditHistory(String profileId);
    List<ProfileAuditEntity> getRecentAuditEntries(String profileId, int hours);
    List<ProfileAuditEntity> getAuditEntriesByDateRange(LocalDateTime start, LocalDateTime end);
    long getUsageCount(String profileId);
}