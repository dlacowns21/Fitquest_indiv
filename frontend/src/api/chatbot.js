import http from '@/api/http';

export const chatbotApi = {
  // 일반 대화
  generateResponse: async (message) => {
    try {
      const response = await http.get(`/chatbot/ai/generate`, {
        params: { message }
      });
      return response.data;
    } catch (error) {
      console.error('챗봇 응답 오류:', error);
      throw error;
    }
  },

  // 운동 추천 받기 
  getWorkoutRecommendation: async (userId) => {
    try {
      const response = await http.get(`/chatbot/workout/recommend/${userId}`);
      return response.data;
    } catch (error) {
      console.error('운동 추천 오류:', error);
      throw error;
    }
  }
}; 