/**
 * ForgotPasswordForm component - Multi-step password recovery form
 */

'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { Button } from '@/components/ui/button';
import { Mail, Lock, AlertCircle, CheckCircle, Loader2, Eye, EyeOff } from 'lucide-react';
import { authAPI } from '@/lib/api';
import { validateEmail, validatePassword } from '@/lib/utils';

type Step = 'email' | 'otp' | 'reset';

interface ForgotPasswordFormProps {
  onBack?: () => void;
}

export function ForgotPasswordForm({ onBack }: ForgotPasswordFormProps) {
  const router = useRouter();
  const [step, setStep] = useState<Step>('email');
  const [email, setEmail] = useState('');
  const [otp, setOtp] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const handleRequestReset = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    // Trim email before validation
    const trimmedEmail = email.trim();
    
    const emailValidation = validateEmail(trimmedEmail);
    if (!emailValidation.isValid) {
      setError(emailValidation.error || 'Email không hợp lệ');
      return;
    }

    setLoading(true);

    try {
      const response = await authAPI.forgotPassword({ email: trimmedEmail });

      if (response.success && response.status === 200) {
        setSuccess('Mã OTP đã được gửi đến email của bạn');
        setStep('otp');
      } else {
        setError(response.message || 'Không thể gửi email đặt lại mật khẩu');
      }
    } catch (err: unknown) {
      const errorMessage = err instanceof Error ? err.message : 'Có lỗi xảy ra';
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const handleVerifyOtp = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    if (!otp) {
      setError('Vui lòng nhập mã OTP');
      return;
    }

    if (otp.length !== 6) {
      setError('Mã OTP phải có 6 chữ số');
      return;
    }

    setLoading(true);

    try {
      setSuccess('Mã OTP hợp lệ! Vui lòng nhập mật khẩu mới');
      setStep('reset');
    } catch (err: unknown) {
      const errorMessage = err instanceof Error ? err.message : 'Mã OTP không hợp lệ';
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const handleResetPassword = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    const passwordValidation = validatePassword(newPassword);
    if (!passwordValidation.isValid) {
      setError(passwordValidation.error || 'Mật khẩu không hợp lệ');
      return;
    }

    if (newPassword !== confirmPassword) {
      setError('Mật khẩu xác nhận không khớp');
      return;
    }

    setLoading(true);

    try {
      const response = await authAPI.verifyOtp({
        email,
        otp,
        newPassword,
        confirmPassword,
      });

      if (response.success && response.status === 200) {
        setSuccess('Đặt lại mật khẩu thành công! Đang chuyển đến trang đăng nhập...');
        setTimeout(() => {
          router.push('/login');
        }, 2000);
      } else {
        setError(response.message || 'Không thể đặt lại mật khẩu');
      }
    } catch (err: unknown) {
      const errorMessage = err instanceof Error ? err.message : 'Có lỗi xảy ra';
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const stepTitles = {
    email: 'Nhập email của bạn',
    otp: 'Nhập mã xác thực',
    reset: 'Tạo mật khẩu mới',
  };

  return (
    <div>
      {/* Header */}
      <div className="bg-primary px-6 py-6 text-center">
        <h1 className="text-2xl font-bold text-primary-foreground">Quên mật khẩu</h1>
        <p className="text-primary-foreground/80 mt-1">{stepTitles[step]}</p>
      </div>

      {/* Form */}
      <div className="p-6">
        {/* Progress Indicator */}
        <div className="flex items-center justify-between mb-6">
          <div className={`flex items-center justify-center w-10 h-10 rounded-full font-semibold text-sm transition-colors ${
            step === 'email' || step === 'otp' || step === 'reset' 
              ? 'bg-primary text-primary-foreground' 
              : 'bg-secondary text-muted-foreground'
          }`}>
            1
          </div>
          <div className={`flex-1 h-1 mx-2 transition-colors ${
            step === 'otp' || step === 'reset' ? 'bg-primary' : 'bg-secondary'
          }`}></div>
          <div className={`flex items-center justify-center w-10 h-10 rounded-full font-semibold text-sm transition-colors ${
            step === 'otp' || step === 'reset' 
              ? 'bg-primary text-primary-foreground' 
              : 'bg-secondary text-muted-foreground'
          }`}>
            2
          </div>
          <div className={`flex-1 h-1 mx-2 transition-colors ${
            step === 'reset' ? 'bg-primary' : 'bg-secondary'
          }`}></div>
          <div className={`flex items-center justify-center w-10 h-10 rounded-full font-semibold text-sm transition-colors ${
            step === 'reset' 
              ? 'bg-primary text-primary-foreground' 
              : 'bg-secondary text-muted-foreground'
          }`}>
            3
          </div>
        </div>

        {/* Alert Messages */}
        {error && (
          <div className="mb-4 p-4 bg-destructive/10 border border-destructive rounded-lg flex items-start gap-3">
            <AlertCircle className="w-5 h-5 text-destructive flex-shrink-0 mt-0.5" />
            <p className="text-sm text-destructive">{error}</p>
          </div>
        )}

        {success && (
          <div className="mb-4 p-4 bg-green-500/10 border border-green-500 rounded-lg flex items-start gap-3">
            <CheckCircle className="w-5 h-5 text-green-500 flex-shrink-0 mt-0.5" />
            <p className="text-sm text-green-500">{success}</p>
          </div>
        )}

        {/* Step 1: Email */}
        {step === 'email' && (
          <form onSubmit={handleRequestReset} className="space-y-4">
            <div>
              <label htmlFor="email" className="block text-sm font-medium text-foreground mb-2">
                Địa chỉ email
              </label>
              <div className="relative">
                <Mail className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground" />
                <input
                  id="email"
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  className="w-full pl-10 pr-4 py-3 bg-secondary border border-border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary text-foreground"
                  placeholder="email@example.com"
                  disabled={loading}
                />
              </div>
              <p className="mt-2 text-sm text-muted-foreground">
                Chúng tôi sẽ gửi mã OTP đến email này
              </p>
            </div>

            <Button type="submit" className="w-full" disabled={loading}>
              {loading ? (
                <>
                  <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                  Đang gửi...
                </>
              ) : (
                'Gửi mã OTP'
              )}
            </Button>
          </form>
        )}

        {/* Step 2: OTP */}
        {step === 'otp' && (
          <form onSubmit={handleVerifyOtp} className="space-y-4">
            <div>
              <label htmlFor="otp" className="block text-sm font-medium text-foreground mb-2">
                Mã OTP (6 chữ số)
              </label>
              <div className="relative">
                <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground" />
                <input
                  id="otp"
                  type="text"
                  value={otp}
                  onChange={(e) => setOtp(e.target.value.replace(/\D/g, '').slice(0, 6))}
                  className="w-full pl-10 pr-4 py-3 bg-secondary border border-border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary text-foreground text-center text-2xl tracking-widest"
                  placeholder="000000"
                  maxLength={6}
                  disabled={loading}
                />
              </div>
              <p className="mt-2 text-sm text-muted-foreground">
                Mã OTP đã được gửi đến <span className="font-medium text-foreground">{email}</span>
              </p>
            </div>

            <Button type="submit" className="w-full" disabled={loading}>
              {loading ? (
                <>
                  <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                  Đang xác thực...
                </>
              ) : (
                'Xác thực OTP'
              )}
            </Button>

            <button
              type="button"
              onClick={() => setStep('email')}
              className="w-full text-sm text-muted-foreground hover:text-foreground transition-colors"
            >
              Gửi lại mã OTP
            </button>
          </form>
        )}

        {/* Step 3: Reset Password */}
        {step === 'reset' && (
          <form onSubmit={handleResetPassword} className="space-y-4">
            <div>
              <label htmlFor="newPassword" className="block text-sm font-medium text-foreground mb-2">
                Mật khẩu mới
              </label>
              <div className="relative">
                <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground" />
                <input
                  id="newPassword"
                  type={showPassword ? 'text' : 'password'}
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  className="w-full pl-10 pr-12 py-3 bg-secondary border border-border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary text-foreground"
                  placeholder="••••••••"
                  disabled={loading}
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
                >
                  {showPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                </button>
              </div>
            </div>

            <div>
              <label htmlFor="confirmPassword" className="block text-sm font-medium text-foreground mb-2">
                Xác nhận mật khẩu
              </label>
              <div className="relative">
                <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground" />
                <input
                  id="confirmPassword"
                  type={showConfirmPassword ? 'text' : 'password'}
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  className="w-full pl-10 pr-12 py-3 bg-secondary border border-border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary text-foreground"
                  placeholder="••••••••"
                  disabled={loading}
                />
                <button
                  type="button"
                  onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
                >
                  {showConfirmPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                </button>
              </div>
              <p className="mt-2 text-sm text-muted-foreground">
                Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường và số
              </p>
            </div>

            <Button type="submit" className="w-full" disabled={loading}>
              {loading ? (
                <>
                  <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                  Đang đặt lại...
                </>
              ) : (
                'Đặt lại mật khẩu'
              )}
            </Button>
          </form>
        )}
      </div>
    </div>
  );
}
