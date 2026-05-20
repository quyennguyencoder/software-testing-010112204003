/**
 * Custom hook for authentication
 * Wraps the auth context for easier usage
 */

'use client';

import { useContext } from 'react';
import { AuthContext } from '@/lib/auth-context';

interface UseAuthReturn {
  user: any | null;
  isLoading: boolean;
  isAuthenticated: boolean;
  isAdmin: boolean;
  isCustomer: boolean;
  logout: () => void;
  setUser: (user: any) => void;
}

export function useAuth(): UseAuthReturn {
  const context = useContext(AuthContext);
  
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }

  const { user, isLoading, logout, setUser } = context;
  
  return {
    user,
    isLoading,
    isAuthenticated: !!user,
    isAdmin: user?.role === 'ADMIN',
    isCustomer: user?.role === 'CUSTOMER',
    logout,
    setUser,
  };
}
