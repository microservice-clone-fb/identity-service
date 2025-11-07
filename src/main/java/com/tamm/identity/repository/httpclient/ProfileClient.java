package com.tamm.identity.repository.httpclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import com.tamm.identity.configuration.AuthenticationRequestInterceptor;
import com.tamm.identity.dto.request.ApiResponse;
import com.tamm.identity.dto.request.ProfileCreationRequest;
import com.tamm.identity.dto.response.UserProfileResponse;

@FeignClient(
        name = "profile-service",
        url = "${app.services.profile}",
        configuration = {AuthenticationRequestInterceptor.class})
public interface ProfileClient {
    @PostMapping(
            value = "/internal/users",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<UserProfileResponse> createProfile(@RequestBody ProfileCreationRequest request);

    @GetMapping(value = "/internal/user/exists/{email}")
    ApiResponse<UserProfileResponse> findByEmail(@PathVariable String email);

    @GetMapping(value = "/internal/users/by-any-field/{username}")
    ApiResponse<UserProfileResponse> findProfileByAnyField(@PathVariable String username);

    @GetMapping("/internal/users/{userId}")
    ApiResponse<UserProfileResponse> getProfileByUserId(@PathVariable String userId);
}
