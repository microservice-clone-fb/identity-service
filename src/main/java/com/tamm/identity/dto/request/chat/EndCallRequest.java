package com.tamm.identity.dto.request.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EndCallRequest {
    @JsonProperty("restUserId")
    private String restUserId;
}
