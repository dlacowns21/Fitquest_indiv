import { defineStore } from "pinia";
import { ref } from "vue";
import { useAuthStore } from "./auth";
import http from "@/api/http";

export const useFollowStore = defineStore("follow", () => {
    const isFollowing = ref(false);
    const authStore = useAuthStore();
    const needsRefresh = ref(false);

    function setNeedsRefresh(value) {
        needsRefresh.value = value;
    }

    const fetchIsFollowing = async (userId) => {
        const isAuth = await authStore.checkAuth();
        if(!isAuth) {
            return;
        }
        const accessToken = authStore.getToken();
        try {
            const response = await http.get(`/follow/status/${authStore.user.id}/${userId}`, {
                headers: {
                    Authorization: `Bearer ${accessToken}`,
                },
            });
            return response.data;
        } catch (error) {
            console.error("팔로잉 상태 조회 실패:", error);
        }
    };

    const fetchFollowers = async (userId) => {
        const accessToken = authStore.getToken();
        try {
            const response = await http.get(`/follow/followers/${userId}`, {
                headers: {
                    Authorization: `Bearer ${accessToken}`,
                },
            });
            return response.data;
        } catch (error) {
            console.error("팔로워 조회 실패:", error);
        }
    }

    const fetchFollowings = async (userId) => {
        const accessToken = authStore.getToken();
        try {
            const response = await http.get(`/follow/followings/${userId}`, {
                headers: {
                    Authorization: `Bearer ${accessToken}`,
                },
            });
            return response.data;
        } catch (error) {
            console.error("팔로잉 조회 실패:", error);
        }
    }

    const fetchFollow = async (userId) => {
        const accessToken = authStore.getToken();
        try {
            const follow = {
                followerId: authStore.user.id,
                followingId: userId
            }
            await http.post(`/follow`, follow, {
                headers: {
                    Authorization: `Bearer ${accessToken}`,
                },
            });
            await Promise.all([
                fetchIsFollowing(userId),
                fetchFollowers(userId),
                fetchFollowings(userId)
            ]);
        } catch (error) {
            console.error("팔로우 실패:", error);
        }
    }

    const fetchUnfollow = async (userId) => {
        try {
            await http.delete(`/follow/${authStore.user.id}/${userId}`);
            await Promise.all([
                fetchIsFollowing(userId),
                fetchFollowers(userId),
                fetchFollowings(userId)
            ]);
        } catch (error) {
            console.error('언팔로우 실패:', error);
        }
    };

    return {
        needsRefresh,
        setNeedsRefresh,
        isFollowing,
        fetchIsFollowing,
        fetchFollowers,
        fetchFollowings,
        fetchFollow,
        fetchUnfollow
    };
});
