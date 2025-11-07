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
        name = "roles_permissions",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_role_permission",
                    columnNames = {"role_id", "permission_id"})
        },
        indexes = {
            @Index(name = "idx_role_permission_role", columnList = "role_id"),
            @Index(name = "idx_role_permission_permission", columnList = "permission_id")
        })
public class RolePermission extends AuditableBaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false, foreignKey = @ForeignKey(name = "fk_role_permission_role"))
    Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "permission_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_role_permission_permission"))
    Permission permission;
}
