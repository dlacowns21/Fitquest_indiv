<template>
  <div class="flex justify-center items-center h-[60px] sm:h-[70px]">
    <nav class="flex justify-between items-center w-full max-w-md px-4 sm:px-6">
      <RouterLink to="/" class="footer-link">
        <span class="material-icons text-2xl sm:text-3xl">home</span>
        <span class="hidden sm:block text-xs mt-1">홈</span>
      </RouterLink>
      
      <RouterLink to="/community" class="footer-link">
        <span class="material-icons text-2xl sm:text-3xl">groups</span>
        <span class="hidden sm:block text-xs mt-1">커뮤니티</span>
      </RouterLink>
      
      <button @click="openUserSearchModal" class="footer-link">
        <span class="material-icons text-2xl sm:text-3xl">search</span>
        <span class="hidden sm:block text-xs mt-1">검색</span>
      </button>
      
      <RouterLink to="/video" class="footer-link">
        <span class="material-icons text-2xl sm:text-3xl">video_library</span>
        <span class="hidden sm:block text-xs mt-1">비디오</span>
      </RouterLink>
      
      <RouterLink to="/news" class="footer-link">
        <span class="material-icons text-2xl sm:text-3xl">newspaper</span>
        <span class="hidden sm:block text-xs mt-1">뉴스</span>
      </RouterLink>
      
      <button @click="check" class="footer-link">
        <span class="material-icons text-2xl sm:text-3xl">person</span>
        <span class="hidden sm:block text-xs mt-1">프로필</span>
      </button>
    </nav>
  </div>
</template>

<script setup>
import { RouterLink } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { useRouter } from 'vue-router';

const emit = defineEmits(['openUserSearchModal', 'needLoginAlert']);
const authStore = useAuthStore();
const router = useRouter();

const check = () => {
  if (!authStore.user.isAuthenticated) {
    emit('needLoginAlert');
  } else {
    router.push('/config');
  }
};

const openUserSearchModal = () => {
  emit('openUserSearchModal');
};
</script>

<style scoped>
.footer-link {
  @apply flex flex-col items-center text-gray-700 hover:text-blue-600 transition-colors duration-200 px-2 sm:px-4;
}

/* 활성 링크 스타일 */
.router-link-active {
  @apply text-blue-600;
}

/* 터치 디바이스를 위한 최적화 */
@media (hover: none) {
  .footer-link:active {
    @apply text-blue-600;
  }
}
</style>