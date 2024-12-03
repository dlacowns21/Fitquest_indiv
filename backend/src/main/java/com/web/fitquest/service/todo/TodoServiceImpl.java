package com.web.fitquest.service.todo;

import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.web.fitquest.mapper.todo.TodoMapper;
import com.web.fitquest.model.activity.Activity;
import com.web.fitquest.model.todo.Todo;
import com.web.fitquest.service.activity.ActivityService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRED)
public class TodoServiceImpl implements TodoService {

    private final Queue<Todo> todoQueue = new ConcurrentLinkedQueue<>();
    private final TodoMapper todoMapper;
    private final ActivityService activityService;

    @Scheduled(fixedRate = 5000) // 5초마다 실행
    @Transactional
    public void processActivityUpdates() {
        Todo todo;
        int retryCount = 0;
        final int MAX_RETRIES = 3;
        
        while ((todo = todoQueue.poll()) != null) {
            Todo currentTodo = todo;
            try {
                double ratio = todoMapper.getDailyCompletionRatio(currentTodo.getUserId(), currentTodo.getDate());
                Activity activity = new Activity(0, currentTodo.getUserId(), currentTodo.getDate(), ratio);
                boolean success = activityService.updateActivityRatio(activity);
                
                if (!success && retryCount < MAX_RETRIES) {
                    todoQueue.offer(currentTodo);
                    retryCount++;
                    log.warn("Activity 업데이트 재시도 {}/{}회: {}", retryCount, MAX_RETRIES, currentTodo);
                }
            } catch (Exception e) {
                log.error("Activity 업데이트 실패: {}", currentTodo, e);
                if (retryCount < MAX_RETRIES) {
                    todoQueue.offer(currentTodo);
                    retryCount++;
                }
            }
        }
    }

    @Async
    private CompletableFuture<Void> updateActivityRatio(int userId, String date) {
        try {
            double ratio = getDailyCompletionRatio(userId, date).get();
            Activity activity = new Activity(0, userId, date, ratio);
            activityService.updateActivityRatio(activity);
            return CompletableFuture.completedFuture(null);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to update activity ratio", e);
        }
    }

    @Async
    public CompletableFuture<Double> getDailyCompletionRatio(int userId, String date) {
        double ratio = todoMapper.getDailyCompletionRatio(userId, date);
        return CompletableFuture.completedFuture(ratio);
    }

    @Override
    public Optional<List<Todo>> getTodoList(Todo todo) {
        return Optional.ofNullable(todoMapper.getTodoList(todo));
    }

    @Override
    public boolean addTodo(Todo todo) {
        boolean success = todoMapper.addTodo(todo) > 0;
        if (success) {
            todoQueue.offer(todo);
        }
        return success;
    }

    @Override
    public boolean updateTodo(Todo todo) {
        boolean success = todoMapper.updateTodo(todo) > 0;
        if (success) {
            todoQueue.offer(todo);
        }
        return success;
    }

    @Override
    public Optional<List<Todo>> getTodoListByYearAndUserId(Todo todo) {
        return Optional.ofNullable(todoMapper.getTodoListByYearAndUserId(todo));
    }

    @Override
    public Optional<Todo> getTodoById(int id) {
        return Optional.ofNullable(todoMapper.getTodoById(id));
    }

    @Override
    public boolean deleteTodo(int id) {
        Todo todo = todoMapper.getTodoById(id);
        boolean success = todoMapper.deleteTodo(id) > 0;
        if (success && todo != null) {
            todoQueue.offer(todo);
        }
        return success;
    }

    @Override
    public Optional<List<Todo>> getTodoListByYearAndMonth(Todo todo) {
        return Optional.ofNullable(todoMapper.getTodoListByYearAndMonth(todo));
    }

    @Override
    public Optional<List<Todo>> getTodoListByCategoryId(int categoryId, int userId) {
        return Optional.ofNullable(todoMapper.getTodoListByCategoryId(categoryId, userId));
    }

    @Override
    public Optional<Integer> getDoneTodoCount(int userId) {
        return Optional.ofNullable(todoMapper.getDoneTodoCount(userId));
    }

}
