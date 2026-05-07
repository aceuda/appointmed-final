import React, { useState } from "react";
import { userAPI } from "../services/api";
import "../css/LoginPage.css";

function LoginPage({ onLogin, onSwitch }) {
    // Restricted roles to exclude Admin
    const roles = ["Patient", "Doctor"];

    const [role, setRole] = useState("Patient");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [message, setMessage] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setMessage("");
        setError("");
        setLoading(true);

        try {
            const response = await userAPI.login({
                email,
                password,
                role: role.toUpperCase(),
            });

            setMessage(`Login successful! Welcome ${response.data.name}`);
            if (onLogin) onLogin(response.data);

        } catch (err) {
            setError(err.response?.data || "Unable to connect to the server.");
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

                    {/* Role Switcher - Now dynamic for 2 roles */}
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

                    {error && <div className="login-alert error">{error}</div>}
                    {message && <div className="login-alert success">{message}</div>}

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
                        <button onClick={onSwitch} className="register-link">Register now</button>
                    </div>
                </div>
            </main>
        </div>
    );
}

export default LoginPage;