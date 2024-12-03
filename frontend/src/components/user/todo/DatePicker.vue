<template>
  <div class="w-full md:w-[21.5rem]">
    <!-- Date and Stats -->
    <div class="flex items-center justify-between mb-4">
      <div class="flex items-center gap-3">
        <span class="text-[1.1rem] font-bold pl-3 w-[8rem]">
          {{ currentYear }}년 {{ currentMonth }}월
        </span>
      </div>
      <div class="flex gap-3">
        <button @click="previousMonth" class="text-gray-400 text-xl">&lt;</button>
        <button @click="nextMonth" class="text-gray-400 text-xl">&gt;</button>
      </div>
    </div>

    <!-- Calendar -->
    <div class="mb-8">
      <!-- Weekdays -->
      <div class="grid grid-cols-7 mb-3 font-semibold">
        <div v-for="{ day, color } in weekdays" :key="day" :class="[
          'text-center text-[0.85rem] w-full sm:w-12 h-8 flex items-center justify-center',
          color,
        ]">
          {{ day }}
        </div>
      </div>
      <!-- Days -->
      <div class="grid grid-cols-7 gap-2 min-h-[18rem]">
        <!-- 빈 칸들 (월요일부터 시작) -->
        <template v-for="empty in firstDayOfMonth" :key="'empty-' + empty">
          <div class="aspect-square w-full sm:w-12 h-auto sm:h-12"></div>
        </template>

        <!-- 1일부터 말일까지 -->
        <template v-for="day in daysInMonth" :key="day">
          <div
            class="aspect-square font-semibold w-full sm:w-12 h-auto sm:h-12 flex flex-col items-center justify-center rounded-lg text-[0.85rem] cursor-pointer relative"
            @click="selectDate(day)">
            <div class="w-6 h-6 text-center text-gray-600 border border-gray-200 bg-gray-200 rounded-[0.3rem] mb-0.5">
              <div v-if="todoStore.getUndoneTodoCount(formatDate(day)) > 0">
                {{ todoStore.getUndoneTodoCount(formatDate(day)) }}
              </div>
            </div>
            <div class="text-center rounded-full w-6 h-6 flex items-center justify-center" 
              :class="[
                { 'bg-black text-white': isSelectedDate(day) },
                { 'bg-gray-200': isToday(day) && !isSelectedDate(day) }
              ]">
              {{ day }}
            </div>
          </div>
        </template>
      </div>
    </div>
    <div class="flex justify-center">
      <button
        class="w-[4rem] py-4 rounded-2xl text-center text-base font-semibold cursor-pointer transition-all duration-300 ease-in-out bg-black text-white hover:bg-gray-800"
        @click="confirmDate">
        확인
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useAuthStore } from "@/stores/auth";
import { useCategoryStore } from "@/stores/category";
import { useDateStore } from "@/stores/date";
import { useTodoStore } from "@/stores/todo";
import { useActivityStore } from "@/stores/activity";

const emit = defineEmits(["closeDatePicker"]);
const authStore = useAuthStore();
const categoryStore = useCategoryStore();
const dateStore = useDateStore();
const todoStore = useTodoStore();
const activityStore = useActivityStore();

onMounted(async () => {
  await authStore.fetchUserInfo();
  if (authStore.user.id) {
    await categoryStore.fetchCategories(authStore.user.id);
  }
  
  // 현재 선택된 날짜 기준으로 초기화
  const selectedDate = new Date(dateStore.selectedDate);
  currentDate.value = selectedDate;
  localSelectedDate.value = dateStore.selectedDate;
  
  await todoStore.fetchMonthlyTodos(
    selectedDate.getFullYear(),
    selectedDate.getMonth() + 1
  );
});

// 현재 날짜 상태 관리
const currentDate = ref(new Date(dateStore.selectedDate));
const currentYear = computed(() => currentDate.value.getFullYear());
const currentMonth = computed(() => currentDate.value.getMonth() + 1);
const weekdays = [
  { day: "월", color: "" },
  { day: "화", color: "" },
  { day: "수", color: "" },
  { day: "목", color: "" },
  { day: "금", color: "" },
  { day: "토", color: "text-blue-500" },
  { day: "일", color: "text-red-500" },
];

// 선택된 날짜 상태 관리
const localSelectedDate = ref(null);

// 선택한 날짜인지 확인
const isSelectedDate = (day) => {
  const dateString = new Date(currentYear.value, currentMonth.value - 1, day + 1)
    .toISOString()
    .split("T")[0];
  return localSelectedDate.value === dateString;
};

// 오늘 날짜인지 확인
const isToday = (day) => {
  const today = new Date();
  return (
    today.getDate() === day &&
    today.getMonth() === currentMonth.value - 1 &&
    today.getFullYear() === currentYear.value
  );
};

// 선택한 날짜 포맷팅
const selectDate = (day) => {
  const selectedDate = new Date(currentYear.value, currentMonth.value - 1, day + 1);
  const formattedDate = selectedDate.toISOString().split("T")[0];
  localSelectedDate.value = formattedDate;
};

// 날짜 수정 완료하기
const confirmDate = async () => {
  const todo = todoStore.todo;
  const oldDate = todo.date;
  const newDate = localSelectedDate.value;

  try {
    await todoStore.fetchTodoUpdate({
      ...todo,
      date: newDate,
    });

    await Promise.all([
      todoStore.fetchTodos(oldDate),
      todoStore.fetchTodos(newDate),
      activityStore.fetchUpdateDailyActivity(oldDate, todo.userId),
      activityStore.fetchUpdateDailyActivity(newDate, todo.userId)
    ]);

    // 달력 날짜 업데이트
    dateStore.setSelectedDate(newDate);
    
    // 월별 투두 목록 갱신
    const newDateObj = new Date(newDate);
    await todoStore.fetchMonthlyTodos(
      newDateObj.getFullYear(),
      newDateObj.getMonth() + 1,
      todo.userId
    );

    emit("closeDatePicker");
  } catch (error) {
    console.error("날짜 수정 실패:", error);
  }
};

// 해당 월의 첫 번째 날의 요일 구하기 (0: 일요일, 1: 월요일, ...)
const firstDayOfMonth = computed(() => {
  const firstDay = new Date(currentYear.value, currentMonth.value - 1, 1).getDay();
  return firstDay === 0 ? 6 : firstDay - 1; // 월요일을 시작으로 조정
});

// 해당 월의 총 일수 구하기
const daysInMonth = computed(() => {
  return new Date(currentYear.value, currentMonth.value, 0).getDate();
});

// 이전 달로 이동
const previousMonth = async () => {
  currentDate.value = new Date(currentYear.value, currentMonth.value - 2, 1);
  await todoStore.fetchMonthlyTodos(
    currentYear.value,
    currentMonth.value,
    todoStore.todo.userId
  );
};

// 다음 달로 이동
const nextMonth = async () => {
  currentDate.value = new Date(currentYear.value, currentMonth.value, 1);
  await todoStore.fetchMonthlyTodos(
    currentYear.value,
    currentMonth.value,
    todoStore.todo.userId
  );
};

// YYYY-MM-DD 형식으로 날짜 변환
const formatDate = (date) => {
  const selectedDate = new Date(currentYear.value, currentMonth.value - 1, date + 1);
  return selectedDate.toISOString().split("T")[0];
};
</script>
