package com.tamm.identity.dto.response;

import java.time.Instant;
import java.util.List;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentResponse {
    String id;
    String postId;
    String userId;
    String username;
    String content;
    String parentCommentId;
    String created;
    Instant createdDate;
    Instant modifiedDate;
    List<CommentResponse> replies; // Danh sách reply cho comment này
}
