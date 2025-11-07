package com.tamm.identity.entity;

import java.util.List;

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
        name = "permissions",
        indexes = {@Index(name = "idx_permission_name", columnList = "name")})
public class Permission extends AuditableBaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    String id;

    @Column(name = "name", unique = true, nullable = false, length = 100)
    String name;

    @Column(name = "description", length = 500)
    String description;

    @ToString.Exclude
    @OneToMany(mappedBy = "permission", cascade = CascadeType.ALL)
    List<RolePermission> rolePermissions;
}
