<template>
  <div class="py-4 relative search-container">
    <div class="flex flex-col sm:flex-row items-stretch sm:items-center gap-2">
      <!-- 검색 필터 선택 -->
      <div class="relative w-full sm:w-[120px]">
        <button @click="toggleDropdown"
          class="flex items-center justify-between gap-2 px-4 py-1.5 w-full sm:w-[120px] bg-white border border-gray-200 rounded-lg hover:border-gray-300 transition-all duration-200 shadow-sm">
          <span class="text-gray-700">{{ selectedLabel }}</span>
          <span class="material-icons text-gray-500 text-lg transition-transform duration-200"
            :class="{ 'rotate-180': isOpen }">
            expand_more
          </span>
        </button>

        <!-- 드롭다운 메뉴 -->
        <Transition
          enter-active-class="transition duration-200 ease-out"
          enter-from-class="transform scale-95 opacity-0"
          enter-to-class="transform scale-100 opacity-100"
          leave-active-class="transition duration-200 ease-in"
          leave-from-class="transform scale-100 opacity-100" 
          leave-to-class="transform scale-95 opacity-0"
        >
          <div v-if="isOpen"
            class="absolute top-full left-0 mt-1 w-full sm:w-[120px] bg-white border border-gray-200 rounded-lg shadow-xl z-20">
            <div v-for="option in searchOptions" :key="option.value" @click="selectOption(option.value)"
              class="px-4 py-2 hover:bg-gray-50 cursor-pointer text-gray-700 first:rounded-t-lg last:rounded-b-lg transition-all duration-200 hover:pl-6">
              {{ option.label }}
            </div>
          </div>
        </Transition>
      </div>

      <!-- 검색창 -->
      <div class="relative flex-1">
        <div
          class="flex items-center border border-gray-200 rounded-lg px-3 hover:border-gray-300 transition-all duration-200 shadow-sm hover:shadow">
          <input type="text" :placeholder="getPlaceholder" v-model="searchText" @input="handleInput" 
            @keydown.down.prevent="handleArrowDown"
            @keydown.up.prevent="handleArrowUp"
            @keydown.enter="handleEnter"
            class="flex-1 border-none outline-none px-1 py-1.5 text-[17px]" autocomplete="off" />
          <button class="p-1 px-2 cursor-pointer hover:text-gray-600 transition-colors duration-200" @click="search">
            <i class="fas fa-search"></i>
          </button>
        </div>

        <!-- 연관검색어 부분 -->
        <Transition
          enter-active-class="transition duration-200 ease-out"
          enter-from-class="transform -translate-y-1 opacity-0"
          enter-to-class="transform translate-y-0 opacity-100"
          leave-active-class="transition duration-200 ease-in"
          leave-from-class="transform translate-y-0 opacity-100"
          leave-to-class="transform -translate-y-1 opacity-0"
        >
          <div v-if="enableRelatedSearches && showRelatedSearches && relatedSearches.length > 0"
            class="absolute w-full bg-white border border-gray-200 rounded-lg mt-1 shadow-xl z-10">
            <div v-for="(search, index) in relatedSearches" :key="index"
              class="px-4 py-2 hover:bg-gray-50 cursor-pointer transition-all duration-200 hover:pl-6"
              :class="{ 'bg-gray-50': selectedIndex === index }"
              @click="selectRelatedSearch(search)">
              {{ search }}
            </div>
          </div>
        </Transition>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref, computed } from "vue";
import { useAuthStore } from "@/stores/auth";
import { disassemble, getChoseong } from "es-hangul";
import http from "@/api/http";

const emit = defineEmits(["update:modelValue", "search"]);
const searchType = ref("all");
const isOpen = ref(false);
const searchText = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
});
const relatedSearches = ref([]);
const showRelatedSearches = ref(false);
const authStore = useAuthStore();
const lastInputValue = ref("");
const selectedIndex = ref(-1);

const searchOptions = [
  { value: 'all', label: '전체' },
  { value: 'title', label: '제목' },
  { value: 'writer', label: '작성자' },
  { value: 'content', label: '내용' }
];

const selectedLabel = computed(() => {
  return searchOptions.find(option => option.value === searchType.value)?.label;
});

let debounceTimer;
let clickOutsideHandler;

const props = defineProps({
  modelValue: String,
  enableRelatedSearches: {
    type: Boolean,
    default: true,
  },
});

const toggleDropdown = () => {
  isOpen.value = !isOpen.value;
};

const selectOption = (value) => {
  searchType.value = value;
  isOpen.value = false;
};

const getPlaceholder = computed(() => {
  switch (searchType.value) {
    case "title": return "제목으로 검색";
    case "writer": return "작성자로 검색";
    case "content": return "내용으로 검색";
    default: return "전체 검색";
  }
});

const debounceFetchSearchHistory = () => {
  clearTimeout(debounceTimer);
  debounceTimer = setTimeout(() => {
    if (lastInputValue.value !== searchText.value && props.enableRelatedSearches) {
      lastInputValue.value = searchText.value;
      fetchSearchHistory();
    }
  }, 100);
};

const fetchSearchHistory = async () => {
  if (!authStore.user.isAuthenticated) {
    return;
  }
  const query = searchText.value.trim();
  if (!query) {
    relatedSearches.value = [];
    showRelatedSearches.value = false;
    return;
  }
  try {
    const response = await http.get(`/board/history/${authStore.user.id}/${query}`, {
      skipLoading: true
    });
    relatedSearches.value = response.data.filter((item) =>
      item.toLowerCase().includes(query.toLowerCase())
    );
    showRelatedSearches.value = relatedSearches.value.length > 0;
    selectedIndex.value = -1;
  } catch (error) {
    console.error("연관 검색어 조회 실패:", error);
    relatedSearches.value = [];
    showRelatedSearches.value = false;
  }
};

const handleInput = (e) => {
  searchText.value = e.target.value;
  debounceFetchSearchHistory();
};

const handleArrowDown = () => {
  if (showRelatedSearches.value && relatedSearches.value.length > 0) {
    selectedIndex.value = (selectedIndex.value + 1) % relatedSearches.value.length;
  }
};

const handleArrowUp = () => {
  if (showRelatedSearches.value && relatedSearches.value.length > 0) {
    selectedIndex.value = selectedIndex.value <= 0 ? relatedSearches.value.length - 1 : selectedIndex.value - 1;
  }
};

const handleEnter = () => {
  if (showRelatedSearches.value && selectedIndex.value !== -1) {
    selectRelatedSearch(relatedSearches.value[selectedIndex.value]);
  } else {
    search();
  }
};

const search = async () => {
  if (!searchText.value.trim()) return;
  if (authStore.checkAuth()) {
    try {
      await http.post(`/board/history`, {
        userId: authStore.user.id,
        content: searchText.value.trim(),
      }, {
        skipLoading: true
      });
    } catch (error) {
      console.error("검색 기록 저장 실패:", error);
    }
  }
  const isChoseong = getChoseong(searchText.value.trim()) === searchText.value.trim();

  // 검색어 처리
  // 초성으로만 이루어져 있으면 초성으로 검색, 아니면 그냥 검색
  // 만약 초성으로 이루어져 있다면 초성을 분해하여 검색(ㄳ,ㄵ,ㄶ,ㄺ,ㄻ,ㄼ,ㄽ,ㄾ,ㄿ,ㅀ,ㅄ 예외처리 위함)
  emit("search", {
    key: searchType.value,
    word: isChoseong ? disassemble(getChoseong(searchText.value.replace(/\s+/g, ''))) : searchText.value.replace(/\s+/g, ''),
    isChoseong: isChoseong,
  });
  showRelatedSearches.value = false;
};

const selectRelatedSearch = (selectedSearch) => {
  searchText.value = selectedSearch;
  emit("search", {
    key: searchType.value,
    word: selectedSearch.trim(),
  });
  showRelatedSearches.value = false;
};

onMounted(() => {
  clickOutsideHandler = (e) => {
    if (!e.target.closest(".search-container")) {
      showRelatedSearches.value = false;
      isOpen.value = false;
    }
  };
  document.addEventListener("click", clickOutsideHandler);
});

onUnmounted(() => {
  clearTimeout(debounceTimer);
  document.removeEventListener("click", clickOutsideHandler);
});
</script>
