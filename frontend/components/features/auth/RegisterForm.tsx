/**
 * RegisterForm component - Registration form with validation
 */

'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { Button } from '@/components/ui/button';
import { User, Mail, Lock, AlertCircle, CheckCircle, Loader2, Eye, EyeOff, Phone } from 'lucide-react';
import { authAPI } from '@/lib/api';
import { useFormValidation } from '@/hooks';
import { ROUTES } from '@/lib/constants';
import { SocialLogin } from './SocialLogin';

export function RegisterForm() {
  const router = useRouter();
  const { errors, validate, validatePasswordConfirmation, clearError, clearAllErrors } = useFormValidation();
  
  const [formData, setFormData] = useState({
    username: '',
    fullName: '',
    email: '',
    phoneNumber: '',
    gender: '' as 'MALE' | 'FEMALE' | 'OTHER' | '',
    dateOfBirth: '',
    password: '',
    confirmPassword: '',
  });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [agreeTerms, setAgreeTerms] = useState(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
    setError('');
    clearError(name);
  };

  const validateForm = (): boolean => {
    clearAllErrors();
    let isValid = true;

    // Validate all required fields
    if (!validate('username', formData.username, 'username')) isValid = false;
    if (!validate('fullName', formData.fullName, 'required', { fieldName: 'Họ và tên' })) isValid = false;
    if (!validate('email', formData.email, 'email')) isValid = false;
    if (!validate('password', formData.password, 'password')) isValid = false;
    if (!validatePasswordConfirmation(formData.password, formData.confirmPassword)) isValid = false;

    if (!agreeTerms) {
      setError('Bạn cần đồng ý với điều khoản dịch vụ');
      return false;
    }

    return isValid;
  };

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    if (!validateForm()) {
      return;
    }

    setLoading(true);

    try {
      const response = await authAPI.register({
        username: formData.username,
        fullName: formData.fullName,
        email: formData.email,
        phoneNumber: formData.phoneNumber || undefined,
        gender: formData.gender || undefined,
        dateOfBirth: formData.dateOfBirth || undefined,
        password: formData.password,
        confirmPassword: formData.confirmPassword,
      });

      if (response.success && response.status === 201) {
        setSuccess('Đăng ký thành công! Đang chuyển đến trang đăng nhập...');
        setTimeout(() => {
          router.push(ROUTES.LOGIN);
        }, 2000);
      } else {
        setError(response.message || 'Đăng ký thất bại');
      }
    } catch (err: unknown) {
      const errorMessage = err instanceof Error ? err.message : 'Có lỗi xảy ra khi đăng ký';
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="p-6 space-y-4">
      {/* Error Message */}
      {error && (
        <div className="bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg p-3 flex gap-3">
          <AlertCircle className="w-5 h-5 text-red-600 dark:text-red-400 flex-shrink-0 mt-0.5" />
          <p className="text-sm text-red-700 dark:text-red-300">{error}</p>
        </div>
      )}

      {/* Success Message */}
      {success && (
        <div className="bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-800 rounded-lg p-3 flex gap-3">
          <CheckCircle className="w-5 h-5 text-green-600 dark:text-green-400 flex-shrink-0 mt-0.5" />
          <p className="text-sm text-green-700 dark:text-green-300">{success}</p>
        </div>
      )}

      {/* Username Field */}
      <div>
        <label htmlFor="username" className="block text-sm font-medium text-foreground mb-1.5">
          Tên đăng nhập <span className="text-destructive">*</span>
        </label>
        <div className="relative">
          <User className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground" />
          <input
            type="text"
            id="username"
            name="username"
            value={formData.username}
            onChange={handleChange}
            placeholder="nguoidung123"
            className="w-full pl-10 pr-4 py-2.5 border border-input rounded-lg bg-background text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent transition-all"
            disabled={loading}
          />
        </div>
        {errors.username ? (
          <p className="text-xs text-red-600 mt-1">{errors.username}</p>
        ) : (
          <p className="text-xs text-muted-foreground mt-1">Tối thiểu 3 ký tự</p>
        )}
      </div>

      {/* Full Name Field */}
      <div>
        <label htmlFor="fullName" className="block text-sm font-medium text-foreground mb-1.5">
          Họ và tên <span className="text-destructive">*</span>
        </label>
        <div className="relative">
          <User className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground" />
          <input
            type="text"
            id="fullName"
            name="fullName"
            value={formData.fullName}
            onChange={handleChange}
            placeholder="Nguyễn Văn A"
            className="w-full pl-10 pr-4 py-2.5 border border-input rounded-lg bg-background text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent transition-all"
            disabled={loading}
          />
        </div>
        {errors.fullName && (
          <p className="text-xs text-red-600 mt-1">{errors.fullName}</p>
        )}
      </div>

      {/* Email Field */}
      <div>
        <label htmlFor="email" className="block text-sm font-medium text-foreground mb-1.5">
          Email <span className="text-destructive">*</span>
        </label>
        <div className="relative">
          <Mail className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground" />
          <input
            type="email"
            id="email"
            name="email"
            value={formData.email}
            onChange={handleChange}
            placeholder="you@example.com"
            className="w-full pl-10 pr-4 py-2.5 border border-input rounded-lg bg-background text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent transition-all"
            disabled={loading}
          />
        </div>
        {errors.email && (
          <p className="text-xs text-red-600 mt-1">{errors.email}</p>
        )}
      </div>

      {/* Phone Number Field */}
      <div>
        <label htmlFor="phoneNumber" className="block text-sm font-medium text-foreground mb-1.5">
          Số điện thoại
        </label>
        <div className="relative">
          <Phone className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground" />
          <input
            type="tel"
            id="phoneNumber"
            name="phoneNumber"
            value={formData.phoneNumber}
            onChange={handleChange}
            placeholder="0912345678"
            className="w-full pl-10 pr-4 py-2.5 border border-input rounded-lg bg-background text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent transition-all"
            disabled={loading}
          />
        </div>
      </div>

      {/* Gender and Date of Birth - Grid Layout */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {/* Gender Field */}
        <div>
          <label htmlFor="gender" className="block text-sm font-medium text-foreground mb-1.5">
            Giới tính
          </label>
          <select
            id="gender"
            name="gender"
            value={formData.gender}
            onChange={handleChange}
            className="w-full px-4 py-2.5 border border-input rounded-lg bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent transition-all"
            disabled={loading}
          >
            <option value="">Chọn giới tính</option>
            <option value="MALE">Nam</option>
            <option value="FEMALE">Nữ</option>
            <option value="OTHER">Khác</option>
          </select>
        </div>

        {/* Date of Birth Field */}
        <div>
          <label htmlFor="dateOfBirth" className="block text-sm font-medium text-foreground mb-1.5">
            Ngày sinh
          </label>
          <input
            type="date"
            id="dateOfBirth"
            name="dateOfBirth"
            value={formData.dateOfBirth}
            onChange={handleChange}
            max={new Date().toISOString().split('T')[0]}
            className="w-full px-4 py-2.5 border border-input rounded-lg bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent transition-all"
            disabled={loading}
          />
        </div>
      </div>

      {/* Password Field */}
      <div>
        <label htmlFor="password" className="block text-sm font-medium text-foreground mb-1.5">
          Mật khẩu <span className="text-destructive">*</span>
        </label>
        <div className="relative">
          <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground" />
          <input
            type={showPassword ? 'text' : 'password'}
            id="password"
            name="password"
            value={formData.password}
            onChange={handleChange}
            placeholder="••••••••"
            className="w-full pl-10 pr-12 py-2.5 border border-input rounded-lg bg-background text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent transition-all"
            disabled={loading}
          />
          <button
            type="button"
            onClick={() => setShowPassword(!showPassword)}
            className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors"
            disabled={loading}
          >
            {showPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
          </button>
        </div>
        {errors.password ? (
          <p className="text-xs text-red-600 mt-1">{errors.password}</p>
        ) : (
          <p className="text-xs text-muted-foreground mt-1">
            Tối thiểu 8 ký tự, bao gồm chữ hoa, chữ thường và số
          </p>
        )}
      </div>

      {/* Confirm Password Field */}
      <div>
        <label htmlFor="confirmPassword" className="block text-sm font-medium text-foreground mb-1.5">
          Xác nhận mật khẩu <span className="text-destructive">*</span>
        </label>
        <div className="relative">
          <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground" />
          <input
            type={showConfirmPassword ? 'text' : 'password'}
            id="confirmPassword"
            name="confirmPassword"
            value={formData.confirmPassword}
            onChange={handleChange}
            placeholder="••••••••"
            className="w-full pl-10 pr-12 py-2.5 border border-input rounded-lg bg-background text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent transition-all"
            disabled={loading}
          />
          <button
            type="button"
            onClick={() => setShowConfirmPassword(!showConfirmPassword)}
            className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors"
            disabled={loading}
          >
            {showConfirmPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
          </button>
        </div>
        {errors.confirmPassword && (
          <p className="text-xs text-red-600 mt-1">{errors.confirmPassword}</p>
        )}
      </div>

      {/* Terms & Conditions */}
      <label className="flex items-start gap-2 cursor-pointer">
        <input
          type="checkbox"
          checked={agreeTerms}
          onChange={(e) => setAgreeTerms(e.target.checked)}
          className="w-4 h-4 rounded border-input text-primary focus:ring-primary mt-0.5"
          disabled={loading}
        />
        <span className="text-sm text-muted-foreground">
          Tôi đồng ý với{' '}
          <Link href="#" className="text-primary hover:underline font-medium">
            Điều khoản dịch vụ
          </Link>
          {' '}và{' '}
          <Link href="#" className="text-primary hover:underline font-medium">
            Chính sách bảo mật
          </Link>
        </span>
      </label>

      {/* Submit Button */}
      <Button
        type="submit"
        disabled={loading}
        className="w-full py-3 text-base font-semibold"
        size="lg"
      >
        {loading ? (
          <>
            <Loader2 className="w-5 h-5 animate-spin mr-2" />
            Đang đăng ký...
          </>
        ) : (
          'Đăng ký'
        )}
      </Button>

      {/* Social Login */}
      <SocialLogin mode="register" loading={loading} />
    </form>
  );
}
