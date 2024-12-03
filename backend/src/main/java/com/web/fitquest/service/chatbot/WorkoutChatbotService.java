package com.web.fitquest.service.chatbot;

import java.util.List;

import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkoutChatbotService {

    private final OpenAiChatClient chatClient;

    public String getWorkoutRecommendation(List<String> categories, String userPreference) {
        if (categories == null || categories.isEmpty()) {
            categories = List.of("일반 운동");
        }
        
        String prompt = String.format(
            "당신은 전문 피트니스 트레이너입니다. 다음 운동 카테고리들을 기반으로 오늘의 30분 운동 루틴을 추천해주세요:\n" +
            "운동 카테고리: %s\n" +
            "사용자 수준: %s\n\n" +
            "다음 형식으로 응답해주세요:\n" +
            "1. 준비운동 (5분)\n" +
            "2. 본운동 (20분)\n" +
            "- 각 운동별 세트 수와 횟수\n" +
            "3. 마무리 스트레칭 (5분)\n" +
            "4. 예상 칼로리 소모량",
            String.join(", ", categories),
            userPreference
        );
        
        try {
            ChatResponse response = chatClient.call(
                new Prompt(prompt, 
                    OpenAiChatOptions.builder()
                        .withTemperature(0.7f)
                        .withMaxTokens(500)
                        .build()
                )
            );
            
            return response.getResult().getOutput().getContent();
        } catch (Exception e) {
            return "죄송합니다. 운동 추천을 생성하는 중 오류가 발생했습니다: " + e.getMessage();
        }
    }
}
