package com.example.demo.src.user;

import com.example.demo.src.user.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Integer> {
    @Query("SELECT u FROM user as u WHERE u.id = :id")
    Optional<User> findById(@Param("id") String id);

}