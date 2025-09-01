package com.itasca.spoofing.service;


import com.itasca.spoofing.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProfileService {

    // Single Profile Operations
    SingleProfileDto createSingleProfile(SingleProfileDto profileDto);
    Optional<SingleProfileDto> getSingleProfile(String id);
    List<SingleProfileDto> getAllSingleProfiles();
    Page<SingleProfileDto> getSingleProfiles(Pageable pageable);
    SingleProfileDto updateSingleProfile(String id, SingleProfileDto profileDto);
    boolean deleteSingleProfile(String id);

    // Group Profile Operations
    GroupProfileDto createGroupProfile(GroupProfileDto profileDto);
    Optional<GroupProfileDto> getGroupProfile(String id);
    List<GroupProfileDto> getAllGroupProfiles();
    Page<GroupProfileDto> getGroupProfiles(Pageable pageable);
    GroupProfileDto updateGroupProfile(String id, GroupProfileDto profileDto);
    boolean deleteGroupProfile(String id);

    // Search and Filter Operations
    List<SingleProfileDto> searchSingleProfiles(String searchTerm);
    List<SingleProfileDto> getSingleProfilesByStatus(String status);
    List<SingleProfileDto> getSingleProfilesByOS(String operatingSystem);
    List<GroupProfileDto> searchGroupProfiles(String searchTerm);
    List<GroupProfileDto> getGroupProfilesByStatus(String status);

    // Business Logic Operations
    boolean activateProfile(String id, ProfileType profileType);
    boolean deactivateProfile(String id, ProfileType profileType);
    SingleProfileDto useProfile(String id);
    String getNextProfileFromGroup(String groupId);

    // Statistics and Analytics
    long getTotalSingleProfiles();
    long getTotalGroupProfiles();
    long getActiveProfilesCount();
    List<SingleProfileDto> getRecentlyUsedProfiles();

    // Bulk Operations
    List<SingleProfileDto> createSingleProfiles(List<SingleProfileDto> profiles);
    boolean deleteMultipleProfiles(List<String> ids, ProfileType profileType);
    boolean bulkStatusUpdate(List<String> ids, String status, ProfileType profileType);
}