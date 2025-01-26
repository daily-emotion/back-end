package com.dailyemotion.tag;

import com.dailyemotion.tag.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tags")
@Tag(name = "4. Tag Controller", description = "태그 API")
public class TagController {
    private final TagService tagService;

    @Operation(summary = "사용 가능한 태그 목록 조회")
    @ApiResponse(responseCode = "200", description = "태그 목록 조회 성공")
    @GetMapping
    public ResponseEntity<List<String>> getAvailableTags() {
        return ResponseEntity.ok(tagService.getAvailableTags());
    }
}

