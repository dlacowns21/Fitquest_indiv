<template>
  <div @keydown.enter="login" class="h-screen flex flex-col">
    <div class="w-[950px] max-w-full px-4 mx-auto mt-0">
      <div class="flex items-center mb-8 pt-4">
        <button class="p-2" @click="router.go(-1)">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5"
            stroke="currentColor" class="w-6 h-6">
            <path stroke-linecap="round" stroke-linejoin="round" d="M15.75 19.5L8.25 12l7.5-7.5" />
          </svg>
        </button>
        <span class="flex-1 text-center font-bold">로그인</span>
        <div class="w-10"></div>
      </div>

      <form @submit.prevent="login" class="space-y-2">
        <div>
          <input type="text" placeholder="이메일" v-model="loginData.email" :disabled="isLoading"
            class="w-full px-4 py-3 font-semibold rounded-lg focus:outline-none bg-[#F2F2F2]" />
        </div>
        <div class="relative">
          <input :type="showPassword ? 'text' : 'password'" placeholder="비밀번호" v-model="loginData.password"
            :disabled="isLoading" class="w-full px-4 py-3 font-semibold rounded-lg focus:outline-none bg-[#F2F2F2]" />
          <button type="button" @click="showPassword = !showPassword"
            class="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-500">
            <EyeIcon v-if="showPassword" class="w-5 h-5" />
            <EyeSlashIcon v-else class="w-5 h-5" />
          </button>
        </div>

        <div v-if="errorMessage" class="text-red-500 text-sm mt-2">
          {{ errorMessage }}
        </div>
      </form>
      <div class="mt-8">
        <button :disabled="isLoading || !isFormValid"
          class="w-[75px] mx-auto block bg-black text-sm text-white py-4 rounded-full font-bold hover:bg-gray-800 disabled:bg-gray-400 disabled:cursor-not-allowed"
          @click="login">
          {{ isLoading ? "로딩중..." : "확인" }}
        </button>
      </div>

      <div class="mt-8 flex flex-col sm:flex-row justify-center items-center space-y-4 sm:space-y-0 sm:space-x-4">
        <button class="text-sm text-gray-400 hover:text-gray-600" @click="router.push('/signup')">
          회원가입
        </button>
        <span class="text-gray-300 hidden sm:inline">|</span>
        <RouterLink to="/community" class="text-sm text-gray-400 hover:text-gray-600">
          게스트로 시작하기
        </RouterLink>
      </div>
    </div>
  </div>
</template>

<script setup>
import { RouterLink, useRouter } from "vue-router";
import { ref, computed } from "vue";
import { EyeIcon, EyeSlashIcon } from "@heroicons/vue/24/outline";
import { useAuthStore } from "@/stores/auth";

const router = useRouter();
const isLoading = ref(false);
const showPassword = ref(false);
const errorMessage = ref("");
const store = useAuthStore();
const loginData = ref({
  email: "",
  password: "",
});

const isFormValid = computed(() => {
  return (
    loginData.value.email.trim() !== "" &&
    loginData.value.password.trim() !== ""
  );
});

const login = async () => {
  if (!isFormValid.value || isLoading.value) return;

  isLoading.value = true;
  errorMessage.value = "";

  const result = await store.login(loginData.value);

  if (result.success) {
    await router.push("/");
  } else {
    errorMessage.value = result.error;
  }

  isLoading.value = false;
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
