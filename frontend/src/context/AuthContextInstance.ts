import { createContext } from "react";
import type { AuthResponse } from "../types/AuthResponse";

export interface AuthContextType {

    user: AuthResponse | null;

    login: (user: AuthResponse) => void;

    logout: () => void;

    updateProfile: (user: AuthResponse) => void;

    isAuthenticated: boolean;

}

export const AuthContext = createContext<AuthContextType | undefined>(undefined);
