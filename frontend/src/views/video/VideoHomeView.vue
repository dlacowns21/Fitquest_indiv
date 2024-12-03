<template>
  <div class="fixed top-0 left-0 right-0 z-[100]">
    <div class="max-w-[950px] mx-auto px-5 md:px-4">
      <VideoHeader :exercise-tags="exerciseTags" @search="(query) => handleSearch(query)"
        @tagSelect="(playlistId) => handleTagSelect(playlistId)" />
    </div>
  </div>
  <div class="h-[calc(100vh-4rem)]">
    <div ref="scrollContainer" class="h-full overflow-y-auto rounded-[15px]" @scroll="handleScroll">
      <div class="grid grid-cols-1 gap-6 p-4">
        <div v-for="item in displayedVideos" :key="item.id"
          class="bg-white rounded-xl shadow-lg hover:shadow-xl transition-all duration-300 cursor-pointer overflow-hidden transform hover:-translate-y-1"
          @click="openVideo(item.id)">
          <div class="flex flex-col md:flex-row">
            <div v-if="item.thumbnail" class="relative w-[full] md:w-[320px] overflow-hidden" style="aspect-ratio: 16/9;">
              <img :src="item.thumbnail"
                class="w-full h-full object-cover transition-transform duration-300 hover:scale-105" loading="lazy"
                :alt="item.title" decoding="async" />
            </div>
            <div class="p-4 flex-1">
              <h3 class="font-bold text-gray-900 text-lg mb-2 line-clamp-2">{{ item.title }}</h3>
              <div class="space-y-2">
                <div class="text-sm text-gray-600">{{ formatDate(item.publishedAt) }}</div>
                <div class="text-sm text-gray-600">{{ item.channelTitle }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-if="isLoading" class="text-center py-8">
        <div class="inline-block animate-spin rounded-full h-10 w-10 border-4 border-gray-300 border-t-blue-600"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { getPlaylistVideos } from '@/api/news';
import VideoHeader from "@/components/common/VideoHeader.vue";

// 운동 태그 정의를 다시 이쪽으로 이동
const exerciseTags = [
  { id: 1, name: '어깨', playlistId: 'PLCG5Keu9l5z8bIqRFc7GDphTd5p2TpLNb' },
  { id: 2, name: '등', playlistId: 'PLCG5Keu9l5z8F7E5DQriYOPo8OudcCcXt' },
  { id: 3, name: '가슴', playlistId: 'PLyrJujskXmubmI-wvGYziw143_dx8ZwFn' },
  { id: 4, name: '하체', playlistId: 'PLCG5Keu9l5z8cmwK46wyl2vep47iqoFfd' },
  { id: 5, name: '전신', playlistId: 'PLMbwxKzLr-eVylPkh4hk-99W0kORbby1X' },
  { id: 6, name: '맨몸운동', playlistId: 'PLCG5Keu9l5z9Vp50xV5BbuC0zjd7jHxRp' },
  { id: 7, name: '운동 플레이리스트', playlistId: 'PLCG5Keu9l5z_6H9rkouuUBGFPi8Yz0-ke' },
  { id: 8, name: '운동 정보', playlistId: 'PLjfaq5HQzMgb4KUBU4OVNoHjn_LBDouUN' }
];

const videosByTag = ref({
  all: [],
  'PLCG5Keu9l5z8bIqRFc7GDphTd5p2TpLNb': [], // 어깨
  'PLCG5Keu9l5z8F7E5DQriYOPo8OudcCcXt': [], // 등
  'PLyrJujskXmubmI-wvGYziw143_dx8ZwFn': [], // 가슴
  'PLCG5Keu9l5z8cmwK46wyl2vep47iqoFfd': [], // 하체
  'PLMbwxKzLr-eVylPkh4hk-99W0kORbby1X': [], // 전신
  'PLCG5Keu9l5z9Vp50xV5BbuC0zjd7jHxRp': [], // 맨몸운동
  'PLCG5Keu9l5z_6H9rkouuUBGFPi8Yz0-ke': [], // 운동 플레이리스트
  'PLjfaq5HQzMgb4KUBU4OVNoHjn_LBDouUN': [], // 운동 정보
});

const displayCount = ref(30);
const isLoading = ref(false);
const searchQuery = ref('all');
const hasMore = ref(true);
const scrollContainer = ref(null);

// displayedVideos computed 속성 수정
const displayedVideos = computed(() => {
  return videosByTag.value[searchQuery.value].slice(0, displayCount.value);
});

// 모든 플레이리스트 로드 함수 수정
const loadAllPlaylists = async () => {
  try {
    isLoading.value = true;

    const promises = exerciseTags.map(tag => getPlaylistVideos(tag.playlistId));
    const responses = await Promise.all(promises);

    responses.forEach((response, index) => {
      if (response.items && response.items.length > 0) {
        const mappedVideos = response.items
          // private 비디오 필터링
          .filter(item => 
            item.snippet.title !== 'Private video' && 
            item.snippet.title !== 'Deleted video' &&
            item.snippet.thumbnails && // 썸네일이 없는 경우도 제외
            Object.keys(item.snippet.thumbnails).length > 0
          )
          .map(item => ({
            id: item.snippet.resourceId.videoId,
            title: item.snippet.title,
            description: item.snippet.description,
            thumbnail: item.snippet.thumbnails.high?.url || item.snippet.thumbnails.medium?.url,
            channelTitle: item.snippet.videoOwnerChannelTitle,
            publishedAt: item.contentDetails.videoPublishedAt
          }));

        // 각 태그별 배열에 저장
        const playlistId = exerciseTags[index].playlistId;
        videosByTag.value[playlistId] = shuffleArray([...mappedVideos]);

        // 전체 배열에도 추가
        videosByTag.value.all.push(...mappedVideos);
      }
    });

    // 전체 배열 셔플
    videosByTag.value.all = shuffleArray(videosByTag.value.all);

  } catch (error) {
    console.error('비디오 로드 실패:', error);
  } finally {
    isLoading.value = false;
  }
};

// handleSearch 함수 수정
const handleSearch = (playlistId) => {
  searchQuery.value = playlistId;
  displayCount.value = 30;  // 표시 개수 초기화
};

// loadMore 함수 수정
const loadMore = () => {
  const currentVideos = videosByTag.value[searchQuery.value];
  if (displayCount.value < currentVideos.length) {
    displayCount.value += 18;
  }
};

// 스크롤 핸들러
const handleScroll = (e) => {
  const element = e.target;
  if (
    element.scrollHeight - element.scrollTop <= element.clientHeight + 100 &&
    !isLoading.value
  ) {
    loadMore();
  }
};

const openVideo = (videoId) => {
  // 현재 비디오 정보 찾기
  const video = videosByTag.value[searchQuery.value].find(v => v.id === videoId);
  
  // 최근 본 비디오에 저장
  if (video) {
    let recentVideos = JSON.parse(localStorage.getItem("recentVideos") || "[]");
    recentVideos = recentVideos.filter(v => v.id !== video.id);
    recentVideos.unshift(video);
    recentVideos = recentVideos.slice(0, 10); // 최대 10개만 저장
    localStorage.setItem("recentVideos", JSON.stringify(recentVideos));
  }
  
  window.open(`https://www.youtube.com/watch?v=${videoId}`, '_blank');
};

const formatDate = (dateStr) => {
  if (!dateStr) return "";
  try {
    const date = new Date(dateStr);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    return `${year}-${month}-${day}`;
  } catch (e) {
    return "";
  }
};

// Fisher-Yates 셔플 알고리즘 구현
const shuffleArray = (array) => {
  const newArray = [...array];  // 원본 배열을 변경하지 않기 위해 복사
  for (let i = newArray.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [newArray[i], newArray[j]] = [newArray[j], newArray[i]];  // 요소 위치 교환
  }
  return newArray;
};

// handleTagSelect 함수 추가
const handleTagSelect = (playlistId) => {
  searchQuery.value = playlistId;
  displayCount.value = 30;
};

onMounted(async () => {
  await loadAllPlaylists();
});
</script>

<style scoped>
.break-inside-avoid {
  break-inside: avoid;
}

/* 태그 스타일 추가 */
.tag-container {
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.tag-container::-webkit-scrollbar {
  display: none;
}
</style>
