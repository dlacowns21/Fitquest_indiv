package com.web.fitquest.model.activity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString 
@Getter 
@Setter 
@AllArgsConstructor
@Builder
@Schema(description = "사용자 활동내역 DTO")
public class Activity {
    private int id;
    private int userId;
    private String date;
    private double ratio;

}
