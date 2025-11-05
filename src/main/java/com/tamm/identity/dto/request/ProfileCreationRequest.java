package com.tamm.identity.dto.request;

import java.time.LocalDate;

import com.tamm.identity.dto.ContactInfo;

import lombok.*;
import lombok.experimental.FieldDefaults;
// import tam.dto.userprofile.request.ContactInfo;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProfileCreationRequest {
    String userId;
    String firstName;
    String lastName;
    LocalDate dateOfBirth;
    String gender;
    String bio;
    String type;
    ContactInfo contactInfo;
}
