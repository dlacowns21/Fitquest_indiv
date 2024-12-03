<template>
  <Transition name="backdrop">
    <div
      class="fixed inset-0 bg-black/40 backdrop-blur-sm flex justify-center items-center z-[999]"
      @click="closeMenu"
    >
      <Transition name="todo-menu">
        <div class="bg-white rounded-xl p-6 w-[400px] shadow-xl" @click.stop>
          <template v-if="!showDatePicker">
            <!-- 헤더 -->
            <div class="mb-6">
              <div class="text-gray-400 text-sm mb-2">할 일</div>
              <div class="text-gray-800 font-medium text-lg">
                {{ todoStore.todo.content }}
              </div>
            </div>

            <!-- 메인 액션 버튼들 -->
            <div class="flex gap-2 mb-6">
              <button
                @click="editTodo"
                class="flex-1 py-2.5 px-4 rounded-lg text-sm font-medium flex items-center justify-center gap-2 border border-gray-200 hover:bg-gray-50 transition-colors"
              >
                <PencilIcon class="w-4 h-4 text-gray-600" />
                수정하기
              </button>
              <button
                @click="deleteTodo"
                class="flex-1 py-2.5 px-4 rounded-lg text-sm font-medium flex items-center justify-center gap-2 text-red-600 border border-red-200 hover:bg-red-50 transition-colors"
              >
                <TrashIcon class="w-4 h-4" />
                삭제하기
              </button>
            </div>

            <!-- 날짜 관련 액션들 -->
            <div class="space-y-3">
              <button
                @click="moveTomorrow"
                class="w-full flex items-center justify-between p-3 rounded-lg hover:bg-gray-50 transition-colors group"
              >
                <div class="flex items-center gap-3">
                  <span
                    class="bg-blue-100 text-blue-600 w-8 h-8 rounded-lg flex items-center justify-center group-hover:bg-blue-600 group-hover:text-white transition-colors"
                  >
                    <ArrowRightIcon class="w-4 h-4" />
                  </span>
                  <span class="text-gray-700 text-sm font-medium"
                    >내일 하기</span
                  >
                </div>
                <ChevronRightIcon
                  class="w-4 h-4 text-gray-400 group-hover:text-gray-600"
                />
              </button>

              <button
                @click="openDatePicker"
                class="w-full flex items-center justify-between p-3 rounded-lg hover:bg-gray-50 transition-colors group"
              >
                <div class="flex items-center gap-3">
                  <span
                    class="bg-purple-100 text-purple-600 w-8 h-8 rounded-lg flex items-center justify-center group-hover:bg-purple-600 group-hover:text-white transition-colors"
                  >
                    <CalendarIcon class="w-4 h-4" />
                  </span>
                  <span class="text-gray-700 text-sm font-medium"
                    >날짜 바꾸기</span
                  >
                </div>
                <ChevronRightIcon
                  class="w-4 h-4 text-gray-400 group-hover:text-gray-600"
                />
              </button>
            </div>
          </template>

          <template v-if="showDatePicker">
            <DatePicker @closeDatePicker="closeDatePicker" />
          </template>
        </div>
      </Transition>
    </div>
  </Transition>
</template>

<script setup>
import { useTodoStore } from "@/stores/todo";
import {
  ArrowRightIcon,
  CalendarIcon,
  PencilIcon,
  TrashIcon,
  ChevronRightIcon,
} from "@heroicons/vue/24/outline";
import { onMounted, ref } from "vue";
import DatePicker from "./DatePicker.vue";

const showDatePicker = ref(false);
const emit = defineEmits(["close"]);
const props = defineProps(["selectedTodoId"]);
const todoStore = useTodoStore();

const closeMenu = () => {
  emit("close");
};
const editTodo = () => {
  emit("edit", props.selectedTodoId);
};
const deleteTodo = () => {
  emit("delete", props.selectedTodoId);
};
const moveTomorrow = () => {
  emit("moveTomorrow", props.selectedTodoId);
};

const openDatePicker = () => {
  showDatePicker.value = true;
};

const closeDatePicker = () => {
  showDatePicker.value = false;
  closeMenu();
};

onMounted(() => {
  todoStore.fetchTodo(props.selectedTodoId);
});
</script>

<style scoped>
.backdrop-enter-active,
.backdrop-leave-active {
  transition: all 0.3s ease;
}

.backdrop-enter-from,
.backdrop-leave-to {
  opacity: 0;
}

.backdrop-enter-active .todo-menu-enter-active {
  transition: all 0.3s ease;
}

.todo-menu-enter-active {
  animation: slide-up 0.3s ease forwards;
}

.todo-menu-leave-active {
  animation: slide-up 0.3s ease reverse;
}

@keyframes slide-up {
  0% {
    transform: translateY(20px);
    opacity: 0;
  }
  100% {
    transform: translateY(0);
    opacity: 1;
  }
}
</style>
