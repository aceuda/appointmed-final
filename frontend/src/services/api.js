import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

const api = axios.create({
    baseURL: API_BASE_URL,
    headers: { 'Content-Type': 'application/json' },
});

// Attach JWT token to every request
api.interceptors.request.use((config) => {
    const token = localStorage.getItem('token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

// Handle 401 responses
api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401) {
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            if (window.location.pathname !== '/login') {
                window.location.href = '/login';
            }
        }
        return Promise.reject(error);
    }
);

// Auth API
export const authAPI = {
    login: (creds) => api.post('/auth/login', creds),
    register: (data) => api.post('/auth/register', data),
};

// User API
export const userAPI = {
    getAll: () => api.get('/users'),
    getById: (id) => api.get(`/users/${id}`),
    create: (userData) => api.post('/users', userData),
    update: (id, userData) => api.put(`/users/${id}`, userData),
    delete: (id) => api.delete(`/users/${id}`),
    getStats: (id) => api.get(`/users/${id}/stats`),
    changePassword: (id, data) => api.post(`/users/${id}/change-password`, data),
};

// Doctor API
export const doctorAPI = {
    getAll: (spec) => api.get('/doctors', { params: spec ? { spec } : {} }),
    getById: (id) => api.get(`/doctors/${id}`),
    search: (q, spec) => api.get('/doctors/search', { params: { q, spec } }),
    getSpecializations: () => api.get('/doctors/specializations'),
    getSlots: (id, date) => api.get(`/doctors/${id}/slots`, { params: { date } }),
    getSlotsWithStatus: (id, date) => api.get(`/doctors/${id}/slots-status`, { params: { date } }),
    toggleSlot: (id, date, time) => api.put(`/doctors/${id}/slots/toggle`, null, { params: { date, time } }),
    getSchedule: (id) => api.get(`/doctors/${id}/schedule`),
    updateSchedule: (id, schedules) => api.put(`/doctors/${id}/schedule`, schedules),
    getByUserId: (userId) => api.get(`/doctors/user/${userId}`),
    updateProfile: (id, data) => api.put(`/doctors/${id}/profile`, data),
};

// Appointment API
export const appointmentAPI = {
    create: (data) => api.post('/appointments', data),
    getAll: () => api.get('/appointments'),
    getById: (id) => api.get(`/appointments/${id}`),
    update: (id, data) => api.put(`/appointments/${id}`, data),
    cancel: (id) => api.put(`/appointments/${id}/cancel`),
    confirm: (id) => api.put(`/appointments/${id}/confirm`),
    complete: (id) => api.put(`/appointments/${id}/complete`),
    getByPatient: (patientId, status) =>
        api.get(`/appointments/patient/${patientId}`, { params: status ? { status } : {} }),
    getByDoctor: (doctorId, date) =>
        api.get(`/appointments/doctor/${doctorId}`, { params: date ? { date } : {} }),
};

// Notification API
export const notificationAPI = {
    getByUser: (userId) => api.get(`/notifications/user/${userId}`),
    getUnread: (userId) => api.get(`/notifications/user/${userId}/unread`),
    markAsRead: (id) => api.put(`/notifications/${id}/read`),
    markAllAsRead: (userId) => api.put(`/notifications/user/${userId}/read-all`),
    send: (data) => api.post('/notifications/send', data),
};

// Message API
export const messageAPI = {
    send: (data) => api.post('/messages', data),
    getConversations: (userId) => api.get(`/messages/conversations/${userId}`),
    getConversation: (user1, user2) => api.get(`/messages/conversation/${user1}/${user2}`),
    markAsRead: (userId, partnerId) => api.put(`/messages/read/${userId}/${partnerId}`),
    getUnreadCount: (userId) => api.get(`/messages/unread/${userId}`),
};

export default api;