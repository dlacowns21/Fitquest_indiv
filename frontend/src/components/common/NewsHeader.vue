<template>
    <div>
        <div class="flex justify-between items-center h-[50px] px-4 mt-1">
            <button @click="router.go(-1)" class="flex items-center">
                <span class="material-icons text-4xl">arrow_back</span>
            </button>
            <h1 class="w-[700px] text-center relative">
                <div class="relative flex items-center">
                    <input type="text" v-model="searchQuery" @keyup.enter="handleSearch"
                        class="w-full h-[40px] pl-4 pr-12 border border-gray-300 rounded-full shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition duration-200"
                        placeholder="검색어를 입력하세요..." />
                    <button @click="handleSearch"
                        class="absolute right-4 text-gray-400 hover:text-gray-600 transition-colors duration-200">
                        <i class="fas fa-search" />
                    </button>
                </div>
            </h1>
            <div class="relative mt-1.5 dropdown-container">
                <button @click="isDropdownOpen = !isDropdownOpen">
                    <span class="material-icons text-4xl">menu</span>
                </button>
                <Transition name="dropdown">
                    <div v-if="isDropdownOpen"
                        class="absolute right-0 mt-2 w-[155px] font-semibold bg-white rounded-[20px] shadow-[0_4px_20px_rgba(0,0,0,0.15)] py-2 origin-top-right">
                        <button @click="showRecentPosts"
                            class="rounded-xl block px-4 py-2 text-sm text-gray-700 w-full text-left transition-colors duration-150 hover:bg-gray-50 hover:text-gray-900 active:bg-gray-100">
                            최근 본 게시물
                        </button>
                        <div class="h-[1px] bg-gray-100"></div>
                        <button @click="showRecentNews"
                            class="rounded-xl block px-4 py-2 text-sm text-gray-700 w-full text-left transition-colors duration-150 hover:bg-gray-50 hover:text-gray-900 active:bg-gray-100">
                            최근 본 카드뉴스
                        </button>
                        <div class="h-[1px] bg-gray-100"></div>
                        <button @click="showRecentVideo"
                            class="rounded-xl block px-4 py-2 text-sm text-gray-700 w-full text-left transition-colors duration-150 hover:bg-gray-50 hover:text-gray-900 active:bg-gray-100">
                            최근 본 영상
                        </button>
                        <div class="h-[1px] bg-gray-100"></div>
                        <button v-if="isLogin" @click="authStore.logout()"
                            class="rounded-xl block w-full text-left px-4 py-2 text-sm text-gray-700 transition-colors duration-150 hover:bg-gray-50 hover:text-gray-900 active:bg-gray-100">
                            로그아웃
                        </button>
                        <button v-else @click="router.push('/login')"
                            class="rounded-xl block w-full text-left px-4 py-2 text-sm text-gray-700 transition-colors duration-150 hover:bg-gray-50 hover:text-gray-900 active:bg-gray-100">
                            로그인
                        </button>
                    </div>
                </Transition>
            </div>
        </div>
        <!-- 최근 본 게시물 모달 -->
        <RecentPostsModal :is-open="isRecentPostsModalOpen" @close="closeRecentPostsModal" />
        <RecentNewsModal :is-open="isRecentNewsModalOpen" @close="closeRecentNewsModal" />
        <RecentVideoModal :is-open="isRecentVideoModalOpen" @close="closeRecentVideoModal" />
    </div>
</template>

<script setup>
import { useRouter } from "vue-router";
import { computed, onMounted, onUnmounted, ref } from "vue";
import { useAuthStore } from "@/stores/auth";
import RecentPostsModal from "@/components/recent/RecentBoard.vue";
import RecentNewsModal from "@/components/recent/RecentNews.vue";
import RecentVideoModal from "@/components/recent/RecentVideo.vue";

const router = useRouter();
const authStore = useAuthStore();
const isDropdownOpen = ref(false);
const isRecentPostsModalOpen = ref(false);
const isRecentNewsModalOpen = ref(false);
const isRecentVideoModalOpen = ref(false);
const searchQuery = ref("");
const isLogin = computed(() => authStore.user.isAuthenticated);

// 최근 본 게시물 모달 표시
const showRecentPosts = () => {
    isRecentPostsModalOpen.value = true;
    isDropdownOpen.value = false;
};

// 모달 닫기
const closeRecentPostsModal = () => {
    isRecentPostsModalOpen.value = false;
};

const showRecentNews = () => {
    isRecentNewsModalOpen.value = true;
    isDropdownOpen.value = false;
};

const showRecentVideo = () => {
    isRecentVideoModalOpen.value = true;
    isDropdownOpen.value = false;
};

const handleSearch = () => {
    if (!searchQuery.value.trim()) return; // 빈 검색어 체크
    emit("search", searchQuery.value.trim());
};

const closeRecentNewsModal = () => {
    isRecentNewsModalOpen.value = false;
};

const closeRecentVideoModal = () => {
    isRecentVideoModalOpen.value = false;
};

// 드롭다운 외부 클릭 감지를 위한 이벤트 리스너
const handleClickOutside = (event) => {
    const dropdown = document.querySelector(".dropdown-container");
    if (dropdown && !dropdown.contains(event.target)) {
        isDropdownOpen.value = false;
    }
};

const emit = defineEmits(["search"]);

// 컴포넌트가 마운트될 때 이벤트 리스너 추가
onMounted(() => {
    document.addEventListener("click", handleClickOutside);
});

// 컴포넌트가 언마운트될 때 이벤트 리스너 제거
onUnmounted(() => {
    document.removeEventListener("click", handleClickOutside);
});
</script>

<style scoped>
.dropdown-enter-active {
    animation: bounce-in 0.5s;
}

.dropdown-leave-active {
    animation: bounce-in 0.5s reverse;
}

@keyframes bounce-in {
    0% {
        transform: scale(0.3);
        opacity: 0;
    }

    50% {
        transform: scale(1.05);
        opacity: 0.5;
    }

    100% {
        transform: scale(1);
        opacity: 1;
    }
}
</style>