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
        name = "validated_refresh_tokens",
        indexes = {
            @Index(name = "idx_validated_refresh_token_expiry", columnList = "expiry_time"),
            @Index(name = "idx_validated_refresh_token_user", columnList = "user_id")
        })
public class ValidatedRefreshToken {
    @Id
    @Column(name = "jti", nullable = false, length = 255)
    String jti; // JWT ID - unique identifier for each refresh token

    @Column(name = "user_id", nullable = false, length = 255)
    String userId;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "expiry_time", nullable = false)
    Date expiryTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false)
    Date createdAt;
}
