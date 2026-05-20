'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { Facebook, Mail, MapPin, Phone, Smartphone } from 'lucide-react';
import { cn } from '@/lib/utils';

export function Footer() {
  const pathname = usePathname();
  const currentYear = new Date().getFullYear();

  // Hide footer on dashboard routes
  if (pathname?.startsWith('/user') || pathname?.startsWith('/admin')) {
    return null;
  }

  return (
    <footer className="relative mt-16 border-t border-border/50 bg-gradient-to-b from-sidebar via-sidebar to-sidebar/95 text-sidebar-foreground">
      {/* Background decoration */}
      <div className="absolute inset-0 bg-gradient-to-br from-primary/5 via-transparent to-primary/5 opacity-50" />

      <div className="relative max-w-7xl mx-auto px-4 py-12 md:py-16">
        <div className="grid grid-cols-1 gap-8 md:grid-cols-2 lg:grid-cols-4">
          {/* Company Info */}
          <div className="space-y-4">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-primary to-amber-500 flex items-center justify-center shadow-lg">
                <Smartphone className="w-5 h-5 text-primary-foreground" />
              </div>
              <span className="text-xl font-bold bg-gradient-to-r from-primary-foreground to-primary-foreground/80 bg-clip-text text-transparent">
                UTE Phone Hub
              </span>
            </div>
            <p className="text-sm leading-relaxed text-muted-foreground max-w-xs">
              Hệ thống bán lẻ điện thoại di động uy tín hàng đầu Việt Nam
            </p>
            <div className="flex gap-3 pt-2">
              <a
                href="#"
                aria-label="Facebook UTE Phone Hub"
                className="group flex h-10 w-10 items-center justify-center rounded-full bg-gradient-to-br from-primary to-amber-500 text-primary-foreground shadow-md transition-all duration-300 hover:scale-110 hover:shadow-lg hover:shadow-primary/50"
              >
                <Facebook className="w-5 h-5 transition-transform group-hover:scale-110" />
              </a>
            </div>
          </div>

          {/* Customer Support */}
          <div>
            <h3 className="mb-4 text-sm font-bold uppercase tracking-wider text-foreground">
              Hỗ trợ khách hàng
            </h3>
            <ul className="space-y-2.5">
              {[
                { href: '#', label: 'Hướng dẫn mua hàng' },
                { href: '#', label: 'Chính sách đổi trả' },
                { href: '#', label: 'Chính sách bảo hành' },
                { href: '#', label: 'Câu hỏi thường gặp' },
              ].map((item) => (
                <li key={item.label}>
                  <Link
                    href={item.href}
                    className="group inline-flex items-center gap-2 text-sm text-muted-foreground transition-all duration-200 hover:text-primary hover:translate-x-1"
                  >
                    <span className="w-1.5 h-1.5 rounded-full bg-primary/0 group-hover:bg-primary transition-all duration-200" />
                    {item.label}
                  </Link>
                </li>
              ))}
            </ul>
          </div>

          {/* About */}
          <div>
            <h3 className="mb-4 text-sm font-bold uppercase tracking-wider text-foreground">
              Về chúng tôi
            </h3>
            <ul className="space-y-2.5">
              {[
                { href: '#', label: 'Giới thiệu công ty' },
                { href: '#', label: 'Tuyển dụng' },
                { href: '#', label: 'Tin tức' },
                { href: '#', label: 'Liên hệ' },
              ].map((item) => (
                <li key={item.label}>
                  <Link
                    href={item.href}
                    className="group inline-flex items-center gap-2 text-sm text-muted-foreground transition-all duration-200 hover:text-primary hover:translate-x-1"
                  >
                    <span className="w-1.5 h-1.5 rounded-full bg-primary/0 group-hover:bg-primary transition-all duration-200" />
                    {item.label}
                  </Link>
                </li>
              ))}
            </ul>
          </div>

          {/* Contact */}
          <div>
            <h3 className="mb-4 text-sm font-bold uppercase tracking-wider text-foreground">
              Liên hệ
            </h3>
            <ul className="space-y-3.5">
              <li className="flex items-start gap-3">
                <div className="mt-0.5 flex h-8 w-8 flex-shrink-0 items-center justify-center rounded-lg bg-gradient-to-br from-primary/20 to-amber-500/20">
                  <MapPin className="h-4 w-4 text-primary" />
                </div>
                <span className="text-sm leading-relaxed text-muted-foreground">
                  01 Đ. Võ Văn Ngân, Linh Chiểu, Thủ Đức, TP. Hồ Chí Minh
                </span>
              </li>
              <li className="flex items-center gap-3">
                <div className="flex h-8 w-8 flex-shrink-0 items-center justify-center rounded-lg bg-gradient-to-br from-primary/20 to-amber-500/20">
                  <Phone className="h-4 w-4 text-primary" />
                </div>
                <span className="text-sm text-muted-foreground">1800.1234 (Miễn phí)</span>
              </li>
              <li className="flex items-center gap-3">
                <div className="flex h-8 w-8 flex-shrink-0 items-center justify-center rounded-lg bg-gradient-to-br from-primary/20 to-amber-500/20">
                  <Mail className="h-4 w-4 text-primary" />
                </div>
                <a
                  href="mailto:support@utephonehub.vn"
                  className="text-sm text-muted-foreground transition-colors hover:text-primary"
                >
                  support@utephonehub.vn
                </a>
              </li>
            </ul>
          </div>
        </div>

        {/* Copyright */}
        <div className="mt-12 border-t border-border/50 pt-8">
          <div className="flex flex-col items-center justify-between gap-4 md:flex-row">
            <p className="text-xs text-muted-foreground">
              © {currentYear} UTE Phone Hub. All rights reserved.
            </p>
            <div className="flex items-center gap-4 text-xs text-muted-foreground">
              <Link href="#" className="transition-colors hover:text-primary">
                Privacy Policy
              </Link>
              <span className="text-border">•</span>
              <Link href="#" className="transition-colors hover:text-primary">
                Terms of Service
              </Link>
            </div>
          </div>
        </div>
      </div>
    </footer>
  );
}
