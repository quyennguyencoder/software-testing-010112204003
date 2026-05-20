"use client";

import React from 'react';
import initPromotions from '@/lib/initPromotions';
import { Button } from '@/components/ui/button';

export default function AvailablePromotions() {
  const promos = (initPromotions || []).filter((p) => (p as any).status === 'ACTIVE');

  if (!promos.length) return <div className="text-sm text-gray-600">Không có khuyến mãi nào hiện có.</div>;

  return (
    <div className="space-y-2">
      <div className="text-sm font-medium">Khuyến mãi hiện có</div>
      <div className="grid gap-2">
        {promos.map((p) => (
          <div key={String(p.id)} className="p-3 bg-yellow-50 rounded-md flex items-center justify-between">
            <div>
              <div className="text-sm font-semibold">{String(p.id)}</div>
              <div className="text-sm text-gray-700">{p.title} {p.description ? `— ${p.description}` : ''}</div>
            </div>
            <div className="text-right">
              { (p as any).percent_discount ? (
                <div className="font-bold text-red-600">Giảm {(p as any).percent_discount}%</div>
              ) : (p as any).fixed_amount ? (
                <div className="font-bold text-red-600">Giảm {(p as any).fixed_amount.toLocaleString('vi-VN')}₫</div>
              ) : (
                <div className="text-sm text-gray-600">Xem chi tiết</div>
              )}
              <div className="text-xs text-gray-500">Điều kiện: từ {(p as any).min_value_to_be_applied?.toLocaleString ? (p as any).min_value_to_be_applied.toLocaleString('vi-VN') : (p as any).min_value_to_be_applied}₫</div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
