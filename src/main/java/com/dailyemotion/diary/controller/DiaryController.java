package com.dailyemotion.diary.controller;

import com.dailyemotion.diary.dto.request.DiaryReqDto;
import com.dailyemotion.diary.dto.response.DiaryGetResDto;
import com.dailyemotion.diary.dto.response.DiaryResDto;
import com.dailyemotion.diary.service.DiaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/diaries")
public class DiaryController {

    private final DiaryService diaryService;

    // 다이어리 생성
    @PostMapping("/{date}")
    public ResponseEntity<DiaryResDto> createDiary(@PathVariable(name = "date") LocalDate date,
                                                   @RequestBody DiaryReqDto diaryReqDto) {
        DiaryResDto diaryResDto = diaryService.createDiary(date, diaryReqDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(diaryResDto);
    }

    // 다이어리 삭제
    @DeleteMapping("/{date}")
    public ResponseEntity<Void> deleteDiary(@PathVariable(name = "date") LocalDate date) {
        diaryService.deleteDiary(date);
        return ResponseEntity.noContent().build();
    }

    // 다이어리 조회 (일간)
    @GetMapping("/{date}")
    public ResponseEntity<DiaryResDto> getDiary(@PathVariable(name = "date") LocalDate date) {
        DiaryResDto diaryResDto = diaryService.getDiary(date);
        return ResponseEntity.status(HttpStatus.OK).body(diaryResDto);
    }

    // 다이어리 수정
    @PutMapping("/{date}")
    public ResponseEntity<DiaryResDto> updateDiary(@PathVariable(name = "date") LocalDate date,
                                                   @RequestBody DiaryReqDto diaryReqDto) {
        DiaryResDto diaryResDto = diaryService.updateDiary(date, diaryReqDto);
        return ResponseEntity.status(HttpStatus.OK).body(diaryResDto);
    }

    // 다이어리 조회 (월간)
    @GetMapping("/monthly/{month}")
    public ResponseEntity<List<DiaryGetResDto>> getMonthlyDiary(@PathVariable(name = "month") String month) {
        List<DiaryGetResDto> diaryGetResDto = diaryService.getMonthlyDiary(month);
        return ResponseEntity.status(HttpStatus.OK).body(diaryGetResDto);
    }
}
