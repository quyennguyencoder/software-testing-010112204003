'use client';

import React from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { Star, Flame, Zap, Clock, Package, ChevronRight } from 'lucide-react';
import { cn } from '@/lib/utils';

interface QuickLink {
  href: string;
  icon: React.ComponentType<{ className?: string }>;
  label: string;
  shortLabel: string;
  gradient: string;
  activeGradient: string;
  iconBg: string;
}

const QUICK_LINKS: QuickLink[] = [
  {
    href: '/products',
    icon: Package,
    label: 'Tất cả',
    shortLabel: 'Tất cả',
    gradient: 'from-slate-50 to-gray-50 border-slate-200 hover:border-slate-400',
    activeGradient: 'from-slate-100 to-gray-100 border-slate-500 ring-2 ring-slate-200',
    iconBg: 'from-slate-500 to-gray-600',
  },
  {
    href: '/products/featured',
    icon: Star,
    label: 'Nổi bật',
    shortLabel: 'Nổi bật',
    gradient: 'from-amber-50 to-orange-50 border-amber-200 hover:border-amber-400',
    activeGradient: 'from-amber-100 to-orange-100 border-amber-500 ring-2 ring-amber-200',
    iconBg: 'from-amber-400 to-orange-500',
  },
  {
    href: '/products/best-selling',
    icon: Flame,
    label: 'Bán chạy',
    shortLabel: 'HOT',
    gradient: 'from-orange-50 to-red-50 border-orange-200 hover:border-orange-400',
    activeGradient: 'from-orange-100 to-red-100 border-orange-500 ring-2 ring-orange-200',
    iconBg: 'from-orange-500 to-red-500',
  },
  {
    href: '/products/flash-sale',
    icon: Zap,
    label: 'Flash Sale',
    shortLabel: 'SALE',
    gradient: 'from-red-50 to-pink-50 border-red-200 hover:border-red-400',
    activeGradient: 'from-red-100 to-pink-100 border-red-500 ring-2 ring-red-200',
    iconBg: 'from-red-500 to-pink-500',
  },
  {
    href: '/products/new-arrivals',
    icon: Clock,
    label: 'Mới nhất',
    shortLabel: 'Mới',
    gradient: 'from-emerald-50 to-teal-50 border-emerald-200 hover:border-emerald-400',
    activeGradient: 'from-emerald-100 to-teal-100 border-emerald-500 ring-2 ring-emerald-200',
    iconBg: 'from-emerald-500 to-teal-500',
  },
];

interface QuickLinksBarProps {
  className?: string;
}

export function QuickLinksBar({ className }: QuickLinksBarProps) {
  const pathname = usePathname();

  return (
    <div className={cn('w-full', className)}>
      <div className="flex items-center gap-2 overflow-x-auto scrollbar-hide py-1">
        {QUICK_LINKS.map((link) => {
          const Icon = link.icon;
          const isActive = pathname === link.href;
          
          return (
            <Link
              key={link.href}
              href={link.href}
              className={cn(
                'group flex items-center gap-2 px-3 py-2 rounded-full border transition-all duration-200 whitespace-nowrap',
                'bg-gradient-to-r hover:shadow-md hover:scale-[1.02]',
                isActive ? link.activeGradient : link.gradient
              )}
            >
              <div className={cn(
                'p-1.5 rounded-full bg-gradient-to-br text-white shadow-sm',
                link.iconBg,
                isActive && 'shadow-md'
              )}>
                <Icon className="w-3.5 h-3.5" />
              </div>
              <span className={cn(
                'text-sm font-medium',
                isActive ? 'text-foreground' : 'text-muted-foreground group-hover:text-foreground'
              )}>
                <span className="hidden sm:inline">{link.label}</span>
                <span className="sm:hidden">{link.shortLabel}</span>
              </span>
              {link.href === '/products/flash-sale' && (
                <span className="hidden sm:inline-flex px-1.5 py-0.5 text-[10px] font-bold text-white bg-red-500 rounded-full animate-pulse">
                  HOT
                </span>
              )}
            </Link>
          );
        })}
      </div>
    </div>
  );
}
