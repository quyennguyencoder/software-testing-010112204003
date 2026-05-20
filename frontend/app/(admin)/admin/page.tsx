import { Metadata } from 'next';
import { Suspense } from 'react';
import AdminDashboardClient from '@/components/features/admin/AdminDashboardClient';

export const metadata: Metadata = {
    title: 'Quản trị hệ thống | UTE Phone Hub',
    description: 'Trang quản trị hệ thống UTE Phone Hub',
};

export default function AdminPage() {
    return (
        <Suspense
            fallback={
                <div className="min-h-screen bg-secondary flex items-center justify-center">
                    <p className="text-muted-foreground">Đang tải dashboard...</p>
                </div>
            }
        >
            <AdminDashboardClient />
        </Suspense>
    );
}
