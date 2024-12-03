<template>
  <div class="max-w-[900px] mx-auto px-2 flex flex-col gap-2" @keyup.esc="showRandomUsers = false">
    <!-- 메인 컨텐츠 영역 -->
    <div class="flex flex-col md:flex-row gap-2">
      <!-- 왼쪽 섹션: 프로필 + 캘린더 -->
      <div class="md:w-1/2 flex flex-col gap-2 h-[600px]">
        <!-- 통합된 프로필 섹션 -->
        <div class="h-[200px] p-2 bg-white rounded-2xl shadow-md">
          <div class="flex items-center justify-between w-full mb-1">
            <div class="flex items-center gap-6 mx-6">
              <!-- 프로필 이미지 -->
              <RouterLink to="/config" class="relative group">
                <img :src="profileImage"
                  class="w-20 h-20 rounded-full object-cover border-3 border-white shadow-md group-hover:scale-105 transition duration-300"
                  alt="프로필 이미지" @error="handleImageError" />
              </RouterLink>
              <!-- 사용자 정보 -->
              <div class="flex flex-col">
                <div class="flex items-center gap-4">
                  <div class="flex items-center gap-2 font-bold text-gray-800 text-lg mb-2">
                    {{ authStore.user.name }}
                    <button @click="showGuestbook = true"
                      class="flex items-center justify-center w-8 h-8 rounded-full hover:bg-gray-200 transition-colors">
                      <span class="material-icons text-gray-600 text-sm">chat</span>
                    </button>
                  </div>
                </div>
                <div class="text-gray-600 text-sm">
                  {{ authStore.user.description || "자기소개를 입력해주세요." }}
                </div>
              </div>
            </div>
          </div>
          <!-- 팔로워/팔로잉/완료 정보 -->
          <div class="flex justify-around pt-1">
            <div class="flex flex-col items-center px-4 py-2 hover:bg-gray-50 rounded-xl transition-all duration-200">
              <span class="font-bold text-lg text-gray-800">{{
                doneTodoCount
              }}</span>
              <span class="material-icons text-gray-600 text-md">check_circle</span>
              <span class="text-xs text-gray-500 font-medium">완료</span>
            </div>
            <button @click="showFollowers = true"
              class="flex flex-col items-center px-4 py-2 hover:bg-gray-50 rounded-xl transition-all duration-200">
              <span class="font-bold text-lg text-gray-800">{{
                followerList.length
              }}</span>
              <span class="material-icons text-gray-600 text-md">group</span>
              <span class="text-xs text-gray-500 font-medium">팔로워</span>
            </button>
            <button @click="showFollowings = true"
              class="flex flex-col items-center px-4 py-2 hover:bg-gray-50 rounded-xl transition-all duration-200">
              <span class="font-bold text-lg text-gray-800">{{
                followingList.length
              }}</span>
              <span class="material-icons text-gray-600 text-md">person_add</span>
              <span class="text-xs text-gray-500 font-medium">팔로잉</span>
            </button>
          </div>
        </div>

        <!-- 캘린더 섹션 -->
        <div class="h-[400px] rounded-[15px]">
          <Calendar @dateSelected="handleDateSelected" :userId="Number(authStore.user.id)" />
        </div>
      </div>

      <!-- 오른쪽 섹션: 카테고리 -->
      <div class="md:w-1/2">
        <div class="p-3 rounded-[15px] bg-white shadow-md">
          <CategoryHeader />
          <div class="h-[532px] overflow-y-auto">
            <CategoryList :selectedDate="selectedDate" :userId="Number(authStore.user.id)"
              @doneTodoCountUpdate="fetchDoneTodoCount" />
          </div>
        </div>
      </div>
    </div>

    <!-- 잔디 그래프 섹션 -->
    <div class="w-full bg-[#f7f8f9] p-2 rounded-[15px] mb-8">
      <GrassGraph :userId="Number(authStore.user.id)" />
    </div>

    <!-- 방명록 모달 -->
    <Guestbook :userId="Number(authStore.user.id)" v-if="showGuestbook" @close="showGuestbook = false" />
  </div>

  <Transition enter-active-class="transition-all duration-300 ease-out" enter-from-class="opacity-0"
    enter-to-class="opacity-100" leave-active-class="transition-all duration-200 ease-in" leave-from-class="opacity-100"
    leave-to-class="opacity-0">
    <div v-if="showFollowers || showFollowings" class="fixed inset-0 bg-black/5 backdrop-blur-sm z-[101]"
      @click="closeAllModals">
      <Transition enter-active-class="transition-all duration-300 ease-out"
        enter-from-class="opacity-0 translate-y-4 scale-95" enter-to-class="opacity-100 translate-y-0 scale-100"
        leave-active-class="transition-all duration-200 ease-in" leave-from-class="opacity-100 translate-y-0 scale-100"
        leave-to-class="opacity-0 translate-y-4 scale-95">
        <div class="fixed inset-0 flex items-center justify-center" @click.stop>
          <Followers v-if="showFollowers" @close="closeFollowers" :followers="followerList"
            :userId="Number(authStore.user.id)" />
          <Followings v-if="showFollowings" @close="closeFollowings" :followings="followingList"
            :userId="Number(authStore.user.id)" />
        </div>
      </Transition>
    </div>
  </Transition>
</template>

<script setup>
import Calendar from "@/components/user/Calendar.vue";
import CategoryList from "@/components/user/category/CategoryList.vue";
import CategoryHeader from "@/components/user/category/CategoryHeader.vue";
import GrassGraph from "@/components/user/grass/GrassGraph.vue";
import { ref, computed, onMounted, watchEffect, watch } from "vue";
import { useAuthStore } from "@/stores/auth";
import http from "@/api/http";
import Followers from "@/components/user/Followers.vue";
import Followings from "@/components/user/Followings.vue";
import { useFollowStore } from "@/stores/follow";
import Guestbook from "@/components/user/Guestbook.vue";
import { defineExpose } from 'vue'

const authStore = useAuthStore();
const selectedDate = ref(null);
const profileImage = computed(() => {
  if (!authStore.user?.profileImage) {
    return "/default-profile.png";
  }
  return `${http.defaults.baseURL}/user${authStore.user.profileImage}`;
});
const followerList = ref([]);
const followingList = ref([]);
const doneTodoCount = ref(0);
const showFollowers = ref(false);
const showFollowings = ref(false);
const followStore = useFollowStore();
const showGuestbook = ref(false);

// 팔로워/팔로잉 조회
const fetchFollowData = async () => {
  try {
    // 사용자가 로그인되어 있지 않으면 early return
    if (!authStore.checkAuth()) {
      followerList.value = [];
      followingList.value = [];
      return;
    }

    const [followers, followings] = await Promise.all([
      followStore.fetchFollowers(authStore.user.id),
      followStore.fetchFollowings(authStore.user.id),
    ]);
    followerList.value = followers || [];
    followingList.value = followings || [];
  } catch (error) {
    console.error("팔로우 데이터 조회 실패:", error);
    followerList.value = [];
    followingList.value = [];
  }
};

onMounted(async () => {
  if (authStore.checkAuth()) {
    try {
      await Promise.all([fetchFollowData(), fetchDoneTodoCount()]);
    } catch (error) {
      console.error("초기 데이터 로딩 실패:", error);
    }
  }
});

// 모달 닫을 때 데이터 새로고침
const closeFollowers = async () => {
  showFollowers.value = false;
  await fetchFollowData();
};

const closeFollowings = async () => {
  showFollowings.value = false;
  await fetchFollowData();
};

const handleDateSelected = (date) => {
  selectedDate.value = date;
};

// 투두 수 가져오기
const fetchDoneTodoCount = async () => {
  try {
    if (!authStore.checkAuth()) {
      doneTodoCount.value = 0;
      return;
    }
    const response = await http.get(`/todo/count/${authStore.user.id}`);
    doneTodoCount.value = response.data;
  } catch (error) {
    console.error("투두 수 조회 실패:", error);
    doneTodoCount.value = 0;
  }
};

// 이미지 에러 핸들러 추가
const handleImageError = (e) => {
  e.target.src = "/default-profile.png";
  e.target.onerror = null; // 무한 루프 방지
};

// 모든 모달 닫기 함수 추가
const closeAllModals = () => {
  showFollowers.value = false;
  showFollowings.value = false;
};

// needsRefresh 상태 감시
watch(() => followStore.needsRefresh, async () => {
  if (followStore.needsRefresh) {
    await fetchFollowData();
    followStore.setNeedsRefresh(false);
  }
});

// fetchFollowData를 명시적으로 expose
defineExpose({
  fetchFollowData
});
</script>

<style scoped>
.backdrop-enter-active,
.backdrop-leave-active {
  transition: all 0.3s ease;
}

.backdrop-enter-from,
.backdrop-leave-to {
  opacity: 0;
}

.modal-enter-active,
.modal-leave-active {
  transition: all 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(0.95);
}
</style>
