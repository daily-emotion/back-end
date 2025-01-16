package com.dailyemotion.dailyemotion.diary.controller;

import com.dailyemotion.diary.controller.DiaryController;
import com.dailyemotion.diary.dto.request.DiaryReqDto;
import com.dailyemotion.diary.dto.response.DiaryGetResDto;
import com.dailyemotion.diary.dto.response.DiaryResDto;
import com.dailyemotion.diary.service.DiaryService;
import com.dailyemotion.domain.entity.Diary;
import com.dailyemotion.tag.service.TagService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DiaryController.class)
@TestMethodOrder(OrderAnnotation.class)
public class DiaryControllerTest {

    @MockitoBean
    private DiaryService diaryService;

    @MockitoBean
    private TagService tagService;

    @Autowired
    private MockMvc mockMvc;

    // DTO를 JSON 문자열로 변환하는 Jackson 라이브러리
    @Autowired
    private ObjectMapper objectMapper;

    private LocalDate date = LocalDate.of(2025, 1, 12);

    @Test
    @Order(1)
    @DisplayName("Diary 생성 - 성공")
    @WithMockUser(username = "testUsername", roles = "USER")
    void createDiary_success() throws Exception {

        // given
        List<String> tags = List.of("날씨", "맑음", "오늘");

        DiaryReqDto reqDto = DiaryReqDto.builder()
                .emotion("HAPPINESS")
                .content("안녕하세요. 이건 테스트 코드입니다.")
                .tag(tags)
                .imageUrl("www.이미지.com")
                .build();

        DiaryResDto resDto = DiaryResDto.builder()
                .emotion("HAPPINESS")
                .content("안녕하세요. 이건 테스트 코드입니다.")
                .tag(tags)
                .imageUrl("www.이미지.com")
                .build();

        given(diaryService.createDiary(any(LocalDate.class), any(DiaryReqDto.class))).willReturn(resDto);
        given(tagService.createTag(any(Diary.class), any(DiaryReqDto.class))).willReturn(tags);

        // when
        ResultActions result = mockMvc.perform(post("/diaries/" + date) // /api/diaries/2025-01-12
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto)) // DTO를 JSON으로
                .with(csrf())); // 시큐리티에서 일반적으로 CSRF토큰을 요구, 자동으로 유효한 CSRF 토큰을 추가해준 것

        // then
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.emotion").value("HAPPINESS"))
                .andExpect(jsonPath("$.content").value("안녕하세요. 이건 테스트 코드입니다."))
                .andExpect(jsonPath("$.tag[0]").value("날씨"))
                .andExpect(jsonPath("$.tag[1]").value("맑음"))
                .andExpect(jsonPath("$.tag[2]").value("오늘"))
                // .andExpect(jsonPath("$.tag").value(tags))가 안되는 이유는 List와 JSON 배열의 타입이 달라서 테스트 실패
                .andExpect(jsonPath("$.imageUrl").value("www.이미지.com"));
    }

    @Test
    @Order(2)
    @DisplayName("Diary 삭제 - 성공")
    @WithMockUser(username = "testUsername", roles = "USER")
    void deleteDiary_success() throws Exception {

        // given
        doNothing().when(diaryService).deleteDiary(date);

        // when
        ResultActions result = mockMvc.perform(delete("/diaries/" + date)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()));

        // then
        result.andExpect(status().isNoContent());
    }

    @Test
    @Order(3)
    @DisplayName("Diary 조회 - 성공")
    @WithMockUser(username = "testUsername", roles = "USER")
    void getDiary_success() throws Exception {

        // given
        List<String> tags = List.of("날씨", "맑음", "오늘");

        DiaryResDto resDto = DiaryResDto.builder()
                .emotion("HAPPINESS")
                .content("안녕하세요. 이건 테스트 코드입니다.")
                .tag(tags)
                .imageUrl("www.이미지.com")
                .build();

        given(diaryService.getDiary(date)).willReturn(resDto);

        // when
        ResultActions result = mockMvc.perform(get("/diaries/" + date)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resDto)) // 요청 JSON
                .with(csrf()));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.emotion").value("HAPPINESS"))
                .andExpect(jsonPath("$.content").value("안녕하세요. 이건 테스트 코드입니다."))
                .andExpect(jsonPath("$.tag[0]").value("날씨"))
                .andExpect(jsonPath("$.tag[1]").value("맑음"))
                .andExpect(jsonPath("$.tag[2]").value("오늘"))
                // .andExpect(jsonPath("$.tag").value(tags))가 안되는 이유는 List와 JSON 배열의 타입이 달라서 테스트 실패
                .andExpect(jsonPath("$.imageUrl").value("www.이미지.com"));
    }

    @Test
    @Order(4)
    @DisplayName("Diary 수정 - 성공")
    @WithMockUser(username = "testUsername", roles = "USER")
    void updateDiary_success() throws Exception {

        // given
        List<String> updatedTags = List.of("순살", "뼈해", "장국");

        // 요청 DTO
        DiaryReqDto updatedReqDto = DiaryReqDto.builder()
                .emotion("HAPPINESS")
                .content("안녕하세요. 이건 수정된 테스트 코드입니다.")
                .tag(updatedTags)
                .imageUrl("www.수정된 이미지.com")
                .build();

        // 응답 DTO
        DiaryResDto resDto = DiaryResDto.builder()
                .emotion("HAPPINESS")
                .content("안녕하세요. 이건 수정된 테스트 코드입니다.")
                .tag(updatedTags)
                .imageUrl("www.수정된 이미지.com")
                .build();

        given(diaryService.updateDiary(eq(date), eq(updatedReqDto))).willReturn(resDto);

        // when
        ResultActions result = mockMvc.perform(put("/diaries/" + date)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedReqDto)) // 요청 JSON
                .with(csrf()));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.emotion").value("HAPPINESS"))
                .andExpect(jsonPath("$.content").value("안녕하세요. 이건 수정된 테스트 코드입니다."))
                .andExpect(jsonPath("$.tag[0]").value("순살"))
                .andExpect(jsonPath("$.tag[1]").value("뼈해"))
                .andExpect(jsonPath("$.tag[2]").value("장국"))
                .andExpect(jsonPath("$.imageUrl").value("www.수정된 이미지.com"));
    }

    @Test
    @Order(5)
    @DisplayName("Diary 수정 - 성공")
    @WithMockUser(username = "testUsername", roles = "USER")
    void getMonthlyDiary_success() throws Exception {

        // given
        String month = "202501";

        List<DiaryGetResDto> resDto = List.of(
                DiaryGetResDto.builder()
                        .emotion("HAPPINESS")
                        .date(LocalDate.of(2025, 1, 1))
                        .build(),
                DiaryGetResDto.builder()
                        .emotion("SADNESS")
                        .date(LocalDate.of(2025, 1, 15))
                        .build(),
                DiaryGetResDto.builder()
                        .emotion("ANGER")
                        .date(LocalDate.of(2025, 1, 31))
                        .build()
        );

        given(diaryService.getMonthlyDiary(month)).willReturn(resDto);

        // when
        ResultActions result = mockMvc.perform(get("/diaries/monthly/" + month)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resDto))
                .with(csrf()));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$[0].emotion").value("HAPPINESS"))
                .andExpect(jsonPath("$[1].emotion").value("SADNESS"))
                .andExpect(jsonPath("$[2].emotion").value("ANGER"))
                .andExpect(jsonPath("$[0].date").value("2025-01-01"))
                .andExpect(jsonPath("$[1].date").value("2025-01-15"))
                .andExpect(jsonPath("$[2].date").value("2025-01-31"));
    }

}
