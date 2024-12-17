package com.dailyemotion.dailyemotion.diary.service;

import com.dailyemotion.common.errorCode.DiaryErrorCode;
import com.dailyemotion.common.exception.DiaryException;
import com.dailyemotion.common.exception.UserException;
import com.dailyemotion.diary.dto.request.DiaryReqDto;
import com.dailyemotion.diary.dto.response.DiaryResDto;
import com.dailyemotion.diary.service.DiaryService;
import com.dailyemotion.domain.entity.Diary;
import com.dailyemotion.domain.entity.User;
import com.dailyemotion.domain.enums.Emotion;
import com.dailyemotion.domain.repository.DiaryRepository;
import com.dailyemotion.domain.repository.TagRepository;
import com.dailyemotion.domain.repository.UserRepository;
import com.dailyemotion.user.Oauth.CustomOAuth2User;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // Order(n)에 따라 순서 보장
public class DiaryServiceTest {

    @Mock
    private DiaryRepository diaryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private DiaryService diaryService;

    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Mock 객체들을 초기화
        testDate = LocalDate.of(2024, 1, 12);

        // SecurityContext, Authentication을 Mock처리
        SecurityContext securityContext = mock(SecurityContext.class); // 스프링 시큐리티의 인증 전반의 생태계 느낌
        Authentication authentication = mock(Authentication.class); // 유저 개개인의 인증정보

        // CustomOAuth2 객체를 Mock으로 처리, username 값을 넣어줌
        CustomOAuth2User customOAuth2User = mock(CustomOAuth2User.class);
        when(customOAuth2User.getUsername()).thenReturn("testUsername");

        when(authentication.getPrincipal()).thenReturn(customOAuth2User); // 유저의 정보를 요청할 때는 mock 처리해 둔 customOAuth2User 객체를 반환
        when(securityContext.getAuthentication()).thenReturn(authentication); // 현재 인증정보를 가져오는 로직

        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        // SecurityContextHolder 초기화
        SecurityContextHolder.clearContext();
    }

    @Test
    @Order(1)
    @DisplayName("Diary 생성 - 성공")
    void createDiary_success() {

        // GIVEN
        DiaryReqDto reqDto = DiaryReqDto.builder()
                .emotion(Emotion.HAPPINESS.name())
                .content("안녕하세요 오늘 날씨가 진짜 너무 좋아서 기분이 좋아염 뿌우")
                .tag(List.of("기쁨", "날씨", "소풍"))
                .imageUrl("www.이미지.com")
                .build();

        User mockUser = User.builder()
                .username("testUsername")
                .build();

        Diary diary = Diary.builder()
                .diaryId(1L)
                .emotion(Emotion.HAPPINESS)
                .content("안녕하세요 오늘 날씨가 진짜 너무 좋아서 기분이 좋아염 뿌우")
                .imageUrl("www.이미지.com")
                .date(testDate)
                .tags(new ArrayList<>())
                .build();

        when(userRepository.findByUsername("testUsername")).thenReturn(mockUser);
        when(diaryRepository.save(diary)).thenReturn(diary);


        // WHEN
        DiaryResDto resDto = diaryService.createDiary(testDate, reqDto);

        // THEN
        assertThat(reqDto.getEmotion()).isEqualTo(resDto.getEmotion());
        assertThat(reqDto.getContent()).isEqualTo(resDto.getContent());
        assertThat(reqDto.getImageUrl()).isEqualTo(resDto.getImageUrl());
    }

    @Test
    @Order(2)
    @DisplayName("Diary 생성 - 실패 (이미 다이어리가 존재함)")
    void createDiary_fail_already_exist () {

        // GIVEN
        when(diaryRepository.existsByDate(testDate)).thenReturn(true);

        // WHEN & THEN
        DiaryException exception = assertThrows(DiaryException.class, () -> {
            diaryService.createDiary(testDate, any());
        });

        assertEquals(DiaryErrorCode.DIARY_ALREADY_EXIST, exception.getErrorCode());
        verify(diaryRepository, times(1)).existsByDate(testDate);
    }

    @Test
    @Order(3)
    @DisplayName("Diary 생성  - 실패 (사용자 인증 실패)")
    void createDiary_fail_unauthorized () {

        // GIVEN
        DiaryReqDto reqDto = DiaryReqDto.builder()
                .emotion(Emotion.HAPPINESS.name())
                .content("안녕하세요 오늘 날씨가 진짜 너무 좋아서 기분이 좋아염 뿌우")
                .tag(List.of("기쁨", "날씨", "소풍"))
                .imageUrl("www.이미지.com")
                .build();

        SecurityContextHolder.clearContext();

        // WHEN & THEN
        assertThrows(UserException.class, () -> diaryService.createDiary(testDate, reqDto));
    }

    @Test
    @Order(4)
    @DisplayName("Diary 생성 - 실패 (Enum 외의 Emotion값)")
    void createDiary_fail_invalid_emotion () {

        // GIVEN
        DiaryReqDto reqDto = DiaryReqDto.builder()
                .emotion("INVALID_EMOTION")
                .content("안녕하세요 오늘 날씨가 진짜 너무 좋아서 기분이 좋아염 뿌우")
                .tag(List.of("기쁨", "날씨", "소풍"))
                .imageUrl("www.이미지.com")
                .build();

        // WHEN & THEN
        assertThrows(DiaryException.class, () -> diaryService.createDiary(testDate, reqDto));
    }

}
