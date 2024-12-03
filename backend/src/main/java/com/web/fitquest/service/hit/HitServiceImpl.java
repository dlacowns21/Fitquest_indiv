package com.web.fitquest.service.hit;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.web.fitquest.mapper.hit.HitMapper;
import com.web.fitquest.model.hit.Hit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class HitServiceImpl implements HitService {
    
    private final HitMapper hitMapper;
    
    @Override
    @Transactional
    public Optional<Integer> getHitCount(int boardId) {
        try {
            int count = hitMapper.countHitsByBoardId(boardId);
            return Optional.of(count);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    @Override
    @Transactional
    public Optional<Boolean> isHitByUser(int boardId, int userId) {
        try {
            boolean exists = hitMapper.existsByBoardIdAndUserId(boardId, userId);
            return Optional.of(exists);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    @Override
    @Transactional
    public Optional<Integer> addHit(Hit hit) {
        try {
            log.info("Adding hit - boardId: {}, userId: {}", hit.getBoardId(), hit.getUserId());
            hitMapper.insertHit(hit);
            log.info("Successfully added hit");
            
            // 좋아요를 추가한 후 현재 좋아요 수를 반환
            return getHitCount(hit.getBoardId());
        } catch (Exception e) {
            log.error("Error adding hit: ", e);
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public Optional<Integer> removeHit(int boardId, int userId) {
        try {
            log.info("Removing hit - boardId: {}, userId: {}", boardId, userId);
            hitMapper.deleteHit(boardId, userId);
            log.info("Successfully removed hit");
            
            // 좋아요를 제거한 후 현재 좋아요 수를 반환
            return getHitCount(boardId);
        } catch (Exception e) {
            log.error("Error removing hit: ", e);
            return Optional.empty();
        }
    }
}