package com.itasca.spoofing.service.impl;

import com.itasca.spoofing.entity.URLEntity;
import com.itasca.spoofing.entity.URLGroupEntity;
import com.itasca.spoofing.entity.UserEntity;
import com.itasca.spoofing.entity.UserRole;
import com.itasca.spoofing.repository.URLGroupRepository;
import com.itasca.spoofing.repository.URLRepository;
import com.itasca.spoofing.repository.UserRepository;
import com.itasca.spoofing.service.DataInitializationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@Transactional
@Slf4j
public class DataInitializationServiceImpl implements DataInitializationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private URLRepository urlRepository;

    @Autowired
    private URLGroupRepository urlGroupRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void initializeDefaultData() {
        createDefaultUser();
        createDefaultUrl();
        createDefaultUrlGroup();
    }

    private void createDefaultUser() {
        String defaultEmail = "sk";
        
        if (userRepository.findByEmail(defaultEmail).isEmpty()) {
            UserEntity defaultUser = UserEntity.builder()
                    .username(defaultEmail)
                    .email(defaultEmail)
                    .password(passwordEncoder.encode("sk123"))
                    .firstName("Super")
                    .lastName("Admin")
                    .status("ACTIVE")
                    .roles(Set.of(UserRole.SUPER_ADMIN, UserRole.ADMIN, UserRole.USER))
                    .build();

            userRepository.save(defaultUser);
            log.info("Default user created: {} with SUPER_ADMIN role", defaultEmail);
        } else {
            log.info("Default user already exists: {}", defaultEmail);
        }
    }

    private void createDefaultUrl() {
        String defaultUrl = "about:blank";
        
        if (urlRepository.findAll().stream().noneMatch(url -> url.getUrl().equals(defaultUrl))) {
            URLEntity defaultUrlEntity = URLEntity.builder()
                    .url(defaultUrl)
                    .name("Blank Page")
                    .description("Default blank page for browser startup")
                    .build();

            urlRepository.save(defaultUrlEntity);
            log.info("Default URL created: {}", defaultUrl);
        } else {
            log.info("Default URL already exists: {}", defaultUrl);
        }
    }

    private void createDefaultUrlGroup() {
        String defaultGroupName = "Default";
        
        if (urlGroupRepository.findAll().stream().noneMatch(group -> group.getName().equals(defaultGroupName))) {
            URLGroupEntity defaultUrlGroup = URLGroupEntity.builder()
                    .name(defaultGroupName)
                    .urls(List.of("about:blank"))
                    .build();

            urlGroupRepository.save(defaultUrlGroup);
            log.info("Default URLGroup created: {}", defaultGroupName);
        } else {
            log.info("Default URLGroup already exists: {}", defaultGroupName);
        }
    }
}