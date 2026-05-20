'use client';

import { useEffect, useState, useRef } from 'react';
import { useRouter } from 'next/navigation';
import { Loader2, AlertCircle, CheckCircle2 } from 'lucide-react';
import { setAuthTokens, setStoredUser, userAPI } from '@/lib/api';
import { useAuth } from '@/hooks';
import { ROUTES } from '@/lib/constants';

export default function GoogleCallbackPage() {
  const router = useRouter();
  const { setUser } = useAuth();
  const [status, setStatus] = useState<'loading' | 'success' | 'error'>('loading');
  const [message, setMessage] = useState<string>('Đang xử lý đăng nhập Google...');
  const hasProcessed = useRef(false);

  useEffect(() => {
    if (typeof window === 'undefined') return;

    // Tránh chạy nhiều lần (React Strict Mode hoặc re-render)
    if (hasProcessed.current) {
      console.log('Already processed, skipping...');
      return;
    }

    const hash = window.location.hash.startsWith('#')
      ? window.location.hash.substring(1)
      : window.location.hash;

    const params = new URLSearchParams(hash);
    const accessToken = params.get('accessToken');
    const refreshToken = params.get('refreshToken');
    const tokenType = params.get('tokenType');
    const expiresIn = params.get('expiresIn');

    const processLogin = async () => {
      // Đánh dấu đã xử lý ngay từ đầu
      hasProcessed.current = true;

      if (!accessToken || !refreshToken) {
        setStatus('error');
        setMessage(
          'Đã có lỗi xảy ra với việc đăng nhập Google, vui lòng thử lại.'
        );
        return;
      }

      try {
        console.log('Processing Google login callback...');

        // Lưu token vào localStorage
        setAuthTokens(accessToken, refreshToken);
        console.log('Tokens saved to localStorage');

        // Verify token was saved
        const savedToken = localStorage.getItem('accessToken');
        if (!savedToken) {
          throw new Error('Failed to save access token to localStorage');
        }
        console.log('Token verified in localStorage');

        // Gọi API lấy thông tin người dùng hiện tại
        console.log('Calling userAPI.getMe()...');
        const meResponse = await userAPI.getMe();
        console.log('getMe response:', meResponse);

        // Kiểm tra response format
        // Backend trả về: { success: true, status: 200, message: "...", data: UserResponse }
        if (meResponse && meResponse.success !== false && meResponse.data) {
          console.log('Login successful, user data:', meResponse.data);
          setStoredUser(meResponse.data);
          setUser(meResponse.data);
          setStatus('success');
          setMessage('Đăng nhập Google thành công! Đang chuyển hướng...');

          // Delete hash from URL
          window.history.replaceState(null, '', window.location.pathname);

          // Get role from response to decide redirect
          const role = meResponse.data?.role;
          const targetRoute = role === 'ADMIN' ? ROUTES.ADMIN : ROUTES.USER;

          // Redirect to appropriate dashboard
          setTimeout(() => {
            router.push(targetRoute);
          }, 800);
        } else {
          console.error('Invalid response format or missing data:', {
            success: meResponse?.success,
            status: meResponse?.status,
            hasData: !!meResponse?.data,
            fullResponse: meResponse
          });
          setStatus('error');
          setMessage(
            meResponse?.message ||
            'Không thể lấy thông tin người dùng sau khi đăng nhập Google.'
          );
        }
      } catch (error) {
        console.error('Google login callback error:', error);
        setStatus('error');
        const errorMessage = error instanceof Error
          ? error.message
          : 'Đã có lỗi xảy ra với việc đăng nhập Google, vui lòng thử lại.';
        setMessage(errorMessage);

        // Log chi tiết để debug
        if (error instanceof Error) {
          console.error('Error details:', {
            message: error.message,
            stack: error.stack
          });
        }
      }
    };

    void processLogin();
  }, [router, setUser]);

  return (
    <div className="min-h-screen flex items-center justify-center bg-background px-4">
      <div className="max-w-md w-full bg-card border border-border rounded-2xl shadow-lg p-8 text-center space-y-4">
        {status === 'loading' && (
          <>
            <Loader2 className="w-10 h-10 mx-auto animate-spin text-primary" />
            <h1 className="text-lg font-semibold mt-4">
              Đang xử lý đăng nhập Google...
            </h1>
            <p className="text-muted-foreground text-sm">
              Vui lòng đợi trong giây lát, hệ thống đang hoàn tất quá trình đăng
              nhập.
            </p>
          </>
        )}

        {status === 'success' && (
          <>
            <CheckCircle2 className="w-10 h-10 mx-auto text-green-500" />
            <h1 className="text-lg font-semibold mt-4">
              Đăng nhập Google thành công
            </h1>
            <p className="text-muted-foreground text-sm">{message}</p>
          </>
        )}

        {status === 'error' && (
          <>
            <AlertCircle className="w-10 h-10 mx-auto text-red-500" />
            <h1 className="text-lg font-semibold mt-4">
              Không thể đăng nhập bằng Google
            </h1>
            <p className="text-muted-foreground text-sm mb-4">{message}</p>
            <button
              type="button"
              onClick={() => router.push(ROUTES.LOGIN)}
              className="inline-flex items-center justify-center px-4 py-2 rounded-lg bg-primary text-primary-foreground hover:bg-primary/90 text-sm font-medium transition-colors"
            >
              Quay lại trang đăng nhập
            </button>
          </>
        )}
      </div>
    </div>
  );
}


