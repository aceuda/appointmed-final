import React, { useState, useEffect } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { AuthContext, ToastContext, useAuth, useToast } from './contexts';
import { LoginPage, RegisterPage } from './features/auth';
import { DoctorDashboard, PatientDashboard } from './features/dashboard';
import { ProfilePage } from './features/profile';
import { SelectSpecialist, BookAppointment, BookingConfirmed, AppointmentsPage } from './features/appointments';
import { NotificationsPage } from './features/notifications';
import './App.css';

// Re-export hooks for backward compatibility with any direct App imports
export { useAuth, useToast };

function Toast({ toasts, removeToast }) {
  return (
    <div style={{ position: 'fixed', top: 20, right: 20, zIndex: 9999, display: 'flex', flexDirection: 'column', gap: 8 }}>
      {toasts.map(t => (
        <div key={t.id} onClick={() => removeToast(t.id)} style={{
          padding: '12px 20px', borderRadius: 10, color: '#fff', fontSize: 14, fontFamily: 'Lexend,sans-serif',
          cursor: 'pointer', minWidth: 280, boxShadow: '0 4px 20px rgba(0,0,0,0.15)',
          animation: 'slideIn 0.3s ease',
          background: t.type === 'success' ? 'linear-gradient(135deg,#10B981,#059669)' :
            t.type === 'error' ? 'linear-gradient(135deg,#EF4444,#DC2626)' :
              'linear-gradient(135deg,#3B82F6,#2563EB)'
        }}>
          {t.message}
        </div>
      ))}
    </div>
  );
}

function App() {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [toasts, setToasts] = useState([]);
  const [selectedDoctor, setSelectedDoctor] = useState(null);
  const [bookingDetails, setBookingDetails] = useState(null);

  useEffect(() => {
    const stored = localStorage.getItem('user');
    const token = localStorage.getItem('token');
    if (stored && token) {
      try { setUser(JSON.parse(stored)); } catch { }
    }
    setLoading(false);
  }, []);

  const showToast = (message, type = 'info') => {
    const id = Date.now();
    setToasts(prev => [...prev, { id, message, type }]);
    setTimeout(() => setToasts(prev => prev.filter(t => t.id !== id)), 4000);
  };

  const handleLogin = (authResponse) => {
    const userData = {
      id: authResponse.id,
      name: authResponse.name,
      email: authResponse.email,
      role: authResponse.role,
      avatarUrl: authResponse.avatarUrl,
      avatarData: authResponse.avatarData || null,
    };
    localStorage.setItem('token', authResponse.token);
    localStorage.setItem('user', JSON.stringify(userData));
    setUser(userData);
    showToast(`Welcome back, ${userData.name}!`, 'success');
  };

  const handleLogout = () => {
    localStorage.removeItem('user');
    localStorage.removeItem('token');
    setUser(null);
    setSelectedDoctor(null);
    setBookingDetails(null);
    showToast('Logged out successfully', 'info');
  };

  const updateUser = (updatedUser) => {
    localStorage.setItem('user', JSON.stringify(updatedUser));
    setUser(updatedUser);
  };

  if (loading) return <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', fontFamily: 'Lexend' }}>Loading...</div>;

  return (
    <AuthContext.Provider value={{ user, handleLogin, handleLogout, updateUser }}>
      <ToastContext.Provider value={showToast}>
        <Toast toasts={toasts} removeToast={(id) => setToasts(prev => prev.filter(t => t.id !== id))} />
        <Routes>
          {/* Public routes */}
          <Route path="/login" element={!user ? <LoginPage /> : <Navigate to="/dashboard" />} />
          <Route path="/register" element={!user ? <RegisterPage /> : <Navigate to="/dashboard" />} />

          {/* Protected routes */}
          <Route path="/dashboard" element={
            !user ? <Navigate to="/login" /> :
              user.role === 'DOCTOR' ? <DoctorDashboard /> :
                <PatientDashboard onSelectDoctor={(doc) => setSelectedDoctor(doc)} />
          } />
          <Route path="/profile" element={!user ? <Navigate to="/login" /> : <ProfilePage />} />
          <Route path="/appointments" element={!user ? <Navigate to="/login" /> : <AppointmentsPage />} />
          <Route path="/specialists" element={!user ? <Navigate to="/login" /> :
            <SelectSpecialist onSelectDoctor={(doc) => setSelectedDoctor(doc)} />
          } />
          <Route path="/book-appointment" element={!user ? <Navigate to="/login" /> :
            <BookAppointment
              doctor={selectedDoctor}
              onConfirm={(details) => setBookingDetails(details)}
            />
          } />
          <Route path="/booking-confirmed" element={!user ? <Navigate to="/login" /> :
            <BookingConfirmed booking={bookingDetails} />
          } />
          <Route path="/notifications" element={!user ? <Navigate to="/login" /> : <NotificationsPage />} />

          {/* Redirects */}
          <Route path="/" element={<Navigate to={user ? "/dashboard" : "/login"} />} />
          <Route path="*" element={
            <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', height: '100vh', fontFamily: 'Lexend', color: '#64748b' }}>
              <h1 style={{ fontSize: 72, margin: 0, color: '#2563EB' }}>404</h1>
              <p style={{ fontSize: 18 }}>Page not found</p>
              <a href={user ? "/dashboard" : "/login"} style={{ color: '#2563EB', textDecoration: 'none', marginTop: 16 }}>
                ← Go back
              </a>
            </div>
          } />
        </Routes>
      </ToastContext.Provider>
    </AuthContext.Provider>
  );
}

export default App;
