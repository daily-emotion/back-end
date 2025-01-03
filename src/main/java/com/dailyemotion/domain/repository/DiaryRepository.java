package com.dailyemotion.domain.repository;

import com.dailyemotion.domain.entity.Diary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long> {

    boolean existsByDate(LocalDate date);

    Diary findByDate(LocalDate date);

    @Query("SELECT d FROM Diary d WHERE d.date BETWEEN :startDate AND :endDate")
    List<Diary> findByDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
