package com.dailyemotion.user.dto.response;

import com.dailyemotion.domain.enums.Role;
import com.dailyemotion.domain.enums.SocialType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder (toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class UserResDto {

    private Long userId;
    private String username;
    private String name;
    private String email;
    private Role role;
    private SocialType socialType;

    public UserResDto(String username, String name, String email, Role role, SocialType socialType) {
        this.username = username;
        this.name = name;
        this.role = role;
        this.socialType = socialType;
    }
}
