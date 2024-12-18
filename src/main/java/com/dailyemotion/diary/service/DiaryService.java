package com.dailyemotion.diary.service;

import com.dailyemotion.common.exception.DiaryException;
import com.dailyemotion.common.exception.UserException;
import com.dailyemotion.diary.dto.request.DiaryReqDto;
import com.dailyemotion.diary.dto.response.DiaryResDto;
import com.dailyemotion.domain.entity.Diary;
import com.dailyemotion.domain.entity.User;
import com.dailyemotion.domain.enums.Emotion;
import com.dailyemotion.domain.repository.DiaryRepository;
import com.dailyemotion.domain.repository.TagRepository;
import com.dailyemotion.domain.repository.UserRepository;
import com.dailyemotion.tag.service.TagService;
import com.dailyemotion.user.Oauth.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

import static com.dailyemotion.common.errorCode.DiaryErrorCode.DIARY_ALREADY_EXIST;
import static com.dailyemotion.common.errorCode.DiaryErrorCode.INVALID_EMOTION_VALUE;
import static com.dailyemotion.common.errorCode.UserErrorCode.USER_NOT_AUTHORIZED;

@Service
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final TagService tagService;

    public DiaryResDto createDiary(LocalDate date, DiaryReqDto diaryReqDto) {

        validateDiaryCreation(date); // 이미 다이어리가 존재하는지 확인

        // 현재 로그인한 유저
        String username = getCustomOAuth2User();
        User user = userRepository.findByUsername(username);

        Diary diary = from(diaryReqDto, user, date);
        diaryRepository.save(diary);

        List<String> tags = tagService.createTag(diary, diaryReqDto);
        return DiaryResDto.from(diary, tags);
    }


    // OAuth2 커스터마이징 한 클래스에서 username 가져오는 메소드
    private static String getCustomOAuth2User() {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            throw new UserException(USER_NOT_AUTHORIZED);
        }

        CustomOAuth2User customOAuth2User = (CustomOAuth2User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return customOAuth2User.getUsername();
    }

    // 다이어리 생성 시 이미 작성한 다이어리가 존재하는지 확인하는 메소드
    private void validateDiaryCreation(LocalDate date) {
        if (diaryRepository.existsByDate(date)) {
            throw new DiaryException(DIARY_ALREADY_EXIST);
        }
    }

    static Diary from(DiaryReqDto diaryReqDto, User username, LocalDate date) {
        return Diary.builder()
                .user(username)
                .emotion(Emotion.valueOf(diaryReqDto.getEmotion())) // String을 Enum으로
                .content(diaryReqDto.getContent())
                .imageUrl(diaryReqDto.getImageUrl())
                .date(date) // @PathVariable값
                .build();
    }


}
