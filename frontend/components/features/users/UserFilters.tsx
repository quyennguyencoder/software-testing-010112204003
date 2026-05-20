'use client';

import { Input } from '@/components/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Search } from 'lucide-react';
import type { UserFilters } from '@/types';

interface UserFiltersComponentProps {
  filters: UserFilters;
  onFiltersChange: (filters: UserFilters) => void;
}

export function UserFiltersComponent({ filters, onFiltersChange }: UserFiltersComponentProps) {
  return (
    <div className="flex flex-col gap-4 sm:flex-row sm:items-center">
      {/* Search */}
      <div className="relative flex-1">
        <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
        <Input
          placeholder="Tìm theo ID người dùng..."
          type="number"
          value={filters.keyword || ''}
          onChange={(e) => onFiltersChange({ ...filters, keyword: e.target.value })}
          className="pl-10"
        />
      </div>

      {/* Role Filter */}
      <Select
        value={filters.role || 'ALL'}
        onValueChange={(value) => onFiltersChange({ ...filters, role: value as any })}
      >
        <SelectTrigger className="w-full sm:w-[180px]">
          <SelectValue placeholder="Loại tài khoản" />
        </SelectTrigger>
        <SelectContent>
          <SelectItem value="ALL">Tất cả</SelectItem>
          <SelectItem value="CUSTOMER">Khách hàng</SelectItem>
          <SelectItem value="ADMIN">Quản trị viên</SelectItem>
        </SelectContent>
      </Select>

      {/* Status Filter */}
      <Select
        value={filters.status || 'ALL'}
        onValueChange={(value) => onFiltersChange({ ...filters, status: value as any })}
      >
        <SelectTrigger className="w-full sm:w-[180px]">
          <SelectValue placeholder="Trạng thái" />
        </SelectTrigger>
        <SelectContent>
          <SelectItem value="ALL">Tất cả</SelectItem>
          <SelectItem value="ACTIVE">Đang hoạt động</SelectItem>
          <SelectItem value="LOCKED">Đã khóa</SelectItem>
        </SelectContent>
      </Select>
    </div>
  );
}
