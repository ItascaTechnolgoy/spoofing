package com.itasca.spoofing.service.impl;

import com.itasca.spoofing.entity.UserEntity;
import com.itasca.spoofing.entity.UserRole;
import com.itasca.spoofing.repository.UserRepository;
import com.itasca.spoofing.service.DataInitializationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@Transactional
@Slf4j
public class DataInitializationServiceImpl implements DataInitializationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void initializeDefaultData() {
        createDefaultUser();
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
}