package com.tamm.identity.controller;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tamm.identity.dto.request.ApiResponse;
import com.tamm.identity.dto.request.relationship.FriendRequestCancelRequest;
import com.tamm.identity.dto.request.relationship.FriendRequestRespondRequest;
import com.tamm.identity.dto.request.relationship.FriendRequestSendRequest;
import com.tamm.identity.dto.request.relationship.UnfriendRequest;
import com.tamm.identity.dto.response.relationshipuser.RelationshipUserResponse;
import com.tamm.identity.service.RelationshipService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/relationships")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RelationshipController {

    RelationshipService relationshipService;

    @GetMapping("/me")
    ApiResponse<RelationshipUserResponse> getMyRelationships() {
        return ApiResponse.<RelationshipUserResponse>builder()
                .result(relationshipService.getMyRelationships())
                .build();
    }

    @GetMapping("/{userId}")
    ApiResponse<RelationshipUserResponse> getRelationshipsOf(@PathVariable String userId) {
        return ApiResponse.<RelationshipUserResponse>builder()
                .result(relationshipService.getRelationshipsOf(userId))
                .build();
    }

    @PostMapping("/friend-requests")
    ApiResponse<String> sendFriendRequest(@Valid @RequestBody FriendRequestSendRequest request) {
        relationshipService.sendFriendRequest(request);
        return ApiResponse.<String>builder().result("Friend request sent").build();
    }

    @PostMapping("/friend-requests/cancel")
    ApiResponse<String> cancelFriendRequest(@Valid @RequestBody FriendRequestCancelRequest request) {
        relationshipService.cancelFriendRequest(request);
        return ApiResponse.<String>builder().result("Friend request cancelled").build();
    }

    @PostMapping("/friend-requests/respond")
    ApiResponse<String> respondFriendRequest(@Valid @RequestBody FriendRequestRespondRequest request) {
        relationshipService.respondFriendRequest(request);
        return ApiResponse.<String>builder().result("Friend request updated").build();
    }

    @PostMapping("/friendships/remove")
    ApiResponse<String> unfriend(@Valid @RequestBody UnfriendRequest request) {
        relationshipService.unfriend(request);
        return ApiResponse.<String>builder().result("Friend removed").build();
    }
}
