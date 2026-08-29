import api from "./api";

export interface UserProfile {
    id: number;
    name: string;
    email: string;
    role: string;
}

export interface UserUpdateRequest {
    name: string;
    email: string;
    password?: string;
}

class UserService {
    async getMyProfile(): Promise<UserProfile> {
        const response = await api.get<UserProfile>("/user/me");
        return response.data;
    }

    async updateMyProfile(data: UserUpdateRequest): Promise<UserProfile> {
        const response = await api.put<UserProfile>("/user/me", data);
        return response.data;
    }
}

export default new UserService();
