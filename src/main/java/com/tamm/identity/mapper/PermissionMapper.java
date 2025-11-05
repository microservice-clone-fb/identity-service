package com.tamm.identity.mapper;

import org.mapstruct.Mapper;

import com.tamm.identity.dto.request.PermissionRequest;
import com.tamm.identity.dto.response.PermissionResponse;
import com.tamm.identity.entity.Permission;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);

    PermissionResponse toPermissionResponse(Permission permission);
}
