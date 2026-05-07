import React, { useState } from "react";
import { userAPI } from "../services/api";
import "../css/RegisterPage.css";

function RegisterPage({ onSwitch }) {
    const [role, setRole] = useState("PATIENT");
    const [formData, setFormData] = useState({
        fullName: "",
        email: "",
        password: "",
        confirmPassword: "",
        phone: "",
        address: "",
        gender: "",
        birthDate: "",
        specialization: "",
        licenseNumber: "",
        clinicAddress: "",
    });

    const [termsAccepted, setTermsAccepted] = useState(false);
    const [showPassword, setShowPassword] = useState(false);
    const [message, setMessage] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData((prev) => ({ ...prev, [name]: value }));
    };

    const handleRoleChange = (selectedRole) => {
        setRole(selectedRole);
        setError("");
        setMessage("");
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError("");
        setMessage("");

        if (formData.password !== formData.confirmPassword) {
            setError("Passwords do not match.");
            return;
        }

        if (!termsAccepted) {
            setError("You must accept the Terms and Privacy Policy.");
            return;
        }

        setLoading(true);
        try {
            const response = await userAPI.register({ ...formData, role });
            setMessage(`Registration successful! Welcome ${response.data.name}`);
            if (onSwitch) setTimeout(() => onSwitch(), 2000);
        } catch (err) {
            setError(err.response?.data || "Unable to connect to the server.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="register-page">
            <header className="register-navbar">
                <div className="register-logo">
                    Appoint<span>Med</span>
                </div>
                <div className="navbar-links">
                    <button className="nav-link">Help Center</button>
                    <button className="nav-link">Privacy</button>
                </div>
            </header>

            <main className="register-main-content">
                <div className="register-card">
                    <div className="register-card-header">
                        <h1>Create Account</h1>
                        <p>Join the AppointMed medical portal</p>
                    </div>

                    <div className="role-toggle-container">
                        <button
                            type="button"
                            onClick={() => handleRoleChange("PATIENT")}
                            className={`role-toggle-btn ${role === "PATIENT" ? "active" : ""}`}
                        >
                            Patient
                        </button>
                        <button
                            type="button"
                            onClick={() => handleRoleChange("DOCTOR")}
                            className={`role-toggle-btn ${role === "DOCTOR" ? "active" : ""}`}
                        >
                            Doctor
                        </button>
                    </div>

                    {error && <div className="register-alert error">{error}</div>}
                    {message && <div className="register-alert success">{message}</div>}

                    <form onSubmit={handleSubmit} className="register-form-body">
                        <div className="input-field">
                            <label>Full Name</label>
                            <input className="input-control" name="fullName" placeholder="John Doe" type="text" value={formData.fullName} onChange={handleChange} required />
                        </div>

                        <div className="input-field">
                            <label>Email Address</label>
                            <input className="input-control" name="email" placeholder="name@example.com" type="email" value={formData.email} onChange={handleChange} required />
                        </div>

                        <div className="input-grid">
                            <div className="input-field">
                                <label>Phone</label>
                                <input className="input-control" name="phone" placeholder="09123456789" type="text" value={formData.phone} onChange={handleChange} />
                            </div>
                            <div className="input-field">
                                <label>Address</label>
                                <input className="input-control" name="address" placeholder="Cebu City" type="text" value={formData.address} onChange={handleChange} />
                            </div>
                        </div>

                        {role === "PATIENT" && (
                            <div className="input-grid">
                                <div className="input-field">
                                    <label>Gender</label>
                                    <select className="input-control" name="gender" value={formData.gender} onChange={handleChange}>
                                        <option value="">Select</option>
                                        <option value="MALE">Male</option>
                                        <option value="FEMALE">Female</option>
                                    </select>
                                </div>
                                <div className="input-field">
                                    <label>Birth Date</label>
                                    <input className="input-control" name="birthDate" type="date" value={formData.birthDate} onChange={handleChange} />
                                </div>
                            </div>
                        )}

                        {role === "DOCTOR" && (
                            <>
                                <div className="input-field">
                                    <label>Specialization</label>
                                    <input className="input-control" name="specialization" placeholder="Cardiology" type="text" value={formData.specialization} onChange={handleChange} />
                                </div>
                                <div className="input-grid">
                                    <div className="input-field">
                                        <label>License No.</label>
                                        <input className="input-control" name="licenseNumber" placeholder="DOC-123" type="text" value={formData.licenseNumber} onChange={handleChange} />
                                    </div>
                                    <div className="input-field">
                                        <label>Clinic Address</label>
                                        <input className="input-control" name="clinicAddress" placeholder="Street Name" type="text" value={formData.clinicAddress} onChange={handleChange} />
                                    </div>
                                </div>
                            </>
                        )}

                        <div className="input-grid">
                            <div className="input-field">
                                <label>Password</label>
                                <div className="password-wrapper">
                                    <input className="input-control" name="password" type={showPassword ? "text" : "password"} value={formData.password} onChange={handleChange} required />
                                    <span className="pw-toggle" onClick={() => setShowPassword(!showPassword)}>{showPassword ? "🙈" : "👁"}</span>
                                </div>
                            </div>
                            <div className="input-field">
                                <label>Confirm</label>
                                <input className="input-control" name="confirmPassword" type={showPassword ? "text" : "password"} value={formData.confirmPassword} onChange={handleChange} required />
                            </div>
                        </div>

                        <div className="terms-container">
                            <input type="checkbox" id="terms" checked={termsAccepted} onChange={(e) => setTermsAccepted(e.target.checked)} />
                            <label htmlFor="terms">I agree to the Terms and Privacy Policy</label>
                        </div>

                        <button type="submit" className="submit-btn" disabled={loading}>
                            {loading ? "Creating Account..." : "Create Account"}
                        </button>
                    </form>

                    <div className="register-card-footer">
                        Already have an account?
                        <button onClick={onSwitch} className="login-link">Sign in</button>
                    </div>
                </div>
            </main>
        </div>
    );
}

export default RegisterPage;