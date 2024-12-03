package com.web.fitquest.responses;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@AllArgsConstructor
@RequiredArgsConstructor
@Schema(description = "TokenResponse DTO")
public class TokenResponse {
    private String accessToken;
    private String refreshToken;
}
