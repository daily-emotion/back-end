package com.dailyemotion.domain.repository;

import com.dailyemotion.domain.entity.Diary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long> {

    boolean existsByDate(LocalDate date);

    Diary findByDate(LocalDate date);
}
