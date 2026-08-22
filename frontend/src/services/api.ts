import axios, { AxiosError } from 'axios';
import { toast } from 'react-toastify';

const api = axios.create({
    baseURL: "http://localhost:8091/api/v1/ecommerce",
    headers: { "Content-Type": "application/json" }
});

api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem("token");
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
)

interface ApiErrorResponse {
    message?: string;
    validationErrors?: Record<string, string>;
}

interface ExtendedAxiosError extends AxiosError<ApiErrorResponse> {
    isHandled?: boolean;
}

function isAuthEndpoint(url?: string): boolean {
    if (!url) return false;
    return url.includes("/auth/login") || url.includes("/auth/register");
}

function clearAuthStorage() {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
}

api.interceptors.response.use(
    (response) => response,
    (error: ExtendedAxiosError) => {
        const status = error.response?.status;
        const url = error.config?.url;

        // 401 (session expired / invalid token): clear auth, notify, let route guards redirect
        if (status === 401 && !isAuthEndpoint(url)) {
            clearAuthStorage();
            window.dispatchEvent(new Event("auth:expired"));
            toast.error("Your session has expired. Please log in again.");
            error.isHandled = true;
        }

        return Promise.reject(error);
    }
)

export function getErrorMessage(
    error: unknown,
    fallback = "Something went wrong. Please try again later."
): string {
    if (axios.isAxiosError<ApiErrorResponse>(error)) {
        const status = error.response?.status;
        const data = error.response?.data;

        if (status === 500) {
            return "Something went wrong. Please try again later.";
        }

        if (data?.message) {
            return data.message;
        }

        if (data?.validationErrors) {
            const messages = Object.values(data.validationErrors);
            if (messages.length > 0) {
                return messages.join(" ");
            }
        }
    }

    return fallback;
}

export function notifyError(
    error: unknown,
    fallback = "Something went wrong. Please try again later."
): string {
    const message = getErrorMessage(error, fallback);
    const handled = (error as { isHandled?: boolean })?.isHandled === true;

    if (!handled) {
        toast.error(message);
    }

    return message;
}

export default api;