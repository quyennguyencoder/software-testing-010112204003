/**
 * Register Page - Refactored to use RegisterForm component
 */

'use client';

import Link from 'next/link';
import { Smartphone } from 'lucide-react';
import { RegisterForm } from '@/components/features/auth';
import { ROUTES } from '@/lib/constants';

export default function RegisterPage() {
  return (
    <div className="w-full max-w-md">
      {/* Logo */}
      <div className="mb-6 text-center">
        <Link href={ROUTES.HOME} className="inline-flex items-center gap-2">
          <Smartphone className="w-10 h-10 text-primary" />
          <span className="text-2xl font-bold">UTE Phone Hub</span>
        </Link>
      </div>

      {/* Card */}
      <div className="overflow-hidden rounded-2xl border border-border bg-card shadow-2xl">
        {/* Header */}
        <div className="bg-primary px-6 py-6 text-center">
          <h1 className="text-2xl font-bold text-primary-foreground">Tạo tài khoản</h1>
          <p className="text-primary-foreground/80 mt-1">Tham gia UTE Phone Hub ngay hôm nay</p>
        </div>

        {/* Form */}
        <RegisterForm />

        {/* Footer */}
        <div className="bg-secondary/50 px-6 py-4 text-center border-t border-border">
          <p className="text-muted-foreground">
            Đã có tài khoản?{' '}
            <Link href={ROUTES.LOGIN} className="text-primary hover:underline font-semibold">
              Đăng nhập
            </Link>
          </p>
        </div>
      </div>

      {/* Back to home */}
      <div className="text-center mt-6">
        <Link href={ROUTES.HOME} className="text-muted-foreground hover:text-foreground transition-colors text-sm">
          ← Quay lại trang chủ
        </Link>
      </div>
    </div>
  );
}
