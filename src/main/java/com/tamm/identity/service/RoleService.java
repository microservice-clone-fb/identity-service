package com.tamm.identity.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.tamm.identity.dto.request.RoleRequest;
import com.tamm.identity.dto.response.RoleResponse;
import com.tamm.identity.entity.Role;
import com.tamm.identity.mapper.RoleMapper;
import com.tamm.identity.repository.PermissionRepository;
import com.tamm.identity.repository.RoleRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleService {
    RoleRepository roleRepository;
    PermissionRepository permissionRepository;
    RoleMapper roleMapper;

    public RoleResponse create(RoleRequest request) {
        var role = roleMapper.toRole(request);

        var permissions = permissionRepository.findAllById(request.getPermissions());
        //        role.setPermissions(new HashSet<>(permissions));

        role = roleRepository.save(role);
        return roleMapper.toRoleResponse(role);
    }

    public Role getRoleEntityById(String name) {
        return roleRepository.findByName(name).orElse(null);
    }

    public List<RoleResponse> getAll() {
        return roleRepository.findAll().stream().map(roleMapper::toRoleResponse).toList();
    }

    public void delete(String role) {
        roleRepository.deleteById(role);
    }
}
