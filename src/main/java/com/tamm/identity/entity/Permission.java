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
@Table(name = "permissions")
public class Permission extends AuditableBaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(name = "name", unique = true)
    String name;

    @Column(name = "description")
    String description;

    @ToString.Exclude
    @OneToMany(mappedBy = "permission", cascade = CascadeType.ALL)
    List<RolePermission> rolePermissions;
}
