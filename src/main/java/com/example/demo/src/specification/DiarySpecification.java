package com.example.demo.src.specification;

import org.springframework.data.jpa.domain.Specification;
import com.example.demo.src.model.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;

import java.time.LocalDateTime;

public class DiarySpecification {

    public static Specification<Diary> chkMyDiary(Integer idx) {
        return new Specification<Diary>() {
            @Override
            public Predicate toPredicate(Root<Diary> root,
                                         CriteriaQuery<?> query,
                                         CriteriaBuilder builder){
                Join<Diary,User> m = root.join("user",JoinType.INNER);
                return builder.equal(root.get("idx"),idx);
            }
        };
    }



}