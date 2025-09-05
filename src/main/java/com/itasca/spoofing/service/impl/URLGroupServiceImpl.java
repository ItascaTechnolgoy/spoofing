package com.itasca.spoofing.service.impl;

import com.itasca.spoofing.entity.URLGroupEntity;
import com.itasca.spoofing.model.URLGroupDto;
import com.itasca.spoofing.model.URLDto;
import com.itasca.spoofing.repository.URLGroupRepository;
import com.itasca.spoofing.service.URLGroupService;
import com.itasca.spoofing.service.URLService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class URLGroupServiceImpl implements URLGroupService {

    @Autowired
    private URLGroupRepository urlGroupRepository;
    
    @Autowired
    private URLService urlService;

    @Override
    public List<URLGroupDto> getAllURLGroups() {
        return urlGroupRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<URLGroupDto> getURLGroupById(Long id) {
        return urlGroupRepository.findById(id)
                .map(this::convertToDto);
    }

    @Override
    public URLGroupDto createURLGroup(URLGroupDto urlGroupDto) {
        URLGroupEntity entity = convertToEntity(urlGroupDto);
        URLGroupEntity savedEntity = urlGroupRepository.save(entity);
        return convertToDto(savedEntity);
    }

    @Override
    public URLGroupDto updateURLGroup(Long id, URLGroupDto urlGroupDto) {
        URLGroupEntity entity = urlGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("URLGroup not found"));
        
        entity.setName(urlGroupDto.getName());
        List<String> urlStrings = urlGroupDto.getUrls().stream()
                .map(URLDto::getUrl)
                .collect(Collectors.toList());
        entity.setUrls(urlStrings);
        
        URLGroupEntity savedEntity = urlGroupRepository.save(entity);
        return convertToDto(savedEntity);
    }

    @Override
    public void deleteURLGroup(Long id) {
        urlGroupRepository.deleteById(id);
    }

    @Override
    public URLGroupDto assignUrlsToGroup(Long groupId, List<URLDto> urls) {
        URLGroupEntity entity = urlGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("URLGroup not found"));
        
        // Add new URLs to existing ones, avoiding duplicates
        for (URLDto url : urls) {
            if (!entity.getUrls().contains(url.getUrl())) {
                entity.getUrls().add(url.getUrl());
            }
        }
        
        URLGroupEntity savedEntity = urlGroupRepository.save(entity);
        return convertToDto(savedEntity);
    }

    @Override
    public URLGroupDto addUrlToGroup(Long groupId, URLDto url) {
        URLGroupEntity entity = urlGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("URLGroup not found"));
        
        if (!entity.getUrls().contains(url.getUrl())) {
            entity.getUrls().add(url.getUrl());
        }
        URLGroupEntity savedEntity = urlGroupRepository.save(entity);
        return convertToDto(savedEntity);
    }

    @Override
    public URLGroupDto removeUrlFromGroup(Long groupId, URLDto url) {
        URLGroupEntity entity = urlGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("URLGroup not found"));
        
        entity.getUrls().remove(url.getUrl());
        URLGroupEntity savedEntity = urlGroupRepository.save(entity);
        return convertToDto(savedEntity);
    }

    @Override
    public URLGroupDto getDefaultUrlGroup() {
        URLGroupEntity defaultGroup = urlGroupRepository.findByName("Default");
        if (defaultGroup != null) {
            return convertToDto(defaultGroup);
        }
        throw new RuntimeException("Default URLGroup not found");
    }

    @Override
    public List<URLDto> getAvailableUrlsForGroup(Long groupId) {
        URLGroupEntity entity = urlGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("URLGroup not found"));
        
        // Get assigned URL strings
        List<String> assignedUrls = entity.getUrls();
        
        // Filter out URLs already assigned to this group
        return urlService.getAllUrls().stream()
                .filter(url -> !assignedUrls.contains(url.getUrl()))
                .collect(Collectors.toList());
    }

    private URLGroupDto convertToDto(URLGroupEntity entity) {
        // Convert URL strings to URLDto objects
        List<URLDto> urlDtos = urlService.getAllUrls().stream()
                .filter(url -> entity.getUrls().contains(url.getUrl()))
                .collect(Collectors.toList());
        
        return URLGroupDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .urls(urlDtos)
                .build();
    }

    private URLGroupEntity convertToEntity(URLGroupDto dto) {
        List<String> urlStrings = dto.getUrls().stream()
                .map(URLDto::getUrl)
                .collect(Collectors.toList());
        
        return URLGroupEntity.builder()
                .name(dto.getName())
                .urls(urlStrings)
                .build();
    }
}