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
    // 기존 메서드 유지
    boolean existsByDate(LocalDate date);
    Diary findByDate(LocalDate date);

    @Query("SELECT d FROM Diary d WHERE d.date BETWEEN :startDate AND :endDate")
    List<Diary> findByDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // 사용자별 조회를 위한 새로운 메서드 추가
    @Query("SELECT d FROM Diary d WHERE d.user.username = :username AND d.date BETWEEN :startDate AND :endDate")
    List<Diary> findByUserUsernameAndDateBetween(
            @Param("username") String username,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
