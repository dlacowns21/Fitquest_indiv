package com.web.fitquest.mapper.hit;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.web.fitquest.model.hit.Hit;

@Mapper
public interface HitMapper {

    int insertHit(Hit hit);

    int deleteHit(@Param("boardId") int boardId, @Param("userId") int userId);

    int countHitsByBoardId(int boardId);

    boolean existsByBoardIdAndUserId(@Param("boardId") int boardId, @Param("userId") int userId);

}