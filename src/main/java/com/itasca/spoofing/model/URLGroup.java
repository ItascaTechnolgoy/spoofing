package com.itasca.spoofing.model;


import lombok.*;

import java.util.List;
import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class URLGroup {

    private String name;

    @Builder.Default
    private List<String> urls = new ArrayList<>();

    private String description;
}