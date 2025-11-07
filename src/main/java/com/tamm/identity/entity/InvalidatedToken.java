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
@Table(
        name = "invalidated_tokens",
        indexes = {@Index(name = "idx_invalidated_token_expiry", columnList = "expiry_time")})
public class InvalidatedToken {
    @Id
    @Column(name = "id", nullable = false, length = 255)
    String id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "expiry_time", nullable = false)
    Date expiryTime;
}
