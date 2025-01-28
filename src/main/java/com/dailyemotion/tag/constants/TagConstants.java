package com.dailyemotion.tag.constants;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TagConstants {

    private TagConstants() {} // 인스턴스화 방지

    public static final List<String> ALLOWED_TAGS = Collections.unmodifiableList(Arrays.asList(
            "인간관계", "직장", "연인", "가족", "건강",
            "취미", "학업", "돈", "일상", "여행",
            "음식", "운동", "독서", "음악", "영화",
            "게임", "반려동물", "자기계발", "쇼핑", "날씨"
    ));
}
