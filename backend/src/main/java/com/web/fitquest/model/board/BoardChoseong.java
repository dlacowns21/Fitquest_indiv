package com.web.fitquest.model.board;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "게시글 제목 초성 DTO")
public class BoardChoseong {
    private Integer boardId;
    private String titleChoseong;
    private String contentChoseong;
    private String writerChoseong;
}