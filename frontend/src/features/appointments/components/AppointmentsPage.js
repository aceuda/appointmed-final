import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth, useToast } from '../../../App';
import { appointmentAPI } from '../../../shared/services/api';
import './AppointmentsPage.css';

function AppointmentsPage() {
    const navigate = useNavigate();
    const { user, handleLogout } = useAuth();
    const showToast = useToast();

    const [appointments, setAppointments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [filter, setFilter] = useState('ALL');

    useEffect(() => {
        if (!user) return;
        loadAppointments();
    }, [user]);

    const loadAppointments = async () => {
        try {
            let res;
            if (user.role === 'DOCTOR') {
                res = await appointmentAPI.getAll();
                setAppointments((res.data || []).filter(a => a.doctor?.user?.id === user.id));
            } else {
                res = await appointmentAPI.getByPatient(user.id);
                setAppointments(res.data || []);
            }
        } catch (err) {
            console.error('Failed to load appointments:', err);
        }
        setLoading(false);
    };

    const handleCancel = async (id) => {
        if (!window.confirm('Are you sure you want to cancel this appointment?')) return;
        try {
            await appointmentAPI.cancel(id);
            showToast('Appointment cancelled', 'success');
            setAppointments(prev => prev.map(a => a.id === id ? { ...a, status: 'CANCELLED' } : a));
        } catch { showToast('Failed to cancel', 'error'); }
    };

    const handleConfirm = async (id) => {
        try {
            await appointmentAPI.confirm(id);
            showToast('Appointment confirmed!', 'success');
            setAppointments(prev => prev.map(a => a.id === id ? { ...a, status: 'CONFIRMED' } : a));
        } catch { showToast('Failed to confirm', 'error'); }
    };

    const handleComplete = async (id) => {
        try {
            await appointmentAPI.complete(id);
            showToast('Appointment completed!', 'success');
            setAppointments(prev => prev.map(a => a.id === id ? { ...a, status: 'COMPLETED' } : a));
        } catch { showToast('Failed to complete', 'error'); }
    };

    const filtered = appointments.filter(a => {
        if (filter === 'ALL') return true;
        return a.status === filter;
    });

    const upcoming = filtered.filter(a => a.status === 'CONFIRMED' || a.status === 'PENDING');
    const past = filtered.filter(a => a.status === 'COMPLETED' || a.status === 'CANCELLED');

    const getStatusClass = (status) => {
        switch (status) {
            case 'CONFIRMED': return 'status-confirmed';
            case 'PENDING': return 'status-pending';
            case 'COMPLETED': return 'status-completed';
            case 'CANCELLED': return 'status-cancelled';
            default: return '';
        }
    };

    const formatDate = (dateStr) => {
        if (!dateStr) return '';
        const d = new Date(dateStr + 'T00:00:00');
        return d.toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric', year: 'numeric' });
    };

    const formatTime = (timeStr) => {
        if (!timeStr) return '';
        const [h, m] = timeStr.split(':');
        const hour = parseInt(h);
        const ampm = hour >= 12 ? 'PM' : 'AM';
        const h12 = hour % 12 || 12;
        return `${h12}:${m} ${ampm}`;
    };

    const renderCard = (appt) => (
        <div className="appt-card" key={appt.id}>
            <div className="appt-card-left">
                <div className="appt-avatar">
                    <span className="material-symbols-outlined">account_circle</span>
                </div>
                <div className="appt-info">
                    <h4>
                        {user.role === 'DOCTOR'
                            ? (appt.patient?.name || 'Patient')
                            : (appt.doctor?.user?.name || 'Doctor')}
                    </h4>
                    {user.role === 'PATIENT' && appt.doctor?.specialization && (
                        <p className="appt-specialty">{appt.doctor.specialization}</p>
                    )}
                    <div className="appt-meta">
                        <span><span className="material-symbols-outlined">calendar_today</span> {formatDate(appt.appointmentDate)}</span>
                        <span><span className="material-symbols-outlined">schedule</span> {formatTime(appt.appointmentTime)}</span>
                    </div>
                    <p className="appt-reason"><span className="material-symbols-outlined">description</span> {appt.reason || 'General Consultation'}</p>
                </div>
            </div>
            <div className="appt-card-right">
                <span className={`appt-status ${getStatusClass(appt.status)}`}>{appt.status}</span>
                <span className="appt-fee">₱{(appt.fee || 0).toLocaleString('en-PH', { minimumFractionDigits: 2 })}</span>
                <div className="appt-actions">
                    {user.role === 'DOCTOR' && appt.status === 'PENDING' && (
                        <button className="btn-confirm-appt" onClick={() => handleConfirm(appt.id)}>Confirm</button>
                    )}
                    {user.role === 'DOCTOR' && appt.status === 'CONFIRMED' && (
                        <button className="btn-complete-appt" onClick={() => handleComplete(appt.id)}>Complete</button>
                    )}
                    {(appt.status === 'PENDING' || appt.status === 'CONFIRMED') && (
                        <button className="btn-cancel-appt" onClick={() => handleCancel(appt.id)}>Cancel</button>
                    )}
                </div>
            </div>
        </div>
    );

    return (
        <div className="appointments-page">
            <aside className="appt-sidebar">
                <div className="sidebar-logo">
                    <span className="logo-dark">Appoint</span><span className="logo-blue">Med</span>
                </div>
                <nav className="sidebar-nav">
                    <button className="nav-item" onClick={() => navigate('/dashboard')}>
                        <span className="material-symbols-outlined">dashboard</span> Dashboard
                    </button>
                    <button className="nav-item active">
                        <span className="material-symbols-outlined">calendar_today</span> Appointments
                    </button>
                    {user.role === 'PATIENT' && (
                        <button className="nav-item" onClick={() => navigate('/specialists')}>
                            <span className="material-symbols-outlined">stethoscope</span> Specialists
                        </button>
                    )}
                    <button className="nav-item" onClick={() => navigate('/notifications')}>
                        <span className="material-symbols-outlined">notifications</span> Notifications
                    </button>
                    <button className="nav-item" onClick={() => navigate('/profile')}>
                        <span className="material-symbols-outlined">settings</span> Settings
                    </button>
                </nav>
                <div className="sidebar-footer">
                    <button className="logout-btn" onClick={handleLogout}>
                        <span className="material-symbols-outlined">logout</span> Logout
                    </button>
                </div>
            </aside>

            <main className="appt-main">
                <div className="appt-header">
                    <div>
                        <h1>My Appointments</h1>
                        <p>{user.role === 'DOCTOR' ? 'Manage your patient appointments' : 'View and manage your scheduled visits'}</p>
                    </div>
                    {user.role === 'PATIENT' && (
                        <button className="btn-book-new" onClick={() => navigate('/specialists')}>
                            <span className="material-symbols-outlined">add_circle</span> Book New Appointment
                        </button>
                    )}
                </div>

                {/* Stats */}
                <div className="appt-stats">
                    <div className="stat-card-mini" onClick={() => setFilter('ALL')}>
                        <span className="material-symbols-outlined">event_note</span>
                        <div><p className="stat-num">{appointments.length}</p><p className="stat-label">Total</p></div>
                    </div>
                    <div className="stat-card-mini" onClick={() => setFilter('PENDING')}>
                        <span className="material-symbols-outlined" style={{color:'#f59e0b'}}>pending_actions</span>
                        <div><p className="stat-num">{appointments.filter(a => a.status === 'PENDING').length}</p><p className="stat-label">Pending</p></div>
                    </div>
                    <div className="stat-card-mini" onClick={() => setFilter('CONFIRMED')}>
                        <span className="material-symbols-outlined" style={{color:'#2563eb'}}>event_available</span>
                        <div><p className="stat-num">{appointments.filter(a => a.status === 'CONFIRMED').length}</p><p className="stat-label">Confirmed</p></div>
                    </div>
                    <div className="stat-card-mini" onClick={() => setFilter('COMPLETED')}>
                        <span className="material-symbols-outlined" style={{color:'#10b981'}}>check_circle</span>
                        <div><p className="stat-num">{appointments.filter(a => a.status === 'COMPLETED').length}</p><p className="stat-label">Completed</p></div>
                    </div>
                </div>

                {/* Filter Chips */}
                <div className="filter-row">
                    {['ALL', 'PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED'].map(f => (
                        <button key={f} className={`filter-chip ${filter === f ? 'active' : ''}`}
                            onClick={() => setFilter(f)}>
                            {f === 'ALL' ? 'All' : f.charAt(0) + f.slice(1).toLowerCase()}
                        </button>
                    ))}
                </div>

                {/* Upcoming */}
                {upcoming.length > 0 && (
                    <div className="appt-section">
                        <h3><span className="material-symbols-outlined">upcoming</span> Upcoming Appointments</h3>
                        <div className="appt-list">{upcoming.map(renderCard)}</div>
                    </div>
                )}

                {/* Past */}
                {past.length > 0 && (
                    <div className="appt-section">
                        <h3><span className="material-symbols-outlined">history</span> Past Appointments</h3>
                        <div className="appt-list">{past.map(renderCard)}</div>
                    </div>
                )}

                {/* Empty */}
                {loading ? (
                    <div className="appt-empty"><p>Loading appointments...</p></div>
                ) : filtered.length === 0 && (
                    <div className="appt-empty">
                        <span className="material-symbols-outlined empty-icon">event_busy</span>
                        <h3>{filter === 'ALL' ? 'No Appointments Yet' : `No ${filter.toLowerCase()} appointments`}</h3>
                        <p>{user.role === 'PATIENT' ? 'Book your first appointment with a specialist.' : 'No appointments found for this filter.'}</p>
                        {user.role === 'PATIENT' && (
                            <button className="btn-book-new" onClick={() => navigate('/specialists')}>
                                <span className="material-symbols-outlined">add_circle</span> Book Now
                            </button>
                        )}
                    </div>
                )}
            </main>
        </div>
    );
}

export default AppointmentsPage;
