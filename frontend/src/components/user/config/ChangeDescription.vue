<template>
  <div
    class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-[100]"
    ref="modalRef"
    tabindex="0"
    @click="$emit('close')"
  >
    <div class="bg-white rounded-2xl p-6 w-[90%] max-w-[400px]" @click.stop>
      <!-- 입력 폼 -->
      <div class="mb-6">
        <input
          v-model="newDescription"
          type="text"
          placeholder="새로운 자기소개를 입력하세요"
          @keydown.esc="$emit('close')"
          @keydown.enter="changeDescription"
          class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:border-black"
          :class="{ 'border-red-500': error }"
          ref="inputRef"
        />
        <p v-if="error" class="mt-2 text-sm text-red-500">{{ error }}</p>
      </div>
      <!-- 버튼 -->
      <div class="flex justify-center gap-3">
        <button
          @click="$emit('close')"
          class="w-full px-4 py-2 text-gray-600 hover:bg-gray-100 rounded-lg"
        >
          취소
        </button>
        <button
          @click="changeDescription"
          class="w-full px-4 py-2 bg-black font-semibold text-white rounded-lg hover:bg-gray-800"
          :disabled="!isValid"
        >
          {{ loading ? "변경 중..." : "변경하기" }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from "vue";
import { useAuthStore } from "@/stores/auth";
import http from "@/api/http";

const newDescription = ref("");
const error = ref("");
const loading = ref(false);
const emit = defineEmits(["close", "update:description"]);
const props = defineProps(["user"]);
const inputRef = ref(null);
const authStore = useAuthStore();

const isValid = computed(() => {
  return (
    newDescription.value &&
    !loading.value &&
    !error.value &&
    newDescription.value.length <= 20
  );
});

const changeDescription = async () => {
  if (!newDescription.value) {
    error.value = "자기소개를 입력해주세요.";
    return;
  }
  if (newDescription.value.length > 20) {
    error.value = "자기소개는 20자를 초과할 수 없습니다.";
    return;
  }
  try {
    const userId = authStore.user.id;
    const accessToken = sessionStorage.getItem("accessToken");
    loading.value = true;

    await http.put(
      `/user/${userId}`,
      {
        id: userId,
        email: authStore.user.email,
        description: newDescription.value,
      },
      {
        headers: {
          Authorization: `Bearer ${accessToken}`,
        },
      }
    );
    loading.value = false;
    authStore.user.description = newDescription.value;
    emit("update:description", newDescription.value);
    emit("close");
  } catch (err) {
    error.value = "자기소개 변경에 실패했습니다.";
    loading.value = false;
  }
};

watch(newDescription, (newValue) => {
  if (newValue.length > 20) {
    error.value = "자기소개는 20자를 초과할 수 없습니다.";
  } else {
    error.value = "";
  }
});

onMounted(async () => {
  // async 추가
  await Promise.resolve(); // nextTick 대신 사용 가능
  inputRef.value?.focus();
});
</script>
