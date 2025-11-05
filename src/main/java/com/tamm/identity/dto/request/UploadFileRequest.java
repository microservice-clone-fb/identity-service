package com.tamm.identity.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
public class UploadFileRequest {
    String ownerId;
    String relatedIds; // Comma-separated list of related IDs (file trong doan chat, bai dang, story...)
}
