package com.web.fitquest.requests;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutRecommendationRequest {
    private List<String> categories;
    private String userPreference;  // 예: "초보자", "중급자", "고급자" 또는 특별한 요구사항
}
