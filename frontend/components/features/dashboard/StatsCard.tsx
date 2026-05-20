/**
 * StatsCard component - Display statistics with icon and trend
 */

'use client';

import { LucideIcon } from 'lucide-react';
import { cn } from '@/lib/utils';

interface StatsCardProps {
  label: string;
  value: string;
  change: string;
  icon: LucideIcon;
  colorClass: string;
}

export function StatsCard({ label, value, change, icon: Icon, colorClass }: StatsCardProps) {
  return (
    <div className="bg-card rounded-xl border border-border p-4 md:p-6">
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-sm font-medium text-muted-foreground">
          {label}
        </h3>
        <div className={cn(colorClass, "p-2 md:p-3 rounded-lg bg-primary/15 text-primary")}>
          <Icon className="w-4 h-4 md:w-5 md:h-5" />
        </div>
      </div>
      <p className="text-xl md:text-2xl font-bold text-foreground mb-1">
        {value}
      </p>
      <p className="text-xs md:text-sm text-primary">{change} so với tháng trước</p>
    </div>
  );
}
