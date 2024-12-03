package com.web.fitquest.mapper.category;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.web.fitquest.model.category.Category;

@Mapper
public interface CategoryMapper {
    List<Category> getCategoryList(int userId);
    Category getCategoryByUserIdAndCategoryId(@Param("userId") int userId, @Param("categoryId") int categoryId);
    int addCategory(Category category);
    int updateCategory(Category category);
    int deleteCategory(int categoryId);
}
