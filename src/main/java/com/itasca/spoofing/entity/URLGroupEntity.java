package com.itasca.spoofing.entity;

import lombok.*;
import jakarta.persistence.*;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "url_groups")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class URLGroupEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @ElementCollection
    @CollectionTable(name = "url_group_urls", joinColumns = @JoinColumn(name = "url_group_id"))
    @Column(name = "url")
    @Builder.Default
    private List<String> urls = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "single_profile_id")
    private SingleProfileEntity singleProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_profile_id")
    private GroupProfileEntity groupProfile;
}