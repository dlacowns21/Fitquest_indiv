// stores/todo.js
// 할일 관련 스토어
// 월별 투두 목록과 일자별 투두 목록을 관리함
// 할일 삭제, 추가, 수정 시 월별 투두 목록과 일자별 투두 목록을 갱신함
import { defineStore } from "pinia";
import { computed, ref } from "vue";
import { useAuthStore } from "./auth";
import http from "@/api/http";
import { useDateStore } from "./date";

export const useTodoStore = defineStore("todo", () => {
  const monthlyTodos = ref([]);
  const dailyTodos = ref([]);
  const todo = ref({});
  const dateStore = useDateStore();
  const authStore = useAuthStore();
  const monthlyUndoneCounts = computed(() => {
    const counts = {};
    monthlyTodos.value.forEach((todo) => {
      if (!todo.isDone) {
        counts[todo.date] = (counts[todo.date] || 0) + 1;
      }
    });
    return counts;
  });

  // 투두 목록 조회(일자별로)
  const fetchTodos = async (date, userId = authStore.user.id) => {
    const accessToken = authStore.getToken();
    try {
      const response = await http.get(`/todo/${date}/${userId}`, {
        headers: {
          Authorization: `Bearer ${accessToken}`,
        },
      });
      if (response.data) {
        dailyTodos.value = response.data;
      }
    } catch (error) {
      console.error("할일 조회 실패:", error);
    }
  };

  // 할일 등록
  const fetchAddTodo = async (todoData) => {
    if (!todoData.content?.trim()) {
      throw new Error("할일 내용을 입력해주세요");
    }

    const categoryTodos = dailyTodos.value.filter(
      (todo) => todo.categoryId === todoData.categoryId
    );
    const maxOrder = categoryTodos.length > 0
      ? Math.max(...categoryTodos.map((t) => t.todoOrder || 0))
      : -1;
    todoData.todoOrder = maxOrder + 1;

    try {
      const accessToken = authStore.getToken();
      const response = await http.post("/todo", todoData, {
        headers: { Authorization: `Bearer ${accessToken}` },
      });

      if (response.data) {
        // 한 번의 데이터 갱신으로 통합
        await Promise.all([
          fetchTodos(todoData.date, todoData.userId),
          fetchMonthlyTodos(
            new Date(todoData.date).getFullYear(),
            new Date(todoData.date).getMonth() + 1,
            todoData.userId
          )
        ]);
        return { success: true };
      }
    } catch (error) {
      if (error.response?.status === 401) {
        throw new Error("인증 토큰이 만료되었거나 유효하지 않습니다.");
      } else {
        throw new Error("할일 추가에 실패했습니다.");
      }
    }
  };

  // 할일 수정
  const fetchTodoUpdate = async (todoData) => {
    const originalTodos = [...dailyTodos.value];
    try {
      dailyTodos.value = dailyTodos.value.map(todo => 
        todo.id === todoData.id ? { ...todo, ...todoData } : todo
      );

      const accessToken = authStore.getToken();
      const response = await http.put(`/todo/${todoData.id}`, todoData, {
        headers: {
          Authorization: `Bearer ${accessToken}`,
        },
      });

      if (response.data) {
        await fetchTodos(todoData.date, todoData.userId);
        const date = new Date(todoData.date);
        await fetchMonthlyTodos(date.getFullYear(), date.getMonth() + 1, todoData.userId);
        return { success: true };
      }
    } catch (error) {
      dailyTodos.value = originalTodos;
      throw new Error(error.response?.data?.message || "할일 수정에 실패했습니다.");
    }
  };

  const fetchTodo = async (todoId) => {
    try {
      const accessToken = authStore.getToken();
      const response = await http.get(`/todo/${todoId}`, {
        headers: {
          Authorization: `Bearer ${accessToken}`,
        },
      });
      if (response.data) {
        todo.value = response.data;
      }
    } catch (error) {
      throw new Error(
        error.response?.data?.message || "할일 조회에 실패했습니다."
      );
    }
  };

  const fetchDeleteTodo = async (todoId) => {
    const todoToDelete = dailyTodos.value.find(t => t.id === todoId);
    if (!todoToDelete) return;

    try {
      const accessToken = authStore.getToken();
      const response = await http.delete(`/todo/${todoId}`, {
        headers: {
          Authorization: `Bearer ${accessToken}`,
        },
      });
      if (response.data) {
        await fetchTodos(todoToDelete.date, todoToDelete.userId);
        const date = new Date(todoToDelete.date);
        await fetchMonthlyTodos(date.getFullYear(), date.getMonth() + 1, todoToDelete.userId);
        return { success: true };
      }
    } catch (error) {
      throw new Error(
        error.response?.data?.message || "할일 삭제에 실패했습니다."
      );
    }
  };

  const getUndoneTodoCount = (date) => {
    return monthlyUndoneCounts.value[date] || 0;
  };

  const fetchMonthlyTodos = async (year, month, userId) => {
    if (!userId) return;
    
    if (month < 10) {
      month = "0" + month;
    }
    try {
      const accessToken = authStore.getToken();
      const response = await http.get(`/todo/${userId}/${year}/${month}`, {
        headers: {
          Authorization: `Bearer ${accessToken}`,
        },
      });
      monthlyTodos.value = response.data;
    } catch (error) {
      console.error('todos 조회 실패:', error);
      monthlyTodos.value = [];  // 에러 시 초기화
    }
  };

  // 로컬 상태 즉시 업데이트를 위한 메서드
  const updateLocalTodos = (todos) => {
    dailyTodos.value = todos;
  };

  return {
    monthlyTodos,
    dailyTodos,
    todo,
    monthlyUndoneCounts,
    getUndoneTodoCount,
    fetchTodo,
    fetchTodos,
    fetchAddTodo,
    fetchTodoUpdate,
    fetchDeleteTodo,
    fetchMonthlyTodos,
    updateLocalTodos,
  };
});
