package com.web.fitquest.service.user;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.web.multipart.MultipartFile;

import com.web.fitquest.model.searchCondition.SearchCondition;
import com.web.fitquest.model.user.User;
import com.web.fitquest.requests.LoginRequest;

public interface UserService {
    Optional<User> login(LoginRequest loginRequest);

    boolean regist(User user);

    boolean updateUser(User user);

    Optional<User> selectUserByName(String name);

    Optional<List<User>> selectUsersByNameQuery(SearchCondition searchCondition);

    Optional<User> selectUserByEmail(String email);

    Optional<User> selectUserById(Integer id);

    String updateProfileImage(Integer userId, MultipartFile image) throws IOException;

    Optional<User> selectRandomUser();

    boolean isAdmin(Integer userId);
}
