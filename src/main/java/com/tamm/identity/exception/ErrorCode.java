package com.tamm.identity.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // Common errors
    UNCATEGORIZED_EXCEPTION(9999, "Unexpected error", HttpStatus.INTERNAL_SERVER_ERROR),
    NOT_FOUND(9998, "Resource not found", HttpStatus.NOT_FOUND),
    LOGIN_FAILED(9997, "Username or password is incorrect", HttpStatus.UNAUTHORIZED),

    // 1xxx - Authentication related errors
    UNAUTHENTICATED(1001, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1002, "You do not have permission", HttpStatus.FORBIDDEN),

    // 2xxx - Identity Service
    INVALID_KEY(2001, "Invalid key", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(2002, "User not exists", HttpStatus.NOT_FOUND),
    USER_EXISTED(2003, "User already exists", HttpStatus.CONFLICT),
    REFRESH_TOKEN_EXPIRED(2004, "Refresh token expired", HttpStatus.BAD_REQUEST),

    // 3xxx - Profile Service
    USER_PROFILE_NOT_FOUND(3101, "User profile not found", HttpStatus.NOT_FOUND),
    USER_PROFILE_ALREADY_EXISTS(3102, "User profile already exists", HttpStatus.CONFLICT),

    // 4xxx - File Service
    FILE_NOT_UPLOADED_CORRECTLY(4101, "File not uploaded correctly", HttpStatus.UNSUPPORTED_MEDIA_TYPE),
    FILE_NOT_FOUND(4102, "File not found", HttpStatus.NOT_FOUND),
    FILE_UPLOAD_FAILED(4103, "File upload failed", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_TOO_LARGE(4104, "File size exceeds the maximum allowed limit", HttpStatus.PAYLOAD_TOO_LARGE),
    FILE_INVALID_NAME(4105, "Invalid file name", HttpStatus.BAD_REQUEST),
    INVALID_FILE_TYPE(4106, "Invalid file type", HttpStatus.BAD_REQUEST),
    POST_ID_REQUIRED(4107, "Post ID is required for post image upload", HttpStatus.BAD_REQUEST),
    STORY_ID_REQUIRED(4108, "Story ID is required for story image upload", HttpStatus.BAD_REQUEST),
    MESSAGE_ID_REQUIRED(4109, "Message ID is required for message image upload", HttpStatus.BAD_REQUEST),

    // 5xxx - Post Service
    POST_NOT_FOUND(5101, "Post not found", HttpStatus.NOT_FOUND),

    // 6xxx - Notification Service
    CANNOT_SEND_EMAIL(6101, "Cannot send email", HttpStatus.BAD_REQUEST),

    // 7xxx - Relationship Service
    // 7xxx - Relationship Service
    RELATIONSHIP_NOT_FOUND(7101, "Relationship not found", HttpStatus.NOT_FOUND),
    INVALID_REQUEST(7102, "Invalid request", HttpStatus.BAD_REQUEST),
    USER_BLOCKED(7103, "User is blocked", HttpStatus.FORBIDDEN),
    RELATIONSHIP_ALREADY_EXISTS(7104, "Relationship already exists", HttpStatus.CONFLICT),
    FRIEND_REQUEST_NOT_FOUND(7105, "Friend request not found", HttpStatus.NOT_FOUND),
    INVALID_FRIEND_REQUEST_STATUS(7106, "Invalid friend request status", HttpStatus.BAD_REQUEST),
    FRIENDSHIP_NOT_FOUND(7107, "Friendship not found", HttpStatus.NOT_FOUND),
    NOT_FRIENDS(7108, "Users are not friends", HttpStatus.BAD_REQUEST),
    ALREADY_FOLLOWING(7109, "Already following this user", HttpStatus.CONFLICT),
    NOT_FOLLOWING(7110, "Not following this user", HttpStatus.BAD_REQUEST),
    ALREADY_BLOCKED(7111, "User is already blocked", HttpStatus.CONFLICT),
    USER_NOT_BLOCKED(7112, "User is not blocked", HttpStatus.BAD_REQUEST),
    USER_CANNOT_CREATED(7113, "User cannot be created", HttpStatus.INTERNAL_SERVER_ERROR),

    // 7200 - Entity CRUD errors
    GROUP_NOT_FOUND(7201, "Group not found", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND(7202, "User not found", HttpStatus.NOT_FOUND),
    LOCATION_NOT_FOUND(7203, "Location not found", HttpStatus.NOT_FOUND),
    PAGE_NOT_FOUND(7204, "Page not found", HttpStatus.NOT_FOUND),
    USERNAME_ALREADY_EXISTS(7205, "Username already exists", HttpStatus.CONFLICT),

    // 8xxx - Conversation Service
    CONVERSATION_NOT_FOUND(8101, "Conversation not found", HttpStatus.NOT_FOUND),

    // 9xxx - Call Service
    CALL_NOT_FOUND(9101, "Call not found", HttpStatus.NOT_FOUND),

    // 10xxx - Analysis Service
    ANALYSIS_FAILED(10101, "Analysis failed", HttpStatus.INTERNAL_SERVER_ERROR),
    ;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}
