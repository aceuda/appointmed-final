import React, { useState, useEffect } from 'react';
import { userAPI } from '../services/api';

const UserManager = () => {
    const [users, setUsers] = useState([]);
    const [formData, setFormData] = useState({ name: '', email: '', password: '' });
    const [editingId, setEditingId] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    // FETCH all users on component mount
    useEffect(() => {
        fetchUsers();
    }, []);

    const fetchUsers = async () => {
        setLoading(true);
        try {
            const response = await userAPI.getAll();
            setUsers(response.data);
            setError('');
        } catch (err) {
            setError('Failed to fetch users: ' + err.message);
        }
        setLoading(false);
    };

    const handleInputChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    // CREATE or UPDATE user
    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            if (editingId) {
                await userAPI.update(editingId, formData);
                setEditingId(null);
            } else {
                await userAPI.create(formData);
            }
            setFormData({ name: '', email: '', password: '' });
            fetchUsers(); // Refresh the list
            setError('');
        } catch (err) {
            setError('Failed to save user: ' + err.message);
        }
    };

    // SET form for editing
    const handleEdit = (user) => {
        setEditingId(user.id);
        setFormData({ name: user.name, email: user.email, password: user.password });
    };

    // DELETE user
    const handleDelete = async (id) => {
        if (window.confirm('Are you sure you want to delete this user?')) {
            try {
                await userAPI.delete(id);
                fetchUsers();
            } catch (err) {
                setError('Failed to delete user: ' + err.message);
            }
        }
    };

    // CANCEL editing
    const handleCancel = () => {
        setEditingId(null);
        setFormData({ name: '', email: '', password: '' });
    };

    return (
        <div style={{ maxWidth: '800px', margin: '0 auto', padding: '20px' }}>
            <h1>👥 User Management</h1>
            <p style={{ color: 'gray' }}>Spring Boot + React + Supabase</p>

            {error && <div style={{ color: 'red', padding: '10px', background: '#ffe0e0', borderRadius: '5px', marginBottom: '15px' }}>{error}</div>}

            {/* FORM */}
            <form onSubmit={handleSubmit} style={{ background: '#f5f5f5', padding: '20px', borderRadius: '8px', marginBottom: '20px' }}>
                <h3>{editingId ? '✏️ Edit User' : '➕ Add New User'}</h3>
                <div style={{ marginBottom: '10px' }}>
                    <input
                        type="text"
                        name="name"
                        placeholder="Name"
                        value={formData.name}
                        onChange={handleInputChange}
                        required
                        style={{ padding: '10px', width: '100%', borderRadius: '5px', border: '1px solid #ccc' }}
                    />
                </div>
                <div style={{ marginBottom: '10px' }}>
                    <input
                        type="email"
                        name="email"
                        placeholder="Email"
                        value={formData.email}
                        onChange={handleInputChange}
                        required
                        style={{ padding: '10px', width: '100%', borderRadius: '5px', border: '1px solid #ccc' }}
                    />
                </div>
                <div style={{ marginBottom: '10px' }}>
                    <input
                        type="password"
                        name="password"
                        placeholder="Password"
                        value={formData.password}
                        onChange={handleInputChange}
                        required
                        style={{ padding: '10px', width: '100%', borderRadius: '5px', border: '1px solid #ccc' }}
                    />
                </div>
                <button type="submit" style={{ padding: '10px 20px', background: '#4CAF50', color: 'white', border: 'none', borderRadius: '5px', cursor: 'pointer', marginRight: '10px' }}>
                    {editingId ? 'Update' : 'Create'}
                </button>
                {editingId && (
                    <button type="button" onClick={handleCancel} style={{ padding: '10px 20px', background: '#999', color: 'white', border: 'none', borderRadius: '5px', cursor: 'pointer' }}>
                        Cancel
                    </button>
                )}
            </form>

            {/* USER LIST TABLE */}
            {loading ? (
                <p>Loading...</p>
            ) : (
                <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                    <thead>
                        <tr style={{ background: '#333', color: 'white' }}>
                            <th style={{ padding: '12px' }}>ID</th>
                            <th style={{ padding: '12px' }}>Name</th>
                            <th style={{ padding: '12px' }}>Email</th>
                            <th style={{ padding: '12px' }}>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {users.map((user) => (
                            <tr key={user.id} style={{ borderBottom: '1px solid #ddd' }}>
                                <td style={{ padding: '12px' }}>{user.id}</td>
                                <td style={{ padding: '12px' }}>{user.name}</td>
                                <td style={{ padding: '12px' }}>{user.email}</td>
                                <td style={{ padding: '12px' }}>
                                    <button onClick={() => handleEdit(user)} style={{ padding: '5px 10px', background: '#2196F3', color: 'white', border: 'none', borderRadius: '3px', cursor: 'pointer', marginRight: '5px' }}>
                                        Edit
                                    </button>
                                    <button onClick={() => handleDelete(user.id)} style={{ padding: '5px 10px', background: '#f44336', color: 'white', border: 'none', borderRadius: '3px', cursor: 'pointer' }}>
                                        Delete
                                    </button>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            )}
            {!loading && users.length === 0 && <p style={{ textAlign: 'center', color: '#999' }}>No users found.</p>}
        </div>
    );
};

export default UserManager;