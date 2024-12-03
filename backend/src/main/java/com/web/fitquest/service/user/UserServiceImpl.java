package com.web.fitquest.service.user;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.web.fitquest.mapper.user.UserMapper;
import com.web.fitquest.model.searchCondition.SearchCondition;
import com.web.fitquest.model.user.User;
import com.web.fitquest.model.user.UserChoseong;
import com.web.fitquest.requests.LoginRequest;
import com.web.fitquest.service.board.BoardService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final BoardService boardService;

    @Value("${upload.path}")
    private String uploadPath;

    @Override
    public Optional<User> login(LoginRequest loginRequest) {
        User user = userMapper.selectUserByEmail(loginRequest.getEmail());
        if (user != null) {
            log.info("user: {}", user);
            if (user.getPassword().equals(loginRequest.getPassword())) {
                String encodedPassword = passwordEncoder.encode(loginRequest.getPassword());
                user.setPassword(encodedPassword);
                userMapper.updateUser(user);

                // 초성 데이터 확인 및 추가
                checkAndInsertChoseong(user);

                return Optional.of(user);
            } else if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                // 초성 데이터 확인 및 추가
                checkAndInsertChoseong(user);

                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    // 초성 확인 및 추가 메서드
    private void checkAndInsertChoseong(User user) {
        try {
            UserChoseong existingChoseong = userMapper.selectUserChoseong(user.getId());

            if (existingChoseong == null) {
                // 이름을 초성으로 변환
                String nameChoseong = getChoseong(user.getName());
                UserChoseong newChoseong = new UserChoseong(user.getId(), nameChoseong);
                userMapper.insertUserChoseong(newChoseong);
                log.info("Added choseong data for user: {} -> {}", user.getName(), nameChoseong);
            }
        } catch (Exception e) {
            log.error("Error checking/inserting choseong data for user: {}", user.getId(), e);
        }
    }

    @Override
    public boolean regist(User user) {
        String[] parts = user.getName().split(",");
        String name = parts[0];
        String choseong = "";
        if (parts.length > 1) {
            choseong = parts[1];
        }
        user.setName(name);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 1. 먼저 user를 저장
        boolean userInserted = userMapper.insertUser(user) > 0;

        if (userInserted) {
            // 2. user가 저장된 후 초성 저장
            UserChoseong userChoseong = new UserChoseong(user.getId(), choseong);
            userMapper.insertUserChoseong(userChoseong);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean updateUser(User user) {
        try {
            if (user.getName() != null) {
                String[] parts = user.getName().split(",");
                String name = parts[0];
                String choseong = "";
                if (parts.length > 1) {
                    choseong = parts[1];
                }
                user.setName(name);

                UserChoseong userChoseong = new UserChoseong(user.getId(), choseong);
                userMapper.updateUserChoseong(userChoseong);
                boardService.updateWriterChoseongByUserId(user.getId(), choseong);
            }
            return userMapper.updateUser(user) > 0;
        } catch (Exception e) {
            log.error("사용자 정보 수정 실패", e);
            throw e;
        }
    }

    @Override
    public Optional<User> selectUserByName(String name) {
        return Optional.ofNullable(userMapper.selectUserByName(name));
    }

    @Override
    public Optional<List<User>> selectUsersByNameQuery(SearchCondition searchCondition) {
        return Optional.ofNullable(userMapper.selectUsersByNameQuery(searchCondition));
    }

    @Override
    public Optional<User> selectUserByEmail(String email) {
        return Optional.ofNullable(userMapper.selectUserByEmail(email));
    }

    @Override
    public Optional<User> selectUserById(Integer id) {
        return Optional.ofNullable(userMapper.selectUserById(id));
    }

    @Override
    public String updateProfileImage(Integer userId, MultipartFile file) throws IOException {
        // 기존 이미지 삭제 로직 추가
        User user = userMapper.selectUserById(userId);
        if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
            // DB에 저장된 경로에서 파일명만 추출
            String oldFileName = user.getProfileImage().substring(user.getProfileImage().lastIndexOf("/") + 1);
            File oldFile = new File(uploadPath + "/profiles/" + oldFileName);
            if (oldFile.exists()) {
                if (oldFile.delete()) {
                    log.info("이전 프로필 이미지 삭제 성공: {}", oldFile.getAbsolutePath());
                } else {
                    log.warn("이전 프로필 이미지 삭제 실패: {}", oldFile.getAbsolutePath());
                }
            }
        }

        String uniqueFileName = UUID.randomUUID().toString() + getExtension(file.getOriginalFilename());

        // profiles 디렉토리 생성
        File uploadDir = new File(uploadPath + "/profiles");
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // 파일 저장 (uploads/profiles 폴더 아래에)
        String filePath = uploadPath + "/profiles/" + uniqueFileName;
        File savedFile = new File(filePath);

        log.info("파일 저장 경로: {}", filePath); // 로그 추가

        file.transferTo(savedFile);

        // DB에 저장할 경로 (/uploads/profiles/파일명 형식)
        String dbPath = "/uploads/profiles/" + uniqueFileName;
        user.setProfileImage(dbPath);
        userMapper.updateUser(user);

        return dbPath;
    }

    @Override
    public Optional<User> selectRandomUser() {
        return Optional.ofNullable(userMapper.selectRandomUser());
    }

    private String getExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex == -1) {
            return "";
        }
        return filename.substring(dotIndex);
    }

    private String getChoseong(String text) {
        String[] chosung = { "ㄱ", "ㄲ", "ㄴ", "ㄷ", "ㄸ", "ㄹ", "ㅁ", "ㅂ", "ㅃ", "ㅅ", "ㅆ", "ㅇ", "ㅈ", "ㅉ", "ㅊ", "ㅋ", "ㅌ", "ㅍ",
                "ㅎ" };
        StringBuilder result = new StringBuilder();

        for (char ch : text.toCharArray()) {
            if (ch >= '가' && ch <= '힣') {
                int unicode = ch - '가';
                int chosungIndex = unicode / (21 * 28);
                result.append(chosung[chosungIndex]);
            } else {
                // 한글이 아닌 경우 그대로 추가
                result.append(ch);
            }
        }

        return result.toString();
    }

    @Override
    public boolean isAdmin(Integer userId) {
        return userMapper.isAdmin(userId);
    }
}
