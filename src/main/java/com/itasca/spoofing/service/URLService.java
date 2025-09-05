package com.itasca.spoofing.service;

import com.itasca.spoofing.model.URLDto;
import java.util.List;

public interface URLService {
    List<URLDto> getAllUrls();
    List<URLDto> getAvailableUrls();
    URLDto createUrl(URLDto urlDto);
    void deleteUrl(Long id);
    URLDto getDefaultUrl();
}