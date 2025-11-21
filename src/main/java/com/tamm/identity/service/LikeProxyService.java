package com.tamm.identity.service;

import org.springframework.stereotype.Service;

import com.tamm.identity.dto.request.ApiResponse;
import com.tamm.identity.repository.httpclient.PostClient;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LikeProxyService {
    PostClient postClient;

    public ApiResponse<Void> likePost(String postId) {
        return postClient.likePost(postId);
    }

    public ApiResponse<Void> unlikePost(String postId) {
        return postClient.unlikePost(postId);
    }
}
