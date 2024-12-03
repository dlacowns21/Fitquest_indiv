<template>
  <div class="comment-form">
    <div class="flex items-start gap-4">
      <div class="flex-1 flex gap-1">
        <textarea
          v-model="content"
          placeholder="댓글을 남겨주세요."
          class="flex-1 p-3 border border-gray-300 rounded-lg resize-none focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
        ></textarea>
        <div class="flex flex-col items-stretch gap-2">
          <template v-if="parentId">
            <button
              @click="submitComment"
              class="px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-md hover:bg-blue-700 transition-colors duration-200"
            >
              등록
            </button>
            <button
              @click="$emit('cancel')"
              class="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 rounded-md hover:bg-gray-200 transition-colors duration-200"
            >
              취소
            </button>
          </template>
        </div>
      </div>
    </div>

    <!-- Alert 컴포넌트들 -->
    <CommentEmptyAlert v-if="showEmptyAlert" @close="showEmptyAlert = false" />
    <CommentFailAlert v-if="showFailAlert" @close="showFailAlert = false" />
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useAuthStore } from "@/stores/auth";
import http from "@/api/http";
import CommentEmptyAlert from "@/components/alert/CommentEmptyAlert.vue";
import CommentFailAlert from "@/components/alert/CommentFailAlert.vue";

const props = defineProps({
  boardId: {
    type: Number,
    required: true,
  },
  parentId: {
    type: Number,
    default: 0,
  },
});

const emit = defineEmits(["comment-added", "cancel"]);
const authStore = useAuthStore();
const content = ref("");
const showEmptyAlert = ref(false);
const showFailAlert = ref(false);

const submitComment = async () => {
  if (!content.value.trim()) {
    showEmptyAlert.value = true;
    return;
  }

  try {
    const comment = {
      boardId: props.boardId,
      userId: authStore.user.id,
      writer: authStore.user.name,
      content: content.value.trim(),
      parentId: props.parentId > 0 ? props.parentId : null,
    };

    await http.post("/comment", comment);
    content.value = "";
    emit("comment-added");
  } catch (error) {
    showFailAlert.value = true;
  }
};

defineExpose({
  submitComment,
});
</script>

<style scoped>
textarea {
  min-height: 36px;
  max-height: 200px;
}
</style>
