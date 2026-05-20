'use client';

import Link from 'next/link';
import { ShieldX, Home } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { ROUTES } from '@/lib/constants';

export default function AccountLockedPage() {
    return (
        <div className="min-h-screen flex items-center justify-center bg-background p-4">
            <div className="max-w-md w-full text-center space-y-6">
                <div className="mx-auto w-20 h-20 bg-red-100 dark:bg-red-900/30 rounded-full flex items-center justify-center">
                    <ShieldX className="w-10 h-10 text-red-600 dark:text-red-400" />
                </div>

                <div className="space-y-2">
                    <h1 className="text-2xl font-bold text-foreground">
                        Tài khoản đã bị khóa
                    </h1>
                    <p className="text-muted-foreground">
                        Tài khoản của bạn đã bị khóa bởi quản trị viên. Nếu bạn cho rằng đây là lỗi, vui lòng liên hệ bộ phận hỗ trợ.
                    </p>
                </div>

                <div className="flex flex-col sm:flex-row gap-3 justify-center">
                    <Button asChild variant="default">
                        <Link href={ROUTES.HOME}>
                            <Home className="w-4 h-4 mr-2" />
                            Về trang chủ
                        </Link>
                    </Button>
                    <Button asChild variant="outline">
                        <Link href={ROUTES.LOGIN}>
                            Thử đăng nhập lại
                        </Link>
                    </Button>
                </div>

                <p className="text-sm text-muted-foreground">
                    Liên hệ hỗ trợ: <a href="mailto:support@utephonehub.com" className="text-primary hover:underline">support@utephonehub.com</a>
                </p>
            </div>
        </div>
    );
}
