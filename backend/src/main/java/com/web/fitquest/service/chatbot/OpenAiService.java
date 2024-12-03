package com.web.fitquest.service.chatbot;

import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OpenAiService {
    
    private final OpenAiChatClient chatClient;

    public String generateResponse(String message) {
        try {
            ChatResponse response = chatClient.call(new Prompt(message));
            return response.getResult().getOutput().getContent();
        } catch (Exception e) {
            return "죄송합니다. 응답을 생성하는 중 오류가 발생했습니다: " + e.getMessage();
        }
    }
} 