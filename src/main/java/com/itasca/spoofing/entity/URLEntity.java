package com.itasca.spoofing.entity;

import lombok.*;
import jakarta.persistence.*;

@Entity
@Table(name = "urls")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class URLEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "url", nullable = false, unique = true)
    private String url;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;
}