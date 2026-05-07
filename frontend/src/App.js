import React, { useState, useEffect } from 'react';
import LoginPage from './components/LoginPage';
import RegisterPage from './components/RegisterPage';
import DoctorDashboard from './components/DoctorDashboard';
import PatientDashboard from './components/PatientDashboard';
import ProfilePage from './components/ProfilePage';

function App() {
  const [user, setUser] = useState(null);
  const [showRegister, setShowRegister] = useState(false);
  const [currentView, setCurrentView] = useState('DASHBOARD');

  useEffect(() => {
    const stored = localStorage.getItem('user');
    if (stored) {
      try {
        setUser(JSON.parse(stored));
      } catch { }
    }
  }, []);

  const handleLogin = (u) => {
    localStorage.setItem('user', JSON.stringify(u));
    setUser(u);
  };

  const handleLogout = () => {
    localStorage.removeItem('user');
    setUser(null);
    setShowRegister(false);
    setCurrentView('DASHBOARD');
  };

  if (user) {
    if (currentView === 'PROFILE') {
      return (
        <ProfilePage 
          user={user} 
          onBack={() => setCurrentView('DASHBOARD')} 
        />
      );
    }
    return user.role === 'DOCTOR' ? (
      <DoctorDashboard onLogout={handleLogout} />
    ) : (
      <PatientDashboard onLogout={handleLogout} onNavigateProfile={() => setCurrentView('PROFILE')} />
    );
  }
  
  return showRegister ? (
    <RegisterPage onSwitch={() => setShowRegister(false)} />
  ) : (
    <LoginPage onLogin={handleLogin} onSwitch={() => setShowRegister(true)} />
  );
}

export default App;
