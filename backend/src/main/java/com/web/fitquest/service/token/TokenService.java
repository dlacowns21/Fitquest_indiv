package com.web.fitquest.service.token;

import com.web.fitquest.responses.TokenResponse;

public interface TokenService {
    TokenResponse createTokens(int userId);
    TokenResponse refreshAccessToken(String refreshToken);
    void revokeRefreshToken(int userId);
    boolean validateRefreshToken(String refreshToken);
}