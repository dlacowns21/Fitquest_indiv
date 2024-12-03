<template>
  <div class="flex flex-col">
    <div class="flex items-center justify-between mb-2">
      <span class="text-sm font-medium text-gray-500">{{ month + 1 }}월</span>
    </div>
    <div class="flex flex-col gap-1">
      <div v-for="row in 6" :key="row" class="flex gap-1">
        <template v-for="col in 7" :key="col">
          <div
            v-if="isValidDate(row - 1, col - 1)"
            class="w-3.5 h-3.5 rounded-sm relative group cursor-pointer hover:ring-2 hover:ring-gray-400"
            :class="getActivityColor(getActivityLevel(row - 1, col - 1))"
            @click="handleDateClick(getDate(row - 1, col - 1))"
            @mouseenter="handleMouseEnter($event, row - 1, col - 1)"
            @mouseleave="handleMouseLeave"
          >
            <!-- 툴팁 -->
            <div
              v-if="tooltipVisible && currentCell.row === row - 1 && currentCell.col === col - 1"
              :class="[
                'fixed z-50 px-2 py-1 text-xs text-white bg-gray-800 rounded-md whitespace-nowrap transition-opacity duration-200',
                tooltipPosition === 'top' ? 'mb-2' : 'mt-2'
              ]"
              :style="tooltipStyle"
            >
              {{ formatDate(getDate(row - 1, col - 1)) }}
              <div
                :class="[
                  'absolute left-1/2 transform -translate-x-1/2 border-4 border-transparent',
                  tooltipPosition === 'top' ? 'bottom-0 translate-y-full border-t-gray-800' : 'top-0 -translate-y-full border-b-gray-800'
                ]"
              />
            </div>
          </div>
          <div v-else class="w-3.5 h-3.5"></div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue';

const props = defineProps({
  month: { type: Number, required: true },
  year: { type: Number, required: true },
  activities: { type: Object, required: true },
  activityLevels: { type: Array, required: true }
});

const emit = defineEmits(['date-click']);

// 해당 월의 첫 날짜와 마지막 날짜 계산
const monthDates = computed(() => {
  const firstDay = new Date(props.year, props.month, 1);
  const lastDay = new Date(props.year, props.month + 1, 0);
  const dates = [];
  
  // 첫 주의 시작일까지 이전 달의 날짜로 채우기 (월요일부터 시작)
  let prevMonthDays = firstDay.getDay();
  prevMonthDays = prevMonthDays === 0 ? 6 : prevMonthDays-1; // 월요일이 0이 되도록 조정
  
  const prevMonth = new Date(props.year, props.month, 0);
  for (let i = prevMonthDays - 1; i >= 0; i--) {
    dates.unshift(new Date(props.year, props.month - 1, prevMonth.getDate() - i));
  }
  
  // 현재 달의 날짜 추가
  for (let date = 1; date <= lastDay.getDate(); date++) {
    dates.push(new Date(props.year, props.month, date));
  }
  
  // 마지막 주 남은 칸을 다음 달의 날짜로 채우기
  const remainingDays = 42 - dates.length; // 6주 x 7일 = 42
  for (let i = 1; i <= remainingDays; i++) {
    dates.push(new Date(props.year, props.month + 1, i));
  }
  
  return dates;
});

const isValidDate = (row, col) => {
  const date = getDate(row, col);
  if (!date) return false;
  const currentMonth = new Date(date).getMonth();
  return currentMonth === props.month;
};

const getDate = (row, col) => {
  const index = row * 7 + col;
  const date = monthDates.value[index];
  return date ? date.toISOString().split('T')[0] : null;
};

const getActivityLevel = (row, col) => {
  const dateString = getDate(row, col);
  if (!dateString || !isValidDate(row, col)) return 0;

  const ratio = props.activities[dateString] || 0;
  if (ratio === 0) return 0;
  if (ratio <= 0.25) return 1;
  if (ratio <= 0.5) return 2;
  if (ratio <= 0.75) return 3;
  return 4;
};

const getActivityColor = (level) => {
  return props.activityLevels[level].color;
};

const formatDate = (dateString) => {
  if (!dateString) return "";
  const date = new Date(dateString);
  const month = date.getMonth() + 1;
  const day = date.getDate();
  const dayOfWeek = ["월", "화", "수", "목", "금", "토", "일"][date.getDay() === 0 ? 6 : date.getDay() - 1];
  return `${month}월 ${day}일 (${dayOfWeek})`;
};

const handleDateClick = (dateString) => {
  if (dateString) {
    emit('date-click', dateString);
  }
};

const tooltipVisible = ref(false);
const tooltipPosition = ref('top');
const tooltipStyle = ref({});
const currentCell = ref({ row: -1, col: -1 });

const handleMouseEnter = (event, row, col) => {
  const element = event.target;
  const rect = element.getBoundingClientRect();
  const tooltipHeight = 40;
  const windowHeight = window.innerHeight;
  
  const hasSpaceOnTop = rect.top > tooltipHeight;
  tooltipPosition.value = hasSpaceOnTop ? 'top' : 'bottom';
  
  const left = rect.left + (rect.width / 2);
  const top = hasSpaceOnTop 
    ? rect.top - tooltipHeight 
    : rect.bottom + 8;

  tooltipStyle.value = {
    left: `${left}px`,
    top: `${top}px`,
    transform: 'translateX(-50%)',
  };
  
  currentCell.value = { row, col };
  tooltipVisible.value = true;
};

const handleMouseLeave = () => {
  tooltipVisible.value = false;
  currentCell.value = { row: -1, col: -1 };
};
</script> 