package com.itasca.spoofing.service;


import com.itasca.spoofing.model.GroupProfileDto;
import com.itasca.spoofing.model.SingleProfileDto;
import com.itasca.spoofing.model.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface GroupManagementService {

    // Group Operations
    GroupProfileDto createCustomGroup(GroupProfileDto groupDto);
    Optional<GroupProfileDto> getGroup(String groupId);
    Page<GroupProfileDto> getAllGroups(Pageable pageable);
    GroupProfileDto updateGroup(String groupId, GroupProfileDto groupDto);
    void deleteGroup(String groupId);
    GroupProfileDto getGroupWithMembers(String groupId);
    List<GroupProfileDto> getGroupsForUser(Long userId);
    List<GroupProfileDto> getCustomGroups();
    Page<GroupProfileDto> getCustomGroups(Pageable pageable);

    // Group Membership Management
    boolean addProfilesToGroup(String groupId, Set<String> profileIds);
    boolean removeProfilesFromGroup(String groupId, Set<String> profileIds);
    List<SingleProfileDto> getGroupMembers(String groupId);
    List<GroupProfileDto> getGroupsForProfile(String profileId);

    // User Assignment Management (Admin only)
    boolean assignGroupsToUser(Long userId, Set<String> groupIds);
    boolean removeGroupsFromUser(Long userId, Set<String> groupIds);
    List<UserDto> getGroupUsers(String groupId);
    boolean assignGroupToUsers(String groupId, Set<Long> userIds);

    // Profile Selection for Client
    SingleProfileDto getNextProfileFromGroup(String groupId, Long userId);
    SingleProfileDto selectSpecificProfileFromGroup(String groupId, String profileId, Long userId);
    List<SingleProfileDto> getAvailableProfilesInGroup(String groupId, Long userId);

    // Session Management
    boolean startGroupSession(String groupId, String profileId, Long userId);
    boolean endGroupSession(String groupId, String profileId, Long userId);
    int getActiveSessionsForGroup(String groupId);

    // Default Group Management
    GroupProfileDto createDefaultGroupForProfile(String profileId);
    GroupProfileDto getDefaultGroupForProfile(String profileId);
    boolean deleteDefaultGroupForProfile(String profileId);

    // Statistics and Analytics
    GroupProfileDto getMostUsedGroupForUser(Long userId);
    List<GroupProfileDto> getPopularGroups(int limit);
    Map<String, Object> getGroupUsageStatistics(String groupId);
}