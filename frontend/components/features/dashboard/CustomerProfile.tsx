/**
 * CustomerProfile component - Profile view for customer users
 */

'use client';

import { useState } from 'react';
import { Edit, Settings } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { useAuth } from '@/lib/auth-context';
import { userAPI } from '@/lib/api';
import { toast } from 'sonner';
import type { User } from '@/types';

interface CustomerProfileProps {
  user: User;
}

export function CustomerProfile({ user }: CustomerProfileProps) {
  const { setUser } = useAuth();
  const [editOpen, setEditOpen] = useState(false);
  const [passwordOpen, setPasswordOpen] = useState(false);

  const [fullName, setFullName] = useState(user.fullName);
  const [phoneNumber, setPhoneNumber] = useState(user.phoneNumber || '');
  const [isUpdating, setIsUpdating] = useState(false);

  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [isChangingPassword, setIsChangingPassword] = useState(false);

  const getStatusLabel = (status: User['status']) => {
    if (status === 'ACTIVE') return 'Hoạt động';
    if (status === 'EMAIL_VERIFIED') return 'Đã xác thực email';
    if (status === 'LOCKED') return 'Đã khóa';
    return status;
  };

  const handleUpdateProfile = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsUpdating(true);
    try {
      const response = await userAPI.updateProfile({
        fullName: fullName.trim(),
        phoneNumber: phoneNumber.trim() || undefined,
      });

      if (response.success && response.data) {
        setUser(response.data);
        toast.success('Cập nhật thông tin thành công');
        setEditOpen(false);
      } else {
        toast.error(response.message || 'Không thể cập nhật thông tin');
      }
    } catch (error: any) {
      toast.error('Lỗi', {
        description: error.message || 'Không thể cập nhật thông tin',
      });
    } finally {
      setIsUpdating(false);
    }
  };

  const handleChangePassword = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!currentPassword || !newPassword || !confirmPassword) {
      toast.error('Vui lòng điền đầy đủ thông tin mật khẩu');
      return;
    }

    if (newPassword !== confirmPassword) {
      toast.error('Mật khẩu xác nhận không khớp');
      return;
    }

    setIsChangingPassword(true);
    try {
      const response = await userAPI.changePassword({
        currentPassword,
        newPassword,
        confirmPassword,
      });

      if (response.success) {
        toast.success('Đổi mật khẩu thành công');
        setPasswordOpen(false);
        setCurrentPassword('');
        setNewPassword('');
        setConfirmPassword('');
      } else {
        toast.error(response.message || 'Không thể đổi mật khẩu');
      }
    } catch (error: any) {
      toast.error('Lỗi', {
        description: error.message || 'Không thể đổi mật khẩu',
      });
    } finally {
      setIsChangingPassword(false);
    }
  };

  return (
    <div className="max-w-2xl space-y-6">
      <div className="rounded-xl border border-border bg-card p-6 shadow-sm">
        <h3 className="mb-4 text-lg font-semibold text-foreground">Thông tin cá nhân</h3>
        <div className="space-y-4">
          <div className="flex items-center gap-4">
            <div className="flex h-20 w-20 items-center justify-center rounded-full bg-primary text-3xl font-bold text-primary-foreground shadow-sm">
              {user.fullName?.charAt(0) || 'U'}
            </div>
            <div>
              <p className="text-xl font-bold text-foreground">{user.fullName}</p>
              <p className="text-muted-foreground">{user.email}</p>
            </div>
          </div>

          <div className="grid gap-4 mt-6">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-muted-foreground mb-1">Tên đăng nhập</label>
                <p className="text-foreground">{user.username}</p>
              </div>
              <div>
                <label className="block text-sm font-medium text-muted-foreground mb-1">Số điện thoại</label>
                <p className="text-foreground">{user.phoneNumber || 'Chưa cập nhật'}</p>
              </div>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-muted-foreground mb-1">Vai trò</label>
                <p className="text-foreground">{user.role === 'ADMIN' ? 'Quản trị viên' : 'Khách hàng'}</p>
              </div>
              <div>
                <label className="block text-sm font-medium text-muted-foreground mb-1">Trạng thái</label>
                <span className="px-2 py-1 rounded-full text-xs font-semibold bg-green-100 text-green-700">
                  {getStatusLabel(user.status)}
                </span>
              </div>
            </div>
          </div>

          <div className="flex gap-3 mt-6">
            <Button onClick={() => setEditOpen(true)}>
              <Edit className="w-4 h-4 mr-2" />
              Chỉnh sửa
            </Button>
            <Button variant="outline" onClick={() => setPasswordOpen(true)}>
              <Settings className="w-4 h-4 mr-2" />
              Đổi mật khẩu
            </Button>
          </div>
        </div>
      </div>

      {/* Edit Profile Dialog */}
      <Dialog open={editOpen} onOpenChange={setEditOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Cập nhật thông tin cá nhân</DialogTitle>
          </DialogHeader>
          <form onSubmit={handleUpdateProfile} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="fullName">Họ tên</Label>
              <Input
                id="fullName"
                value={fullName}
                onChange={(e) => setFullName(e.target.value)}
                required
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="phoneNumber">Số điện thoại</Label>
              <Input
                id="phoneNumber"
                value={phoneNumber}
                onChange={(e) => setPhoneNumber(e.target.value)}
                placeholder="0123456789"
              />
            </div>
            <DialogFooter className="gap-2">
              <Button type="button" variant="outline" onClick={() => setEditOpen(false)}>
                Hủy
              </Button>
              <Button type="submit" disabled={isUpdating}>
                {isUpdating ? 'Đang lưu...' : 'Lưu thay đổi'}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      {/* Change Password Dialog */}
      <Dialog open={passwordOpen} onOpenChange={setPasswordOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Đổi mật khẩu</DialogTitle>
          </DialogHeader>
          <form onSubmit={handleChangePassword} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="currentPassword">Mật khẩu hiện tại</Label>
              <Input
                id="currentPassword"
                type="password"
                value={currentPassword}
                onChange={(e) => setCurrentPassword(e.target.value)}
                required
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="newPassword">Mật khẩu mới</Label>
              <Input
                id="newPassword"
                type="password"
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                required
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="confirmPassword">Xác nhận mật khẩu mới</Label>
              <Input
                id="confirmPassword"
                type="password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                required
              />
            </div>
            <DialogFooter className="gap-2">
              <Button type="button" variant="outline" onClick={() => setPasswordOpen(false)}>
                Hủy
              </Button>
              <Button type="submit" disabled={isChangingPassword}>
                {isChangingPassword ? 'Đang đổi...' : 'Đổi mật khẩu'}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
}
