package com.web.fitquest.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.web.fitquest.model.hit.Hit;
import com.web.fitquest.service.hit.HitService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("api/hit")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "좋아요 API", description = "게시글 좋아요 관리 API")
public class HitController {

    private final HitService hitService;

    @Operation(summary = "좋아요 토글", description = "게시글의 좋아요를 추가하거나 제거합니다. 이미 좋아요가 있으면 제거하고, 없으면 추가합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토글 성공", content = @Content(schema = @Schema(description = "좋아요 상태 및 개수 정보", example = "{\"action\": \"added\", \"isHit\": true, \"hitCount\": 5}"))),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    // 좋아요 토글
    @PostMapping("/{boardId}/{userId}")
    public ResponseEntity<Map<String, Object>> toggleHit(
            @PathVariable int boardId,
            @PathVariable int userId) {

        Map<String, Object> response = new HashMap<>();

        try {
            // 현재 좋아요 상태 확인
            boolean wasHit = hitService.isHitByUser(boardId, userId)
                    .orElse(false);

            if (wasHit) {
                // 좋아요가 있으면 제거
                hitService.removeHit(boardId, userId);
                response.put("action", "removed");
                response.put("isHit", false);
            } else {
                // 좋아요가 없으면 추가
                Hit hit = new Hit(0, boardId, userId);
                hitService.addHit(hit);
                response.put("action", "added");
                response.put("isHit", true);
            }

            // 현재 좋아요 수 조회
            int currentCount = hitService.getHitCount(boardId)
                    .orElse(0);
            response.put("hitCount", currentCount);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error toggling hit", e);
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @Operation(summary = "좋아요 수 조회", description = "특정 게시글의 전체 좋아요 수를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(type = "integer", example = "5", description = "좋아요 수"))),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    // 좋아요 수 조회
    @GetMapping("/count/{boardId}")
    public ResponseEntity<Integer> getHitCount(@PathVariable int boardId) {
        try {
            int count = hitService.getHitCount(boardId)
                    .orElse(0);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            log.error("Error getting hit count", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "좋아요 상태 확인", description = "특정 사용자가 특정 게시글에 좋아요를 했는지 확인합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(type = "boolean", example = "true", description = "좋아요 여부"))),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    // 좋아요 상태 확인
    @GetMapping("/status/{boardId}/{userId}")
    public ResponseEntity<Boolean> getHitStatus(
            @PathVariable int boardId,
            @PathVariable int userId) {

        try {
            boolean isHit = hitService.isHitByUser(boardId, userId)
                    .orElse(false);
            return ResponseEntity.ok(isHit);
        } catch (Exception e) {
            log.error("Error checking hit status", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}