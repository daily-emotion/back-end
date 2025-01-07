package com.dailyemotion.domain.entity;

import com.dailyemotion.domain.enums.Role;
import com.dailyemotion.domain.enums.SocialType;
import com.dailyemotion.user.dto.response.UserResDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseTimeEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Enumerated(EnumType.STRING)
    private SocialType socialType;

    private String username;

    private String name;

    @Enumerated(EnumType.STRING)
    private Role role;


    //사용자 정보 업데이트
    public void updateFromDTO(UserResDto userResDto) {
        if (userResDto != null && userResDto.getName() != null) {
            this.name = userResDto.getName();
        }
    }
}
