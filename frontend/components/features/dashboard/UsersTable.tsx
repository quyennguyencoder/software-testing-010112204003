/**
 * UsersTable component - Display users in table format for admin
 */

'use client';

import { Edit, Trash2 } from 'lucide-react';
import { getUserStatus } from '@/lib/constants';
import { cn } from '@/lib/utils';
import type { UserData } from '@/lib/mockData';

interface UsersTableProps {
  users: UserData[];
}

export function UsersTable({ users }: UsersTableProps) {
  return (
    <div className="space-y-4">
      <h3 className="text-lg font-semibold text-foreground">Quản lý người dùng</h3>

      <div className="bg-card rounded-xl border border-border overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-border bg-secondary/50">
                <th className="text-left py-3 px-4 font-semibold text-muted-foreground">Tên</th>
                <th className="text-left py-3 px-4 font-semibold text-muted-foreground hidden sm:table-cell">Email</th>
                <th className="text-left py-3 px-4 font-semibold text-muted-foreground">Vai trò</th>
                <th className="text-left py-3 px-4 font-semibold text-muted-foreground hidden md:table-cell">
                  Trạng thái
                </th>
                <th className="text-left py-3 px-4 font-semibold text-muted-foreground hidden lg:table-cell">
                  Ngày tham gia
                </th>
                <th className="text-left py-3 px-4 font-semibold text-muted-foreground">Thao tác</th>
              </tr>
            </thead>
            <tbody>
              {users.map((userData) => {
                const statusConfig = getUserStatus(userData.status);
                return (
                  <tr key={userData.id} className="border-b border-border hover:bg-secondary/50">
                    <td className="py-3 px-4 font-medium">{userData.name}</td>
                    <td className="py-3 px-4 text-muted-foreground hidden sm:table-cell">{userData.email}</td>
                    <td className="py-3 px-4">
                      <span
                        className={cn(
                          "px-2 py-1 rounded-full text-xs font-semibold",
                          userData.role === 'ADMIN' ? "bg-purple-100 text-purple-700" : "bg-blue-100 text-blue-700"
                        )}
                      >
                        {userData.role === 'ADMIN' ? 'Admin' : 'Khách hàng'}
                      </span>
                    </td>
                    <td className="py-3 px-4 hidden md:table-cell">
                      <span className={cn("px-2 py-1 rounded-full text-xs font-semibold", statusConfig.class)}>
                        {statusConfig.label}
                      </span>
                    </td>
                    <td className="py-3 px-4 text-muted-foreground hidden lg:table-cell">{userData.joinDate}</td>
                    <td className="py-3 px-4">
                      <div className="flex gap-1">
                        <button className="p-2 hover:bg-secondary rounded-lg text-blue-600">
                          <Edit className="w-4 h-4" />
                        </button>
                        <button className="p-2 hover:bg-secondary rounded-lg text-red-600">
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
