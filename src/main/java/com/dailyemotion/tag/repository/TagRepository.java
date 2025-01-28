package com.dailyemotion.tag.repository;

import com.dailyemotion.domain.entity.Diary;
import com.dailyemotion.domain.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    List<Tag> findTagByDiary_DiaryId(Long diaryId);

    void deleteAllByDiary(Diary diary);

}
