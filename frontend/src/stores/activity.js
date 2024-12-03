import { defineStore } from 'pinia';
import { ref } from 'vue';
import http from '@/api/http';

export const useActivityStore = defineStore('activity', () => {
    const activities = ref({});

    const fetchActivities = async (userId, year) => {
        try {
            const response = await http.get(`/activity/${year}/${userId}`);
            activities.value = {
                ...activities.value,
                ...response.data
            };
        } catch (error) {
            console.error('활동 데이터 조회 실패:', error);
        }
    };

    const fetchUpdateDailyActivity = async (date, userId) => {
        try {
            const response = await http.put(`/activity/daily/${date}/${userId}`);
            activities.value[date] = response.data;
        } catch (error) {
            console.error('일일 활동 데이터 업데이트 실패:', error);
        }
    };

    return {
        activities,
        fetchActivities,
        fetchUpdateDailyActivity
    };
}); 