<template>
  <div v-if="isLoading" class="fixed inset-0 z-[9999] bg-white">
    <Transition
      enter-active-class="transition-opacity duration-300"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-active-class="transition-opacity duration-300"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <div v-if="isLoading" class="absolute inset-0 flex items-center justify-center">
        <div class="relative">
          <!-- 스피너 애니메이션 -->
          <div class="w-16 h-16">
            <div 
              class="absolute w-16 h-16 rounded-full border-4 border-solid border-blue-500 border-t-transparent animate-spin">
            </div>
            <div 
              class="absolute w-16 h-16 rounded-full border-4 border-solid border-blue-300 border-t-transparent animate-ping">
            </div>
          </div>
          <!-- 로딩 텍스트 -->
          <div class="mt-4 text-center text-gray-600 font-medium">
            Loading...
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { storeToRefs } from 'pinia';
import { useLoadingStore } from '@/stores/loading';

const { isLoading } = storeToRefs(useLoadingStore());
</script>

<style scoped>
.fixed {
  overflow: hidden;
}

/* 트랜지션 애니메이션 부드럽게 처리 */
.transition-opacity {
  transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
}
</style> 