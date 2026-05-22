import React from 'react';
import { useNavigate } from 'react-router-dom';
import './BookingConfirmed.css';

function BookingConfirmed({ booking }) {
    const navigate = useNavigate();
    const handleAddToCalendar = () => {
        const title = `Appointment with ${booking?.doctor?.name || 'Doctor'}`;
        const details = `${booking?.doctor?.specialty || ''} appointment - ${booking?.reason || 'General consultation'}`;
        const dateStr = booking?.date || 'October 24, 2023';

        // Create a Google Calendar link
        const url = `https://calendar.google.com/calendar/render?action=TEMPLATE&text=${encodeURIComponent(title)}&details=${encodeURIComponent(details)}&dates=${encodeURIComponent(dateStr)}`;
        window.open(url, '_blank');
    };

    return (
        <div className="confirmed-page">
            <header className="confirmed-topbar">
                <div className="confirmed-logo">
                    <span className="logo-dark">Appoint</span><span className="logo-blue">Med</span>
                </div>
                <div className="topbar-actions">
                    <button className="icon-btn"><span className="material-symbols-outlined">notifications</span></button>
                    <button className="icon-btn"><span className="material-symbols-outlined">account_circle</span></button>
                </div>
            </header>

            <main className="confirmed-main">
                <div className="confirmed-card">
                    <div className="success-icon">
                        <span className="material-symbols-outlined">check_circle</span>
                    </div>
                    <h1>Booking Confirmed!</h1>
                    <p className="confirmed-subtitle">Your appointment has been successfully scheduled and added to our system.</p>

                    <div className="appointment-details">
                        <p className="details-label">APPOINTMENT DETAILS</p>

                        <div className="details-doctor">
                            <div className="details-doc-avatar">
                                <span className="material-symbols-outlined">clinical_notes</span>
                            </div>
                            <div>
                                <h3>{booking?.doctor?.name || 'Dr. Sarah Henderson'}</h3>
                                <p>{booking?.doctor?.specialty ? `Senior ${booking.doctor.specialty}` : 'Senior Cardiologist'}</p>
                            </div>
                        </div>

                        <div className="details-row">
                            <div className="detail-item">
                                <span className="detail-label"><span className="material-symbols-outlined">calendar_month</span> Date</span>
                                <span className="detail-value">{booking?.date || 'October 24, 2023'}</span>
                            </div>
                            <div className="detail-item">
                                <span className="detail-label"><span className="material-symbols-outlined">schedule</span> Time</span>
                                <span className="detail-value">{booking?.time || '10:30 AM'} (EST)</span>
                            </div>
                        </div>

                        <div className="detail-item location-item">
                            <span className="detail-label"><span className="material-symbols-outlined">location_on</span> Location</span>
                            <span className="detail-value">{booking?.doctor?.clinicAddress || 'Medical Center'}</span>
                        </div>
                        <div className="detail-item location-item">
                            <span className="detail-label"><span className="material-symbols-outlined">payments</span> Fee</span>
                            <span className="detail-value">₱{Number(booking?.doctor?.consultationFee || 0).toLocaleString('en-PH', { minimumFractionDigits: 2 })}</span>
                        </div>
                    </div>

                    <div className="confirmed-actions">
                        <button className="btn-calendar" onClick={handleAddToCalendar}>
                            <span className="material-symbols-outlined">event</span> Add to Calendar
                        </button>
                        <button className="btn-dashboard" onClick={() => navigate('/dashboard')}>
                            <span className="material-symbols-outlined">dashboard</span> Go to Dashboard
                        </button>
                    </div>

                    <p className="reschedule-link">
                        <span className="material-symbols-outlined">schedule</span> Need to reschedule or cancel?
                    </p>
                </div>

                <div className="info-cards">
                    <div className="info-card">
                        <span className="material-symbols-outlined">info</span>
                        <div>
                            <h4>Preparation</h4>
                            <p>Please arrive 15 minutes early with your ID and insurance card.</p>
                        </div>
                    </div>
                    <div className="info-card">
                        <span className="material-symbols-outlined">mail</span>
                        <div>
                            <h4>Confirmation Email</h4>
                            <p>We've sent a detailed summary to your registered email address.</p>
                        </div>
                    </div>
                </div>
            </main>
        </div>
    );
}

export default BookingConfirmed;
