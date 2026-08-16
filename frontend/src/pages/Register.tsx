import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import AuthService from "../services/AuthService";

const PASSWORD_PATTERN = /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$/;
const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

function Register() {

    const navigate = useNavigate();

    const [name, setName] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [errors, setErrors] = useState<{ [key: string]: string }>({});
    const [serverError, setServerError] = useState("");

    const validate = () => {

        const newErrors: { [key: string]: string } = {};

        if (!name.trim()) {
            newErrors.name = "Name is required.";
        }

        if (!email.trim()) {
            newErrors.email = "Email is required.";
        } else if (!EMAIL_PATTERN.test(email.trim())) {
            newErrors.email = "Please enter a valid email address.";
        }

        if (!password) {
            newErrors.password = "Password is required.";
        } else {
            if (password.length < 4 || password.length > 10) {
                newErrors.password = "Password must be between 4 and 10 characters.";
            } else if (!PASSWORD_PATTERN.test(password)) {
                newErrors.password = "Password must contain at least one digit, lowercase, uppercase, and special character.";
            }
        }

        if (!confirmPassword) {
            newErrors.confirmPassword = "Please confirm your password.";
        } else if (confirmPassword !== password) {
            newErrors.confirmPassword = "Passwords do not match.";
        }

        return newErrors;
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        const newErrors = validate();
        setErrors(newErrors);
        setServerError("");

        if (Object.keys(newErrors).length > 0) {
            return;
        }

        try {
            await AuthService.register({ name: name.trim(), email: email.trim(), password });
            alert("Registration successful. Please login.");
            navigate("/login");
        }
        catch (error: unknown) {
            console.error("Registration failed:", error);

            const status = (error as { response?: { status?: number } })?.response?.status;
            const message = (error as { response?: { data?: { message?: string } } })?.response?.data?.message;

            if (status === 409) {
                setServerError("Email is already registered.");
            } else {
                setServerError(message || "Registration failed. Please try again.");
            }
        }
    };

    return (
        <>
            <div className="container mt-5">
                <div className="row justify-content-center">
                    <div className="col-md-6">
                        <h2>Register Page</h2>

                        <form onSubmit={handleSubmit} noValidate>
                            <div className="mb-3">
                                <label>Name</label>
                                <input
                                    type="text"
                                    className="form-control"
                                    value={name}
                                    onChange={(e) => setName(e.target.value)}
                                />
                                {errors.name && <div className="text-danger small">{errors.name}</div>}
                            </div>

                            <div className="mb-3">
                                <label>Email</label>
                                <input
                                    type="email"
                                    className="form-control"
                                    value={email}
                                    onChange={(e) => setEmail(e.target.value)}
                                />
                                {errors.email && <div className="text-danger small">{errors.email}</div>}
                            </div>

                            <div className="mb-3">
                                <label>Password</label>
                                <input
                                    type="password"
                                    className="form-control"
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                />
                                {errors.password && <div className="text-danger small">{errors.password}</div>}
                            </div>

                            <div className="mb-3">
                                <label>Confirm Password</label>
                                <input
                                    type="password"
                                    className="form-control"
                                    value={confirmPassword}
                                    onChange={(e) => setConfirmPassword(e.target.value)}
                                />
                                {errors.confirmPassword && <div className="text-danger small">{errors.confirmPassword}</div>}
                            </div>

                            {serverError && <div className="alert alert-danger">{serverError}</div>}

                            <button type="submit" className="btn btn-primary">Register</button>
                        </form>

                        <div className="mt-3">
                            <Link to="/login">Already have an account? Login</Link>
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
}

export default Register;
