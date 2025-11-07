package com.tamm.identity.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class IntrospectResponse {
    // Token validity
    boolean valid;
    String message;

    // Token type (ACCESS or REFRESH)
    @JsonProperty("tokenType")
    String tokenType;

    // Token expiration time (Unix timestamp in milliseconds)
    @JsonProperty("expirationTime")
    Long expirationTime;
}
