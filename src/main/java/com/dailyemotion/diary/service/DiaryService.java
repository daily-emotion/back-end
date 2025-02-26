package com.dailyemotion.diary.service;

import com.dailyemotion.common.exception.DiaryException;
import com.dailyemotion.common.exception.TagException;
import com.dailyemotion.common.exception.UserException;
import com.dailyemotion.diary.dto.request.DiaryReqDto;
import com.dailyemotion.diary.dto.response.DiaryGetResDto;
import com.dailyemotion.diary.dto.response.DiaryResDto;
import com.dailyemotion.diary.repository.DiaryRepository;
import com.dailyemotion.domain.entity.Diary;
import com.dailyemotion.domain.entity.Tag;
import com.dailyemotion.domain.entity.User;
import com.dailyemotion.domain.enums.Emotion;
import com.dailyemotion.tag.repository.TagRepository;
import com.dailyemotion.tag.service.TagService;
import com.dailyemotion.user.oAuth2.CustomOAuth2User;
import com.dailyemotion.user.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.dailyemotion.common.errorCode.DiaryErrorCode.*;
import static com.dailyemotion.common.errorCode.TagErrorCode.INVALID_TAG_NAME;
import static com.dailyemotion.common.errorCode.UserErrorCode.USER_NOT_AUTHORIZED;
import static com.dailyemotion.common.errorCode.UserErrorCode.USER_NOT_FOUND;


@Slf4j
@Service
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;
    private final TagService tagService;
    private final TagRepository tagRepository;
    private final ImageService imageService;
    private final DiaryCacheService diaryCacheService;

    // 다이어리 생성
    public DiaryResDto createDiary(LocalDate date, DiaryReqDto diaryReqDto) {
        String username = getCustomOAuth2User();
        validateDiaryCreation(username, date);

        Optional<User> user = userRepository.findByUsername(username);
        Diary diary = from(diaryReqDto, user.orElseThrow(() -> new UserException(USER_NOT_FOUND)), date);
        diaryRepository.save(diary);

        List<String> tags = tagService.createTag(diary, diaryReqDto);
        return DiaryResDto.from(diary, tags);
    }

    // 다이어리 삭제
    public void deleteDiary(LocalDate date) {
        String username = getCustomOAuth2User();
        Diary diary = findDiaryorThrow(username, date);

        diaryRepository.delete(diary);
    }

    // 다이어리 조회
    public DiaryResDto getDiary(LocalDate date) {
        String username = getCustomOAuth2User();
        Diary diary = findDiaryorThrow(username, date);

        // 다이어리 ID에 해당하는 태그를 조회하고 태그 이름만 리스트로 저장해서 반환
        List<String> resTags = Optional.ofNullable(tagRepository.findTagByDiary_DiaryId(diary.getDiaryId()))
                .orElseThrow(() -> new TagException(INVALID_TAG_NAME))
                .stream()
                .map(Tag::getName)
                .collect(Collectors.toList());
        return DiaryResDto.from(diary, resTags);
    }

    // 다이어리 수정
    @Transactional
    public DiaryResDto updateDiary(LocalDate date, DiaryReqDto diaryReqDto) {
        String username = getCustomOAuth2User();
        Diary diary = findDiaryorThrow(username, date);

        Optional<User> user = userRepository.findByUsername(username);
        Diary updatedDiary = from(diaryReqDto, user.orElseThrow(() -> new UserException(USER_NOT_FOUND)), date);
        diary.updateFrom(updatedDiary);

        // 1. 기존 태그 삭제
        tagRepository.deleteAllByDiary(diary);
        diary.getTags().clear();
        diaryRepository.save(diary); // 태그가 제거된 다이어리를 저장

        // 2. 새로운 태그 추가
        List<String> tags = tagService.createTag(diary, diaryReqDto);

        // 3. 최종적으로 다이어리를 저장
        diaryRepository.save(diary);

        return DiaryResDto.from(diary, tags);
    }

    // 월별 다이어리 조회
    public List<DiaryGetResDto> getMonthlyDiary(String month) {
        invalidMonth(month);
        String username = getCustomOAuth2User();
        String cacheKey = "cache:getMonthlyDiaries:" + username + ":" + month;

        // Redis에서 일치하는 데이터를 찾고 있으면 반환
        // 즉, 캐싱된 데이터가 있다면 이 밑의 로직으로 안내려가고 메소드가 종료된다.
        List<DiaryGetResDto> cachedData = diaryCacheService.getCache(cacheKey, new TypeReference<List<DiaryGetResDto>>() {});
        if (cachedData != null) {
            return cachedData;
        }

        LocalDate targetMonthStart = LocalDate.parse(month + "01", DateTimeFormatter.ofPattern("yyyyMMdd"));
        LocalDate startDate = targetMonthStart.minusMonths(1).withDayOfMonth(1);
        LocalDate endDate = targetMonthStart.plusMonths(1)
                .withDayOfMonth(targetMonthStart.plusMonths(1).lengthOfMonth());

        List<Diary> diaries = diaryRepository.findByUserUsernameAndDateBetween(
                username, startDate, endDate
        );

        if (diaries.isEmpty()) {
            return Collections.emptyList();
        }

        List<DiaryGetResDto> diaryGetResDto = diaries.stream()
                .map(DiaryGetResDto::from)
                .collect(Collectors.toList());

        // 캐싱된 데이터가 없는 경우 캐시에 저장
        diaryCacheService.setCache(cacheKey, diaryGetResDto);

        return diaryGetResDto;
    }

    // OAuth2 인증된 사용자의 username을 가져오는 메소드
    private static String getCustomOAuth2User() {{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null) {
                log.debug("인증 정보가 없습니다");
                throw new UserException(USER_NOT_AUTHORIZED);
            }

            Object principal = authentication.getPrincipal();

            // 안전한 타입 체크 후 처리
            if (principal instanceof CustomOAuth2User customOAuth2User) {
                log.debug("CustomOAuth2User로부터 사용자 정보를 추출합니다: {}",
                        customOAuth2User.getUsername());
                return customOAuth2User.getUsername();
            }

            // JWT 토큰에서 추출한 사용자 정보 처리
            if (principal instanceof Map) {
                log.debug("JWT 토큰으로부터 사용자 정보를 추출합니다");
                @SuppressWarnings("unchecked")
                Map<String, Object> principalMap = (Map<String, Object>) principal;
                return (String) principalMap.get("username");
            }

            log.debug("지원하지 않는 Principal 타입입니다: {}",
                    principal != null ? principal.getClass().getName() : "null");
            throw new UserException(USER_NOT_AUTHORIZED);
        }
    }

    // 다이어리 생성 시 해당 사용자의 다이어리가 이미 존재하는지 확인하는 메소드
    private void validateDiaryCreation(String username, LocalDate date) {
        if (diaryRepository.existsByUserUsernameAndDate(username, date)) {
            throw new DiaryException(DIARY_ALREADY_EXIST);
        }
    }

    //해당 다이어리의 유무 판단 메소드

    private Diary findDiaryorThrow(String username, LocalDate date) {
        Diary diary = diaryRepository.findByUserUsernameAndDate(username, date);

        if (diary == null) {
            throw new DiaryException(DIARY_NOT_FOUND);
        }
        return diary;
    }

    // ReqDto를 Diary 엔티티로 변환하는 메소드
    private static Diary from(DiaryReqDto diaryReqDto, User user, LocalDate date) {
        return Diary.builder()
                .user(user)
                .emotion(Emotion.valueOf(diaryReqDto.getEmotion()))
                .content(diaryReqDto.getContent())
                .imageUrl(diaryReqDto.getImageUrl())
                .date(date)
                .tags(new ArrayList<>())
                .build();
    }

    // 월 형식이 올바른지 확인하는 메소드
    private void invalidMonth(String month) {
        if (month == null || month.length() != 6) {
            throw new DiaryException(INVALID_MONTH_DATE_FORMAT);
        }
    }

    // 이미지 업로드
    public String uploadImageToGcs(MultipartFile file) {
        return imageService.uploadImage(file);
    }
}