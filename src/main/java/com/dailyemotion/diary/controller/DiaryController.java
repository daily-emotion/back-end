package com.dailyemotion.diary.controller;

import com.dailyemotion.diary.dto.request.DiaryReqDto;
import com.dailyemotion.diary.dto.response.DiaryResDto;
import com.dailyemotion.diary.service.DiaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/diaries")
public class DiaryController {

    private final DiaryService diaryService;

    @PostMapping("/{date}")
    public ResponseEntity<DiaryResDto> createDiary(@PathVariable(name = "date") LocalDate date,
                                                   @RequestBody DiaryReqDto diaryReqDto) {
        DiaryResDto diaryResDto = diaryService.createDiary(date, diaryReqDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(diaryResDto);
    }

    @DeleteMapping("/{date}")
    public ResponseEntity<Void> deleteDiary(@PathVariable(name = "date") LocalDate date) {
        diaryService.deleteDiary(date);
        return ResponseEntity.noContent().build();
    }

}
