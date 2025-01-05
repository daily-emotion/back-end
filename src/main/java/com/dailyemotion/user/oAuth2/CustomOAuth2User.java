package com.dailyemotion.user.oAuth2;

import com.dailyemotion.user.dto.response.UserResDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@RequiredArgsConstructor
public class CustomOAuth2User implements OAuth2User {

    private final UserResDto userResDto;

    @Override
    public Map<String, Object> getAttributes() {
        // OAuth2 인증 과정에서 필요한 속성이 있다면 여기에 추가
        return Collections.emptyMap();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(createAuthority());
    }

    @Override
    public String getName() {
        return userResDto.getName();
    }

    public String getUsername() {
        return userResDto.getUsername();
    }

    private GrantedAuthority createAuthority() {
        return () -> userResDto.getRole().getAuthority();
    }
}