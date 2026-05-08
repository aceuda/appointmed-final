import React, { useState, useMemo, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../../contexts';
import { doctorAPI } from '../../../shared/services/api';
import './SelectSpecialist.css';

function SelectSpecialist({ onSelectDoctor }) {
    const navigate = useNavigate();
    const { user, handleLogout } = useAuth();
    const [doctors, setDoctors] = useState([]);
    const [specializations, setSpecializations] = useState([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [activeFilter, setActiveFilter] = useState('All Specialists');
    const [visibleCount, setVisibleCount] = useState(8);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchData = async () => {
            try {
                const [docRes, specRes] = await Promise.all([
                    doctorAPI.getAll(),
                    doctorAPI.getSpecializations()
                ]);
                setDoctors(docRes.data);
                setSpecializations(['All Specialists', ...specRes.data]);
            } catch (err) {
                console.error('Failed to load doctors:', err);
            }
            setLoading(false);
        };
        fetchData();
    }, []);

    const filteredDoctors = useMemo(() => {
        return doctors.filter(doc => {
            const matchSearch = doc.name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
                doc.specialization?.toLowerCase().includes(searchTerm.toLowerCase()) ||
                doc.clinicAddress?.toLowerCase().includes(searchTerm.toLowerCase());
            const matchFilter = activeFilter === 'All Specialists' || doc.specialization === activeFilter;
            return matchSearch && matchFilter;
        });
    }, [doctors, searchTerm, activeFilter]);

    const totalFound = filteredDoctors.length;
    const visibleDoctors = filteredDoctors.slice(0, visibleCount);

    const handleSelect = (doctor) => {
        if (onSelectDoctor) onSelectDoctor(doctor);
        navigate('/book-appointment');
    };

    const formatFee = (fee) => {
        if (!fee && fee !== 0) return '—';
        return `₱${Number(fee).toLocaleString('en-PH', { minimumFractionDigits: 2 })}`;
    };

    return (
        <div className="specialist-page">
            <aside className="specialist-sidebar">
                <nav className="sidebar-nav">
                    <button className="nav-item" onClick={() => navigate('/dashboard')}>
                        <span className="material-symbols-outlined">dashboard</span> Dashboard
                    </button>
                    <button className="nav-item" onClick={() => navigate('/appointments')}>
                        <span className="material-symbols-outlined">calendar_today</span> Appointments
                    </button>
                    <button className="nav-item active">
                        <span className="material-symbols-outlined">stethoscope</span> Specialists
                    </button>
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

            <main className="specialist-main">
                <div className="specialist-header">
                    <h1>Select Specialist</h1>
                    <p>Find and book the right healthcare professional for your needs.</p>
                </div>

                <div className="search-container">
                    <span className="material-symbols-outlined search-icon">search</span>
                    <input type="text" placeholder="Search doctors, specialties, or clinics"
                        value={searchTerm} onChange={(e) => setSearchTerm(e.target.value)} />
                </div>

                <div className="filter-chips">
                    {specializations.map(spec => (
                        <button key={spec} className={`chip ${activeFilter === spec ? 'active' : ''}`}
                            onClick={() => { setActiveFilter(spec); setVisibleCount(8); }}>
                            {spec}
                        </button>
                    ))}
                </div>

                <div className="results-header">
                    <h2>Available Specialists</h2>
                    <span className="results-count">{totalFound} practitioners found</span>
                </div>

                <div className="doctors-grid">
                    {loading ? (
                        <div style={{gridColumn:'1/-1',textAlign:'center',padding:40,color:'#94a3b8'}}>Loading specialists...</div>
                    ) : visibleDoctors.length === 0 ? (
                        <div style={{gridColumn:'1/-1',textAlign:'center',padding:40,color:'#94a3b8'}}>
                            <span className="material-symbols-outlined" style={{fontSize:48}}>person_search</span>
                            <p>No specialists found matching your criteria</p>
                        </div>
                    ) : (
                        visibleDoctors.map(doc => (
                            <div className="doctor-card" key={doc.id}>
                                <div className="card-avatar-wrapper">
                                    <div className="card-avatar">
                                        {doc.user?.avatarData || (doc.avatarUrl && doc.avatarUrl !== 'null') ? (
                                            <img src={doc.user?.avatarData || doc.avatarUrl} alt={doc.name} style={{ width: '100%', height: '100%', borderRadius: '50%', objectFit: 'cover' }} />
                                        ) : (
                                            <span className="material-symbols-outlined">person</span>
                                        )}
                                    </div>
                                    {doc.available && <span className="online-dot"></span>}
                                </div>
                                <h4>{doc.name}</h4>
                                <p className="card-specialty">{doc.specialization}</p>
                                <div className="card-meta-details">
                                    {doc.clinicAddress && (
                                        <p className="card-clinic"><span className="material-symbols-outlined" style={{fontSize:14}}>location_on</span> {doc.clinicAddress}</p>
                                    )}
                                    <p className="card-fee"><span className="material-symbols-outlined" style={{fontSize:14}}>payments</span> {formatFee(doc.consultationFee)} / visit</p>
                                </div>
                                <div className="card-rating">
                                    <span className="star">★</span> {doc.rating?.toFixed(1) || '4.5'} <span className="review-count">({doc.reviews || 0} reviews)</span>
                                </div>
                                <div className="card-status-row">
                                    <span className={`availability-badge ${doc.available ? 'avail-yes' : 'avail-no'}`}>
                                        {doc.available ? '● Available' : '● Unavailable'}
                                    </span>
                                </div>
                                <button className="btn-select" onClick={() => handleSelect(doc)}>Book Appointment</button>
                            </div>
                        ))
                    )}
                </div>

                {visibleCount < totalFound && (
                    <div className="load-more-container">
                        <button className="btn-load-more" onClick={() => setVisibleCount(prev => prev + 8)}>
                            Load More Specialists
                        </button>
                    </div>
                )}
            </main>
        </div>
    );
}

export default SelectSpecialist;
