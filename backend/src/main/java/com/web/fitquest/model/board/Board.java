package com.web.fitquest.model.board;

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
@Schema(description = "게시글 DTO")
public class Board {
    private int id;
    private int userId;      
    @NonNull private String tag;
    private LocalDateTime date;
    private String writer;  
    @NonNull private String title;
    @NonNull private String content;
    private int viewCount;
    private int commentCount; 
    private int hitCount;
    private String postImage;
    private BoardChoseong choseong;
}
