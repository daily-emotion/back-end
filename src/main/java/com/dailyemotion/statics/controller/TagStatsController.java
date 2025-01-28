package com.dailyemotion.statics.controller;

import com.dailyemotion.statics.dto.response.TagStatsRes;
import com.dailyemotion.statics.service.TagStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reports/tags")
@Tag(name = "3. Statistics Controller", description = "태그 통계 API")
public class TagStatsController {
    private final TagStatsService tagStatsService;

    @Operation(summary = "월별 태그 통계 조회",
            description = "특정 연도와 월의 태그 통계를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "통계 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 년도/월 입력"),
            @ApiResponse(responseCode = "404", description = "해당 월의 통계 데이터가 없음")
    })
    @GetMapping("/{year}/{month}")
    public ResponseEntity<TagStatsRes> getMonthlyStatistics(
            @Parameter(description = "조회할 연도", example = "2024") @PathVariable int year,
            @Parameter(description = "조회할 월(1-12)", example = "1") @PathVariable int month) {
        return ResponseEntity.ok(tagStatsService.getMonthlyStatistics(year, month));
    }
}