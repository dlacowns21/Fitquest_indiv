<template>
  <div class="max-w-4xl mx-auto px-4 pb-16">
    <CommunitySearch v-model="searchText" @search="handleSearch" />
    <CommunityTag :tags="tags" v-model="selectedTags" />
    <div class="flex justify-end gap-3 my-2">
      <button
        class="px-3 py-1.5 rounded-md font-medium text-sm bg-gray-100 text-gray-700 hover:bg-gray-200 transition-all duration-200"
        @click="viewAllPosts">
        전체 보기
      </button>
      <button
        class="px-3 py-1.5 rounded-md font-medium text-sm bg-black text-white hover:bg-gray-700 transition-all duration-200"
        @click="goToWrite">
        글 쓰기
      </button>
    </div>
    <CommunityBoard :boards="paginatedBoards" />
    <CommunityPagenation :currentPage="currentPage" :totalPages="totalPages" @page-change="handlePageChange" />
  </div>
  <NeedLoginAlert @close="needLoginAlert = false" v-if="needLoginAlert" />
</template>

<script setup>
import { ref, computed, onMounted, watch } from "vue";
import { useRouter, useRoute } from "vue-router";
import { useBoardStore } from "@/stores/board";
import { COMMUNITY_TAGS } from "@/stores/tags";
import CommunitySearch from "@/components/community/CommunitySearch.vue";
import CommunityTag from "@/components/community/CommunityTag.vue";
import CommunityBoard from "@/components/community/CommunityBoard.vue";
import CommunityPagenation from "@/components/community/CommunityPagenation.vue";
import NeedLoginAlert from "@/components/alert/NeedLoginAlert.vue";
import { useAuthStore } from "@/stores/auth";

// 반응형 상태 정의
const router = useRouter();
const route = useRoute();
const boardStore = useBoardStore();
const authStore = useAuthStore();
const tags = ref([]);
const selectedTags = ref([]);
const searchText = ref("");
const currentPage = ref(1);
const itemsPerPage = ref(20);
const needLoginAlert = ref(false);

const fetchBoards = async (preserveSearch = false) => {
  try {
    if (!preserveSearch) {
      boardStore.lastSearchCondition = null;
    }
    await boardStore.fetchBoards({
      skipLoading: true,
      preserveSearch: preserveSearch
    });
    const dynamicTags = [
      ...new Set(boardStore.boards.map((board) => board.tag)),
    ];
    tags.value = [...new Set([...COMMUNITY_TAGS, ...dynamicTags])];

    if (!preserveSearch) {
      currentPage.value = 1;
      updateUrlQuery(1);
    } else {
      const pageFromQuery = Number(route.query.page) || 1;
      currentPage.value = pageFromQuery;

      const maxPage = Math.ceil(boardStore.boards.length / itemsPerPage.value);
      if (currentPage.value > maxPage) {
        currentPage.value = maxPage || 1;
        updateUrlQuery(currentPage.value);
      }
    }
  } catch (error) {
    console.error("Error fetching boards:", error);
  }
};

watch(
  () => route.query,
  (query) => {
    if (query.page) {
      currentPage.value = Number(query.page);
    }
    if (query.tags) {
      selectedTags.value = query.tags.split(',');
    }
  },
  { immediate: true, deep: true }
);

const filteredBoards = computed(() => {
  let result = [...boardStore.boards];

  // 공지사항과 일반 게시글 분리
  const notices = result.filter((board) => board.tag === "공지");
  const regularPosts = result.filter((board) => board.tag !== "공지");

  // ���각 ID 기준으로 내림차순 정렬
  notices.sort((a, b) => b.id - a.id);
  regularPosts.sort((a, b) => b.id - a.id);

  // 공지사항을 항상 위에 배치
  result = [...notices];

  // 선택된 태그가 있는 경우 필터링
  if (selectedTags.value.length > 0) {
    const filteredPosts = regularPosts.filter((board) => 
      selectedTags.value.includes(board.tag)
    );
    result = [...notices, ...filteredPosts];
  } else {
    result = [...notices, ...regularPosts];
  }

  return result;
});

const paginatedBoards = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value;
  const end = start + itemsPerPage.value;
  return filteredBoards.value.slice(start, end);
});

const totalPages = computed(() => {
  return Math.ceil(filteredBoards.value.length / itemsPerPage.value);
});

// 검색 처리 함수 수정
const handleSearch = async (searchCondition) => {
  try {
    await boardStore.searchBoards({
      ...searchCondition,
      tags: selectedTags.value, // 선택된 태그들 전달
      skipLoading: true
    });
    currentPage.value = 1;
    updateUrlQuery(1);
  } catch (error) {
    console.error("검색 중 오류 발생:", error);
  }
};

// 태그 선택 핸들러 수정
const handleTagSelect = async (tag) => {
  selectedTags.value = tag;
  currentPage.value = 1;

  // 검색어가 있는 경우, 태그와 함께 검색 수행
  if (searchText.value.trim()) {
    await handleSearch({
      key: 'all', // 또는 마지막으로 선택된 검색 타입
      word: searchText.value.trim(),
      skipLoading: true
    });
  }

  const query = { page: 1 };
  if (tag) {
    query.tag = tag;
  }
  router.push({ query }).catch(() => { });
};

const handlePageChange = (page) => {
  currentPage.value = page;
  updateUrlQuery(page);
};

// URL 쿼리 업데이트 함수 수정
const updateUrlQuery = (page) => {
  const query = { page };
  if (selectedTags.value.length > 0) {
    query.tags = selectedTags.value.join(',');
  }
  router.push({ query }).catch(() => {});
};

// URL 쿼리 감시 로직 강화
watch(
  () => route.query,
  async (query) => {
    if (query.page) {
      currentPage.value = Number(query.page);
      await fetchBoards(true);
    }
  },
  { immediate: true, deep: true }
);



// 전체 보기 메서드 수정
const viewAllPosts = () => {
  searchText.value = "";
  router.push({ query: { page: 1 } }).catch(() => {});
  fetchBoards(false);
};

// 글쓰기 페이지로 이동하는 메서드
const goToWrite = async () => {
  if (!(await authStore.checkAuth())) {
    needLoginAlert.value = true;
    return;
  }
  const currentPage = route.query.page || 1;
  router.push({
    path: "/community/write",
    query: { returnPage: currentPage }, // 현재 페이지 정보 저장
  });
};

// 컴포넌트 마운트 시 실행
onMounted(async () => {
  boardStore.lastSearchCondition = null;
  const pageFromQuery = Number(route.query.page) || 1;
  const tagFromQuery = route.query.tag;
  currentPage.value = pageFromQuery;
  if (tagFromQuery) {
    selectedTags.value = tagFromQuery;
  }
  await fetchBoards(true);
});

// 라우트 변경 감시 로직 수정
watch(
  () => route.path,
  async (newPath, oldPath) => {
    if (newPath === "/community" && oldPath !== "/community") {
      // 검색어와 검색 결과 초기화
      searchText.value = "";
      selectedTags.value = [];
      boardStore.lastSearchCondition = null;
      
      // URL의 태그 파라미터 확인
      const tagFromQuery = route.query.tag;
      if (tagFromQuery) {
        selectedTags.value = tagFromQuery;
      }

      // 페이지 정보 유지
      const pageFromQuery = Number(route.query.page) || 1;
      currentPage.value = pageFromQuery;

      // 전체 게시글 새로 불러오기
      await fetchBoards(false);
    }
  }
);
</script>
