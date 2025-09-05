package com.itasca.spoofing.repository;

import com.itasca.spoofing.entity.URLEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface URLRepository extends JpaRepository<URLEntity, Long> {
    
    @Query("SELECT u FROM URLEntity u WHERE u.url NOT IN " +
           "(SELECT urls FROM URLGroupEntity ug JOIN ug.urls urls)")
    List<URLEntity> findAvailableUrls();
    
    URLEntity findByUrl(String url);
}