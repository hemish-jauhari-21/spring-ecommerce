import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { useState } from "react";
import AuthService from "../services/AuthService";


function Login() {

    const navigate = useNavigate();
    const { login } = useAuth();
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            const response = await AuthService.login({email, password});
            login(response);
            alert("Login successful");
            navigate("/");
        }
        catch (error) {
            console.error("Login failed:", error);
            alert("Login failed. Please check your credentials and try again.");
        }
    }

    return (
        <>
            <div className="container mt-5">
                <h2>Login Page</h2>
                
                <form onSubmit={handleSubmit}>
                    <div className="mb-3">
                        <label>Email</label>
                        <input type="email" className="form-control" value={email} onChange={(e) => setEmail(e.target.value)} />
                    </div>

                    <div className="mb-3">
                        <label>Password</label>
                        <input type="password" className="form-control" value={password} onChange={(e) => setPassword(e.target.value)} />
                    </div>

                    <button type="submit" className="btn btn-primary">Login</button>
                </form>

                <div className="mt-3">
                    <Link to="/register">Don't have an account? Register</Link>
                </div>
            </div>
        </>
    );
}

export default Login;