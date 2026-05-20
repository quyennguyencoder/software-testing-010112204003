/**
 * Refactored Header component - Now uses modular sub-components
 */

'use client';

import { useAuth } from '@/hooks';
import { MainHeader } from '@/components/features/layout';

export function Header() {
  const { user, logout } = useAuth();

  return <MainHeader user={user} onLogout={logout} />;
}
