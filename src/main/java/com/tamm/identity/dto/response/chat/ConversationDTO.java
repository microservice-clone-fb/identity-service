package com.tamm.identity.dto.response.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDTO {
    @JsonProperty("_id")
    private String id;
    
    @JsonProperty("conversation_type")
    private String conversationType;
    
    @JsonProperty("conversation_name")
    private String conversationName;
    
    @JsonProperty("conversation_avatar")
    private String conversationAvatar;
    
    @JsonProperty("members")
    private List<String> members;
    
    @JsonProperty("last_message")
    private String lastMessage;
    
    @JsonProperty("last_message_at")
    private Date lastMessageAt;
    
    @JsonProperty("createdAt")
    private Date createdAt;
    
    @JsonProperty("updatedAt")
    private Date updatedAt;
}
