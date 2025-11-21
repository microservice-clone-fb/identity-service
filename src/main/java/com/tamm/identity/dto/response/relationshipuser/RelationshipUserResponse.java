package com.tamm.identity.dto.response.relationshipuser;

import java.util.Set;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RelationshipUserResponse {
    String userId;
    Set<String> friends;
    Set<String> blocked;
    Set<String> dating;
    Set<String> family;
    Set<String> colleagues;
    Set<String> following;
    Set<String> closeFriends;
    Set<String> anotherUserFollowedIt; //  nhung nguoi theo doi nguoi nay
}
