package com.web.fitquest.model.todo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString 
@Getter 
@Setter 
@AllArgsConstructor 
@NoArgsConstructor
@RequiredArgsConstructor
@Schema(description = "오늘 할 일 DTO")
@Builder
public class Todo {
    private int id;
    private int userId;
    @NonNull private Integer categoryId;
    @NonNull private Integer isDone;
    @NonNull private String content;
    @NonNull private String date;
    @NonNull private Integer todoOrder;
}