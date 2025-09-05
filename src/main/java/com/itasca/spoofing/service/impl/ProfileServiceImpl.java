package com.itasca.spoofing.service.impl;


import com.itasca.spoofing.exception.ProfileNotFoundException;
import com.itasca.spoofing.exception.ProfileValidationException;
import com.itasca.spoofing.model.*;
import com.itasca.spoofing.entity.*;
import com.itasca.spoofing.repository.*;
import com.itasca.spoofing.mapper.ProfileMapper;
import com.itasca.spoofing.service.ProfileService;
import com.itasca.spoofing.service.ProfileAuditService;
import com.itasca.spoofing.service.ProfileStatsService;
import com.itasca.spoofing.exception.UnauthorizedAccessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class ProfileServiceImpl implements ProfileService {

    @Autowired
    private SingleProfileRepository singleProfileRepository;

    @Autowired
    private GroupProfileRepository groupProfileRepository;

    @Autowired
    private ProfileMapper profileMapper;

    @Autowired
    private ProfileAuditService auditService;

    @Autowired
    private ProfileStatsService statsService;
    
    @Autowired
    private URLGroupRepository urlGroupRepository;

    // ==================== SINGLE PROFILE OPERATIONS ====================

    @Override
    public SingleProfileDto createSingleProfile(SingleProfileDto profileDto) {
        log.info("Creating single profile: {}", profileDto.getName());

        validateSingleProfile(profileDto);

        // Always generate new ID for security
        profileDto.setId(UUID.randomUUID().toString());

        // Check for duplicate name
        if (singleProfileRepository.existsByNameIgnoreCase(profileDto.getName())) {
            throw new ProfileValidationException("Profile name already exists: " + profileDto.getName());
        }

        // Set default values
        if (profileDto.getStatus() == null) {
            profileDto.setStatus("Active");
        }

        // Convert DTO to Entity and save
        SingleProfileEntity entity = profileMapper.toEntity(profileDto);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        SingleProfileEntity savedEntity = singleProfileRepository.save(entity);

        // Create audit entry
        auditService.logProfileCreation(savedEntity.getId(), ProfileType.SINGLE);

        log.info("Single profile created successfully with ID: {}", savedEntity.getId());
        return profileMapper.toDto(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SingleProfileDto> getSingleProfile(String id) {
        log.debug("Retrieving single profile with ID: {}", id);

        Optional<SingleProfileEntity> entity = singleProfileRepository.findById(id);
        if (entity.isPresent()) {
            return Optional.of(profileMapper.toDto(entity.get()));
        }

        log.warn("Single profile not found with ID: {}", id);
        return Optional.empty();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SingleProfileDto> getAllSingleProfiles() {
        log.debug("Retrieving all single profiles");

        List<SingleProfileEntity> entities = singleProfileRepository.findAll();
        return entities.stream()
                .map(profileMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SingleProfileDto> getSingleProfiles(Pageable pageable) {
        log.debug("Retrieving single profiles with pagination: {}", pageable);

        Page<SingleProfileEntity> entityPage = singleProfileRepository.findAll(pageable);
        return entityPage.map(profileMapper::toDto);
    }

    @Override
    public SingleProfileDto updateSingleProfile(String id, SingleProfileDto profileDto) {
        log.info("Updating single profile with ID: {}", id);

        SingleProfileEntity existingEntity = singleProfileRepository.findById(id)
                .orElseThrow(() -> new ProfileNotFoundException("Single profile not found with ID: " + id));

        validateSingleProfile(profileDto);

        // Check for duplicate name (excluding current profile)
        if (!existingEntity.getName().equalsIgnoreCase(profileDto.getName()) &&
                singleProfileRepository.existsByNameIgnoreCase(profileDto.getName())) {
            throw new ProfileValidationException("Profile name already exists: " + profileDto.getName());
        }

        // Update entity fields
        updateSingleProfileEntity(existingEntity, profileDto);
        existingEntity.setUpdatedAt(LocalDateTime.now());

        SingleProfileEntity updatedEntity = singleProfileRepository.save(existingEntity);

        // Create audit entry
        auditService.logProfileUpdate(updatedEntity.getId(), ProfileType.SINGLE);

        log.info("Single profile updated successfully: {}", id);
        return profileMapper.toDto(updatedEntity);
    }

    @Override
    public boolean deleteSingleProfile(String id) {
        log.info("Deleting single profile with ID: {}", id);

        if (!singleProfileRepository.existsById(id)) {
            throw new ProfileNotFoundException("Single profile not found with ID: " + id);
        }

        // Create audit entry before deletion
        auditService.logProfileDeletion(id, ProfileType.SINGLE);

        // Remove from any group profiles
        List<GroupProfileEntity> groupsWithMember = groupProfileRepository.findGroupsContainingProfile(id);
//        for (GroupProfileEntity group : groupsWithMember) {
//            group.removeMemberProfile(id);
//            groupProfileRepository.save(group);
//        }

        singleProfileRepository.deleteById(id);

        log.info("Single profile deleted successfully: {}", id);
        return true;
    }

    // ==================== GROUP PROFILE OPERATIONS ====================

    @Override
    public GroupProfileDto createGroupProfile(GroupProfileDto profileDto) {
        log.info("Creating group profile: {}", profileDto.getName());

        validateGroupProfile(profileDto);

        // Always generate new ID for security
        profileDto.setId(UUID.randomUUID().toString());

        // Check for duplicate name
        if (groupProfileRepository.existsByNameIgnoreCase(profileDto.getName())) {
            throw new ProfileValidationException("Group profile name already exists: " + profileDto.getName());
        }

        // Validate member profiles exist
        validateMemberProfiles(profileDto.getMemberProfileIds());

        // Set default values
        if (profileDto.getStatus() == null) {
            profileDto.setStatus("Active");
        }

        // Convert DTO to Entity and save
        GroupProfileEntity entity = profileMapper.toEntity(profileDto);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        
        // Assign default URLGroup if none specified
        assignUrlGroupToEntity(entity, profileDto.getUrlGroupId());

        GroupProfileEntity savedEntity = groupProfileRepository.save(entity);

        // Create audit entry
        auditService.logProfileCreation(savedEntity.getId(), ProfileType.GROUP);

        log.info("Group profile created successfully with ID: {}", savedEntity.getId());
        return profileMapper.toDto(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GroupProfileDto> getGroupProfile(String id) {
        log.debug("Retrieving group profile with ID: {}", id);

        Optional<GroupProfileEntity> entity = groupProfileRepository.findByIdWithMembers(id);
        if (entity.isPresent()) {
            return Optional.of(profileMapper.toDto(entity.get()));
        }

        log.warn("Group profile not found with ID: {}", id);
        return Optional.empty();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupProfileDto> getAllGroupProfiles() {
        log.debug("Retrieving all group profiles");

        List<GroupProfileEntity> entities = groupProfileRepository.findAll();
        return entities.stream()
                .map(profileMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GroupProfileDto> getGroupProfiles(Pageable pageable) {
        log.debug("Retrieving group profiles with pagination: {}", pageable);

        Page<GroupProfileEntity> entityPage = groupProfileRepository.findAll(pageable);
        return entityPage.map(profileMapper::toDto);
    }

    @Override
    public GroupProfileDto updateGroupProfile(String id, GroupProfileDto profileDto) {
        log.info("Updating group profile with ID: {}", id);

        GroupProfileEntity existingEntity = groupProfileRepository.findById(id)
                .orElseThrow(() -> new ProfileNotFoundException("Group profile not found with ID: " + id));

        validateGroupProfile(profileDto);
        validateMemberProfiles(profileDto.getMemberProfileIds());

        updateGroupProfileEntity(existingEntity, profileDto);
        existingEntity.setUpdatedAt(LocalDateTime.now());

        groupProfileRepository.save(existingEntity);
        auditService.logProfileUpdate(existingEntity.getId(), ProfileType.GROUP);

        // Fetch the complete updated entity with all relationships
        GroupProfileEntity completeUpdatedEntity = groupProfileRepository.findByIdWithMembers(id)
                .orElseThrow(() -> new ProfileNotFoundException("Group profile not found after update: " + id));

        log.info("Group profile updated successfully: {}", id);
        return profileMapper.toDto(completeUpdatedEntity);
    }

    @Override
    public boolean deleteGroupProfile(String id) {
        if (!groupProfileRepository.existsById(id)) {
            throw new ProfileNotFoundException("Group profile not found with ID: " + id);
        }
        auditService.logProfileDeletion(id, ProfileType.GROUP);
        groupProfileRepository.deleteById(id);
        return true;
    }

    @Override
    public List<SingleProfileDto> searchSingleProfiles(String searchTerm) {
        return singleProfileRepository.findByNameContainingIgnoreCase(searchTerm)
                .stream().map(profileMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<SingleProfileDto> getSingleProfilesByStatus(String status) {
        return singleProfileRepository.findByStatus(status)
                .stream().map(profileMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<SingleProfileDto> getSingleProfilesByOS(String operatingSystem) {
        return singleProfileRepository.findByOperatingSystem(operatingSystem)
                .stream().map(profileMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<GroupProfileDto> searchGroupProfiles(String searchTerm) {
        return groupProfileRepository.findByNameContainingIgnoreCase(searchTerm)
                .stream().map(profileMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<GroupProfileDto> getGroupProfilesByStatus(String status) {
        return groupProfileRepository.findByStatus(status)
                .stream().map(profileMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public boolean activateProfile(String id, ProfileType profileType) {
        if (profileType == ProfileType.SINGLE) {
            SingleProfileEntity entity = singleProfileRepository.findById(id)
                    .orElseThrow(() -> new ProfileNotFoundException("Single profile not found: " + id));
            entity.setStatus("Active");
            singleProfileRepository.save(entity);
        } else {
            GroupProfileEntity entity = groupProfileRepository.findById(id)
                    .orElseThrow(() -> new ProfileNotFoundException("Group profile not found: " + id));
            entity.setStatus("Active");
            groupProfileRepository.save(entity);
        }
        return true;
    }

    @Override
    public boolean deactivateProfile(String id, ProfileType profileType) {
        if (profileType == ProfileType.SINGLE) {
            SingleProfileEntity entity = singleProfileRepository.findById(id)
                    .orElseThrow(() -> new ProfileNotFoundException("Single profile not found: " + id));
            entity.setStatus("Inactive");
            singleProfileRepository.save(entity);
        } else {
            GroupProfileEntity entity = groupProfileRepository.findById(id)
                    .orElseThrow(() -> new ProfileNotFoundException("Group profile not found: " + id));
            entity.setStatus("Inactive");
            groupProfileRepository.save(entity);
        }
        return true;
    }

    @Override
    public SingleProfileDto useProfile(String id) {
        SingleProfileEntity entity = singleProfileRepository.findById(id)
                .orElseThrow(() -> new ProfileNotFoundException("Single profile not found: " + id));
        entity.setLastUsed(LocalDateTime.now().toString());
        SingleProfileEntity updatedEntity = singleProfileRepository.save(entity);
        return profileMapper.toDto(updatedEntity);
    }

    @Override
    public String getNextProfileFromGroup(String groupId) {
        GroupProfileEntity groupEntity = groupProfileRepository.findById(groupId)
                .orElseThrow(() -> new ProfileNotFoundException("Group profile not found: " + groupId));
        return groupEntity.getMemberProfiles().stream().findFirst().map(SingleProfileEntity::getId).orElse(null);
    }

    @Override
    public long getTotalSingleProfiles() {
        return singleProfileRepository.count();
    }

    @Override
    public long getTotalGroupProfiles() {
        return groupProfileRepository.count();
    }

    @Override
    public long getActiveProfilesCount() {
        return singleProfileRepository.countByStatus("Active") + groupProfileRepository.countByStatus("Active");
    }

    @Override
    public List<SingleProfileDto> getRecentlyUsedProfiles() {
        return singleProfileRepository.findRecentlyUsedProfiles()
                .stream().map(profileMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<SingleProfileDto> createSingleProfiles(List<SingleProfileDto> profiles) {
        return profiles.stream().map(this::createSingleProfile).collect(Collectors.toList());
    }

    @Override
    public boolean deleteMultipleProfiles(List<String> ids, ProfileType profileType) {
        ids.forEach(id -> {
            if (profileType == ProfileType.SINGLE) {
                deleteSingleProfile(id);
            } else {
                deleteGroupProfile(id);
            }
        });
        return true;
    }

    @Override
    public boolean bulkStatusUpdate(List<String> ids, String status, ProfileType profileType) {
        ids.forEach(id -> {
            if ("Active".equals(status)) {
                activateProfile(id, profileType);
            } else {
                deactivateProfile(id, profileType);
            }
        });
        return true;
    }

    private void validateSingleProfile(SingleProfileDto profileDto) {
        if (profileDto == null || !StringUtils.hasText(profileDto.getName())) {
            throw new ProfileValidationException("Profile name is required");
        }
    }

    private void validateGroupProfile(GroupProfileDto profileDto) {
        if (profileDto == null || !StringUtils.hasText(profileDto.getName())) {
            throw new ProfileValidationException("Group profile name is required");
        }
    }

    private void validateMemberProfiles(Set<String> memberProfileIds) {
        if (memberProfileIds != null) {
            memberProfileIds.forEach(profileId -> {
                if (!singleProfileRepository.existsById(profileId)) {
                    throw new ProfileValidationException("Member profile not found: " + profileId);
                }
            });
        }
    }

    private void updateSingleProfileEntity(SingleProfileEntity entity, SingleProfileDto dto) {
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setStatus(dto.getStatus());
    }

    private void updateGroupProfileEntity(GroupProfileEntity entity, GroupProfileDto dto) {
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setStatus(dto.getStatus());
        entity.setSelectionMode(dto.getSelectionMode());
        entity.setProxyConfig(profileMapper.toEntity(dto.getProxyConfig()));
        entity.setTimezone(dto.getTimezone());
        entity.setLanguage(dto.getLanguage());
        
        // Assign URLGroup or default
        assignUrlGroupToEntity(entity, dto.getUrlGroupId());
        
        // Update member profiles
        if (dto.getMemberProfileIds() != null) {
            entity.getMemberProfiles().clear();
            for (String profileId : dto.getMemberProfileIds()) {
                singleProfileRepository.findById(profileId).ifPresent(profile -> 
                    entity.getMemberProfiles().add(profile)
                );
            }
        }
    }
    
    private void assignUrlGroupToEntity(GroupProfileEntity entity, Long urlGroupId) {
        if (urlGroupId != null) {
            groupProfileRepository.findById(entity.getId()).ifPresent(existingEntity -> {
                URLGroupEntity urlGroup = urlGroupRepository.findById(urlGroupId).orElse(null);
                if (urlGroup != null) {
                    entity.setUrlGroup(urlGroup);
                    entity.setUrlGroupId(urlGroupId);
                }
            });
        } else {
            // Assign default URLGroup
            URLGroupEntity defaultUrlGroup = urlGroupRepository.findByName("Default");
            if (defaultUrlGroup != null) {
                entity.setUrlGroup(defaultUrlGroup);
                entity.setUrlGroupId(defaultUrlGroup.getId());
            }
        }
    }
}