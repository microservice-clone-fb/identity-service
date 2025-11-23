package com.tamm.identity.controller;

import org.springframework.web.bind.annotation.*;

import com.tamm.identity.dto.PageResponse;
import com.tamm.identity.dto.request.ApiResponse;
import com.tamm.identity.dto.response.ShareResponse;
import com.tamm.identity.service.PostProxyService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ShareController {
    PostProxyService postProxyService;

    @PostMapping("/{postId}/share")
    public ApiResponse<Void> sharePost(@PathVariable String postId) {
        return postProxyService.sharePost(postId);
    }

    @DeleteMapping("/{postId}/share")
    public ApiResponse<Void> unsharePost(@PathVariable String postId) {
        return postProxyService.unsharePost(postId);
    }

    @GetMapping("/{postId}/shares")
    public ApiResponse<PageResponse<ShareResponse>> getSharesByPostId(
            @PathVariable String postId,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size) {
        return postProxyService.getSharesByPostId(postId, page, size);
    }
}
