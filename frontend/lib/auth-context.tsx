'use client';

import React, { createContext, useContext, useEffect, useState } from 'react';
import { User } from '@/types';
import { getStoredUser, setStoredUser, clearAuthTokens } from './api';

interface AuthContextType {
  user: User | null;
  isLoading: boolean;
  logout: () => void;
  setUser: (user: User) => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export { AuthContext };
export type { AuthContextType };

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // Check if user is stored in localStorage
    const storedUser = getStoredUser();
    if (storedUser) {
      setUser(storedUser);
    }
    setIsLoading(false);
  }, []);

  const logout = () => {
    clearAuthTokens();
    setUser(null);
  };

  const updateUser = (newUser: User) => {
    setUser(newUser);
    setStoredUser(newUser);
  };

  return (
    <AuthContext.Provider value={{ user, isLoading, logout, setUser: updateUser }}>
      {children}
      <CartSyncRunner />
      <WishlistSyncRunner />
    </AuthContext.Provider>
  );
}

// Mounts `useWishlistSync` inside the provider so wishlist is synced when user changes
function WishlistSyncRunner() {
  const [SyncComponent, setSyncComponent] = useState<React.ComponentType | null>(null);

  useEffect(() => {
    let mounted = true;
    (async () => {
      try {
        const mod = await import('@/hooks/useWishlistSync');
        if (!mounted) return;
        const Comp = (mod && (mod.WishlistSyncClient)) as React.ComponentType | undefined;
        if (Comp) setSyncComponent(() => Comp);
      } catch (e) {
        // eslint-disable-next-line no-console
        console.error('Failed to load WishlistSyncClient dynamically', e);
      }
    })();
    return () => {
      mounted = false;
    };
  }, []);

  if (SyncComponent) return <SyncComponent />;
  return null;
}

// Mounts `useCartSync` inside the provider so it can read auth state and run when user logs in
function CartSyncRunner() {
  // Dynamically import a small client component that mounts the hook,
  // then render that component. This avoids calling hooks as plain functions
  // (which causes invalid hook call errors) and also prevents circular imports.
  const [SyncComponent, setSyncComponent] = useState<React.ComponentType | null>(null);

  useEffect(() => {
    let mounted = true;
    (async () => {
      try {
        const mod = await import('@/hooks/useCartSync');
        if (!mounted) return;
        // Prefer named CartSyncClient component exported from the hook module
        const Comp = (mod && (mod.CartSyncClient)) as React.ComponentType | undefined;
        if (Comp) setSyncComponent(() => Comp);
      } catch (e) {
        // eslint-disable-next-line no-console
        console.error('Failed to load CartSyncClient dynamically', e);
      }
    })();
    return () => {
      mounted = false;
    };
  }, []);

  if (SyncComponent) return <SyncComponent />;
  return null;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}

