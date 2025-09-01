package com.itasca.spoofing.service.impl;

import com.itasca.spoofing.model.UserDto;
import com.itasca.spoofing.entity.*;
import com.itasca.spoofing.repository.UserRepository;
import com.itasca.spoofing.service.UserRoleService;
import com.itasca.spoofing.service.ProfileAuditService;
import com.itasca.spoofing.exception.ProfileNotFoundException;
import com.itasca.spoofing.exception.UnauthorizedAccessException;
import com.itasca.spoofing.exception.ProfileValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class UserRoleServiceImpl implements UserRoleService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileAuditService auditService;

    @Override
    @CacheEvict(value = "users", key = "#userId")
    public boolean changeUserRole(Long userId, UserRole newRole, String changedBy) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ProfileNotFoundException("User not found: " + userId));

        UserEntity changer = userRepository.findByEmail(changedBy)
                .orElseThrow(() -> new ProfileNotFoundException("Changer not found: " + changedBy));

        if (!isValidRoleChange(changer.getId(), userId, newRole)) {
            throw new UnauthorizedAccessException("Insufficient permissions to change role to " + newRole);
        }

        user.getRoles().clear();
        user.getRoles().add(newRole);
        userRepository.save(user);
        return true;
    }

    @Override
    @CacheEvict(value = "users", key = "#userId")
    public boolean addAdditionalRole(Long userId, UserRole role, String changedBy) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ProfileNotFoundException("User not found: " + userId));

        if (user.hasRole(role)) {
            throw new ProfileValidationException("User already has role: " + role);
        }

        user.getRoles().add(role);
        userRepository.save(user);
        return true;
    }

    @Override
    @CacheEvict(value = "users", key = "#userId")
    public boolean removeAdditionalRole(Long userId, UserRole role, String changedBy) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ProfileNotFoundException("User not found: " + userId));

        if (!user.getRoles().contains(role)) {
            throw new ProfileValidationException("User does not have additional role: " + role);
        }

        user.getRoles().remove(role);
        userRepository.save(user);
        return true;
    }

    @Override
    @CacheEvict(value = "users", key = "#userId")
    public boolean assignManager(Long userId, Long managerId, String changedBy) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ProfileNotFoundException("User not found: " + userId));

        UserEntity manager = userRepository.findById(managerId)
                .orElseThrow(() -> new ProfileNotFoundException("Manager not found: " + managerId));

        if (
            manager.getRoles().contains(UserRole.ADMIN) &&
            !manager.getRoles().contains(UserRole.SUPER_ADMIN)) {
            throw new ProfileValidationException("Manager must have TEAM_LEAD role or higher");
        }

        user.setManager(manager);
        userRepository.save(user);
        return true;
    }

    @Override
    @CacheEvict(value = "users", key = "#userId")
    public boolean removeManager(Long userId, String changedBy) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ProfileNotFoundException("User not found: " + userId));
        
        user.setManager(null);
        userRepository.save(user);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "user_hierarchy", key = "#managerId + '_reports'")
    public List<UserDto> getDirectReports(Long managerId) {
        return new ArrayList<>();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getTeamMembers(Long teamLeadId) {
        UserEntity teamLead = userRepository.findById(teamLeadId)
                .orElseThrow(() -> new ProfileNotFoundException("Team lead not found: " + teamLeadId));

        if (
            teamLead.getRoles().contains(UserRole.ADMIN) &&
            !teamLead.getRoles().contains(UserRole.SUPER_ADMIN)) {
            throw new UnauthorizedAccessException("User is not a team lead");
        }

        return new ArrayList<>();
    }

    @Override
    @CacheEvict(value = "users", key = "#userId")
    public boolean assignUserToDepartment(Long userId, String department, String team, String changedBy) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ProfileNotFoundException("User not found: " + userId));

        user.setDepartment(department);
        user.setTeam(team);
        userRepository.save(user);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "department_users", key = "#department")
    public List<UserDto> getDepartmentUsers(String department) {
        return new ArrayList<>();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "team_users", key = "#team")
    public List<UserDto> getTeamUsers(String team) {
        return new ArrayList<>();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "user_permissions", key = "#userId")
    public boolean hasPermission(Long userId, String permission) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ProfileNotFoundException("User not found: " + userId));

        return user.getRoles().stream().anyMatch(role -> role.hasPermission(permission));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "user_permissions", key = "#userId + '_all'")
    public Set<String> getUserPermissions(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ProfileNotFoundException("User not found: " + userId));

        return user.getRoles().stream()
                .flatMap(role -> role.getAllPermissions().stream())
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canUserManageOtherUser(Long managerId, Long targetUserId) {
        UserEntity manager = userRepository.findById(managerId)
                .orElseThrow(() -> new ProfileNotFoundException("Manager not found: " + managerId));
        UserEntity target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ProfileNotFoundException("Target user not found: " + targetUserId));

        if (manager.hasRole(UserRole.ADMIN) || manager.hasRole(UserRole.SUPER_ADMIN)) {
            return true;
        }



        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getUsersByRole(UserRole role) {
        return userRepository.findByRolesContaining(role).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getUsersWithPermission(String permission) {
        return userRepository.findAll().stream()
                .filter(user -> user.getRoles().stream().anyMatch(r -> r.hasPermission(permission)))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UserRole, Long> getRoleDistribution() {
        Map<UserRole, Long> distribution = new HashMap<>();
        for (UserRole role : UserRole.values()) {
            distribution.put(role, (long) userRepository.findByRolesContaining(role).size());
        }
        return distribution;
    }

    @Override
    @CacheEvict(value = "users", allEntries = true)
    public boolean bulkRoleChange(List<Long> userIds, UserRole newRole, String changedBy) {
        for (Long userId : userIds) {
            changeUserRole(userId, newRole, changedBy);
        }
        return true;
    }

    @Override
    @CacheEvict(value = "users", allEntries = true)
    public boolean bulkDepartmentAssignment(List<Long> userIds, String department, String team, String changedBy) {
        for (Long userId : userIds) {
            assignUserToDepartment(userId, department, team, changedBy);
        }
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isValidRoleChange(Long changerId, Long targetUserId, UserRole newRole) {
        UserEntity changer = userRepository.findById(changerId)
                .orElseThrow(() -> new ProfileNotFoundException("Changer not found: " + changerId));

        if (changer.hasRole(UserRole.SUPER_ADMIN)) {
            return true;
        }

        if (changer.hasRole(UserRole.ADMIN)) {
            return newRole != UserRole.SUPER_ADMIN;
        }

        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserRole> getAvailableRolesForUser(Long changerId, Long targetUserId) {
        UserEntity changer = userRepository.findById(changerId)
                .orElseThrow(() -> new ProfileNotFoundException("Changer not found: " + changerId));

        List<UserRole> availableRoles = new ArrayList<>();

        if (changer.hasRole(UserRole.SUPER_ADMIN)) {
            availableRoles.addAll(Arrays.asList(UserRole.values()));
        } else if (changer.hasRole(UserRole.ADMIN)) {
            availableRoles.addAll(Arrays.asList(UserRole.USER,  UserRole.ADMIN));
        }

        return availableRoles;
    }

    private UserDto mapToDto(UserEntity user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setAllRoles(user.getRoles());
        dto.setPrimaryRole(user.getRoles().iterator().next());
        dto.setStatus(user.getStatus());
        dto.setDepartment(user.getDepartment());
        dto.setTeam(user.getTeam());
        dto.setLastLogin(user.getLastLogin());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        
        Set<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getAllPermissions().stream())
                .collect(Collectors.toSet());
        dto.setPermissions(permissions);
        
        if (user.getManager() != null) {
            dto.setManagerId(user.getManager().getId());
            dto.setManagerName(user.getManager().getFirstName() + " " + user.getManager().getLastName());
        }
        
        return dto;
    }
}