package com.tamm.identity.controller;

import org.springframework.web.bind.annotation.*;

import com.tamm.identity.dto.PageResponse;
import com.tamm.identity.dto.request.ApiResponse;
import com.tamm.identity.dto.response.LikeResponse;
import com.tamm.identity.service.PostProxyService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LikeController {
    PostProxyService postProxyService;

    @PostMapping("/{postId}/like")
    public ApiResponse<Void> likePost(@PathVariable String postId) {
        return postProxyService.likePost(postId);
    }

    @DeleteMapping("/{postId}/like")
    public ApiResponse<Void> unlikePost(@PathVariable String postId) {
        return postProxyService.unlikePost(postId);
    }

    @GetMapping("/{postId}/likes")
    public ApiResponse<PageResponse<LikeResponse>> getLikesByPostId(
            @PathVariable String postId,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size) {
        return postProxyService.getLikesByPostId(postId, page, size);
    }
}
