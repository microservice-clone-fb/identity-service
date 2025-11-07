package com.tamm.identity.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RefreshRequest {
    // Refresh token to exchange for new access token
    String token;

    // Optional: timestamp of request (for security logging)
    @JsonProperty("timestamp")
    Long timestamp;
}
