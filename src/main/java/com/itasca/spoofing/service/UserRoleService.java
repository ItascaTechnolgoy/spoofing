package com.itasca.spoofing.service;


import com.itasca.spoofing.model.UserDto;
import com.itasca.spoofing.model.RoleChangeRequestDto;
import com.itasca.spoofing.entity.UserRole;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface UserRoleService {

    // Role assignment and management
    boolean changeUserRole(Long userId, UserRole newRole, String changedBy);
    boolean addAdditionalRole(Long userId, UserRole role, String changedBy);
    boolean removeAdditionalRole(Long userId, UserRole role, String changedBy);

    // User hierarchy management
    boolean assignManager(Long userId, Long managerId, String changedBy);
    boolean removeManager(Long userId, String changedBy);
    List<UserDto> getDirectReports(Long managerId);
    List<UserDto> getTeamMembers(Long teamLeadId);

    // Department and team management
    boolean assignUserToDepartment(Long userId, String department, String team, String changedBy);
    List<UserDto> getDepartmentUsers(String department);
    List<UserDto> getTeamUsers(String team);

    // Permission checking
    boolean hasPermission(Long userId, String permission);
    Set<String> getUserPermissions(Long userId);
    boolean canUserManageOtherUser(Long managerId, Long targetUserId);

    // Role validation and queries
    List<UserDto> getUsersByRole(UserRole role);
    List<UserDto> getUsersWithPermission(String permission);
    Map<UserRole, Long> getRoleDistribution();

    // Bulk operations
    boolean bulkRoleChange(List<Long> userIds, UserRole newRole, String changedBy);
    boolean bulkDepartmentAssignment(List<Long> userIds, String department, String team, String changedBy);

    // Role hierarchy validation
    boolean isValidRoleChange(Long currentUserId, Long targetUserId, UserRole newRole);
    List<UserRole> getAvailableRolesForUser(Long currentUserId, Long targetUserId);
}