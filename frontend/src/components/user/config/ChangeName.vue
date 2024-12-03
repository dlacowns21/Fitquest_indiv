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
          v-model="newName"
          type="text"
          placeholder="새로운 닉네임을 입력하세요"
          @keydown.esc="$emit('close')"
          @keydown.enter="changeUserName"
          class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:border-black"
          :class="{ 'border-red-500': error }"
          ref="inputRef"
          @compositionstart="onCompositionStart"
          @compositionend="onCompositionEnd"
          @input="handleInput"
          maxlength="16"
        />
        <p v-if="error" class="mt-2 text-sm text-red-500">{{ error }}</p>
        <p class="mt-2 text-sm text-gray-500">{{ newName.length }}/16자</p>
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
          @click="changeUserName"
          class="w-full px-4 py-2 bg-black font-semibold text-white rounded-lg hover:bg-gray-800"
          :disabled="!isValid || newName.length > 16"
        >
          {{ loading ? "변경 중..." : "변경하기" }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import axios from "axios";
import { useAuthStore } from "@/stores/auth";
import http from "@/api/http";
import { getChoseong } from "es-hangul";

const newName = ref("");
const error = ref("");
const loading = ref(false);
const isDuplicateChecked = ref(false);
let timeout = null;
const isComposing = ref(false);
const emit = defineEmits(["close", "update:userName"]);
const props = defineProps(["user"]);
const inputRef = ref(null);
const authStore = useAuthStore();

const isValid = computed(() => {
  return newName.value && !loading.value && !error.value && isDuplicateChecked.value && newName.value.length <= 16;
});

const onCompositionStart = () => {
  isComposing.value = true;
};

const onCompositionEnd = () => {
  isComposing.value = false;
  debouncedCheckUserName();
};

const handleInput = (e) => {
  newName.value = e.target.value;
  if (newName.value.length > 16) {
    error.value = "닉네임은 16자를 초과할 수 없습니다.";
    isDuplicateChecked.value = false;
    return;
  }
  if (!isComposing.value) {
    debouncedCheckUserName();
  }
};

const debouncedCheckUserName = () => {
  isDuplicateChecked.value = false;
  error.value = "";

  if (timeout) {
    clearTimeout(timeout);
  }

  timeout = setTimeout(() => {
    checkUserNameDuplicate();
  }, 300);
};

const checkUserNameDuplicate = async () => {
  if (!newName.value) {
    error.value = "닉네임을 입력해주세요.";
    isDuplicateChecked.value = false;
    return;
  }

  if (newName.value.length > 16) {
    error.value = "닉네임은 16자를 초과할 수 없습니다.";
    isDuplicateChecked.value = false;
    return;
  }

  try {
    const response = await http.get(`/user/check-name/${newName.value}`);
    if (response.data) {
      error.value = "";
      isDuplicateChecked.value = true;
    } else {
      error.value = "사용할 수 없는 닉네임입니다.";
      isDuplicateChecked.value = false;
    }
  } catch (err) {
    if (err.response?.status === 409) {
      error.value = "사용할 수 없는 닉네임입니다.";
      isDuplicateChecked.value = false;
    } else {
      error.value = "닉네임 중복 확인 중 오류가 발생했습니다.";
      isDuplicateChecked.value = false;
    }
  }
};

const changeUserName = async () => {
  if (!newName.value) {
    error.value = "닉네임을 입력해주세요.";
    return;
  }

  if (newName.value.length > 16) {
    error.value = "닉네임은 16자를 초과할 수 없습니다.";
    return;
  }

  await checkUserNameDuplicate();

  if (!isDuplicateChecked.value) {
    error.value = "닉네임 중복 확인이 필요합니다.";
    return;
  }

  try {
    const userId = authStore.user.id;
    const accessToken = sessionStorage.getItem("accessToken");
    loading.value = true;

    const newNameChoseong = getChoseong(newName.value.replace(/\s+/g, ''));
    await http.put(
      `/user/${userId}`,
      {
        id: userId,
        email: authStore.user.email,
        name: newName.value + "," + newNameChoseong,
      },
      {
        headers: {
          Authorization: `Bearer ${accessToken}`,
        },
      }
    );

    loading.value = false;
    authStore.user.name = newName.value;
    emit("update:name", newName.value);
    emit("close");
  } catch (err) {
    error.value = "닉네임 변경에 실패했습니다.";
    loading.value = false;
  }
};

onMounted(async () => {
  await Promise.resolve();
  inputRef.value?.focus();
});
</script>
