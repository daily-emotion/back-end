package com.dailyemotion.diary.controller;

import com.dailyemotion.diary.dto.request.DiaryReqDto;
import com.dailyemotion.diary.dto.response.DiaryGetResDto;
import com.dailyemotion.diary.dto.response.DiaryResDto;
import com.dailyemotion.diary.service.DiaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/diaries")
@Tag(name = "2. Diary Controller", description = "Diary API")
public class DiaryController {

    private final DiaryService diaryService;

    @Operation(summary = "다이어리 생성", description = "특정 날짜에 새로운 다이어리를 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "다이어리 생성 성공"),
            @ApiResponse(responseCode = "409", description = "이미 해당 날짜에 Diary가 존재합니다."),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 감정 값입니다.")
    })
    @PostMapping("/{date}")
    public ResponseEntity<DiaryResDto> createDiary(
            @Parameter(description = "다이어리 작성 날짜 (YYYY-MM-DD)", example = "2024-01-16", required = true)
            @PathVariable(name = "date") LocalDate date,
            @Parameter(description = "다이어리 생성 정보", required = true)
            @Valid @RequestBody DiaryReqDto diaryReqDto) {
        DiaryResDto diaryResDto = diaryService.createDiary(date, diaryReqDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(diaryResDto);
    }

    @Operation(summary = "다이어리 삭제", description = "특정 날짜의 다이어리를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "다이어리 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "다이어리를 찾을 수 없습니다.")
    })
    @DeleteMapping("/{date}")
    public ResponseEntity<Void> deleteDiary(
            @Parameter(description = "삭제할 다이어리 날짜 (YYYY-MM-DD)", example = "2024-01-16", required = true)
            @PathVariable(name = "date") LocalDate date) {
        diaryService.deleteDiary(date);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "일간 다이어리 조회", description = "특정 날짜의 다이어리를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "다이어리 조회 성공"),
            @ApiResponse(responseCode = "404", description = "다이어리를 찾을 수 없습니다.")
    })
    @GetMapping("/{date}")
    public ResponseEntity<DiaryResDto> getDiary(
            @Parameter(description = "조회할 다이어리 날짜 (YYYY-MM-DD)", example = "2024-01-16", required = true)
            @PathVariable(name = "date") LocalDate date) {
        DiaryResDto diaryResDto = diaryService.getDiary(date);
        return ResponseEntity.status(HttpStatus.OK).body(diaryResDto);
    }

    @Operation(summary = "다이어리 수정", description = "특정 날짜의 다이어리 내용을 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "다이어리 수정 성공"),
            @ApiResponse(responseCode = "404", description = "다이어리를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 감정 값입니다.")
    })
    @PutMapping("/{date}")
    public ResponseEntity<DiaryResDto> updateDiary(
            @Parameter(description = "수정할 다이어리 날짜 (YYYY-MM-DD)", example = "2024-01-16", required = true)
            @PathVariable(name = "date") LocalDate date,
            @Parameter(description = "다이어리 수정 정보", required = true)
            @Valid @RequestBody DiaryReqDto diaryReqDto) {

        DiaryResDto diaryResDto = diaryService.updateDiary(date, diaryReqDto);
        return ResponseEntity.status(HttpStatus.OK).body(diaryResDto);
    }

    @Operation(summary = "월간 다이어리 조회", description = "특정 월의 모든 다이어리를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "월간 다이어리 조회 성공"),
            @ApiResponse(responseCode = "400", description = "Month 형식이 맞지 않습니다. (6자리)")
    })
    @GetMapping("/monthly/{month}")
    public ResponseEntity<List<DiaryGetResDto>> getMonthlyDiary(
            @Parameter(description = "조회할 월 (YYYYMM)", example = "202401", required = true)
            @PathVariable(name = "month") String month) {
        List<DiaryGetResDto> diaryGetResDto = diaryService.getMonthlyDiary(month);
        return ResponseEntity.status(HttpStatus.OK).body(diaryGetResDto);
    }
}