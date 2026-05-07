import React, { useState } from 'react';
import { 
  User, Lock, Bell, HelpCircle, Camera, 
  Phone, MapPin, Calendar, Droplets, BellRing 
} from 'lucide-react';
import '../css/ProfilePage.css';
import { userAPI } from "../services/api";

    const ProfilePage = () => {
    const [activeTab, setActiveTab] = useState('Profile Details');
    const [newPassword, setNewPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");    
    
    const handleSaveChanges = async () => {
    if (newPassword !== confirmPassword) {
        alert("Passwords do not match!");
        return;
    }

    try {
        const savedUser = JSON.parse(localStorage.getItem("user"));
        
        if (!savedUser || !savedUser.id) {
            alert("Session expired. Please log in again.");
            return;
        }

        const updatedUserData = {
            ...savedUser,
            password: newPassword 
        };

        await userAPI.update(savedUser.id, updatedUserData); 
        
        alert("Password updated successfully!");
    
        setNewPassword("");
        setConfirmPassword("");
        
    } catch (error) {
        console.error("Update Error:", error.response?.data);
        alert("Failed to update: " + (error.response?.data?.message || "Check console for details"));
    }
};

  return (
    <div className="profile-container">
      {/* Header */}
      <header className="profile-header">
        <div className="logo">Appoint<span>Med</span></div>
        <nav className="header-nav">
          <a href="#">Home</a>
          <a href="#">Appointments</a>
          <a href="#" className="active">Settings</a>
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
            <h2>Alex Johnson</h2>
            <p className="patient-id">Patient ID: #88291</p>
            <span className="member-badge">MEMBER SINCE 2021</span>
          </div>

          <nav className="sidebar-nav">
            <button 
              className={activeTab === 'Profile Details' ? 'active' : ''} 
              onClick={() => setActiveTab('Profile Details')}
            >
              <User size={18} /> Profile Details
            </button>
            <button 
              className={activeTab === 'Security' ? 'active' : ''} 
              onClick={() => setActiveTab('Security')}
            >
              <Lock size={18} /> Security
            </button>
            <button 
              className={activeTab === 'Notifications' ? 'active' : ''} 
              onClick={() => setActiveTab('Notifications')}
            >
              <BellRing size={18} /> Notifications
            </button>
            <button 
              className={activeTab === 'Help' ? 'active' : ''} 
              onClick={() => setActiveTab('Help')}
            >
              <HelpCircle size={18} /> Help & Support
            </button>
          </nav>
        </aside>

        {/* Form Content */}
        <main className="profile-form-content">
          <div className="form-header">
            <h3>Personal Information</h3>
            <p>Update your personal details and how we can reach you.</p>
          </div>

          <div className="form-grid">
            <div className="input-group">
              <label>Phone Number</label>
              <div className="input-wrapper">
                <Phone size={18} className="input-icon" />
                <input type="text" defaultValue="+1 (555) 123-4567" />
              </div>
            </div>

            <div className="input-group full-width">
              <label>Home Address</label>
              <div className="input-wrapper">
                <MapPin size={18} className="input-icon" />
                <textarea defaultValue="123 Medical Plaza, Apartment 4B, New York, NY 10001" />
              </div>
            </div>

            <div className="input-group">
              <label>Date of Birth</label>
              <div className="input-wrapper">
                <Calendar size={18} className="input-icon" />
                <input type="text" defaultValue="May 14, 1992" />
              </div>
            </div>

            <div className="input-group">
              <label>Blood Type</label>
              <div className="input-wrapper">
                <Droplets size={18} className="input-icon" />
                <select defaultValue="O+">
                  <option value="O+">O+</option>
                  <option value="A+">A+</option>
                  <option value="B+">B+</option>
                  <option value="AB+">AB+</option>
                </select>
              </div>
            </div>
            <div className="form-header full-width" style={{ marginTop: '20px', borderTop: '1px solid #e2e8f0', paddingTop: '20px' }}>
                <h3>Security</h3>
                <p>Update your password to keep your account secure.</p>
            </div>

            <div className="input-group">
    <label>New Password</label>
    <div className="input-wrapper">
        <Lock size={18} className="input-icon" />
        <input 
            type="password" 
            placeholder="Enter new password" 
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
            />
        </div>
    </div>

        <div className="input-group">
            <label>Confirm Password</label>
            <div className="input-wrapper">
                <Lock size={18} className="input-icon" />
                    <input 
                        type="password" 
                        placeholder="Repeat new password" 
                        value={confirmPassword}
                        onChange={(e) => setConfirmPassword(e.target.value)}
                    />
                </div>
            </div>
          </div>

          <div className="form-actions">
            <button className="btn-cancel">Cancel</button>
            <button className="btn-save" onClick={handleSaveChanges}>Save Changes</button>
          </div>
        </main>
      </div>
    </div>
  );
};

export default ProfilePage;