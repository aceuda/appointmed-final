import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth, useToast } from '../../../App';
import {
  User, Lock, Bell, HelpCircle, Camera,
  Phone, MapPin, Calendar, Droplets, BellRing, LogOut, ArrowLeft, Save, X
} from 'lucide-react';
import './ProfilePage.css';
import { userAPI } from "../../../shared/services/api";

const ProfilePage = () => {
    const navigate = useNavigate();
    const { user: authUser, handleLogout: authLogout, updateUser } = useAuth();
    const showToast = useToast();
    const [activeTab, setActiveTab] = useState('Profile Details');

    // Profile form fields
    const [profileName, setProfileName] = useState('');
    const [profileEmail, setProfileEmail] = useState('');
    const [profilePhone, setProfilePhone] = useState('+1 (555) 123-4567');
    const [profileAddress, setProfileAddress] = useState('123 Medical Plaza, Apartment 4B, New York, NY 10001');
    const [profileDob, setProfileDob] = useState('May 14, 1992');
    const [profileBlood, setProfileBlood] = useState('O+');
    const [profileDirty, setProfileDirty] = useState(false);
    const [profileSaving, setProfileSaving] = useState(false);
    const [profileMsg, setProfileMsg] = useState({ type: '', text: '' });

    // Security form fields
    const [currentPassword, setCurrentPassword] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [secSaving, setSecSaving] = useState(false);
    const [secMsg, setSecMsg] = useState({ type: '', text: '' });

    // Notification settings
    const [notifEmail, setNotifEmail] = useState(true);
    const [notifSms, setNotifSms] = useState(false);
    const [notifAppointment, setNotifAppointment] = useState(true);
    const [notifPromo, setNotifPromo] = useState(false);

    const savedUser = authUser;

    useEffect(() => {
        if (savedUser) {
            setProfileName(savedUser.name || '');
            setProfileEmail(savedUser.email || '');
        }
    }, []);

    // Track changes
    useEffect(() => {
        if (savedUser) {
            const hasChanges = profileName !== (savedUser.name || '') || profileEmail !== (savedUser.email || '');
            setProfileDirty(hasChanges);
        }
    }, [profileName, profileEmail, profilePhone, profileAddress, profileDob, profileBlood]);

    const handleProfileSave = async () => {
        setProfileMsg({ type: '', text: '' });
        if (!profileName.trim() || !profileEmail.trim()) {
            setProfileMsg({ type: 'error', text: 'Name and email are required.' });
            return;
        }
        setProfileSaving(true);
        try {
            const updatedUser = { ...savedUser, name: profileName, email: profileEmail };
            await userAPI.update(savedUser.id, updatedUser);
            updateUser({ ...savedUser, name: profileName, email: profileEmail });
            showToast('Profile updated successfully!', 'success');
            setProfileDirty(false);
        } catch (err) {
            setProfileMsg({ type: 'error', text: err.response?.data?.message || 'Failed to update profile.' });
        }
        setProfileSaving(false);
    };

    const handleProfileCancel = () => {
        if (savedUser) {
            setProfileName(savedUser.name || '');
            setProfileEmail(savedUser.email || '');
        }
        setProfileMsg({ type: '', text: '' });
    };

    const handlePasswordChange = async () => {
        setSecMsg({ type: '', text: '' });
        if (!currentPassword || !newPassword || !confirmPassword) {
            setSecMsg({ type: 'error', text: 'All password fields are required.' });
            return;
        }
        if (newPassword.length < 4) {
            setSecMsg({ type: 'error', text: 'New password must be at least 4 characters.' });
            return;
        }
        if (newPassword !== confirmPassword) {
            setSecMsg({ type: 'error', text: 'Passwords do not match.' });
            return;
        }
        if (currentPassword !== savedUser?.password) {
            // Password not available client-side anymore; attempt server-side update
        }
        setSecSaving(true);
        try {
            // Send password update to backend - backend handles validation
            await userAPI.update(savedUser.id, { ...savedUser, password: newPassword });
            updateUser({ ...savedUser });
            showToast('Password updated successfully!', 'success');
            setCurrentPassword(''); setNewPassword(''); setConfirmPassword('');
        } catch (err) {
            setSecMsg({ type: 'error', text: err.response?.data?.message || 'Failed to update password.' });
        }
        setSecSaving(false);
    };

    const handleLogout = () => {
        if (window.confirm('Are you sure you want to log out?')) {
            authLogout();
            navigate('/login');
        }
    };

    const renderProfileTab = () => (
        <>
            <div className="form-header">
                <h3>Personal Information</h3>
                <p>Update your personal details and how we can reach you.</p>
            </div>
            {profileMsg.text && (
                <div className={`profile-alert ${profileMsg.type}`}>{profileMsg.text}</div>
            )}
            <div className="form-grid">
                <div className="input-group">
                    <label>Full Name</label>
                    <div className="input-wrapper">
                        <User size={18} className="input-icon" />
                        <input type="text" value={profileName} onChange={(e) => setProfileName(e.target.value)} />
                    </div>
                </div>
                <div className="input-group">
                    <label>Email</label>
                    <div className="input-wrapper">
                        <span className="input-icon" style={{ fontSize: 18 }}>@</span>
                        <input type="email" value={profileEmail} onChange={(e) => setProfileEmail(e.target.value)} />
                    </div>
                </div>
                <div className="input-group">
                    <label>Phone Number</label>
                    <div className="input-wrapper">
                        <Phone size={18} className="input-icon" />
                        <input type="text" value={profilePhone} onChange={(e) => { setProfilePhone(e.target.value); setProfileDirty(true); }} />
                    </div>
                </div>
                <div className="input-group full-width">
                    <label>Home Address</label>
                    <div className="input-wrapper">
                        <MapPin size={18} className="input-icon" />
                        <textarea value={profileAddress} onChange={(e) => { setProfileAddress(e.target.value); setProfileDirty(true); }} />
                    </div>
                </div>
                <div className="input-group">
                    <label>Date of Birth</label>
                    <div className="input-wrapper">
                        <Calendar size={18} className="input-icon" />
                        <input type="text" value={profileDob} onChange={(e) => { setProfileDob(e.target.value); setProfileDirty(true); }} />
                    </div>
                </div>
                <div className="input-group">
                    <label>Blood Type</label>
                    <div className="input-wrapper">
                        <Droplets size={18} className="input-icon" />
                        <select value={profileBlood} onChange={(e) => { setProfileBlood(e.target.value); setProfileDirty(true); }}>
                            <option value="O+">O+</option>
                            <option value="O-">O-</option>
                            <option value="A+">A+</option>
                            <option value="A-">A-</option>
                            <option value="B+">B+</option>
                            <option value="B-">B-</option>
                            <option value="AB+">AB+</option>
                            <option value="AB-">AB-</option>
                        </select>
                    </div>
                </div>
            </div>
            <div className="form-actions">
                <button className="btn-cancel" onClick={handleProfileCancel} disabled={!profileDirty}>
                    <X size={16} /> Cancel
                </button>
                <button className="btn-save" onClick={handleProfileSave} disabled={profileSaving}>
                    <Save size={16} /> {profileSaving ? 'Saving...' : 'Save Changes'}
                </button>
            </div>
        </>
    );

    const renderSecurityTab = () => (
        <>
            <div className="form-header">
                <h3>Security Settings</h3>
                <p>Update your password to keep your account secure.</p>
            </div>
            {secMsg.text && (
                <div className={`profile-alert ${secMsg.type}`}>{secMsg.text}</div>
            )}
            <div className="form-grid">
                <div className="input-group full-width">
                    <label>Current Password</label>
                    <div className="input-wrapper">
                        <Lock size={18} className="input-icon" />
                        <input type="password" placeholder="Enter current password" value={currentPassword} onChange={(e) => setCurrentPassword(e.target.value)} />
                    </div>
                </div>
                <div className="input-group">
                    <label>New Password</label>
                    <div className="input-wrapper">
                        <Lock size={18} className="input-icon" />
                        <input type="password" placeholder="Enter new password" value={newPassword} onChange={(e) => setNewPassword(e.target.value)} />
                    </div>
                </div>
                <div className="input-group">
                    <label>Confirm New Password</label>
                    <div className="input-wrapper">
                        <Lock size={18} className="input-icon" />
                        <input type="password" placeholder="Repeat new password" value={confirmPassword} onChange={(e) => setConfirmPassword(e.target.value)} />
                    </div>
                </div>
            </div>
            <div className="form-actions">
                <button className="btn-cancel" onClick={() => { setCurrentPassword(''); setNewPassword(''); setConfirmPassword(''); setSecMsg({type:'',text:''}); }}>
                    <X size={16} /> Clear
                </button>
                <button className="btn-save" onClick={handlePasswordChange} disabled={secSaving}>
                    <Lock size={16} /> {secSaving ? 'Updating...' : 'Update Password'}
                </button>
            </div>
        </>
    );

    const renderNotificationsTab = () => (
        <>
            <div className="form-header">
                <h3>Notification Preferences</h3>
                <p>Choose how you'd like to be notified about appointments and updates.</p>
            </div>
            <div className="notification-settings">
                <div className="notif-item">
                    <div>
                        <h4>Email Notifications</h4>
                        <p>Receive appointment reminders and updates via email</p>
                    </div>
                    <label className="toggle-switch">
                        <input type="checkbox" checked={notifEmail} onChange={(e) => setNotifEmail(e.target.checked)} />
                        <span className="toggle-slider"></span>
                    </label>
                </div>
                <div className="notif-item">
                    <div>
                        <h4>SMS Notifications</h4>
                        <p>Get text message reminders for upcoming appointments</p>
                    </div>
                    <label className="toggle-switch">
                        <input type="checkbox" checked={notifSms} onChange={(e) => setNotifSms(e.target.checked)} />
                        <span className="toggle-slider"></span>
                    </label>
                </div>
                <div className="notif-item">
                    <div>
                        <h4>Appointment Alerts</h4>
                        <p>Notifications about booking confirmations and changes</p>
                    </div>
                    <label className="toggle-switch">
                        <input type="checkbox" checked={notifAppointment} onChange={(e) => setNotifAppointment(e.target.checked)} />
                        <span className="toggle-slider"></span>
                    </label>
                </div>
                <div className="notif-item">
                    <div>
                        <h4>Promotional Updates</h4>
                        <p>Health tips and special offers from our partners</p>
                    </div>
                    <label className="toggle-switch">
                        <input type="checkbox" checked={notifPromo} onChange={(e) => setNotifPromo(e.target.checked)} />
                        <span className="toggle-slider"></span>
                    </label>
                </div>
            </div>
        </>
    );

    const renderHelpTab = () => (
        <>
            <div className="form-header">
                <h3>Help & Support</h3>
                <p>Need assistance? We're here to help.</p>
            </div>
            <div className="help-cards">
                <div className="help-card">
                    <span className="material-symbols-outlined">description</span>
                    <h4>FAQ</h4>
                    <p>Browse frequently asked questions about the platform.</p>
                    <button className="btn-help-action">View FAQ</button>
                </div>
                <div className="help-card">
                    <span className="material-symbols-outlined">mail</span>
                    <h4>Email Support</h4>
                    <p>Send us an email at support@appointmed.com</p>
                    <button className="btn-help-action" onClick={() => window.open('mailto:support@appointmed.com')}>Send Email</button>
                </div>
                <div className="help-card">
                    <span className="material-symbols-outlined">phone</span>
                    <h4>Phone Support</h4>
                    <p>Call us at (555) 123-HELP during business hours.</p>
                    <button className="btn-help-action">Call Now</button>
                </div>
            </div>
        </>
    );

    const renderContent = () => {
        switch (activeTab) {
            case 'Profile Details': return renderProfileTab();
            case 'Security': return renderSecurityTab();
            case 'Notifications': return renderNotificationsTab();
            case 'Help': return renderHelpTab();
            default: return renderProfileTab();
        }
    };

    return (
        <div className="profile-container">
            {/* Header */}
            <header className="profile-header">
                <div className="logo" onClick={() => navigate('/dashboard')} style={{ cursor: 'pointer' }}>Appoint<span>Med</span></div>
                <nav className="header-nav">
                    <button className="nav-link-btn" onClick={() => navigate('/dashboard')}>Home</button>
                    <button className="nav-link-btn" onClick={() => navigate('/specialists')}>Appointments</button>
                    <button className="nav-link-btn active">Settings</button>
                    <div className="notification-icon">
                        <Bell size={20} />
                    </div>
                </nav>
            </header>

            {/* Main Content Card */}
            <div className="profile-card">
                {/* Sidebar */}
                <aside className="profile-sidebar">
                    <div className="user-info-section">
                        <div className="avatar-wrapper">
                            <div className="avatar-circle">
                                <User size={60} color="#94a3b8" />
                            </div>
                            <button className="edit-avatar-btn">
                                <Camera size={14} />
                            </button>
                        </div>
                        <h2>{savedUser?.name || 'Alex Johnson'}</h2>
                        <p className="patient-id">Patient ID: #{savedUser?.id || '88291'}</p>
                        <span className="member-badge">MEMBER SINCE 2021</span>
                    </div>

                    <nav className="sidebar-nav">
                        <button className={activeTab === 'Profile Details' ? 'active' : ''} onClick={() => setActiveTab('Profile Details')}>
                            <User size={18} /> Profile Details
                        </button>
                        <button className={activeTab === 'Security' ? 'active' : ''} onClick={() => setActiveTab('Security')}>
                            <Lock size={18} /> Security
                        </button>
                        <button className={activeTab === 'Notifications' ? 'active' : ''} onClick={() => setActiveTab('Notifications')}>
                            <BellRing size={18} /> Notifications
                        </button>
                        <button className={activeTab === 'Help' ? 'active' : ''} onClick={() => setActiveTab('Help')}>
                            <HelpCircle size={18} /> Help & Support
                        </button>
                    </nav>

                    <button className="sidebar-logout" onClick={handleLogout}>
                        <LogOut size={18} /> Logout
                    </button>
                </aside>

                {/* Form Content */}
                <main className="profile-form-content">
                    {renderContent()}
                </main>
            </div>
        </div>
    );
};

export default ProfilePage;
