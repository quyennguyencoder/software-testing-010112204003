/**
 * TopBar component - Displays contact info and promotional messages
 */

'use client';

import { Phone, MapPin, Clock } from 'lucide-react';

export function TopBar() {
  return (
    <div className="bg-sidebar text-sidebar-foreground text-sm py-2">
      <div className="max-w-7xl mx-auto px-4 flex items-center justify-between">
        <div className="flex items-center gap-6">
          <span className="flex items-center gap-1">
            <Phone className="w-3 h-3" />
            Hotline: 1800.1234
          </span>
          <span className="hidden md:flex items-center gap-1">
            <MapPin className="w-3 h-3" />
            Hệ thống 100+ cửa hàng
          </span>
        </div>
        <div className="flex items-center gap-4">
          <span className="hidden sm:flex items-center gap-1">
            <Clock className="w-3 h-3" />
            8:00 - 22:00
          </span>
        </div>
      </div>
    </div>
  );
}
