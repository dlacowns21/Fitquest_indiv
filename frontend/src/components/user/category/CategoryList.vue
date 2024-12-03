<template>
  <div class="relative">
    <div v-if="filteredCategories.length > 0">
      <div class="space-y-4">
        <div v-for="category in filteredCategories" :key="category.id">
          <div class="flex items-center bg-gray-100 rounded-[16px] py-2 px-3 cursor-pointer hover:bg-gray-200 w-fit"
            @click.stop="props.userId === authStore.user.id && openTodoForm(category)">
            <div class="flex items-center">
              <GlobeAltIcon v-if="category.isPublic" class="w-3.5 h-3.5 text-gray-400" />
              <LockClosedIcon v-else class="w-3.5 h-3.5 text-gray-400" />
              <span class="text-sm font-bold bg-clip-text mx-1.5" :style="{
                color: category.color.includes('gradient') ? 'transparent' : category.color,
                backgroundImage: category.color.includes('gradient') ? category.color : 'none',
                '-webkit-background-clip': 'text'
              }">
                {{ truncateText(category.title) }}
              </span>
            </div>
            <span v-if="props.userId === authStore.user.id" class="text-gray-400">+</span>
          </div>
          <div class="mb-7">
            <TodoList 
              :categoryId="category.id" 
              :group="'todos'"
              :userId="Number(props.userId)"
              @doneTodoCountUpdate="emit('doneTodoCountUpdate', props.userId)" />
            <Transition name="todo-form">
              <div v-if="selectedCategory && selectedCategory.id === category.id && props.userId === authStore.user.id"
                class="mt-3.5 mb-7">
                <div class="flex items-center gap-1.5">
                  <input class="w-[19px] h-[19px] rounded border bg-[#dddfe0] border-[#dddfe0]" />
                  <input type="text" placeholder="할 일 입력" class="pb-1.5 w-full text-sm outline-none caret-blue-500"
                    :ref="(el) => {if (selectedCategory?.id === category.id) todoInput = el;}"
                    :style="{
                      borderImage: selectedCategory.color.includes('gradient') ?
                        `${selectedCategory.color} 1` :
                        'none',
                      borderBottom: selectedCategory.color.includes('gradient') ?
                        '2px solid transparent' :
                        `2px solid ${selectedCategory.color}`,
                      borderImageSlice: selectedCategory.color.includes('gradient') ? 1 : 'none'
                    }"
                    v-model="todo.content" @click.stop @keyup.enter="handleAddTodo(todo)"
                    @compositionstart="isComposing = true"
                    @compositionend="isComposing = false; handleCompositionEnd($event, todo)" />
                </div>
              </div>
            </Transition>
          </div>
        </div>
      </div>
    </div>
    <div v-else class="h-full flex flex-col items-center justify-center text-gray-400 pb-10">
      <div class="material-icons text-5xl mb-3">sentiment_neutral</div>
      <div>카테고리가 없어요</div>
      <RouterLink v-if="props.userId === authStore.user.id" to="/category-regist"
        class="text-sm font-semibold text-blue-600 hover:text-blue-800 flex items-center gap-1">
        추가하러 가기
      </RouterLink>
    </div>
    <button @click="toggleChatbot"
      class="fixed bottom-4 right-4 bg-black text-white rounded-full p-3 shadow-lg hover:bg-gray-800 transition-colors">
      <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
          d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z" />
      </svg>
    </button>
    <ChatbotDialog :isOpen="isChatbotOpen" @close="isChatbotOpen = false" />
  </div>
</template>

<script setup>
import { useAuthStore } from "@/stores/auth";
import { useCategoryStore } from "@/stores/category";
import {
  GlobeAltIcon,
  LockClosedIcon,
} from "@heroicons/vue/24/outline";
import { onMounted, watch, ref, onUnmounted, computed } from "vue";
import TodoList from "@/components/user/todo/TodoList.vue";
import { useTodoStore } from "@/stores/todo";
import { useDateStore } from "@/stores/date";
import { useActivityStore } from "@/stores/activity";
import ChatbotDialog from "@/components/chatbot/ChatbotDialog.vue";
const emit = defineEmits(["doneTodoCountUpdate"]);
const props = defineProps({
  selectedDate: {
    type: String,
    required: false
  },
  userId: {
    type: Number,
    required: true
  }
});

const authStore = useAuthStore();
const categoryStore = useCategoryStore();
const todoStore = useTodoStore();
const dateStore = useDateStore();
const activityStore = useActivityStore();
const selectedCategory = ref(null);
const filteredCategories = computed(() => {
  return categoryStore.categories.filter(category =>
    category.isPublic === 1 || props.userId === authStore.user.id
  );
});
const todo = computed(() => ({
  userId: props.userId || authStore.user.id,
  categoryId: null,
  content: "",
  date: dateStore.selectedDate,
}));
const todoInput = ref(null);
const isComposing = ref(false);
const isChatbotOpen = ref(false);

const closeAddTodo = () => {
  selectedCategory.value = null;
  todo.value.content = "";
};

const openTodoForm = (category) => {
  selectedCategory.value = category;
  todo.value.categoryId = category.id;
  todo.value.date = dateStore.selectedDate;

  setTimeout(() => {
    if (todoInput.value) {
      todoInput.value.focus();
    }
  }, 0);
};

const handleCompositionEnd = (event, todo) => {
  if (event.key === 'Enter') {
    handleAddTodo(todo);
  }
};

const handleAddTodo = async (todoData) => {
  if (isComposing.value) return;
  if (todoData.content.trim()) {
    try {
      const currentTodo = {
        ...todoData,
        date: dateStore.selectedDate
      };

      const result = await todoStore.fetchAddTodo(currentTodo);
      if (result?.success) {
        await todoStore.fetchTodos(currentTodo.date, props.userId);
        currentTodo.content = "";
        await activityStore.fetchUpdateDailyActivity(currentTodo.date, currentTodo.userId);
      }
    } catch (error) {
      console.error("할 일 추가 실패:", error);
    }
  }
  closeAddTodo();
};

const truncateText = (text) => {
  return text.length > 20 ? text.slice(0, 20) + "..." : text;
};

const toggleChatbot = () => {
  isChatbotOpen.value = !isChatbotOpen.value;
};

onMounted(async () => {
  if (!props.selectedDate && !dateStore.selectedDate) {
    dateStore.setSelectedDate(new Date().toISOString().split("T")[0]);
  }

  if (props.userId) {
    await Promise.all([
      categoryStore.fetchCategories(props.userId),
      todoStore.fetchTodos(props.selectedDate || dateStore.selectedDate, props.userId)
    ]);
  }

  window.addEventListener("click", closeAddTodo);
  window.addEventListener("keydown", (event) => {
    if (event.key === "Escape") closeAddTodo();
  });
});

watch(
  [
    () => props.selectedDate || dateStore.selectedDate,
    () => props.userId
  ],
  async ([newDate, userId]) => {
    if (newDate && userId) {
      await todoStore.fetchTodos(newDate, userId);
    }
  },
  { immediate: true }
);

onUnmounted(() => {
  window.removeEventListener("click", closeAddTodo);
  window.removeEventListener("keydown", closeAddTodo);
});
</script>
<style scoped>
.todo-form-enter-active {
  animation: bounce-in 0.5s;
}

.todo-form-leave-active {
  animation: bounce-in 0.5s reverse;
}

@keyframes bounce-in {
  0% {
    transform: scale(0.3);
    opacity: 0;
  }

  50% {
    transform: scale(1.05);
    opacity: 0.5;
  }

  100% {
    transform: scale(1);
    opacity: 1;
  }
}
</style>
