package com.example.demo.src.user.model;

import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ResponseUserDto{
    private String id;
    private String jwt;

    public ResponseUserDto(User entity){
        this.id = entity.getId();
        this.jwt = entity.getJwt();
    }

}