package com.web.fitquest.mapper.todo;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.web.fitquest.model.todo.Todo;

@Mapper
public interface TodoMapper {
    List<Todo> getTodoList(Todo todo);

    int addTodo(Todo todo);

    int updateTodo(Todo todo);

    List<Todo> getTodoListByYearAndUserId(Todo todo);

    Todo getTodoById(int id);

    int deleteTodo(int id);

    List<Todo> getTodoListByYearAndMonth(Todo todo);

    double getDailyCompletionRatio(@Param("userId") int userId, @Param("date") String date);

    List<Todo> getTodoListByCategoryId(@Param("categoryId") int categoryId, @Param("userId") int userId);

    int getDoneTodoCount(int userId);

    void deleteTestTodos();
}
