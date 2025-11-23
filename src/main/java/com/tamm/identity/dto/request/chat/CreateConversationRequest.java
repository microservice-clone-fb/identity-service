package com.tamm.identity.dto.request.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateConversationRequest {
    @JsonProperty("conversation_type")
    private String conversationType;
    
    @JsonProperty("conversation_name")
    private String conversationName;
    
    @JsonProperty("conversation_avatar")
    private String conversationAvatar;
    
    @JsonProperty("members")
    private List<String> members;
}
