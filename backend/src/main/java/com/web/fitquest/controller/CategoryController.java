package com.web.fitquest.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.web.fitquest.model.category.Category;
import com.web.fitquest.service.category.CategoryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "카테고리 API", description = "카테고리 CRUD API")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "카테고리 목록 조회", description = "특정 사용자의 모든 카테고리를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "카테고리 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/{userId}")
    public ResponseEntity<?> getCategoryList(
            @Parameter(description = "사용자 ID", required = true) @PathVariable int userId) {
        try {
            Optional<List<Category>> opCategoryList = categoryService.getCategoryList(userId);
            return opCategoryList.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("서버 오류 발생");
        }
    }

    @Operation(summary = "특정 카테고리 조회", description = "특정 사용자의 특정 카테고리를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "카테고리 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/{userId}/{categoryId}")
    public ResponseEntity<?> getCategory(
            @Parameter(description = "사용자 ID", required = true) @PathVariable int userId,
            @Parameter(description = "카테고리 ID", required = true) @PathVariable int categoryId) {
        try {
            Optional<Category> opCategory = categoryService.getCategoryByUserIdAndCategoryId(userId, categoryId);
            return opCategory.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("서버 오류 발생");
        }
    }

    @Operation(summary = "카테고리 추가", description = "새로운 카테고리를 추가합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "추가 성공"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/{userId}")
    public ResponseEntity<?> addCategory(
            @Parameter(description = "사용자 ID", required = true) @PathVariable int userId,
            @Parameter(description = "카테고리 정보", required = true) @RequestBody Category category) {
        try {
            boolean success = categoryService.addCategory(category);
            return success
                    ? ResponseEntity.ok(category)
                    : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("카테고리 추가 실패");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("서버 오류 발생");
        }
    }

    @Operation(summary = "카테고리 수정", description = "기존 카테고리를 수정합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PutMapping("/{categoryId}")
    public ResponseEntity<?> updateCategory(
            @Parameter(description = "카테고리 ID", required = true) @PathVariable int categoryId,
            @Parameter(description = "수정할 카테고리 정보", required = true) @RequestBody Category category) {
        try {
            boolean success = categoryService.updateCategory(category);
            return success
                    ? ResponseEntity.ok(category)
                    : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("카테고리 수정 실패");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("서버 오류 발생");
        }
    }

    @Operation(summary = "카테고리 삭제", description = "특정 카테고리를 삭제합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "삭제 성공"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<?> deleteCategory(
            @Parameter(description = "카테고리 ID", required = true) @PathVariable int categoryId) {
        try {
            boolean success = categoryService.deleteCategory(categoryId);
            return success 
                ? ResponseEntity.ok("카테고리 삭제 성공")
                : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("카테고리 삭제 실패");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("서버 오류 발생");
        }
    }
}
