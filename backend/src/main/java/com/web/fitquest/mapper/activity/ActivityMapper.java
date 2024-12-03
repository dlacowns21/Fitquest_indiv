package com.web.fitquest.mapper.activity;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.web.fitquest.model.activity.Activity;

@Mapper
public interface ActivityMapper {
    void initializeUserActivities(int userId);

    List<Activity> getActivityRatios(Activity activity);

    int insertOrUpdateActivity(Activity activity);
}
