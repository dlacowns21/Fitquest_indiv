<template>
  <Transition name="modal">
    <div class="fixed inset-0 z-[100]" @click="$emit('closeModal')">
      <!-- 모달 배경 -->
      <div class="absolute inset-0 bg-black/60 backdrop-blur-md"></div>

      <!-- 모달 컨텐츠 -->
      <div class="relative h-full flex items-center justify-center p-4">
        <div
          class="bg-white/95 rounded-3xl shadow-[0_20px_60px_-15px_rgba(0,0,0,0.3)] w-[400px] max-w-2xl backdrop-blur-sm"
          @click.stop>
          <!-- 검색 영역 -->
          <div class="p-6">
            <div class="relative">
              <input v-model="searchQuery" type="text" placeholder="이름으로 검색하세요"
                class="w-full px-5 py-3 pr-12 border-0 rounded-xl bg-gray-50/80 focus:outline-none focus:ring-2 focus:ring-gray-900/20 focus:bg-white/90 transition-all duration-200"
                @keyup.enter="handleSearch" />
              <button
                class="absolute right-4 top-1/2 transform -translate-y-1/2 hover:scale-110 transition-transform duration-200"
                @click="handleSearch">
                <MagnifyingGlassIcon class="w-5 h-5 text-gray-500" />
              </button>
            </div>
          </div>
          <!-- 유저 리스트 -->
          <div class="px-6 pb-6 h-[500px] overflow-y-auto relative">
            <!-- 로딩 스피너 -->
            <div v-if="loading || searchLoading" class="absolute inset-0 flex items-center justify-center bg-white/90">
              <div class="w-12 h-12 border-4 border-gray-100 border-t-gray-900 rounded-full animate-spin"></div>
            </div>

            <!-- 실제 컨텐츠 -->
            <div v-else class="h-full pb-10">
              <div v-if="users.length === 0" class="h-full flex items-center justify-center text-gray-500">
                <div v-html="message" class="text-center"></div>
              </div>
              <div v-else class="grid grid-cols-1 gap-3">
                <div v-for="user in users" :key="user.id"
                  class="flex items-center p-4 rounded-xl hover:bg-gray-50/80 transition-all duration-200 cursor-pointer group"
                  @click="goToProfile(user.id)">
                  <!-- 프로필 이미지 -->
                  <div
                    class="w-14 h-14 rounded-full overflow-hidden mr-4 ring-2 ring-gray-100 group-hover:ring-gray-300 transition-all duration-200">
                    <img :src="getUserProfileImage(user.profileImage)" :alt="user.name" @error="handleImageError"
                      class="w-full h-full object-cover transform group-hover:scale-105 transition-transform duration-300" />
                  </div>

                  <!-- 유저 정보 -->
                  <div class="flex-1">
                    <h3 class="font-semibold text-gray-800 group-hover:text-gray-900 transition-colors">
                      {{ user.name }}
                    </h3>
                    <p class="text-sm text-gray-500 line-clamp-1">
                      {{ user.description || "자기소개가 없습니다." }}
                    </p>
                  </div>

                  <!-- 팔로우/언팔로우 버튼 -->
                  <div v-if="authStore.user.id !== user.id">
                    <button v-if="followStatus[user.id]" @click.stop="handleUnfollow(user.id)"
                      class="px-4 py-1.5 bg-gray-100 hover:bg-gray-200 text-gray-700 rounded-full text-sm font-medium transition-all duration-200">
                      언팔로우
                    </button>
                    <button v-else @click.stop="handleFollow(user.id)"
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
    </div>
  </Transition>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from "vue";
import { useRouter } from "vue-router";
import { MagnifyingGlassIcon } from "@heroicons/vue/24/outline";
import http from "@/api/http";
import { debounce } from "lodash";
import { getChoseong, disassemble } from "es-hangul";
import { useAuthStore } from "@/stores/auth";
import { useFollowStore } from "@/stores/follow";

const followStore = useFollowStore();
const router = useRouter();
const emit = defineEmits(["closeModal", "updateFollowList"]);
const searchQuery = ref("");
const users = ref([]);
const loading = ref(false);
const authStore = useAuthStore();
const message = ref(
  '<div class="material-icons text-5xl mb-3 text-gray-400">group_add</div><div>운동 메이트를 만나보세요!</div>'
);
const followStatus = ref({});
const searchLoading = ref(false);

const initFollowStatus = async () => {
  loading.value = true;
  try {
    for (const user of users.value) {
      followStatus.value[user.id] = await followStore.fetchIsFollowing(user.id);
    }
  } finally {
    loading.value = false;
  }
};

const handleFollow = async (userId) => {
  try {
    if (!authStore.checkAuth()) {
      needLoginAlert.value = true;
      return;
    }
    await followStore.fetchFollow(userId);
    followStatus.value[userId] = true;
    followStore.setNeedsRefresh(true);
  } catch (error) {
    console.error('Follow failed:', error);
  }
};

const handleUnfollow = async (userId) => {
  try {
    if (!authStore.checkAuth()) {
      needLoginAlert.value = true;
      return;
    }
    await followStore.fetchUnfollow(userId);
    followStatus.value[userId] = false;
    followStore.setNeedsRefresh(true);
  } catch (error) {
    console.error('Unfollow failed:', error);
  }
};

// 프로필 이미지 URL 생성 함수
const getUserProfileImage = (profileImage) => {
  if (profileImage) {
    return `${http.defaults.baseURL}/user${profileImage}`;
  }
  return "/default-profile.png";
};

// 이미지 로드 실패시 기본 이미지로 대체
const handleImageError = (e) => {
  e.target.src = "/default-profile.png";
};

// 디바운스된 검색 함수
const handleSearch = debounce(async () => {
  if (!searchQuery.value.trim()) {
    users.value = [];
    return;
  }
  const isChoseong = getChoseong(searchQuery.value) === searchQuery.value;
  let tempSearchQuery = searchQuery.value;
  if (isChoseong) {
    tempSearchQuery = disassemble(searchQuery.value);
  }
  try {
    searchLoading.value = true;
    const response = await http.post(`/user/search`, {
      word: tempSearchQuery,
      isChoseong: isChoseong,
    });

    // 자신을 제외한 사용자 필터링
    const filteredUsers = response.data.filter(
      (user) => user.id !== authStore.user.id
    );

    // 필터링된 결과가 있는 경우에만 users 업데이트
    if (filteredUsers.length > 0) {
      users.value = filteredUsers;
      message.value = "";
      await initFollowStatus();
    } else {
      users.value = [];
      message.value =
        '<div class="material-icons text-5xl mb-3 text-gray-400">sentiment_dissatisfied</div><div>검색 결과가 없습니다.</div>';
    }
  } catch (error) {
    message.value =
      '<div class="material-icons text-5xl mb-3 text-gray-400">sentiment_dissatisfied</div><div>검색에 실패했습니다.</div>';
  } finally {
    searchLoading.value = false;
  }
}, 300);

const goToProfile = (userId) => {
  emit("closeModal");
  router.push(`/home/${userId}`);
};

onMounted(() => {
  window.addEventListener("keyup", (e) => {
    if (e.key === "Escape") {
      emit("closeModal");
    }
  });
});

onUnmounted(() => {
  window.removeEventListener("keyup", (e) => {
    if (e.key === "Escape") {
      emit("closeModal");
    }
  });
});
</script>
