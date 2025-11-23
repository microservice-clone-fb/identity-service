package com.tamm.identity.dto.response;

import java.time.Instant;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LikeResponse {
    String id;
    String postId;
    String userId;
    String username;
    String created;
    Instant createdDate;
}
