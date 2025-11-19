package com.tamm.identity.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.tamm.identity.dto.request.ApiResponse;
import com.tamm.identity.dto.request.relationship.FriendRequestCancelRequest;
import com.tamm.identity.dto.request.relationship.FriendRequestRespondRequest;
import com.tamm.identity.dto.request.relationship.FriendRequestSendRequest;
import com.tamm.identity.dto.request.relationship.UnfriendRequest;
import com.tamm.identity.dto.response.relationshipuser.RelationshipUserResponse;
import com.tamm.identity.entity.User;
import com.tamm.identity.exception.AppException;
import com.tamm.identity.exception.ErrorCode;
import com.tamm.identity.repository.UserRepository;
import com.tamm.identity.repository.httpclient.RelationshipClient;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RelationshipService {

    RelationshipClient relationshipClient;
    UserRepository userRepository;

    public RelationshipUserResponse getMyRelationships() {
        String currentUserId = getCurrentUserId();
        ApiResponse<RelationshipUserResponse> response = relationshipClient.getAllRelationship(currentUserId);
        return response.getResult();
    }

    public RelationshipUserResponse getRelationshipsOf(String userId) {
        ApiResponse<RelationshipUserResponse> response = relationshipClient.getAllRelationship(userId);
        return response.getResult();
    }

    public void sendFriendRequest(FriendRequestSendRequest request) {
        request.setRequesterId(getCurrentUserId());
        relationshipClient.sendFriendRequest(request);
    }

    public void cancelFriendRequest(FriendRequestCancelRequest request) {
        request.setRequesterId(getCurrentUserId());
        relationshipClient.cancelFriendRequest(request);
    }

    public void respondFriendRequest(FriendRequestRespondRequest request) {
        request.setTargetUserId(getCurrentUserId());
        relationshipClient.respondFriendRequest(request);
    }

    public void unfriend(UnfriendRequest request) {
        request.setUserId(getCurrentUserId());
        relationshipClient.unfriend(request);
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String principal = authentication.getName();

        return userRepository
                .findById(principal)
                .or(() -> userRepository.findByUsername(principal))
                .map(User::getId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }
}
