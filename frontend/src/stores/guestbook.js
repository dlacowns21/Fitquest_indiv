import { defineStore } from "pinia";
import { ref } from "vue";
import http from "@/api/http";
import { useAuthStore } from "./auth";

export const useGuestbookStore = defineStore("guestbook", () => {
    const authStore = useAuthStore();

    const fetchGuestbook = async (userId) => {
        try {
            const response = await http.get(`/guestbook/${userId}`);
            const guestbooks = response.data;
            guestbooks.forEach((guestbook) => {
                guestbook.user.profileImage = `${http.defaults.baseURL}/user${guestbook.user.profileImage}`;
            });
            return guestbooks;
        } catch (error) {
            throw error;
        }
    };

    const fetchUserGuestbook = async (userId) => {
        try {
            const response = await http.get(`/user/${userId}`);
            return response.data;
        } catch (error) {
            throw error;
        }
    };

    const fetchInsertGuestbook = async (guestbook) => {
        try {
            const response = await http.post(`/guestbook`, guestbook);
            return response.data;
        } catch (error) {
            throw error;
        }
    };

    const fetchDeleteGuestbook = async (guestbookId) => {
        try {
            const response = await http.delete(`/guestbook/${guestbookId}`);
            return response.data;
        } catch (error) {
            throw error;
        }
    };

    return {
        fetchGuestbook,
        fetchUserGuestbook,
        fetchInsertGuestbook,
        fetchDeleteGuestbook
    };
});
