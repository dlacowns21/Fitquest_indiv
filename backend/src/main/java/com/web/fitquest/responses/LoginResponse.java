package com.web.fitquest.responses;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "LoginResponse DTO")
public class LoginResponse {
    private String accessToken;
    private Integer id;
    private String email;
    private String name;
    private String message;
} 