<template>
  <Transition name="bounce">
    <div v-if="isOpen" class="fixed inset-0 z-40" @click="handleOutsideClick">
      <!-- 배경 오버레이 -->
      <div class="absolute inset-0  transition-opacity" @click="$emit('close')"></div>

      <!-- 챗봇 다이얼로그 -->
      <div class="fixed bottom-24 right-4 w-80 z-50" @click.stop>
        <div class="bg-white rounded-lg shadow-lg transform transition-transform duration-200">
          <!-- 헤더 -->
          <div class="flex justify-between items-center p-4">
            <h3 class="font-medium">AI 트레이너</h3>
            <button @click="$emit('close')" class="text-gray-600 hover:text-gray-900">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>

          <!-- 채팅 내용 -->
          <div class="h-96 overflow-y-auto px-4 py-2">
            <div v-if="messages.length === 0" class="text-gray-400 text-center mt-4">
              대화를 시작해보세요
            </div>

            <div v-for="(msg, index) in messages" :key="index" class="mb-3">
              <div v-if="msg.type === 'user'" class="flex justify-end">
                <div class="bg-gray-100 rounded-lg p-2.5 max-w-[75%]">
                  {{ msg.content }}
                </div>
              </div>

              <div v-else class="flex justify-start">
                <div class="bg-gray-50 rounded-lg p-2.5 max-w-[75%]">
                  <div class="whitespace-pre-line">{{ msg.content }}</div>
                </div>
              </div>
            </div>

            <div v-if="loading" class="flex justify-center items-center py-2">
              <div class="animate-spin rounded-full h-5 w-5 border-2 border-gray-300 border-t-gray-600"></div>
            </div>
          </div>

          <!-- 입력 폼 -->
          <div class="p-4">
            <button @click="getWorkoutRecommendation"
              class="w-full bg-black text-white px-4 py-2 rounded-lg hover:bg-gray-800 disabled:opacity-50 text-sm"
              :disabled="loading">
              운동 추천받기
            </button>
          </div>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { chatbotApi } from '@/api/chatbot';
import { useAuthStore } from '@/stores/auth';

const props = defineProps({
  isOpen: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['close']);

const authStore = useAuthStore();
const message = ref('');
const messages = ref([]);
const loading = ref(false);

// 일반 메시지 전송
const sendMessage = async () => {
  if (!message.value.trim() || loading.value) return;

  try {
    loading.value = true;
    messages.value.push({
      type: 'user',
      content: message.value
    });

    const response = await chatbotApi.generateResponse(message.value);

    messages.value.push({
      type: 'bot',
      content: response.generation
    });

    message.value = '';
  } catch (error) {
    messages.value.push({
      type: 'bot',
      content: '죄송합니다. 오류가 발생했습니다.'
    });
  } finally {
    loading.value = false;
  }
};

// 운동 추천 받기
const getWorkoutRecommendation = async () => {
  if (loading.value) return;

  try {
    loading.value = true;
    const userId = authStore.user.id;

    const response = await chatbotApi.getWorkoutRecommendation(userId);

    messages.value.push({
      type: 'bot',
      content: response.recommendation
    });
  } catch (error) {
    console.error('운동 추천 에러:', error);
    messages.value.push({
      type: 'bot',
      content: '운동 추천을 가져오는데 실패했습니다.'
    });
  } finally {
    loading.value = false;
  }
};

// ESC 키 이벤트 핸들러
const handleKeydown = (e) => {
  if (e.key === 'Escape' && props.isOpen) {
    emit('close');
  }
};

// 외부 클릭 핸들러
const handleOutsideClick = (e) => {
  if (e.target === e.currentTarget) {
    emit('close');
  }
};

onMounted(() => {
  window.addEventListener('keydown', handleKeydown);
});

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown);
});
</script>

<style scoped>
.bounce-enter-active {
  animation: bounce-in 0.5s cubic-bezier(0.68, -0.55, 0.265, 1.55);
}

.bounce-leave-active {
  animation: bounce-in 0.5s cubic-bezier(0.68, -0.55, 0.265, 1.55) reverse;
}

@keyframes bounce-in {
  0% {
    opacity: 0;
    transform: scale(0.3) translateY(100px);
  }

  50% {
    opacity: 0.8;
    transform: scale(1.1) translateY(-10px);
  }

  70% {
    opacity: 0.9;
    transform: scale(0.95) translateY(5px);
  }

  100% {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

/* 배경 오버레이 페이드 효과 */
.bounce-enter-active .bg-black\/20,
.bounce-leave-active .bg-black\/20 {
  transition: opacity 0.3s ease;
}

.bounce-enter-from .bg-black\/20,
.bounce-leave-to .bg-black\/20 {
  opacity: 0;
}

.bounce-enter-to .bg-black\/20,
.bounce-leave-from .bg-black\/20 {
  opacity: 1;
}
</style>