package com.tamm.identity.dto.request.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequest {
    @JsonProperty("conversation_id")
    private String conversationId;
    
    @JsonProperty("content")
    private String content;
    
    @JsonProperty("message_type")
    private String messageType;
    
    @JsonProperty("attachments")
    private List<Object> attachments;
}
