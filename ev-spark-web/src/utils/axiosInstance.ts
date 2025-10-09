import axios from "axios";

const axiosInstance = axios.create({
  baseURL: "/api",
  headers: {
    "Content-Type": "application/json",
    Accept: "application/json",
  },
});

axiosInstance.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("authToken");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

axiosInstance.interceptors.response.use(
  (response) => response,
  (error) => {
    const errorMessage =
      error.response?.data?.message || "Something went wrong.";

    alert(errorMessage);

    if (error.response?.status === 401) {
      localStorage.removeItem("authToken");

      window.location.href = "http://localhost:5173/auth/login";
    }

    return Promise.reject(error);
  }
);

export default axiosInstance;
