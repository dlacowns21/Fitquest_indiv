package com.web.fitquest.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Schema(description = "Todo DTO")
public class TodoRequest {
    private int userId;
    private int categoryId;
    private String year;
    private String month;
}
