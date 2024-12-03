package com.web.fitquest.mapper.user;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.web.fitquest.model.searchCondition.SearchCondition;
import com.web.fitquest.model.user.User;
import com.web.fitquest.model.user.UserChoseong;

@Mapper
public interface UserMapper {
    User selectUserByEmail(String email);

    User selectUserById(Integer id);

    User selectUserByName(String name);

    int insertUser(User user);

    int insertUserChoseong(UserChoseong userChoseong);

    int updateUser(User user);

    int updateUserChoseong(UserChoseong userChoseong);

    int updateProfileImage(Integer userId, String imageUrl);

    User selectRandomUser();

    List<User> selectUsersByNameQuery(SearchCondition searchCondition);

    UserChoseong selectUserChoseong(Integer userId);

    boolean isAdmin(Integer userId);

    void deleteTestUsers();
}
