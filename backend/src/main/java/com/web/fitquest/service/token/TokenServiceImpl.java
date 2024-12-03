package com.web.fitquest.service.token;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.web.fitquest.exception.InvalidTokenException;
import com.web.fitquest.mapper.token.RefreshTokenMapper;
import com.web.fitquest.model.token.RefreshToken;
import com.web.fitquest.responses.TokenResponse;
import com.web.fitquest.util.JwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TokenServiceImpl implements TokenService {

	private final JwtUtil jwtUtil;
	private final RefreshTokenMapper refreshTokenMapper;

	@Override
	public TokenResponse createTokens(int userId) {
		String accessToken = jwtUtil.createAccessToken(userId);
		String refreshToken = jwtUtil.createRefreshToken(userId);

		log.debug("accessToken: {}", accessToken);
		log.debug("refreshToken: {}", refreshToken);

		saveOrUpdateRefreshToken(userId, refreshToken);
		return new TokenResponse(accessToken, refreshToken);
	}

	private void saveOrUpdateRefreshToken(int userId, String refreshToken) {
		RefreshToken refreshTokenEntity = new RefreshToken();
		refreshTokenEntity.setUserId(userId);
		refreshTokenEntity.setToken(refreshToken);
		refreshTokenEntity.setExpiryDate(LocalDateTime.now().plusDays(14));

		log.debug("refreshTokenEntity: {}", refreshTokenEntity);
		RefreshToken existingToken = refreshTokenMapper.findByUserId(userId);
		log.debug("existingToken: {}", existingToken);
		if (existingToken != null) {
			refreshTokenMapper.updateRefreshToken(refreshTokenEntity);
		} else {
			refreshTokenMapper.saveRefreshToken(refreshTokenEntity);
		}
	}

	@Override
	public TokenResponse refreshAccessToken(String refreshToken) {
		validateRefreshTokenOrThrow(refreshToken);
		int userId = jwtUtil.getUserId(refreshToken);
		String newAccessToken = jwtUtil.createAccessToken(userId);

		return new TokenResponse(newAccessToken, refreshToken);
	}

	private void validateRefreshTokenOrThrow(String refreshToken) {
		if (!jwtUtil.validateToken(refreshToken)) {
			throw new InvalidTokenException("유효하지 않은 리프레시 토큰");
		}

		int userId = jwtUtil.getUserId(refreshToken);
		RefreshToken savedToken = refreshTokenMapper.findByUserId(userId);

		if (savedToken == null || !savedToken.getToken().equals(refreshToken)) {
			throw new InvalidTokenException("저장된 리프레시 토큰과 불일치");
		}

		if (savedToken.getExpiryDate().isBefore(LocalDateTime.now())) {
			refreshTokenMapper.deleteByUserId(userId);
			throw new InvalidTokenException("만료된 리프레시 토큰");
		}
	}

	@Override
	public void revokeRefreshToken(int userId) {
		refreshTokenMapper.deleteByUserId(userId);
	}

	@Override
	public boolean validateRefreshToken(String refreshToken) {
		try {
			validateRefreshTokenOrThrow(refreshToken);
			return true;
		} catch (InvalidTokenException e) {
			return false;
		}
	}
}
