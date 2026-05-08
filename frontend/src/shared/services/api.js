// Re-export all API services from the central api module
import api, {
    authAPI,
    userAPI,
    doctorAPI,
    appointmentAPI,
    notificationAPI,
    messageAPI,
} from '../../services/api';

export default api;
export {
    authAPI,
    userAPI,
    doctorAPI,
    appointmentAPI,
    notificationAPI,
    messageAPI,
};
