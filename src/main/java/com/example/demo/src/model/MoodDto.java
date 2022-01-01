package com.example.demo.src.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@Getter
@Setter
public class MoodDto {
    private Integer idx;
    private String type;

    public MoodDto(Mood entity){
        this.idx = entity.getIdx();
        this.type = entity.getType();
    }

    public Mood toEntity(){
        return Mood.builder()
                .type(type)
                .build();
    }
}