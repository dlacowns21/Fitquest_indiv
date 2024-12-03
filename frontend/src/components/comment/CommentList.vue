<template>
  <div class="space-y-1">
    <div
      v-for="comment in comments"
      :key="comment.id"
      class="comment-container"
    >
      <!-- 기존 댓글 컨텐츠 부분 -->
      <div
        :class="[
          'p-4 rounded-lg',
          {
            'ml-0': !comment.parentId,
            'ml-8': comment.parentId && getCommentDepth(comment) === 1,
            'ml-16': comment.parentId && getCommentDepth(comment) === 2,
            'ml-24': comment.parentId && getCommentDepth(comment) === 3,
            'ml-32': comment.parentId && getCommentDepth(comment) >= 4,
          },
          'bg-white border border-gray-200',
        ]"
      >
        <!-- 기존 댓글 내용 부분은 그대로 유지 -->
        <div class="flex justify-between items-start mb-2">
          <div class="flex items-center gap-2">
            <span class="font-medium text-gray-900">{{ comment.writer }}</span>
            <span class="text-sm text-gray-500">{{
              formatDate(comment.date)
            }}</span>
          </div>
          <div class="flex items-center gap-2">
            <button
              v-if="isAuthenticated"
              @click="showReplyForm(comment.id)"
              class="text-sm text-gray-600 hover:text-blue-600"
            >
              답글
            </button>
            <button
              v-if="canDelete(comment)"
              @click="handleDelete(comment.id)"
              class="text-sm text-red-500 hover:text-red-600"
            >
              삭제
            </button>
          </div>
        </div>
        <p class="text-gray-700 whitespace-pre-line">{{ comment.content }}</p>
      </div>

      <!-- 대댓글 작성 폼 -->
      <div
        v-if="activeReplyId === comment.id"
        :class="[
          'mt-3',
          {
            'ml-8': !comment.parentId,
            'ml-16': comment.parentId && getCommentDepth(comment) === 1,
            'ml-24': comment.parentId && getCommentDepth(comment) === 2,
            'ml-32': comment.parentId && getCommentDepth(comment) >= 3,
          },
        ]"
      >
        <CommentForm
          :boardId="boardId"
          :parentId="comment.id"
          @comment-added="handleCommentAdded"
          @cancel="activeReplyId = null"
        />
      </div>
    </div>

    <!-- 댓글이 없을 때 -->
    <div v-if="comments.length === 0" class="text-center py-8 text-gray-500">
      첫 댓글을 작성해보세요.
    </div>
  </div>

  <!-- 모달 컴포넌트들 -->
  <CommentDeleteConfirmAlert
    v-if="showDeleteConfirmAlert"
    @close="showDeleteConfirmAlert = false"
    @confirm="confirmDelete"
  />
  <CommentDeleteFailAlert
    v-if="showDeleteFailAlert"
    @close="showDeleteFailAlert = false"
  />
</template>

<script setup>
import { ref, onMounted, computed } from "vue";
import { useAuthStore } from "@/stores/auth";
import CommentForm from "./CommentForm.vue";
import CommentDeleteConfirmAlert from "@/components/alert/CommentDeleteConfirmAlert.vue";
import CommentDeleteFailAlert from "@/components/alert/CommentDeleteFailAlert.vue";
import http from "@/api/http";

const showDeleteConfirmAlert = ref(false);
const showDeleteFailAlert = ref(false);
const commentToDelete = ref(null);

const props = defineProps({
  boardId: {
    type: Number,
    required: true,
  },
});

const authStore = useAuthStore();
const comments = ref([]);
const activeReplyId = ref(null);

const isAuthenticated = computed(() => authStore.user.isAuthenticated);

// 댓글 깊이 계산 함수는 유지
const getCommentDepth = (comment) => {
  let depth = 0;
  let currentComment = comment;

  while (currentComment.parentId) {
    depth++;
    currentComment = comments.value.find(
      (c) => c.id === currentComment.parentId
    );
    if (!currentComment) break;
  }

  return depth;
};

const fetchComments = async () => {
  try {
    const response = await http.get(`/comment/board/${props.boardId}`);
    const rawComments = response.data;

    // 댓글 트리 구조 만들기
    const commentMap = new Map();
    const rootComments = [];

    // 1. 모든 댓글을 Map에 저장
    rawComments.forEach((comment) => {
      commentMap.set(comment.id, { ...comment, children: [] });
    });

    // 2. 부모-자식 관계 설정
    rawComments.forEach((comment) => {
      if (comment.parentId) {
        const parentComment = commentMap.get(comment.parentId);
        if (parentComment) {
          parentComment.children.push(commentMap.get(comment.id));
        }
      } else {
        rootComments.push(commentMap.get(comment.id));
      }
    });

    // 3. 트리 구조를 평탄화하여 순서대로 배열로 변환
    const flattenComments = [];
    const flattenTree = (comment) => {
      flattenComments.push(comment);
      // 자식 댓글들을 ID 기준으로 정렬
      comment.children
        .sort((a, b) => a.id - b.id)
        .forEach((child) => {
          flattenTree(child);
        });
    };

    // 4. 최상위 댓글들을 ID 기준으로 정렬하고 각각의 트리를 평탄화
    rootComments
      .sort((a, b) => a.id - b.id)
      .forEach((rootComment) => {
        flattenTree(rootComment);
      });

    comments.value = flattenComments;
  } catch (error) {
  }
};

const showReplyForm = (id) => {
  activeReplyId.value = id;
};

const handleCommentAdded = () => {
  activeReplyId.value = null;
  fetchComments();
};

// handleDelete 함수 수정
const handleDelete = (id) => {
  commentToDelete.value = id;
  showDeleteConfirmAlert.value = true;
};

const confirmDelete = async () => {
  try {
    const token = authStore.getToken();
    await http.delete(`/comment/${commentToDelete.value}`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });

    // 삭제 성공
    showDeleteConfirmAlert.value = false;

    // 현재 댓글 목록에서 삭제된 댓글과 그 하위 댓글들을 즉시 제거
    comments.value = comments.value.filter((comment) => {
      const isDeletedComment = comment.id === commentToDelete.value;
      const isChildOfDeleted = findParentCommentIds(comment).includes(
        commentToDelete.value
      );
      return !isDeletedComment && !isChildOfDeleted;
    });

    // 서버에서 최신 데이터를 가져와 화면을 갱신
    await fetchComments();
  } catch (error) {
    showDeleteConfirmAlert.value = false;
    showDeleteFailAlert.value = true;
  } finally {
    commentToDelete.value = null;
  }
};

// 댓글의 모든 상위 댓글 ID를 찾는 함수 추가
const findParentCommentIds = (comment) => {
  const parentIds = [];
  let currentComment = comment;
  while (currentComment.parentId) {
    parentIds.push(currentComment.parentId);
    currentComment = comments.value.find(
      (c) => c.id === currentComment.parentId
    );
    if (!currentComment) break;
  }
  return parentIds;
};

const canDelete = (comment) => {
  return isAuthenticated.value && comment.userId === authStore.user.id;
};

const formatDate = (date) => {
  if (!date) return "";
  const dateObj = new Date(date);
  return dateObj.toLocaleDateString("ko-KR", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
  });
};

defineExpose({
  fetchComments,
});

onMounted(() => {
  fetchComments();
});
</script>
