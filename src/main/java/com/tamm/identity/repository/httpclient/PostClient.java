package com.tamm.identity.repository.httpclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.tamm.identity.configuration.AuthenticationRequestInterceptor;
import com.tamm.identity.dto.PageResponse;
import com.tamm.identity.dto.post.PostRequest;
import com.tamm.identity.dto.post.PostResponse;
import com.tamm.identity.dto.request.ApiResponse;
import com.tamm.identity.dto.request.CommentRequest;
import com.tamm.identity.dto.response.CommentResponse;
import com.tamm.identity.dto.response.LikeResponse;
import com.tamm.identity.dto.response.ShareResponse;

@FeignClient(
        name = "post-service",
        url = "${app.services.post}",
        configuration = {AuthenticationRequestInterceptor.class})
public interface PostClient {

    @PostMapping("/")
    ApiResponse<PostResponse> createPost(@RequestBody PostRequest request);

    @GetMapping
    ApiResponse<PageResponse<PostResponse>> getAllPosts(
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size);

    @GetMapping("/{postId}")
    ApiResponse<PostResponse> getPost(@PathVariable("postId") String postId);

    @GetMapping("/my-posts")
    ApiResponse<PageResponse<PostResponse>> getMyPosts(
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size);

    @GetMapping("/user/{userId}/posts")
    ApiResponse<PageResponse<PostResponse>> getPostsByUserId(
            @PathVariable("userId") String userId,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size);

    @PutMapping("/{postId}")
    ApiResponse<PostResponse> updatePost(@PathVariable("postId") String postId, @RequestBody PostRequest request);

    @DeleteMapping("/{postId}")
    ApiResponse<Void> deletePost(@PathVariable("postId") String postId);

    // Comment APIs
    @PostMapping("/{postId}/comment")
    ApiResponse<CommentResponse> createComment(
            @PathVariable("postId") String postId, @RequestBody CommentRequest request);

    @GetMapping("/{postId}/comments")
    ApiResponse<PageResponse<CommentResponse>> getCommentsByPostId(
            @PathVariable("postId") String postId,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size);

    @DeleteMapping("/comment/{commentId}")
    ApiResponse<Void> deleteComment(@PathVariable("commentId") String commentId);

    // Like APIs
    @PostMapping("/{postId}/like")
    ApiResponse<Void> likePost(@PathVariable("postId") String postId);

    @DeleteMapping("/{postId}/like")
    ApiResponse<Void> unlikePost(@PathVariable("postId") String postId);

    @GetMapping("/{postId}/likes")
    ApiResponse<PageResponse<LikeResponse>> getLikesByPostId(
            @PathVariable("postId") String postId,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size);

    // Share APIs
    @PostMapping("/{postId}/share")
    ApiResponse<Void> sharePost(@PathVariable("postId") String postId);

    @DeleteMapping("/{postId}/share")
    ApiResponse<Void> unsharePost(@PathVariable("postId") String postId);

    @GetMapping("/{postId}/shares")
    ApiResponse<PageResponse<ShareResponse>> getSharesByPostId(
            @PathVariable("postId") String postId,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size);
}
