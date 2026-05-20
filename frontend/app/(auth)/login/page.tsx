/**
 * Login Page - Refactored to use LoginForm component
 */

'use client';

import Link from 'next/link';
import { Smartphone } from 'lucide-react';
import { LoginForm } from '@/components/features/auth';
import { ROUTES } from '@/lib/constants';

export default function LoginPage() {
  return (
    <div className="w-full max-w-md">
      {/* Logo */}
      <div className="text-center mb-8">
        <Link href={ROUTES.HOME} className="inline-flex items-center gap-2">
          <Smartphone className="w-10 h-10 text-primary" />
          <span className="text-2xl font-bold">UTE Phone Hub</span>
        </Link>
      </div>

      {/* Card */}
      <div className="bg-card rounded-2xl shadow-2xl border border-border overflow-hidden">
        {/* Header */}
        <div className="bg-primary px-6 py-6 text-center">
          <h1 className="text-2xl font-bold text-primary-foreground">Đăng nhập</h1>
          <p className="text-primary-foreground/80 mt-1">Chào mừng bạn quay lại!</p>
        </div>

        {/* Form */}
        <LoginForm />

        {/* Footer */}
        <div className="bg-secondary/50 px-6 py-4 text-center border-t border-border">
          <p className="text-muted-foreground">
            Chưa có tài khoản?{' '}
            <Link href={ROUTES.REGISTER} className="text-primary hover:underline font-semibold">
              Đăng ký ngay
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
