import { createContext, useContext } from 'react';

// Auth Context
const AuthContext = createContext(null);
export const useAuth = () => useContext(AuthContext);

// Toast notification context
const ToastContext = createContext(null);
export const useToast = () => useContext(ToastContext);

export { AuthContext, ToastContext };
