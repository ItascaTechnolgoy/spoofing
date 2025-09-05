package com.itasca.spoofing.service.impl;




import com.itasca.spoofing.model.*;
import com.itasca.spoofing.entity.*;
import com.itasca.spoofing.repository.*;
import com.itasca.spoofing.mapper.ProfileMapper;
import com.itasca.spoofing.service.GroupManagementService;
import com.itasca.spoofing.service.ProfileAuditService;
import com.itasca.spoofing.service.ProfileStatsService;
import com.itasca.spoofing.exception.ProfileNotFoundException;
import com.itasca.spoofing.exception.ProfileValidationException;
import com.itasca.spoofing.exception.UnauthorizedAccessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class GroupManagementServiceImpl implements GroupManagementService {

    @Autowired
    private GroupProfileRepository groupProfileRepository;

    @Autowired
    private SingleProfileRepository singleProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileMapper profileMapper;

    @Autowired
    private ProfileAuditService auditService;

    @Autowired
    private ProfileStatsService statsService;

    // ==================== GROUP OPERATIONS ====================

    @Override
    public GroupProfileDto createCustomGroup(GroupProfileDto groupDto) {
        log.info("Creating custom group: {}", groupDto.getName());

        if (groupDto.getGroupType() != GroupType.CUSTOM) {
            throw new ProfileValidationException("Only custom groups can be created through this method");
        }

        // Validate group data
        validateGroupProfile(groupDto);

        // Generate ID if not provided
        if (groupDto.getId() == null || groupDto.getId().isEmpty()) {
            groupDto.setId(UUID.randomUUID().toString());
        }

        // Check for duplicate name
        if (groupProfileRepository.existsByNameIgnoreCase(groupDto.getName())) {
            throw new ProfileValidationException("Group name already exists: " + groupDto.getName());
        }

        // Validate and load member profiles
        Set<SingleProfileEntity> memberProfiles = new HashSet<>();
        if (groupDto.getMemberProfileIds() != null && !groupDto.getMemberProfileIds().isEmpty()) {
            memberProfiles = validateAndLoadMemberProfiles(groupDto.getMemberProfileIds());
        }

        // Create entity
        GroupProfileEntity entity = GroupProfileEntity.builder()
                .id(groupDto.getId())
                .name(groupDto.getName())
                .description(groupDto.getDescription())
                .profileType(ProfileType.GROUP)
                .groupType(GroupType.CUSTOM)
                .isSystemGenerated(false)
                .selectionMode(groupDto.getSelectionMode())
                .currentProfileIndex(0)
                .memberProfiles(memberProfiles)
                .urlGroupId(groupDto.getUrlGroupId())
                .status(groupDto.getStatus())
                .maxConcurrentUsage(groupDto.getMaxConcurrentUsage())
                .currentActiveSessions(0)
                .build();

        // Add URL group if any
        if (groupDto.getUrlGroup() != null) {
            URLGroupEntity urlGroup = profileMapper.toEntity(groupDto.getUrlGroup());
            entity.setUrlGroup(urlGroup);
        }

        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        GroupProfileEntity savedEntity = groupProfileRepository.save(entity);

        // Create audit entry
        auditService.logProfileCreation(savedEntity.getId(), ProfileType.GROUP);

        log.info("Custom group created successfully with ID: {}", savedEntity.getId());
        return profileMapper.toDto(savedEntity);
    }

    @Override
    public Optional<GroupProfileDto> getGroup(String groupId) {
        return Optional.empty();
    }

    @Override
    public Page<GroupProfileDto> getAllGroups(Pageable pageable) {
        return null;
    }

    @Override
    public GroupProfileDto updateGroup(String groupId, GroupProfileDto groupDto) {
        return null;
    }

    @Override
    public void deleteGroup(String groupId) {

    }

    @Override
    @Transactional(readOnly = true)
    public GroupProfileDto getGroupWithMembers(String groupId) {
        log.debug("Retrieving group with members: {}", groupId);

        GroupProfileEntity entity = groupProfileRepository.findById(groupId)
                .orElseThrow(() -> new ProfileNotFoundException("Group not found: " + groupId));

        return profileMapper.toDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupProfileDto> getGroupsForUser(Long userId) {
        log.debug("Retrieving groups for user: {}", userId);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ProfileNotFoundException("User not found: " + userId));

        return user.getAssignedGroups().stream()
                .filter(group -> "Active".equals(group.getStatus()))
                .map(profileMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupProfileDto> getCustomGroups() {
        log.debug("Retrieving all custom groups");

        List<GroupProfileEntity> entities = groupProfileRepository.findByGroupType(GroupType.CUSTOM);
        return entities.stream()
                .map(profileMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GroupProfileDto> getCustomGroups(Pageable pageable) {
        log.debug("Retrieving custom groups with pagination: {}", pageable);

        Page<GroupProfileEntity> entityPage = groupProfileRepository.findByGroupType(GroupType.CUSTOM, pageable);
        return entityPage.map(profileMapper::toDto);
    }

    // ==================== GROUP MEMBERSHIP MANAGEMENT ====================

    @Override
    public boolean addProfilesToGroup(String groupId, Set<String> profileIds) {
        log.info("Adding {} profiles to group: {}", profileIds.size(), groupId);

        GroupProfileEntity group = groupProfileRepository.findById(groupId)
                .orElseThrow(() -> new ProfileNotFoundException("Group not found: " + groupId));

        if (group.getGroupType() != GroupType.CUSTOM) {
            throw new ProfileValidationException("Cannot add profiles to default group");
        }

        Set<SingleProfileEntity> profilesToAdd = validateAndLoadMemberProfiles(profileIds);

        // Add profiles to group
        profilesToAdd.forEach(group::addMemberProfile);

        group.setUpdatedAt(LocalDateTime.now());
        groupProfileRepository.save(group);

        // Create audit entries
        profileIds.forEach(profileId ->
                auditService.logProfileUpdate(profileId, ProfileType.SINGLE));
        auditService.logProfileUpdate(groupId, ProfileType.GROUP);

        log.info("Successfully added {} profiles to group: {}", profileIds.size(), groupId);
        return true;
    }

    @Override
    public boolean removeProfilesFromGroup(String groupId, Set<String> profileIds) {
        log.info("Removing {} profiles from group: {}", profileIds.size(), groupId);

        GroupProfileEntity group = groupProfileRepository.findById(groupId)
                .orElseThrow(() -> new ProfileNotFoundException("Group not found: " + groupId));

        if (group.getGroupType() != GroupType.CUSTOM) {
            throw new ProfileValidationException("Cannot remove profiles from default group");
        }

        Set<SingleProfileEntity> profilesToRemove = group.getMemberProfiles().stream()
                .filter(profile -> profileIds.contains(profile.getId()))
                .collect(Collectors.toSet());

        if (profilesToRemove.isEmpty()) {
            log.warn("No profiles found to remove from group: {}", groupId);
            return false;
        }

        // Remove profiles from group
        profilesToRemove.forEach(group::removeMemberProfile);

        group.setUpdatedAt(LocalDateTime.now());
        groupProfileRepository.save(group);

        // Create audit entries
        profileIds.forEach(profileId ->
                auditService.logProfileUpdate(profileId, ProfileType.SINGLE));
        auditService.logProfileUpdate(groupId, ProfileType.GROUP);

        log.info("Successfully removed {} profiles from group: {}", profilesToRemove.size(), groupId);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SingleProfileDto> getGroupMembers(String groupId) {
        log.debug("Retrieving members of group: {}", groupId);

        GroupProfileEntity group = groupProfileRepository.findById(groupId)
                .orElseThrow(() -> new ProfileNotFoundException("Group not found: " + groupId));

        return group.getMemberProfiles().stream()
                .map(profileMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupProfileDto> getGroupsForProfile(String profileId) {
        log.debug("Retrieving groups for profile: {}", profileId);

        SingleProfileEntity profile = singleProfileRepository.findById(profileId)
                .orElseThrow(() -> new ProfileNotFoundException("Profile not found: " + profileId));

        return profile.getAllGroups().stream()
                .map(profileMapper::toDto)
                .collect(Collectors.toList());
    }

    // ==================== USER ASSIGNMENT MANAGEMENT ====================

    @Override
    public boolean assignGroupsToUser(Long userId, Set<String> groupIds) {
        log.info("Assigning {} groups to user: {}", groupIds.size(), userId);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ProfileNotFoundException("User not found: " + userId));

        Set<GroupProfileEntity> groupsToAssign = groupIds.stream()
                .map(groupId -> groupProfileRepository.findById(groupId)
                        .orElseThrow(() -> new ProfileNotFoundException("Group not found: " + groupId)))
                .collect(Collectors.toSet());

        // Add groups to user
//        groupsToAssign.forEach(user::assignGroup);

        userRepository.save(user);

        // Create audit entries
        groupIds.forEach(groupId ->
                auditService.logProfileUpdate(groupId, ProfileType.GROUP));

        log.info("Successfully assigned {} groups to user: {}", groupIds.size(), userId);
        return true;
    }

    @Override
    public boolean removeGroupsFromUser(Long userId, Set<String> groupIds) {
        log.info("Removing {} groups from user: {}", groupIds.size(), userId);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ProfileNotFoundException("User not found: " + userId));

        Set<GroupProfileEntity> groupsToRemove = user.getAssignedGroups().stream()
                .filter(group -> groupIds.contains(group.getId()))
                .collect(Collectors.toSet());

        if (groupsToRemove.isEmpty()) {
            log.warn("No groups found to remove from user: {}", userId);
            return false;
        }

        // Remove groups from user
//        groupsToRemove.forEach(user::removeGroup);

        userRepository.save(user);

        // Create audit entries
        groupIds.forEach(groupId ->
                auditService.logProfileUpdate(groupId, ProfileType.GROUP));

        log.info("Successfully removed {} groups from user: {}", groupsToRemove.size(), userId);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getGroupUsers(String groupId) {
        log.debug("Retrieving users assigned to group: {}", groupId);

        GroupProfileEntity group = groupProfileRepository.findById(groupId)
                .orElseThrow(() -> new ProfileNotFoundException("Group not found: " + groupId));

        return group.getAssignedUsers().stream()
                .map(this::mapUserToDto)
                .collect(Collectors.toList());
    }

    @Override
    public boolean assignGroupToUsers(String groupId, Set<Long> userIds) {
        log.info("Assigning group {} to {} users", groupId, userIds.size());

        GroupProfileEntity group = groupProfileRepository.findById(groupId)
                .orElseThrow(() -> new ProfileNotFoundException("Group not found: " + groupId));

        Set<UserEntity> usersToAssign = userIds.stream()
                .map(userId -> userRepository.findById(userId)
                        .orElseThrow(() -> new ProfileNotFoundException("User not found: " + userId)))
                .collect(Collectors.toSet());

        // Add users to group
//        usersToAssign.forEach(user -> user.assignGroup(group));

        userRepository.saveAll(usersToAssign);

        // Create audit entry
        auditService.logProfileUpdate(groupId, ProfileType.GROUP);

        log.info("Successfully assigned group {} to {} users", groupId, userIds.size());
        return true;
    }

    // ==================== PROFILE SELECTION FOR CLIENT ====================

    @Override
    public SingleProfileDto getNextProfileFromGroup(String groupId, Long userId) {
        log.debug("Getting next profile from group: {} for user: {}", groupId, userId);

        // Verify user access
        UserEntity user = verifyUserGroupAccess(userId, groupId);

        GroupProfileEntity group = groupProfileRepository.findById(groupId)
                .orElseThrow(() -> new ProfileNotFoundException("Group not found: " + groupId));

        if (!group.canAcceptNewSession()) {
            throw new ProfileValidationException("Group has reached maximum concurrent usage limit");
        }

        SingleProfileEntity selectedProfile = group.getNextProfile();
        if (selectedProfile == null) {
            throw new ProfileNotFoundException("No active profiles available in group: " + groupId);
        }

        // Update group usage
        group.setLastUsed(LocalDateTime.now().toString());
        group.setUpdatedAt(LocalDateTime.now());
        groupProfileRepository.save(group);

        // Log usage
        auditService.logProfileUsage(groupId, ProfileType.GROUP);
        statsService.recordProfileUsage(groupId, ProfileType.GROUP);

        return profileMapper.toDto(selectedProfile);
    }

    @Override
    public SingleProfileDto selectSpecificProfileFromGroup(String groupId, String profileId, Long userId) {
        log.debug("Selecting specific profile: {} from group: {} for user: {}", profileId, groupId, userId);

        // Verify user access
        verifyUserGroupAccess(userId, groupId);

        GroupProfileEntity group = groupProfileRepository.findById(groupId)
                .orElseThrow(() -> new ProfileNotFoundException("Group not found: " + groupId));

        SingleProfileEntity selectedProfile = group.getMemberProfiles().stream()
                .filter(profile -> profile.getId().equals(profileId) && "Active".equals(profile.getStatus()))
                .findFirst()
                .orElseThrow(() -> new ProfileNotFoundException("Profile not found or not active in group: " + profileId));

        if (!group.canAcceptNewSession()) {
            throw new ProfileValidationException("Group has reached maximum concurrent usage limit");
        }

        // Update group usage
        group.setLastUsed(LocalDateTime.now().toString());
        group.setUpdatedAt(LocalDateTime.now());
        groupProfileRepository.save(group);

        // Log usage
        auditService.logProfileUsage(groupId, ProfileType.GROUP);
        auditService.logProfileUsage(profileId, ProfileType.SINGLE);
        statsService.recordProfileUsage(groupId, ProfileType.GROUP);
        statsService.recordProfileUsage(profileId, ProfileType.SINGLE);

        return profileMapper.toDto(selectedProfile);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SingleProfileDto> getAvailableProfilesInGroup(String groupId, Long userId) {
        log.debug("Getting available profiles in group: {} for user: {}", groupId, userId);

        // Verify user access
        verifyUserGroupAccess(userId, groupId);

        GroupProfileEntity group = groupProfileRepository.findById(groupId)
                .orElseThrow(() -> new ProfileNotFoundException("Group not found: " + groupId));

        return group.getMemberProfiles().stream()
                .filter(profile -> "Active".equals(profile.getStatus()))
                .map(profileMapper::toDto)
                .collect(Collectors.toList());
    }

    // ==================== SESSION MANAGEMENT ====================

    @Override
    public boolean startGroupSession(String groupId, String profileId, Long userId) {
        log.info("Starting group session - Group: {}, Profile: {}, User: {}", groupId, profileId, userId);

        verifyUserGroupAccess(userId, groupId);

        GroupProfileEntity group = groupProfileRepository.findById(groupId)
                .orElseThrow(() -> new ProfileNotFoundException("Group not found: " + groupId));

        if (!group.canAcceptNewSession()) {
            throw new ProfileValidationException("Group has reached maximum concurrent usage limit");
        }

        group.incrementActiveSession();
        group.setUpdatedAt(LocalDateTime.now());
        groupProfileRepository.save(group);

        // Log session start
        auditService.logProfileUsage(groupId, ProfileType.GROUP);
        if (profileId != null) {
            auditService.logProfileUsage(profileId, ProfileType.SINGLE);
        }

        return true;
    }

    @Override
    public boolean endGroupSession(String groupId, String profileId, Long userId) {
        log.info("Ending group session - Group: {}, Profile: {}, User: {}", groupId, profileId, userId);

        GroupProfileEntity group = groupProfileRepository.findById(groupId)
                .orElseThrow(() -> new ProfileNotFoundException("Group not found: " + groupId));

        group.decrementActiveSession();
        group.setUpdatedAt(LocalDateTime.now());
        groupProfileRepository.save(group);

        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public int getActiveSessionsForGroup(String groupId) {
        GroupProfileEntity group = groupProfileRepository.findById(groupId)
                .orElseThrow(() -> new ProfileNotFoundException("Group not found: " + groupId));

        return group.getCurrentActiveSessions();
    }

    // ==================== DEFAULT GROUP MANAGEMENT ====================

    @Override
    public GroupProfileDto createDefaultGroupForProfile(String profileId) {
        log.info("Creating default group for profile: {}", profileId);

        SingleProfileEntity profile = singleProfileRepository.findById(profileId)
                .orElseThrow(() -> new ProfileNotFoundException("Profile not found: " + profileId));

        if (profile.getDefaultGroup() != null) {
            throw new ProfileValidationException("Default group already exists for profile: " + profileId);
        }

        GroupProfileEntity defaultGroup = profile.createDefaultGroup();
        profile.setUpdatedAt(LocalDateTime.now());

        singleProfileRepository.save(profile);

        log.info("Default group created for profile: {}", profileId);
        return profileMapper.toDto(defaultGroup);
    }

    @Override
    @Transactional(readOnly = true)
    public GroupProfileDto getDefaultGroupForProfile(String profileId) {
        log.debug("Retrieving default group for profile: {}", profileId);

        SingleProfileEntity profile = singleProfileRepository.findById(profileId)
                .orElseThrow(() -> new ProfileNotFoundException("Profile not found: " + profileId));

        if (profile.getDefaultGroup() == null) {
            throw new ProfileNotFoundException("No default group found for profile: " + profileId);
        }

        return profileMapper.toDto(profile.getDefaultGroup());
    }

    @Override
    public boolean deleteDefaultGroupForProfile(String profileId) {
        log.info("Deleting default group for profile: {}", profileId);

        SingleProfileEntity profile = singleProfileRepository.findById(profileId)
                .orElseThrow(() -> new ProfileNotFoundException("Profile not found: " + profileId));

        if (profile.getDefaultGroup() == null) {
            return false;
        }

        // Remove the default group
        GroupProfileEntity defaultGroup = profile.getDefaultGroup();
        profile.setDefaultGroup(null);
        profile.setUpdatedAt(LocalDateTime.now());

        singleProfileRepository.save(profile);
        groupProfileRepository.delete(defaultGroup);

        log.info("Default group deleted for profile: {}", profileId);
        return true;
    }

    // ==================== STATISTICS AND ANALYTICS ====================

    @Override
    @Transactional(readOnly = true)
    public GroupProfileDto getMostUsedGroupForUser(Long userId) {
        log.debug("Getting most used group for user: {}", userId);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ProfileNotFoundException("User not found: " + userId));

        // This would require additional statistics tracking
        // For now, return the first assigned group
        return user.getAssignedGroups().stream()
                .filter(group -> "Active".equals(group.getStatus()))
                .findFirst()
                .map(profileMapper::toDto)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupProfileDto> getPopularGroups(int limit) {
        log.debug("Getting {} most popular groups", limit);

        // This would require usage statistics
        // For now, return active groups ordered by member count
        List<GroupProfileEntity> groups = groupProfileRepository.findByStatus("Active");

        return groups.stream()
                .filter(group -> group.getGroupType() == GroupType.CUSTOM)
                .sorted((g1, g2) -> Integer.compare(g2.getMemberCount(), g1.getMemberCount()))
                .limit(limit)
                .map(profileMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getGroupUsageStatistics(String groupId) {
        log.debug("Getting usage statistics for group: {}", groupId);

        GroupProfileEntity group = groupProfileRepository.findById(groupId)
                .orElseThrow(() -> new ProfileNotFoundException("Group not found: " + groupId));

        Map<String, Object> stats = new HashMap<>();
        stats.put("groupId", groupId);
        stats.put("groupName", group.getName());
        stats.put("groupType", group.getGroupType());
        stats.put("memberCount", group.getMemberCount());
        stats.put("activeMemberCount", group.getActiveMemberCount());
        stats.put("assignedUserCount", group.getAssignedUsers().size());
        stats.put("maxConcurrentUsage", group.getMaxConcurrentUsage());
        stats.put("currentActiveSessions", group.getCurrentActiveSessions());
        stats.put("lastUsed", group.getLastUsed());
        stats.put("status", group.getStatus());

        return stats;
    }

    // ==================== HELPER METHODS ====================

    private UserEntity verifyUserGroupAccess(Long userId, String groupId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ProfileNotFoundException("User not found: " + userId));

//        if (!user.hasAccessToGroup(groupId)) {
//            throw new UnauthorizedAccessException("User does not have access to group: " + groupId);
//        }

        return user;
    }

    private Set<SingleProfileEntity> validateAndLoadMemberProfiles(Set<String> profileIds) {
        Set<SingleProfileEntity> profiles = new HashSet<>();

        for (String profileId : profileIds) {
            SingleProfileEntity profile = singleProfileRepository.findById(profileId)
                    .orElseThrow(() -> new ProfileNotFoundException("Profile not found: " + profileId));
            profiles.add(profile);
        }

        return profiles;
    }

    private void validateGroupProfile(GroupProfileDto groupDto) {
        if (groupDto.getName() == null || groupDto.getName().trim().isEmpty()) {
            throw new ProfileValidationException("Group name is required");
        }

        if (groupDto.getMaxConcurrentUsage() == null || groupDto.getMaxConcurrentUsage() < 1) {
            throw new ProfileValidationException("Max concurrent usage must be at least 1");
        }
    }

    private UserDto mapUserToDto(UserEntity user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
//                .fullName(user.getFullName())
                .allRoles(user.getRoles())
                .status(user.getStatus())
                .lastLogin(user.getLastLogin())
//                .emailVerified(user.getEmailVerified())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}