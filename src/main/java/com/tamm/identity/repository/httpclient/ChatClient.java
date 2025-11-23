package com.tamm.identity.repository.httpclient;

import com.tamm.identity.dto.request.*;
import com.tamm.identity.dto.request.chat.*;
import com.tamm.identity.dto.response.*;
import com.tamm.identity.dto.response.chat.ConversationDTO;
import com.tamm.identity.dto.response.chat.MessageDTO;
import com.tamm.identity.dto.response.chat.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "chat-service",
        url = "${app.services.chat}")
public interface ChatClient {

    // ==================== CONVERSATION ENDPOINTS ====================
    @GetMapping("/v1/api/conversations/{conversation_id}")
    ApiResponse<ConversationDTO> getUserConversation(
            @PathVariable String conversation_id,
            @RequestHeader("authorization") String token
    );

    @GetMapping("/v1/api/conversations")
    ApiResponse<java.util.List<ConversationDTO>> getAllUserConversations(
            @RequestHeader("authorization") String token
    );

    @PostMapping("/v1/api/conversations")
    ApiResponse<ConversationDTO> createConversation(
            @RequestBody CreateConversationRequest request,
            @RequestHeader("authorization") String token
    );

    @PatchMapping("/v1/api/conversations")
    ApiResponse<ConversationDTO> updateConversation(
            @RequestBody UpdateConversationRequest request,
            @RequestHeader("authorization") String token
    );

    @PostMapping("/v1/api/conversations/members")
    ApiResponse<Object> updateMembersToConversation(
            @RequestBody UpdateConversationMembersRequest request,
            @RequestHeader("authorization") String token
    );

    // ==================== MESSAGE ENDPOINTS ====================
    @GetMapping("/v1/api/conversations/{conversation_id}/messages")
    ApiResponse<java.util.List<MessageDTO>> getMessages(
            @PathVariable String conversation_id,
            @RequestHeader("authorization") String token
    );

    @PostMapping("/v1/api/messages")
    ApiResponse<MessageDTO> sendMessage(
            @RequestBody SendMessageRequest request,
            @RequestHeader("authorization") String token
    );

    @PutMapping("/v1/api/messages/{message_id}/revoke")
    ApiResponse<Object> revokeMessage(
            @PathVariable String message_id,
            @RequestHeader("authorization") String token
    );

    @PutMapping("/v1/api/messages/{message_id}/delete")
    ApiResponse<Object> deleteMessage(
            @PathVariable String message_id,
            @RequestHeader("authorization") String token
    );

    @PostMapping("/v1/api/messages/forward")
    ApiResponse<Object> forwardMessage(
            @RequestBody ForwardMessageRequest request,
            @RequestHeader("authorization") String token
    );

    @PutMapping("/v1/api/messages/mark-as-read")
    ApiResponse<Object> markMessageAsRead(
            @RequestBody MarkMessageAsReadRequest request,
            @RequestHeader("authorization") String token
    );

    @PostMapping("/v1/api/messages/reaction")
    ApiResponse<Object> addReaction(
            @RequestBody AddReactionRequest request,
            @RequestHeader("authorization") String token
    );

    @DeleteMapping("/v1/api/messages/reaction")
    ApiResponse<Object> removeReaction(
            @RequestBody RemoveReactionRequest request,
            @RequestHeader("authorization") String token
    );

    // ==================== USER ENDPOINTS ====================
    @PostMapping("/v1/api/user/create-user")
    ApiResponse<UserDTO> createUser(
            @RequestBody CreateUserRequest request
    );

    @GetMapping("/v1/api/user/info")
    ApiResponse<UserDTO> getUserInfo(
            @RequestHeader("authorization") String token
    );

    @PostMapping("/v1/api/user/update-info")
    ApiResponse<UserDTO> updateInfoUser(
            @RequestBody UpdateUserInfoRequest request,
            @RequestHeader("authorization") String token
    );

    @GetMapping("/v1/api/user/getUserBySearch/{search}")
    ApiResponse<java.util.List<UserDTO>> getUserBySearch(
            @PathVariable String search,
            @RequestParam(required = false) Boolean forGroup,
            @RequestHeader("authorization") String token
    );

    @PostMapping("/v1/api/user/update-status")
    ApiResponse<Object> updateUserStatus(
            @RequestBody UpdateUserStatusRequest request,
            @RequestHeader("authorization") String token
    );

    @PostMapping("/v1/api/user/check-password")
    ApiResponse<Object> checkPassword(
            @RequestBody CheckPasswordRequest request,
            @RequestHeader("authorization") String token
    );

    @PostMapping("/v1/api/user/create-password")
    ApiResponse<Object> createPassword(
            @RequestBody CreatePasswordRequest request,
            @RequestHeader("authorization") String token
    );

    @PostMapping("/v1/api/user/change-password")
    ApiResponse<Object> changePassword(
            @RequestBody ChangePasswordRequest request,
            @RequestHeader("authorization") String token
    );

    @PostMapping("/v1/api/user/edit-profile")
    ApiResponse<UserDTO> editProfile(
            @RequestBody EditProfileRequest request,
            @RequestHeader("authorization") String token
    );

    @PostMapping("/v1/api/user/send-friend-request/{friendId}")
    ApiResponse<Object> sendFriendRequest(
            @PathVariable String friendId,
            @RequestHeader("authorization") String token
    );

    @PostMapping("/v1/api/user/cancel-friend-request/{receiverId}")
    ApiResponse<Object> cancelFriendRequest(
            @PathVariable String receiverId,
            @RequestHeader("authorization") String token
    );

    @PostMapping("/v1/api/user/decline-friend-request/{senderId}")
    ApiResponse<Object> declineFriendRequest(
            @PathVariable String senderId,
            @RequestHeader("authorization") String token
    );

    @PostMapping("/v1/api/user/accept-friend-request/{senderId}")
    ApiResponse<Object> acceptFriendRequest(
            @PathVariable String senderId,
            @RequestHeader("authorization") String token
    );

    @PostMapping("/v1/api/user/unfriend/{friendId}")
    ApiResponse<Object> unfriend(
            @PathVariable String friendId,
            @RequestHeader("authorization") String token
    );

    @GetMapping("/v1/api/user/check-friendship/{friendId}")
    ApiResponse<Object> checkFriendShip(
            @PathVariable String friendId,
            @RequestHeader("authorization") String token
    );

    @GetMapping("/v1/api/user/check-send-friend-request/{friendId}")
    ApiResponse<Object> checkSendFriendRequest(
            @PathVariable String friendId,
            @RequestHeader("authorization") String token
    );

    @GetMapping("/v1/api/user/check-receive-friend-request/{friendId}")
    ApiResponse<Object> checkReceiveFriendRequest(
            @PathVariable String friendId,
            @RequestHeader("authorization") String token
    );

    @GetMapping("/v1/api/user/get-send-friend-request")
    ApiResponse<java.util.List<UserDTO>> getSendFriendRequest(
            @RequestHeader("authorization") String token
    );

    @GetMapping("/v1/api/user/get-receive-friend-request")
    ApiResponse<java.util.List<UserDTO>> getReceiveFriendRequest(
            @RequestHeader("authorization") String token
    );

    @GetMapping("/v1/api/user/get-friend-list")
    ApiResponse<java.util.List<UserDTO>> getFriendList(
            @RequestHeader("authorization") String token
    );

    @PostMapping("/v1/api/user/block-user/{userId}")
    ApiResponse<Object> blockUser(
            @PathVariable String userId,
            @RequestHeader("authorization") String token
    );

    @PostMapping("/v1/api/user/unblock-user/{userId}")
    ApiResponse<Object> unblockUser(
            @PathVariable String userId,
            @RequestHeader("authorization") String token
    );

    @GetMapping("/v1/api/user/check-blocked-user/{userId}")
    ApiResponse<Object> checkBlockedUser(
            @PathVariable String userId,
            @RequestHeader("authorization") String token
    );

    @GetMapping("/v1/api/user/check-is-blocked/{userId}")
    ApiResponse<Object> checkIsBlocked(
            @PathVariable String userId,
            @RequestHeader("authorization") String token
    );

    @GetMapping("/v1/api/user/get-blocked-user")
    ApiResponse<java.util.List<UserDTO>> getBlockedUser(
            @RequestHeader("authorization") String token
    );

    @GetMapping("/v1/api/user/getAllUser")
    ApiResponse<java.util.List<UserDTO>> getAllUser();

    // ==================== CALL ENDPOINTS ====================
    @PostMapping("/v1/api/sockets/end-call")
    ApiResponse<Object> endCall(
            @RequestBody EndCallRequest request
    );
}
