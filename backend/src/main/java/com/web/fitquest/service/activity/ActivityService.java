package com.web.fitquest.service.activity;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.web.fitquest.model.activity.Activity;

public interface ActivityService {
    CompletableFuture<Map<String, Double>> getActivityRatios(Activity activity);
    boolean updateActivityRatio(Activity activity);
    double updateDailyActivity(int userId, String date);
}
