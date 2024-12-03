<template>
  <div class="bg-white rounded-xl shadow-sm p-6">
    <div class="flex justify-between items-center mb-6">
      <div class="flex items-center gap-4">
        <h3 class="text-xl font-bold text-gray-800">활동기록</h3>
        <div class="flex items-center gap-3">
          <button 
            @click="thisYear--" 
            class="w-7 h-7 flex items-center justify-center text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-full transition-all"
            :disabled="thisYear <= 2018"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
            </svg>
          </button>
          
          <div class="flex items-center gap-1">
            <span class="text-xl font-bold text-gray-800">{{ thisYear }}</span>
            <span class="text-sm text-gray-500">년</span>
          </div>
          
          <button 
            @click="thisYear++" 
            class="w-7 h-7 flex items-center justify-center text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-full transition-all"
            :disabled="thisYear >= new Date().getFullYear()"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
            </svg>
          </button>
        </div>
      </div>
      <div class="hidden md:flex items-center gap-3">
        <template v-for="(level, index) in activityLevels" :key="index">
          <div class="flex items-center gap-1.5">
            <div class="w-3 h-3 rounded-sm" :class="level.color"></div>
            <span class="text-xs text-gray-500">{{ level.label }}</span>
          </div>
        </template>
      </div>
    </div>

    <div class="overflow-x-auto hide-scrollbar">
      <div class="min-w-[800px]">
        <div class="grid grid-rows-2 gap-6">
          <div class="grid grid-cols-6 gap-2 px-1">
            <MonthlyGrass
              v-for="month in 6"
              :key="month - 1"
              :month="month - 1"
              :year="thisYear"
              :activities="activityStore.activities"
              :activity-levels="activityLevels"
              @date-click="handleDateClick"
            />
          </div>
          <div class="grid grid-cols-6 gap-2 px-1">
            <MonthlyGrass
              v-for="month in 6"
              :key="month + 5"
              :month="month + 5"
              :year="thisYear"
              :activities="activityStore.activities"
              :activity-levels="activityLevels"
              @date-click="handleDateClick"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, computed, ref, watch } from "vue";
import { useAuthStore } from "@/stores/auth";
import { useActivityStore } from "@/stores/activity";
import { useDateStore } from "@/stores/date";
import { useTodoStore } from "@/stores/todo";
import MonthlyGrass from '@/components/user/grass/MonthlyGrass.vue';

const authStore = useAuthStore();
const activityStore = useActivityStore();
const dateStore = useDateStore();
const todoStore = useTodoStore();
const thisYear = ref(new Date().getFullYear());

const props = defineProps({
  userId: {
    type: Number,
    required: false,
  },
});

const activityLevels = [
  { label: "0%", color: "bg-[#dddfe0]" },
  { label: "1~25%", color: "bg-[#c6e48b]" },
  { label: "26~50%", color: "bg-[#7bc96f]" },
  { label: "51~75%", color: "bg-[#239a3b]" },
  { label: "76~100%", color: "bg-[#196127]" },
];

const availableYears = computed(() => {
  const currentYear = new Date().getFullYear();
  return Array.from({length: currentYear - 2017}, (_, i) => currentYear - i);
});

const fetchActivities = async () => {
  const targetUserId = props.userId || authStore.user.id;
  await activityStore.fetchActivities(targetUserId, thisYear.value);
};

const handleDateClick = async (dateString) => {
  const targetUserId = props.userId || authStore.user.id;
  await Promise.all([
    dateStore.setSelectedDate(dateString),
    todoStore.fetchTodos(dateString, targetUserId),
  ]);
};

onMounted(fetchActivities);
watch([() => props.userId, thisYear], fetchActivities);
</script>

<style scoped>
.hide-scrollbar {
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.hide-scrollbar::-webkit-scrollbar {
  display: none;
}
</style>
