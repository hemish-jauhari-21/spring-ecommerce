import {
    createContext,
    useContext,
    useState,
    type ReactNode
} from "react";

import type { AuthResponse } from "../types/AuthResponse";

interface AuthContextType {

    user: AuthResponse | null;

    login: (user: AuthResponse) => void;

    logout: () => void;

    isAuthenticated: boolean;

}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {

    const [user, setUser] = useState<AuthResponse | null>(() => {
        const savedUser = localStorage.getItem("user");
        return savedUser ? JSON.parse(savedUser) : null;
    });

    const login = (userData: AuthResponse) => {

        localStorage.setItem("token", userData.token);

        localStorage.setItem("user", JSON.stringify(userData));

        setUser(userData);

    };

    const logout = () => {

        localStorage.removeItem("token");

        localStorage.removeItem("user");

        setUser(null);

    };

    return (

        <AuthContext.Provider
            value={{
                user,
                login,
                logout,
                isAuthenticated: !!user
            }}
        >

            {children}

        </AuthContext.Provider>

    );

}

export function useAuth() {

    const context = useContext(AuthContext);

    if (!context) {

        throw new Error("useAuth must be used inside AuthProvider");

    }

    return context;

}