import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth, useToast } from '../../../App';
import { notificationAPI, userAPI } from '../../../shared/services/api';
import './NotificationsPage.css';

function NotificationsPage() {
    const navigate = useNavigate();
    const { user, handleLogout } = useAuth();
    const showToast = useToast();

    const [notifications, setNotifications] = useState([]);
    const [loading, setLoading] = useState(true);

    // Doctor: send notification form
    const [showSendForm, setShowSendForm] = useState(false);
    const [patients, setPatients] = useState([]);
    const [selectedPatient, setSelectedPatient] = useState('');
    const [notifTitle, setNotifTitle] = useState('');
    const [notifMessage, setNotifMessage] = useState('');
    const [sending, setSending] = useState(false);

    useEffect(() => {
        if (!user) return;
        loadNotifications();
        if (user.role === 'DOCTOR') loadPatients();
    }, [user]);

    const loadNotifications = async () => {
        try {
            const res = await notificationAPI.getByUser(user.id);
            setNotifications(res.data || []);
        } catch (err) { console.error(err); }
        setLoading(false);
    };

    const loadPatients = async () => {
        try {
            const res = await userAPI.getAll();
            setPatients(res.data.filter(u => u.role === 'PATIENT'));
        } catch (err) { console.error(err); }
    };

    const handleMarkAsRead = async (id) => {
        try {
            await notificationAPI.markAsRead(id);
            setNotifications(prev => prev.map(n => n.id === id ? { ...n, read: true } : n));
        } catch (err) { showToast('Failed to mark as read', 'error'); }
    };

    const handleMarkAllRead = async () => {
        try {
            await notificationAPI.markAllAsRead(user.id);
            setNotifications(prev => prev.map(n => ({ ...n, read: true })));
            showToast('All notifications marked as read', 'success');
        } catch (err) { showToast('Failed to mark all as read', 'error'); }
    };

    const handleSendNotification = async () => {
        if (!selectedPatient || !notifTitle.trim() || !notifMessage.trim()) {
            showToast('Please fill in all fields', 'error');
            return;
        }
        setSending(true);
        try {
            await notificationAPI.send({
                patientId: Number(selectedPatient),
                doctorId: user.id,
                title: notifTitle,
                message: notifMessage
            });
            showToast('Notification sent to patient!', 'success');
            setNotifTitle('');
            setNotifMessage('');
            setSelectedPatient('');
            setShowSendForm(false);
        } catch (err) {
            showToast(err.response?.data?.message || 'Failed to send notification', 'error');
        }
        setSending(false);
    };

    const formatDate = (dateStr) => {
        if (!dateStr) return '';
        const d = new Date(dateStr);
        const today = new Date();
        if (d.toDateString() === today.toDateString()) {
            return 'Today, ' + d.toLocaleTimeString('en-US', { hour: 'numeric', minute: '2-digit', hour12: true });
        }
        return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' }) +
            ' ' + d.toLocaleTimeString('en-US', { hour: 'numeric', minute: '2-digit', hour12: true });
    };

    const getTypeIcon = (type) => {
        switch (type) {
            case 'APPOINTMENT': return 'calendar_today';
            case 'REMINDER': return 'notifications_active';
            default: return 'info';
        }
    };

    const getTypeColor = (type) => {
        switch (type) {
            case 'APPOINTMENT': return '#2563eb';
            case 'REMINDER': return '#f59e0b';
            default: return '#64748b';
        }
    };

    return (
        <div className="notif-page">
            <aside className="notif-sidebar">
                <div className="sidebar-logo">
                    <span className="logo-dark">Appoint</span><span className="logo-blue">Med</span>
                </div>
                <nav className="sidebar-nav">
                    <button className="nav-item" onClick={() => navigate('/dashboard')}>
                        <span className="material-symbols-outlined">dashboard</span> Dashboard
                    </button>
                    <button className="nav-item" onClick={() => navigate('/appointments')}>
                        <span className="material-symbols-outlined">calendar_today</span> Appointments
                    </button>
                    <button className="nav-item active">
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

            <main className="notif-main">
                <div className="notif-header">
                    <div>
                        <h1>Notifications</h1>
                        <p>Stay updated with your appointment reminders and alerts.</p>
                    </div>
                    <div className="notif-actions">
                        {notifications.some(n => !n.read) && (
                            <button className="mark-all-btn" onClick={handleMarkAllRead}>
                                <span className="material-symbols-outlined">done_all</span> Mark All Read
                            </button>
                        )}
                        {user.role === 'DOCTOR' && (
                            <button className="send-notif-btn" onClick={() => setShowSendForm(!showSendForm)}>
                                <span className="material-symbols-outlined">send</span> Send to Patient
                            </button>
                        )}
                    </div>
                </div>

                {/* Doctor: Send Notification Form */}
                {showSendForm && user.role === 'DOCTOR' && (
                    <div className="send-form-card">
                        <h3><span className="material-symbols-outlined">notifications_active</span> Send Notification to Patient</h3>
                        <div className="send-form">
                            <div className="form-field">
                                <label>Select Patient</label>
                                <select value={selectedPatient} onChange={(e) => setSelectedPatient(e.target.value)}>
                                    <option value="">Choose a patient...</option>
                                    {patients.map(p => (
                                        <option key={p.id} value={p.id}>{p.name} ({p.email})</option>
                                    ))}
                                </select>
                            </div>
                            <div className="form-field">
                                <label>Title</label>
                                <input type="text" placeholder="e.g. Appointment Reminder" value={notifTitle}
                                    onChange={(e) => setNotifTitle(e.target.value)} />
                            </div>
                            <div className="form-field">
                                <label>Message</label>
                                <textarea placeholder="Write your notification message..." value={notifMessage}
                                    onChange={(e) => setNotifMessage(e.target.value)} rows={3} />
                            </div>
                            <div className="form-actions">
                                <button className="cancel-btn" onClick={() => setShowSendForm(false)}>Cancel</button>
                                <button className="submit-btn" onClick={handleSendNotification} disabled={sending}>
                                    {sending ? 'Sending...' : 'Send Notification'}
                                </button>
                            </div>
                        </div>
                    </div>
                )}

                {/* Notification List */}
                <div className="notif-list">
                    {loading ? (
                        <div className="notif-empty"><p>Loading notifications...</p></div>
                    ) : notifications.length === 0 ? (
                        <div className="notif-empty">
                            <span className="material-symbols-outlined empty-icon">notifications_off</span>
                            <h3>No Notifications</h3>
                            <p>You're all caught up! New notifications will appear here.</p>
                        </div>
                    ) : (
                        notifications.map((notif) => (
                            <div key={notif.id} className={`notif-card ${notif.read ? 'read' : 'unread'}`}
                                onClick={() => !notif.read && handleMarkAsRead(notif.id)}>
                                <div className="notif-icon" style={{ background: getTypeColor(notif.type) + '20', color: getTypeColor(notif.type) }}>
                                    <span className="material-symbols-outlined">{getTypeIcon(notif.type)}</span>
                                </div>
                                <div className="notif-content">
                                    <div className="notif-top">
                                        <h4>{notif.title}</h4>
                                        <span className="notif-time">{formatDate(notif.createdAt)}</span>
                                    </div>
                                    <p>{notif.message}</p>
                                    <span className="notif-type-tag">{notif.type}</span>
                                </div>
                                {!notif.read && <div className="unread-dot"></div>}
                            </div>
                        ))
                    )}
                </div>
            </main>
        </div>
    );
}

export default NotificationsPage;
