<template>
  <div class="fixed top-0 left-0 right-0 z-[100]">
    <div class="max-w-[950px] mx-auto px-5 md:px-4">
      <NewsHeader @search="(query) => handleSearch(query)" />
    </div>
  </div>
  <div class="h-[calc(100vh-4rem)]">
    <div ref="scrollContainer" class="h-full overflow-y-auto rounded-[15px]" @scroll="handleScroll">
      <masonry-wall :items="newsItems" :column-width="300" :gap="16" class="px-4">
        <template #default="{ item }">
          <div
            class="bg-gray-50 rounded-xl shadow-lg hover:shadow-xl transition-shadow duration-300 cursor-pointer overflow-hidden flex flex-col"
            @click="openNews(item)">
            <div v-if="item.thumbnail" class="w-full overflow-hidden" :style="{
              aspectRatio: `${item.imageWidth}/${item.imageHeight}`,
            }">
              <img :src="item.thumbnail" @error="handleImageError($event, item)" class="w-full h-full object-cover"
                loading="lazy" :alt="item.title" decoding="async" :width="item.imageWidth" :height="item.imageHeight" />
            </div>
            <div class="p-2 h-2/5">
              <h3 class="font-semibold text-gray-800 text-sm truncate" v-html="item.title"></h3>
              <div class="flex items-center justify-between mt-1">
                <span class="text-xs text-gray-600">{{
                  formatDate(item.postdate)
                }}</span>
              </div>
            </div>
          </div>
        </template>
      </masonry-wall>

      <div v-if="isLoading" class="text-center py-4">
        <div class="inline-block animate-spin rounded-full h-8 w-8 border-4 border-gray-300 border-t-blue-600"></div>
      </div>

      <div v-if="!hasMore && !isLoading" class="text-center py-4 text-gray-500">
        더 이상 표시할 내용이 없습니다
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from "vue";
import { searchBlogWithImages } from "@/api/news";
import NewsHeader from "@/components/common/NewsHeader.vue";
  
const searchQuery = ref("운동");
const imageItems = ref([]);
const usedImageIndices = ref(new Set());
const newsItems = ref([]);
const currentPage = ref(1);
const isLoading = ref(false);
const hasMore = ref(true);
const scrollContainer = ref(null);

const INITIAL_LOAD_COUNT = 50;
const MORE_LOAD_COUNT = 30;

const handleScroll = async (e) => {
  const element = e.target;
  if (
    element.scrollHeight - element.scrollTop <= element.clientHeight + 100 &&
    !isLoading.value &&
    hasMore.value
  ) {
    await loadMore();
  }
};

const getUniqueRandomImage = () => {
  if (imageItems.value.length === 0) return null;
  if (usedImageIndices.value.size >= imageItems.value.length) {
    usedImageIndices.value.clear();
  }
  let randomIndex;
  do {
    randomIndex = Math.floor(Math.random() * imageItems.value.length);
  } while (usedImageIndices.value.has(randomIndex));

  usedImageIndices.value.add(randomIndex);
  return imageItems.value[randomIndex];
};

const handleSearch = async (query) => {
  try {
    isLoading.value = true;
    currentPage.value = 1;
    hasMore.value = true;

    const response = await searchBlogWithImages(query, 1, INITIAL_LOAD_COUNT);
    const { blog, images } = response;
    
    // 이미지 배열 저장
    imageItems.value = images || [];
    usedImageIndices.value.clear();

    const updatedItems = blog.items.map((item, index) => {
      // HTML 태그 제거
      item.title = item.title.replace(/<\/?[^>]+(>|$)/g, "");
      // HTML 엔티티 디코딩
      item.title = decodeHtmlEntities(item.title);
      
      // 이미지 정보 가져오기
      const imageData = imageItems.value[index % imageItems.value.length];
      
      return {
        ...item,
        thumbnail: imageData?.thumbnail || 'https://placehold.co/400x300/e2e8f0/475569?text=No+Image',
        imageWidth: imageData?.sizewidth || 400,
        imageHeight: imageData?.sizeheight || 300,
      };
    });

    newsItems.value = updatedItems;
  } catch (error) {
    console.error("검색 실패:", error);
  } finally {
    isLoading.value = false;
  }
};

const loadMore = async () => {
  if (isLoading.value) return;

  try {
    isLoading.value = true;
    const nextPage = currentPage.value + 1;
    const start = (nextPage - 1) * MORE_LOAD_COUNT + 1;

    const blogResponse = await searchBlogWithImages(
      searchQuery.value,
      start,
      MORE_LOAD_COUNT
    );

    if (!blogResponse.items || blogResponse.items.length === 0) {
      hasMore.value = false;
      return;
    }

    const newItems = blogResponse.items.map((item, index) => {
      const uniqueImage = getUniqueRandomImage();
      const imageInfo = uniqueImage || {
        thumbnail: `https://picsum.photos/400/300?random=${newsItems.value.length + index
          }`,
        width: 400,
        height: 300,
      };

      return {
        ...item,
        id: `${item.link}_${Date.now()}_${newsItems.value.length + index}`,
        title: decodeHtmlEntities(item.title),
        postdate: item.postdate,
        thumbnail: imageInfo.thumbnail,
        imageWidth: imageInfo.width,
        imageHeight: imageInfo.height,
      };
    });

    await nextTick(() => {
      newsItems.value = [...newsItems.value, ...newItems];
    });

    currentPage.value = nextPage;
  } catch (error) {
    console.error("추가 데이터 로드 실패:", error);
  } finally {
    isLoading.value = false;
  }
};

const decodeHtmlEntities = (text) => {
  const textArea = document.createElement("textarea");
  textArea.innerHTML = text;
  return textArea.value;
};

const formatDate = (dateStr) => {
  if (!dateStr) return "";

  if (/^\d{8}$/.test(dateStr)) {
    const year = dateStr.substring(0, 4);
    const month = dateStr.substring(4, 6);
    const day = dateStr.substring(6, 8);
    return `${year}-${month}-${day}`;
  }

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

const handleImageError = (event, item) => {
  const fallbackImage = "https://placehold.co/400x300/e2e8f0/475569?text=No+Image";

  if (item.thumbnail === fallbackImage) return;

  const uniqueImage = getUniqueRandomImage();
  if (uniqueImage && !uniqueImage.thumbnail.includes('istockphoto')) {
    item.thumbnail = uniqueImage.thumbnail;
    item.imageWidth = uniqueImage.width;
    item.imageHeight = uniqueImage.height;
  } else {
    item.thumbnail = fallbackImage;
    item.imageWidth = 400;
    item.imageHeight = 300;
  }
};

const openNews = (news) => {
  // 최근 본 카드뉴스 저장
  const recentNews = JSON.parse(localStorage.getItem("recentNews") || "[]");

  // HTML 태그 제거를 위한 임시 element 생성
  const tempDiv = document.createElement('div');
  tempDiv.innerHTML = news.title;
  const cleanTitle = tempDiv.textContent || tempDiv.innerText;

  // 날짜 형식 통일
  const formattedDate = formatDate(news.postdate);

  // 중복 제거를 위해 같은 링크의 뉴스가 있다면 제거
  const filteredNews = recentNews.filter((item) => item.link !== news.link);

  // 새로운 뉴스 추가 (최대 10개까지만 저장)
  filteredNews.unshift({
    id: news.id,
    title: cleanTitle,  // HTML 태그가 제거된 제목
    link: news.link,
    postdate: formattedDate  // YYYY-MM-DD 형식으로 통일된 날짜
  });

  // 최대 10개만 유지
  if (filteredNews.length > 10) {
    filteredNews.pop();
  }

  // localStorage에 저장
  localStorage.setItem("recentNews", JSON.stringify(filteredNews));

  // 새 탭에서 뉴스 열기
  window.open(news.link, "_blank");
};

onMounted(() => {
  handleSearch(searchQuery.value);
});
</script>

<style scoped>
.break-inside-avoid {
  break-inside: avoid;
}
</style>
