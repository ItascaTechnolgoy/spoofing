package com.itasca.spoofing.model;

import lombok.*;
import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class URLDto {

    private Long id;

    @NotBlank(message = "URL is required")
    private String url;

    private String name;
    private String description;
}