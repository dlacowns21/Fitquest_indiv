<template>
  <Transition name="modal">
    <div class="fixed inset-0 z-[150]" @click="$emit('close')">
      <div class="absolute inset-0 bg-black/50 backdrop-blur-sm"></div>
      <div class="relative h-full flex items-center justify-center p-4">
        <div class="bg-white rounded-2xl shadow-2xl w-[300px] p-6" @click.stop>
          <h3 class="text-lg font-semibold mb-4">댓글 삭제</h3>
          <p class="text-gray-600 mb-6">댓글을 삭제하시겠습니까?</p>
          <div class="flex justify-center gap-3">
            <button
              @click="$emit('close')"
              class="flex-1 px-4 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition-colors"
            >
              취소
            </button>
            <button
              @click="$emit('confirm')"
              class="flex-1 px-4 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 transition-colors"
            >
              삭제
            </button>
          </div>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { onMounted, onUnmounted } from "vue";

defineProps({
  show: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(["close", "confirm"]);

const handleEscape = (e) => {
  if (e.key === "Escape") {
    emit("close");
  }
};

onMounted(() => {
  window.addEventListener("keydown", handleEscape);
});

onUnmounted(() => {
  window.removeEventListener("keydown", handleEscape);
});
</script>

<style scoped>
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}
</style>
