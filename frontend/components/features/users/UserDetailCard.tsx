'use client';

import { Button } from '@/components/ui/button';
import { X, Mail, Phone, Calendar, User as UserIcon, Shield } from 'lucide-react';
import type { User } from '@/types';

interface UserDetailCardProps {
  user: User;
  onClose: () => void;
}

export function UserDetailCard({ user, onClose }: UserDetailCardProps) {
  const formatDate = (dateString: string | null | undefined) => {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleDateString('vi-VN');
  };

  const getGenderLabel = (gender: string | null) => {
    if (!gender) return '-';
    const labels = {
      MALE: 'Nam',
      FEMALE: 'Nữ',
      OTHER: 'Khác',
    };
    return labels[gender as keyof typeof labels] || gender;
  };

  const getRoleBadge = (role: string) => {
    return role === 'ADMIN' ? (
      <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-purple-100 text-purple-800">
        <Shield className="w-3 h-3 mr-1" />
        Quản trị viên
      </span>
    ) : (
      <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
        <UserIcon className="w-3 h-3 mr-1" />
        Khách hàng
      </span>
    );
  };

  const getStatusBadge = (status: string) => {
    return status === 'ACTIVE' ? (
      <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
        Đang hoạt động
      </span>
    ) : (
      <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-800">
        Đã khóa
      </span>
    );
  };

  return (
    <div className="rounded-lg border bg-card p-6 space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <h3 className="text-lg font-semibold">Chi tiết người dùng</h3>
        <Button variant="ghost" size="icon" onClick={onClose}>
          <X className="h-4 w-4" />
        </Button>
      </div>

      {/* Content */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Left Column */}
        <div className="space-y-4">
          <div>
            <p className="text-sm text-muted-foreground mb-1">ID</p>
            <p className="font-medium">#{user.id}</p>
          </div>

          <div>
            <p className="text-sm text-muted-foreground mb-1">Username</p>
            <p className="font-medium">{user.username}</p>
          </div>

          <div>
            <p className="text-sm text-muted-foreground mb-1">Họ tên</p>
            <p className="font-medium">{user.fullName}</p>
          </div>

          <div>
            <p className="text-sm text-muted-foreground mb-1 flex items-center gap-1">
              <Mail className="h-4 w-4" /> Email
            </p>
            <p className="font-medium">{user.email}</p>
          </div>

          <div>
            <p className="text-sm text-muted-foreground mb-1 flex items-center gap-1">
              <Phone className="h-4 w-4" /> Số điện thoại
            </p>
            <p className="font-medium">{user.phoneNumber || '-'}</p>
          </div>
        </div>

        {/* Right Column */}
        <div className="space-y-4">
          <div>
            <p className="text-sm text-muted-foreground mb-1">Giới tính</p>
            <p className="font-medium">{getGenderLabel(user.gender)}</p>
          </div>

          <div>
            <p className="text-sm text-muted-foreground mb-1 flex items-center gap-1">
              <Calendar className="h-4 w-4" /> Ngày sinh
            </p>
            <p className="font-medium">{formatDate(user.dateOfBirth)}</p>
          </div>

          <div>
            <p className="text-sm text-muted-foreground mb-1">Loại tài khoản</p>
            <div>{getRoleBadge(user.role)}</div>
          </div>

          <div>
            <p className="text-sm text-muted-foreground mb-1">Trạng thái</p>
            <div>{getStatusBadge(user.status)}</div>
          </div>

          <div>
            <p className="text-sm text-muted-foreground mb-1">Ngày tạo</p>
            <p className="font-medium">{formatDate(user.createdAt)}</p>
          </div>
        </div>
      </div>
    </div>
  );
}
