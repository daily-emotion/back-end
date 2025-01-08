package com.dailyemotion.user.oAuth2;

import com.dailyemotion.domain.entity.User;
import com.dailyemotion.domain.enums.Role;
import com.dailyemotion.domain.enums.SocialType;
import com.dailyemotion.user.repository.UserRepository;
import com.dailyemotion.user.dto.request.UserReqDto;
import com.dailyemotion.user.dto.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    /**
     * OAuth2 인증 과정에서 유저 정보를 불러오고 처리하는 메소드
     * DefaultOAuth2UserService의 loadUser를 오버라이드하여 소셜 로그인 후 사용자 정보를 처리
     *
     * @param userRequest OAuth2 인증 요청 정보를 담고 있는 객체
     * @return 인증된 사용자 정보를 담고 있는 OAuth2User 객체
     * @throws OAuth2AuthenticationException 인증 과정에서 오류가 발생한 경우
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        OAuth2Response oAuth2Response = getOAuth2Response(oAuth2User.getAttributes(), registrationId);
        if (oAuth2Response == null) {
            return null;
        }

        String username = oAuth2Response.getProvider() + " " + oAuth2Response.getProviderId();
        Optional<User> optionalUser = userRepository.findByUsername(username);

        return optionalUser.map(user -> updateExistingUser(user, oAuth2Response))
                .orElseGet(() -> createNewUser(username, oAuth2Response));
    }

    /**
     * 소셜 로그인 제공자(Provider)별로 적절한 OAuth2Response 객체를 생성.
     * 각 소셜 로그인 제공자(네이버, 카카오, 구글)의 응답 형식에 맞는 객체를 반환
     *
     * @param attributes 소셜 로그인 제공자로부터 받은 사용자 속성 정보
     * @param registrationId 소셜 로그인 제공자 식별자
     * @return 제공자별로 파싱된 OAuth2Response 객체, 지원하지 않는 제공자인 경우 null
     */
    private OAuth2Response getOAuth2Response(Map<String, Object> attributes, String registrationId) {
        switch (registrationId) {
            case "naver":
                return new NaverResponse(attributes);
            case "kakao":
                return new KakaoResponse(attributes);
            case "google":
                return new GoogleResponse(attributes);
            default:
                log.warn("Unsupported registrationId: {}", registrationId);
                return null;
        }
    }

    /**
     * 새로운 사용자를 생성하고 데이터베이스에 저장
     * 소셜 로그인을 통해 처음 접근한 사용자의 경우 이 메소드를 통해 회원가입이 진행
     *
     * @param username 고유한 사용자 식별자 (제공자_ID 형식)
     * @param oAuth2Response 소셜 로그인 제공자로부터 받은 사용자 정보
     * @return 생성된 사용자 정보를 담고 있는 CustomOAuth2User 객체
     */
    private CustomOAuth2User createNewUser(String username, OAuth2Response oAuth2Response) {
        SocialType socialType = SocialType.valueOf(oAuth2Response.getProvider().toUpperCase());

        UserReqDto userRequestDto = UserReqDto.builder()
                .username(username)
                .name(oAuth2Response.getName())
                .role(Role.USER)
                .socialType(socialType)
                .build();

        User user = userRequestDto.toEntity();
        userRepository.save(user);

        UserResDto userResponseDto = createUserResponseDto(username, oAuth2Response, socialType);
        log.debug("Created new user: {}", userResponseDto);

        return new CustomOAuth2User(userResponseDto);
    }

    /**
     * 기존 사용자의 정보를 업데이트
     * 이미 가입된 사용자가 다시 소셜 로그인을 할 경우, 최신 정보로 업데이트
     *
     * @param user 데이터베이스에서 찾은 기존 사용자 엔티티
     * @param oAuth2Response 소셜 로그인 제공자로부터 받은 최신 사용자 정보
     * @return 업데이트된 사용자 정보를 담고 있는 CustomOAuth2User 객체
     */
    private CustomOAuth2User updateExistingUser(User user, OAuth2Response oAuth2Response) {
        user.updateFromDTO(UserResDto.builder()
                .name(oAuth2Response.getName())
                .build());
        userRepository.save(user);

        UserResDto userResponseDto = createUserResponseDto(user.getUsername(), oAuth2Response, user.getSocialType());
        log.debug("Updated existing user: {}", userResponseDto);

        return new CustomOAuth2User(userResponseDto);
    }

    /**
     * 사용자 응답 DTO를 생성
     * 클라이언트에게 전달할 사용자 정보를 일관된 형식으로 생성
     *
     * @param username 사용자 식별자
     * @param oAuth2Response 소셜 로그인 제공자로부터 받은 사용자 정보
     * @param socialType 소셜 로그인 제공자 타입
     * @return 클라이언트에게 전달할 형식의 UserResDto 객체
     */
    private UserResDto createUserResponseDto(String username, OAuth2Response oAuth2Response, SocialType socialType) {
        return UserResDto.builder()
                .username(username)
                .name(oAuth2Response.getName())
                .role(Role.USER)
                .socialType(socialType)
                .build();
    }
}