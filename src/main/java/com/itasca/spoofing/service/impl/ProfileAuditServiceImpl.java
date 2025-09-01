package com.itasca.spoofing.service.impl;


import com.itasca.spoofing.model.ProfileType;
import com.itasca.spoofing.entity.ProfileAuditEntity;
import com.itasca.spoofing.repository.ProfileAuditRepository;
import com.itasca.spoofing.service.ProfileAuditService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@Slf4j
public class ProfileAuditServiceImpl implements ProfileAuditService {

    @Autowired
    private ProfileAuditRepository auditRepository;

    @Autowired(required = false)
    private HttpServletRequest request;

    @Override
    public void logProfileCreation(String profileId, ProfileType profileType) {
        ProfileAuditEntity audit = createBaseAuditEntity(profileId, profileType, "CREATE");
        audit.setChanges("Profile created");
        auditRepository.save(audit);
        log.info("Logged profile creation: {} ({})", profileId, profileType);
    }

    @Override
    public void logProfileUpdate(String profileId, ProfileType profileType) {
        ProfileAuditEntity audit = createBaseAuditEntity(profileId, profileType, "UPDATE");
        audit.setChanges("Profile updated");
        auditRepository.save(audit);
        log.info("Logged profile update: {} ({})", profileId, profileType);
    }

    @Override
    public void logProfileDeletion(String profileId, ProfileType profileType) {
        ProfileAuditEntity audit = createBaseAuditEntity(profileId, profileType, "DELETE");
        audit.setChanges("Profile deleted");
        auditRepository.save(audit);
        log.info("Logged profile deletion: {} ({})", profileId, profileType);
    }

    @Override
    public void logProfileUsage(String profileId, ProfileType profileType) {
        ProfileAuditEntity audit = createBaseAuditEntity(profileId, profileType, "USE");
        auditRepository.save(audit);
        log.debug("Logged profile usage: {} ({})", profileId, profileType);
    }

    @Override
    public void logProfileActivation(String profileId, ProfileType profileType) {
        ProfileAuditEntity audit = createBaseAuditEntity(profileId, profileType, "ACTIVATE");
        audit.setChanges("Profile activated");
        auditRepository.save(audit);
        log.info("Logged profile activation: {} ({})", profileId, profileType);
    }

    @Override
    public void logProfileDeactivation(String profileId, ProfileType profileType) {
        ProfileAuditEntity audit = createBaseAuditEntity(profileId, profileType, "DEACTIVATE");
        audit.setChanges("Profile deactivated");
        auditRepository.save(audit);
        log.info("Logged profile deactivation: {} ({})", profileId, profileType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfileAuditEntity> getProfileAuditHistory(String profileId) {
        return auditRepository.findByProfileIdOrderByActionTimestampDesc(profileId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfileAuditEntity> getRecentAuditEntries(String profileId, int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return auditRepository.findRecentAuditEntries(profileId, since);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfileAuditEntity> getAuditEntriesByDateRange(LocalDateTime start, LocalDateTime end) {
        return auditRepository.findByActionTimestampBetween(start, end);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUsageCount(String profileId) {
        return auditRepository.countByProfileIdAndAction(profileId, "USE");
    }

    private ProfileAuditEntity createBaseAuditEntity(String profileId, ProfileType profileType, String action) {
        ProfileAuditEntity audit = ProfileAuditEntity.builder()
                .profileId(profileId)
                .profileType(profileType)
                .action(action)
                .actionTimestamp(LocalDateTime.now())
                .userId(getCurrentUserId())
                .build();

        // Add request details if available
        if (request != null) {
//            audit.setIpAddress(getClientIpAddress());
//            audit.setUserAgent(request.getHeader("User-Agent"));
//            audit.setSessionId(request.getSession(false) != null ?
//                    request.getSession(false).getId());
        }

        return audit;
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "system";
    }

    private String getClientIpAddress() {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}