package com.example.demo.src.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.ArrayList;

@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@Getter
@Setter
public class DiaryImgDto {
    private Integer idx;
    private String imgurl;

    public DiaryImgDto(DiaryImg entity){
        this.idx = entity.getIdx();
        this.imgurl = entity.getImgurl();
    }
}