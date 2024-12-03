package com.web.fitquest.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.web.fitquest.model.follow.Follow;
import com.web.fitquest.model.user.User;
import com.web.fitquest.service.follow.FollowService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/follow")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "팔로우 API", description = "팔로우 관리 API")
public class FollowController {
    private final FollowService followService;

    @Operation(summary = "팔로워 목록 조회", description = "사용자의 팔로워 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "팔로워 목록 조회 성공"),
            @ApiResponse(responseCode = "204", description = "팔로워 목록이 없습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    @GetMapping("/followers/{userId}")
    public ResponseEntity<?> getFollowerList(@PathVariable Integer userId) {
        try {
            Optional<List<User>> followerList = followService.getFollowerList(userId);
            if (followerList.isPresent()) {
                return ResponseEntity.ok(followerList.get());
            } else {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("팔로워 목록이 없습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류 발생");
        }
    }

    @Operation(summary = "팔로잉 목록 조회", description = "사용자의 팔로잉 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "팔로잉 목록 조회 성공"),
            @ApiResponse(responseCode = "204", description = "팔로잉 목록이 없습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    @GetMapping("/followings/{userId}")
    public ResponseEntity<?> getFollowingList(@PathVariable Integer userId) {
        try {
            Optional<List<User>> followingList = followService.getFollowingList(userId);
            if (followingList.isPresent()) {
                return ResponseEntity.ok(followingList.get());
            } else {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("팔로잉 목록이 없습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류 발생");
        }
    }

    @Operation(summary = "팔로잉 상태 조회", description = "사용자의 팔로잉 상태를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "팔로잉 상태 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    @GetMapping("/status/{followerId}/{followingId}")
    public ResponseEntity<?> isFollowing(@PathVariable Integer followerId, @PathVariable Integer followingId) {
        try {
            boolean isFollowing = followService.isFollowing(followerId, followingId);
            return ResponseEntity.ok(isFollowing);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류 발생");
        }
    }

    @Operation(summary = "팔로우", description = "사용자를 팔로우합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "팔로우 추가 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    @PostMapping("")
    public ResponseEntity<?> followUser(@RequestBody Follow follow) {
        try {
            Optional<Integer> result = followService.followUser(follow);
            if (result.isPresent()) {
                return ResponseEntity.ok(result.get());
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("팔로우 실패");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류 발생");
        }
    }

    @Operation(summary = "언팔로우", description = "사용자를 언팔로우합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "언팔로우 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    @DeleteMapping("/{followerId}/{followingId}")
    public ResponseEntity<?> unfollowUser(@PathVariable Integer followerId, @PathVariable Integer followingId) {
        try {
            Follow follow = new Follow(followerId, followingId);
            Optional<Integer> result = followService.unfollowUser(follow);
            if (result.isPresent()) {
                return ResponseEntity.ok(result.get());
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("언팔로우 실패");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류 발생");
        }
    }
}