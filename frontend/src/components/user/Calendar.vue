<template>
  <div class="w-full h-full bg-white rounded-2xl shadow-md p-3">
    <!-- Date and Stats -->
    <div class="flex items-center justify-between mb-2">
      <div class="flex items-center gap-2">
        <span class="text-[0.95rem] font-bold pl-2">
          {{ currentYear }}년 {{ currentMonth }}월
        </span>
      </div>
      <div class="flex gap-2">
        <button @click="previousMonth" class="text-gray-400 hover:text-gray-600 transition-colors text-lg">
          &lt;
        </button>
        <button @click="nextMonth" class="text-gray-400 hover:text-gray-600 transition-colors text-lg">
          &gt;
        </button>
      </div>
    </div>

    <!-- Calendar -->
    <div class="flex flex-col h-[calc(100%-2rem)]">
      <!-- 전체 높이에서 헤더 높이 뺌 -->
      <!-- Weekdays -->
      <div class="grid grid-cols-7 mb-1 font-semibold gap-1">
        <div v-for="{ day, color } in weekdays" :key="day" :class="[
          'text-center text-[0.8rem] w-full h-6 flex items-center justify-center',
          color,
        ]">
          {{ day }}
        </div>
      </div>
      <!-- Days Grid Container -->
      <div class="flex-1 grid grid-cols-7 grid-rows-6 gap-1">
        <!-- 항상 6행 그리드 -->
        <!-- 빈 칸들 -->
        <template v-for="empty in firstDayOfMonth" :key="'empty-' + empty">
          <div class="w-full"></div>
        </template>

        <!-- 1일부터 말일까지 -->
        <template v-for="day in daysInMonth" :key="day">
          <div
            class="w-full flex flex-col items-center justify-center rounded-lg text-[0.8rem] cursor-pointer relative hover:bg-gray-50 transition-all duration-200"
            @click="selectDate(day)">
            <div
              class="w-5 h-5 text-center text-gray-600 border border-[#e5e7e9] bg-[#f8f9fa] hover:bg-[#e9ecef] transition-colors rounded-[0.3rem] mb-0.5 shadow-sm">
              <div v-if="todoStore.getUndoneTodoCount(formatDate(day)) > 0">
                {{ todoStore.getUndoneTodoCount(formatDate(day)) }}
              </div>
            </div>
            <div class="text-center w-6 h-6 flex items-center justify-center transition-all duration-200" :class="[
              {
                'bg-black text-white shadow-md transform scale-105 rounded-full':
                  isSelectedDate(day) || (isToday(day) && isSelectedDate(day)),
                'bg-gray-200 rounded-full': isToday(day) && !isSelectedDate(day),
              },
            ]">
              {{ day }}
            </div>
          </div>
        </template>

        <!-- 나머지 빈 칸 채우기 -->
        <template v-for="empty in remainingEmptyCells" :key="'remaining-' + empty">
          <div class="w-full"></div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from "vue";
import { useAuthStore } from "@/stores/auth";
import { useCategoryStore } from "@/stores/category";
import { useDateStore } from "@/stores/date";
import { useTodoStore } from "@/stores/todo";

const props = defineProps({
  userId: {
    type: Number,
    required: true,
  },
});

const authStore = useAuthStore();
const categoryStore = useCategoryStore();
const dateStore = useDateStore();
const todoStore = useTodoStore();

onMounted(async () => {
  await authStore.fetchUserInfo();
  if (props.userId) {
    await categoryStore.fetchCategories(props.userId);
  }
});

// 현재 날짜 상태 관리
const currentYear = computed(() => dateStore.currentYear);
const currentMonth = computed(() => dateStore.currentMonth);
const weekdays = [
  { day: "월", color: "" },
  { day: "화", color: "" },
  { day: "수", color: "" },
  { day: "목", color: "" },
  { day: "금", color: "" },
  { day: "토", color: "text-blue-500" },
  { day: "일", color: "text-red-500" },
];

// 해당 월의 첫 번째 날의 요일 구하기 (0: 일요일, 1: 월요일, ...)
const firstDayOfMonth = computed(() => {
  const firstDay = new Date(
    currentYear.value,
    currentMonth.value - 1,
    1
  ).getDay();
  return firstDay === 0 ? 6 : firstDay - 1; // 월요일을 시작으로 조정
});

// 해당 월의 총 일수 구하기
const daysInMonth = computed(() => {
  return new Date(currentYear.value, currentMonth.value, 0).getDate();
});

// 오늘 날짜 체크
const isSelectedDate = (day) => {
  return (
    dateStore.selectedDate ===
    new Date(currentYear.value, currentMonth.value - 1, day + 1)
      .toISOString()
      .split("T")[0]
  );
};

// 현재 날짜인지 체크
const isToday = (day) => {
  const today = new Date();
  return (
    today.getFullYear() === currentYear.value &&
    today.getMonth() === currentMonth.value - 1 &&
    today.getDate() === day
  );
};

// 이전 달로 이동
const previousMonth = async () => {
  dateStore.currentYear = new Date(
    currentYear.value,
    currentMonth.value - 2,
    1
  ).getFullYear();
  dateStore.currentMonth =
    new Date(currentYear.value, currentMonth.value - 2, 1).getMonth() + 1;
  await todoStore.fetchMonthlyTodos(
    currentYear.value,
    currentMonth.value,
    props.userId
  );
};

// 다음 달로 이동
const nextMonth = async () => {
  dateStore.currentYear = new Date(
    currentYear.value,
    currentMonth.value,
    1
  ).getFullYear();
  dateStore.currentMonth =
    new Date(currentYear.value, currentMonth.value, 1).getMonth() + 1;
  await todoStore.fetchMonthlyTodos(
    currentYear.value,
    currentMonth.value,
    props.userId
  );
};

// 날짜 선택
const selectDate = async (day) => {
  const selectedDate = new Date(
    dateStore.currentYear,
    dateStore.currentMonth - 1,
    day + 1
  );
  const formattedDate = selectedDate.toISOString().split("T")[0];
  dateStore.setSelectedDate(formattedDate);
  await todoStore.fetchTodos(formattedDate, props.userId);
  emit("dateSelected", formattedDate);
};

const emit = defineEmits(["dateSelected"]);

// YYYY-MM-DD 형식으로 날짜 변환
const formatDate = (date) => {
  const selectedDate = new Date(
    dateStore.currentYear,
    dateStore.currentMonth - 1,
    date + 1
  );
  return selectedDate.toISOString().split("T")[0];
};

watch(
  [
    () => dateStore.currentYear,
    () => dateStore.currentMonth,
    () => props.userId,
  ],
  async ([year, month, userId]) => {
    if (userId) {
      await todoStore.fetchMonthlyTodos(year, month, userId);
    }
  },
  { immediate: true }
);

watch(
  () => props.userId,
  async (newUserId) => {
    if (newUserId) {
      await categoryStore.fetchCategories(newUserId);
    }
  },
  { immediate: true }
);

const remainingEmptyCells = computed(() => {
  const totalCells = 42; // 6행 * 7열
  const usedCells = firstDayOfMonth.value + daysInMonth.value;
  return totalCells - usedCells;
});
</script>

<style scoped>
/* 달력 그리드 컨테이너에 최소 높이 설정 */
.grid-rows-6 {
  grid-template-rows: repeat(6, 1fr);
}
</style>
