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
public class MessageDTO {
    @JsonProperty("_id")
    private String id;
    
    @JsonProperty("conversation_id")
    private String conversationId;
    
    @JsonProperty("sender")
    private String sender;
    
    @JsonProperty("content")
    private String content;
    
    @JsonProperty("message_type")
    private String messageType;
    
    @JsonProperty("attachments")
    private List<AttachmentDTO> attachments;
    
    @JsonProperty("reactions")
    private List<ReactionDTO> reactions;
    
    @JsonProperty("read_by")
    private List<String> readBy;
    
    @JsonProperty("is_revoked")
    private Boolean isRevoked;
    
    @JsonProperty("is_deleted")
    private Boolean isDeleted;
    
    @JsonProperty("createdAt")
    private Date createdAt;
    
    @JsonProperty("updatedAt")
    private Date updatedAt;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class AttachmentDTO {
    private String url;
    private String type;
    private String fileName;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class ReactionDTO {
    private String userId;
    private String emoji;
    private String createdAt;
}
