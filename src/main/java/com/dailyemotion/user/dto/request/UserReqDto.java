package com.dailyemotion.user.dto.request;

import com.dailyemotion.domain.entity.User;
import com.dailyemotion.domain.enums.Role;
import com.dailyemotion.domain.enums.SocialType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserReqDto {
    private String username;
    private String name;
    private Role role;
    private SocialType socialType;

    public User toEntity() {
        return User.builder()
                .username(username)
                .name(name)
                .role(role)
                .socialType(socialType)
                .build();
    }
}
