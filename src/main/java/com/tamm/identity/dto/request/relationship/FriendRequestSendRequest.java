package com.tamm.identity.dto.request.relationship;

import jakarta.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendRequestSendRequest {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String requesterId;

    @NotBlank
    private String targetUserId;

    private String message;
}
