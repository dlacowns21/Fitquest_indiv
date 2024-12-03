<template>
  <div class="max-w-[900px] mx-auto px-2 flex flex-col gap-2">
    <!-- 메인 컨텐츠 영역 -->
    <div class="flex flex-col md:flex-row gap-2">
      <!-- 왼쪽 섹션: 프로필 + 캘린더 -->
      <div class="md:w-1/2 flex flex-col gap-2 h-[600px]">
        <!-- 통합된 프로필 섹션 -->
        <div class="h-[200px] p-2 bg-white rounded-2xl shadow-md">
          <div class="flex items-center gap-4 mb-1">
            <!-- 프로필 이미지 -->
            <div class="relative group">
              <img :src="profileImage"
                class="w-20 h-20 rounded-full object-cover border-3 border-white shadow-md group-hover:scale-105 transition duration-300"
                alt="프로필 이미지" @error="handleImageError" />
            </div>
            <!-- 사용자 정보 -->
            <div class="flex flex-col">
              <div class="flex items-center gap-2 font-bold text-gray-800 text-lg mb-1">
                {{ userProfile.name }}
                <button @click="showGuestbook = true"
                  class="flex items-center justify-center w-8 h-8 rounded-full hover:bg-gray-200 transition-colors">
                  <span class="material-icons text-gray-600 text-sm">chat</span>
                </button>
              </div>
              <div class="text-gray-600 text-sm">
                {{ userProfile.description || "자기소개가 없습니다." }}
              </div>
              <div v-if="authStore.user.id !== userId" class="mt-2">
                <button v-if="!isFollowing" @click="handleFollow"
                  class="px-2 py-0.5 bg-black hover:bg-gray-800 text-white rounded-lg text-sm font-medium transition duration-200 whitespace-nowrap">
                  팔로우
                </button>
                <button v-else @click="handleUnfollow"
                  class="px-2 py-0.5 bg-gray-200 hover:bg-gray-300 text-gray-800 rounded-lg text-sm font-medium transition duration-200 whitespace-nowrap">
                  언팔로우
                </button>
              </div>
            </div>
          </div>
          <!-- 팔로워/팔로잉/완료 정보 -->
          <div class="flex justify-around pt-1">
            <div class="flex flex-col items-center px-4 py-2 hover:bg-gray-50 rounded-xl transition-all duration-200">
              <span class="font-bold text-lg text-gray-800">{{ doneTodoCount }}</span>
              <span class="material-icons text-gray-600 text-md">check_circle</span>
              <span class="text-xs text-gray-500 font-medium">완료</span>
            </div>
            <button @click="showFollowers = true"
              class="flex flex-col items-center px-4 py-2 hover:bg-gray-50 rounded-xl transition-all duration-200">
              <span class="font-bold text-lg text-gray-800">{{ followerList.length }}</span>
              <span class="material-icons text-gray-600 text-md">group</span>
              <span class="text-xs text-gray-500 font-medium">팔로워</span>
            </button>
            <button @click="showFollowings = true"
              class="flex flex-col items-center px-4 py-2 hover:bg-gray-50 rounded-xl transition-all duration-200">
              <span class="font-bold text-lg text-gray-800">{{ followingList.length }}</span>
              <span class="material-icons text-gray-600 text-md">person_add</span>
              <span class="text-xs text-gray-500 font-medium">팔로잉</span>
            </button>
          </div>
        </div>

        <!-- 캘린더 섹션 -->
        <div class="h-[400px] rounded-[15px]">
          <Calendar @dateSelected="handleDateSelected" :userId="Number(userId)" />
        </div>
      </div>

      <!-- 오른쪽 섹션: 카테고리 -->
      <div class="md:w-1/2">
        <div class="p-3 rounded-[15px] bg-white shadow-md">
          <div class="h-[575px] overflow-y-auto">
            <CategoryList :selectedDate="selectedDate" :userId="Number(userId)"
              @doneTodoCountUpdate="fetchDoneTodoCount" />
          </div>
        </div>
      </div>
    </div>

    <!-- 잔디 그래프 섹션 -->
    <div class="w-full bg-[#f7f8f9] p-2 rounded-[15px] mb-8">
      <GrassGraph :userId="Number(userId)" />
    </div>

    <!-- 방명록 모달 -->
    <Guestbook :userId="Number(userId)" v-if="showGuestbook" @close="showGuestbook = false" />
  </div>

  <Followers v-if="showFollowers" @close="closeFollowers" :followers="followerList" :userId="Number(userId)" />
  <Followings v-if="showFollowings" @close="closeFollowings" :followings="followingList" :userId="Number(userId)" />
  <NeedLoginAlert v-if="needLoginAlert" @close="needLoginAlert = false" />
</template>

<script setup>
import { ref, computed, onMounted, watch, onBeforeMount } from "vue";
import { useRoute, useRouter } from 'vue-router';
import Calendar from "@/components/user/Calendar.vue";
import CategoryList from "@/components/user/category/CategoryList.vue";
import GrassGraph from "@/components/user/grass/GrassGraph.vue";
import http from "@/api/http";
import { useAuthStore } from "@/stores/auth";
import { useFollowStore } from "@/stores/follow";
import Followers from "@/components/user/Followers.vue";
import Followings from "@/components/user/Followings.vue";
import NeedLoginAlert from "@/components/alert/NeedLoginAlert.vue";
import Guestbook from "@/components/user/Guestbook.vue";

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const followStore = useFollowStore();
const selectedDate = ref(null);
const userProfile = ref({});
const doneTodoCount = ref(0);
const followerList = ref([]);
const followingList = ref([]);
const isFollowing = ref(false);
const showFollowers = ref(false);
const showFollowings = ref(false);
const needLoginAlert = ref(false);
const showGuestbook = ref(false);

// URL의 userId 파라미터 또는 현재 로그인한 사용자의 ID
const userId = computed(() => route.params.userId);

// 프로필 이미지 URL
const profileImage = computed(() => {
  if (!userProfile.value?.profileImage) {
    return "/default-profile.png";
  }
  return `${http.defaults.baseURL}/user${userProfile.value.profileImage}`;
});

// 투두 수 가져오기
const fetchDoneTodoCount = async () => {
  try {
    const response = await http.get(`/todo/count/${userId.value}`);
    doneTodoCount.value = response.data;
  } catch (error) {
    console.error('투두 수 조회 실패:', error);
  }
};

// 사용자 프로필 정보 가져오기
const fetchUserProfile = async () => {
  try {
    if (userId.value) {
      const response = await http.get(`/user/${userId.value}`);
      userProfile.value = response.data;
    }
  } catch (error) {
    console.error('프로필 정보 조회 실패:', error);
  }
};

const handleDateSelected = (date) => {
  selectedDate.value = date;
};

// 팔로워/팔로잉/팔로우 상태 조회
const fetchFollowData = async () => {
  try {
    const [followers, followings, followStatus] = await Promise.all([
      followStore.fetchFollowers(userId.value),
      followStore.fetchFollowings(userId.value),
      followStore.fetchIsFollowing(userId.value)
    ]);
    followerList.value = followers;
    followingList.value = followings;
    isFollowing.value = followStatus;
  } catch (error) {
    console.error('팔로우 데이터 조회 실패:', error);
  }
};

// 팔로우/언팔로우 핸들러
const handleFollow = async () => {
  const isAuth = await authStore.checkAuth();
  if (!isAuth) {
    needLoginAlert.value = true;
    return;
  }
  await followStore.fetchFollow(userId.value);
  await fetchFollowData();
};

const handleUnfollow = async () => {
  const isAuth = await authStore.checkAuth();
  if (!isAuth) {
    needLoginAlert.value = true;
    return;
  }
  await followStore.fetchUnfollow(userId.value);
  await fetchFollowData();
};

const closeFollowers = async () => {
  showFollowers.value = false;
  await fetchFollowData();
};

const closeFollowings = async () => {
  showFollowings.value = false;
  await fetchFollowData();
};

onMounted(async () => {
  await fetchUserProfile();
  await Promise.all([
    fetchFollowData(),
    fetchDoneTodoCount()
  ]);
});

onBeforeMount(async () => {
  if (Number(route.params.userId) === Number(authStore.user.id)) {
    router.push({ name: "userHome" });
    return;
  }
});

watch(() => userId.value, async () => {
  await fetchUserProfile();
  await Promise.all([
    fetchFollowData(),
    fetchDoneTodoCount()
  ]);
});

// 이미지 에러 핸들러 추가
const handleImageError = (e) => {
  e.target.src = "/default-profile.png";
  e.target.onerror = null;
};

watch(() => followStore.needsRefresh, async () => {
  if (followStore.needsRefresh) {
    await fetchFollowData();
    followStore.setNeedsRefresh(false);
  }
});
</script>

<style scoped>
.modal-enter-active,
.modal-leave-active {
  transition: all 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .bg-white {
  transform: scale(0.95);
}

.modal-enter-active .bg-white {
  transition: transform 0.3s ease;
}

.modal-enter-to .bg-white {
  transform: scale(1);
}
</style>