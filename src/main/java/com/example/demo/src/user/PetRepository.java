package com.example.demo.src.user;

import com.example.demo.src.user.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

public interface PetRepository extends JpaRepository<Pet, Integer> {

}