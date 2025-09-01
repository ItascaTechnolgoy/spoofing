package com.itasca.spoofing.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileResponseDto {
    private String status;
    private String message;
    private String id;
    private Object data;

    public static ProfileResponseDto success(Object data) {
        return ProfileResponseDto.builder()
                .status("success")
                .data(data)
                .build();
    }

    public static ProfileResponseDto error(String message) {
        return ProfileResponseDto.builder()
                .status("error")
                .message(message)
                .build();
    }
}