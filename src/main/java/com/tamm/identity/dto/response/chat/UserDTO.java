package com.tamm.identity.dto.response.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    @JsonProperty("_id")
    private String id;
    
    private String userId;
    
    @JsonProperty("avatar_url")
    private String avatarUrl;
    
    @JsonProperty("full_name")
    private String fullName;
    
    @JsonProperty("phone")
    private String phone;
    
    private List<String> friends;
    
    @JsonProperty("sentRequests")
    private List<String> sentRequests;
    
    @JsonProperty("friendRequests")
    private List<String> friendRequests;
    
    @JsonProperty("blockedUsers")
    private List<String> blockedUsers;
    
    @JsonProperty("isBlocked")
    private List<String> isBlocked;
    
    @JsonProperty("is_online")
    private Boolean isOnline;
    
    @JsonProperty("is_has_password")
    private Boolean isHasPassword;
    
    @JsonProperty("last_seen")
    private Date lastSeen;
    
    @JsonProperty("token_version")
    private Integer tokenVersion;
    
    @JsonProperty("createdAt")
    private Date createdAt;
    
    @JsonProperty("updatedAt")
    private Date updatedAt;
}
