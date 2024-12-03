package com.web.fitquest.service.todo;

import java.util.List;
import java.util.Optional;

import com.web.fitquest.model.todo.Todo;

public interface TodoService {
    Optional<List<Todo>> getTodoList(Todo todo);

    boolean addTodo(Todo todo);

    boolean updateTodo(Todo todo);

    Optional<List<Todo>> getTodoListByYearAndUserId(Todo todo);

    Optional<Todo> getTodoById(int id);

    boolean deleteTodo(int id);

    Optional<List<Todo>> getTodoListByYearAndMonth(Todo todo);

    Optional<List<Todo>> getTodoListByCategoryId(int categoryId, int userId);

    Optional<Integer> getDoneTodoCount(int userId);
}
