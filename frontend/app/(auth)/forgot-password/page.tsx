/**
 * ForgotPasswordPage - Password recovery page
 * Refactored to use ForgotPasswordForm component
 */

import Link from 'next/link';
import { ArrowLeft, Smartphone } from 'lucide-react';
import { ForgotPasswordForm } from '@/components/features/auth/ForgotPasswordForm';

export default function ForgotPasswordPage() {
  return (
    <div className="w-full max-w-md">
      {/* Logo */}
      <div className="mb-8 text-center">
        <Link href="/" className="inline-flex items-center gap-2">
          <Smartphone className="w-10 h-10 text-primary" />
          <span className="text-2xl font-bold">UTE Phone Hub</span>
        </Link>
      </div>

      {/* Card */}
      <div className="overflow-hidden rounded-2xl border border-border bg-card shadow-2xl">
        <ForgotPasswordForm />
      </div>

      {/* Back to Login */}
      <div className="text-center mt-6">
        <Link
          href="/login"
          className="inline-flex items-center gap-2 text-sm text-muted-foreground hover:text-foreground transition-colors"
        >
          <ArrowLeft className="w-4 h-4" />
          Quay lại đăng nhập
        </Link>
      </div>
    </div>
  );
}

