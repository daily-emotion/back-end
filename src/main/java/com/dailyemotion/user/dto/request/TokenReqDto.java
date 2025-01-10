package com.dailyemotion.user.dto.request;


import com.dailyemotion.domain.enums.Role;
import lombok.Data;

@Data
public class TokenReqDto {

    private String username;
    private String name;
    private Role role;
    private Long expiredMs;

}