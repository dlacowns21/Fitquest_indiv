package com.web.fitquest.service.hit;

import java.util.Optional;

import com.web.fitquest.model.hit.Hit;

public interface HitService {
    
    // 특정 게시글의 총 좋아요 수 조회
    Optional<Integer> getHitCount(int boardId);
    
    // 특정 사용자가 특정 게시글에 좋아요를 눌렀는지 확인
    Optional<Boolean> isHitByUser(int boardId, int userId);
    
    // 좋아요 추가
    Optional<Integer> addHit(Hit hit);
    
    // 좋아요 취소
    Optional<Integer> removeHit(int boardId, int userId);
}