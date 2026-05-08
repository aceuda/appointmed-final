import React, { useState, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth, useToast } from '../../../contexts';
import { userAPI } from '../../../shared/services/api';
import './AdminDashboard.css';

const ITEMS_PER_PAGE = 4;

function AdminDashboard() {
    const navigate = useNavigate();
    const { user: authUser, handleLogout } = useAuth();
    const showToast = useToast();
    const savedUser = authUser;
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(false);
    const [searchTerm, setSearchTerm] = useState('');
    const [currentPage, setCurrentPage] = useState(1);
    const [showAddModal, setShowAddModal] = useState(false);
    const [editUser, setEditUser] = useState(null);
    const [formData, setFormData] = useState({ name: '', email: '', password: '', role: 'PATIENT' });
    const [formError, setFormError] = useState('');

    useEffect(() => { fetchUsers(); }, []);

    const fetchUsers = async () => {
        setLoading(true);
        try {
            const res = await userAPI.getAll();
            setUsers(res.data);
        } catch (err) {
            console.error('Failed to fetch users:', err);
        }
        setLoading(false);
    };

    const filteredUsers = useMemo(() => {
        const q = searchTerm.toLowerCase();
        return users.filter(u => u.name?.toLowerCase().includes(q) || u.email?.toLowerCase().includes(q) || u.role?.toLowerCase().includes(q));
    }, [users, searchTerm]);

    const totalPages = Math.ceil(filteredUsers.length / ITEMS_PER_PAGE);
    const paginatedUsers = filteredUsers.slice((currentPage - 1) * ITEMS_PER_PAGE, currentPage * ITEMS_PER_PAGE);

    const stats = useMemo(() => ({
        doctors: users.filter(u => u.role === 'DOCTOR').length,
        patients: users.filter(u => u.role === 'PATIENT').length,
        total: users.length,
    }), [users]);

    const handleOpenAdd = () => {
        setEditUser(null);
        setFormData({ name: '', email: '', password: '', role: 'PATIENT' });
        setFormError('');
        setShowAddModal(true);
    };

    const handleOpenEdit = (user) => {
        setEditUser(user);
        setFormData({ name: user.name, email: user.email, password: user.password || '', role: user.role });
        setFormError('');
        setShowAddModal(true);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!formData.name || !formData.email || !formData.password) {
            setFormError('All fields are required.');
            return;
        }
        try {
            if (editUser) {
                await userAPI.update(editUser.id, formData);
            } else {
                await userAPI.create(formData);
            }
            setShowAddModal(false);
            fetchUsers();
        } catch (err) {
            setFormError(err.response?.data?.message || 'Operation failed.');
        }
    };

    const handleDelete = async (id) => {
        if (window.confirm('Are you sure you want to delete this user?')) {
            try {
                await userAPI.delete(id);
                fetchUsers();
            } catch (err) {
                alert('Failed to delete user.');
            }
        }
    };

    const getRoleBadgeClass = (role) => {
        switch (role) {
            case 'DOCTOR': return 'role-doctor';
            case 'PATIENT': return 'role-patient';
            case 'ADMIN': return 'role-admin';
            default: return 'role-staff';
        }
    };

    return (
        <div className="admin-page">
            {/* Sidebar */}
            <aside className="admin-sidebar">
                <div className="sidebar-logo">
                    <span className="logo-dark">Appoint</span><span className="logo-blue">Med</span>
                </div>
                <nav className="sidebar-nav">
                    <button className="nav-item" onClick={() => navigate('/dashboard')}>
                        <span className="material-symbols-outlined">dashboard</span> Dashboard
                    </button>
                    <button className="nav-item active">
                        <span className="material-symbols-outlined">group</span> User Management
                    </button>
                    <button className="nav-item">
                        <span className="material-symbols-outlined">calendar_today</span> Appointments
                    </button>
                    <button className="nav-item">
                        <span className="material-symbols-outlined">stethoscope</span> Doctors
                    </button>
                    <button className="nav-item">
                        <span className="material-symbols-outlined">assessment</span> Reports
                    </button>
                    <button className="nav-item" onClick={() => navigate('/profile')}>
                        <span className="material-symbols-outlined">settings</span> Settings
                    </button>
                </nav>
                <div className="sidebar-support">
                    <p className="support-title">SUPPORT</p>
                    <p className="support-text">Need help with the admin panel?</p>
                    <button className="btn-support">Contact Support</button>
                </div>
            </aside>

            {/* Main */}
            <div className="admin-main-wrapper">
                {/* Top Bar */}
                <header className="admin-topbar">
                    <div className="search-bar">
                        <span className="material-symbols-outlined">search</span>
                        <input
                            type="text"
                            placeholder="Search patients, doctors or staff..."
                            value={searchTerm}
                            onChange={(e) => { setSearchTerm(e.target.value); setCurrentPage(1); }}
                        />
                    </div>
                    <div className="topbar-right">
                        <button className="icon-btn"><span className="material-symbols-outlined">notifications</span></button>
                        <div className="admin-profile">
                            <div>
                                <p className="admin-name">{savedUser?.name || 'Admin User'}</p>
                                <p className="admin-role">Super Admin</p>
                            </div>
                            <div className="admin-avatar">
                                <span className="material-symbols-outlined">account_circle</span>
                            </div>
                        </div>
                    </div>
                </header>

                <main className="admin-content">
                    {/* Page Header */}
                    <div className="content-header">
                        <div>
                            <h1>User Management</h1>
                            <p>Manage, monitor, and update all healthcare professionals and staff accounts.</p>
                        </div>
                        <button className="btn-add-user" onClick={handleOpenAdd}>
                            <span className="material-symbols-outlined">person_add</span> Add New User
                        </button>
                    </div>

                    {/* Stats */}
                    <div className="admin-stats">
                        <div className="admin-stat-card">
                            <div className="stat-top">
                                <div>
                                    <p className="stat-label">Total Doctors</p>
                                    <h2 className="stat-value">{stats.doctors.toLocaleString()}</h2>
                                </div>
                                <span className="material-symbols-outlined stat-icon">stethoscope</span>
                            </div>
                            <p className="stat-trend up">↗ 12% <span>vs. last month</span></p>
                        </div>
                        <div className="admin-stat-card">
                            <div className="stat-top">
                                <div>
                                    <p className="stat-label">Active Patients</p>
                                    <h2 className="stat-value">{stats.patients.toLocaleString()}</h2>
                                </div>
                                <span className="material-symbols-outlined stat-icon">groups</span>
                            </div>
                            <p className="stat-trend up">↗ 8% <span>Across all branches</span></p>
                        </div>
                        <div className="admin-stat-card">
                            <div className="stat-top">
                                <div>
                                    <p className="stat-label">Total Users</p>
                                    <h2 className="stat-value">{stats.total.toLocaleString()}</h2>
                                </div>
                                <span className="material-symbols-outlined stat-icon">people</span>
                            </div>
                            <p className="stat-trend up">↗ 15% <span>Net growth this month</span></p>
                        </div>
                    </div>

                    {/* User Directory */}
                    <div className="user-directory">
                        <div className="directory-header">
                            <div>
                                <h2>User Directory</h2>
                                <p>High-density view of all registered platform users</p>
                            </div>
                            <div className="directory-actions">
                                <button className="btn-filter"><span className="material-symbols-outlined">tune</span> Filter</button>
                                <button className="btn-export"><span className="material-symbols-outlined">download</span> Export</button>
                            </div>
                        </div>

                        <div className="user-table">
                            <div className="table-header">
                                <span className="col-name">NAME & CONTACT</span>
                                <span className="col-role">ROLE</span>
                                <span className="col-status">STATUS</span>
                                <span className="col-actions">ACTIONS</span>
                            </div>
                            {loading ? (
                                <div className="table-loading">Loading users...</div>
                            ) : paginatedUsers.length === 0 ? (
                                <div className="table-empty">No users found.</div>
                            ) : (
                                paginatedUsers.map(user => (
                                    <div className="table-row" key={user.id}>
                                        <div className="col-name">
                                            <div className="user-avatar-sm">
                                                <span className="material-symbols-outlined">person</span>
                                            </div>
                                            <div>
                                                <p className="user-name-text">{user.name}</p>
                                                <p className="user-email-text">{user.email}</p>
                                            </div>
                                        </div>
                                        <div className="col-role">
                                            <span className={`role-badge ${getRoleBadgeClass(user.role)}`}>
                                                {user.role ? user.role.charAt(0) + user.role.slice(1).toLowerCase() : 'User'}
                                            </span>
                                        </div>
                                        <div className="col-status">
                                            <span className="status-dot active"></span> Active
                                        </div>
                                        <div className="col-actions">
                                            <button className="action-btn edit" onClick={() => handleOpenEdit(user)} title="Edit">
                                                <span className="material-symbols-outlined">edit</span>
                                            </button>
                                            <button className="action-btn delete" onClick={() => handleDelete(user.id)} title="Delete">
                                                <span className="material-symbols-outlined">delete</span>
                                            </button>
                                        </div>
                                    </div>
                                ))
                            )}
                        </div>

                        {/* Pagination */}
                        <div className="pagination">
                            <span className="page-info">Showing {((currentPage-1)*ITEMS_PER_PAGE)+1} to {Math.min(currentPage*ITEMS_PER_PAGE, filteredUsers.length)} of {filteredUsers.length} entries</span>
                            <div className="page-buttons">
                                <button onClick={() => setCurrentPage(p => Math.max(1, p - 1))} disabled={currentPage === 1}>‹</button>
                                {Array.from({ length: Math.min(totalPages, 3) }, (_, i) => i + 1).map(p => (
                                    <button key={p} className={currentPage === p ? 'active' : ''} onClick={() => setCurrentPage(p)}>{p}</button>
                                ))}
                                <button onClick={() => setCurrentPage(p => Math.min(totalPages, p + 1))} disabled={currentPage === totalPages}>›</button>
                            </div>
                        </div>
                    </div>
                </main>
            </div>

            {/* Add/Edit Modal */}
            {showAddModal && (
                <div className="modal-overlay" onClick={() => setShowAddModal(false)}>
                    <div className="modal-card" onClick={(e) => e.stopPropagation()}>
                        <h2>{editUser ? 'Edit User' : 'Add New User'}</h2>
                        {formError && <div className="modal-error">{formError}</div>}
                        <form onSubmit={handleSubmit}>
                            <div className="modal-field">
                                <label>Full Name</label>
                                <input type="text" value={formData.name} onChange={(e) => setFormData({...formData, name: e.target.value})} placeholder="Enter full name" required />
                            </div>
                            <div className="modal-field">
                                <label>Email Address</label>
                                <input type="email" value={formData.email} onChange={(e) => setFormData({...formData, email: e.target.value})} placeholder="user@example.com" required />
                            </div>
                            <div className="modal-field">
                                <label>Password</label>
                                <input type="password" value={formData.password} onChange={(e) => setFormData({...formData, password: e.target.value})} placeholder="Enter password" required />
                            </div>
                            <div className="modal-field">
                                <label>Role</label>
                                <select value={formData.role} onChange={(e) => setFormData({...formData, role: e.target.value})}>
                                    <option value="PATIENT">Patient</option>
                                    <option value="DOCTOR">Doctor</option>
                                    <option value="ADMIN">Admin</option>
                                </select>
                            </div>
                            <div className="modal-actions">
                                <button type="button" className="btn-modal-cancel" onClick={() => setShowAddModal(false)}>Cancel</button>
                                <button type="submit" className="btn-modal-save">{editUser ? 'Update User' : 'Create User'}</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
}

export default AdminDashboard;
