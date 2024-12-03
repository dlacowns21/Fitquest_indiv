package com.web.fitquest.mapper.token;

import org.apache.ibatis.annotations.Mapper;

import com.web.fitquest.model.token.RefreshToken;

@Mapper
public interface RefreshTokenMapper {
    void saveRefreshToken(RefreshToken refreshToken);
    RefreshToken findByUserId(int userId);
    void deleteByUserId(int userId);
    void updateRefreshToken(RefreshToken refreshToken);
} 