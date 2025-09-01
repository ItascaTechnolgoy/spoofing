package com.itasca.spoofing.repository;

import com.itasca.spoofing.entity.UserEntity;
import com.itasca.spoofing.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);
    List<UserEntity> findByRolesContaining(UserRole role);
    List<UserEntity> findByStatus(String status);
}