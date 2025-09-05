package com.itasca.spoofing.service.impl;

import com.itasca.spoofing.entity.URLEntity;
import com.itasca.spoofing.model.URLDto;
import com.itasca.spoofing.repository.URLRepository;
import com.itasca.spoofing.service.URLService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class URLServiceImpl implements URLService {

    @Autowired
    private URLRepository urlRepository;

    @Override
    public List<URLDto> getAllUrls() {
        return urlRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<URLDto> getAvailableUrls() {
        return urlRepository.findAvailableUrls().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public URLDto createUrl(URLDto urlDto) {
        URLEntity entity = convertToEntity(urlDto);
        URLEntity savedEntity = urlRepository.save(entity);
        return convertToDto(savedEntity);
    }

    @Override
    public void deleteUrl(Long id) {
        urlRepository.deleteById(id);
    }

    @Override
    public URLDto getDefaultUrl() {
        URLEntity defaultUrl = urlRepository.findByUrl("about:blank");
        if (defaultUrl != null) {
            return convertToDto(defaultUrl);
        }
        throw new RuntimeException("Default URL not found");
    }

    private URLDto convertToDto(URLEntity entity) {
        return URLDto.builder()
                .id(entity.getId())
                .url(entity.getUrl())
                .name(entity.getName())
                .description(entity.getDescription())
                .build();
    }

    private URLEntity convertToEntity(URLDto dto) {
        return URLEntity.builder()
                .url(dto.getUrl())
                .name(dto.getName())
                .description(dto.getDescription())
                .build();
    }
}