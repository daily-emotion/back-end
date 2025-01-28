package com.dailyemotion.tag.service;

import com.dailyemotion.common.errorCode.TagErrorCode;
import com.dailyemotion.common.exception.TagException;
import com.dailyemotion.diary.dto.request.DiaryReqDto;
import com.dailyemotion.domain.entity.Diary;
import com.dailyemotion.domain.entity.Tag;
import com.dailyemotion.tag.constants.TagConstants;
import com.dailyemotion.tag.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static com.dailyemotion.common.errorCode.TagErrorCode.INVALID_TAG_NAME;
import static com.dailyemotion.common.errorCode.TagErrorCode.TAG_COUNT_EXCEEDED;
import static com.dailyemotion.tag.constants.TagConstants.ALLOWED_TAGS;

@Service
@RequiredArgsConstructor
public class TagService {
    private final TagRepository tagRepository;

    /**
     * 다이어리에 태그를 생성하고 저장
     * @param diary 태그를 추가할 다이어리
     * @param diaryReqDto 다이어리 생성 요청 DTO
     * @return 생성된 태그 이름 리스트
     */
    public List<String> createTag(Diary diary, DiaryReqDto diaryReqDto) {
        validateTags(diaryReqDto.getTag());  // 태그 유효성 검증

        // 태그 이름 리스트를 Tag 엔티티 리스트로 변환
        List<Tag> tags = diaryReqDto.getTag().stream()
                .map(tagName -> Tag.builder()
                        .name(tagName)
                        .diary(diary)
                        .build())
                .collect(Collectors.toList());

        tagRepository.saveAll(tags);  // 태그 리스트 저장
        return diaryReqDto.getTag();  // 생성된 태그 이름 리스트 반환
    }

    public List<String> getAvailableTags() {
        return TagConstants.ALLOWED_TAGS;
    }

    /**
     * 태그 유효성 검증
     * - 태그 개수는 3개를 초과할 수 없음
     * - 태그는 허용된 태그 목록에 있어야 함
     * @param tags 검증할 태그 리스트
     * @throws TagException 유효성 검증 실패 시
     */
    private void validateTags(List<String> tags) {
        if (tags.size() > 3) {
            throw new TagException(TAG_COUNT_EXCEEDED);
        }
        if (!new HashSet<>(ALLOWED_TAGS).containsAll(tags)) {
            throw new TagException(INVALID_TAG_NAME);
        }
    }
}