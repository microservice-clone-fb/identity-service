package com.tamm.identity.dto.post;

import java.time.Instant;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostResponse {
    String id;
    String content;
    String mediaUrl;
    String userId;
    String username;
    String created;
    Instant createdDate;
    Instant modifiedDate;

    int likeCount;
    int commentCount;
    int shareCount;

    boolean isLiked;
    boolean isShared;
}
