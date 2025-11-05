package com.tamm.identity.mapper;

import org.mapstruct.Mapper;

import com.tamm.identity.dto.request.ProfileCreationRequest;
import com.tamm.identity.dto.request.UserCreationRequest;

@Mapper(componentModel = "spring")
public interface ProfileMapper {
    ProfileCreationRequest toProfileCreationRequest(UserCreationRequest request);
}
