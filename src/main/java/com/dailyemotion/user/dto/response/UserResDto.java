package com.dailyemotion.user.dto.response;

import com.dailyemotion.domain.enums.Role;
import com.dailyemotion.domain.enums.SocialType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResDto {

    private Long userId;
    private String username;
    private String name;
    private Role role;
    private SocialType socialType;

    public UserResDto(String username, String name) {
        this.username = username;
        this.name = name;
    }
}
