package com.web.fitquest.service.follow;

import java.util.List;
import java.util.Optional;

import com.web.fitquest.model.follow.Follow;
import com.web.fitquest.model.user.User;

public interface FollowService {
    Optional<List<User>> getFollowerList(Integer userId);
    Optional<List<User>> getFollowingList(Integer userId);
    boolean isFollowing(Integer followerId, Integer followingId);
    Optional<Integer> followUser(Follow follow);
    Optional<Integer> unfollowUser(Follow follow);
}
