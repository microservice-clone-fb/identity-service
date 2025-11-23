package com.tamm.identity.service;

import org.springframework.stereotype.Service;

import com.tamm.identity.dto.PageResponse;
import com.tamm.identity.dto.post.PostRequest;
import com.tamm.identity.dto.post.PostResponse;
import com.tamm.identity.dto.request.ApiResponse;
import com.tamm.identity.dto.request.CommentRequest;
import com.tamm.identity.dto.response.CommentResponse;
import com.tamm.identity.dto.response.LikeResponse;
import com.tamm.identity.dto.response.ShareResponse;
import com.tamm.identity.repository.httpclient.PostClient;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostProxyService {
    PostClient postClient;

    public ApiResponse<PostResponse> createPost(PostRequest request) {
        return postClient.createPost(request);
    }

    public ApiResponse<PageResponse<PostResponse>> getAllPosts(int page, int size) {
        return postClient.getAllPosts(page, size);
    }

    public ApiResponse<PostResponse> getPost(String postId) {
        return postClient.getPost(postId);
    }

    public ApiResponse<PageResponse<PostResponse>> getMyPosts(int page, int size) {
        return postClient.getMyPosts(page, size);
    }

    public ApiResponse<PageResponse<PostResponse>> getPostsByUserId(String userId, int page, int size) {
        return postClient.getPostsByUserId(userId, page, size);
    }

    public ApiResponse<PostResponse> updatePost(String postId, PostRequest request) {
        return postClient.updatePost(postId, request);
    }

    public ApiResponse<Void> deletePost(String postId) {
        return postClient.deletePost(postId);
    }

    // Comment methods
    public ApiResponse<CommentResponse> createComment(String postId, CommentRequest request) {
        return postClient.createComment(postId, request);
    }

    public ApiResponse<PageResponse<CommentResponse>> getCommentsByPostId(String postId, int page, int size) {
        return postClient.getCommentsByPostId(postId, page, size);
    }

    public ApiResponse<Void> deleteComment(String commentId) {
        return postClient.deleteComment(commentId);
    }

    // Like methods
    public ApiResponse<Void> likePost(String postId) {
        return postClient.likePost(postId);
    }

    public ApiResponse<Void> unlikePost(String postId) {
        return postClient.unlikePost(postId);
    }

    public ApiResponse<PageResponse<LikeResponse>> getLikesByPostId(String postId, int page, int size) {
        return postClient.getLikesByPostId(postId, page, size);
    }

    // Share methods
    public ApiResponse<Void> sharePost(String postId) {
        return postClient.sharePost(postId);
    }

    public ApiResponse<Void> unsharePost(String postId) {
        return postClient.unsharePost(postId);
    }

    public ApiResponse<PageResponse<ShareResponse>> getSharesByPostId(String postId, int page, int size) {
        return postClient.getSharesByPostId(postId, page, size);
    }
}
