package com.tamm.identity.controller;

import org.springframework.web.bind.annotation.*;

import com.tamm.identity.dto.PageResponse;
import com.tamm.identity.dto.request.ApiResponse;
import com.tamm.identity.dto.request.CommentRequest;
import com.tamm.identity.dto.response.CommentResponse;
import com.tamm.identity.service.PostProxyService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentController {
    PostProxyService postProxyService;

    @PostMapping("/{postId}/comment")
    public ApiResponse<CommentResponse> createComment(
            @PathVariable String postId, @RequestBody CommentRequest request) {
        return postProxyService.createComment(postId, request);
    }

    @GetMapping("/{postId}/comments")
    public ApiResponse<PageResponse<CommentResponse>> getCommentsByPostId(
            @PathVariable String postId,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size) {
        return postProxyService.getCommentsByPostId(postId, page, size);
    }

    @DeleteMapping("/comment/{commentId}")
    public ApiResponse<Void> deleteComment(@PathVariable String commentId) {
        return postProxyService.deleteComment(commentId);
    }
}
