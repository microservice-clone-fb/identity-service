package com.tamm.identity.mapper;

import org.mapstruct.Mapper;

import com.tamm.identity.dto.request.RoleRequest;
import com.tamm.identity.dto.response.RoleResponse;
import com.tamm.identity.entity.Role;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    //    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest request);

    RoleResponse toRoleResponse(Role role);
}
