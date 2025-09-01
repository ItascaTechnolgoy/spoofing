package com.itasca.spoofing.service.impl;

import com.itasca.spoofing.entity.UserEntity;
import com.itasca.spoofing.model.UserDto;
import com.itasca.spoofing.repository.UserRepository;
import com.itasca.spoofing.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.List;

@Service
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private com.itasca.spoofing.repository.GroupProfileRepository groupProfileRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> getUsers(Pageable pageable) {
        log.debug("Retrieving users with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<UserEntity> userEntities = userRepository.findAll(pageable);
        log.debug("Found {} users, total pages: {}, current page: {}", userEntities.getTotalElements(), userEntities.getTotalPages(), userEntities.getNumber());
        return userEntities.map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDto> getUserById(Long id) {
        log.debug("Retrieving user by id: {}", id);
        return userRepository.findById(id).map(this::convertToDto);
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        UserEntity entity = UserEntity.builder()
                .username(userDto.getUsername())
                .email(userDto.getEmail())
                .password(userDto.getPassword())
                .firstName(userDto.getFirstName())
                .lastName(userDto.getLastName())
                .status(userDto.getStatus())
                .build();
        UserEntity saved = userRepository.save(entity);
        return convertToDto(saved);
    }

    @Override
    public UserDto updateUser(Long id, UserDto userDto) {
        UserEntity entity = userRepository.findById(id).orElseThrow();
        entity.setUsername(userDto.getUsername());
        entity.setEmail(userDto.getEmail());
        entity.setFirstName(userDto.getFirstName());
        entity.setLastName(userDto.getLastName());
        entity.setStatus(userDto.getStatus());
        UserEntity updated = userRepository.save(entity);
        return convertToDto(updated);
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public UserDto assignProfilesToUser(Long userId, Set<String> profileIds) {
        UserEntity user = userRepository.findById(userId).orElseThrow();
        
        // Clear existing assignments
        user.getAssignedGroups().clear();
        
        // Find and assign new profiles
        if (profileIds != null && !profileIds.isEmpty()) {
            java.util.List<com.itasca.spoofing.entity.GroupProfileEntity> profiles = 
                groupProfileRepository.findAllById(profileIds);
            user.getAssignedGroups().addAll(profiles);
        }
        
        UserEntity saved = userRepository.save(user);
        return convertToDto(saved);
    }

    @Override
    public java.util.List<com.itasca.spoofing.model.GroupProfileDto> getAssignedProfiles(Long userId) {
        UserEntity user = userRepository.findById(userId).orElseThrow();
        return user.getAssignedGroups().stream()
                .map(this::convertGroupToDto)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public java.util.List<com.itasca.spoofing.model.GroupProfileDto> getUnassignedProfiles(Long userId) {
        UserEntity user = userRepository.findById(userId).orElseThrow();
        Set<String> assignedIds = user.getAssignedGroups().stream()
                .map(com.itasca.spoofing.entity.GroupProfileEntity::getId)
                .collect(java.util.stream.Collectors.toSet());
        
        return groupProfileRepository.findAll().stream()
                .filter(profile -> "Active".equals(profile.getStatus()))
                .filter(profile -> !assignedIds.contains(profile.getId()))
                .map(this::convertGroupToDto)
                .collect(java.util.stream.Collectors.toList());
    }

    private UserDto convertToDto(UserEntity entity) {
        Set<String> assignedGroupIds = entity.getAssignedGroups().stream()
                .map(group -> group.getId())
                .collect(java.util.stream.Collectors.toSet());
                
        return UserDto.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .email(entity.getEmail())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .status(entity.getStatus())
                .allRoles(entity.getRoles())
                .lastLogin(entity.getLastLogin())
                .assignedGroupIds(assignedGroupIds)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private com.itasca.spoofing.model.GroupProfileDto convertGroupToDto(com.itasca.spoofing.entity.GroupProfileEntity entity) {
        return com.itasca.spoofing.model.GroupProfileDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .profileType(entity.getProfileType())
                .groupType(entity.getGroupType())
                .isSystemGenerated(entity.getIsSystemGenerated())
                .selectionMode(entity.getSelectionMode())
                .currentProfileIndex(entity.getCurrentProfileIndex())
                .timezone(entity.getTimezone())
                .language(entity.getLanguage())
                .status(entity.getStatus())
                .lastUsed(entity.getLastUsed())
                .maxConcurrentUsage(entity.getMaxConcurrentUsage())
                .currentActiveSessions(entity.getCurrentActiveSessions())
                .build();
    }
}