package com.dailyemotion.diary.repository;

import com.dailyemotion.domain.entity.Diary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long> {
    // 단일 다이어리 조회
    @Query("SELECT d FROM Diary d WHERE d.user.username = :username AND d.date = :date")
    Diary findByUserUsernameAndDate(
            @Param("username") String username,
            @Param("date") LocalDate date
    );

    // 기간별 다이어리 조회
    @Query("SELECT d FROM Diary d WHERE d.user.username = :username AND d.date BETWEEN :startDate AND :endDate")
    List<Diary> findByUserUsernameAndDateBetween(
            @Param("username") String username,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // 다이어리 존재 여부 체크
    @Query("SELECT EXISTS (SELECT 1 FROM Diary d WHERE d.user.username = :username AND d.date = :date)")
    boolean existsByUserUsernameAndDate(
            @Param("username") String username,
            @Param("date") LocalDate date
    );
}