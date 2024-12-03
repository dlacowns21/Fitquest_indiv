package com.web.fitquest.requests;

import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "LoginRequest DTO")
public class LoginRequest {
    private String email;
    private String password;
}
