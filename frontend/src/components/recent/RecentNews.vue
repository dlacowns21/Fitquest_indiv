<template>
  <div v-if="isOpen">
    <div class="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50" @click="handleOverlayClick">
      <div class="bg-white rounded-2xl shadow-2xl w-11/12 max-w-2xl p-6" @click.stop>
        <!-- 헤더 -->
        <div class="flex justify-between items-center mb-6">
          <h2 class="text-xl font-bold text-gray-800">최근 본 카드뉴스</h2>
          <button @click="close" class="text-gray-400 hover:text-gray-500 focus:outline-none">
            <svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        <!-- 뉴스 목록 -->
        <div class="mt-4 max-h-[60vh] overflow-y-auto">
          <div v-if="recentNews.length > 0" class="space-y-3">
            <div v-for="news in recentNews" :key="news.id"
              class="group p-4 hover:bg-gray-50 rounded-xl transition-all duration-200 cursor-pointer"
              @click="navigateToNews(news.link)">
              <div class="flex items-start space-x-4">
                <div class="flex-shrink-0">
                  <span class="material-icons text-gray-400 group-hover:text-blue-500 transition-colors">feed</span>
                </div>
                <div class="flex-1 min-w-0">
                  <p class="text-sm font-medium text-gray-900 truncate group-hover:text-blue-600 transition-colors">
                    {{ news.title }}
                  </p>
                  <div class="mt-1 flex items-center text-xs text-gray-500">
                    <span>{{ news.postdate }}</span>
                  </div>
                </div>
                <div class="flex-shrink-0">
                  <span class="material-icons text-gray-400 group-hover:text-blue-500 transition-colors">open_in_new</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 뉴스가 없을 때 -->
          <div v-else class="flex flex-col items-center justify-center py-12 text-gray-500 text-center">
            <span class="material-icons text-4xl mb-2 w-[40px] block">feed_off</span>
            <p class="mt-2">최근 본 카드뉴스가 없습니다.</p>
          </div>
        </div>

        <!-- 하단 버튼 -->
        <div class="mt-6 flex justify-end">
          <button @click="close"
            class="px-6 py-2 bg-gray-900 text-white rounded-xl hover:bg-gray-800 transition-colors duration-150 focus:outline-none focus:ring-2 focus:ring-gray-500">
            닫기
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<!-- RecentBoard.vue -->
<script setup>
import { ref, onMounted, watch } from "vue"; // watch import 필요

const props = defineProps({
  isOpen: Boolean,
});

const emit = defineEmits(["close"]);
const recentNews = ref([]);

// 게시물 클릭 시 처리하는 함수
const navigateToNews = (link) => {
  close();
  window.open(link, "_blank");
};

const loadRecentNews = () => {
  const news = JSON.parse(localStorage.getItem("recentNews") || "[]");
  recentNews.value = news;
};

const close = () => {
  emit("close");
};

// const formatDate = (date) => {
//     if (!date) return "";
//     const dateObj = new Date(date);
//     return dateObj.toLocaleDateString("ko-KR", {
//         year: "numeric",
//         month: "2-digit",
//         day: "2-digit",
//         hour: "2-digit",
//         minute: "2-digit",
//     });
// };

// 오버레이 클릭 핸들러 추가
const handleOverlayClick = (event) => {
  // 오버레이를 직접 클릭했을 때만 모달 닫기
  if (event.target === event.currentTarget) {
    close();
  }
};

onMounted(() => {
  loadRecentNews();
  window.addEventListener("keydown", (event) => {
    if (event.key === "Escape") {
      close();
    }
  });
});

// isOpen prop이 변경될 때마다 데이터를 새로 로드
watch(
  () => props.isOpen,
  (newValue) => {
    if (newValue) {
      loadRecentNews();
    }
  }
);
</script>

<style scoped>
/* 스크롤바 스타일링 */
</style>
