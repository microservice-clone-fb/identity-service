package com.tamm.identity.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationResponse {
    // ✅ Access Token
    String token;

    @JsonProperty("expirationTime")
    Long expirationTime; // Unix timestamp in milliseconds
    // ✅ Refresh Token
    String refreshToken;

    @JsonProperty("refreshTokenExpiration")
    Long refreshTokenExpiration; // Unix timestamp in milliseconds

    // ✅ Status & User Info
    boolean authenticated;
    String userId;
    UserProfileResponse userProfile;
}
