package com.web.fitquest.controller;

import java.util.List;
import java.util.Map;
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

import com.web.fitquest.model.todo.Todo;
import com.web.fitquest.requests.TodoRequest;
import com.web.fitquest.service.todo.TodoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/todo")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "할 일 API", description = "운동 할 일(Todo) 관리 API")
public class TodoController {

    private final TodoService todoService;

    @Operation(summary = "일별 할 일 목록 조회", description = "특정 날짜와 사용자의 모든 할 일 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "데이터 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    // 날짜와 userId에 해당하는 모든 todo를 가져온다.
    @GetMapping("/{date}/{userId}")
    public ResponseEntity<?> getTodoList(@PathVariable String date, @PathVariable int userId) {
        try {
            Todo todo = new Todo(0, userId, 0, 0, "", date, 0);
            Optional<List<Todo>> opTodoList = todoService.getTodoList(todo);
            return opTodoList.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("서버 오류 발생");
        }
    }

    @Operation(summary = "할 일 추가", description = "새로운 할 일을 추가합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "추가 성공"),
            @ApiResponse(responseCode = "404", description = "추가 실패"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    // todo를 추가한다.
    @PostMapping("")
    public ResponseEntity<?> addTodo(@RequestBody Todo todo) {
        try {
            boolean success = todoService.addTodo(todo);
            return success ? ResponseEntity.ok().body("todo 추가 성공") : ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("서버 오류 발생");
        }
    }

    @Operation(summary = "할 일 수정", description = "기존 할 일의 정보를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "404", description = "할 일을 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    // todo를 수정한다.
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTodo(@PathVariable int id, @RequestBody Todo todo) {
        try {
            boolean success = todoService.updateTodo(todo);
            return success ? ResponseEntity.ok().body("todo 수정 성공") : ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("서버 오류 발생");
        }
    }

    @Operation(summary = "연간 통계 조회", description = "특정 연도의 할 일 통계를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "데이터 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    // year 별 통계
    // year와 userId에 해당하는 모든 todo를 가져온다.
    @GetMapping("/statistics/{year}")
    public ResponseEntity<?> getStatistics(@PathVariable int year, @RequestBody TodoRequest todoRequest) {
        try {
            Todo todo = new Todo(0, todoRequest.getUserId(), 0, 0, "", year + "-01-01", 0);
            Optional<List<Todo>> opTodoList = todoService.getTodoListByYearAndUserId(todo);
            return opTodoList.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("서버 오류 발생");
        }
    }

    @Operation(summary = "특정 할 일 조회", description = "ID로 특정 할 일을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "할 일을 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    // id에 해당하는 todo를 가져온다.
    @GetMapping("/{id}")
    public ResponseEntity<?> getTodo(@PathVariable int id) {
        try {
            Optional<Todo> opTodo = todoService.getTodoById(id);
            return opTodo.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("서버 오류 발생");
        }
    }

    @Operation(summary = "할 일 삭제", description = "특정 할 일을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "할 일을 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    // id에 해당하는 todo를 삭제한다.
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTodo(@PathVariable int id) {
        try {
            boolean success = todoService.deleteTodo(id);
            return success ? ResponseEntity.ok().body("todo 삭제 성공") : ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("서버 오류 발생");
        }
    }

    @Operation(summary = "월별 할 일 목록 조회", description = "특정 연도와 월의 할 일 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "데이터 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    // year와 month에 해당하는 모든 todo를 가져온다.
    @GetMapping("/{userId}/{year}/{month}")
    public ResponseEntity<?> getTodoListByYearAndMonth(
            @PathVariable int userId,
            @PathVariable String year,
            @PathVariable String month) {
        try {
            Todo todo = new Todo(0, userId, 0, 0, "", year + "-" + month, 0);
            Optional<List<Todo>> opTodoList = todoService.getTodoListByYearAndMonth(todo);
            return opTodoList.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("서버 오류 발생");
        }
    }

    @Operation(summary = "카테고리별 할 일 목록 조회", description = "특정 카테고리의 모든 할 일 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "데이터 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    // 카테고리 id에 해당하는 모든 todo를 가져온다.
    @GetMapping("/list/{categoryId}/{userId}")
    public ResponseEntity<?> getTodoListByCategoryId(@PathVariable int categoryId, @PathVariable int userId) {
        try {
            Optional<List<Todo>> opTodoList = todoService.getTodoListByCategoryId(categoryId, userId);
            return opTodoList.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("서버 오류 발생");
        }
    }

    @Operation(summary = "완료한 할 일 수 조회", description = "특정 사용자의 완료한 할 일 수를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "데이터 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    // userId에 해당하는 모든 완료한 todo의 수를 가져온다.
    @GetMapping("/count/{userId}")
    public ResponseEntity<?> getDoneTodoCount(@PathVariable int userId) {
        try {
            Optional<Integer> opCount = todoService.getDoneTodoCount(userId);
            return opCount.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("서버 오류 발생");
        }
    }
}
