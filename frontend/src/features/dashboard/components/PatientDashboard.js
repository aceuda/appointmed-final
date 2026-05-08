import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth, useToast } from "../../../contexts";
import { appointmentAPI, doctorAPI, notificationAPI } from "../../../shared/services/api";
import "./PatientDashboard.css";

function PatientDashboard({ onSelectDoctor }) {
    const navigate = useNavigate();
    const { user, handleLogout } = useAuth();
    const showToast = useToast();
    const patientName = user?.name || "Patient";

    const [upcomingAppt, setUpcomingAppt] = useState(null);
    const [recentAppointments, setRecentAppointments] = useState([]);
    const [doctors, setDoctors] = useState([]);
    const [unreadCount, setUnreadCount] = useState(0);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        if (!user) return;
        const fetchData = async () => {
            try {
                // Fetch all appointments and find the most recent upcoming one
                const apptRes = await appointmentAPI.getByPatient(user.id);
                const allAppts = apptRes.data || [];

                // Store recent appointments for the list
                setRecentAppointments(allAppts.slice(0, 5));

                const upcoming = allAppts
                    .filter(a => a.status !== 'CANCELLED' && a.status !== 'COMPLETED')
                    .sort((a, b) => new Date(a.appointmentDate) - new Date(b.appointmentDate));
                if (upcoming.length > 0) setUpcomingAppt(upcoming[0]);

                // Fetch doctors for specialist cards
                const docRes = await doctorAPI.getAll();
                setDoctors(docRes.data.slice(0, 4));

                // Fetch notification count
                try {
                    const notifRes = await notificationAPI.getUnread(user.id);
                    setUnreadCount(notifRes.data.count || 0);
                } catch { }
            } catch (err) {
                console.error('Dashboard load error:', err);
            }
            setLoading(false);
        };
        fetchData();
    }, [user]);

    const formatDate = (dateStr) => {
        if (!dateStr) return '';
        const d = new Date(dateStr + 'T00:00:00');
        return d.toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric' });
    };

    const formatTime = (timeStr) => {
        if (!timeStr) return '';
        const [h, m] = timeStr.split(':');
        const hour = parseInt(h);
        const ampm = hour >= 12 ? 'PM' : 'AM';
        const h12 = hour % 12 || 12;
        return `${h12}:${m} ${ampm}`;
    };

    const formatFee = (fee) => {
        if (!fee && fee !== 0) return '—';
        return `₱${Number(fee).toLocaleString('en-PH', { minimumFractionDigits: 2 })}`;
    };

    const getStatusClass = (status) => {
        switch (status) {
            case 'CONFIRMED': return 'stat-confirmed';
            case 'PENDING': return 'stat-pending';
            case 'COMPLETED': return 'stat-completed';
            case 'CANCELLED': return 'stat-cancelled';
            default: return '';
        }
    };

    return (
        <div className="patient-dashboard">
            <header className="patient-global-header">
                <div className="logo-section">
                    <span className="logo-text-dark">Appoint</span>
                    <span className="logo-text-blue">Med</span>
                </div>
                <div className="header-actions">
                    <button className="icon-button notification-btn" onClick={() => navigate('/notifications')}>
                        <span className="material-symbols-outlined">notifications</span>
                        {unreadCount > 0 && <span className="notification-dot"></span>}
                    </button>
                    <div className="user-profile-header" onClick={() => navigate('/profile')} style={{ cursor: 'pointer' }}>
                        <div>
                            <p className="profile-name">{patientName}</p>
                            <p className="profile-role">{user?.role || ""}</p>
                        </div>
                        <div className="profile-avatar">
                            {(user?.avatarData || user?.avatarUrl) ? (
                                <img src={user.avatarData || user.avatarUrl} alt="" style={{ width: 36, height: 36, borderRadius: '50%', objectFit: 'cover' }} />
                            ) : (
                                <span className="material-symbols-outlined">account_circle</span>
                            )}
                        </div>
                    </div>
                </div>
            </header>

            <aside className="patient-sidebar">
                <nav className="patient-nav">
                    <button className="patient-nav-button active" onClick={() => navigate('/dashboard')}>
                        <span className="material-symbols-outlined">dashboard</span>
                        <span>Dashboard</span>
                    </button>
                    <button className="patient-nav-button" onClick={() => navigate('/appointments')}>
                        <span className="material-symbols-outlined">calendar_today</span>
                        <span>Appointments</span>
                    </button>
                    <button className="patient-nav-button" onClick={() => navigate('/specialists')}>
                        <span className="material-symbols-outlined">stethoscope</span>
                        <span>Specialists</span>
                    </button>
                    <button className="patient-nav-button" onClick={() => navigate('/notifications')}>
                        <span className="material-symbols-outlined">notifications</span>
                        <span>Notifications</span>
                    </button>
                    <button className="patient-nav-button" onClick={() => navigate('/profile')}>
                        <span className="material-symbols-outlined">settings</span>
                        <span>Settings</span>
                    </button>
                </nav>
                <div className="sidebar-footer">
                    <button className="logout-btn" onClick={handleLogout}>
                        <span className="material-symbols-outlined">logout</span>
                        <span>Logout</span>
                    </button>
                </div>
            </aside>

            <main className="patient-main">
                <div className="page-intro">
                    <div className="header-text">
                        <h1>Welcome back to your health summary.</h1>
                    </div>
                    <div className="header-actions">
                        <button className="btn-primary" onClick={() => navigate('/specialists')}>
                            <span className="material-symbols-outlined">add_circle</span>
                            Quick Book
                        </button>
                    </div>
                </div>

                {/* Upcoming Appointment */}
                <section className="upcoming-card">
                    {upcomingAppt ? (
                        <>
                            <div className="upcoming-date-section">
                                <p className="label-tiny">Upcoming</p>
                                <div className="date-display">
                                    <span className="material-symbols-outlined">calendar_month</span>
                                    <div>
                                        <p className="date-main">{formatDate(upcomingAppt.appointmentDate)}</p>
                                        <p className="date-sub">{formatTime(upcomingAppt.appointmentTime)}</p>
                                    </div>
                                </div>
                            </div>
                            <div className="upcoming-info-section">
                                <div className="doctor-meta">
                                    <div className="avatar-large">
                                        {upcomingAppt.doctor?.user?.avatarData ? (
                                            <img src={upcomingAppt.doctor.user.avatarData} alt="" style={{ width: '100%', height: '100%', borderRadius: '50%', objectFit: 'cover' }} />
                                        ) : null}
                                    </div>
                                    <div className="meta-text">
                                        <h3>{upcomingAppt.doctor?.user?.name || 'Doctor'}</h3>
                                        <p>{upcomingAppt.doctor?.specialization || 'Specialist'} • {upcomingAppt.doctor?.clinicAddress || ''}</p>
                                        <div className="tags">
                                            <span className="tag-blue">{upcomingAppt.reason || 'Consultation'}</span>
                                            <span className="tag-price">{formatFee(upcomingAppt.fee)} {upcomingAppt.paymentStatus}</span>
                                        </div>
                                    </div>
                                </div>
                                <div className="card-actions">
                                    <button className="btn-outline" onClick={() => navigate('/specialists')}>Reschedule</button>
                                    <button className="btn-dark" onClick={() => navigate('/appointments')}>View Details</button>
                                </div>
                            </div>
                        </>
                    ) : (
                        <div style={{ padding: 24, textAlign: 'center', color: '#94a3b8' }}>
                            <span className="material-symbols-outlined" style={{ fontSize: 40 }}>event_available</span>
                            <p style={{ margin: '8px 0 0' }}>No upcoming appointments</p>
                            <button className="btn-primary" style={{ marginTop: 12 }} onClick={() => navigate('/specialists')}>Book Now</button>
                        </div>
                    )}
                </section>



                {/* Specialist Grid */}
                <div className="section-title-row">
                    <h2>Find a Specialist</h2>
                    <button className="btn-ghost" onClick={() => navigate('/specialists')} style={{ fontSize: 13 }}>View All →</button>
                </div>

                <div className="specialist-grid">
                    {doctors.length > 0 ? doctors.map(doc => (
                        <div className="doctor-card" key={doc.id}>
                            <div className="card-top">
                                <div className="avatar-placeholder">
                                    {doc.user?.avatarData || (doc.avatarUrl && doc.avatarUrl !== 'null') ? (
                                        <img src={doc.user?.avatarData || doc.avatarUrl} alt={doc.name} style={{ width: '100%', height: '100%', borderRadius: '50%', objectFit: 'cover' }} />
                                    ) : (
                                        <span className="material-symbols-outlined">person</span>
                                    )}
                                </div>
                                <span className="badge-available">{doc.available ? 'Available' : 'Busy'}</span>
                            </div>
                            <h4>{doc.name}</h4>
                            <p className="specialty-text">{doc.specialization}</p>
                            <div className="card-details">
                                <p><span className="material-symbols-outlined">location_on</span>{doc.clinicAddress || 'Clinic'}</p>
                                <p><span className="material-symbols-outlined">payments</span> {formatFee(doc.consultationFee)} / session</p>
                            </div>
                            <button className="btn-ghost" onClick={() => {
                                if (onSelectDoctor) onSelectDoctor(doc);
                                navigate('/book-appointment');
                            }}>Book Now</button>
                        </div>
                    )) : (
                        <div style={{ gridColumn: '1/-1', textAlign: 'center', padding: 32, color: '#94a3b8' }}>
                            {loading ? 'Loading doctors...' : 'No doctors available yet. Check back soon!'}
                        </div>
                    )}
                </div>
            </main>
        </div>
    );
}

export default PatientDashboard;
