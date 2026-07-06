import type { AuthResponse } from "../types/AuthResponse";
import type { LoginRequest } from "../types/LoginRequest";
import type { RegisterRequest } from "../types/RegisterRequest";
import api from "./api";

class AuthService {
    async login(request: LoginRequest): Promise<AuthResponse> {
        const response = await api.post<AuthResponse>("/auth/login", request);
        return response.data;
    }

    async register(request: RegisterRequest): Promise<AuthResponse> {
        const response = await api.post<AuthResponse>("/auth/register", request);
        return response.data;
    }
}

export default new AuthService();