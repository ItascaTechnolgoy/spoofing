package com.itasca.spoofing.repository;

import com.itasca.spoofing.entity.URLGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface URLGroupRepository extends JpaRepository<URLGroupEntity, Long> {
    
    URLGroupEntity findByName(String name);
}