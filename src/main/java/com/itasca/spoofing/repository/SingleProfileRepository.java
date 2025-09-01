package com.itasca.spoofing.repository;


import com.itasca.spoofing.entity.SingleProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SingleProfileRepository extends JpaRepository<SingleProfileEntity, String> {

    /**
     * Find profiles by name (case insensitive)
     */
    List<SingleProfileEntity> findByNameContainingIgnoreCase(String name);

    /**
     * Find profiles by status
     */
    List<SingleProfileEntity> findByStatus(String status);

    /**
     * Find active profiles
     */
    default List<SingleProfileEntity> findActiveProfiles() {
        return findByStatus("Active");
    }

    /**
     * Find profiles by operating system
     */
    List<SingleProfileEntity> findByOperatingSystem(String operatingSystem);

    /**
     * Find profiles created after a specific date
     */
    List<SingleProfileEntity> findByCreatedAtAfter(LocalDateTime date);



    /**
     * Count profiles by status
     */
    long countByStatus(String status);

    /**
     * Check if profile name exists (case insensitive)
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Find most recently used profiles
     */
    @Query("SELECT p FROM SingleProfileEntity p WHERE p.lastUsed != 'Never' ORDER BY p.updatedAt DESC")
    List<SingleProfileEntity> findRecentlyUsedProfiles();
}