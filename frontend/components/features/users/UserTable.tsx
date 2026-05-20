'use client';

import { Button } from '@/components/ui/button';
import { Lock, LockOpen, Eye } from 'lucide-react';
import type { User } from '@/types';
import { cn } from '@/lib/utils';

interface UserTableProps {
  users: User[];
  isLoading: boolean;
  onLockUser: (user: User) => void;
  onUnlockUser: (user: User) => void;
  onViewUser?: (user: User) => void;
}

export function UserTable({ users, isLoading, onLockUser, onUnlockUser, onViewUser }: UserTableProps) {
  if (isLoading) {
    return (
      <div className="rounded-lg border">
        <div className="p-8 text-center">
          <div className="inline-block h-8 w-8 animate-spin rounded-full border-4 border-solid border-primary border-r-transparent"></div>
          <p className="mt-4 text-sm text-muted-foreground">Đang tải dữ liệu...</p>
        </div>
      </div>
    );
  }

  if (!users || users.length === 0) {
    return (
      <div className="rounded-lg border">
        <div className="p-8 text-center">
          <p className="text-sm text-muted-foreground">Không có người dùng nào</p>
        </div>
      </div>
    );
  }

  return (
    <div className="rounded-lg border">
      <div className="overflow-x-auto">
        <table className="w-full">
          <thead>
            <tr className="border-b bg-muted/50">
              <th className="px-4 py-3 text-left text-sm font-medium">ID</th>
              <th className="px-4 py-3 text-left text-sm font-medium">Họ tên</th>
              <th className="px-4 py-3 text-left text-sm font-medium">Email</th>
              <th className="px-4 py-3 text-left text-sm font-medium">Số điện thoại</th>
              <th className="px-4 py-3 text-left text-sm font-medium">Loại tài khoản</th>
              <th className="px-4 py-3 text-left text-sm font-medium">Trạng thái</th>
              <th className="px-4 py-3 text-left text-sm font-medium">Ngày tạo</th>
              <th className="px-4 py-3 text-center text-sm font-medium">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            {users.map((user) => (
              <tr key={user.id} className="border-b transition-colors hover:bg-muted/50">
                <td className="px-4 py-3 text-sm">{user.id}</td>
                <td className="px-4 py-3 text-sm font-medium">{user.fullName}</td>
                <td className="px-4 py-3 text-sm">{user.email}</td>
                <td className="px-4 py-3 text-sm">{user.phoneNumber || '-'}</td>
                <td className="px-4 py-3 text-sm">
                  <span
                    className={cn(
                      'inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium',
                      user.role === 'ADMIN'
                        ? 'bg-purple-100 text-purple-800 dark:bg-purple-900/30 dark:text-purple-300'
                        : 'bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-300'
                    )}
                  >
                    {user.role === 'ADMIN' ? 'Quản trị viên' : 'Khách hàng'}
                  </span>
                </td>
                <td className="px-4 py-3 text-sm">
                  <span
                    className={cn(
                      'inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium',
                      user.status === 'ACTIVE'
                        ? 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-300'
                        : 'bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-300'
                    )}
                  >
                    {user.status === 'ACTIVE' ? 'Đang hoạt động' : 'Đã khóa'}
                  </span>
                </td>
                <td className="px-4 py-3 text-sm">
                  {user.createdAt ? new Date(user.createdAt).toLocaleDateString('vi-VN') : '-'}
                </td>
                <td className="px-4 py-3">
                  <div className="flex items-center justify-center gap-2">
                    {onViewUser && (
                      <Button variant="ghost" size="sm" onClick={() => onViewUser(user)} title="Xem chi tiết">
                        <Eye className="h-4 w-4" />
                      </Button>
                    )}
                    {user.role !== 'ADMIN' && (
                      <>
                        {user.status === 'ACTIVE' ? (
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => onLockUser(user)}
                            className="text-red-600 hover:text-red-700 hover:bg-red-50 dark:hover:bg-red-950"
                            title="Khóa tài khoản"
                          >
                            <Lock className="h-4 w-4" />
                          </Button>
                        ) : (
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => onUnlockUser(user)}
                            className="text-green-600 hover:text-green-700 hover:bg-green-50 dark:hover:bg-green-950"
                            title="Mở khóa tài khoản"
                          >
                            <LockOpen className="h-4 w-4" />
                          </Button>
                        )}
                      </>
                    )}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
