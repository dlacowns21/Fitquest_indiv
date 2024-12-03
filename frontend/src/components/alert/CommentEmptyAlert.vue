<template>
  <Transition name="modal">
    <div class="fixed inset-0 z-[150]" @click="$emit('close')">
      <div class="absolute inset-0 bg-black/50 backdrop-blur-sm"></div>
      <div class="relative h-full flex items-center justify-center p-4">
        <div class="bg-white rounded-2xl shadow-2xl w-[300px] p-6" @click.stop>
          <h3 class="text-lg font-semibold mb-4">댓글 작성 실패</h3>
          <p class="text-gray-600 mb-6">댓글 내용을 입력해주세요.</p>
          <div class="flex justify-center">
            <button
              @click="$emit('close')"
              class="px-4 py-2 w-full bg-black text-white rounded-lg hover:bg-gray-800 transition-colors"
            >
              확인
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

const emit = defineEmits(["close"]);

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
