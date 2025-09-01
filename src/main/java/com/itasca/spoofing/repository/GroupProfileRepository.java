package com.itasca.spoofing.repository;


import com.itasca.spoofing.entity.GroupProfileEntity;
import com.itasca.spoofing.entity.GroupType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GroupProfileRepository extends JpaRepository<GroupProfileEntity, String> {

    /**
     * Find groups by name (case insensitive)
     */
    List<GroupProfileEntity> findByNameContainingIgnoreCase(String name);

    /**
     * Find groups by status
     */
    List<GroupProfileEntity> findByStatus(String status);

    /**
     * Find groups by type
     */
    List<GroupProfileEntity> findByGroupType(GroupType groupType);

    /**
     * Find groups by type with pagination
     */
    Page<GroupProfileEntity> findByGroupType(GroupType groupType, Pageable pageable);

    /**
     * Find active groups
     */
    default List<GroupProfileEntity> findActiveGroups() {
        return findByStatus("Active");
    }

    /**
     * Find groups by selection mode
     */
    List<GroupProfileEntity> findBySelectionMode(String selectionMode);

    /**
     * Find groups created after a specific date
     */
    List<GroupProfileEntity> findByCreatedAtAfter(LocalDateTime date);

    /**
     * Find groups containing a specific member profile
     */
    @Query("SELECT g FROM GroupProfileEntity g JOIN g.memberProfiles p WHERE p.id = :profileId")
    List<GroupProfileEntity> findGroupsContainingProfile(@Param("profileId") String profileId);

    /**
     * Find groups assigned to a user
     */
    @Query("SELECT g FROM GroupProfileEntity g JOIN g.assignedUsers u WHERE u.id = :userId AND g.status = 'Active'")
    List<GroupProfileEntity> findActiveGroupsForUser(@Param("userId") Long userId);

    /**
     * Find default group for profile
     */
    @Query("SELECT g FROM GroupProfileEntity g WHERE g.defaultForProfile.id = :profileId")
    Optional<GroupProfileEntity> findDefaultGroupForProfile(@Param("profileId") String profileId);

    /**
     * Find custom groups with members
     */
    @Query("SELECT g FROM GroupProfileEntity g WHERE g.groupType = :groupType AND SIZE(g.memberProfiles) > 0")
    List<GroupProfileEntity> findCustomGroupsWithMembers(@Param("groupType") GroupType groupType);

    /**
     * Count groups by status
     */
    long countByStatus(String status);

    /**
     * Count groups by type
     */
    long countByGroupType(GroupType groupType);

    /**
     * Check if group name exists (case insensitive)
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Find groups with minimum member count
     */
    @Query("SELECT g FROM GroupProfileEntity g WHERE SIZE(g.memberProfiles) >= :minCount")
    List<GroupProfileEntity> findGroupsWithMinMemberCount(@Param("minCount") int minCount);

    /**
     * Find most popular groups by member count
     */
    @Query("SELECT g FROM GroupProfileEntity g WHERE g.groupType = 'CUSTOM' AND g.status = 'Active' ORDER BY SIZE(g.memberProfiles) DESC")
    List<GroupProfileEntity> findMostPopularGroups(Pageable pageable);

    /**
     * Find groups with available capacity
     */
    @Query("SELECT g FROM GroupProfileEntity g WHERE g.currentActiveSessions < g.maxConcurrentUsage AND g.status = 'Active'")
    List<GroupProfileEntity> findGroupsWithAvailableCapacity();

    /**
     * Find system generated groups
     */
    List<GroupProfileEntity> findByIsSystemGeneratedTrue();

    /**
     * Find custom groups only
     */
    List<GroupProfileEntity> findByIsSystemGeneratedFalse();
}