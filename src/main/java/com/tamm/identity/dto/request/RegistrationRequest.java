package com.tamm.identity.dto.request;

import java.time.LocalDate;

import com.tamm.identity.dto.ContactInfo;

import lombok.*;
import lombok.experimental.FieldDefaults;
// import tam.dto.userprofile.request.ContactInfo;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString
public class RegistrationRequest {
    String firstName;
    String lastName;
    LocalDate dateOfBirth;

    String bio;
    ContactInfo contactInfo;

    String email;
    String gender;
    String password;
    String type; // normal, famous, admin, enterprise
}
