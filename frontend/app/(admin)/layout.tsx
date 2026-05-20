import type { Metadata } from 'next';

export const metadata: Metadata = {
  title: 'Admin Dashboard - UTE Phone Hub',
  description: 'Manage products, orders, and users',
};

/**
 * Layout for admin pages (dashboard, manage products, orders, users)
 * Can add admin sidebar navigation here
 */
export default function AdminLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900">
      {children}
    </div>
  );
}
