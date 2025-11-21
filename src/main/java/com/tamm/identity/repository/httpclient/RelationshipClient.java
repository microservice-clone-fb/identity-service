package com.tamm.identity.repository.httpclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import com.tamm.identity.configuration.AuthenticationRequestInterceptor;
import com.tamm.identity.dto.request.ApiResponse;
import com.tamm.identity.dto.response.relationshipuser.RelationshipUserResponse;

@FeignClient(
        name = "relationship-service",
        url = "${app.services.relationship}",
        configuration = {AuthenticationRequestInterceptor.class})
public interface RelationshipClient {

    // ===========================
    // USER RELATIONSHIP
    // ===========================

    // GET all relationships between user and others
    @GetMapping("/users/all-relationship/{userId}")
    ApiResponse<RelationshipUserResponse> getAllRelationship(@PathVariable String userId);

    // ===========================
    // USER CRUD
    // ===========================

    // Create a user
    @PostMapping(
            value = "/user/create",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    boolean createUser(@RequestBody String userId);
}
