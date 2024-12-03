<template>
  <div class="bg-white rounded-lg shadow-sm my-5 w-full max-w-7xl mx-auto">
    <div class="w-full">
      <!-- 게시판 헤더 -->
      <div
        class="hidden md:grid md:grid-cols-[80px_2fr_300px] md:px-4 md:py-3 gap-4 bg-gray-50 border-b-2 border-gray-200 font-semibold text-gray-700">
        <div class="text-center">태그</div>
        <div class="text-center px-2">제목</div>
        <div class="grid grid-cols-[2fr_2fr_1fr_1fr] text-center gap-2">
          <span>작성자</span>
          <span>작성일</span>
          <span>조회수</span>
          <span>좋아요</span>
        </div>
      </div>

      <!-- 게시글 목록 -->
      <div v-for="board in boards" :key="board.id"
        class="grid md:grid-cols-[80px_2fr_300px] grid-cols-1 gap-4 px-4 py-3 border-b border-gray-200 transition-colors duration-200 cursor-pointer items-center hover:bg-gray-50/50"
        :class="{
          'bg-yellow-50/50 hover:bg-yellow-100/50': board.tag === '공지',
        }" @click="openBoard(board.id)">
        <!-- 태그 -->
        <div class="text-sm font-medium text-center" :class="{
          'text-blue-600': board.tag !== '공지',
          'text-red-600': board.tag === '공지',
        }">
          <div v-if="board.tag === '공지'" class="flex items-center justify-center gap-0.5">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-3.5 w-3.5" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd"
                d="M18 3a1 1 0 00-1.447-.894L8.763 6H5a3 3 0 000 6h.28l1.771 5.316A1 1 0 008 18h1a1 1 0 001-1v-4.382l6.553 3.276A1 1 0 0018 15V3z"
                clip-rule="evenodd" />
            </svg>
            {{ board.tag }}
          </div>
          <div v-else>#{{ board.tag }}</div>
        </div>

        <!-- 제목 -->
        <div class="text-center">
          <span :class="{ 'font-semibold': board.tag === '공지' }" class="text-sm">
            {{ board.title }}
          </span>
          <span v-if="board.commentCount > 0" class="text-gray-500 text-xs ml-1">
            [{{ board.commentCount }}]
          </span>
        </div>

        <!-- 메타 정보 -->
        <div class="grid grid-cols-[2fr_2fr_1fr_1fr] items-center text-center text-xs text-gray-700 gap-2">
          <span class="font-medium break-words">{{ board.writer }}</span>
          <span class="text-gray-500">{{ formatDate(board.date) }}</span>
          <span class="flex items-center justify-center gap-0.5">
            <i class="fas fa-eye text-gray-400 text-[10px]"></i>
            {{ board.viewCount }}
          </span>
          <span class="flex items-center justify-center gap-0.5">
            <i class="fas fa-heart text-gray-400 text-[10px]"></i>
            {{ board.hitCount || 0 }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter, useRoute } from "vue-router";

const router = useRouter();
const route = useRoute();

const props = defineProps({
  boards: {
    type: Array,
    required: true,
  },
});

const formatDate = (date) => {
  if (!date) return "";
  const dateObj = new Date(date);
  const year = dateObj.getFullYear();
  const month = String(dateObj.getMonth() + 1).padStart(2, "0");
  const day = String(dateObj.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
};

// openBoard 함수 수정
const openBoard = (id) => {
  // 현재 route에서 page와 tag 정보를 가져옴
  const currentPage = route.query.page || "1";
  const currentTag = route.query.tag;

  // query 객체 생성
  const query = { page: currentPage };
  if (currentTag) {
    query.tag = currentTag;
  }

  // 상세 페이지로 이동하면서 현재 페이지와 태그 정보 전달
  router.push({
    path: `/community/detail/${id}`,
    query,
  });
};

const truncateContent = (content) => {
  if (!content) return "";
  return content.length > 50 ? content.slice(0, 50) + "..." : content;
};
</script>
