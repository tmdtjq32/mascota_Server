package com.example.demo.src.user;

import com.example.demo.src.user.model.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Integer> {

}