'use client';

import { useState } from 'react';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { adminUserAPI } from '@/lib/api';
import { toast } from 'sonner';
import type { CreateUserRequest } from '@/types';

interface CreateUserDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSuccess: () => void;
}

export function CreateUserDialog({ open, onOpenChange, onSuccess }: CreateUserDialogProps) {
  const [isLoading, setIsLoading] = useState(false);
  const [formData, setFormData] = useState<CreateUserRequest>({
    email: '',
    password: '',
    fullName: '',
    role: 'CUSTOMER',
  });

  const [errors, setErrors] = useState<Record<string, string>>({});

  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {};

    // Username validation (optional, but if provided must be >= 3 chars)
    if (formData.username && formData.username.trim() && formData.username.trim().length < 3) {
      newErrors.username = 'Username phải có ít nhất 3 ký tự';
    }

    // Email validation
    if (!formData.email.match(/^[^\s@]+@[^\s@]+\.[^\s@]+$/)) {
      newErrors.email = 'Email không hợp lệ';
    }

    // Password validation
    if (formData.password.length < 8) {
      newErrors.password = 'Mật khẩu phải có ít nhất 8 ký tự';
    }

    // Full name validation
    if (!formData.fullName.trim()) {
      newErrors.fullName = 'Họ tên không được để trống';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validateForm()) return;

    setIsLoading(true);
    try {
      // Clean up data: only send fields that have values
      const requestData: CreateUserRequest = {
        email: formData.email.trim(),
        password: formData.password,
        fullName: formData.fullName.trim(),
        role: formData.role,
      };

      // Optional fields: only include if they have values
      if (formData.username?.trim()) {
        requestData.username = formData.username.trim();
      }
      if (formData.phoneNumber?.trim()) {
        requestData.phoneNumber = formData.phoneNumber.trim();
      }
      if (formData.gender) {
        requestData.gender = formData.gender;
      }
      if (formData.dateOfBirth) {
        requestData.dateOfBirth = formData.dateOfBirth;
      }

      await adminUserAPI.createUser(requestData);
      toast.success('Thành công', {
        description: 'Tạo tài khoản mới thành công',
      });
      onSuccess();
      onOpenChange(false);
      
      // Reset form
      setFormData({
        email: '',
        password: '',
        fullName: '',
        role: 'CUSTOMER',
      });
      setErrors({});
    } catch (error: any) {
      toast.error('Lỗi', {
        description: error.message || 'Không thể tạo tài khoản',
      });
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[600px] max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Tạo tài khoản mới</DialogTitle>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-4">
          {/* Username - Optional */}
          <div className="space-y-2">
            <Label htmlFor="username">
              Username <span className="text-sm text-muted-foreground">(Tùy chọn - Hệ thống tự tạo nếu để trống)</span>
            </Label>
            <Input
              id="username"
              value={formData.username || ''}
              onChange={(e) => setFormData({ ...formData, username: e.target.value })}
              placeholder="john_doe"
              className={errors.username ? 'border-red-500' : ''}
            />
            {errors.username && <p className="text-sm text-red-500">{errors.username}</p>}
          </div>

          {/* Email - Required */}
          <div className="space-y-2">
            <Label htmlFor="email">
              Email <span className="text-red-500">*</span>
            </Label>
            <Input
              id="email"
              type="email"
              value={formData.email}
              onChange={(e) => setFormData({ ...formData, email: e.target.value })}
              placeholder="example@email.com"
              className={errors.email ? 'border-red-500' : ''}
            />
            {errors.email && <p className="text-sm text-red-500">{errors.email}</p>}
          </div>

          {/* Password - Required */}
          <div className="space-y-2">
            <Label htmlFor="password">
              Mật khẩu <span className="text-red-500">*</span>
            </Label>
            <Input
              id="password"
              type="password"
              value={formData.password}
              onChange={(e) => setFormData({ ...formData, password: e.target.value })}
              placeholder="••••••••"
              className={errors.password ? 'border-red-500' : ''}
            />
            {errors.password && <p className="text-sm text-red-500">{errors.password}</p>}
          </div>

          {/* Full Name - Required */}
          <div className="space-y-2">
            <Label htmlFor="fullName">
              Họ tên <span className="text-red-500">*</span>
            </Label>
            <Input
              id="fullName"
              value={formData.fullName}
              onChange={(e) => setFormData({ ...formData, fullName: e.target.value })}
              placeholder="Nguyễn Văn A"
              className={errors.fullName ? 'border-red-500' : ''}
            />
            {errors.fullName && <p className="text-sm text-red-500">{errors.fullName}</p>}
          </div>

          {/* Phone Number - Optional */}
          <div className="space-y-2">
            <Label htmlFor="phoneNumber">Số điện thoại</Label>
            <Input
              id="phoneNumber"
              value={formData.phoneNumber || ''}
              onChange={(e) => setFormData({ ...formData, phoneNumber: e.target.value })}
              placeholder="0123456789"
            />
          </div>

          {/* Gender - Optional */}
          <div className="space-y-2">
            <Label htmlFor="gender">Giới tính</Label>
            <Select
              value={formData.gender || ''}
              onValueChange={(value: 'MALE' | 'FEMALE' | 'OTHER') =>
                setFormData({ ...formData, gender: value })
              }
            >
              <SelectTrigger>
                <SelectValue placeholder="Chọn giới tính" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="MALE">Nam</SelectItem>
                <SelectItem value="FEMALE">Nữ</SelectItem>
                <SelectItem value="OTHER">Khác</SelectItem>
              </SelectContent>
            </Select>
          </div>

          {/* Date of Birth - Optional */}
          <div className="space-y-2">
            <Label htmlFor="dateOfBirth">Ngày sinh</Label>
            <Input
              id="dateOfBirth"
              type="date"
              value={formData.dateOfBirth || ''}
              onChange={(e) => setFormData({ ...formData, dateOfBirth: e.target.value })}
              max={new Date().toISOString().split('T')[0]}
            />
          </div>

          {/* Role - Required */}
          <div className="space-y-2">
            <Label htmlFor="role">
              Loại tài khoản <span className="text-red-500">*</span>
            </Label>
            <Select
              value={formData.role}
              onValueChange={(value: 'CUSTOMER' | 'ADMIN') => setFormData({ ...formData, role: value })}
            >
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="CUSTOMER">Khách hàng</SelectItem>
                <SelectItem value="ADMIN">Quản trị viên</SelectItem>
              </SelectContent>
            </Select>
          </div>

          <DialogFooter className="gap-2">
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)} disabled={isLoading}>
              Hủy
            </Button>
            <Button type="submit" disabled={isLoading}>
              {isLoading ? 'Đang tạo...' : 'Tạo tài khoản'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
