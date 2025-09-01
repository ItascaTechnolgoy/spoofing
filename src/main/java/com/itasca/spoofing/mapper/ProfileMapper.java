package com.itasca.spoofing.mapper;


import com.itasca.spoofing.model.*;
import com.itasca.spoofing.entity.*;
import com.itasca.spoofing.repository.SingleProfileRepository;
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
                .selectionMode(entity.getSelectionMode())
                .currentProfileIndex(entity.getCurrentProfileIndex())
                .proxyConfig(toDto(entity.getProxyConfig()))
                .timezone(entity.getTimezone())
                .language(entity.getLanguage())
                .urlGroups(entity.getUrlGroups().stream().map(this::toDto).collect(Collectors.toList()))
                .defaultUrlGroup(entity.getDefaultUrlGroup())
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
                .defaultUrlGroup(dto.getDefaultUrlGroup())
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

        // Map URL groups
        if (dto.getUrlGroups() != null) {
            List<URLGroupEntity> urlGroups = dto.getUrlGroups().stream()
                    .map(this::toEntity)
                    .collect(Collectors.toList());
            urlGroups.forEach(group -> group.setGroupProfile(entity));
            entity.setUrlGroups(urlGroups);
        }

        return entity;
    }

    // URLGroup mappings

    public URLGroupDto toDto(URLGroupEntity entity) {
        if (entity == null) {
            return null;
        }

        return URLGroupDto.builder()
                .name(entity.getName())
                .urls(entity.getUrls())
//                .currentIndex(entity.getCurrentIndex())
//                .completedUrls(entity.getCompletedUrls())
                .build();
    }

    public URLGroupEntity toEntity(URLGroupDto dto) {
        if (dto == null) {
            return null;
        }

        return URLGroupEntity.builder()
                .name(dto.getName())
                .urls(dto.getUrls())
//                .currentIndex(dto.getCurrentIndex())
//                .completedUrls(dto.getCompletedUrls())
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
                .username(dto.getUsername())
                .password(dto.getPassword())
                .ipType(dto.getIpType())
                .fixedIp(dto.getFixedIp())
                .country(dto.getCountry())
                .build();
    }
}