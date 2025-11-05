package com.tamm.identity.entity;

import java.util.Date;

import jakarta.persistence.*;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "invalidated_tokens")
public class InvalidatedToken {
    @Id
    @Column(name = "id")
    String id;

    @Column(name = "expiry_time")
    Date expiryTime;
}
