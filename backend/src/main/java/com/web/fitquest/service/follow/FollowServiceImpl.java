package com.web.fitquest.service.follow;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.web.fitquest.mapper.follow.FollowMapper;
import com.web.fitquest.model.follow.Follow;
import com.web.fitquest.model.user.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class FollowServiceImpl implements FollowService {

    private final FollowMapper followMapper;

    @Override
    public Optional<List<User>> getFollowerList(Integer userId) {
        return Optional.ofNullable(followMapper.getFollowerList(userId));
    }

    @Override
    public Optional<List<User>> getFollowingList(Integer userId) {
        return Optional.ofNullable(followMapper.getFollowingList(userId));
    }

    @Override
    public boolean isFollowing(Integer followerId, Integer followingId) {
        return followMapper.isFollowing(followerId, followingId);
    }

    @Override
    public Optional<Integer> followUser(Follow follow) {
        return Optional.ofNullable(followMapper.followUser(follow));
    }

    @Override
    public Optional<Integer> unfollowUser(Follow follow) {
        return Optional.ofNullable(followMapper.unfollowUser(follow));
    }

}
