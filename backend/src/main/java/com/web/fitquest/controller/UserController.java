package com.web.fitquest.controller;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.web.fitquest.exception.InvalidTokenException;
import com.web.fitquest.model.searchCondition.SearchCondition;
import com.web.fitquest.model.user.User;
import com.web.fitquest.requests.LoginRequest;
import com.web.fitquest.requests.RefreshTokenRequest;
import com.web.fitquest.responses.LoginResponse;
import com.web.fitquest.responses.TokenResponse;
import com.web.fitquest.responses.UserInfoResponse;
import com.web.fitquest.service.token.TokenService;
import com.web.fitquest.service.user.UserService;
import com.web.fitquest.util.JwtUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "사용자 API", description = "사용자 인증 및 프로필 관리 API")
public class UserController {

    private final UserService userService;
    private final TokenService tokenService;
    private final JwtUtil jwtUtil;

    @Value("${upload.path}")
    private String uploadPath;

    @Operation(summary = "랜덤 사용자 조회", description = "랜덤한 사용자를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/random")
    public ResponseEntity<?> getRandomUser() {
        try {
            Optional<User> opUser = userService.selectRandomUser();
            log.info("randomUser: {}", opUser);
            return opUser
                    .map(user -> ResponseEntity.status(HttpStatus.OK).body(user))
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류 발생");
        }
    }

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "가입 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/regist")
    public ResponseEntity<?> regist(@RequestBody User user) {
        try {
            boolean success = userService.regist(user);
            if (success) {
                return ResponseEntity.status(HttpStatus.CREATED).body("회원가입이 완료되었습니다.");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("회원가입에 실패하였습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    @Operation(summary = "로그인", description = "사용자 인증 후 토큰을 발급합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        log.info("loginRequest: {}", loginRequest);
        try {
            Optional<User> opUser = userService.login(loginRequest);
            return opUser
                    .map(user -> {
                        TokenResponse tokens = tokenService.createTokens(user.getId());

                        ResponseCookie refreshTokenCookie = ResponseCookie
                                .from("refreshToken", tokens.getRefreshToken())
                                .httpOnly(true) // 자바스크립트 접근 불가
                                .secure(false) // https 프로토콜에서만 전송(개발 단계에서는 주석 처리)
                                .path("/") // 모든 경로에서 접근 가능
                                .maxAge(60 * 60 * 24 * 14) // 2주
                                .sameSite("Lax") // CSRF 방지
                                .domain("") // 도메인 설정
                                .build();

                        return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                                .body(new LoginResponse(
                                        tokens.getAccessToken(),
                                        user.getId(),
                                        user.getEmail(),
                                        user.getName(),
                                        "로그인 성공!"));
                    })
                    .orElse(ResponseEntity
                            .status(HttpStatus.UNAUTHORIZED)
                            .body(new LoginResponse(null, null, null, null, "로그인 실패")));
        } catch (Exception e) {
            log.error("Login error: ", e); // 상세 로그 추가
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new LoginResponse(null, null, null, null, "서버 오류"));
        }
    }

    @Operation(summary = "토큰 갱신", description = "만료된 액세스 토큰을 리프레시 토큰으로 갱신합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 리프레시 토큰"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        try {
            if (request.getRefreshToken() == null || request.getRefreshToken().isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("리프레시 토큰이 필요합니다.");
            }

            TokenResponse tokens = tokenService.refreshAccessToken(request.getRefreshToken());
            return ResponseEntity.ok(tokens);
        } catch (InvalidTokenException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("토큰 갱신 중 오류가 발생했습니다.");
        }
    }

    @Operation(summary = "로그아웃", description = "사용자의 리프레시 토큰을 무효화합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String bearerToken) {
        try {
            String token = bearerToken.substring(7); // "Bearer " 제거
            int userId = jwtUtil.getUserId(token);
            tokenService.revokeRefreshToken(userId);
            return ResponseEntity.ok().body("로그아웃 성공");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("로그아웃 실패");
        }
    }

    @Operation(summary = "닉네임 중복 확인", description = "닉네임 사용 가능 여부를 확인합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "사용 가능한 닉네임"),
            @ApiResponse(responseCode = "409", description = "중복된 닉네임"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/check-name/{name}")
    public ResponseEntity<?> checkNameDuplicated(@PathVariable String name) {
        try {
            Optional<User> opUser = userService.selectUserByName(name);
            return opUser
                    .map(user -> {
                        return ResponseEntity.status(HttpStatus.CONFLICT).body("사용할 수 없는 닉네임입니다.");
                    })
                    .orElse(ResponseEntity.status(HttpStatus.OK).body("사용 가능한 닉네임입니다."));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류 발생");
        }
    }

    @Operation(summary = "이메일 중복 확인", description = "이메일 사용 가능 여부를 확인합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "사용 가능한 이메일"),
            @ApiResponse(responseCode = "409", description = "중복된 이메일"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/check-email/{email}")
    public ResponseEntity<?> checkEmailDuplicated(@PathVariable String email) {
        try {
            Optional<User> opUser = userService.selectUserByEmail(email);
            return opUser
                    .map(user -> {
                        return ResponseEntity.status(HttpStatus.CONFLICT).body("사용할 수 없는 이메일입니다.");
                    })
                    .orElse(ResponseEntity.status(HttpStatus.OK).body("사용 가능한 이메일입니다."));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류 발생");
        }
    }

    @Operation(summary = "사용자 정보 조회", description = "특정 사용자의 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserInfo(@PathVariable Integer userId) {
        try {
            Optional<User> opUser = userService.selectUserById(userId);
            if (opUser.isPresent()) {
                User user = opUser.get();
                return ResponseEntity.ok()
                        .body(new UserInfoResponse(
                                user.getId(),
                                user.getEmail(),
                                user.getName(),
                                user.getProfileImage(),
                                user.getIsAdmin(),
                                user.getDescription()));
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "사용자 정보 수정", description = "사용자의 프로필 정보를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUserInfo(@PathVariable Integer userId, @RequestBody User user) {
        try {
            boolean success = userService.updateUser(user);
            if (success) {
                return new ResponseEntity<>(user, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "리프레시 토큰 확인", description = "리프레시 토큰의 존재 여부를 확인합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "확인 성공")
    })
    @GetMapping("/check-refresh-token")
    public ResponseEntity<?> checkRefreshToken(
            @CookieValue(name = "refreshToken", required = false) String refreshToken) {
        return ResponseEntity.ok()
                .body(Map.of("exists", refreshToken != null));
    }

    @Operation(summary = "프로필 이미지 업로드", description = "사용자의 프로필 이미지를 업로드합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "업로드 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/{userId}/profile-image")
    public ResponseEntity<?> updateProfileImage(@PathVariable Integer userId,
            @RequestParam("image") MultipartFile image) {
        try {
            String imageUrl = userService.updateProfileImage(userId, image);
            log.info("imageUrl: {}", imageUrl);
            return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류 발생");
        }
    }

    @Operation(summary = "프로필 이미지 조회", description = "업로드된 프로필 이미지를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "이미지를 찾을 수 없음")
    })
    @GetMapping("/uploads/profiles/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            File file = new File(uploadPath + "/profiles/" + filename);
            Resource resource = new UrlResource(file.toURI());
            if (resource.exists() || resource.isReadable()) {
                String contentType = Files.probeContentType(file.toPath());
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                log.error("파일을 찾을 수 없음: {}", file.getAbsolutePath());
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("파일 서비스 에러: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "사용자 검색", description = "사용자 이름으로 검색합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "204", description = "검색 결과가 없음")
    })
    @PostMapping("/search")
    public ResponseEntity<?> searchUsers(@RequestBody SearchCondition searchCondition) {
        log.info("searchCondition: {}", searchCondition.toString());
        try {
            Optional<List<User>> result = userService.selectUsersByNameQuery(searchCondition);
            if (result.isPresent() && !result.get().isEmpty()) {
                return ResponseEntity.ok(result.get());
            } else {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(List.of());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류 발생");
        }
    }

    @Operation(summary = "관리자 권한 확인", description = "사용자의 관리자 권한 여부를 확인합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "확인 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/admin/{userId}")
    public ResponseEntity<?> isAdmin(@PathVariable Integer userId) {
        try {
            boolean isAdmin = userService.isAdmin(userId);
            return new ResponseEntity<>(Map.of("isAdmin", isAdmin), HttpStatus.OK);
        } catch (Exception e) {
            log.error("관리자 권한 확인 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류 발생");
        }
    }
}
