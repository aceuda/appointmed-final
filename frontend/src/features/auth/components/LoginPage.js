import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth, useToast } from "../../../contexts";
import { authAPI } from "../../../shared/services/api";
import "./LoginPage.css";

function LoginPage() {
    const navigate = useNavigate();
    const { handleLogin } = useAuth();
    const showToast = useToast();

    const roles = ["Patient", "Doctor"];
    const [role, setRole] = useState("Patient");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        try {
            const response = await authAPI.login({
                email,
                password,
                role: role.toUpperCase(),
            });
            handleLogin(response.data);
            navigate('/dashboard');
        } catch (err) {
            showToast(err.response?.data?.message || "Invalid email or password.", "error");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="login-page">
            <header className="login-navbar">
                <div className="login-logo">
                    Appoint<span>Med</span>
                </div>
                <button className="help-button">Help</button>
            </header>

            <main className="login-main-content">
                <div className="login-card">
                    <div className="login-card-header">
                        <h1>Welcome Back</h1>
                        <p>Secure portal for patients and providers</p>
                    </div>

                    <div className="role-toggle-container">
                        {roles.map((r) => (
                            <button
                                key={r}
                                type="button"
                                onClick={() => setRole(r)}
                                className={`role-toggle-btn ${role === r ? "active" : ""}`}
                            >
                                {r}
                            </button>
                        ))}
                    </div>

                    <form onSubmit={handleSubmit} className="login-form-body">
                        <div className="input-field">
                            <label>Email Address</label>
                            <div className="input-control">
                                <span className="material-symbols-outlined icon">alternate_email</span>
                                <input
                                    type="email"
                                    placeholder="name@example.com"
                                    value={email}
                                    onChange={(e) => setEmail(e.target.value)}
                                    required
                                />
                            </div>
                        </div>

                        <div className="input-field">
                            <div className="label-flex">
                                <label>Password</label>
                                <button type="button" className="forgot-pw">Forgot password?</button>
                            </div>
                            <div className="input-control">
                                <span className="material-symbols-outlined icon">lock</span>
                                <input
                                    type="password"
                                    placeholder="••••••••"
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                    required
                                />
                            </div>
                        </div>

                        <button type="submit" className="submit-btn" disabled={loading}>
                            {loading ? "Signing In..." : "Sign In"}
                            <span className="material-symbols-outlined">account_circle</span>
                        </button>
                    </form>

                    <div className="login-card-footer">
                        Don't have an account?
                        <button onClick={() => navigate('/register')} className="register-link">Register now</button>
                    </div>
                </div>
            </main>
        </div>
    );
}

export default LoginPage;
