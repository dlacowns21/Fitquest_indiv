<template>
  <div class="max-w-4xl mx-auto px-5 my-1 md:my-2">
    <h1 class="text-3xl font-bold text-gray-800 mb-8">게시글 작성</h1>
    <form @submit.prevent="submitPost" class="bg-white p-8 md:p-5 rounded-lg shadow-sm">
      <div class="mb-6">
        <label for="tag" class="block font-semibold text-gray-700 mb-2">태그</label>
        <div class="relative">
          <button type="button" :disabled="isAdmin" @click.stop="toggleDropdown"
            class="w-full px-3 py-2.5 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500/10 focus:border-blue-600 flex justify-between items-center"
            :class="[
              { 'bg-gray-50': isAdmin },
              { 'border-red-500': showTagError },
              'border-gray-300'
            ]">
            <span class="text-gray-700">{{
              post.tag || "태그를 선택하세요"
            }}</span>
            <ChevronDownIcon v-if="!isAdmin" class="h-5 w-5 text-gray-400" />
          </button>

          <div v-if="showTagError" class="mt-1 text-sm text-red-500 transition-all duration-200">
            태그를 선택해주세요.
          </div>

          <Transition name="dropdown">
            <div v-if="showDropdown && !isAdmin"
              class="absolute z-10 w-full mt-1 bg-white border rounded-md shadow-lg origin-top-right max-h-[240px] overflow-y-scroll">
              <div v-for="tag in COMMUNITY_TAGS" :key="tag" @click="selectTag(tag)"
                class="px-4 py-2.5 hover:bg-gray-100 cursor-pointer text-sm text-gray-700">
                {{ tag }}
              </div>
            </div>
          </Transition>
        </div>
      </div>
      <div class="mb-6">
        <label for="title" class="block font-semibold text-gray-700 mb-2">제목</label>
        <input type="text" id="title" v-model="post.title" placeholder="제목을 입력하세요" :class="[
          { 'border-red-500': showTitleError },
          'w-full px-3 py-2.5 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500/10 focus:border-blue-600'
        ]" />
        <div v-if="showTitleError" class="mt-1 text-sm text-red-500 transition-all duration-200">
          제목을 입력해주세요.
        </div>
      </div>
      <div class="mb-6">
        <label for="content" class="block font-semibold text-gray-700 mb-2">내용</label>
        <textarea id="content" v-model="post.content" placeholder="내용을 입력하세요" rows="10" :class="[
          { 'border-red-500': showContentError },
          'w-full px-3 py-2.5 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500/10 focus:border-blue-600 min-h-[200px] resize-y'
        ]" />
        <div v-if="showContentError" class="mt-1 text-sm text-red-500 transition-all duration-200">
          내용을 입력해주세요.
        </div>
      </div>
      <div class="mb-6">
        <label for="image" class="block font-semibold text-gray-700 mb-2">이미지</label>
        <div class="relative">
          <input type="file" id="image" ref="fileInput" @change="handleImageChange" accept="image/*" class="w-full px-3 py-2.5 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500/10 focus:border-blue-600
            file:mr-4 file:py-2 file:px-4 file:rounded-md file:border-0 file:text-sm file:font-medium
            file:bg-blue-50 file:text-blue-700 hover:file:bg-blue-100
            sm:text-sm md:text-base" />
        </div>
      </div>
      <div class="flex justify-end gap-3 mt-8">
        <button type="button"
          class="px-5 py-2.5 rounded-md font-medium bg-gray-100 text-gray-700 hover:bg-gray-200 transition-all duration-200"
          @click="router.go(-1)">
          취소
        </button>
        <button type="submit" :disabled="isSubmitting" :class="[
          'px-5 py-2.5 rounded-md font-medium transition-all duration-200',
          isSubmitting
            ? 'bg-gray-400 cursor-not-allowed'
            : 'bg-blue-600 hover:bg-blue-700 text-white'
        ]">
          {{ isSubmitting ? '등록 중...' : '등록' }}
        </button>
      </div>
    </form>
  </div>
  <NeedLoginAlert @close="needLoginAlert = false" v-if="needLoginAlert" />
  <FileSizeAlert v-if="showFileSizeAlert" @close="showFileSizeAlert = false" />
  <ImageUploadFailAlert v-if="showImageUploadFailAlert" @close="showImageUploadFailAlert = false" />
  <WriteSuccessAlert v-if="showWriteSuccessAlert" @close="handleWriteSuccess" />
  <WriteFailAlert v-if="showWriteFailAlert" @close="showWriteFailAlert = false" />
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, watch } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "@/stores/auth";
import { useBoardStore } from "@/stores/board";
import { COMMUNITY_TAGS } from "@/stores/tags";
import http from "@/api/http";
import { getChoseong } from "es-hangul";
import { ChevronDownIcon } from "@heroicons/vue/24/outline";
import NeedLoginAlert from "@/components/alert/NeedLoginAlert.vue";
import FileSizeAlert from "@/components/alert/FileSizeAlert.vue";
import ImageUploadFailAlert from "@/components/alert/ImageUploadFailAlert.vue";
import WriteSuccessAlert from "@/components/alert/WriteSuccessAlert.vue";
import WriteFailAlert from "@/components/alert/WriteFailAlert.vue";

const router = useRouter();
const authStore = useAuthStore();
const boardStore = useBoardStore();
const showFileSizeAlert = ref(false);
const showImageUploadFailAlert = ref(false);
const showWriteSuccessAlert = ref(false);
const showWriteFailAlert = ref(false);
const showTagError = ref(false);
const showTitleError = ref(false);
const showContentError = ref(false);

const needLoginAlert = ref(false);

const isAdmin = computed(() => {
  return authStore.user.isAdmin === true || authStore.user.isAdmin === 1;
});

const fileInput = ref(null);

// post ref 수정
const post = ref({
  userId: null,
  writer: "",
  tag: "",
  title: "",
  content: "",
  postImage: null,
  isNotice: false, // 공지사항 여부 추가
});

onMounted(async () => {
  if (!authStore.user.isAuthenticated) {
    needLoginAlert.value = true;
    return;
  }

  // 관리자 권한 확인을 위한 서버 요청 추가
  try {
    const response = await http.get(`/user/admin/${authStore.user.id}`);
    const isAdminUser = response.data.isAdmin;

    post.value.userId = authStore.user.id;
    post.value.writer = authStore.user.name;

    // 관리자인 경우 자동으로 공지 태그 설정
    if (isAdminUser) {
      post.value.tag = "공지";
      post.value.isNotice = true;
    }
  } catch (error) {
    console.error("관리자 권한 확인 실패:", error);
  }
});

const handleImageChange = (event) => {
  const file = event.target.files[0];
  if (file) {
    post.value.postImage = file;
  }
};

// 제출 상태를 관리할 ref 추가
const isSubmitting = ref(false);

// submitPost 함수 수정
const submitPost = async () => {
  // 이미 제출 중이면 중단
  if (isSubmitting.value) return;

  try {
    isSubmitting.value = true; // 제출 시작

    const isAuth = await authStore.checkAuth();
    if (!isAuth) {
      needLoginAlert.value = true;
      return;
    }

    // 모든 필수 필드 검사
    let hasError = false;

    if (!post.value.tag) {
      showTagError.value = true;
      hasError = true;
    }

    if (!post.value.title.trim()) {
      showTitleError.value = true;
      hasError = true;
    }

    if (!post.value.content.trim()) {
      showContentError.value = true;
      hasError = true;
    }

    if (hasError) {
      return;
    }

    // 에러 상태 초기화
    showTagError.value = false;
    showTitleError.value = false;
    showContentError.value = false;

    const postData = {
      userId: post.value.userId,
      writer: post.value.writer,
      tag: post.value.tag,
      title: post.value.title,
      content: post.value.content,
      isNotice: post.value.isNotice,
      choseong: {
        titleChoseong: getChoseong(post.value.title.replace(/\s+/g, "")),
        contentChoseong: getChoseong(post.value.content.replace(/\s+/g, "")),
        writerChoseong: getChoseong(post.value.writer.replace(/\s+/g, "")),
      },
    };

    const response = await boardStore.addBoard(postData);
    const boardId = response.id;

    const file = fileInput.value?.files[0];
    if (file) {
      if (file.size > 5 * 1024 * 1024) {
        showFileSizeAlert.value = true;
        return;
      }
      try {
        const formData = new FormData();
        formData.append("image", file);
        await http.post(`/board/${boardId}/post-image`, formData, {
          headers: {
            "Content-Type": "multipart/form-data",
          },
        });
      } catch (error) {
        console.error("이미지 업로드 실패:", error);
        showImageUploadFailAlert.value = true;
        return;
      }
    }

    showWriteSuccessAlert.value = true;
  } catch (error) {
    console.error("Error posting:", error);
    if (error.response?.status === 401) {
      needLoginAlert.value = true;
    } else {
      showWriteFailAlert.value = true;
    }
  } finally {
    isSubmitting.value = false; // 제출 완료 또는 에러 발생 시
  }
};

// handleWriteSuccess 함수 수정
const handleWriteSuccess = async () => {
  if (isSubmitting.value) return; // 이미 처리 중이면 중단

  showWriteSuccessAlert.value = false;
  await boardStore.fetchBoards();
  router.push({
    path: "/community",
    query: { page: 1 },
  });
};

// 입력 필드 변경 시 에러 메시지 숨김
watch(() => post.value.title, () => {
  if (post.value.title.trim()) {
    showTitleError.value = false;
  }
});

watch(() => post.value.content, () => {
  if (post.value.content.trim()) {
    showContentError.value = false;
  }
});

const showDropdown = ref(false);

const toggleDropdown = () => {
  showDropdown.value = !showDropdown.value;
};

const selectTag = (tag) => {
  if (!isAdmin.value) {
    post.value.tag = tag;
    post.value.isNotice = false;
    showDropdown.value = false;
    showTagError.value = false;
  }
};

// 드롭다운 외부 클릭 감지
const handleClickOutside = (event) => {
  if (showDropdown.value) {
    showDropdown.value = false;
  }
};

onMounted(() => {
  document.addEventListener("click", handleClickOutside);
});

onUnmounted(() => {
  document.removeEventListener("click", handleClickOutside);
});
</script>

<!-- style 부분 추가 -->
<style scoped>
/* 슬라이드 + 페이드 효과 */
.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.3s ease;
}

.dropdown-enter-from,
.dropdown-leave-to {
  transform: translateY(-10px);
  opacity: 0;
}

/* 또는 확장 효과 */

.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  transform-origin: top;
}

.dropdown-enter-from,
.dropdown-leave-to {
  transform: scaleY(0);
  opacity: 0;
}

/* 또는 슬라이드 + 줌 효과 */
.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.dropdown-enter-from,
.dropdown-leave-to {
  transform: translateY(-10px) scale(0.95);
  opacity: 0;
}

/* 스크롤바 스타일링 (overflow-y-scroll로 변경하여 항상 보이게 함) */
.absolute::-webkit-scrollbar {
  width: 8px;
}

.absolute::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.absolute::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

.absolute::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 툴팁 애니메이션 추가 */
.text-red-500 {
  animation: slideDown 0.2s ease-out;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-5px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
