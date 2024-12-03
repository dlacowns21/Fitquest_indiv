<!-- 홈 화면 컴포넌트 -->
<template>
    <div class="h-screen flex justify-center items-center bg-white px-4">
        <div class="flex flex-col items-center gap-8 w-full max-w-md">
            <div class="flex flex-col items-center gap-2">
                <div class="w-[200px] h-[200px] sm:w-[250px] sm:h-[250px]">
                    <img src="@/assets/images/kettlebell.png" alt="kettlebell" class="w-full h-full">
                </div>
                <h1 class="text-3xl sm:text-4xl font-extrabold text-black">FitQuest</h1>
                <p class="text-gray-600 text-sm">나만의 운동 플래너</p>
            </div>
            <div class="flex flex-col gap-4 w-full max-w-[300px]">
                <RouterLink to="/community"
                    class="w-full py-4 rounded-full text-center text-base font-semibold cursor-pointer transition-all duration-300 ease-in-out bg-black text-white hover:bg-gray-800">
                    게스트로 시작
                </RouterLink>
                <RouterLink to="/signup"
                    class="w-full py-4 rounded-full text-center text-base font-semibold cursor-pointer transition-all duration-300 ease-in-out bg-black text-white hover:bg-gray-800">
                    회원가입
                </RouterLink>
                <RouterLink to="/login"
                    class="text-gray-600 text-base text-semibold text-center hover:text-black underline">
                    로그인
                </RouterLink>
            </div>
        </div>
    </div>
</template>

<script setup>
import { RouterLink } from 'vue-router';

</script>