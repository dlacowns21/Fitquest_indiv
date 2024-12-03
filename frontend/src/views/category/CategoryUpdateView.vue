<template>
  <div class="max-w-[900px] mx-auto" @click="closeAllDropdowns" @keydown.esc="closeAllDropdowns"
    @keydown.enter="handleUpdate">
    <!-- 메인 컨텐츠 -->
    <main class="flex-1 flex justify-center items-start">
      <div class="w-[900px] p-4 mx-auto">
        <div class="space-y-6">
          <!-- 카테고리 입력 -->
          <div class="space-y-2">
            <input type="text" v-model="localCategory.title"
              class="w-full p-2 rounded-md border-b-[3px] focus:outline-none" :style="{
                borderImage: localCategory.color.includes('gradient') ? 
                  `${localCategory.color} 1` : 'none',
                borderBottom: localCategory.color.includes('gradient') ?
                  '3px solid transparent' :
                  `3px solid ${localCategory.color}`,
                borderImageSlice: localCategory.color.includes('gradient') ? 1 : 'none',
                color: localCategory.color.includes('gradient') ? 'transparent' : localCategory.color,
                backgroundImage: localCategory.color.includes('gradient') ? localCategory.color : 'none',
                '-webkit-background-clip': 'text',
                'background-clip': 'text'
              }" />
          </div>
          <!-- 에러 메시지 표시 -->
          <p v-if="error" class="text-sm text-red-500">{{ error }}</p>

          <!-- 공개 설정 세팅 -->
          <div class="space-y-4">
            <div class="space-y-2 pb-2 border-b-[1px]">
              <div class="flex items-center justify-between relative">
                <div class="flex items-center space-x-2">
                  <span class="text-sm text-gray-700">공개설정</span>
                </div>
                <button @click.stop="toggleDropdown" class="flex items-center space-x-2">
                  <span class="text-sm text-gray-600">
                    <template v-if="localCategory.isPublic">
                      <GlobeAltIcon class="w-4 h-4 inline-block" />
                      전체공개
                    </template>
                    <template v-else>
                      <LockClosedIcon class="w-4 h-4 inline-block" />
                      나만보기
                    </template>
                  </span>
                  <ChevronDownIcon class="h-5 w-5 text-gray-400" />
                </button>
                <!-- 드롭다운 메뉴에 Transition 추가 -->
                <Transition name="dropdown">
                  <div
                    v-if="showDropdown"
                    class="absolute right-0 top-8 w-32 bg-white border rounded-md shadow-lg z-10 origin-top-right"
                  >
                    <div
                      @click="selectVisibility(1)"
                      class="px-4 py-2 hover:bg-gray-100 cursor-pointer text-sm whitespace-nowrap"
                    >
                      <GlobeAltIcon class="w-4 h-4 inline-block" />
                      전체공개
                    </div>
                    <div
                      @click="selectVisibility(0)"
                      class="px-4 py-2 hover:bg-gray-100 cursor-pointer text-sm whitespace-nowrap"
                    >
                      <LockClosedIcon class="w-4 h-4 inline-block" />
                      나만보기
                    </div>
                  </div>
                </Transition>
              </div>
            </div>

            <!-- 색상 선택 -->
            <div class="pb-2 border-b-[1px]">
              <div class="flex items-center justify-between mb-3">
                <span class="text-sm text-gray-700">색상</span>
                <button 
                  @click.stop="toggleColorOptions" 
                  class="text-sm text-gray-600 flex items-center gap-2"
                >
                  <div 
                    class="w-8 h-8 rounded-full border"
                    :style="{ background: localCategory.color }"
                  ></div>
                  <ChevronDownIcon class="h-5 w-5 text-gray-400" />
                </button>
              </div>

              <Transition name="color-picker">
                <div v-if="showColorOptions" 
                  class="mt-2 p-4 bg-white rounded-lg shadow-lg origin-top-right"
                  @click.stop
                >
                  <!-- 탭 선택 -->
                  <div class="flex gap-4 mb-4 border-b">
                    <button 
                      @click="activeTab = 'solid'"
                      :class="['px-3 py-2', activeTab === 'solid' ? 'border-b-2 border-blue-500' : '']"
                    >
                      단색
                    </button>
                    <button 
                      @click="activeTab = 'gradient'"
                      :class="['px-3 py-2', activeTab === 'gradient' ? 'border-b-2 border-blue-500' : '']"
                    >
                      그라데이션
                    </button>
                  </div>

                  <!-- 단색 탭 내용 -->
                  <div v-if="activeTab === 'solid'" class="space-y-4">
                    <div class="grid grid-cols-8 gap-2">
                      <button 
                        v-for="color in defaultColors" 
                        :key="color"
                        @click="selectColor(color)"
                        class="w-8 h-8 rounded-full hover:scale-110 transition-transform"
                        :style="{ backgroundColor: color }"
                      ></button>
                    </div>
                    <div class="flex items-center gap-2">
                      <input
                        type="color"
                        v-model="localCategory.color"
                        class="w-8 h-8 rounded-full overflow-hidden [&::-webkit-color-swatch-wrapper]:p-0 [&::-webkit-color-swatch]:border-none [&::-webkit-color-swatch]:rounded-full"
                      />
                      <span class="text-sm text-gray-600">커스텀 색상</span>
                    </div>
                  </div>

                  <!-- 그라데이션 탭 내용 -->
                  <div v-if="activeTab === 'gradient'" class="space-y-4">
                    <div class="grid grid-cols-2 gap-2">
                      <button 
                        v-for="gradient in gradients" 
                        :key="gradient.name"
                        @click="selectGradient(gradient.value)"
                        class="h-12 rounded-lg hover:scale-105 transition-transform"
                        :style="{ background: gradient.value }"
                      ></button>
                    </div>

                    <!-- 커스텀 그라데이션 -->
                    <div class="space-y-2 pt-4 border-t">
                      <p class="text-sm text-gray-600 mb-2">커스텀 그라데이션</p>
                      <div class="flex items-center gap-4">
                        <div class="flex items-center gap-2">
                          <input type="color" v-model="gradientStart" @input="updateCustomGradient"
                            class="w-8 h-8 rounded-full overflow-hidden [&::-webkit-color-swatch-wrapper]:p-0 [&::-webkit-color-swatch]:border-none [&::-webkit-color-swatch]:rounded-full" />
                          <span class="text-sm text-gray-600">시작</span>
                        </div>
                        <div class="flex items-center gap-2">
                          <input type="color" v-model="gradientEnd" @input="updateCustomGradient"
                            class="w-8 h-8 rounded-full overflow-hidden [&::-webkit-color-swatch-wrapper]:p-0 [&::-webkit-color-swatch]:border-none [&::-webkit-color-swatch]:rounded-full" />
                          <span class="text-sm text-gray-600">끝</span>
                        </div>
                      </div>
                      <!-- 미리보기 -->
                      <button @click="selectGradient(customGradient)"
                        class="w-full h-12 rounded-lg mt-2 hover:scale-105 transition-transform"
                        :style="{ background: customGradient }"></button>
                    </div>
                  </div>
                </div>
              </Transition>
            </div>

            <!-- 삭제 버튼 -->
            <div class="flex justify-center gap-3">
              <button @click="showDeleteModal = true"
                class="py-2 px-12 font-semibold rounded-md bg-[#f5f5f5] text-red-500 hover:bg-red-100">
                삭제
              </button>
              <button @click="handleUpdate"
                class="py-2 px-12 font-semibold rounded-md bg-[#f5f5f5] text-blue-500 hover:bg-blue-100">
                확인
              </button>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- 삭제 확인 모달 -->
    <Transition name="modal">
      <div v-if="showDeleteModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-[999]"
        @click="showDeleteModal = false">
        <div class="bg-white rounded-lg p-6 w-80" @click.stop>
          <p class="text-gray-600 mb-6 text-center">카테고리를 삭제하시겠습니까?</p>
          <div class="flex justify-center gap-3">
            <button @click="showDeleteModal = false"
              class="w-full px-4 py-2 text-gray-600 hover:bg-gray-100 rounded-md">
              취소
            </button>
            <button @click="categoryStore.fetchDeleteCategory(id)"
              class="w-full px-4 py-2 bg-red-500 text-white rounded-md hover:bg-red-600">
              삭제
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { onMounted, ref, computed, onUnmounted, watch } from "vue";
import { ChevronDownIcon, GlobeAltIcon, LockClosedIcon } from "@heroicons/vue/24/outline";
import { useRoute } from "vue-router";
import { useCategoryStore } from "@/stores/category";

const route = useRoute();
const showDropdown = ref(false);
const showDeleteModal = ref(false);
const categoryStore = useCategoryStore();
const id = route.params.id;
const localCategory = ref({
  title: "",
  color: "#000000",
  isPublic: 1,
});
const error = ref("");
const activeTab = ref('solid');
const showColorOptions = ref(false);
const gradientStart = ref('#FF512F');
const gradientEnd = ref('#DD2476');
const customGradient = ref('linear-gradient(to right, #FF512F, #DD2476)');

const defaultColors = [
  '#FF6B6B', '#4ECDC4', '#45B7D1', '#96CEB4', '#FFEEAD',
  '#D4A5A5', '#9B59B6', '#3498DB', '#E74C3C', '#2ECC71',
  '#F1C40F', '#1ABC9C', '#34495E', '#7F8C8D', '#D35400',
  '#C0392B', '#8E44AD', '#2980B9', '#27AE60', '#F39C12'
];

const gradients = [
  { name: 'Sunset', value: 'linear-gradient(to right, #FF512F, #DD2476)' },
  { name: 'Ocean', value: 'linear-gradient(to right, #2193b0, #6dd5ed)' },
  { name: 'Purple', value: 'linear-gradient(to right, #834d9b, #d04ed6)' },
  { name: 'Forest', value: 'linear-gradient(to right, #134E5E, #71B280)' },
  { name: 'Morning', value: 'linear-gradient(to right, #FDC830, #F37335)' },
  { name: 'Royal', value: 'linear-gradient(to right, #141E30, #243B55)' }
];

const closeAllDropdowns = () => {
  showDropdown.value = false;
  showColorOptions.value = false;
};

const toggleDropdown = () => {
  showDropdown.value = !showDropdown.value;
};

const selectVisibility = (isPublic) => {
  localCategory.value.isPublic = isPublic;
  showDropdown.value = false;
};

const handleEscape = (e) => {
  if (e.key === "Escape") {
    if (showDeleteModal.value) {
      showDeleteModal.value = false;
    }
    if (showColorOptions.value) {
      showColorOptions.value = false;
    }
  }
};

onMounted(async () => {
  try {
    await categoryStore.fetchCategory(id);
    localCategory.value = {
      ...categoryStore.category,
      title: categoryStore.category.title || "",
    };
  } catch (err) {
    error.value = "카테고리 정보를 불러오는데 실패했습니다";
  }
});

watch(showDeleteModal, (newValue) => {
  if (newValue) {
    document.addEventListener("keydown", handleEscape);
  } else {
    document.removeEventListener("keydown", handleEscape);
  }
});

onUnmounted(() => {
  document.removeEventListener("keydown", handleEscape);
});

// 유효성 검사
const isValid = computed(() => {
  if (!localCategory.value?.title?.trim()) {
    error.value = "카테고리 이름을 입력해주세요";
    return false;
  }
  if (localCategory.value.title.trim().length < 1) {
    error.value = "카테고리 이름은 1글자 이상 입력해주세요";
    return false;
  }
  error.value = "";
  return true;
});

// 업데이트 핸들러
const handleUpdate = async () => {
  if (!isValid.value) return;

  try {
    await categoryStore.fetchUpdateCategory(localCategory.value);
  } catch (err) {
    error.value = err.message;
  }
};

const toggleColorOptions = () => {
  showColorOptions.value = !showColorOptions.value;
};

const selectColor = (color) => {
  localCategory.value.color = color;
};

const selectGradient = (gradient) => {
  localCategory.value.color = gradient;
};

const updateCustomGradient = () => {
  customGradient.value = `linear-gradient(to right, ${gradientStart.value}, ${gradientEnd.value})`;
};
</script>

<style scoped>
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.color-picker-enter-active {
  animation: bounce-in 0.5s;
}
.color-picker-leave-active {
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

/* 기존 스타일은 유지하고 dropdown 애니메이션 추가 */
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
