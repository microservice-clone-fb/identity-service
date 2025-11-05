package com.tamm.identity.entity;

import jakarta.persistence.*;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
// @Table(name = "roles_permissions")
// @IdClass(RolePermission.RolePermissionId.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(
        name = "roles_permissions",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"role_id", "permission_id"})})
public class RolePermission extends AuditableBaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id")
    Permission permission;

    //    Instant createdAt;
    //    Instant lastUpdatedAt;
    //    String createdBy;
    //    String lastUpdatedBy;
    //    boolean isActive;
    //
    //    @Lob
    //    String history;

    //    @EqualsAndHashCode
    //    @AllArgsConstructor
    //    @NoArgsConstructor
    //    @Builder
    //    @Data
    //    public static class RolePermissionId implements Serializable {
    //        private Role role;
    //        private Permission permission;
    //
    //    }
}
