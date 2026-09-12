import {
    useCallback,
    useEffect,
    useState,
    type ReactNode
} from "react";

import { AuthContext } from "./AuthContextInstance";

import type { AuthResponse } from "../types/AuthResponse";

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
