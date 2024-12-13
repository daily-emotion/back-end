package com.dailyemotion.domain.entity;

import com.dailyemotion.domain.enums.Role;
import com.dailyemotion.domain.enums.SocialType;
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

}
