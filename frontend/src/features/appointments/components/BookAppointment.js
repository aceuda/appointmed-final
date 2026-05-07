import React, { useState, useMemo, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth, useToast } from '../../../App';
import { doctorAPI, appointmentAPI } from '../../../shared/services/api';
import './BookAppointment.css';

function BookAppointment({ doctor, onConfirm }) {
    const navigate = useNavigate();
    const { user } = useAuth();
    const showToast = useToast();

    const [selectedDate, setSelectedDate] = useState(new Date().getDate());
    const [selectedSlot, setSelectedSlot] = useState(null);
    const [reason, setReason] = useState('');
    const [currentMonth, setCurrentMonth] = useState(new Date());
    const [allSlots, setAllSlots] = useState([]); // [{time, status, patientName?}]
    const [loadingSlots, setLoadingSlots] = useState(false);
    const [booking, setBooking] = useState(false);

    const year = currentMonth.getFullYear();
    const month = currentMonth.getMonth();
    const monthName = currentMonth.toLocaleString('default', { month: 'long' });

    const daysInMonth = new Date(year, month + 1, 0).getDate();
    const firstDayOfWeek = new Date(year, month, 1).getDay();

    const calendarCells = useMemo(() => {
        const cells = [];
        for (let i = 0; i < firstDayOfWeek; i++) cells.push(null);
        for (let d = 1; d <= daysInMonth; d++) cells.push(d);
        return cells;
    }, [firstDayOfWeek, daysInMonth]);

    const today = new Date();
    const isToday = (day) => day && year === today.getFullYear() && month === today.getMonth() && day === today.getDate();
    const isPast = (day) => {
        if (!day) return true;
        const cellDate = new Date(year, month, day);
        const todayClear = new Date(today.getFullYear(), today.getMonth(), today.getDate());
        return cellDate < todayClear;
    };

    // Fetch ALL slots with status when date is selected
    useEffect(() => {
        if (!selectedDate || !doctor) return;
        const fetchSlots = async () => {
            setLoadingSlots(true);
            try {
                const dateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(selectedDate).padStart(2, '0')}`;
                const res = await doctorAPI.getSlotsWithStatus(doctor.id, dateStr);
                setAllSlots(res.data);
            } catch {
                setAllSlots([]);
            }
            setLoadingSlots(false);
        };
        fetchSlots();
        setSelectedSlot(null);
    }, [selectedDate, doctor, year, month]);

    const prevMonth = () => setCurrentMonth(new Date(year, month - 1));
    const nextMonth = () => setCurrentMonth(new Date(year, month + 1));

    const formatTime = (time) => {
        const [h, m] = time.split(':');
        const hour = parseInt(h);
        const ampm = hour >= 12 ? 'PM' : 'AM';
        const h12 = hour % 12 || 12;
        return `${h12}:${m} ${ampm}`;
    };

    // Check if the selected date is today
    const isSelectedDateToday = year === today.getFullYear() && month === today.getMonth() && selectedDate === today.getDate();

    // Check if a slot's time has already passed (only relevant for today)
    const isSlotPast = (timeStr) => {
        if (!isSelectedDateToday) return false;
        const now = new Date();
        const [h, m] = timeStr.split(':').map(Number);
        const slotDate = new Date(year, month, selectedDate, h, m, 0);
        return slotDate <= now;
    };

    const morningSlots = allSlots.filter(s => parseInt(s.time.split(':')[0]) < 12);
    const afternoonSlots = allSlots.filter(s => parseInt(s.time.split(':')[0]) >= 12);

    const handleConfirm = async () => {
        if (!selectedDate || !selectedSlot) {
            showToast('Please select a date and time slot.', 'error');
            return;
        }
        setBooking(true);
        try {
            const dateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(selectedDate).padStart(2, '0')}`;
            await appointmentAPI.create({
                patientId: user.id,
                doctorId: doctor.id,
                appointmentDate: dateStr,
                appointmentTime: selectedSlot,
                reason: reason || 'General Consultation',
                fee: 1500
            });

            const bookingDetails = {
                doctor,
                date: `${monthName} ${selectedDate}, ${year}`,
                time: formatTime(selectedSlot),
                reason: reason || 'General Consultation'
            };
            if (onConfirm) onConfirm(bookingDetails);
            showToast('Appointment booked successfully!', 'success');
            navigate('/booking-confirmed');
        } catch (err) {
            const msg = err.response?.data?.message || 'Failed to book appointment';
            showToast(msg, 'error');
            // If slot was taken (409 Conflict), refresh slots to show updated availability
            if (err.response?.status === 409) {
                const dateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(selectedDate).padStart(2, '0')}`;
                try {
                    const res = await doctorAPI.getSlotsWithStatus(doctor.id, dateStr);
                    setAllSlots(res.data);
                    setSelectedSlot(null);
                } catch {}
            }
        }
        setBooking(false);
    };

    const step = !selectedDate ? 1 : !selectedSlot ? 2 : 3;

    const renderSlot = (slot) => {
        const isBooked = slot.status === 'booked';
        const isBlocked = slot.status === 'blocked';
        const isPastSlot = isSlotPast(slot.time);
        const isDisabled = isBooked || isBlocked || isPastSlot;
        const isSelected = selectedSlot === slot.time && !isDisabled;
        return (
            <button
                key={slot.time}
                className={`time-slot ${isSelected ? 'selected' : ''} ${isBooked ? 'booked' : ''} ${isBlocked ? 'blocked-unavailable' : ''} ${isPastSlot && !isBooked && !isBlocked ? 'past' : ''}`}
                onClick={() => !isDisabled && setSelectedSlot(slot.time)}
                disabled={isDisabled}
                title={isPastSlot ? 'This time has already passed' : isBooked ? 'Booked' : isBlocked ? 'Not available' : 'Available — click to select'}
            >
                <span className="slot-time-text">{formatTime(slot.time)}</span>
                {isBooked && <span className="slot-booked-badge">Booked</span>}
                {isBlocked && <span className="slot-unavailable-badge">Not Available</span>}
                {isPastSlot && !isBooked && !isBlocked && <span className="slot-past-badge">Past</span>}
            </button>
        );
    };

    if (!doctor) {
        return (
            <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', height: '100vh', fontFamily: 'Lexend' }}>
                <p style={{ color: '#64748b' }}>No doctor selected</p>
                <button onClick={() => navigate('/specialists')} style={{ marginTop: 12, padding: '10px 24px', borderRadius: 8, border: 'none', background: '#2563EB', color: '#fff', cursor: 'pointer' }}>
                    Browse Specialists
                </button>
            </div>
        );
    }

    return (
        <div className="booking-page">
            <header className="booking-topbar">
                <div className="booking-logo">
                    <span className="logo-dark">Appoint</span><span className="logo-blue">Med</span>
                </div>
                <div className="topbar-actions">
                    <button className="icon-btn"><span className="material-symbols-outlined">notifications</span></button>
                    <button className="icon-btn"><span className="material-symbols-outlined">calendar_month</span></button>
                </div>
            </header>

            <div className="booking-breadcrumb">
                <span className={step >= 1 ? 'bc-active' : ''}>Select Doctor</span>
                <span className="bc-sep">›</span>
                <span className={step >= 1 ? 'bc-bold' : ''}>Select Date &amp; Time</span>
                <span className="bc-sep">›</span>
                <span className={step >= 3 ? '' : 'bc-muted'}>Confirm Booking</span>
            </div>

            <div className="booking-layout">
                <aside className="booking-sidebar">
                    <div className="selected-doctor-card">
                        <p className="label-upper">SELECTED PROFESSIONAL</p>
                        <div className="doc-info">
                            <div className="doc-avatar"><span className="material-symbols-outlined">person</span></div>
                            <div>
                                <h3>{doctor.name}</h3>
                                <p className="doc-specialty">Senior {doctor.specialization || 'Specialist'}</p>
                                <p className="doc-exp">Experienced Practitioner</p>
                            </div>
                        </div>
                        <div className="doc-meta-list">
                            <p>★ {doctor.rating?.toFixed(1) || '4.5'} ({doctor.reviews || 0}+ reviews)</p>
                            <p><span className="material-symbols-outlined">location_on</span> {doctor.clinicAddress || 'Medical Center'}</p>
                            <p><span className="material-symbols-outlined">payments</span> ₱1,500 per visit</p>
                        </div>
                        <button className="btn-change-doc" onClick={() => navigate('/specialists')}>Change Doctor</button>
                    </div>
                    <div className="booking-note">
                        <p className="note-title"><span className="material-symbols-outlined">info</span> Booking Note</p>
                        <p>Cancellations are accepted up to 24 hours before the appointment. Please arrive 10 minutes early.</p>
                    </div>
                </aside>

                <div className="booking-content">
                    <section className="booking-section">
                        <h2><span className="step-num">1.</span> Select Date</h2>
                        <div className="calendar-header">
                            <button onClick={prevMonth} className="cal-nav">‹</button>
                            <span className="cal-month">{monthName} {year}</span>
                            <button onClick={nextMonth} className="cal-nav">›</button>
                        </div>
                        <div className="calendar-grid">
                            {['SUN', 'MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT'].map(d => (
                                <div key={d} className="cal-day-header">{d}</div>
                            ))}
                            {calendarCells.map((day, i) => (
                                <button key={i}
                                    className={`cal-day ${!day ? 'empty' : ''} ${selectedDate === day ? 'selected' : ''} ${isToday(day) ? 'today' : ''} ${isPast(day) ? 'past' : ''}`}
                                    onClick={() => day && !isPast(day) && setSelectedDate(day)}
                                    disabled={!day || isPast(day)}>
                                    {day}
                                </button>
                            ))}
                        </div>
                    </section>

                    <section className="booking-section">
                        <h2><span className="step-num">2.</span> Available Time Slots</h2>
                        {/* Slot legend */}
                        <div className="slot-legend">
                            <span className="legend-item"><span className="legend-dot available"></span> Available</span>
                            <span className="legend-item"><span className="legend-dot booked"></span> Booked</span>
                            <span className="legend-item"><span className="legend-dot unavailable-legend"></span> Not Available</span>
                            <span className="legend-item"><span className="legend-dot past-legend"></span> Past</span>
                            <span className="legend-item"><span className="legend-dot selected-legend"></span> Selected</span>
                        </div>
                        {loadingSlots ? (
                            <p style={{ color: '#94a3b8', padding: 16 }}>Loading available slots...</p>
                        ) : !selectedDate ? (
                            <p style={{ color: '#94a3b8', padding: 16 }}>Select a date to see available slots</p>
                        ) : (
                            <>
                                {morningSlots.length > 0 && (
                                    <div className="time-group">
                                        <p className="time-label"><span className="material-symbols-outlined">light_mode</span> MORNING</p>
                                        <div className="time-slots">
                                            {morningSlots.map(renderSlot)}
                                        </div>
                                    </div>
                                )}
                                {afternoonSlots.length > 0 && (
                                    <div className="time-group">
                                        <p className="time-label"><span className="material-symbols-outlined">dark_mode</span> AFTERNOON</p>
                                        <div className="time-slots">
                                            {afternoonSlots.map(renderSlot)}
                                        </div>
                                    </div>
                                )}
                                {morningSlots.length === 0 && afternoonSlots.length === 0 && (
                                    <p style={{ color: '#94a3b8', padding: 16 }}>No slots available for this date</p>
                                )}
                            </>
                        )}
                    </section>

                    <section className="booking-section">
                        <h2><span className="step-num">3.</span> Reason for Visit</h2>
                        <textarea className="reason-input" placeholder="Please describe your symptoms or reason for the appointment (optional)..."
                            value={reason} onChange={(e) => setReason(e.target.value)} rows={4} />
                    </section>

                    <div className="booking-actions">
                        <button className="btn-back" onClick={() => navigate('/specialists')}>
                            <span className="material-symbols-outlined">arrow_back</span> Back
                        </button>
                        <button className="btn-confirm" onClick={handleConfirm} disabled={booking}>
                            {booking ? 'Booking...' : 'Confirm Booking'} <span className="material-symbols-outlined">check_circle</span>
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default BookAppointment;
