package com.web.fitquest.model.comment;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
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
@Schema(description = "댓글 DTO")
public class Comment {
    private int id;
    private int boardId;    // 게시글 ID (NOT NULL)
    private int userId;     // 사용자 ID (NOT NULL)
    @NonNull private String writer;
    @NonNull private String content; 
    private LocalDateTime date;    // DB default값 사용
    private Integer parentId;  // 대댓글인 경우만 사용되므로 nullable
    private int isDelete;
}