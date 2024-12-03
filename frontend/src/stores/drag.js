import { defineStore } from "pinia";
import { ref } from "vue";

export const useDragStore = defineStore("drag", () => {
  const dragState = ref({
    todoId: null,
    startCategoryId: null,
    endCategoryId: null,
    startIndex: null,
    endIndex: null
  });

  const setDragStart = (todoId, categoryId, index) => {
    dragState.value = {
      todoId,
      startCategoryId: categoryId,
      startIndex: index,
      endCategoryId: null,
      endIndex: null
    };
  };

  const setDragEnd = (categoryId, index) => {
    dragState.value.endCategoryId = categoryId;
    dragState.value.endIndex = index;
  };

  const resetDragState = () => {
    dragState.value = {
      todoId: null,
      startCategoryId: null,
      endCategoryId: null,
      startIndex: null,
      endIndex: null
    };
  };

  return {
    dragState,
    setDragStart,
    setDragEnd,
    resetDragState
  };
});