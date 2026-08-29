import { useEffect, useState } from "react";
import UserService from "../services/UserService";
import { notifyError } from "../services/api";
import { toast } from "react-toastify";
import { useAuth } from "../context/AuthContext";

type FormErrors = Partial<Record<string, string>>;

interface FormData {
    name: string;
    email: string;
    password: string;
    confirmPassword: string;
}

function validate(form: FormData, isPasswordProvided: boolean): FormErrors {
    const errors: FormErrors = {};

    if (!form.name.trim()) {
        errors.name = "Name is required.";
    }

    if (!form.email.trim()) {
        errors.email = "Email is required.";
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
        errors.email = "Not a valid email address.";
    }

    if (isPasswordProvided) {
        if (form.password.length < 4 || form.password.length > 10) {
            errors.password = "Password must be between 4 and 10 characters.";
        } else if (
            !/^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$/.test(
                form.password
            )
        ) {
            errors.password =
                "Password must contain at least one digit, lowercase, uppercase, and special character.";
        }

        if (form.password !== form.confirmPassword) {
            errors.confirmPassword = "Passwords do not match.";
        }
    }

    return errors;
}

function MyAccount() {
    const { user, updateProfile } = useAuth();
    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);
    const [form, setForm] = useState<FormData>({
        name: "",
        email: "",
        password: "",
        confirmPassword: "",
    });
    const [errors, setErrors] = useState<FormErrors>({});

    useEffect(() => {
        UserService.getMyProfile()
            .then((data) => {
                setForm({
                    name: data.name,
                    email: data.email,
                    password: "",
                    confirmPassword: "",
                });
            })
            .catch((error) => {
                notifyError(error, "Unable to load profile.");
            })
            .finally(() => {
                setLoading(false);
            });
    }, []);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setForm((prev) => ({ ...prev, [name]: value }));

        if (errors[name]) {
            setErrors((prev) => {
                const next = { ...prev };
                delete next[name];
                return next;
            });
        }
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        const isPasswordProvided = form.password.trim().length > 0;
        const validationErrors = validate(form, isPasswordProvided);
        if (Object.keys(validationErrors).length > 0) {
            setErrors(validationErrors);
            return;
        }

        setSubmitting(true);

        try {
            const payload: { name: string; email: string; password?: string } = {
                name: form.name.trim(),
                email: form.email.trim(),
            };

            if (isPasswordProvided) {
                payload.password = form.password;
            }

            const updated = await UserService.updateMyProfile(payload);
            updateProfile({
                id: updated.id,
                name: updated.name,
                email: updated.email,
                role: updated.role,
                token: user!.token,
            });
            setForm((prev) => ({
                ...prev,
                password: "",
                confirmPassword: "",
            }));
            toast.success("Profile updated successfully.");
        } catch (error) {
            notifyError(error, "Failed to update profile.");
        } finally {
            setSubmitting(false);
        }
    };

    if (loading) {
        return (
            <div className="container mt-5">
                <div className="row justify-content-center">
                    <div className="col-md-8">
                        <p>Loading profile...</p>
                    </div>
                </div>
            </div>
        );
    }

    return (
        <div className="container mt-5">
            <div className="row justify-content-center">
                <div className="col-md-8">
                    <div className="card shadow">
                        <div className="card-header bg-primary text-white">
                            <h3>My Account</h3>
                        </div>
                        <div className="card-body">
                            <div className="mb-4">
                                <strong>Role:</strong> {user?.role}
                            </div>

                            <form onSubmit={handleSubmit}>
                                <div className="mb-3">
                                    <label className="form-label">Name</label>
                                    <input
                                        type="text"
                                        className={`form-control${errors.name ? " is-invalid" : ""}`}
                                        name="name"
                                        value={form.name}
                                        onChange={handleChange}
                                    />
                                    {errors.name && (
                                        <div className="invalid-feedback">
                                            {errors.name}
                                        </div>
                                    )}
                                </div>

                                <div className="mb-3">
                                    <label className="form-label">Email</label>
                                    <input
                                        type="email"
                                        className={`form-control${errors.email ? " is-invalid" : ""}`}
                                        name="email"
                                        value={form.email}
                                        onChange={handleChange}
                                    />
                                    {errors.email && (
                                        <div className="invalid-feedback">
                                            {errors.email}
                                        </div>
                                    )}
                                </div>

                                <hr />

                                <p className="text-muted mb-3">
                                    Leave password fields empty to keep your current
                                    password.
                                </p>

                                <div className="mb-3">
                                    <label className="form-label">
                                        New Password
                                    </label>
                                    <input
                                        type="password"
                                        className={`form-control${errors.password ? " is-invalid" : ""}`}
                                        name="password"
                                        value={form.password}
                                        onChange={handleChange}
                                    />
                                    {errors.password && (
                                        <div className="invalid-feedback">
                                            {errors.password}
                                        </div>
                                    )}
                                </div>

                                <div className="mb-3">
                                    <label className="form-label">
                                        Confirm New Password
                                    </label>
                                    <input
                                        type="password"
                                        className={`form-control${errors.confirmPassword ? " is-invalid" : ""}`}
                                        name="confirmPassword"
                                        value={form.confirmPassword}
                                        onChange={handleChange}
                                    />
                                    {errors.confirmPassword && (
                                        <div className="invalid-feedback">
                                            {errors.confirmPassword}
                                        </div>
                                    )}
                                </div>

                                <button
                                    type="submit"
                                    className="btn btn-primary me-3"
                                    disabled={submitting}
                                >
                                    {submitting ? "Saving..." : "Save Changes"}
                                </button>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default MyAccount;
