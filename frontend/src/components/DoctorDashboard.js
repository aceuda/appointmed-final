import React, { useMemo, useState, useEffect } from "react";
import "../css/DoctorDashboard.css";

function DoctorDashboard(props) {
    const savedUser = JSON.parse(localStorage.getItem("user"));

    // Guard: if not logged in or not doctor, go back to login
    useEffect(() => {
        if (!savedUser || savedUser.role !== "DOCTOR") {
            window.location.href = "/";
        }
    }, [savedUser]);

    const doctorName = savedUser?.name || "Dr. Julian Smith";
    const doctorRole = savedUser?.role || "DOCTOR";

    const [searchTerm, setSearchTerm] = useState("");

    const [appointments] = useState([
        { id: 1, patientName: "Sarah Jenkins", type: "General Checkup", time: "09:00 AM", status: "Scheduled" },
        { id: 2, patientName: "Michael Chen", type: "Follow-up Exam", time: "10:30 AM", status: "Completed" },
        { id: 3, patientName: "Elena Rodriguez", type: "Consultation", time: "11:45 AM", status: "Cancelled" },
        { id: 4, patientName: "David Kim", type: "Lab Results", time: "02:00 PM", status: "Scheduled" },
    ]);

    const [slots, setSlots] = useState([
        { time: "08:00 AM", status: "Available" },
        { time: "09:00 AM", status: "Blocked" },
        { time: "10:00 AM", status: "Available" },
        { time: "11:00 AM", status: "Available" },
        { time: "12:00 PM", status: "Lunch" },
        { time: "01:00 PM", status: "Available" },
        { time: "02:00 PM", status: "Available" },
        { time: "03:00 PM", status: "Available" },
    ]);

    const filteredAppointments = useMemo(() => {
        return appointments.filter((item) => {
            const query = searchTerm.toLowerCase();
            return (
                item.patientName.toLowerCase().includes(query) ||
                item.type.toLowerCase().includes(query) ||
                item.status.toLowerCase().includes(query)
            );
        });
    }, [appointments, searchTerm]);

    const toggleSlot = (clickedIndex) => {
        setSlots((prev) =>
            prev.map((slot, index) => {
                if (index !== clickedIndex || slot.status === "Lunch") return slot;
                return {
                    ...slot,
                    status: slot.status === "Available" ? "Blocked" : "Available",
                };
            })
        );
    };

    const handleSaveChanges = () => alert("Availability changes saved.");
    const handlePrint = () => window.print();
    const handleLogout = () => {
        localStorage.removeItem("user");
        props.onLogout ? props.onLogout() : (window.location.href = "/");
    };

    const getStatusBadge = (status) => {
        switch (status) {
            case "Scheduled": return "badge-scheduled";
            case "Completed": return "badge-completed";
            case "Cancelled": return "badge-cancelled";
            default: return "badge-default";
        }
    };

    const getSlotClass = (status) => {
        switch (status) {
            case "Available": return "slot-available";
            case "Blocked": return "slot-blocked";
            case "Lunch": return "slot-lunch";
            default: return "";
        }
    };

    return (
        <div className="doctor-dashboard">
            {/* top search bar separate from sidebar */}
            <header className="search-header">
                <div className="logo-section">
                    <span className="logo-text-dark">Appoint</span>
                    <span className="logo-text-blue">Med</span>
                </div>
                <div className="search-bar-container">
                    <span className="material-symbols-outlined search-icon">search</span>
                    <input
                        className="search-input"
                        placeholder="Search patients, records..."
                        type="text"
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />
                </div>
                <div className="header-actions">
                    <button className="icon-button notification-btn">
                        <span className="material-symbols-outlined">notifications</span>
                        <span className="notification-dot"></span>
                    </button>
                    <div className="divider"></div>
                    <div className="user-profile-header">
                        <div className="profile-text">
                            <p className="profile-name">{doctorName}</p>
                            <p className="profile-role">{doctorRole}</p>
                        </div>
                        <div className="profile-avatar">
                            <span className="material-symbols-outlined">account_circle</span>
                        </div>
                    </div>
                </div>
            </header>

            {/* sidebar and main content start after search bar */}
            <div className="dashboard-layout">
                <aside className="doctor-sidebar">
                    <nav className="doctor-nav">
                        <a href="/dashboard" className="nav-link active">
                            <span className="material-symbols-outlined">dashboard</span>
                            <span>Dashboard</span>
                        </a>
                        <a href="/patients" className="nav-link">
                            <span className="material-symbols-outlined">groups</span>
                            <span>Patients</span>
                        </a>
                        <a href="/schedule" className="nav-link">
                            <span className="material-symbols-outlined">calendar_today</span>
                            <span>Schedule</span>
                        </a>
                        <a href="/messages" className="nav-link">
                            <span className="material-symbols-outlined">mail</span>
                            <span>Messages</span>
                        </a>
                        <a href="/settings" className="nav-link">
                            <span className="material-symbols-outlined">settings</span>
                            <span>Settings</span>
                        </a>
                    </nav>
                    <div className="sidebar-footer">
                        <button className="logout-btn" onClick={handleLogout}>
                            <span className="material-symbols-outlined">logout</span>
                            <span>Logout</span>
                        </button>
                    </div>
                </aside>

                <main className="doctor-main-content">
                    <div className="content-header">
                        <div>
                            <h1 className="page-title">Doctor's Dashboard</h1>
                            <p className="page-subtitle">Wednesday, October 25th, 2023</p>
                        </div>
                        <div className="action-buttons">
                            <button onClick={handlePrint} className="btn-secondary">
                                <span className="material-symbols-outlined">print</span> Print List
                            </button>
                            <button onClick={() => alert("New Appointment")} className="btn-primary">
                                <span className="material-symbols-outlined">add</span> New Appointment
                            </button>
                        </div>
                    </div>

                    <div className="stats-grid">
                        <div className="stat-card">
                            <div className="stat-header">
                                <div className="stat-icon blue"><span className="material-symbols-outlined">pending_actions</span></div>
                                <span className="stat-trend">+4 Today</span>
                            </div>
                            <p className="stat-label">Scheduled Today</p>
                            <h3 className="stat-value">12 Patients</h3>
                        </div>
                        {/* More stat cards... */}
                    </div>

                    <div className="dashboard-grid">
                        <section className="overview-section">
                            <div className="section-header">
                                <h2 className="section-title"><span className="material-symbols-outlined">view_list</span> Daily Overview</h2>
                                <button className="text-link">View All</button>
                            </div>
                            <div className="appointment-list">
                                {filteredAppointments.map((appointment) => (
                                    <div key={appointment.id} className={`appointment-item ${appointment.status === "Completed" ? "completed" : ""}`}>
                                        <div className="patient-avatar"><span className="material-symbols-outlined">account_circle</span></div>
                                        <div className="appointment-info">
                                            <h4>{appointment.patientName}</h4>
                                            <p>{appointment.type} • {appointment.time}</p>
                                        </div>
                                        <div className="appointment-status">
                                            <span className={`status-badge ${getStatusBadge(appointment.status)}`}>{appointment.status}</span>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </section>

                        <section className="availability-section">
                            <h2 className="section-title"><span className="material-symbols-outlined">schedule</span> Toggle Availability</h2>
                            <div className="availability-card">
                                <p className="helper-text">Click a time slot to mark as active or inactive.</p>
                                <div className="slots-grid">
                                    {slots.map((slot, index) => (
                                        <button
                                            key={index}
                                            className={`slot-btn ${getSlotClass(slot.status)}`}
                                            onClick={() => toggleSlot(index)}
                                            disabled={slot.status === "Lunch"}
                                        >
                                            <span className="slot-time">{slot.time}</span>
                                            <span className="slot-status-text">{slot.status}</span>
                                        </button>
                                    ))}
                                </div>
                                <button className="btn-save" onClick={handleSaveChanges}>
                                    <span className="material-symbols-outlined">save</span> Save Changes
                                </button>
                            </div>
                        </section>
                    </div>
                </main>
            </div>
        </div>
    );
}

export default DoctorDashboard;