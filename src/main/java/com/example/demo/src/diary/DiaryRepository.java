package com.example.demo.src.diary;

import com.example.demo.src.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

public interface DiaryRepository extends JpaRepository<Diary, Integer> {

    @EntityGraph(attributePaths = {"moods","imgurls"})
    Optional<Diary> findById(Integer idx);

    @Query(value = "SELECT DISTINCT d from mood as m INNER JOIN m.diary d WHERE m.name = :name AND d.user = :user")
    List<Diary> findByNameAndUser(@Param(value = "name") String name, @Param(value = "user") User user);

    @Query(value = "DELETE FROM diary d where d.diaryList = :diaryList")
    void deleteByDiaryList(@Param(value = "diaryList") DiaryList diaryList);
}