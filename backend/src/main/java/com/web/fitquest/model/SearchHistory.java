package com.web.fitquest.model;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
@Builder
@Schema(description = "사용자 검색기록 DTO")
public class SearchHistory {
    private int id;
    private int userId;
    private String content;
    private LocalDateTime createdAt;
}