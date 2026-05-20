/**
 * LoginForm component - Login form with validation
 */

'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { Button } from '@/components/ui/button';
import { Mail, Lock, AlertCircle, Loader2, Eye, EyeOff } from 'lucide-react';
import { authAPI, setAuthTokens, setStoredUser } from '@/lib/api';
import { useAuth } from '@/hooks';
import { useFormValidation } from '@/hooks';
import { ROUTES } from '@/lib/constants';
import { SocialLogin } from './SocialLogin';

export function LoginForm() {
  const router = useRouter();
  const { setUser } = useAuth();
  const { errors, validate, clearAllErrors } = useFormValidation();

  const [formData, setFormData] = useState({
    usernameOrEmail: '',
    password: '',
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
    setError('');
    clearAllErrors();
  };

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      // Validate inputs
      if (!formData.usernameOrEmail || !formData.password) {
        setError('Vui lòng điền đầy đủ thông tin');
        setLoading(false);
        return;
      }

      // Call login API
      const response = await authAPI.login({
        usernameOrEmail: formData.usernameOrEmail,
        password: formData.password,
      });

      if (response.success && response.status === 200) {
        // Store tokens and user data
        setAuthTokens(response.data.accessToken, response.data.refreshToken);
        setStoredUser(response.data.user);
        setUser(response.data.user);

        // Redirect based on user role
        if (response.data.user.role === 'ADMIN') {
          router.push(ROUTES.ADMIN);
        } else {
          router.push(ROUTES.HOME);
        }
      } else {
        // Check if account is locked
        const msg = response.message || 'Đăng nhập thất bại';
        if (response.status === 401 && msg.includes('khóa')) {
          router.push(ROUTES.ACCOUNT_LOCKED);
          return;
        }
        setError(msg);
      }
    } catch (err: unknown) {
      const errorMessage = err instanceof Error ? err.message : 'Có lỗi xảy ra khi đăng nhập';
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="p-6 space-y-4">
      {/* Error Message */}
      {error && (
        <div className="bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg p-4 flex gap-3">
          <AlertCircle className="w-5 h-5 text-red-600 dark:text-red-400 flex-shrink-0 mt-0.5" />
          <p className="text-sm text-red-700 dark:text-red-300">{error}</p>
        </div>
      )}

      {/* Email/Username Field */}
      <div>
        <label htmlFor="usernameOrEmail" className="block text-sm font-medium text-foreground mb-2">
          Email hoặc Tên đăng nhập
        </label>
        <div className="relative">
          <Mail className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground" />
          <input
            type="text"
            id="usernameOrEmail"
            name="usernameOrEmail"
            value={formData.usernameOrEmail}
            onChange={handleChange}
            placeholder="you@example.com"
            className="w-full pl-10 pr-4 py-3 border border-input rounded-lg bg-background text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent transition-all"
            disabled={loading}
          />
        </div>
        {errors.usernameOrEmail && (
          <p className="text-xs text-red-600 mt-1">{errors.usernameOrEmail}</p>
        )}
      </div>

      {/* Password Field */}
      <div>
        <label htmlFor="password" className="block text-sm font-medium text-foreground mb-2">
          Mật khẩu
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
            className="w-full pl-10 pr-12 py-3 border border-input rounded-lg bg-background text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent transition-all"
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
        {errors.password && (
          <p className="text-xs text-red-600 mt-1">{errors.password}</p>
        )}
      </div>

      {/* Remember Me & Forgot Password */}
      <div className="flex items-center justify-between text-sm">
        <label className="flex items-center gap-2 cursor-pointer">
          <input
            type="checkbox"
            className="w-4 h-4 rounded border-input text-primary focus:ring-primary"
            disabled={loading}
          />
          <span className="text-muted-foreground">Ghi nhớ đăng nhập</span>
        </label>
        <Link
          href={ROUTES.FORGOT_PASSWORD}
          className="text-primary hover:underline font-medium"
        >
          Quên mật khẩu?
        </Link>
      </div>

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
            Đang đăng nhập...
          </>
        ) : (
          'Đăng nhập'
        )}
      </Button>

      {/* Social Login */}
      <SocialLogin mode="login" loading={loading} />
    </form>
  );
}
