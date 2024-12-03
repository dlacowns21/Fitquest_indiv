// stores/category.js
import { defineStore } from "pinia";
import { ref } from "vue";
import { useAuthStore } from "./auth";
import http from "@/api/http";
import { useRouter } from "vue-router";
import { useTodoStore } from "./todo";
import { useActivityStore } from "./activity";

export const useCategoryStore = defineStore("category", () => {
  const categories = ref([]);
  const category = ref({});
  const authStore = useAuthStore();
  const todoStore = useTodoStore();
  const activityStore = useActivityStore();
  const router = useRouter();

  // 카테고리 목록 조회
  const fetchCategories = async (userId) => {
    if (!userId) {
      console.warn("fetchCategories: userId is required");
      return;
    }
    
    const accessToken = authStore.getToken();
    try {
      const response = await http.get(`/category/${userId}`, {
        headers: {
          Authorization: `Bearer ${accessToken}`,
        },
      });
      if (response.data) {
        categories.value = response.data;
      }
    } catch (error) {
      console.error(
        "카테고리 조회 실패:",
        error.response?.data || error.message
      );
      if (error.response?.status === 401) {
        console.error("인증 토큰이 만료되었거나 유효하지 않습니다");
      }
    }
  };

  const fetchCategory = async (categoryId) => {
    const userId = authStore.user.id;
    const accessToken = authStore.getToken();

    try {
      const response = await http.get(`/category/${userId}/${categoryId}`, {
        headers: {
          Authorization: `Bearer ${accessToken}`,
        },
      });
      if (response.data) {
        category.value = response.data;
      }
    } catch (error) {
      console.error(
        "카테고리 조회 실패:",
        error.response?.data || error.message
      );
      if (error.response?.status === 401) {
        console.error("인증 토큰이 만료되었거나 유효하지 않습니다");
      }
    }
  };

  const fetchAddCategory = async (category) => {
    const userId = authStore.user.id;
    const accessToken = authStore.getToken();
    try {
      const response = await http.post(`/category/${userId}`, category, {
        headers: {
          Authorization: `Bearer ${accessToken}`,
        },
      });
      if (response.data) {
        categories.value.push(response.data);
        router.go(-1);
      }
    } catch (error) {
      console.error(
        "카테고리 등록 실패:",
        error.response?.data || error.message
      );
      if (error.response?.status === 401) {
        console.error("인증 토큰이 만료되었거나 유효하지 않습니다");
      }
    }
  };

  const fetchUpdateCategory = async (category) => {
    const accessToken = authStore.getToken();
    try {
      const response = await http.put(`/category/${category.id}`, category, {
        headers: {
          Authorization: `Bearer ${accessToken}`,
        },
      });
      if (response.data) {
        fetchCategories();
        router.go(-1);
      }
    } catch (error) {
      console.error(
        "카테고리 수정 실패:",
        error.response?.data || error.message
      );
      if (error.response?.status === 401) {
        console.error("인증 토큰이 만료되었거나 유효하지 않습니다");
      }
    }
  };

  const fetchDeleteCategory = async (categoryId) => {
    const userId = authStore.user.id;
    const accessToken = authStore.getToken();
    try {
      const todos = await http.get(`/todo/list/${categoryId}/${userId}`, {
        headers: {
          Authorization: `Bearer ${accessToken}`,
        },
      });
      const todosDates = todos.data.map((todo) => todo.date);
      const response = await http.delete(`/category/${categoryId}`, {
        headers: {
          Authorization: `Bearer ${accessToken}`,
        },
      });
      if (response.data) {
        await Promise.all([
          fetchCategories(userId),
          todosDates.map((date) =>
            activityStore.fetchUpdateDailyActivity(date, userId)
          ),
          todoStore.fetchMonthlyTodos(
            new Date().getFullYear(),
            new Date().getMonth() + 1,
            userId
          ),
          activityStore.fetchActivities(userId, new Date().getFullYear()),
        ]);
        categories.value = categories.value.filter(cat => cat.id !== categoryId);
        router.go(-1);
      }
    } catch (error) {
      console.error("카테고리 삭제 실패:", error);
      if (error.response?.status === 401) {
        console.error("인증 토큰이 만료되었거나 유효하지 않습니다");
      }
    }
  };

  return {
    categories,
    category,
    fetchCategories,
    fetchCategory,
    fetchAddCategory,
    fetchUpdateCategory,
    fetchDeleteCategory,
  };
});
