package com.tamm.identity.service;

import org.springframework.stereotype.Service;

import com.tamm.identity.dto.PageResponse;
import com.tamm.identity.dto.request.ApiResponse;
import com.tamm.identity.dto.request.CommentRequest;
import com.tamm.identity.dto.response.CommentResponse;
import com.tamm.identity.repository.httpclient.PostClient;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentProxyService {
    PostClient postClient;

    public ApiResponse<CommentResponse> createComment(String postId, CommentRequest request) {
        return postClient.createComment(postId, request);
    }

    public ApiResponse<PageResponse<CommentResponse>> getCommentsByPostId(String postId, int page, int size) {
        return postClient.getCommentsByPostId(postId, page, size);
    }

    public ApiResponse<Void> deleteComment(String commentId) {
        return postClient.deleteComment(commentId);
    }
}
