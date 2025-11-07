package com.tamm.identity.entity;

import jakarta.persistence.*;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(
        name = "users_roles",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_user_role",
                    columnNames = {"user_id", "role_id"})
        },
        indexes = {
            @Index(name = "idx_user_role_user", columnList = "user_id"),
            @Index(name = "idx_user_role_role", columnList = "role_id")
        })
public class UserRole extends AuditableBaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_role_user"))
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_role_role"))
    Role role;
}
