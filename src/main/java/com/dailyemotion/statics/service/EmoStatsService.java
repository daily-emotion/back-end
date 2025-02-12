package com.dailyemotion.statics.service;



import com.dailyemotion.common.exception.UserException;
import com.dailyemotion.diary.repository.DiaryRepository;
import com.dailyemotion.domain.entity.Diary;
import com.dailyemotion.domain.enums.Emotion;
import com.dailyemotion.statics.dto.response.EmoStatsRes;
import com.dailyemotion.user.oAuth2.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.dailyemotion.common.errorCode.StatsErrorCode.STATISTICS_NOT_FOUND;
import static com.dailyemotion.common.errorCode.UserErrorCode.USER_NOT_AUTHORIZED;

@Service
@RequiredArgsConstructor
public class EmoStatsService {

    private final DiaryRepository diaryRepository;

    // OAuth2 인증된 사용자의 username을 가져오는 메소드
    private static String getCustomOAuth2User() {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            throw new UserException(USER_NOT_AUTHORIZED);
        }

        CustomOAuth2User customOAuth2User = (CustomOAuth2User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return customOAuth2User.getUsername();
    }

    // 월별 감정 통계 조회
    public EmoStatsRes getMonthlyStatistics(int year, int month) {
        String username = getCustomOAuth2User();
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        List<Diary> diaries = diaryRepository.findByUserUsernameAndDateBetween(
                username, startDate, endDate
        );

        // 감정 데이터를 Map으로 변환 (데이터가 없는 경우 빈 Map 생성)
        Map<Emotion, Long> emotionCounts = diaries.stream()
                .collect(Collectors.groupingBy(
                        Diary::getEmotion,
                        Collectors.counting()
                ));

        return EmoStatsRes.builder()
                .yearMonth(String.format("%d-%02d", year, month))
                .emotionCounts(emotionCounts)
                .build();
    }
}