package com.tamm.identity.dto.request.relationship;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendRequestRespondRequest {

    @NotBlank
    private String requesterId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String targetUserId;

    @NotNull
    private FriendRequestDecision decision;
}
