package com.web.fitquest.mapper.follow;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.web.fitquest.model.follow.Follow;
import com.web.fitquest.model.user.User;

@Mapper
public interface FollowMapper {
    List<User> getFollowerList(Integer userId);

    List<User> getFollowingList(Integer userId);

    boolean isFollowing(Integer followerId, Integer followingId);

    Integer followUser(Follow follow);

    Integer unfollowUser(Follow follow);
}
