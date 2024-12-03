package com.web.fitquest.model.hit;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString 
@Getter 
@Setter 
@AllArgsConstructor
@Schema(description = "좋아요 DTO")
public class Hit {
    private int id;
    private int boardId;
    private int userId;
}
