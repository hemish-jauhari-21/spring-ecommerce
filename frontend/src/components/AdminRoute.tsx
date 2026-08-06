import { Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

function AdminRoute({
    children,
}: {
    children: React.ReactNode;
}) {
    const { user } = useAuth();

    if (!user) {
        return <Navigate to="/login" replace />
    }

    if (user.role !== "ADMIN") {
        return <Navigate to="/" replace />
    }

    return <>{children}</>;
}

export default AdminRoute;