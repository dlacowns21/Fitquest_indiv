<template>
  <div class="flex justify-center items-center py-5 gap-2">
    <button
      class="px-4 py-2 border border-gray-200 rounded bg-white disabled:bg-gray-50 disabled:text-gray-400 disabled:cursor-not-allowed transition-colors duration-200"
      :disabled="currentPage === 1" @click="changePage(currentPage - 1)">
      이전
    </button>
    <div class="flex gap-1">
      <button v-for="pageNum in totalPages" :key="pageNum" @click="changePage(pageNum)"
        class="w-10 h-10 flex items-center justify-center rounded transition-colors duration-200" :class="{
          'bg-black text-white': currentPage === pageNum,
          'hover:bg-gray-100': currentPage !== pageNum,
        }">
        {{ pageNum }}
      </button>
    </div>
    <button
      class="px-4 py-2 border border-gray-200 rounded bg-white disabled:bg-gray-50 disabled:text-gray-400 disabled:cursor-not-allowed transition-colors duration-200"
      :disabled="currentPage === totalPages" @click="changePage(currentPage + 1)">
      다음
    </button>
  </div>
</template>

<script setup>
const props = defineProps({
  currentPage: {
    type: Number,
    required: true,
  },
  totalPages: {
    type: Number,
    required: true,
  },
});

const emit = defineEmits(['page-change']);

const changePage = (page) => {
  if (page >= 1 && page <= props.totalPages) {
    emit('page-change', page);
  }
};
</script>

<style scoped>
.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>