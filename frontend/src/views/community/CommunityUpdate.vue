<template>
  <div class="max-w-4xl mx-auto px-5 my-1 md:my-2">
    <h1 class="text-3xl font-bold text-gray-800 mb-8">게시글 수정</h1>

    <form @submit.prevent="submitPost" class="bg-white p-8 md:p-5 rounded-lg shadow-sm">
      <div class="mb-6">
        <label for="tag" class="block font-semibold text-gray-700 mb-2">태그</label>
        <div class="relative">
          <button type="button" @click.stop="toggleDropdown" :disabled="post.tag === '공지'"
            class="w-full px-3 py-2.5 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500/10 focus:border-blue-600 flex justify-between items-center"
            :class="[
              { 'border-red-500': showTagError },
              { 'bg-gray-50 cursor-not-allowed': post.tag === '공지' },
              'border-gray-300'
            ]">
            <span class="text-gray-700">{{
              post.tag || "태그를 선택하세요"
            }}</span>
            <ChevronDownIcon v-if="post.tag !== '공지'" class="h-5 w-5 text-gray-400" />
          </button>
          <div v-if="showTagError" class="mt-1 text-sm text-red-500 transition-all duration-200">
            태그를 선택해주세요.
          </div>

          <Transition name="dropdown">
            <div v-if="showDropdown"
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
          {{ isSubmitting ? '수정 중...' : '수정 완료' }}
        </button>
      </div>
    </form>
    <NeedLoginAlert v-if="needLoginAlert" @close="needLoginAlert = false" />
    <FileSizeAlert v-if="showFileSizeAlert" @close="showFileSizeAlert = false" />
    <ImageUploadFailAlert v-if="showImageUploadFailAlert" @close="showImageUploadFailAlert = false" />
    <UpdateSuccessAlert v-if="showUpdateSuccessAlert" @close="handleUpdateSuccess" />
    <UpdateFailAlert v-if="showUpdateFailAlert" @close="showUpdateFailAlert = false" />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from "vue";
import { useRouter, useRoute } from "vue-router";
import { useAuthStore } from "@/stores/auth";
import { useBoardStore } from "@/stores/board";
import http from "@/api/http";
import { COMMUNITY_TAGS } from "@/stores/tags";
import { getChoseong } from "es-hangul";
import { ChevronDownIcon } from "@heroicons/vue/24/outline";
import NeedLoginAlert from "@/components/alert/NeedLoginAlert.vue";
import FileSizeAlert from "@/components/alert/FileSizeAlert.vue";
import ImageUploadFailAlert from "@/components/alert/ImageUploadFailAlert.vue";
import UpdateSuccessAlert from "@/components/alert/UpdateSuccessAlert.vue";
import UpdateFailAlert from "@/components/alert/UpdateFailAlert.vue";

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();
const boardStore = useBoardStore();

const needLoginAlert = ref(false);
const showFileSizeAlert = ref(false);
const showImageUploadFailAlert = ref(false);
const showUpdateSuccessAlert = ref(false);
const showUpdateFailAlert = ref(false);

const fileInput = ref(null);
const post = ref({
  userId: null,
  writer: "",
  tag: "",
  title: "",
  content: "",
  postImage: null,
});

const showDropdown = ref(false);

const toggleDropdown = () => {
  if (post.value.tag === '공지') return;
  showDropdown.value = !showDropdown.value;
};

const selectTag = (tag) => {
  if (post.value.tag === '공지') return;
  post.value.tag = tag;
  showDropdown.value = false;
  showTagError.value = false;
};

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

const fetchPost = async () => {
  const response = await boardStore.fetchPost(route.params.id);
  post.value = response;
};

const handleImageChange = (event) => {
  const file = event.target.files[0];
  if (file) {
    post.value.postImage = file;
  }
};

// 에러 상태 관리를 위한 ref 추가
const showTagError = ref(false);
const showTitleError = ref(false);
const showContentError = ref(false);
const isSubmitting = ref(false);

// watch 추가
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

// 수정 완료 후 처리하는 함수
const handleUpdateSuccess = () => {
  if (isSubmitting.value) return;

  showUpdateSuccessAlert.value = false;
  router.push({
    path: `/community/detail/${route.params.id}`,
    query: {
      page: route.query.returnPage || "1",
      tag: route.query.returnTag,
    },
  });
};

const submitPost = async () => {
  if (isSubmitting.value) return;

  try {
    const isAuth = await authStore.checkAuth();
    if (!isAuth) {
      needLoginAlert.value = true;
      return;
    }

    // 유효성 검사
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

    isSubmitting.value = true;

    const postData = {
      userId: post.value.userId,
      writer: post.value.writer,
      tag: post.value.tag,
      title: post.value.title,
      content: post.value.content,
      choseong: {
        titleChoseong: getChoseong(post.value.title.replace(/\s+/g, "")),
        contentChoseong: getChoseong(post.value.content.replace(/\s+/g, "")),
        writerChoseong: getChoseong(post.value.writer.replace(/\s+/g, "")),
      },
    };

    const response = await boardStore.fetchUpdateBoard(
      route.params.id,
      postData
    );
    const boardId = route.params.id;

    // 이미지 처리
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

    showUpdateSuccessAlert.value = true;
  } catch (error) {
    console.error("Error posting:", error);
    if (error.response?.status === 401) {
      needLoginAlert.value = true;
    } else {
      showUpdateFailAlert.value = true;
    }
  } finally {
    isSubmitting.value = false;
  }
};

onMounted(async () => {
  if (!authStore.user.isAuthenticated) {
    needLoginAlert.value = true;
    return;
  }
  await fetchPost();
});
</script>

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

/* 스크롤바 스타일링 */
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
