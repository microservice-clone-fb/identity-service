package com.tamm.identity.controller;

import org.springframework.web.bind.annotation.*;

import com.tamm.identity.dto.PageResponse;
import com.tamm.identity.dto.post.PostRequest;
import com.tamm.identity.dto.post.PostResponse;
import com.tamm.identity.dto.request.ApiResponse;
import com.tamm.identity.service.PostProxyService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostController {
    PostProxyService postProxyService;

    @PostMapping
    public ApiResponse<PostResponse> createPost(@RequestBody PostRequest request) {
        return postProxyService.createPost(request);
    }

    @GetMapping
    public ApiResponse<PageResponse<PostResponse>> getAllPosts(
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size) {
        return postProxyService.getAllPosts(page, size);
    }

    @GetMapping("/{postId}")
    public ApiResponse<PostResponse> getPost(@PathVariable String postId) {
        return postProxyService.getPost(postId);
    }

    @GetMapping("/my-posts")
    public ApiResponse<PageResponse<PostResponse>> myPosts(
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size) {
        return postProxyService.getMyPosts(page, size);
    }

    @GetMapping("/user/{userId}/posts")
    public ApiResponse<PageResponse<PostResponse>> getPostsByUserId(
            @PathVariable String userId,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size) {
        return postProxyService.getPostsByUserId(userId, page, size);
    }

    @PutMapping("/{postId}")
    public ApiResponse<PostResponse> updatePost(@PathVariable String postId, @RequestBody PostRequest request) {
        return postProxyService.updatePost(postId, request);
    }

    @DeleteMapping("/{postId}")
    public ApiResponse<Void> deletePost(@PathVariable String postId) {
        return postProxyService.deletePost(postId);
    }
}
