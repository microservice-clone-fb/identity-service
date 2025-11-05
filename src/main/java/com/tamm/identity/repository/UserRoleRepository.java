package com.tamm.identity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tamm.identity.entity.UserRole;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, String> {}
