<template>
  <draggable 
    v-model="filteredTodos" 
    :group="isOwner ? { name: props.group, pull: true, put: true } : false"
    item-key="id" 
    @end="handleDragEnd" 
    @start="handleDragStart" 
    @change="handleChange" 
    :animation="300" 
    :delay="50"
    :delayOnTouchOnly="true" 
    :force-fallback="true" 
    :disabled="!isOwner">
    <template #item="{ element: todo }">
      <div class="flex items-center gap-1.5 pt-3.5">
        <div class="relative w-[19px] h-[19px]" :class="{ 'cursor-pointer': isOwner }"
          @click="isOwner && handleDone(todo.id)">
          <svg v-if="todo.isDone" class="w-[19px] h-[19px] text-black" viewBox="0 0 20 20" fill="currentColor">
            <rect width="18" height="18" x="1" y="1" rx="4" fill="currentColor" />
            <path fill="white"
              d="M13.293 7.293a1 1 0 0 1 1.414 1.414l-5 5a1 1 0 0 1-1.414 0l-2.5-2.5a1 1 0 0 1 1.414-1.414L9 11.586l4.293-4.293z" />
          </svg>
          <div v-else class="w-[19px] h-[19px] rounded border bg-[#dadddf] border-gray-300"></div>
        </div>
        <div v-if="isOwner && contentUpdateMode && todo.id === selectedTodoId" class="flex-1">
          <div class="flex" @click.stop>
            <input type="text"
              placeholder="할 일 입력"
              class="pb-1.5 w-full text-sm outline-none caret-blue-500"
              v-model="newTodoContent"
              @compositionstart="isComposing = true"
              @compositionend="isComposing = false; handleCompositionEnd($event)"
              :style="{
                borderImage: categoryStore.category.color.includes('gradient') ?
                  `${categoryStore.category.color} 1` : 'none',
                borderBottom: categoryStore.category.color.includes('gradient') ?
                  '2px solid transparent' : `2px solid ${categoryStore.category.color}`,
                borderImageSlice: categoryStore.category.color.includes('gradient') ? 1 : 'none'
              }" />
            <EllipsisHorizontalIcon class="w-[19px] h-[19px] flex-shrink-0" />
          </div>
        </div>
        <div v-else class="flex-1">
          <button 
            @click="isOwner && toggleMenu(todo.id)" 
            class="flex w-full">
            <div class="w-full text-[14.3px] text-left">
              {{ truncateText(todo.content) }}
            </div>
            <EllipsisHorizontalIcon v-if="isOwner" class="w-[19px] h-[19px] flex-shrink-0" />
          </button>
        </div>
      </div>
    </template>
  </draggable>
  <Transition name="menu">
    <TodoMenu v-if="isOwner && !contentUpdateMode && selectedTodoId !== null" 
      :selectedTodoId="selectedTodoId"
      @close="closeMenu" 
      @edit="handleContent" 
      @delete="goDelete" 
      @moveTomorrow="handleMoveTomorrow" />
  </Transition>
</template>

<script setup>
import { useCategoryStore } from "@/stores/category";
import { useTodoStore } from "@/stores/todo";
import { EllipsisHorizontalIcon } from "@heroicons/vue/24/outline";
import { computed, onBeforeUnmount, onMounted, ref, watch } from "vue";
import draggable from "vuedraggable";
import TodoMenu from "./TodoMenu.vue";
import { useDragStore } from "@/stores/drag";
import { useAuthStore } from "@/stores/auth";
import { useActivityStore } from "@/stores/activity";
import { useDateStore } from "@/stores/date";

const props = defineProps({
  categoryId: {
    type: Number,
    required: true,
  },
  userId: {
    type: Number,
    required: true
  },
  group: {
    type: String,
    default: "todos",
  },
  onDragstart: {
    type: Function,
    default: () => { },
  },
  onDragend: {
    type: Function,
    default: () => { },
  },
});

const authStore = useAuthStore();
const dateStore = useDateStore();
const categoryStore = useCategoryStore();
const todoStore = useTodoStore();
const activityStore = useActivityStore();
const dragStore = useDragStore();

const selectedTodoId = ref(null);
const contentUpdateMode = ref(false);
const newTodoContent = ref(null);
const isDragging = ref(false);
const emit = defineEmits(["doneTodoCountUpdate"]);
const filteredTodos = computed({
  get: () => {
    return todoStore.dailyTodos
      .filter((todo) => todo.categoryId === props.categoryId)
      .sort((a, b) => (a.todoOrder || 0) - (b.todoOrder || 0));
  },
  set: (value) => {
    const updatedTodos = todoStore.dailyTodos.map((todo) => {
      const newTodo = value.find((t) => t.id === todo.id);
      return newTodo || todo;
    });
    todoStore.dailyTodos = updatedTodos;
  },
});

const truncateText = (text) => {
  return text.length > 20 ? text.slice(0, 20) + "..." : text;
};

const isOwner = computed(() => {
  return props.userId === authStore.user.id;
});

const toggleMenu = (id) => {
  if (!isOwner.value) return;
  selectedTodoId.value = selectedTodoId.value === id ? null : id;
};

const isComposing = ref(false);

const handleCompositionEnd = (event) => {
  if (event.key === 'Enter' && !isComposing.value) {
    goUpdate();
  }
};

const goUpdate = async () => {
  if (isComposing.value) return; // IME 입력 중에는 업데이트하지 않음

  if (contentUpdateMode.value && selectedTodoId.value) {
    // 빈 문자열이거나 공백만 있는 경우 early return
    if (!newTodoContent.value || !newTodoContent.value.trim()) {
      contentUpdateMode.value = false;
      selectedTodoId.value = null;
      return;
    }

    const todoToUpdate = todoStore.dailyTodos.find(
      (todo) => todo.id === selectedTodoId.value
    );
    
    if (todoToUpdate) {
      try {
        const updatedTodo = { ...todoToUpdate, content: newTodoContent.value.trim() };
        await todoStore.fetchTodoUpdate(updatedTodo);
        contentUpdateMode.value = false;
        selectedTodoId.value = null;
      } catch (error) {
        console.error("할 일 수정 실패:", error);
      }
    }
  }
};

const goDelete = async (id) => {
  const todo = todoStore.dailyTodos.find((t) => t.id === id);
  try {
    await todoStore.fetchDeleteTodo(id);
    await activityStore.fetchUpdateDailyActivity(todo.date, todo.userId);
    emit("doneTodoCountUpdate", todo.userId);
    closeMenu();
  } catch (error) {
    console.error("할 일 삭제 실패:", error);
  }
};

const closeMenu = () => {
  selectedTodoId.value = null;
};

const handleDone = async (id) => {
  if (!isOwner.value) return;
  try {
    const todo = todoStore.dailyTodos.find((t) => t.id === id);
    const updatedTodo = {
      id: todo.id,
      userId: todo.userId,
      categoryId: todo.categoryId,
      isDone: (todo.isDone + 1) % 2,
      content: todo.content,
      date: todo.date,
      todoOrder: todo.todoOrder,
    };
    await todoStore.fetchTodoUpdate(updatedTodo);
    await activityStore.fetchUpdateDailyActivity(todo.date, todo.userId);
    emit("doneTodoCountUpdate", todo.userId);
  } catch (error) {
    console.error("할 일 수정 실패:", error);
  }
};

const handleContent = async (id) => {
  if (!isOwner.value) return;
  const todo = todoStore.dailyTodos.find((t) => t.id === id);
  if (todo && todo.content.trim()) {
    await categoryStore.fetchCategory(todo.categoryId);
    contentUpdateMode.value = true;
    newTodoContent.value = todo.content;
  }
};

const handleMoveTomorrow = async (id) => {
  if (!isOwner.value) return;
  try {
    const todo = todoStore.dailyTodos.find((t) => t.id === id);
    const oldDate = todo.date;
    const tomorrow = new Date(todo.date);
    tomorrow.setDate(tomorrow.getDate() + 1);
    const newDate = tomorrow.toISOString().split("T")[0];

    await todoStore.fetchTodoUpdate({
      ...todo,
      date: newDate,
    });
    await Promise.all([
      todoStore.fetchTodos(oldDate),
      todoStore.fetchTodos(newDate),
      activityStore.fetchUpdateDailyActivity(oldDate, todo.userId),
      activityStore.fetchUpdateDailyActivity(newDate, todo.userId),
    ]);
    dateStore.setSelectedDate(newDate);
    await todoStore.fetchMonthlyTodos(
      tomorrow.getFullYear(),
      tomorrow.getMonth() + 1,
      props.userId
    );
    closeMenu();
  } catch (error) {
    console.error("할 일 내일로 이동 실패:", error);
  }
};

const handleDragStart = (event) => {
  if (!isOwner.value) return;
  isDragging.value = true;
  const draggedTodo = event.item.__draggable_context.element;
  dragStore.setDragStart(draggedTodo.id, props.categoryId, event.oldIndex);
  props.onDragstart?.(event);
};

const handleDragEnd = async (event) => {
  if (!isOwner.value) return;
  if (!isDragging.value || !dragStore.dragState.todoId) return;

  try {
    const draggedTodo = todoStore.dailyTodos.find(
      (t) => t.id === dragStore.dragState.todoId
    );
    if (!draggedTodo) return;

    if (dragStore.dragState.endIndex === null) {
      return;
    }

    const finalTodos = [...todoStore.dailyTodos];

    if (dragStore.dragState.startCategoryId === dragStore.dragState.endCategoryId) {
      const categoryTodos = finalTodos
        .filter(t => t.categoryId === dragStore.dragState.startCategoryId)
        .sort((a, b) => (a.todoOrder || 0) - (b.todoOrder || 0));

      const startIndex = categoryTodos.findIndex(t => t.id === draggedTodo.id);
      const endIndex = dragStore.dragState.endIndex;

      if (startIndex < endIndex) {
        finalTodos.forEach(todo => {
          if (todo.id === draggedTodo.id) {
            todo.todoOrder = endIndex;
          } else if (todo.categoryId === dragStore.dragState.startCategoryId) {
            const todoIndex = categoryTodos.findIndex(t => t.id === todo.id);
            if (todoIndex > startIndex && todoIndex <= endIndex) {
              todo.todoOrder = todoIndex - 1;
            }
          }
        });
      } else if (startIndex > endIndex) {
        finalTodos.forEach(todo => {
          if (todo.id === draggedTodo.id) {
            todo.todoOrder = endIndex;
          } else if (todo.categoryId === dragStore.dragState.startCategoryId) {
            const todoIndex = categoryTodos.findIndex(t => t.id === todo.id);
            if (todoIndex >= endIndex && todoIndex < startIndex) {
              todo.todoOrder = todoIndex + 1;
            }
          }
        });
      }
    } else {
      draggedTodo.categoryId = dragStore.dragState.endCategoryId;
      draggedTodo.todoOrder = dragStore.dragState.endIndex;

      finalTodos.forEach(todo => {
        if (todo.categoryId === dragStore.dragState.endCategoryId && todo.id !== draggedTodo.id) {
          const todoIndex = finalTodos.filter(t => t.categoryId === dragStore.dragState.endCategoryId)
            .findIndex(t => t.id === todo.id);
          todo.todoOrder = todoIndex >= dragStore.dragState.endIndex ? todoIndex + 1 : todoIndex;
        }
      });
    }

    todoStore.updateLocalTodos(finalTodos);

    const updates = finalTodos
      .filter(t =>
        t.categoryId === dragStore.dragState.startCategoryId ||
        t.categoryId === dragStore.dragState.endCategoryId
      )
      .map(todo => todoStore.fetchTodoUpdate(todo));

    await Promise.all(updates);
    props.onDragend?.(event);
  } catch (error) {
    console.error("Todo 순서 업데이트 실패:", error);
    await todoStore.fetchTodos(dateStore.selectedDate, props.userId);
  } finally {
    dragStore.resetDragState();
    isDragging.value = false;
  }
};

const handleChange = (event) => {
  if (!isOwner.value) return;
  if (event.added || event.moved) {
    dragStore.setDragEnd(props.categoryId, event.added?.newIndex ?? event.moved.newIndex);
  }
};

onMounted(() => {
  const handleKeydown = (event) => {
    if (event.key === "Escape") {
      if (!contentUpdateMode.value) closeMenu();
      else goUpdate();
    }
    if (event.key === "Enter" && !isComposing.value) {
      goUpdate();
    }
  };

  const handleClick = (event) => {
    if (contentUpdateMode.value) {
      goUpdate();
    }
  };

  window.addEventListener("keydown", handleKeydown);
  window.addEventListener("click", handleClick);

  onBeforeUnmount(() => {
    window.removeEventListener("keydown", handleKeydown);
    window.removeEventListener("click", handleClick);
  });
});

watch(() => props.categoryId, async (newCategoryId) => {
  if (newCategoryId) {
    await todoStore.fetchTodos(dateStore.selectedDate, props.userId);
  }
});

watch(() => dateStore.selectedDate, async (newDate) => {
  if (newDate) {
    await todoStore.fetchTodos(newDate, props.userId);
  }
});
</script>

<style scoped>
[draggable="true"]:hover {
  background-color: rgba(0, 0, 0, 0.02);
}

.menu-enter-active,
.menu-leave-active {
  transition: opacity 0.3s ease;
}

.menu-enter-from,
.menu-leave-to {
  opacity: 0;
}
</style>
