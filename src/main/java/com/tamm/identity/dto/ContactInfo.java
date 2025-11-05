package com.tamm.identity.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ContactInfo {
    String userId;
    String email;
    String phoneNumber;
    String address;
    String socialMediaLinks; // JSON or comma-separated links
    String mediaLinks; // JSON or comma-separated links
}
