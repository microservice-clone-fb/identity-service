package com.tamm.identity.dto.response;

import com.tamm.identity.dto.PageResponse;
import com.tamm.identity.dto.post.PostResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileWithPostsResponse {
    private UserProfileResponse profile;
    private PageResponse<PostResponse> posts;
}
