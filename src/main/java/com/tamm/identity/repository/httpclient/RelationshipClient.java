package com.tamm.identity.repository.httpclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.tamm.identity.configuration.AuthenticationRequestInterceptor;
import com.tamm.identity.dto.request.ApiResponse;
import com.tamm.identity.dto.request.relationship.FriendRequestCancelRequest;
import com.tamm.identity.dto.request.relationship.FriendRequestRespondRequest;
import com.tamm.identity.dto.request.relationship.FriendRequestSendRequest;
import com.tamm.identity.dto.request.relationship.UnfriendRequest;
import com.tamm.identity.dto.response.relationshipuser.RelationshipUserResponse;

@FeignClient(
        name = "relationship-service",
        url = "${app.services.relationship}",
        configuration = {AuthenticationRequestInterceptor.class})
public interface RelationshipClient {

    @GetMapping("/users/all-relationship/{userId}")
    ApiResponse<RelationshipUserResponse> getAllRelationship(@PathVariable String userId);

    @PostMapping(
            value = "/user/create",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    boolean createUser(@RequestBody String userId);

    @PostMapping("/users/friend-requests/send")
    ApiResponse<String> sendFriendRequest(@RequestBody FriendRequestSendRequest request);

    @PostMapping("/users/friend-requests/cancel")
    ApiResponse<String> cancelFriendRequest(@RequestBody FriendRequestCancelRequest request);

    @PostMapping("/users/friend-requests/respond")
    ApiResponse<String> respondFriendRequest(@RequestBody FriendRequestRespondRequest request);

    @PostMapping("/users/friendships/remove")
    ApiResponse<String> unfriend(@RequestBody UnfriendRequest request);
}
