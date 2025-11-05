package com.tamm.identity.entity;

import jakarta.persistence.*;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
// @Table(name = "user_roles")
@FieldDefaults(level = AccessLevel.PRIVATE)
// @EqualsAndHashCode

// @IdClass(UserRole.UserRoleId.class)
@Entity
@Table(
        name = "users_roles",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "role_id"})})
public class UserRole extends AuditableBaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;

    @ManyToOne
    @JoinColumn(name = "role_id")
    Role role;

    //    @EqualsAndHashCode
    //    @AllArgsConstructor
    //    @NoArgsConstructor
    //    @Builder
    //    @Data
    //    public static class UserRoleId implements Serializable {
    //        private User user;
    //        private Role role;
    //    }
}
