import React, { useMemo, useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth, useToast } from "../../../App";
import { appointmentAPI, doctorAPI } from "../../../shared/services/api";
import "./DoctorDashboard.css";

function DoctorDashboard() {
    const navigate = useNavigate();
    const { user, handleLogout } = useAuth();
    const showToast = useToast();

    const doctorName = user?.name || "Doctor";
    const [searchTerm, setSearchTerm] = useState("");
    const [appointments, setAppointments] = useState([]);
    const [doctorProfile, setDoctorProfile] = useState(null);
    const [loading, setLoading] = useState(true);

    // Real slot data from the API: [{time, status, patientName?, appointmentId?, appointmentStatus?}]
    const [slots, setSlots] = useState([]);
    const [slotsLoading, setSlotsLoading] = useState(false);

    useEffect(() => {
        if (!user) return;
        const fetchData = async () => {
            try {
                // Get doctor profile
                const docRes = await doctorAPI.getByUserId(user.id);
                setDoctorProfile(docRes.data);

                // Get today's appointments
                const today = new Date().toISOString().split('T')[0];
                const apptRes = await appointmentAPI.getByDoctor(docRes.data.id, today);
                setAppointments(apptRes.data);

                // Get today's slots with status
                await fetchSlotsForDate(docRes.data.id, today);
            } catch (err) {
                // Doctor profile might not exist yet, use fallback
                try {
                    const allAppts = await appointmentAPI.getAll();
                    setAppointments(allAppts.data.filter(a => a.doctor?.user?.id === user.id));
                } catch {}
            }
            setLoading(false);
        };
        fetchData();
    }, [user]);

    const fetchSlotsForDate = async (doctorId, dateStr) => {
        setSlotsLoading(true);
        try {
            const res = await doctorAPI.getSlotsWithStatus(doctorId, dateStr);
            setSlots(res.data);
        } catch {
            setSlots([]);
        }
        setSlotsLoading(false);
    };

    const filteredAppointments = useMemo(() => {
        return appointments.filter((item) => {
            const query = searchTerm.toLowerCase();
            const name = item.patient?.name || item.patientName || '';
            const type = item.reason || item.type || '';
            const status = item.status || '';
            return name.toLowerCase().includes(query) || type.toLowerCase().includes(query) || status.toLowerCase().includes(query);
        });
    }, [appointments, searchTerm]);

    const handleSaveChanges = async () => {
        if (!doctorProfile) {
            showToast("Doctor profile not loaded yet.", "error");
            return;
        }
        try {
            // Build schedule entries from available slots for each weekday
            const dayNames = ['MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY'];
            const schedules = [];
            const availableSlots = slots.filter(s => s.status === 'available');

            if (availableSlots.length > 0) {
                const times = availableSlots.map(s => s.time);
                const startTime = times[0] + (times[0].length === 5 ? ':00' : '');
                const endTime = times[times.length - 1] + (times[times.length - 1].length === 5 ? ':00' : '');

                for (const day of dayNames) {
                    schedules.push({
                        dayOfWeek: day,
                        startTime: startTime,
                        endTime: endTime,
                        slotDurationMinutes: 30,
                        isActive: true
                    });
                }
            }

            await doctorAPI.updateSchedule(doctorProfile.id, schedules);
            showToast("Availability saved successfully!", "success");
        } catch (err) {
            showToast("Failed to save availability.", "error");
            console.error(err);
        }
    };
    const handlePrint = () => window.print();

    const getStatusBadge = (status) => {
        switch (status) {
            case "CONFIRMED": case "Scheduled": return "badge-scheduled";
            case "COMPLETED": case "Completed": return "badge-completed";
            case "CANCELLED": case "Cancelled": return "badge-cancelled";
            case "PENDING": return "badge-scheduled";
            default: return "badge-default";
        }
    };

    const getSlotClass = (status) => {
        switch (status) {
            case "available": return "slot-available";
            case "booked": return "slot-booked-real";
            case "blocked": return "slot-blocked";
            default: return "";
        }
    };

    const formatSlotTime = (timeStr) => {
        if (!timeStr) return '';
        const [h, m] = timeStr.split(':');
        const hour = parseInt(h);
        const ampm = hour >= 12 ? 'PM' : 'AM';
        const h12 = hour % 12 || 12;
        return `${h12}:${m || '00'} ${ampm}`;
    };

    // Check if a slot time has already passed today
    const isSlotPast = (timeStr) => {
        if (!timeStr) return false;
        const now = new Date();
        const [h, m] = timeStr.split(':').map(Number);
        const slotDate = new Date(now.getFullYear(), now.getMonth(), now.getDate(), h, m, 0);
        return slotDate <= now;
    };

    const handleConfirm = async (id) => {
        try {
            await appointmentAPI.confirm(id);
            showToast("Appointment confirmed!", "success");
            setAppointments(prev => prev.map(a => a.id === id ? {...a, status: 'CONFIRMED'} : a));
            // Refresh slots to reflect status change
            if (doctorProfile) {
                const today = new Date().toISOString().split('T')[0];
                fetchSlotsForDate(doctorProfile.id, today);
            }
        } catch { showToast("Failed to confirm", "error"); }
    };

    const handleComplete = async (id) => {
        try {
            await appointmentAPI.complete(id);
            showToast("Appointment marked as completed!", "success");
            setAppointments(prev => prev.map(a => a.id === id ? {...a, status: 'COMPLETED'} : a));
            // Refresh slots to reflect status change
            if (doctorProfile) {
                const today = new Date().toISOString().split('T')[0];
                fetchSlotsForDate(doctorProfile.id, today);
            }
        } catch { showToast("Failed to complete", "error"); }
    };

    const handleToggleSlot = async (slot) => {
        if (!doctorProfile) return;
        try {
            const today = new Date().toISOString().split('T')[0];
            await doctorAPI.toggleSlot(doctorProfile.id, today, slot.time);
            // Refresh slots after toggling
            await fetchSlotsForDate(doctorProfile.id, today);
            const action = slot.status === 'blocked' ? 'unblocked' : 'blocked';
            showToast(`${formatSlotTime(slot.time)} ${action} successfully`, 'success');
        } catch {
            showToast('Failed to update slot', 'error');
        }
    };

    // Compute stats from real slot data
    const totalSlots = slots.length;
    const bookedSlots = slots.filter(s => s.status === 'booked').length;
    const blockedSlotCount = slots.filter(s => s.status === 'blocked').length;
    const availableSlotCount = totalSlots - bookedSlots - blockedSlotCount;

    return (
        <div className="doctor-dashboard">
            <header className="search-header">
                <div className="logo-section">
                    <span className="logo-text-dark">Appoint</span>
                    <span className="logo-text-blue">Med</span>
                </div>
                <div className="search-bar-container">
                    <span className="material-symbols-outlined search-icon">search</span>
                    <input className="search-input" placeholder="Search patients, records..." type="text"
                        value={searchTerm} onChange={(e) => setSearchTerm(e.target.value)} />
                </div>
                <div className="header-actions">
                    <button className="icon-button notification-btn">
                        <span className="material-symbols-outlined">notifications</span>
                    </button>
                    <div className="divider"></div>
                    <div className="user-profile-header" onClick={() => navigate('/profile')} style={{cursor:'pointer'}}>
                        <div className="profile-text">
                            <p className="profile-name">{doctorName}</p>
                            <p className="profile-role">DOCTOR</p>
                        </div>
                        <div className="profile-avatar">
                            <span className="material-symbols-outlined">account_circle</span>
                        </div>
                    </div>
                </div>
            </header>

            <div className="dashboard-layout">
                <aside className="doctor-sidebar">
                    <nav className="doctor-nav">
                        <button className="nav-link active" onClick={() => navigate('/dashboard')}>
                            <span className="material-symbols-outlined">dashboard</span><span>Dashboard</span>
                        </button>
                        <button className="nav-link" onClick={() => navigate('/appointments')}>
                            <span className="material-symbols-outlined">groups</span><span>Patients</span>
                        </button>
                        <button className="nav-link" onClick={() => navigate('/appointments')}>
                            <span className="material-symbols-outlined">calendar_today</span><span>Schedule</span>
                        </button>
                        <button className="nav-link" onClick={() => navigate('/notifications')}>
                            <span className="material-symbols-outlined">notifications</span><span>Notifications</span>
                        </button>
                        <button className="nav-link" onClick={() => navigate('/profile')}>
                            <span className="material-symbols-outlined">settings</span><span>Settings</span>
                        </button>
                    </nav>
                    <div className="sidebar-footer">
                        <button className="logout-btn" onClick={handleLogout}>
                            <span className="material-symbols-outlined">logout</span><span>Logout</span>
                        </button>
                    </div>
                </aside>

                <main className="doctor-main-content">
                    <div className="content-header">
                        <div>
                            <h1 className="page-title">Doctor's Dashboard</h1>
                            <p className="page-subtitle">{new Date().toLocaleDateString('en-US', { weekday:'long', year:'numeric', month:'long', day:'numeric' })}</p>
                        </div>
                        <div className="action-buttons">
                            <button onClick={handlePrint} className="btn-secondary">
                                <span className="material-symbols-outlined">print</span> Print List
                            </button>
                        </div>
                    </div>

                    <div className="stats-grid">
                        <div className="stat-card">
                            <div className="stat-header">
                                <div className="stat-icon blue"><span className="material-symbols-outlined">pending_actions</span></div>
                                <span className="stat-trend">Today</span>
                            </div>
                            <p className="stat-label">Total Appointments</p>
                            <h3 className="stat-value">{appointments.length} Patients</h3>
                        </div>
                        <div className="stat-card">
                            <div className="stat-header">
                                <div className="stat-icon blue"><span className="material-symbols-outlined">event_busy</span></div>
                                <span className="stat-trend">{bookedSlots}/{totalSlots}</span>
                            </div>
                            <p className="stat-label">Booked Slots</p>
                            <h3 className="stat-value">{bookedSlots} Booked</h3>
                        </div>
                        <div className="stat-card">
                            <div className="stat-header">
                                <div className="stat-icon blue"><span className="material-symbols-outlined">event_available</span></div>
                                <span className="stat-trend">{availableSlotCount}/{totalSlots}</span>
                            </div>
                            <p className="stat-label">Available Slots</p>
                            <h3 className="stat-value">{availableSlotCount} Open</h3>
                        </div>
                    </div>

                    <div className="dashboard-grid">
                        <section className="overview-section">
                            <div className="section-header">
                                <h2 className="section-title"><span className="material-symbols-outlined">view_list</span> Daily Overview</h2>
                            </div>
                            <div className="appointment-list">
                                {loading ? (
                                    <div style={{padding:24,textAlign:'center',color:'#94a3b8'}}>Loading appointments...</div>
                                ) : filteredAppointments.length === 0 ? (
                                    <div style={{padding:24,textAlign:'center',color:'#94a3b8'}}>
                                        <span className="material-symbols-outlined" style={{fontSize:36}}>event_busy</span>
                                        <p>No appointments for today</p>
                                    </div>
                                ) : (
                                    filteredAppointments.map((appointment) => (
                                        <div key={appointment.id} className={`appointment-item ${appointment.status === "COMPLETED" ? "completed" : ""}`}>
                                            <div className="patient-avatar"><span className="material-symbols-outlined">account_circle</span></div>
                                            <div className="appointment-info">
                                                <h4>{appointment.patient?.name || 'Patient'}</h4>
                                                <p>{appointment.reason || 'Consultation'} • {appointment.appointmentTime || ''}</p>
                                            </div>
                                            <div className="appointment-status">
                                                <span className={`status-badge ${getStatusBadge(appointment.status)}`}>{appointment.status}</span>
                                                {appointment.status === 'PENDING' && (
                                                    <button onClick={() => handleConfirm(appointment.id)} style={{marginLeft:8,padding:'4px 10px',borderRadius:6,border:'none',background:'#10B981',color:'#fff',cursor:'pointer',fontSize:12}}>Confirm</button>
                                                )}
                                                {appointment.status === 'CONFIRMED' && (
                                                    <button onClick={() => handleComplete(appointment.id)} style={{marginLeft:8,padding:'4px 10px',borderRadius:6,border:'none',background:'#2563EB',color:'#fff',cursor:'pointer',fontSize:12}}>Complete</button>
                                                )}
                                            </div>
                                        </div>
                                    ))
                                )}
                            </div>
                        </section>

                        <section className="availability-section">
                            <h2 className="section-title"><span className="material-symbols-outlined">schedule</span> Today's Schedule</h2>
                            <div className="availability-card">
                                {/* Slot legend */}
                                <div className="doc-slot-legend">
                                    <span className="doc-legend-item"><span className="doc-legend-dot doc-legend-available"></span> Available</span>
                                    <span className="doc-legend-item"><span className="doc-legend-dot doc-legend-booked"></span> Booked</span>
                                    <span className="doc-legend-item"><span className="doc-legend-dot doc-legend-blocked"></span> Blocked</span>
                                    <span className="doc-legend-item"><span className="doc-legend-dot doc-legend-past"></span> Past</span>
                                </div>
                                <p className="toggle-hint"><span className="material-symbols-outlined">touch_app</span> Click an available or blocked slot to toggle its availability</p>
                                {slotsLoading ? (
                                    <p style={{textAlign:'center',color:'#94a3b8',padding:16}}>Loading schedule...</p>
                                ) : slots.length === 0 ? (
                                    <p style={{textAlign:'center',color:'#94a3b8',padding:16}}>No schedule configured for today</p>
                                ) : (
                                    <div className="slots-grid">
                                        {slots.map((slot, index) => {
                                            const pastSlot = isSlotPast(slot.time);
                                            const isBooked = slot.status === 'booked';
                                            const isBlocked = slot.status === 'blocked';
                                            const canToggle = !pastSlot && !isBooked;
                                            const slotClass = pastSlot && !isBooked
                                                ? 'slot-past'
                                                : getSlotClass(slot.status);
                                            return (
                                                <div
                                                    key={index}
                                                    className={`slot-btn ${slotClass} ${canToggle ? 'slot-clickable' : ''}`}
                                                    onClick={() => canToggle && handleToggleSlot(slot)}
                                                    title={pastSlot ? 'This time has passed' : isBooked ? 'Booked by patient' : isBlocked ? 'Click to unblock' : 'Click to block this slot'}
                                                >
                                                    <span className="slot-time">{formatSlotTime(slot.time)}</span>
                                                    {isBooked ? (
                                                        <div className="slot-booked-info">
                                                            <span className="slot-status-text booked-text">Booked</span>
                                                            <span className="slot-patient-name">{slot.patientName || 'Patient'}</span>
                                                            {slot.appointmentStatus && (
                                                                <span className={`slot-appt-status ${slot.appointmentStatus === 'CONFIRMED' ? 'confirmed' : slot.appointmentStatus === 'PENDING' ? 'pending' : ''}`}>
                                                                    {slot.appointmentStatus}
                                                                </span>
                                                            )}
                                                        </div>
                                                    ) : isBlocked ? (
                                                        <span className="slot-status-text blocked-text">Blocked</span>
                                                    ) : pastSlot ? (
                                                        <span className="slot-status-text past-text">Past</span>
                                                    ) : (
                                                        <span className="slot-status-text available-text">Available</span>
                                                    )}
                                                </div>
                                            );
                                        })}
                                    </div>
                                )}
                                <button className="btn-save" onClick={handleSaveChanges}>
                                    <span className="material-symbols-outlined">save</span> Save Schedule
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
