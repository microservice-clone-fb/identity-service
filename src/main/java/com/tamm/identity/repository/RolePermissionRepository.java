package com.tamm.identity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tamm.identity.entity.RolePermission;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, String> {
    boolean existsByRoleIdAndPermissionId(String roleId, String permissionId);
}
