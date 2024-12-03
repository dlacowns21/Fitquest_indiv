package com.web.fitquest.performance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import com.web.fitquest.mapper.category.CategoryMapper;
import com.web.fitquest.model.todo.Todo;
import com.web.fitquest.model.user.User;
import com.web.fitquest.service.todo.TodoService;
import com.web.fitquest.service.user.UserService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Sql(scripts = {
    "classpath:cleanup.sql",
    "classpath:schema-test.sql",
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class TodoServiceConcurrencyTest {

    @Autowired
    private TodoService todoService;
    
    @Autowired
    private UserService userService;

    @Autowired
    private CategoryMapper categoryMapper;

    private static final int USER_COUNT = 1250;
    private List<User> testUsers;
    private List<Todo> testTodos;

    @BeforeEach
    public void setUp() {
        testUsers = new ArrayList<>();
        testTodos = new ArrayList<>();
        
        for (int i = 1; i <= USER_COUNT; i++) {
            int idx=i;
            try {             
                User user = User.builder()
                        .email("test" + i + "@test.com")
                        .password("password")
                        .name("테스트유저" + i)
                        .isAdmin(0)
                        .profileImage("/uploads/profiles/default_profile.png")
                        .build();

                boolean result = userService.regist(user);                
                if (!result) {
                    fail("유저 등록 실패 - index: " + idx);
                    continue;
                }

                User registeredUser = userService.selectUserByEmail(user.getEmail())
                    .orElseThrow(() -> new AssertionError("등록된 유저를 찾을 수 없습니다. - index: " + idx));                
                testUsers.add(registeredUser);
                
                int categoryId = categoryMapper.getCategoryList(registeredUser.getId()).get(0).getId();
                Todo todo = Todo.builder()
                    .id(idx)
                    .userId(registeredUser.getId())
                    .categoryId(categoryId)
                    .content("Test Todo " + idx)
                    .date(LocalDate.now().toString())
                    .isDone(0)
                    .todoOrder(0)
                    .build();
                
                boolean todoResult = todoService.addTodo(todo);                
                if (!todoResult) {
                    fail("Todo 생성 실패 - index: " + idx);
                    continue;
                }
                testTodos.add(todo);
            } catch (Exception e) {
                fail("테스트 데이터 생성 실패 - index: " + idx + ", error: " + e.getMessage());
            }
        }
        
        log.info("생성된 유저 수: {}", testUsers.size());
        log.info("생성된 Todo 수: {}", testTodos.size());
        
        assertEquals(USER_COUNT, testUsers.size(), "생성된 유저 수가 일치하지 않습니다.");
        assertEquals(USER_COUNT, testTodos.size(), "생성된 Todo 수가 일치하지 않습니다.");
    }

    @Test
    public void testConcurrentTodoUpdates() throws InterruptedException {
        int threadCount = USER_COUNT;
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<CompletableFuture<Long>> futures = new ArrayList<>();
        
        // 각 유저별로 Todo 업데이트 동시 실행
        for (int i = 0; i < threadCount; i++) {
            int idx=i;
            CompletableFuture<Long> future = CompletableFuture.supplyAsync(() -> {
                long startTime = System.currentTimeMillis();
                try {
                    Todo todo = testTodos.get(idx);
                    todo.setIsDone(1);
                    todoService.updateTodo(todo);                    
                } catch (Exception e) {
                    log.error("Todo {} 업데이트 중 에러: {}", idx+1, e.getMessage(), e);
                } finally {
                    latch.countDown();
                }
                return System.currentTimeMillis() - startTime;
            });
            futures.add(future);
        }

        // 모든 작업이 완료될 때까지 대기 (최대 30초)
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        assertTrue(completed, "일부 작업이 시간 내에 완료되지 않았습니다.");

        List<Long> executionTimes = futures.stream()
            .map(CompletableFuture::join)
            .collect(Collectors.toList());

        // 통계 출력
        double avgTime = executionTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
        long maxTime = executionTimes.stream().mapToLong(Long::longValue).max().orElse(0);
        long minTime = executionTimes.stream().mapToLong(Long::longValue).min().orElse(0);

        log.info("동시성 테스트 결과:");
        log.info("평균 실행 시간: {}ms", String.format("%.2f", avgTime));
        log.info("최대 실행 시간: {}ms", maxTime);
        log.info("최소 실행 시간: {}ms", minTime);

        // TodoService를 통한 검증
        for (int i=1;i<testTodos.size();i++) {
            int idx=i;
            Todo updatedTodo = todoService.getTodoById(idx)
                .orElseThrow(() -> new AssertionError("Todo가 존재하지 않습니다. ID: " + idx));
            assertEquals(1, updatedTodo.getIsDone(), "Todo가 완료 상태로 업데이트되지 않았습니다.");
        }
    }

    @Test
    public void testConcurrentActivityUpdates() throws InterruptedException {
        int threadCount = USER_COUNT;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<Future<Long>> futures = new ArrayList<>();
        
        // 각 유저별로 Todo 업데이트와 Activity 조회를 동시에 실행
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            Future<Long> future = executorService.submit(() -> {
                long startTime = System.currentTimeMillis();
                try {
                    // Todo 업데이트
                    Todo todo = testTodos.get(index);
                    todo.setIsDone(1);
                    todoService.updateTodo(todo);
                } finally {
                    latch.countDown();
                }
                return System.currentTimeMillis() - startTime;
            });
            futures.add(future);
        }

        // 모든 작업이 완료될 때까지 대기 (최대 30초)
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        assertTrue(completed, "일부 작업이 시간 내에 완료되지 않았습니다.");

        // 실행 시간 통계 계산 및 출력
        List<Long> executionTimes = new ArrayList<>();
        for (Future<Long> future : futures) {
            try {
                executionTimes.add(future.get());
            } catch (Exception e) {
                fail("작업 실행 중 예외 발생: " + e.getMessage());
            }
        }

        double avgTime = executionTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
        log.info("Todo 업데이트 및 Activity 조회 동시성 테스트 평균 실행 시간: {}ms", String.format("%.2f", avgTime));

        executorService.shutdown();
    }

    @Test
    public void testRealisticTodoUpdates() throws InterruptedException {
        int userCount = USER_COUNT;
        int todosPerUser = 7;
        int timeWindowSeconds = 60; // n초의 제한시간(소요시간이 n초가 넘어가면 종료)
        
        CountDownLatch latch = new CountDownLatch(userCount * todosPerUser);
        AtomicInteger successCount = new AtomicInteger(0);
        List<Long> responseTimes = Collections.synchronizedList(new ArrayList<>());
        
        ExecutorService executorService = Executors.newFixedThreadPool(userCount);
        long startTime = System.currentTimeMillis();
        
        try {
            // 각 사용자별로 여러 Todo를 체크하는 시나리오
            for (int userId = 0; userId < userCount; userId++) {
                final int currentUser = userId;
                executorService.submit(() -> {
                    Random random = new Random();
                    
                    for (int todoCount = 0; todoCount < todosPerUser; todoCount++) {
                        try {
                            // timeWindowSeconds를 활용하여 요청 간격 조절
                            long elapsedTime = System.currentTimeMillis() - startTime;
                            if (elapsedTime >= timeWindowSeconds * 1000) {
                                break; // 지정된 시간 초과시 종료
                            }
                            
                            // 남은 시간 내에서 랜덤하게 대기
                            long remainingTime = (timeWindowSeconds * 1000) - elapsedTime;
                            if (remainingTime > 0) {
                                Thread.sleep(random.nextInt((int)Math.min(remainingTime, 100)));
                            }
                            
                            long operationStart = System.currentTimeMillis();
                            Todo todo = testTodos.get(currentUser);
                            todo.setIsDone(1);
                            
                            if (todoService.updateTodo(todo)) {
                                successCount.incrementAndGet();
                                responseTimes.add(System.currentTimeMillis() - operationStart);
                            }
                        } catch (Exception e) {
                            log.error("Todo 업데이트 중 에러 - User {}, Todo {}: {}", 
                                currentUser, todoCount, e.getMessage());
                        } finally {
                            latch.countDown();
                        }
                    }
                    return null;
                });
            }
            
            // timeWindowSeconds + 약간의 여유시간으로 타임아웃 설정
            boolean completed = latch.await(timeWindowSeconds + 5, TimeUnit.SECONDS);
            assertTrue(completed, "일부 작업이 시간 내에 완료되지 않았습니다.");
            
            // 결과 분석
            long totalTime = System.currentTimeMillis() - startTime;
            double totalTimeSeconds = totalTime / 1000.0;
            
            DoubleSummaryStatistics stats = responseTimes.stream()
                .mapToDouble(Long::doubleValue)
                .summaryStatistics();
            
            // 결과 출력
            log.info("\n[실제 사용자 시나리오 테스트 결과]");
            log.info("동시 사용자 수: {} 명", userCount);
            log.info("사용자당 Todo 수: {} 개", todosPerUser);
            log.info("총 요청 수: {} 건", userCount * todosPerUser);
            log.info("성공한 요청 수: {} 건", successCount.get());
            log.info("총 소요 시간: {} 초", String.format("%.2f", totalTimeSeconds));
            log.info("평균 응답 시간: {} ms", String.format("%.2f", stats.getAverage()));
            log.info("최대 응답 시간: {} ms", (long)stats.getMax());
            log.info("최소 응답 시간: {} ms", (long)stats.getMin());
            log.info("초당 처리량 (TPS): {}", String.format("%.1f", (successCount.get() / totalTimeSeconds)));
            
            assertEquals(userCount * todosPerUser, successCount.get(), 
                "모든 Todo 업데이트가 성공해야 합니다.");
                
        } finally {
            executorService.shutdown();
        }
    }
}