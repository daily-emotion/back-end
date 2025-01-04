package com.dailyemotion.dailyemotion.diary;

import com.dailyemotion.common.errorCode.DiaryErrorCode;
import com.dailyemotion.common.errorCode.UserErrorCode;
import com.dailyemotion.common.exception.DiaryException;
import com.dailyemotion.common.exception.TagException;
import com.dailyemotion.common.exception.UserException;
import com.dailyemotion.diary.dto.request.DiaryReqDto;
import com.dailyemotion.diary.dto.response.DiaryGetResDto;
import com.dailyemotion.diary.dto.response.DiaryResDto;
import com.dailyemotion.diary.service.DiaryService;
import com.dailyemotion.domain.entity.Diary;
import com.dailyemotion.domain.entity.Tag;
import com.dailyemotion.domain.entity.User;
import com.dailyemotion.domain.enums.Emotion;
import com.dailyemotion.domain.repository.DiaryRepository;
import com.dailyemotion.domain.repository.TagRepository;
import com.dailyemotion.domain.repository.UserRepository;
import com.dailyemotion.tag.service.TagService;
import com.dailyemotion.user.Oauth.CustomOAuth2User;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.dailyemotion.common.errorCode.DiaryErrorCode.INVALID_MONTH_DATE_FORMAT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
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

    @Mock
    private TagService tagService;

    @InjectMocks
    private DiaryService diaryService;

    private LocalDate date;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Mock 객체들을 초기화
        date = LocalDate.of(2024, 1, 12);

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


        // given
        DiaryReqDto reqDto = DiaryReqDto.builder()
                .emotion(Emotion.HAPPINESS.name())
                .content("안녕하세요. 오늘 날씨가 진짜 너무 좋아서 기분이 좋아염 뿌우")
                .tag(List.of("기쁨", "날씨", "소풍"))
                .imageUrl("www.이미지.com")
                .build();

        User mockUser = User.builder()
                .username("testUsername")
                .build();

        Diary diary = Diary.builder()
                .diaryId(1L)
                .user(mockUser)
                .emotion(Emotion.HAPPINESS)
                .content("안녕하세요. 오늘 날씨가 진짜 너무 좋아서 기분이 좋아염 뿌우")
                .imageUrl("www.이미지.com")
                .date(date)
                .build();

        Tag mockTag1 = Tag.builder()
                .tagId(1L)
                .diary(diary)
                .name("기쁨")
                .build();
        Tag mockTag2 = Tag.builder()
                .tagId(2L)
                .diary(diary)
                .name("날씨")
                .build();
        Tag mockTag3 = Tag.builder()
                .tagId(3L)
                .diary(diary)
                .name("소풍")
                .build();

        when(userRepository.findByUsername("testUsername")).thenReturn(mockUser);
        when(diaryRepository.save(diary)).thenReturn(diary);

        // when
        DiaryResDto resDto = diaryService.createDiary(date, reqDto);

        // then
        assertThat(reqDto.getEmotion()).isEqualTo(resDto.getEmotion());
        assertThat(reqDto.getContent()).isEqualTo(resDto.getContent());
        assertThat(reqDto.getImageUrl()).isEqualTo(resDto.getImageUrl());
        assertThat(mockTag1.getName()).isEqualTo(reqDto.getTag().get(0));
        assertThat(mockTag2.getName()).isEqualTo(reqDto.getTag().get(1));
        assertThat(mockTag3.getName()).isEqualTo(reqDto.getTag().get(2));
    }

    @Test
    @Order(2)
    @DisplayName("Diary 생성 - 실패 (이미 다이어리가 존재함)")
    void createDiary_fail_already_exist() {

        // given
        when(diaryRepository.existsByDate(date)).thenReturn(true);

        // when & then
        DiaryException exception = assertThrows(DiaryException.class, () -> {
            diaryService.createDiary(date, any());
        });

        assertEquals(DiaryErrorCode.DIARY_ALREADY_EXIST, exception.getErrorCode());
        verify(diaryRepository, times(1)).existsByDate(date);
    }

    @Test
    @Order(3)
    @DisplayName("Diary 생성  - 실패 (사용자 인증 실패)")
    void createDiary_fail_unauthorized() {

        // given
        DiaryReqDto reqDto = DiaryReqDto.builder()
                .emotion(Emotion.HAPPINESS.name())
                .content("안녕하세요 오늘 날씨가 진짜 너무 좋아서 기분이 좋아염 뿌우")
                .tag(List.of("기쁨", "날씨", "소풍"))
                .imageUrl("www.이미지.com")
                .build();

        SecurityContextHolder.clearContext();

        // when & then
        assertThrows(UserException.class, () -> diaryService.createDiary(date, reqDto));
    }

    @Test
    @Order(4)
    @DisplayName("Diary 삭제 - 성공")
    void deleteDiary_success() {

        // given
        User user = User.builder()
                .username("testUsername")
                .build();

        Diary diary = Diary.builder()
                .diaryId(1L)
                .user(user)
                .emotion(Emotion.HAPPINESS)
                .content("안녕하세요. 오늘 날씨가 진짜 너무 좋아서 기분이 좋아염 뿌우")
                .imageUrl("www.이미지.com")
                .date(date)
                .build();

        when(userRepository.findByUsername("testUsername")).thenReturn(user);
        when(diaryRepository.findByDate(date)).thenReturn(diary);

        // when & then
        assertDoesNotThrow(() -> diaryService.deleteDiary(date)); // 한 번만 호출

        // delete 호출 여부 검증
        verify(diaryRepository, times(1)).delete(diary);

        // findByDate가 호출되었는지 검증
        verify(diaryRepository, times(1)).findByDate(date);
    }

    @Test
    @Order(5)
    @DisplayName("Diary 삭제 - 실패 (다이어리가 존재하지 않을 경우 예외 발생)")
    void deleteDiary_fail_diary_not_found() {

        // given
        when(diaryRepository.findByDate(date)).thenReturn(null);

        // when & then
        DiaryException exception = assertThrows(DiaryException.class, () -> diaryService.deleteDiary(date));
        assertThat(exception.getErrorCode()).isEqualTo(DiaryErrorCode.DIARY_NOT_FOUND);
    }

    @Test
    @Order(6)
    @DisplayName("Diary 삭제 - 실패 (해당 다이어리를 작성한 사용자가 아닐 경우 예외 발생)")
    void deleteDiary_fail_is_not_diary_owner() {

        // given
        User diaryOwner = User.builder()
                .username("diaryOwner")
                .build();

        User loggedUser = User.builder()
                .username("loggedUser")
                .build();

        Diary diary = Diary.builder()
                .diaryId(1L)
                .user(diaryOwner)
                .emotion(Emotion.HAPPINESS)
                .content("안녕하세요. 오늘 날씨가 진짜 너무 좋아서 기분이 좋아염 뿌우")
                .imageUrl("www.이미지.com")
                .date(date)
                .build();

        when(userRepository.findByUsername("loggedUser")).thenReturn(loggedUser);
        when(diaryRepository.findByDate(date)).thenReturn(diary);

        UserException exception = assertThrows(UserException.class, () -> diaryService.deleteDiary(date));
        assertThat(exception.getErrorCode()).isEqualTo(UserErrorCode.USER_NOT_MATCHED);
    }

    @Test
    @Order(7)
    @DisplayName("Diary 조회 - 성공")
    void getDiary_success() {

        // given
        User user = User.builder()
                .username("testUsername")
                .build();

        Diary diary = Diary.builder()
                .diaryId(1L)
                .user(user)
                .emotion(Emotion.HAPPINESS)
                .content("안녕하세요. 오늘 날씨가 진짜 너무 좋아서 기분이 좋아염 뿌우")
                .imageUrl("www.이미지.com")
                .date(date)
                .build();

        List<Tag> tags = List.of(
                Tag.builder().name("기쁨").build(),
                Tag.builder().name("날씨").build(),
                Tag.builder().name("소풍").build()
        );

        List<String> expectedTags = tags.stream()
                .map(Tag::getName)
                .toList();

        when(userRepository.findByUsername("testUsername")).thenReturn(user);
        when(diaryRepository.findByDate(date)).thenReturn(diary);
        when(tagRepository.findTagByDiary_DiaryId(diary.getDiaryId())).thenReturn(tags);

        // when
        DiaryResDto resDto = diaryService.getDiary(date);

        // then
        assertThat(diary.getEmotion().name()).isEqualTo(resDto.getEmotion());
        assertThat(diary.getContent()).isEqualTo(resDto.getContent());
        assertThat(diary.getImageUrl()).isEqualTo(resDto.getImageUrl());
        assertThat(resDto.getTag()).isEqualTo(expectedTags); // 태그 검증
    }

    @Test
    @Order(8)
    @DisplayName("Diary 조회 - 실패 (해당 날짜에 다이어리가 존재하지 않는 경우)")
    void getDiary_fail_diary_not_found() {

        // given
        when(diaryRepository.findByDate(date)).thenReturn(null);

        // when & then
        DiaryException exception = assertThrows(DiaryException.class, () -> diaryService.getDiary(date));
        assertThat(exception.getErrorCode()).isEqualTo(DiaryErrorCode.DIARY_NOT_FOUND);

    }

    @Test
    @Order(9)
    @DisplayName("Diary 조회 - 실패 (다이어리에 태그가 존재하지 않는 경우")
    void getDiary_fail_tag_not_found() {

        // given

        User user = User.builder()
                .username("testUsername")
                .build();

        Diary diary = Diary.builder()
                .diaryId(1L)
                .user(user)
                .emotion(Emotion.HAPPINESS)
                .content("안녕하세요. 오늘 날씨가 진짜 너무 좋아서 기분이 좋아염 뿌우")
                .imageUrl("www.이미지.com")
                .date(date)
                .build();

        when(diaryRepository.findByDate(date)).thenReturn(diary);
        when(tagRepository.findTagByDiary_DiaryId(diary.getDiaryId())).thenReturn(null);

        // when & then
        assertThrows(TagException.class, () -> diaryService.getDiary(date));
    }

    @Test
    @Order(10)
    @DisplayName("Diary 수정 - 성공")
    void updateDiary_success() {

        // given
        DiaryReqDto reqDto = DiaryReqDto.builder()
                .emotion(Emotion.SADNESS.name())
                .content("오늘은 날씨가 흐려서 기분이 우울하네요.")
                .tag(List.of("우울", "날씨", "비"))
                .imageUrl("www.업데이트된이미지.com")
                .build();

        User mockUser = User.builder()
                .username("testUsername")
                .build();

        Diary existingDiary = Diary.builder()
                .diaryId(1L)
                .user(mockUser)
                .emotion(Emotion.HAPPINESS)
                .content("안녕하세요. 오늘 날씨가 진짜 너무 좋아서 기분이 좋아염 뿌우")
                .imageUrl("www.이미지.com")
                .date(date)
                .build();

        Tag existingTag1 = Tag.builder()
                .tagId(1L)
                .diary(existingDiary)
                .name("기쁨")
                .build();
        Tag existingTag2 = Tag.builder()
                .tagId(2L)
                .diary(existingDiary)
                .name("날씨")
                .build();
        Tag existingTag3 = Tag.builder()
                .tagId(3L)
                .diary(existingDiary)
                .name("소풍")
                .build();

        Diary updatedDiary = Diary.builder()
                .diaryId(1L)
                .user(mockUser)
                .emotion(Emotion.SADNESS)
                .content("오늘은 날씨가 흐려서 기분이 우울하네요.")
                .imageUrl("www.업데이트된이미지.com")
                .date(date)
                .build();

        Tag newTag1 = Tag.builder()
                .tagId(4L)
                .diary(updatedDiary)
                .name("우울")
                .build();
        Tag newTag2 = Tag.builder()
                .tagId(5L)
                .diary(updatedDiary)
                .name("날씨")
                .build();
        Tag newTag3 = Tag.builder()
                .tagId(6L)
                .diary(updatedDiary)
                .name("비")
                .build();

        when(userRepository.findByUsername("testUsername")).thenReturn(mockUser);
        when(diaryRepository.findByDate(date)).thenReturn(existingDiary);
        when(diaryRepository.save(existingDiary)).thenReturn(updatedDiary);
        when(tagService.createTag(existingDiary, reqDto)).thenReturn(reqDto.getTag());

        // when
        DiaryResDto resDto = diaryService.updateDiary(date, reqDto);

        // then
        assertThat(resDto.getEmotion()).isEqualTo(reqDto.getEmotion());
        assertThat(resDto.getContent()).isEqualTo(reqDto.getContent());
        assertThat(resDto.getImageUrl()).isEqualTo(reqDto.getImageUrl());
        assertThat(resDto.getTag()).containsExactly("우울", "날씨", "비");

    }

    @Test
    @Order(11)
    @DisplayName("Diary 조회(월간) - 성공")
    void getMonthlyDiary_success() {

        // given
        LocalDate date1 = LocalDate.of(2025, 1, 1);
        LocalDate date2 = LocalDate.of(2025, 1, 5);
        LocalDate date3 = LocalDate.of(2025, 1, 31);

        Diary diary1 = Diary.builder().diaryId(1L).date(date1).emotion(Emotion.HAPPINESS).build();
        Diary diary2 = Diary.builder().diaryId(2L).date(date2).emotion(Emotion.SADNESS).build();
        Diary diary3 = Diary.builder().diaryId(3L).date(date3).emotion(Emotion.ANGER).build();

        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 31);
        List<Diary> diaryList = Arrays.asList(diary1, diary2, diary3);

        when(diaryRepository.findByDateBetween(startDate, endDate)).thenReturn(diaryList);

        // when
        List<DiaryGetResDto> result = diaryService.getMonthlyDiary("202501");

        // then
        assertThat(result).hasSize(3); // 리스트 크기 검증

        assertThat(result.get(0).getEmotion()).isEqualTo(Emotion.HAPPINESS.name());
        assertThat(result.get(0).getDate()).isEqualTo(LocalDate.of(2025, 1, 1));

        assertThat(result.get(1).getEmotion()).isEqualTo(Emotion.SADNESS.name());
        assertThat(result.get(1).getDate()).isEqualTo(LocalDate.of(2025, 1, 5));

        assertThat(result.get(2).getEmotion()).isEqualTo(Emotion.ANGER.name());
        assertThat(result.get(2).getDate()).isEqualTo(LocalDate.of(2025, 1, 31));

    }

    @Test
    @Order(12)
    @DisplayName("Diary 조회(월간) - 성공 (해당 월에 데이터가 없는 경우)")
    void getMonthlyDiary_fail_diary_not_found() {

        // given
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 31);

        when(diaryRepository.findByDateBetween(startDate, endDate)).thenReturn(Collections.emptyList());

        // when
        List<DiaryGetResDto> result = diaryService.getMonthlyDiary("202502");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @Order(13)
    @DisplayName("Diary 조회(월간) - 실패 (month가 6자리가 아닌 경우)")
    void getMonthlyDiary_fail_invalid_month_format() {

        // given
        String month = "2025";

        // when & then
        DiaryException exception = assertThrows(DiaryException.class, () -> diaryService.getMonthlyDiary(month));
        assertThat(exception.getErrorCode()).isEqualTo(INVALID_MONTH_DATE_FORMAT);
    }

    @Test
    @Order(14)
    @DisplayName("Diary 조회(월간) - 실패 (month가 null일 때)")
    void getMonthlyDiary_fail_null_month_format() {

        // given
        String month = null;

        // when & then
        DiaryException exception = assertThrows(DiaryException.class, () -> diaryService.getMonthlyDiary(month));
        assertThat(exception.getErrorCode()).isEqualTo(INVALID_MONTH_DATE_FORMAT);
    }
}
