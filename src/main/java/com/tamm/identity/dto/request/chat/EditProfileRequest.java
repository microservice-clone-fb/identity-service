package com.tamm.identity.dto.request.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EditProfileRequest {
    @JsonProperty("full_name")
    private String fullName;
    
    @JsonProperty("date_of_birth")
    private String dateOfBirth;
    
    @JsonProperty("gender")
    private String gender;
    
    @JsonProperty("base64")
    private String base64;
}
