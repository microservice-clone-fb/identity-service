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
public class Role extends AuditableBaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    String name;
    String description;

    @ToString.Exclude
    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL)
    List<UserRole> userRoles;

    @ToString.Exclude
    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL)
    List<RolePermission> rolePermissions;
}
