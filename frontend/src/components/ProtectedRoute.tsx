import { Navigate } from "react-router-dom";
import type { JSX } from "react/jsx-runtime";
import { useAuth } from "../context/AuthContext";

function ProtectedRoute ({children} : {children: JSX.Element}) {
    const { user } = useAuth();

    if (!user) {
        return <Navigate to="/login" />
    }

    return children;
}

export default ProtectedRoute;