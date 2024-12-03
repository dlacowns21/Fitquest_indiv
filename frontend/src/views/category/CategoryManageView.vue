<template>
  <div class="max-w-[900px] mx-auto">
    <div v-if="categoryStore.categories.length > 0">
      <!-- 카테고리 목록 -->
      <div
        class="space-y-2 h-[calc(100vh-160px)] overflow-auto [&::-webkit-scrollbar]:hidden [-ms-overflow-style:none] [scrollbar-width:none]">
        <div v-for="category in categoryStore.categories" :key="category.id" @click="goUpdate(category.id)"
          class="flex items-center p-3 rounded-lg transition-colors cursor-pointer hover:bg-gray-100">
          <div class="flex items-center gap-2 bg-[#f2f2f2] px-3.5 py-2 rounded-full">
            <span class="text-base font-semibold" :style="{
              color: category.color.includes('gradient') ? 'transparent' : category.color,
              backgroundImage: category.color.includes('gradient') ? category.color : 'none',
              '-webkit-background-clip': 'text',
              'background-clip': 'text'
            }">{{ category.title }}</span>
            <div class="flex items-center">
              <GlobeAltIcon v-if="category.isPublic" class="w-5 h-5 text-gray-400" />
              <LockClosedIcon v-else class="w-5 h-5 text-gray-400" />
            </div>
          </div>
        </div>
      </div>
    </div>
    <div v-else class="flex flex-col items-center justify-center text-gray-400 pb-10 h-[calc(100vh-160px)]">
      <div class="material-icons text-5xl mb-3">sentiment_neutral</div>
      <div>카테고리가 없어요</div>
    </div>
    <!-- 카테고리 추가 버튼 -->
    <div class="flex justify-center">
      <RouterLink v-if="authStore.user.id" to="/category-regist"
        class="fixed bottom-24 left-1/2 -translate-x-1/2 flex justify-center items-center w-[120px] h-10 bg-[#f2f2f2] rounded-full">
        <PlusIcon class="w-6 h-6 text-gray-600" />
      </RouterLink>
    </div>
  </div>
</template>

<script setup>
import { useCategoryStore } from "@/stores/category";
import { useAuthStore } from "@/stores/auth";
import { GlobeAltIcon, LockClosedIcon, PlusIcon } from "@heroicons/vue/24/outline";
import { onMounted, onBeforeUnmount } from "vue";
import { useRouter } from "vue-router";

const categoryStore = useCategoryStore();
const authStore = useAuthStore();
const router = useRouter();

const goUpdate = (id) => {
  router.push({
    name: "CategoryUpdate",
    params: { id },
  });
};

// 카테고리 목록 갱신 함수
const refreshCategories = async () => {
  if (authStore.user.id) {
    await categoryStore.fetchCategories(authStore.user.id);
  }
};

onMounted(async () => {
  await refreshCategories();
});

// 라우터 가드 대신 watch 사용
const unwatch = router.afterEach(async (to, from) => {
  if (to.name === 'CategoryManage') {
    await refreshCategories();
  }
});

// 컴포넌트 언마운트 시 이벤트 리스너 제거
onBeforeUnmount(() => {
  unwatch();
});
</script>
