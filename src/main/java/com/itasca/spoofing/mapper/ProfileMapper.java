package com.itasca.spoofing.mapper;


import com.itasca.spoofing.model.*;
import com.itasca.spoofing.entity.*;
import com.itasca.spoofing.repository.SingleProfileRepository;
import com.itasca.spoofing.repository.URLGroupRepository;
import com.itasca.spoofing.repository.UserRepository;
import com.itasca.spoofing.repository.URLRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

@Component
public class ProfileMapper {

    @Autowired
    private SingleProfileRepository singleProfileRepository;
    
    @Autowired
    private URLGroupRepository urlGroupRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private URLRepository urlRepository;

    // SingleProfile mappings

    public SingleProfileDto toDto(SingleProfileEntity entity) {
        if (entity == null) {
            return null;
        }

        return SingleProfileDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .profileType(entity.getProfileType())
                .operatingSystem(entity.getOperatingSystem())
                .userAgent(entity.getUserAgent())
                .screenResolution(entity.getScreenResolution())
                .webglVendor(entity.getWebglVendor())
                .webglRenderer(entity.getWebglRenderer())
                .hardwareConcurrency(entity.getHardwareConcurrency())
                .deviceMemory(entity.getDeviceMemory())
                .canvasFingerprint(entity.getCanvasFingerprint())
                .webrtcEnabled(entity.getWebrtcEnabled())
                .javascriptEnabled(entity.getJavascriptEnabled())
                .cookiesEnabled(entity.getCookiesEnabled())
                .geolocationEnabled(entity.getGeolocationEnabled())
                .doNotTrack(entity.getDoNotTrack())
                .urlGroups(entity.getUrlGroups().stream().map(this::toDto).collect(Collectors.toList()))
                .defaultUrlGroup(entity.getDefaultUrlGroup())
                .status(entity.getStatus())
                .created(entity.getCreatedAt() != null ? entity.getCreatedAt().toString() : null)
                .lastUsed(entity.getLastUsed())
                .generatedFingerprint(entity.getGeneratedFingerprint())
                .build();
    }

    public SingleProfileEntity toEntity(SingleProfileDto dto) {
        if (dto == null) {
            return null;
        }

        SingleProfileEntity entity = SingleProfileEntity.builder()
                .id(dto.getId())
                .name(dto.getName())
                .description(dto.getDescription())
                .profileType(dto.getProfileType())
                .operatingSystem(dto.getOperatingSystem())
                .userAgent(dto.getUserAgent())
                .screenResolution(dto.getScreenResolution())
                .webglVendor(dto.getWebglVendor())
                .webglRenderer(dto.getWebglRenderer())
                .hardwareConcurrency(dto.getHardwareConcurrency())
                .deviceMemory(dto.getDeviceMemory())
                .canvasFingerprint(dto.getCanvasFingerprint())
                .webrtcEnabled(dto.getWebrtcEnabled())
                .javascriptEnabled(dto.getJavascriptEnabled())
                .cookiesEnabled(dto.getCookiesEnabled())
                .geolocationEnabled(dto.getGeolocationEnabled())
                .doNotTrack(dto.getDoNotTrack())
                .defaultUrlGroup(dto.getDefaultUrlGroup())
                .status(dto.getStatus())
                .lastUsed(dto.getLastUsed())
                .generatedFingerprint(dto.getGeneratedFingerprint())
                .build();

        // Map URL groups
        if (dto.getUrlGroups() != null) {
            List<URLGroupEntity> urlGroups = dto.getUrlGroups().stream()
                    .map(this::toEntity)
                    .collect(Collectors.toList());
            urlGroups.forEach(group -> group.setSingleProfile(entity));
            entity.setUrlGroups(urlGroups);
        }

        return entity;
    }

    // GroupProfile mappings

    public GroupProfileDto toDto(GroupProfileEntity entity) {
        if (entity == null) {
            return null;
        }

        return GroupProfileDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .profileType(entity.getProfileType())
                .memberProfileIds(entity.getMemberProfiles().stream()
                        .map(SingleProfileEntity::getId)
                        .collect(Collectors.toSet()))
                .memberProfiles(entity.getMemberProfiles().stream()
                        .map(this::toDto)
                        .collect(Collectors.toCollection(HashSet::new)))
                .assignedUserIds(entity.getAssignedUsers().stream()
                        .map(UserEntity::getId)
                        .collect(Collectors.toSet()))
                .selectionMode(entity.getSelectionMode())
                .currentProfileIndex(entity.getCurrentProfileIndex())
                .proxyConfig(toDto(entity.getProxyConfig()))
                .timezone(entity.getTimezone())
                .language(entity.getLanguage())
                .urlGroupId(entity.getUrlGroupId())
                .urlGroup(entity.getUrlGroup() != null ? toDto(entity.getUrlGroup()) : null)
                .status(entity.getStatus())
                .created(entity.getCreatedAt() != null ? entity.getCreatedAt().toString() : null)
                .lastUsed(entity.getLastUsed())
                .build();
    }

    public GroupProfileEntity toEntity(GroupProfileDto dto) {
        if (dto == null) {
            return null;
        }

        GroupProfileEntity entity = GroupProfileEntity.builder()
                .id(dto.getId())
                .name(dto.getName())
                .description(dto.getDescription())
                .profileType(dto.getProfileType())
                .selectionMode(dto.getSelectionMode())
                .currentProfileIndex(dto.getCurrentProfileIndex())
                .proxyConfig(toEntity(dto.getProxyConfig()))
                .timezone(dto.getTimezone())
                .language(dto.getLanguage())
                .urlGroupId(dto.getUrlGroupId())
                .status(dto.getStatus())
                .lastUsed(dto.getLastUsed())
                .build();

        // Map member profiles
        if (dto.getMemberProfileIds() != null && !dto.getMemberProfileIds().isEmpty()) {
            Set<SingleProfileEntity> memberProfiles = new HashSet<>();
            for (String profileId : dto.getMemberProfileIds()) {
                singleProfileRepository.findById(profileId).ifPresent(memberProfiles::add);
            }
            entity.setMemberProfiles(memberProfiles);
        }
        
        // Map assigned users
        if (dto.getAssignedUserIds() != null && !dto.getAssignedUserIds().isEmpty()) {
            Set<UserEntity> assignedUsers = new HashSet<>();
            for (Long userId : dto.getAssignedUserIds()) {
                userRepository.findById(userId).ifPresent(assignedUsers::add);
            }
            entity.setAssignedUsers(assignedUsers);
        }

        // Map URL group by ID
        if (dto.getUrlGroupId() != null) {
            urlGroupRepository.findById(dto.getUrlGroupId())
                    .ifPresent(entity::setUrlGroup);
        }

        return entity;
    }

    // URLGroup mappings

    public URLGroupDto toDto(URLGroupEntity entity) {
        if (entity == null) {
            return null;
        }

        // Convert URL strings to URLDto objects with proper IDs
        List<URLDto> urlDtos = entity.getUrls().stream()
                .map(urlString -> {
                    URLEntity urlEntity = urlRepository.findByUrl(urlString);
                    if (urlEntity != null) {
                        return URLDto.builder()
                                .id(urlEntity.getId())
                                .url(urlEntity.getUrl())
                                .name(urlEntity.getName())
                                .description(urlEntity.getDescription())
                                .build();
                    } else {
                        return URLDto.builder()
                                .url(urlString)
                                .name(urlString)
                                .build();
                    }
                })
                .collect(Collectors.toList());

        return URLGroupDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .urls(urlDtos)
                .build();
    }

    public URLGroupEntity toEntity(URLGroupDto dto) {
        if (dto == null) {
            return null;
        }

        List<String> urlStrings = dto.getUrls().stream()
                .map(URLDto::getUrl)
                .collect(Collectors.toList());

        return URLGroupEntity.builder()
                .name(dto.getName())
                .urls(urlStrings)
                .build();
    }

    // ProxyConfig mappings

    public ProxyConfig toDto(ProxyConfigEntity entity) {
        if (entity == null) {
            return null;
        }

        return ProxyConfig.builder()
                .proxyType(entity.getProxyType())
                .host(entity.getHost())
                .port(entity.getPort())
                .endPort(entity.getEndPort())
                .username(entity.getUsername())
                .password(entity.getPassword())
                .ipType(entity.getIpType())
                .fixedIp(entity.getFixedIp())
                .country(entity.getCountry())
                .build();
    }

    public ProxyConfigEntity toEntity(ProxyConfig dto) {
        if (dto == null) {
            return null;
        }

        return ProxyConfigEntity.builder()
                .proxyType(dto.getProxyType())
                .host(dto.getHost())
                .port(dto.getPort())
                .endPort(dto.getEndPort())
                .username(dto.getUsername())
                .password(dto.getPassword())
                .ipType(dto.getIpType())
                .fixedIp(dto.getFixedIp())
                .country(dto.getCountry())
                .build();
    }
}