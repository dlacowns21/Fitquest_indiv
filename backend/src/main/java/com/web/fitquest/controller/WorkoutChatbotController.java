package com.web.fitquest.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.web.fitquest.model.user.User;
import com.web.fitquest.service.chatbot.OpenAiService;
import com.web.fitquest.service.chatbot.WorkoutChatbotService;
import com.web.fitquest.service.user.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class WorkoutChatbotController {
    
    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    private final OpenAiService openAiService;
    private final WorkoutChatbotService workoutChatbotService;
    private final UserService userService;

    @Operation(summary = "AI 응답 생성", description = "OpenAI API를 사용하여 응답을 생성합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "응답 생성 성공"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/ai/generate")
    public Map generate(@RequestParam(value = "message", defaultValue = "안녕하세요") String message) {
        return Map.of("generation", openAiService.generateResponse(message));
    }

    @Operation(summary = "운동 추천 생성", description = "사용자의 카테고리와 목표에 따라 운동 추천을 생성합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "운동 추천 생성 성공"),
        @ApiResponse(responseCode = "404", description = "사용자 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/workout/recommend/{userId}")
    public ResponseEntity<?> getWorkoutRecommendation(@PathVariable int userId) {
        try {
            Optional<User> user = userService.selectUserById(userId);
            if (!user.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found"));
            }
            
            String recommendation = workoutChatbotService.getWorkoutRecommendation(
                user.get().getCategories(),
                "일반"
            );
            
            return ResponseEntity.ok(Map.of("recommendation", recommendation));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "운동 추천 생성 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
} 