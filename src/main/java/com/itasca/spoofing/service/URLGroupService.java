package com.itasca.spoofing.service;

import com.itasca.spoofing.model.URLGroupDto;
import com.itasca.spoofing.model.URLDto;
import java.util.List;
import java.util.Optional;

public interface URLGroupService {
    List<URLGroupDto> getAllURLGroups();
    Optional<URLGroupDto> getURLGroupById(Long id);
    URLGroupDto createURLGroup(URLGroupDto urlGroupDto);
    URLGroupDto updateURLGroup(Long id, URLGroupDto urlGroupDto);
    void deleteURLGroup(Long id);
    URLGroupDto assignUrlsToGroup(Long groupId, List<URLDto> urls);
    URLGroupDto addUrlToGroup(Long groupId, URLDto url);
    URLGroupDto removeUrlFromGroup(Long groupId, URLDto url);
    URLGroupDto getDefaultUrlGroup();
    List<URLDto> getAvailableUrlsForGroup(Long groupId);
}