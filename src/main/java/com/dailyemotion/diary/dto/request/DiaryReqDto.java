package com.dailyemotion.diary.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiaryReqDto {

    @NotNull
    private String emotion;

    @Size(max = 1000, message = "글자수는 최대 1000자까지 가능합니다.")
    private String content;

    @NotNull
    @Size (min = 1, max = 3, message = "태그는 최소 1개, 최대 3개까지 설정할 수 있습니다.")
    private List<String> tag;

    private String imageUrl;
}
