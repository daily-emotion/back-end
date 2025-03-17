package com.dailyemotion.diary.service;

import com.dailyemotion.common.exception.DiaryException;
import com.dailyemotion.common.exception.TagException;
import com.dailyemotion.common.exception.UserException;
import com.dailyemotion.common.utill.SecurityUtilsUsername;
import com.dailyemotion.diary.dto.request.DiaryReqDto;
import com.dailyemotion.diary.dto.response.DiaryGetResDto;
import com.dailyemotion.diary.dto.response.DiaryResDto;
import com.dailyemotion.diary.repository.DiaryRepository;
import com.dailyemotion.domain.entity.Diary;
import com.dailyemotion.domain.entity.Tag;
import com.dailyemotion.domain.entity.User;
import com.dailyemotion.tag.repository.TagRepository;
import com.dailyemotion.tag.service.TagService;
import com.dailyemotion.user.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.dailyemotion.common.errorCode.DiaryErrorCode.*;
import static com.dailyemotion.common.errorCode.TagErrorCode.INVALID_TAG_NAME;
import static com.dailyemotion.common.errorCode.UserErrorCode.USER_NOT_FOUND;
import static com.dailyemotion.domain.entity.Diary.fromReqDto;


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
        String username = SecurityUtilsUsername.getCustomOAuth2UserName();
        validateDiaryCreation(username, date);

        User user = findUserOrThrow(username);
        Diary diary = fromReqDto(diaryReqDto, user, date);
        diaryRepository.save(diary);

        List<String> tags = tagService.createTag(diary, diaryReqDto);
        return DiaryResDto.fromDiary(diary, tags);
    }

    // 다이어리 삭제
    public void deleteDiary(LocalDate date) {
        String username = SecurityUtilsUsername.getCustomOAuth2UserName();
        Diary diary = findDiaryOrThrow(username, date);

        diaryRepository.delete(diary);
    }

    // 다이어리 조회
    public DiaryResDto getDiary(LocalDate date) {
        String username = SecurityUtilsUsername.getCustomOAuth2UserName();
        Diary diary = findDiaryOrThrow(username, date);

        // 다이어리 ID에 해당하는 태그를 조회하고 태그 이름만 리스트로 저장해서 반환
        List<String> resTags = Optional.ofNullable(tagRepository.findTagByDiary_DiaryId(diary.getDiaryId()))
                .orElseThrow(() -> new TagException(INVALID_TAG_NAME))
                .stream()
                .map(Tag::getName)
                .collect(Collectors.toList());
        return DiaryResDto.fromDiary(diary, resTags);
    }

    // 다이어리 수정
    @Transactional
    public DiaryResDto updateDiary(LocalDate date, DiaryReqDto diaryReqDto) {
        String username = SecurityUtilsUsername.getCustomOAuth2UserName();
        Diary diary = findDiaryOrThrow(username, date);

        User user = findUserOrThrow(username);
        Diary updatedDiary = fromReqDto(diaryReqDto, user, date);
        diary.updateFrom(updatedDiary);

        // 1. 기존 태그 삭제
        tagRepository.deleteAllByDiary(diary);
        diary.getTags().clear();

        // 2. 새로운 태그 추가
        List<String> tags = tagService.createTag(diary, diaryReqDto);

        // 3. 최종적으로 다이어리를 저장
        diaryRepository.save(diary);

        return DiaryResDto.fromDiary(diary, tags);
    }

    // 월별 다이어리 조회
    public List<DiaryGetResDto> getMonthlyDiary(String month) {
        validateDiaryCreation(month);
        String username = SecurityUtilsUsername.getCustomOAuth2UserName();
        String cacheKey = "cache:getMonthlyDiaries:" + username + ":" + month;

        // 캐시가 있으면 반환
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

        // 캐시에 저장
        diaryCacheService.setCache(cacheKey, diaryGetResDto);

        return diaryGetResDto;
    }



    // 다이어리 생성 시 해당 사용자의 다이어리가 이미 존재하는지 확인하는 메소드
    private void validateDiaryCreation(String username, LocalDate date) {
        if (diaryRepository.existsByUserUsernameAndDate(username, date)) {
            throw new DiaryException(DIARY_ALREADY_EXIST);
        }
    }

    //해당 다이어리의 유무 판단 메소드
    private Diary findDiaryOrThrow(String username, LocalDate date) {
        Diary diary = diaryRepository.findByUserUsernameAndDate(username, date);

        if (diary == null) {
            throw new DiaryException(DIARY_NOT_FOUND);
        }
        return diary;
    }

    // 사용자 찾기 메소드
    private User findUserOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserException(USER_NOT_FOUND));
    }



    // 월 형식이 올바른지 확인하는 메소드
    private void validateDiaryCreation(String month) {
        if (month == null || month.length() != 6) {
            throw new DiaryException(INVALID_MONTH_DATE_FORMAT);
        }
    }

    // 이미지 업로드
    public String uploadImageToGcs(MultipartFile file) {
        return imageService.uploadImage(file);
    }
}