import React, { useEffect } from "react";
import "../css/PatientDashboard.css";

function PatientDashboard({ onLogout, onNavigateProfile }) {
    const savedUser = JSON.parse(localStorage.getItem("user"));
    const patientName = savedUser?.name || "Juan Dela Cruz";
    const patientId = savedUser?.id || "8821";
    // searchTerm no longer needed

    // Redirect if not logged in
    useEffect(() => {
        if (!savedUser) window.location.href = "/";
    }, [savedUser]);

    return (
        <div className="patient-dashboard">
            <header className="patient-global-header">
                <div className="logo-section">
                    <span className="logo-text-dark">Appoint</span>
                    <span className="logo-text-blue">Med</span>
                </div>
                <div className="header-actions">
                    <button className="icon-button notification-btn">
                        <span className="material-symbols-outlined">notifications</span>
                        <span className="notification-dot"></span>
                    </button>
                    <div className="user-profile-header" onClick={onNavigateProfile} style={{ cursor: 'pointer' }}>
                        <div>
                            <p className="profile-name">{patientName}</p>
                            <p className="profile-role">{savedUser?.role || ""}</p>
                        </div>
                        <div className="profile-avatar">
                            <span className="material-symbols-outlined">account_circle</span>
                        </div>
                    </div>
                </div>
            </header>
            {/* Sidebar Navigation */}
            <aside className="patient-sidebar">

                <nav className="patient-nav">
                    <a href="/dashboard" className="patient-nav-button">
                        <span className="material-symbols-outlined">dashboard</span>
                        <span>Dashboard</span>
                    </a>
                    <a href="/appointments" className="patient-nav-button">
                        <span className="material-symbols-outlined">calendar_today</span>
                        <span>Appointments</span>
                    </a>
                    <a href="/doctors" className="patient-nav-button">
                        <span className="material-symbols-outlined">medical_services</span>
                        <span>Doctors</span>
                    </a>
                    <a href="/messages" className="patient-nav-button">
                        <span className="material-symbols-outlined">chat_bubble</span>
                        <span>Messages</span>
                    </a>
                    <a href="/billing" className="patient-nav-button">
                        <span className="material-symbols-outlined">payments</span>
                        <span>Billing</span>
                    </a>
                    <button className="patient-nav-button" onClick={onNavigateProfile} style={{ background: 'none', border: 'none', cursor: 'pointer', width: '100%' }}>
    <span className="material-symbols-outlined">settings</span>
    <span>Settings</span>
</button>
                </nav>

                <div className="sidebar-footer">
                    <button className="logout-btn" onClick={() => { localStorage.clear(); window.location.href = "/"; }}>
                        <span className="material-symbols-outlined">logout</span>
                        <span>Logout</span>
                    </button>
                </div>
            </aside>

            {/* Main Content Area */}
            <main className="patient-main">
                {/* Page intro text/actions above the upcoming card */}
                <div className="page-intro">
                    <div className="header-text">
                        <h1>Welcome back to your health summary.</h1>
                    </div>
                    <div className="header-actions">
                        <button className="btn-primary">
                            <span className="material-symbols-outlined">add_circle</span>
                            Quick Book
                        </button>
                    </div>
                </div>

                {/* Upcoming Appointment Card */}
                <section className="upcoming-card">
                    <div className="upcoming-date-section">
                        <p className="label-tiny">Upcoming</p>
                        <div className="date-display">
                            <span className="material-symbols-outlined">calendar_month</span>
                            <div>
                                <p className="date-main">Oct 24</p>
                                <p className="date-sub">Thursday, 10:00 AM</p>
                            </div>
                        </div>
                    </div>
                    <div className="upcoming-info-section">
                        <div className="doctor-meta">
                            <div className="avatar-large"></div>
                            <div className="meta-text">
                                <h3>Dr. Maria Santos</h3>
                                <p>Senior Cardiologist • Makati Medical Center</p>
                                <div className="tags">
                                    <span className="tag-blue">Consultation</span>
                                    <span className="tag-price">₱2,500.00 Fee Paid</span>
                                </div>
                            </div>
                        </div>
                        <div className="card-actions">
                            <button className="btn-outline">Reschedule</button>
                            <button className="btn-dark">View Details</button>
                        </div>
                    </div>
                </section>

                <div className="section-title-row">
                    <h2>Find a Specialist</h2>
                    <div className="search-bar">
                        <span className="material-symbols-outlined">search</span>
                        <input type="text" placeholder="Search by name, specialty, or hospital..." />
                    </div>
                </div>

                {/* Speciality Grid */}
                <div className="specialist-grid">
                    {/* Repeat this card structure for doctors */}
                    <div className="doctor-card">
                        <div className="card-top">
                            <div className="avatar-placeholder"><span className="material-symbols-outlined">person</span></div>
                            <span className="badge-available">Available</span>
                        </div>
                        <h4>Dr. Elena Reyes</h4>
                        <p className="specialty-text">Pediatrics</p>
                        <div className="card-details">
                            <p><span className="material-symbols-outlined">location_on</span> St. Luke's Medical Center</p>
                            <p><span className="material-symbols-outlined">payments</span> ₱1,800.00 / session</p>
                        </div>
                        <button className="btn-ghost">Book Now</button>
                    </div>
                    {/* Add more doctor-cards here */}
                </div>

                {/* Bottom Stats Section */}
                <div className="stats-row">
                    <div className="stat-card">
                        <div className="stat-icon blue"><span className="material-symbols-outlined">description</span></div>
                        <div><p className="stat-label">UNPAID INVOICES</p><p className="stat-value">₱0.00</p></div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-icon green"><span className="material-symbols-outlined">folder_open</span></div>
                        <div><p className="stat-label">MEDICAL RECORDS</p><p className="stat-value">12 Files</p></div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-icon orange"><span className="material-symbols-outlined">pill</span></div>
                        <div><p className="stat-label">ACTIVE PRESCRIPTIONS</p><p className="stat-value">3 Active</p></div>
                    </div>
                </div>
            </main>
        </div>
    );
}

export default PatientDashboard;