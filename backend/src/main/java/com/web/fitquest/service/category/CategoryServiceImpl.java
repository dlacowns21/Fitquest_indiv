package com.web.fitquest.service.category;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.web.fitquest.mapper.category.CategoryMapper;
import com.web.fitquest.model.category.Category;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    @Override
    public Optional<List<Category>> getCategoryList(int userId) {
        return Optional.ofNullable(categoryMapper.getCategoryList(userId));
    }

    @Override
    public boolean addCategory(Category category) {
        return categoryMapper.addCategory(category) > 0;
    }

    @Override
    public Optional<Category> getCategoryByUserIdAndCategoryId(int userId, int categoryId) {
        return Optional.ofNullable(categoryMapper.getCategoryByUserIdAndCategoryId(userId, categoryId));
    }

    @Override
    public boolean updateCategory(Category category) {
        return categoryMapper.updateCategory(category) > 0;
    }

    @Override
    public boolean deleteCategory(int categoryId) {
        return categoryMapper.deleteCategory(categoryId) > 0;
    }
}
