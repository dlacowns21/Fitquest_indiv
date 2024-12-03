<template>
    <div class="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-[101]"
        @click="handleClose">
        <div class="bg-white/95 rounded-2xl w-full max-w-md mx-4 shadow-2xl transform transition-all duration-300 ease-out"
            @click.stop>
            <!-- 모달 헤더 -->
            <div class="flex items-center justify-between p-5 border-b border-gray-100">
                <h2 class="text-xl font-bold bg-gradient-to-r from-gray-900 to-gray-700 bg-clip-text text-transparent">
                    방명록</h2>
                <button @click="$emit('close')" class="text-gray-400 hover:text-gray-600 transition-colors">
                    <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                            d="M6 18L18 6M6 6l12 12" />
                    </svg>
                </button>
            </div>

            <!-- 방명록 목록 -->
            <div class="px-5 pt-5 h-[400px] overflow-y-auto">
                <div v-if="guestbooks.length === 0"
                    class="flex flex-col items-center justify-center py-10 text-gray-500">
                    <span class="material-icons text-4xl mb-2">edit_note</span>
                    <p>첫 번째 방명록을 남겨보세요!</p>
                </div>
                <div v-else class="space-y-4">
                    <div v-for="g in guestbooks" :key="g.id"
                        class="p-4 bg-gray-50 rounded-xl hover:bg-gray-100 transition-colors">
                        <div class="flex items-center justify-between mb-2">
                            <RouterLink :to="`/home/${g.user.id}`" class="flex items-center gap-2" @click="$emit('close')">
                                <img :src="g.user.profileImage" :alt="g.user.name" class="w-8 h-8 rounded-full object-cover">
                                <span class="font-medium text-gray-900">{{ g.user.name }}</span>
                            </RouterLink>
                            <div class="flex items-center gap-2">
                                <span class="text-sm text-gray-500">{{ formatDate(g.date) }}</span>
                                <button v-if="g.user.id === authStore.user.id" 
                                    @click="deleteGuestbook(g.id)"
                                    class="text-gray-400 hover:text-red-500 transition-colors cursor-pointer flex items-center">
                                    <span class="material-icons text-sm">delete</span>
                                </button>
                            </div>
                        </div>
                        <p class="text-gray-700">{{ g.content }}</p>
                    </div>
                </div>
            </div>

            <!-- 방명록 작성 영역 -->
            <div class="p-5">
                <div class="flex flex-col gap-2">
                    <div class="flex gap-2">
                        <textarea v-model="guestbook.content" placeholder="방명록을 남겨주세요 (최대 50자)"
                            maxlength="50"
                            class="flex-1 h-16 p-3 rounded-xl bg-gray-50 border border-gray-200 focus:border-gray-300 focus:ring-0 resize-none transition-colors"
                            :class="{'border-red-500': showError}"></textarea>
                        <button @click="submitGuestbook"
                            :disabled="isOverLimit"
                            :class="{'opacity-50 cursor-not-allowed': isOverLimit}"
                            class="px-4 py-2 bg-gradient-to-r from-gray-900 to-gray-700 text-white rounded-xl hover:from-gray-800 hover:to-gray-600 transition-all duration-200 font-medium h-16">
                            등록하기
                        </button>
                    </div>
                    <div class="flex justify-between">
                        <span v-if="showError" class="text-red-500 text-sm">방명록 내용을 입력해주세요.</span>
                        <span class="text-sm" :class="{'text-red-500': isOverLimit, 'text-gray-500': !isOverLimit}">
                            {{ guestbook.content?.length || 0 }}/50
                        </span>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <ConflictedGuestbookAlert v-if="conflictAlert" @close="conflictAlert = false" />
    <div v-if="showDeleteConfirm" 
        class="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-[102]"
        @click="showDeleteConfirm = false">
        <div class="bg-white rounded-2xl p-6 w-80" @click.stop>
            <h3 class="text-lg font-semibold mb-4">방명록 삭제</h3>
            <p class="text-gray-600 mb-6">정말 삭제하시겠습니까?</p>
            <div class="flex justify-end gap-2">
                <button @click="showDeleteConfirm = false"
                    class="px-4 py-2 text-gray-600 hover:bg-gray-100 rounded-lg transition-colors">
                    취소
                </button>
                <button @click="confirmDelete"
                    class="px-4 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 transition-colors">
                    삭제
                </button>
            </div>
        </div>
    </div>
</template>

<script setup>
import { onMounted, ref, computed, onUnmounted } from 'vue';
import { useAuthStore } from '@/stores/auth';
import { useGuestbookStore } from '@/stores/guestbook';
import ConflictedGuestbookAlert from '../alert/ConflictedGuestbookAlert.vue';
import { RouterLink } from 'vue-router';

const props = defineProps(['userId']);
const emit = defineEmits(['close']);
const authStore = useAuthStore();
const guestbookStore = useGuestbookStore();
const guestbooks = ref([]);
const guestbook = ref({});
const conflictAlert = ref(false);
const showError = ref(false);
const showDeleteConfirm = ref(false);
const deleteTargetId = ref(null);

const isOverLimit = computed(() => {
    return (guestbook.value.content?.length || 0) > 50;
});

//YYYY-MM-DD 형식으로 날짜 변환
const formatDate = (date) => {
    return date.split('T')[0];
};

const handleClose = () => {
    emit('close');
};

const handleEscape = (e) => {
    if (e.key === 'Escape') {
        handleClose();
    }
};

const submitGuestbook = async () => {
    if (!guestbook.value.content?.trim()) {
        showError.value = true;
        return;
    }
    showError.value = false;
    try {
        const guestbookData = {
            targetId: props.userId,
            userId: authStore.user.id,
            content: guestbook.value.content.slice(0, 50)
        };
        const response = await guestbookStore.fetchInsertGuestbook(guestbookData);
        
        // 응답 상태가 409(Conflict)인 경우
        if (response.status === 409) {
            conflictAlert.value = true;
            return;
        }
        
        guestbooks.value = await guestbookStore.fetchGuestbook(props.userId);
        guestbook.value.content = '';
    } catch (error) {
        if (error.response?.status === 409) {
            conflictAlert.value = true;
        } else {
            console.error('방명록 등록 실패:', error);
        }
    }
};

const deleteGuestbook = (guestbookId) => {
    deleteTargetId.value = guestbookId;
    showDeleteConfirm.value = true;
};

const confirmDelete = async () => {
    try {
        await guestbookStore.fetchDeleteGuestbook(deleteTargetId.value);
        guestbooks.value = await guestbookStore.fetchGuestbook(props.userId);
        showDeleteConfirm.value = false;
    } catch (error) {
        console.error('방명록 삭제 실패:', error);
    }
};

onMounted(async () => {
    window.addEventListener('keydown', handleEscape);
    guestbooks.value = await guestbookStore.fetchGuestbook(props.userId);
});

onUnmounted(() => {
    window.removeEventListener('keydown', handleEscape);
});
</script>