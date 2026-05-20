import { Metadata } from 'next';
import { Suspense } from 'react';
import UserDashboardClient from '@/components/features/user/UserDashboardClient';

export const metadata: Metadata = {
    title: 'Tài khoản của tôi | UTE Phone Hub',
    description: 'Quản lý thông tin tài khoản, đơn hàng và danh sách yêu thích',
};

export default function UserPage() {
    return (
        <Suspense
            fallback={
                <div className="min-h-screen bg-secondary flex items-center justify-center">
                    <p className="text-muted-foreground">Đang tải thông tin...</p>
                </div>
            }
        >
            <UserDashboardClient />
        </Suspense>
    );
}
