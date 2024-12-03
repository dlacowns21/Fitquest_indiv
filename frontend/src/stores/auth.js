// stores/auth.js
import { defineStore } from "pinia";
import { ref } from "vue";
import axios from "axios";
import { useRouter } from "vue-router";
import http from "@/api/http";
import { disassemble, getChoseong } from "es-hangul";

export const useAuthStore = defineStore(
  "auth",
  () => {
    const router = useRouter();
    const showSignupSuccessAlert = ref(false);

    const user = ref({
      id: null,
      email: null,
      name: "",
      profileImage: "",
      description: "",
      isAuthenticated: false,
      isAdmin: 0,
    });

    const signup = async (userData) => {
      try {
        const temp = { ...userData };
        if (getChoseong(temp.name) === temp.name) {
          temp.name = temp.name + "," + disassemble(temp.name);
        } else {
          temp.name = temp.name + "," + getChoseong(temp.name);
        }

        await http.post("/user/regist", temp);
        showSignupSuccessAlert.value = true;
        return { success: true };
      } catch (error) {
        throw error;
      }
    };

    const handleSignupSuccess = () => {
      showSignupSuccessAlert.value = false;
      router.push("/login"); // alert 닫힌 후 로그인 페이지로 이동
    };

    const getToken = () => sessionStorage.getItem("accessToken");

    // 로그인 액션
    const login = async (loginData) => {
      try {
        const response = await http.post("/user/login", loginData, {
          withCredentials: true,
        });
        const data = response.data;
        if (!data.accessToken) {
          throw new Error("로그인 응답에 토큰이 없습니다.");
        }

        // 유저 정보 저장
        user.value = {
          id: data.id,
          email: data.email,
          name: data.name,
          profileImage: data.profileImage,
          description: data.description,
          isAuthenticated: true,
          isAdmin: data.isAdmin,
        };

        // 토큰 설정
        sessionStorage.setItem("accessToken", data.accessToken);
        axios.defaults.headers.common[
          "Authorization"
        ] = `Bearer ${data.accessToken}`;

        // 로그인 성공 후 라우터 히스토리 조작
        router.replace("/"); // replace를 사용하여 히스토리에서 로그인 페이지를 제거

        return { success: true };
      } catch (error) {
        let errorMessage = "아이디 또는 비밀번호가 올바르지 않습니다.";
        if (error.response) {
          if (error.response.status === 401) {
            errorMessage = "아이디 또는 비밀번호가 올바르지 않습니다.";
          } else {
            errorMessage =
              error.response.data.message ||
              "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
          }
        } else if (error.request) {
          errorMessage =
            "서버에 연결할 수 없습니다. 네트워크 연결을 확인해주세요.";
        }

        return { success: false, error: errorMessage };
      }
    };

    // 로그아웃
    const logout = () => {
      user.value = {
        id: null,
        email: null,
        name: "",
        profileImage: "",
        description: "",
        isAuthenticated: false,
        isAdmin: 0,
      };
      sessionStorage.clear();
      delete axios.defaults.headers.common["Authorization"];
      router.push("/");
    };

    // fetchUserInfo는 필요한 경우에만 명시적으로 호출
    const fetchUserInfo = async () => {
      if (!getToken() || !user.value.id) return;
      try {
        const response = await http.get(`/user/${user.value.id}`);
        if (response.data) {
          user.value = {
            ...response.data,
            isAuthenticated: true,
            isAdmin: response.data.isAdmin,
          };
        }
      } catch (error) {
        console.error("유저 정보 조회 실패:", error);
        if (error.response?.status === 401) {
          logout();
        }
      }
    };

    // 회원가입
    const regist = async (userData) => {
      try {
        const temp = { ...userData };
        if (getChoseong(temp.name) === temp.name) {
          temp.name = temp.name + "," + disassemble(temp.name);
        } else {
          temp.name = temp.name + "," + getChoseong(temp.name);
        }
        await http.post("/user/regist", temp);
        return { success: true, message: "회원가입이 완료되었습니다." };
      } catch (error) {
        return {
          success: false,
          message: "회원가입에 실패하였습니다.",
          error: error.response?.data?.message || "서버 오류가 발생했습니다.",
        };
      }
    };

    // 닉네임 중복 체크
    const checkDuplicateName = async (name) => {
      try {
        const response = await http.get(`/user/check-name/${name}`, {
          validateStatus: (status) =>
            (status >= 200 && status < 300) || status === 409,
        });
        return {
          isDuplicated: response.status === 409,
          message:
            response.status === 409
              ? "사용할 수 없는 닉네임입니다."
              : "사용 가능한 닉네임입니다.",
        };
      } catch (error) {
        console.error("닉네임 중복 체크 실패:", error);
        return {
          isDuplicated: true,
          message: "중복 확인 중 오류가 발생했습니다.",
        };
      }
    };

    // 이메일 중복 체크
    const checkDuplicateEmail = async (email) => {
      try {
        const response = await http.get(`/user/check-email/${email}`, {
          validateStatus: (status) =>
            (status >= 200 && status < 300) || status === 409,
        });
        return {
          isDuplicated: response.status === 409,
          message:
            response.status === 409
              ? "사용할 수 없는 아이디입니다."
              : "사용 가능한 아이디입니다.",
        };
      } catch (error) {
        console.error("이메일 중복 체크 실패:", error);
        return {
          isDuplicated: true,
          message: "중복 확인 중 오류가 발생했습니다.",
        };
      }
    };

    const validateAccessToken = () => {
      const token = getToken();
      if (!token) return false;

      // JWT 토큰 만료 시간 확인 로직
      try {
        const payload = JSON.parse(atob(token.split(".")[1]));
        return payload.exp * 1000 > Date.now();
      } catch {
        return false;
      }
    };

    // axios 인터셉터 추가
    http.interceptors.response.use(
      (response) => response,
      async (error) => {
        const originalRequest = error.config;

        if (error.response.status === 401 && !originalRequest._retry) {
          originalRequest._retry = true;

          const refreshed = await refreshToken();
          if (refreshed) {
            originalRequest.headers[
              "Authorization"
            ] = `Bearer ${sessionStorage.getItem("accessToken")}`;
            return http(originalRequest);
          }
        }
        return Promise.reject(error);
      }
    );

    const checkAuth = async () => {
      const hasRefreshToken = await checkRefreshTokenExists();
      if (!hasRefreshToken) {
        return false;
      }
      const accessToken = getToken();
      if (!accessToken || !validateAccessToken()) {
        return false;
      }
      return true;
    };

    const checkRefreshTokenExists = async () => {
      try {
        const response = await http.get(
          "/user/check-refresh-token",
          {
            withCredentials: true,
          },
          {
            skipLoading: true,
          }
        );
        return response.data.exists;
      } catch (error) {
        console.error("리프레시 토큰 확인 실패:", error);
        return false;
      }
    };

    return {
      user,
      login,
      logout,
      fetchUserInfo,
      getToken,
      regist,
      checkDuplicateName,
      checkDuplicateEmail,
      validateAccessToken,
      checkRefreshTokenExists,
      checkAuth,
      signup,
      showSignupSuccessAlert,
      handleSignupSuccess,
    };
  },
  {
    persist: {
      storage: sessionStorage,
      paths: ["user"],
    },
  }
);
