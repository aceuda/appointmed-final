import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth, useToast } from '../../../contexts';
import {
  User, Lock, Bell, HelpCircle, Camera,
  Phone, MapPin, Calendar, Droplets, BellRing, LogOut, Save, X, DollarSign, Stethoscope, Building2
} from 'lucide-react';
import './ProfilePage.css';
import { userAPI, doctorAPI } from "../../../shared/services/api";

const ProfilePage = () => {
    const navigate = useNavigate();
    const { user: authUser, handleLogout: authLogout, updateUser } = useAuth();
    const showToast = useToast();
    const [activeTab, setActiveTab] = useState('Profile Details');

    // Profile form fields
    const [profileName, setProfileName] = useState('');
    const [profileEmail, setProfileEmail] = useState('');
    const [profilePhone, setProfilePhone] = useState('');
    const [profileAddress, setProfileAddress] = useState('');
    const [profileDob, setProfileDob] = useState('');
    const [profileBlood, setProfileBlood] = useState('');
    const [profileDirty, setProfileDirty] = useState(false);
    const [profileSaving, setProfileSaving] = useState(false);
    const [profileMsg, setProfileMsg] = useState({ type: '', text: '' });

    // Avatar state
    const [avatarPreview, setAvatarPreview] = useState(null);
    const [avatarData, setAvatarData] = useState(null);

    // Security form fields
    const [currentPassword, setCurrentPassword] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [secSaving, setSecSaving] = useState(false);
    const [secMsg, setSecMsg] = useState({ type: '', text: '' });

    // Doctor-specific settings
    const [doctorProfile, setDoctorProfile] = useState(null);
    const [consultationFee, setConsultationFee] = useState('');
    const [clinicAddress, setClinicAddress] = useState('');
    const [docSpecialization, setDocSpecialization] = useState('');
    const [docPhone, setDocPhone] = useState('');
    const [docSaving, setDocSaving] = useState(false);
    const [docMsg, setDocMsg] = useState({ type: '', text: '' });

    // Notification settings
    const [notifEmail, setNotifEmail] = useState(true);
    const [notifSms, setNotifSms] = useState(false);
    const [notifAppointment, setNotifAppointment] = useState(true);
    const [notifPromo, setNotifPromo] = useState(false);

    const savedUser = authUser;
    const isDoctor = savedUser?.role === 'DOCTOR';

    useEffect(() => {
        if (!savedUser) return;

        // Fetch full user data from backend
        const fetchUserData = async () => {
            try {
                const res = await userAPI.getById(savedUser.id);
                const u = res.data;
                setProfileName(u.name || '');
                setProfileEmail(u.email || '');
                setProfilePhone(u.phone || '');
                setProfileAddress(u.address || '');
                setProfileDob(u.birthDate || '');
                setProfileBlood(u.bloodType || '');
                if (u.avatarData) {
                    setAvatarPreview(u.avatarData);
                    setAvatarData(u.avatarData);
                } else if (u.avatarUrl) {
                    setAvatarPreview(u.avatarUrl);
                }
            } catch (err) {
                // Fallback to auth context
                setProfileName(savedUser.name || '');
                setProfileEmail(savedUser.email || '');
            }
        };
        fetchUserData();

        // If doctor, fetch doctor profile
        if (isDoctor) {
            const fetchDoctorProfile = async () => {
                try {
                    const res = await doctorAPI.getByUserId(savedUser.id);
                    setDoctorProfile(res.data);
                    setConsultationFee(res.data.consultationFee || '');
                    setClinicAddress(res.data.clinicAddress || '');
                    setDocSpecialization(res.data.specialization || '');
                    setDocPhone(res.data.phone || '');
                } catch (err) {
                    console.error('Failed to load doctor profile:', err);
                }
            };
            fetchDoctorProfile();
        }
    }, []);

    // Track changes
    useEffect(() => {
        if (savedUser) {
            setProfileDirty(true);
        }
    }, [profileName, profileEmail, profilePhone, profileAddress, profileDob, profileBlood, avatarData]);

    const handleAvatarUpload = (e) => {
        const file = e.target.files[0];
        if (!file) return;

        if (file.size > 2 * 1024 * 1024) {
            showToast('Image must be under 2MB', 'error');
            return;
        }

        const reader = new FileReader();
        reader.onload = (event) => {
            const base64 = event.target.result;
            setAvatarPreview(base64);
            setAvatarData(base64);
            setProfileDirty(true);
        };
        reader.readAsDataURL(file);
    };

    const handleProfileSave = async () => {
        setProfileMsg({ type: '', text: '' });
        if (!profileName.trim() || !profileEmail.trim()) {
            setProfileMsg({ type: 'error', text: 'Name and email are required.' });
            return;
        }
        setProfileSaving(true);
        try {
            const payload = {
                ...savedUser,
                name: profileName,
                email: profileEmail,
                phone: profilePhone,
                address: profileAddress,
                birthDate: profileDob,
                bloodType: profileBlood,
            };
            if (avatarData) {
                payload.avatarData = avatarData;
            }
            await userAPI.update(savedUser.id, payload);
            const updatedCtx = {
                ...savedUser,
                name: profileName,
                email: profileEmail,
                avatarUrl: avatarData || savedUser.avatarUrl,
            };
            updateUser(updatedCtx);
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
        setSecSaving(true);
        try {
            await userAPI.changePassword(savedUser.id, {
                currentPassword: currentPassword,
                newPassword: newPassword,
            });
            showToast('Password updated successfully!', 'success');
            setCurrentPassword(''); setNewPassword(''); setConfirmPassword('');
        } catch (err) {
            setSecMsg({ type: 'error', text: err.response?.data?.message || 'Failed to update password. Is your current password correct?' });
        }
        setSecSaving(false);
    };

    const handleDoctorProfileSave = async () => {
        if (!doctorProfile) return;
        setDocMsg({ type: '', text: '' });
        setDocSaving(true);
        try {
            const res = await doctorAPI.updateProfile(doctorProfile.id, {
                specialization: docSpecialization,
                phone: docPhone,
                clinicAddress: clinicAddress,
                consultationFee: parseFloat(consultationFee) || 0,
            });
            setDoctorProfile(res.data);
            showToast('Doctor profile updated!', 'success');
        } catch (err) {
            setDocMsg({ type: 'error', text: err.response?.data?.message || 'Failed to update doctor profile.' });
        }
        setDocSaving(false);
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
                        <input type="text" value={profilePhone} onChange={(e) => setProfilePhone(e.target.value)} placeholder="Enter phone number" />
                    </div>
                </div>
                <div className="input-group full-width">
                    <label>Home Address</label>
                    <div className="input-wrapper">
                        <MapPin size={18} className="input-icon" />
                        <textarea value={profileAddress} onChange={(e) => setProfileAddress(e.target.value)} placeholder="Enter your address" />
                    </div>
                </div>
                <div className="input-group">
                    <label>Date of Birth</label>
                    <div className="input-wrapper">
                        <Calendar size={18} className="input-icon" />
                        <input type="date" value={profileDob} onChange={(e) => setProfileDob(e.target.value)} />
                    </div>
                </div>
                <div className="input-group">
                    <label>Blood Type</label>
                    <div className="input-wrapper">
                        <Droplets size={18} className="input-icon" />
                        <select value={profileBlood} onChange={(e) => setProfileBlood(e.target.value)}>
                            <option value="">Select</option>
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

    const renderDoctorSettingsTab = () => (
        <>
            <div className="form-header">
                <h3>Doctor Settings</h3>
                <p>Manage your consultation fee, clinic details, and specialization.</p>
            </div>
            {docMsg.text && (
                <div className={`profile-alert ${docMsg.type}`}>{docMsg.text}</div>
            )}
            <div className="form-grid">
                <div className="input-group">
                    <label>Consultation Fee (₱)</label>
                    <div className="input-wrapper">
                        <DollarSign size={18} className="input-icon" />
                        <input type="number" step="0.01" min="0" value={consultationFee} onChange={(e) => setConsultationFee(e.target.value)} placeholder="e.g. 1500.00" />
                    </div>
                </div>
                <div className="input-group">
                    <label>Specialization</label>
                    <div className="input-wrapper">
                        <Stethoscope size={18} className="input-icon" />
                        <input type="text" value={docSpecialization} onChange={(e) => setDocSpecialization(e.target.value)} placeholder="e.g. Cardiology" />
                    </div>
                </div>
                <div className="input-group full-width">
                    <label>Clinic Address</label>
                    <div className="input-wrapper">
                        <Building2 size={18} className="input-icon" />
                        <textarea value={clinicAddress} onChange={(e) => setClinicAddress(e.target.value)} placeholder="Enter your clinic address" />
                    </div>
                </div>
                <div className="input-group">
                    <label>Clinic Phone</label>
                    <div className="input-wrapper">
                        <Phone size={18} className="input-icon" />
                        <input type="text" value={docPhone} onChange={(e) => setDocPhone(e.target.value)} placeholder="Clinic phone number" />
                    </div>
                </div>
            </div>
            <div className="form-actions">
                <button className="btn-save" onClick={handleDoctorProfileSave} disabled={docSaving}>
                    <Save size={16} /> {docSaving ? 'Saving...' : 'Save Doctor Settings'}
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
            case 'Doctor Settings': return renderDoctorSettingsTab();
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
                    <button className="nav-link-btn" onClick={() => navigate(isDoctor ? '/appointments' : '/specialists')}>Appointments</button>
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
                                {avatarPreview ? (
                                    <img src={avatarPreview} alt="Profile" className="avatar-img" />
                                ) : (
                                    <User size={60} color="#94a3b8" />
                                )}
                            </div>
                            <label className="edit-avatar-btn" htmlFor="avatar-upload" style={{ cursor: 'pointer' }}>
                                <Camera size={14} />
                            </label>
                            <input
                                id="avatar-upload"
                                type="file"
                                accept="image/*"
                                style={{ display: 'none' }}
                                onChange={handleAvatarUpload}
                            />
                        </div>
                        <h2>{savedUser?.name || 'User'}</h2>
                        <p className="patient-id">{isDoctor ? 'Doctor' : 'Patient'} ID: #{savedUser?.id || '0'}</p>
                        <span className="member-badge">{isDoctor ? 'DOCTOR' : 'PATIENT'}</span>
                    </div>

                    <nav className="sidebar-nav">
                        <button className={activeTab === 'Profile Details' ? 'active' : ''} onClick={() => setActiveTab('Profile Details')}>
                            <User size={18} /> Profile Details
                        </button>
                        <button className={activeTab === 'Security' ? 'active' : ''} onClick={() => setActiveTab('Security')}>
                            <Lock size={18} /> Security
                        </button>
                        {isDoctor && (
                            <button className={activeTab === 'Doctor Settings' ? 'active' : ''} onClick={() => setActiveTab('Doctor Settings')}>
                                <Stethoscope size={18} /> Doctor Settings
                            </button>
                        )}
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
