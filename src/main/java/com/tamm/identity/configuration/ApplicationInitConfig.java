package com.tamm.identity.configuration;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.tamm.identity.constant.PredefinedRole;
import com.tamm.identity.entity.*;
import com.tamm.identity.repository.*;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Transactional
public class ApplicationInitConfig {

    PasswordEncoder passwordEncoder;

    @NonFinal
    static final String ADMIN_USER_NAME = "admin";

    @NonFinal
    static final String ADMIN_PASSWORD = "admin";

    @NonFinal
    static final String USER_USER_NAME = "user";

    @NonFinal
    static final String USER_PASSWORD = "user";

    @Bean
    ApplicationRunner applicationRunner(
            UserRepository userRepository,
            RoleRepository roleRepository,
            UserRoleRepository userRoleRepository,
            PermissionRepository permissionRepository,
            RolePermissionRepository rolePermissionRepository,
            EntityManager entityManager) {
        log.info("Initializing application.....");
        return args -> {

            // 1. Tạo tất cả permissions
            Set<Permission> allPermissions = createPermissions(permissionRepository);

            // 2. Tạo User permissions (chỉ CREATE, GET, UPDATE, FIND, ACTIVE, INACTIVE)
            Set<Permission> userPermissions = createUserPermissions(allPermissions);

            // 3. Tạo roles (chỉ tạo role entity, chưa gán permission)
            Role userRole = createUserRole(roleRepository);
            Role adminRole = createAdminRole(roleRepository);

            // 4. Tạo role-permission relationships
            createRolePermissions(
                    rolePermissionRepository, roleRepository, permissionRepository, userRole, userPermissions);
            createRolePermissions(
                    rolePermissionRepository, roleRepository, permissionRepository, adminRole, allPermissions);

            // 5. Tạo users nếu cần (hiện tại đang comment)
            createUsers(userRepository, userRoleRepository, userRole, adminRole, roleRepository);

            log.info("Application initialization completed .....");
        };
    }

    private Set<Permission> createPermissions(PermissionRepository permissionRepository) {
        log.info("Creating permissions...");

        String[] actions = {"CREATE", "UPDATE", "GET", "FIND", "GETALL", "DELETE", "ACTIVE", "INACTIVE"};
        String[] resources = {
            "USER", "PROFILE", "ROLE", "PERMISSION", "RELATIONSHIP", "FILE", "POST", "MESSAGE", "GROUP"
        };

        Set<Permission> permissions = new HashSet<>();

        for (String action : actions) {
            for (String resource : resources) {
                String permissionName = action + "_" + resource;

                // Kiểm tra permission đã tồn tại chưa
                Permission permission = permissionRepository
                        .findByName(permissionName)
                        .orElseGet(() -> {
                            Permission newPermission = Permission.builder()
                                    .name(permissionName)
                                    .description(action + " permission for " + resource.toLowerCase())
                                    .build();
                            return permissionRepository.save(newPermission);
                        });

                permissions.add(permission);
            }
        }

        log.info("Created {} permissions", permissions.size());
        return permissions;
    }

    private Set<Permission> createUserPermissions(Set<Permission> allPermissions) {
        String[] allowedActions = {"CREATE", "GET", "UPDATE", "FIND", "ACTIVE", "INACTIVE"};

        Set<Permission> userPermissions = new HashSet<>();

        for (Permission permission : allPermissions) {
            for (String allowedAction : allowedActions) {
                if (permission.getName().startsWith(allowedAction + "_")) {
                    userPermissions.add(permission);
                    break;
                }
            }
        }

        log.info("User will have {} permissions", userPermissions.size());
        return userPermissions;
    }

    /**
     * Tạo USER role (chỉ tạo role entity, không gán permission)
     */
    private Role createUserRole(RoleRepository roleRepository) {
        log.info("Creating USER role...");

        return roleRepository.findByName(PredefinedRole.USER_ROLE).orElseGet(() -> {
            Role role = Role.builder()
                    .name(PredefinedRole.USER_ROLE)
                    .description("User role with limited permissions")
                    .build();

            Role savedRole = roleRepository.save(role);
            log.info("Created USER role with ID: {}", savedRole.getId());
            return savedRole;
        });
    }

    /**
     * Tạo ADMIN role (chỉ tạo role entity, không gán permission)
     */
    private Role createAdminRole(RoleRepository roleRepository) {
        log.info("Creating ADMIN role...");

        return roleRepository.findByName(PredefinedRole.ADMIN_ROLE).orElseGet(() -> {
            Role role = Role.builder()
                    .name(PredefinedRole.ADMIN_ROLE)
                    .description("Admin role with full permissions")
                    .build();

            Role savedRole = roleRepository.save(role);
            log.info("Created ADMIN role with ID: {}", savedRole.getId());
            return savedRole;
        });
    }

    /**
     * Tạo role-permission relationships
     */
    protected void createRolePermissions(
            RolePermissionRepository rolePermissionRepository,
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            Role role,
            Set<Permission> permissions) {
        log.info("Creating role-permission relationships for role: {}", role.getName());

        int createdCount = 0;

        //        var attachedRole = entityManager.merge(role);
        var attachedRole = roleRepository.findById(role.getId()).orElseThrow();

        for (Permission permission : permissions) {
            // Kiểm tra xem relationship đã tồn tại chưa
            boolean exists = rolePermissionRepository.existsByRoleIdAndPermissionId(
                    attachedRole.getId(),
                    //                    role.getId(),
                    permission.getId());

            if (!exists) {
                // Re-attach permission to current session

                //                var attachedPermission = entityManager.merge(permission);
                var attachedPermission =
                        permissionRepository.findById(permission.getId()).orElseThrow();

                RolePermission rolePermission = new RolePermission();
                rolePermission.setRole(attachedRole); // attachedRole
                rolePermission.setPermission(attachedPermission); // attachedPermission

                //                rolePermission = entityManager.merge(rolePermission);

                rolePermissionRepository.save(rolePermission);
                createdCount++;
            }
        }

        log.info("Created {} role-permission relationships for role: {}", createdCount, role.getName());
    }

    @Transactional
    public void createUsers(
            UserRepository userRepository,
            UserRoleRepository userRoleRepository,
            Role userRole,
            Role adminRole,
            RoleRepository roleRepository) {

        // ==== Tạo admin user ====
        if (userRepository.findByUsername(ADMIN_USER_NAME).isEmpty()) {
            log.info("Creating admin user...");

            // 1. Tạo user mới
            User adminUser = User.builder()
                    .username(ADMIN_USER_NAME)
                    .password(passwordEncoder.encode(ADMIN_PASSWORD))
                    .build();

            User savedAdminUser = userRepository.save(adminUser);

            // 2. Lấy lại role đã có (attached entity)
            Role attachedAdminRole = roleRepository
                    .findById(adminRole.getId())
                    .orElseThrow(() -> new IllegalStateException("Admin role not found"));

            log.info("Admin role found with ID: {}", attachedAdminRole);

            // 3. Tạo quan hệ UserRole
            UserRole adminUserRole = new UserRole();
            adminUserRole.setUser(savedAdminUser); // dùng savedAdminUser chứ ko phải adminUser
            adminUserRole.setRole(attachedAdminRole);

            // 4. Lưu UserRole
            userRoleRepository.save(adminUserRole);

            log.warn(
                    "Admin user created with username: {} and password: {} - PLEASE CHANGE DEFAULT PASSWORD!",
                    ADMIN_USER_NAME,
                    ADMIN_PASSWORD);
        }

        // ==== Tạo normal user ====
        if (userRepository.findByUsername(USER_USER_NAME).isEmpty()) {
            log.info("Creating normal user...");

            // 1. Tạo user mới
            User normalUser = User.builder()
                    .username(USER_USER_NAME)
                    .password(passwordEncoder.encode(USER_PASSWORD))
                    .build();

            User savedNormalUser = userRepository.save(normalUser);

            // 2. Lấy lại role user đã có
            Role attachedUserRole = roleRepository
                    .findById(userRole.getId())
                    .orElseThrow(() -> new IllegalStateException("User role not found"));

            // 3. Tạo quan hệ UserRole
            UserRole normalUserRole = new UserRole();
            normalUserRole.setUser(savedNormalUser);
            normalUserRole.setRole(attachedUserRole);

            // 4. Lưu UserRole
            userRoleRepository.save(normalUserRole);

            log.info("Normal user created with username: {} and password: {}", USER_USER_NAME, USER_PASSWORD);
        }
    }
}
