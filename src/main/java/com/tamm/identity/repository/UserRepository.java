package com.tamm.identity.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tamm.identity.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByUsername(String username);

    Optional<User> findByUsername(String username);
    // Trong UserRepository, sửa query thành:
    @Query("SELECT u FROM User u " + "LEFT JOIN FETCH u.userRoles ur "
            + "LEFT JOIN FETCH ur.role r "
            + "LEFT JOIN FETCH r.rolePermissions rp "
            + "LEFT JOIN FETCH rp.permission "
            + "WHERE u.username = :username")
    Optional<User> findByUsernameWithRolesAndPermissions(@Param("username") String username);
}
