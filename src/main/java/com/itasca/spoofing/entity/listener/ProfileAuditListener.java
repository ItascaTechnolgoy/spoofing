package com.itasca.spoofing.entity.listener;




import com.itasca.spoofing.entity.ProfileAuditEntity;
import com.itasca.spoofing.entity.SingleProfileEntity;
import com.itasca.spoofing.entity.GroupProfileEntity;
import com.itasca.spoofing.repository.ProfileAuditRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PostUpdate;
import jakarta.persistence.PreRemove;

@Component
@Slf4j
public class ProfileAuditListener {

    @Autowired
    private ProfileAuditRepository auditRepository;

    @PostPersist
    public void postPersist(Object entity) {
        try {
            if (entity instanceof SingleProfileEntity) {
                SingleProfileEntity profile = (SingleProfileEntity) entity;
                ProfileAuditEntity audit = ProfileAuditEntity.createAudit(
                        profile.getId(),
                        profile.getProfileType(),
                        getCurrentUserId()
                );
                auditRepository.save(audit);
                log.info("Created audit entry for new single profile: {}", profile.getId());

            } else if (entity instanceof GroupProfileEntity) {
                GroupProfileEntity profile = (GroupProfileEntity) entity;
                ProfileAuditEntity audit = ProfileAuditEntity.createAudit(
                        profile.getId(),
                        profile.getProfileType(),
                        getCurrentUserId()
                );
                auditRepository.save(audit);
                log.info("Created audit entry for new group profile: {}", profile.getId());
            }
        } catch (Exception e) {
            log.error("Error creating audit entry for persist operation", e);
        }
    }

    @PostUpdate
    public void postUpdate(Object entity) {
        try {
            if (entity instanceof SingleProfileEntity) {
                SingleProfileEntity profile = (SingleProfileEntity) entity;
                ProfileAuditEntity audit = ProfileAuditEntity.updateAudit(
                        profile.getId(),
                        profile.getProfileType(),
                        getCurrentUserId(),
                        "Profile updated"
                );
                auditRepository.save(audit);
                log.info("Created audit entry for updated single profile: {}", profile.getId());

            } else if (entity instanceof GroupProfileEntity) {
                GroupProfileEntity profile = (GroupProfileEntity) entity;
                ProfileAuditEntity audit = ProfileAuditEntity.updateAudit(
                        profile.getId(),
                        profile.getProfileType(),
                        getCurrentUserId(),
                        "Profile updated"
                );
                auditRepository.save(audit);
                log.info("Created audit entry for updated group profile: {}", profile.getId());
            }
        } catch (Exception e) {
            log.error("Error creating audit entry for update operation", e);
        }
    }

    @PreRemove
    public void preRemove(Object entity) {
        try {
            if (entity instanceof SingleProfileEntity) {
                SingleProfileEntity profile = (SingleProfileEntity) entity;
                ProfileAuditEntity audit = ProfileAuditEntity.builder()
                        .profileId(profile.getId())
                        .profileType(profile.getProfileType())
                        .action("DELETE")
                        .userId(getCurrentUserId())
                        .changes("Profile deleted")
                        .build();
                auditRepository.save(audit);
                log.info("Created audit entry for deleted single profile: {}", profile.getId());

            } else if (entity instanceof GroupProfileEntity) {
                GroupProfileEntity profile = (GroupProfileEntity) entity;
                ProfileAuditEntity audit = ProfileAuditEntity.builder()
                        .profileId(profile.getId())
                        .profileType(profile.getProfileType())
                        .action("DELETE")
                        .userId(getCurrentUserId())
                        .changes("Profile deleted")
                        .build();
                auditRepository.save(audit);
                log.info("Created audit entry for deleted group profile: {}", profile.getId());
            }
        } catch (Exception e) {
            log.error("Error creating audit entry for delete operation", e);
        }
    }

    private String getCurrentUserId() {
        // This should be replaced with actual security context
        // For example: SecurityContextHolder.getContext().getAuthentication().getName()
        return "system"; // Default fallback
    }
}