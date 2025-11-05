package com.tamm.identity.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.tamm.identity.dto.request.RegistrationRequest;
import com.tamm.identity.dto.request.UserCreationRequest;
import com.tamm.identity.dto.request.UserUpdateRequest;
import com.tamm.identity.dto.response.UserResponse;
import com.tamm.identity.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreationRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userRoles", ignore = true)
    @Mapping(source = "email", target = "username")
    User toUser(RegistrationRequest request);

    UserResponse toUserResponse(User user);

    //    @Mapping(target = "roles", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);
}
