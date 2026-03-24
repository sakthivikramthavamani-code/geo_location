package com.georeport.dto;

import lombok.*;
import java.util.List;

/**
 * DTO for authentication response containing JWT token.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token;
    private String tokenType;
    private Long userId;
    private String email;
    private String fullName;
    private List<String> roles;
    private Long expiresIn;

    public static AuthResponse of(String token, Long userId, String email, String fullName, List<String> roles,
            Long expiresIn) {
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(userId)
                .email(email)
                .fullName(fullName)
                .roles(roles)
                .expiresIn(expiresIn)
                .build();
    }
}
