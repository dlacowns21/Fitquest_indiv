package com.web.fitquest.controller;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.web.fitquest.model.activity.Activity;
import com.web.fitquest.service.activity.ActivityService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/activity")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "사용자 활동 API", description = "사용자 활동 내역 관리 API")
public class ActivityController {
    private final ActivityService activityService;

    @Operation(summary = "연간 활동 데이터 조회", description = "특정 연도의 사용자 활동 데이터(완료된 todo 비율)를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "404", description = "데이터를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/{year}/{userId}")
    public ResponseEntity<Map<String, Double>> getActivities(
            @PathVariable int year,
            @PathVariable int userId) {
        Activity activity = Activity.builder()
                .userId(userId)
                .date(year + "-01-01")
                .build();
        try {
            return ResponseEntity.ok(activityService.getActivityRatios(activity).get());
        } catch (InterruptedException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (ExecutionException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "일일 활동 데이터 갱신", description = "특정 날짜의 사용자 활동 데이터를 갱신합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "갱신 성공", content = @Content(schema = @Schema(type = "number", format = "double", example = "0.75"))),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PutMapping("/daily/{date}/{userId}")
    public ResponseEntity<Double> updateDailyActivity(@PathVariable String date, @PathVariable int userId) {
        double ratio = activityService.updateDailyActivity(userId, date);
        return ResponseEntity.ok(ratio);
    }
}
