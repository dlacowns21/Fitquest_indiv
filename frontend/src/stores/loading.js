import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useLoadingStore = defineStore('loading', () => {
  const isLoading = ref(false);
  const loadingCount = ref(0);
  const loadingTimeout = ref(null);
  const isRouteLoading = ref(false);

  const show = (isRoute = false) => {
    if (isRoute) {
      isRouteLoading.value = true;
      isLoading.value = true;
    } else {
      loadingCount.value++;
      if (!isRouteLoading.value) {
        isLoading.value = true;
      }
    }

    if (loadingTimeout.value) clearTimeout(loadingTimeout.value);
    loadingTimeout.value = setTimeout(() => {
      hide(true);
    }, 10000);
  };

  const hide = (force = false) => {
    if (force) {
      loadingCount.value = 0;
      isLoading.value = false;
      isRouteLoading.value = false;
      if (loadingTimeout.value) {
        clearTimeout(loadingTimeout.value);
        loadingTimeout.value = null;
      }
      return;
    }

    if (isRouteLoading.value) {
      isRouteLoading.value = false;
      isLoading.value = loadingCount.value > 0;
    } else {
      loadingCount.value--;
      if (loadingCount.value <= 0) {
        loadingCount.value = 0;
        isLoading.value = false;
      }
    }

    if (loadingTimeout.value) {
      clearTimeout(loadingTimeout.value);
      loadingTimeout.value = null;
    }
  };

  return {
    isLoading,
    loadingCount,
    show,
    hide,
  };
}); 