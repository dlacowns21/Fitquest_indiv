<template>
  <div class="h-screen flex flex-col" @keydown.enter="regist">
    <div class="w-[950px] max-w-full px-4 mx-auto mt-0">
      <div class="flex items-center mb-8 pt-4">
        <button class="p-2" @click="router.go(-1)">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            fill="none"
            viewBox="0 0 24 24"
            stroke-width="1.5"
            stroke="currentColor"
            class="w-6 h-6"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              d="M15.75 19.5L8.25 12l7.5-7.5"
            />
          </svg>
        </button>
        <span class="flex-1 text-center font-bold">가입하기</span>
        <div class="w-10"></div>
      </div>

      <form @submit.prevent="regist" class="space-y-2">
        <div>
          <input
            type="text"
            placeholder="닉네임 등록"
            :disabled="isLoading"
            class="w-full px-4 py-3 font-semibold rounded-lg focus:outline-none bg-[#F2F2F2]"
            v-model="user.name"
            @blur="checkName"
          />
          <p
            v-if="isNameEmpty && isNameBlurred"
            class="text-sm text-red-400 pl-2 pt-1"
          >
            닉네임을 입력해주세요.
          </p>
          <p
            v-else-if="isNameDuplicated && isNameBlurred"
            class="text-sm text-red-400 pl-2 pt-1"
          >
            사용할 수 없는 닉네임입니다.
          </p>
        </div>
        <div>
          <input
            type="email"
            placeholder="이메일 등록"
            :disabled="isLoading"
            class="w-full px-4 py-3 font-semibold rounded-lg focus:outline-none bg-[#F2F2F2]"
            v-model="user.email"
            @blur="checkEmail"
            @input="validateEmail"
          />
          <p
            v-if="isEmailEmpty && isEmailBlurred"
            class="text-sm text-red-400 pl-2 pt-1"
          >
            이메일을 입력해주세요.
          </p>
          <p
            v-else-if="isEmailDuplicated && isEmailBlurred"
            class="text-sm text-red-400 pl-2 pt-1"
          >
            사용할 수 없는 이메일입니다.
          </p>
          <p
            v-else-if="!isValidEmail && user.email"
            class="text-sm text-red-400 pl-2 pt-1"
          >
            올바른 이메일 형식이 아닙니다.
          </p>
        </div>
        <div class="relative">
          <input
            :type="showPassword ? 'text' : 'password'"
            placeholder="비밀번호 등록"
            :disabled="isLoading"
            class="w-full px-4 py-3 font-semibold rounded-lg focus:outline-none bg-[#F2F2F2]"
            v-model="user.password"
            @blur="checkPassword"
          />
          <button
            type="button"
            @click="showPassword = !showPassword"
            class="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-500"
          >
            <EyeIcon v-if="showPassword" class="w-5 h-5" />
            <EyeSlashIcon v-else class="w-5 h-5" />
          </button>
          <p
            v-if="isPasswordEmpty && isPasswordBlurred"
            class="text-sm text-red-400 pl-2 pt-1"
          >
            패스워드를 입력해주세요.
          </p>
        </div>
      </form>

      <div class="mt-8">
        <button
          :disabled="isLoading || !isFormValid"
          class="w-[75px] mx-auto block bg-black text-sm text-white py-4 rounded-full font-bold hover:bg-gray-800 disabled:bg-gray-400 disabled:cursor-not-allowed"
          @click="regist"
        >
          {{ isLoading ? "로딩중..." : "확인" }}
        </button>
      </div>
    </div>
  </div>
  <SignupSuccessAlert
    v-if="authStore.showSignupSuccessAlert"
    @close="authStore.handleSignupSuccess"
  />
</template>

<script setup>
import { useRouter } from "vue-router";
import { ref, computed } from "vue";
import { useAuthStore } from "@/stores/auth";
import { EyeIcon, EyeSlashIcon } from "@heroicons/vue/24/outline";
import SignupSuccessAlert from "@/components/alert/SignupSuccessAlert.vue";

const authStore = useAuthStore();
const router = useRouter();
const isLoading = ref(false);
const showPassword = ref(false);

const isValidEmail = ref(true);
const emailPattern = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;

const user = ref({
  name: "",
  email: "",
  password: "",
  isAdmin: 0,
});

const isNameEmpty = computed(() => user.value.name.trim() === "");
const isEmailEmpty = computed(() => user.value.email.trim() === "");
const isPasswordEmpty = computed(() => user.value.password.trim() === "");

// isFormValid computed 속성 수정
const isFormValid = computed(() => {
  return (
    !isNameEmpty.value &&
    !isEmailEmpty.value &&
    !isPasswordEmpty.value &&
    !isNameDuplicated.value &&
    !isEmailDuplicated.value &&
    isValidEmail.value
  ); // 이메일 유효성 검사 추가
});

// 이메일 유효성 검사 함수 추가
const validateEmail = () => {
  const filteredEmail = user.value.email.replace(/[^A-Za-z0-9@._-]/g, "");
  user.value.email = filteredEmail;
  isValidEmail.value = emailPattern.test(user.value.email);
};

// blur 이벤트가 발생했는지 여부를 체크
const isNameBlurred = ref(false);
const isEmailBlurred = ref(false);
const isPasswordBlurred = ref(false);

const isNameDuplicated = ref(false);
const isEmailDuplicated = ref(false);

const checkName = async () => {
  isNameBlurred.value = true;
  if (!isNameEmpty.value) {
    const result = await authStore.checkDuplicateName(user.value.name);
    isNameDuplicated.value = result.isDuplicated;
  }
};

// 기존 checkEmail 함수 수정
const checkEmail = async () => {
  isEmailBlurred.value = true;
  if (!isEmailEmpty.value && isValidEmail.value) {
    const result = await authStore.checkDuplicateEmail(user.value.email);
    isEmailDuplicated.value = result.isDuplicated;
  }
};

const checkPassword = () => {
  isPasswordBlurred.value = true;
  isPasswordEmpty.value = user.value.password.trim() === "";
};

const regist = async () => {
  if (!isFormValid.value || isLoading.value) return;
  isLoading.value = true;
  try {
    // auth store의 signup 함수 사용
    await authStore.signup(user.value);

    // 입력 필드 초기화
    user.value = {
      name: "",
      email: "",
      password: "",
      isAdmin: 0,
    };

    // blur 상태 초기화
    isNameBlurred.value = false;
    isEmailBlurred.value = false;
    isPasswordBlurred.value = false;

    // 중복 체크 상태 초기화
    isNameDuplicated.value = false;
    isEmailDuplicated.value = false;
  } catch (error) {
    console.error("회원가입 실패:", error);
    alert(error.response?.data?.message || "회원가입에 실패했습니다.");
  } finally {
    isLoading.value = false;
  }
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
