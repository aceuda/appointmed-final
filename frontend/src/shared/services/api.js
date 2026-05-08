// Re-export all API services from the central api module
export {
    default,
    authAPI,
    userAPI,
    doctorAPI,
    appointmentAPI,
    notificationAPI,
    messageAPI
} from '../../services/api';
