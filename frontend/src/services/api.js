import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

// User API calls
export const userAPI = {
    // GET all users
    getAll: () => api.get('/users'),

    // GET user by ID
    getById: (id) => api.get(`/users/${id}`),

    // POST create user
    create: (userData) => api.post('/users', userData),

    // PUT update user
    update: (id, userData) => api.put(`/users/${id}`, userData),

    // DELETE user
    delete: (id) => api.delete(`/users/${id}`),
    // auth endpoints
    register: (data) => api.post('/users/register', data),
    login: (creds) => api.post('/users/login', creds),

    update: (id, userData) => api.put(`/users/${id}`, userData),
};

export default api;