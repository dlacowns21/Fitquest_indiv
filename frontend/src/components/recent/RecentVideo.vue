<template>
    <div v-if="isOpen">
        <!-- 모달 오버레이 -->
        <div class="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50"
            @click="handleOverlayClick">
            <!-- 모달 컨텐츠 -->
            <div class="bg-white rounded-2xl shadow-2xl w-11/12 max-w-2xl p-6" @click.stop>
                <!-- 헤더 -->
                <div class="flex justify-between items-center mb-6">
                    <h2 class="text-xl font-bold text-gray-800">최근 본 운동 영상</h2>
                    <button @click="close" class="text-gray-400 hover:text-gray-500 focus:outline-none">
                        <svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                d="M6 18L18 6M6 6l12 12" />
                        </svg>
                    </button>
                </div>

                <!-- 비디오 목록 -->
                <div class="mt-4 max-h-[60vh] overflow-y-auto">
                    <div v-if="recentVideos.length > 0" class="space-y-4">
                        <div v-for="video in recentVideos" :key="video.id"
                            class="flex gap-4 p-3 hover:bg-gray-50 rounded-xl transition-colors duration-150 cursor-pointer"
                            @click="navigateToVideo(video.id)">
                            <!-- 썸네일 -->
                            <div class="w-40 h-24 flex-shrink-0 overflow-hidden rounded-lg">
                                <img :src="video.thumbnail" :alt="video.title"
                                    class="w-full h-full object-cover transition-transform duration-300 hover:scale-105">
                            </div>
                            <!-- 비디오 정보 -->
                            <div class="flex flex-col flex-1">
                                <h3 class="font-medium text-gray-900 line-clamp-2 mb-1">{{ video.title }}</h3>
                                <div class="flex flex-col gap-1">
                                    <span class="text-sm text-gray-600">{{ video.channelTitle }}</span>
                                    <span class="text-sm text-gray-500">{{ formatDate(video.publishedAt) }}</span>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- 비디오가 없을 때 -->
                    <div v-else class="flex flex-col items-center justify-center py-12 text-gray-500">
                        <span class="material-icons text-4xl mb-2">videocam_off</span>
                        <p>최근 본 운동 영상이 없습니다.</p>
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

<script setup>
import { ref, onMounted, watch } from "vue";

const props = defineProps({
    isOpen: Boolean,
});

const emit = defineEmits(["close"]);
const recentVideos = ref([]);

const navigateToVideo = (videoId) => {
    close();
    window.open(`https://www.youtube.com/watch?v=${videoId}`, '_blank');
};

const loadRecentVideos = () => {
    const videos = JSON.parse(localStorage.getItem("recentVideos") || "[]");
    recentVideos.value = videos;
};

const close = () => {
    emit("close");
};

const formatDate = (dateStr) => {
    if (!dateStr) return "";
    const date = new Date(dateStr);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    return `${year}-${month}-${day}`;
};

const handleOverlayClick = (event) => {
    if (event.target === event.currentTarget) {
        close();
    }
};

onMounted(() => {
    loadRecentVideos();
    window.addEventListener("keydown", (event) => {
        if (event.key === "Escape") {
            close();
        }
    });
});

watch(
    () => props.isOpen,
    (newValue) => {
        if (newValue) {
            loadRecentVideos();
        }
    }
);
</script>

<style scoped>
/* 스크롤바 스타일링 */
.overflow-y-auto {
    scrollbar-width: thin;
    scrollbar-color: #CBD5E0 #EDF2F7;
}

.overflow-y-auto::-webkit-scrollbar {
    width: 6px;
}

.overflow-y-auto::-webkit-scrollbar-track {
    background: #EDF2F7;
    border-radius: 3px;
}

.overflow-y-auto::-webkit-scrollbar-thumb {
    background-color: #CBD5E0;
    border-radius: 3px;
}

.line-clamp-2 {
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
}
</style>