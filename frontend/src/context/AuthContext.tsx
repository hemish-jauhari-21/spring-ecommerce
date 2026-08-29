import {
    createContext,
    useCallback,
    useContext,
    useEffect,
    useState,
    type ReactNode
} from "react";

import type { AuthResponse } from "../types/AuthResponse";

interface AuthContextType {

    user: AuthResponse | null;

    login: (user: AuthResponse) => void;

    logout: () => void;

    updateProfile: (user: AuthResponse) => void;

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

    const logout = useCallback(() => {

        localStorage.removeItem("token");

        localStorage.removeItem("user");

        setUser(null);

    }, []);

    const updateProfile = (userData: AuthResponse) => {

        localStorage.setItem("user", JSON.stringify(userData));

        setUser(userData);

    };

    useEffect(() => {

        const handleSessionExpired = () => {
            logout();
        };

        window.addEventListener("auth:expired", handleSessionExpired);

        return () => {
            window.removeEventListener("auth:expired", handleSessionExpired);
        };

    }, [logout]);

    return (

        <AuthContext.Provider
            value={{
                user,
                login,
                logout,
                updateProfile,
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