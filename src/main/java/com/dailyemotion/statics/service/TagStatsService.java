package com.dailyemotion.statics.service;

import com.dailyemotion.common.exception.UserException;
import com.dailyemotion.diary.repository.DiaryRepository;
import com.dailyemotion.domain.entity.Diary;
import com.dailyemotion.domain.entity.Tag;
import com.dailyemotion.statics.dto.response.TagStatsRes;
import com.dailyemotion.user.oAuth2.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.dailyemotion.common.errorCode.UserErrorCode.USER_NOT_AUTHORIZED;

@Service
@RequiredArgsConstructor
public class TagStatsService {
    private final DiaryRepository diaryRepository;

    // OAuth2 인증된 사용자의 username을 가져오는 메소드
    private static String getCustomOAuth2User() {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            throw new UserException(USER_NOT_AUTHORIZED);
        }

        CustomOAuth2User customOAuth2User = (CustomOAuth2User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return customOAuth2User.getUsername();
    }

    // 월별 태그 통계 조회
    public TagStatsRes getMonthlyStatistics(int year, int month) {
        String username = getCustomOAuth2User();
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        List<Diary> diaries = diaryRepository.findByUserUsernameAndDateBetween(
                username, startDate, endDate
        );

        // 태그 데이터를 Map으로 변환 (데이터가 없는 경우 빈 Map 생성)
        Map<String, Long> tagCounts = diaries.stream()
                .flatMap(diary -> diary.getTags().stream())
                .collect(Collectors.groupingBy(
                        Tag::getName,
                        Collectors.counting()
                ));

        return TagStatsRes.builder()
                .yearMonth(String.format("%d-%02d", year, month))
                .tagCounts(tagCounts)
                .build();
    }
}