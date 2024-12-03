<template>
  <div class="max-w-[950px] mx-auto" @click="closeAllDropdowns" @keydown.esc="closeAllDropdowns"
    @keydown.enter="handleAddCategory">
    <!-- 메인 컨텐츠 -->
    <main class="flex-1 flex justify-center items-start">
      <div class="w-[900px] p-4 mx-auto">
        <div class="space-y-6">
          <!-- 카테고리 입력 -->
          <div class="space-y-2">
            <!-- 카테고리 이름 입력 -->
            <input type="text" v-model="category.title" class="w-full p-2 rounded-md border-b-[3px] focus:outline-none"
              :style="{
                borderBottomColor: category.color.includes('gradient') ? 'transparent' : category.color,
                borderImage: category.color.includes('gradient') ? `${category.color} 1` : 'none',
                color: category.color.includes('gradient') ?
                  category.color.match(/(?:to right,\s*)([^,]+)/)[1].trim() :
                  category.color
              }" placeholder="카테고리 입력" />
            <!-- 에러 메시지 -->
            <p v-if="error" class="text-sm text-red-500">{{ error }}</p>
          </div>

          <!-- 공개 설정 세팅 -->
          <div class="space-y-4">
            <div class="space-y-2 pb-2 border-b-[1px]">
              <div class="flex items-center justify-between relative">
                <div class="flex items-center space-x-2">
                  <span class="text-sm text-gray-700">공개설정</span>
                </div>
                <button @click.stop="toggleDropdown" class="flex items-center space-x-2">
                  <span class="text-sm text-gray-600">
                    <template v-if="category.isPublic">
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
                  <div v-if="showDropdown"
                    class="absolute right-0 top-8 w-32 bg-white border rounded-md shadow-lg z-10 origin-top-right">
                    <div @click="selectVisibility(1)"
                      class="px-4 py-2 hover:bg-gray-100 cursor-pointer text-sm whitespace-nowrap">
                      <GlobeAltIcon class="w-4 h-4 inline-block" />
                      전체공개
                    </div>
                    <div @click="selectVisibility(0)"
                      class="px-4 py-2 hover:bg-gray-100 cursor-pointer text-sm whitespace-nowrap">
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
                <button @click.stop="toggleColorOptions" class="text-sm text-gray-600 flex items-center gap-2">
                  <div class="w-8 h-8 rounded-full border" :style="{ background: category.color }"></div>
                  <ChevronDownIcon class="h-5 w-5 text-gray-400" />
                </button>
              </div>

              <!-- Transition 컴포넌트 추가 -->
              <Transition name="color-picker">
                <!-- 색상 옵션 패널 -->
                <div v-if="showColorOptions" class="mt-2 p-4 bg-white rounded-lg shadow-lg origin-top-right" @click.stop>
                  <!-- 탭 선택 -->
                  <div class="flex gap-4 mb-4 border-b">
                    <button @click="activeTab = 'solid'"
                      :class="['px-3 py-2', activeTab === 'solid' ? 'border-b-2 border-blue-500' : '']">
                      단색
                    </button>
                    <button @click="activeTab = 'gradient'"
                      :class="['px-3 py-2', activeTab === 'gradient' ? 'border-b-2 border-blue-500' : '']">
                      그라데이션
                    </button>
                  </div>

                  <!-- 단색 탭 내용 -->
                  <div v-if="activeTab === 'solid'" class="space-y-4">
                    <!-- 기본 컬러 팔레트 -->
                    <div class="grid grid-cols-8 gap-2">
                      <button v-for="color in defaultColors" :key="color" @click="selectColor(color)"
                        class="w-8 h-8 rounded-full hover:scale-110 transition-transform"
                        :style="{ backgroundColor: color }"></button>
                    </div>
                    <!-- 커스텀 컬러 피커 -->
                    <div class="flex items-center gap-2">
                      <input type="color" v-model="category.color"
                        class="w-8 h-8 rounded-full overflow-hidden [&::-webkit-color-swatch-wrapper]:p-0 [&::-webkit-color-swatch]:border-none [&::-webkit-color-swatch]:rounded-full" />
                      <span class="text-sm text-gray-600">커스텀 색상</span>
                    </div>
                  </div>
                  <!-- 그라데이션 탭 내용 -->
                  <div v-if="activeTab === 'gradient'" class="space-y-4">
                    <!-- 미리 정의된 그라데이션 -->
                    <div class="grid grid-cols-2 gap-2">
                      <button v-for="gradient in gradients" :key="gradient.name" @click="selectGradient(gradient.value)"
                        class="h-12 rounded-lg hover:scale-105 transition-transform"
                        :style="{ background: gradient.value }"></button>
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
          </div>
        </div>
      </div>
    </main>
    <div class="flex justify-center items-center h-[60px]">
      <button @click="handleAddCategory()" class="w-[130px] h-10 mt-6 bg-black text-white rounded-full">
        확인
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from "vue";
import { ChevronDownIcon, GlobeAltIcon, LockClosedIcon } from "@heroicons/vue/24/outline";
import { useCategoryStore } from "@/stores/category";
import { useAuthStore } from "@/stores/auth";

const authStore = useAuthStore();
const error = ref("");
const showDropdown = ref(false);
const showColorPicker = ref(false);
const categoryStore = useCategoryStore();
const category = ref({
  userId: authStore.user.id,
  title: "",
  isPublic: 1,
  color: "#000000",
});

const isValid = computed(() => {
  if (!category.value.title.trim()) {
    error.value = "카테고리 이름을 입력해주세요";
    return false;
  }
  if (category.value.title.trim().length < 1) {
    error.value = "카테고리 이름은 1글자 이상 입력해주세요";
    return false;
  }
  error.value = "";
  return true;
});

const closeAllDropdowns = () => {
  showDropdown.value = false;
  showColorOptions.value = false;
};

const toggleDropdown = () => {
  showDropdown.value = !showDropdown.value;
};

const selectVisibility = (isPublic) => {
  category.value.isPublic = isPublic;
  showDropdown.value = false;
};

const toggleColorPicker = () => {
  showColorPicker.value = !showColorPicker.value;
};

const handleAddCategory = async () => {
  if (!isValid.value) return;
  try {
    await categoryStore.fetchAddCategory(category.value);
  } catch (err) {
    error.value = err.message;
  }
};

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
  {
    name: 'Sunset',
    value: 'linear-gradient(to right, #FF512F, #DD2476)'
  },
  {
    name: 'Ocean',
    value: 'linear-gradient(to right, #2193b0, #6dd5ed)'
  },
  {
    name: 'Purple',
    value: 'linear-gradient(to right, #834d9b, #d04ed6)'
  },
  {
    name: 'Forest',
    value: 'linear-gradient(to right, #134E5E, #71B280)'
  },
  {
    name: 'Morning',
    value: 'linear-gradient(to right, #FDC830, #F37335)'
  },
  {
    name: 'Royal',
    value: 'linear-gradient(to right, #141E30, #243B55)'
  }
];

const toggleColorOptions = () => {
  showColorOptions.value = !showColorOptions.value;
};

const selectColor = (color) => {
  category.value.color = color;
};

const selectGradient = (gradient) => {
  category.value.color = gradient;
};

const updateCustomGradient = () => {
  customGradient.value = `linear-gradient(to right, ${gradientStart.value}, ${gradientEnd.value})`;
};

</script>

<style scoped>
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

.dropdown-enter-active {
  animation: bounce-in 0.5s;
}

.dropdown-leave-active {
  animation: bounce-in 0.5s reverse;
}
</style>
