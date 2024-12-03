<template>
  <div class="flex flex-col bg-white">
    <main class="flex-1 flex justify-center items-start bg-white">
      <div class="w-[900px] mx-auto">
        <div class="justify-items-center py-10">
          <div class="relative w-20 h-20">
            <img
              :src="computedProfileImage"
              class="w-20 h-20 rounded-full object-cover"
              alt="프로필 이미지"
              @error="(e) => (e.target.src = '/default-profile.png')"
            />
            <input
              type="file"
              ref="fileInput"
              @change="handleImageUpload"
              accept="image/*"
              class="hidden"
            />
          </div>
          <button
            @click="$refs.fileInput.click()"
            class="text-[0.78rem] font-bold text-blue-500 mt-4"
          >
            프로필 이미지
          </button>
        </div>
        <div class="space-y-[0.6rem]">
          <button
            class="flex justify-between items-center px-6 w-full h-[50px] text-[0.9rem] font-medium p-2 rounded-[20px] bg-[#f6f6f6]"
            @click="showChangeNameModal = true"
          >
            <span>이름</span>
            <span class="font-bold">
              {{ authStore.user.name }}
              <ChevronRightIcon class="w-4 h-4 inline" />
            </span>
          </button>
          <button
            class="flex justify-between items-center px-6 w-full h-[50px] text-[0.9rem] font-medium p-2 rounded-[20px] bg-[#f6f6f6]"
            @click="showChangeDescriptionModal = true"
          >
            <span>자기소개</span>
            <span class="font-bold">
              {{ authStore.user.description || "등록안됨" }}
              <svg
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 24 24"
                stroke-width="1.5"
                stroke="currentColor"
                class="w-4 h-4 inline"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  d="M8.25 4.5l7.5 7.5-7.5 7.5"
                />
              </svg>
            </span>
          </button>
        </div>
      </div>
    </main>
  </div>
  <Transition name="fade">
    <ChangeName
      v-if="showChangeNameModal"
      @close="showChangeNameModal = false"
    />
  </Transition>
  <Transition name="fade">
    <ChangeDescription
      v-if="showChangeDescriptionModal"
      @close="showChangeDescriptionModal = false"
    />
  </Transition>
  <!-- Alert 컴포넌트 추가 -->
  <FileSizeAlert v-if="showFileSizeAlert" @close="showFileSizeAlert = false" />
  <ImageUploadFailAlert
    v-if="showImageUploadFailAlert"
    @close="showImageUploadFailAlert = false"
  />
</template>

<script setup>
import { ref, computed } from "vue";
import ChangeName from "@/components/user/config/ChangeName.vue";
import ChangeDescription from "@/components/user/config/ChangeDescription.vue";
import { useAuthStore } from "@/stores/auth";
import http from "@/api/http";
import { ChevronRightIcon } from "@heroicons/vue/24/outline";
import FileSizeAlert from "@/components/alert/FileSizeAlert.vue";
import ImageUploadFailAlert from "@/components/alert/ImageUploadFailAlert.vue";

const authStore = useAuthStore();
const fileInput = ref(null);

const showChangeNameModal = ref(false);
const showChangeDescriptionModal = ref(false);
const showFileSizeAlert = ref(false);
const showImageUploadFailAlert = ref(false);

// computed 속성으로 프로필 이미지 URL 계산
const computedProfileImage = computed(() => {
  if (authStore.user?.profileImage) {
    return `${http.defaults.baseURL}/user${authStore.user.profileImage}`;
  }
  return "/default-profile.png";
});

// 이미지 업로드 처리 함수 수정
const handleImageUpload = async (event) => {
  const file = event.target.files[0];
  if (!file) return;
  if (file.size > 5 * 1024 * 1024) {
    showFileSizeAlert.value = true;
    return;
  }
  try {
    const formData = new FormData();
    formData.append("image", file);

    const response = await http.post(
      `/user/${authStore.user.id}/profile-image`,
      formData,
      {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      }
    );
    authStore.user.profileImage = response.data.imageUrl;
  } catch (error) {
    console.error("이미지 업로드 실패:", error);
    showImageUploadFailAlert.value = true;
  }
};
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
