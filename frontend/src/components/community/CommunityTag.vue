<template>
  <div class="py-4 border-b border-gray-100">
    <div class="flex items-center justify-between mb-2 cursor-pointer" @click="toggleTags">
      <div class="flex items-center gap-2">
        <h3 class="font-medium text-gray-700">태그 필터</h3>
        <span v-if="selectedTags.length > 0" 
          class="text-sm text-blue-600 bg-blue-50 px-2 py-0.5 rounded-full">
          {{ selectedTags.length }}개 선택됨
        </span>
      </div>
      <span class="material-icons text-gray-500 transition-transform duration-200"
        :class="{ 'rotate-180': !isCollapsed }">
        expand_more
      </span>
    </div>
    
    <Transition
      enter-active-class="transition-all duration-300 ease-out"
      enter-from-class="opacity-0 max-h-0"
      enter-to-class="opacity-100 max-h-[200px]"
      leave-active-class="transition-all duration-300 ease-in"
      leave-from-class="opacity-100 max-h-[200px]"
      leave-to-class="opacity-0 max-h-0"
    >
      <div v-show="!isCollapsed" class="overflow-hidden">
        <div class="flex flex-wrap gap-2">
          <button
            v-for="tag in tags"
            :key="tag"
            class="whitespace-nowrap px-3 py-1.5 rounded-full bg-gray-100 hover:bg-gray-200 transition-colors duration-200 text-sm"
            :class="{
              'font-bold bg-blue-600 text-blue-700 hover:bg-blue-700': selectedTags.includes(tag),
            }"
            @click="toggleTag(tag)"
          >
            #{{ tag }}
          </button>
        </div>
        <div v-if="selectedTags.length > 0" class="mt-3 flex justify-end">
          <button
            @click="clearTags"
            class="text-sm text-gray-600 hover:text-gray-800 transition-colors duration-200"
          >
            선택 초기화
          </button>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue';

const props = defineProps({
  tags: {
    type: Array,
    required: true,
  },
  modelValue: {
    type: Array,
    default: () => [],
  },
});

const emit = defineEmits(["update:modelValue"]);
const isCollapsed = ref(true);
const selectedTags = ref([...props.modelValue]);

// 초기값 설정
onMounted(() => {
  selectedTags.value = [...props.modelValue];
});

// modelValue 변경 감시
watch(() => props.modelValue, (newValue) => {
  selectedTags.value = [...newValue];
}, { deep: true });

const toggleTags = () => {
  isCollapsed.value = !isCollapsed.value;
};

const toggleTag = (tag) => {
  const index = selectedTags.value.indexOf(tag);
  if (index === -1) {
    selectedTags.value = [...selectedTags.value, tag];
  } else {
    selectedTags.value = selectedTags.value.filter(t => t !== tag);
  }
  emit("update:modelValue", [...selectedTags.value]);
};

const clearTags = () => {
  selectedTags.value = [];
  emit("update:modelValue", []);
};
</script>

<style scoped>
.flex-wrap {
  margin: -4px;
}

.flex-wrap button {
  margin: 4px;
  transition: all 0.2s ease;
}

@media (max-width: 640px) {
  .flex-wrap button {
    font-size: 0.875rem;
    padding: 0.375rem 0.75rem;
  }
}
</style>
