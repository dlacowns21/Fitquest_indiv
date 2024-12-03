<template>
    <div class="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-[101]" @click="handleClose">
        <div class="bg-white/95 rounded-2xl w-full max-w-md mx-4 shadow-2xl transform transition-all duration-300 ease-out" @click.stop>
            <!-- 모달 헤더 -->
            <div class="flex items-center justify-between p-5 border-b border-gray-100">
                <h2 class="text-xl font-bold bg-gradient-to-r from-gray-900 to-gray-700 bg-clip-text text-transparent">팔로잉 목록</h2>
                <button @click="$emit('close')" class="text-gray-400 hover:text-gray-600 transition-colors">
                    <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                    </svg>
                </button>
            </div>

            <!-- 모달 본문 -->
            <div class="p-5 h-[500px] overflow-y-auto relative">
                <!-- 로딩 스피너 -->
                <div v-if="isLoading" class="absolute inset-0 flex items-center justify-center bg-white/80">
                    <div class="w-10 h-10 border-4 border-gray-200 border-t-gray-600 rounded-full animate-spin"></div>
                </div>

                <!-- 실제 컨텐츠 -->
                <div v-else>
                    <!-- 팔로잉이 없는 경우 -->
                    <div v-if="!props.followings.length" class="flex flex-col items-center justify-center py-12 text-gray-500">
                        <span class="material-icons text-4xl mb-3">sentiment_dissatisfied</span>
                        <p>아직 팔로잉하는 사용자가 없습니다</p>
                    </div>

                    <!-- 팔로잉 목록 -->
                    <div v-else class="space-y-3">
                        <div v-for="user in props.followings" :key="user.id"
                            class="flex items-center justify-between p-3 hover:bg-gray-50/80 rounded-xl transition-all duration-200">
                            <!-- 사용자 정보 -->
                            <button @click="goToUserHome(user.id)" class="flex items-center gap-4 group text-left">
                                <img :src="getUserProfileImage(user)" :alt="user.name"
                                    class="w-12 h-12 rounded-full object-cover ring-2 ring-gray-100 group-hover:ring-gray-300 transition-all duration-200" 
                                    @error="handleImageError">
                                <div>
                                    <div class="font-semibold group-hover:text-gray-900 transition-colors">{{ user.name }}</div>
                                    <div class="text-sm text-gray-500 line-clamp-1">{{ user.description || "자기소개가 없습니다." }}</div>
                                </div>
                            </button>

                            <!-- 팔로우/언팔로우 버튼 -->
                            <div v-if="authStore.user.id !== user.id">
                                <button v-if="followStatus[user.id]" @click="handleUnfollow(user.id)"
                                    class="px-4 py-1.5 bg-gray-100 hover:bg-gray-200 text-gray-700 rounded-full text-sm font-medium transition-all duration-200">
                                    언팔로우
                                </button>
                                <button v-else @click="handleFollow(user.id)"
                                    class="px-4 py-1.5 bg-gradient-to-r from-gray-900 to-gray-700 hover:from-gray-800 hover:to-gray-600 text-white rounded-full text-sm font-medium transition-all duration-200">
                                    팔로우
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <NeedLoginAlert v-if="needLoginAlert" @close="needLoginAlert = false" />
</template>

<script setup>
import { ref, onMounted, watch, onBeforeUnmount } from 'vue';
import http from '@/api/http';
import { useFollowStore } from '@/stores/follow';
import { useAuthStore } from '@/stores/auth';
import NeedLoginAlert from '@/components/alert/NeedLoginAlert.vue';
import { useRouter } from 'vue-router';

const emit = defineEmits(['close', 'updateFollowList']);
const props = defineProps({
    userId: {
        type: Number,
        required: true
    },
    followings: {
        type: Array,
        required: true
    }
});

const followStore = useFollowStore();
const authStore = useAuthStore();
const isLoading = ref(true);
const followStatus = ref({});
const router = useRouter();
const needLoginAlert = ref(false);

const initFollowStatus = async () => {
    isLoading.value = true;
    try {
        for (const user of props.followings) {
            followStatus.value[user.id] = await followStore.fetchIsFollowing(user.id);
        }
    } finally {
        isLoading.value = false;
    }
};

const goToUserHome = (userId) => {
    emit('close');
    if (Number(userId) === Number(authStore.user.id)) {
        router.push({ path: "/home" });
    } else {
        router.push({ path: `/home/${userId}` });
    }
};

const handleFollow = async (userId) => {
    const isAuth = await authStore.checkAuth();
    if (!isAuth) {
        needLoginAlert.value = true;
        return;
    }
    await followStore.fetchFollow(userId);
    followStatus.value[userId] = true;
    emit('updateFollowList');
};

const handleUnfollow = async (userId) => {
    const isAuth = await authStore.checkAuth();
    if (!isAuth) {
        needLoginAlert.value = true;
        return;
    }
    await followStore.fetchUnfollow(userId);
    followStatus.value[userId] = false;
    emit('updateFollowList');
};

const handleKeydown = (e) => {
    if (e.key === 'Escape') {
        emit('close');
    }
};

const handleClose = () => {
    emit('close');
};

onMounted(() => {
    initFollowStatus();
    window.addEventListener('keydown', handleKeydown);
});

onBeforeUnmount(() => {
    window.removeEventListener('keydown', handleKeydown);
});

watch(() => props.followings, () => {
    initFollowStatus();
}, { deep: true });

const getUserProfileImage = (user) => {
    if (user.profileImage) {
        return `${http.defaults.baseURL}/user${user.profileImage}`;
    }
    return "/default-profile.png";
};

const handleImageError = (e) => {
    e.target.src = '/default-profile.png';
};
</script>

<style scoped>
.overflow-y-auto {
    scrollbar-width: thin;
    scrollbar-color: #E2E8F0 #F7FAFC;
}

.overflow-y-auto::-webkit-scrollbar {
    width: 4px;
}

.overflow-y-auto::-webkit-scrollbar-track {
    background: #F7FAFC;
    border-radius: 2px;
}

.overflow-y-auto::-webkit-scrollbar-thumb {
    background-color: #E2E8F0;
    border-radius: 2px;
}

.overflow-y-auto::-webkit-scrollbar-thumb:hover {
    background-color: #CBD5E0;
}
</style>