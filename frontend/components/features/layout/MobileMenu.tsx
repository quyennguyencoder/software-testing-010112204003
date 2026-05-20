/**
 * MobileMenu component - Mobile navigation menu với UI/UX cải thiện
 */

'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import {
  Search,
  Home,
  Smartphone,
  Bot,
  ShoppingCart,
  User,
  LogOut,
  ChevronRight,
  Sparkles,
  Zap,
  Tag,
  X,
  Heart,
  TrendingUp
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { ROUTES } from '@/lib/constants';
import { cn } from '@/lib/utils';
import { useCartStore, useWishlistStore } from '@/store';

interface MobileMenuProps {
  isOpen: boolean;
  user: any | null;
  onLogout: () => void;
  onClose?: () => void;
}

export function MobileMenu({ isOpen, user, onLogout, onClose }: MobileMenuProps) {
  const [searchKeyword, setSearchKeyword] = useState('');
  const router = useRouter();
  const { totalItems: cartItems } = useCartStore();
  const { totalItems: wishlistItems } = useWishlistStore();

  const handleSearch = () => {
    const keyword = searchKeyword.trim();
    if (!keyword) return;
    router.push(`/products?keyword=${encodeURIComponent(keyword)}`);
    onClose?.();
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') handleSearch();
  };

  if (!isOpen) return null;

  const menuItems = [
    { href: ROUTES.HOME, icon: Home, label: 'Trang chủ', color: 'text-blue-500' },
    { href: '/products', icon: Smartphone, label: 'Sản phẩm', color: 'text-green-500' },
    { href: '/products/featured', icon: Sparkles, label: 'Nổi bật', color: 'text-amber-500' },
    { href: '/products/best-selling', icon: TrendingUp, label: 'Bán chạy', color: 'text-pink-500' },
    { href: '/products/new-arrivals', icon: Zap, label: 'Mới nhất', color: 'text-purple-500' },
    { href: '/products/flash-sale', icon: Tag, label: 'Khuyến mãi', color: 'text-red-500' },
    { href: '/chatbot', icon: Bot, label: 'Trợ lý AI', color: 'text-cyan-500' },
  ];

  return (
    <div className="md:hidden fixed inset-0 z-50 bg-background/95 backdrop-blur-sm animate-in slide-in-from-top duration-300">
      {/* Header */}
      <div className="flex items-center justify-between px-4 py-3 border-b bg-primary">
        <span className="text-lg font-bold text-primary-foreground">Menu</span>
        <button
          onClick={onClose}
          className="p-2 rounded-full text-primary-foreground hover:bg-primary-foreground/20 transition-colors"
        >
          <X className="w-5 h-5" />
        </button>
      </div>

      <div className="overflow-y-auto h-[calc(100vh-60px)] pb-20">
        {/* Search */}
        <div className="p-4 border-b">
          <div className="relative">
            <input
              type="text"
              placeholder="Tìm kiếm sản phẩm..."
              value={searchKeyword}
              onChange={(e) => setSearchKeyword(e.target.value)}
              onKeyDown={handleKeyDown}
              className="w-full px-4 py-3 pl-11 rounded-xl bg-muted border-2 border-transparent focus:border-primary focus:outline-none transition-colors"
            />
            <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground" />
            <button
              onClick={handleSearch}
              className="absolute right-2 top-1/2 -translate-y-1/2 px-3 py-1.5 bg-primary text-primary-foreground text-sm font-medium rounded-lg"
            >
              Tìm
            </button>
          </div>
        </div>

        {/* User Info */}
        {user && (
          <div className="p-4 border-b bg-muted/50">
            <div className="flex items-center gap-3">
              <div className="w-12 h-12 rounded-full bg-gradient-to-br from-primary to-primary/70 flex items-center justify-center text-primary-foreground text-lg font-bold">
                {user.fullName?.charAt(0)?.toUpperCase() || 'U'}
              </div>
              <div className="flex-1">
                <p className="font-semibold text-foreground">{user.fullName}</p>
                <p className="text-sm text-muted-foreground">{user.email}</p>
              </div>
            </div>
          </div>
        )}

        {/* Menu Items */}
        <nav className="p-2">
          {menuItems.map((item) => (
            <Link
              key={item.href}
              href={item.href}
              onClick={onClose}
              className="flex items-center gap-4 px-4 py-3.5 rounded-xl hover:bg-muted transition-colors group"
            >
              <div className={cn("p-2 rounded-lg bg-muted group-hover:bg-background transition-colors", item.color)}>
                <item.icon className="w-5 h-5" />
              </div>
              <span className="flex-1 font-medium text-foreground">{item.label}</span>
              <ChevronRight className="w-4 h-4 text-muted-foreground group-hover:text-foreground transition-colors" />
            </Link>
          ))}

          {/* Divider */}
          <div className="my-2 mx-4 border-t" />

          {/* Wishlist */}
          <Link
            href="/wishlist"
            onClick={onClose}
            className="flex items-center gap-4 px-4 py-3.5 rounded-xl hover:bg-muted transition-colors group"
          >
            <div className="p-2 rounded-lg bg-muted group-hover:bg-background transition-colors text-red-500">
              <Heart className="w-5 h-5" />
            </div>
            <span className="flex-1 font-medium text-foreground">Yêu thích</span>
            {wishlistItems > 0 && (
              <span className="px-2 py-0.5 text-xs font-semibold bg-red-100 text-red-600 rounded-full">
                {wishlistItems}
              </span>
            )}
            <ChevronRight className="w-4 h-4 text-muted-foreground group-hover:text-foreground transition-colors" />
          </Link>

          {/* Cart */}
          <Link
            href="/cart"
            onClick={onClose}
            className="flex items-center gap-4 px-4 py-3.5 rounded-xl hover:bg-muted transition-colors group"
          >
            <div className="p-2 rounded-lg bg-muted group-hover:bg-background transition-colors text-orange-500">
              <ShoppingCart className="w-5 h-5" />
            </div>
            <span className="flex-1 font-medium text-foreground">Giỏ hàng</span>
            {cartItems > 0 && (
              <span className="px-2 py-0.5 text-xs font-semibold bg-orange-100 text-orange-600 rounded-full">
                {cartItems}
              </span>
            )}
            <ChevronRight className="w-4 h-4 text-muted-foreground group-hover:text-foreground transition-colors" />
          </Link>

          {/* Account Link */}
          {user && (
            <>
              <div className="my-2 mx-4 border-t" />
              <Link
                href={user.role === 'ADMIN' ? ROUTES.ADMIN : ROUTES.USER}
                onClick={onClose}
                className="flex items-center gap-4 px-4 py-3.5 rounded-xl hover:bg-muted transition-colors group"
              >
                <div className="p-2 rounded-lg bg-muted group-hover:bg-background transition-colors text-indigo-500">
                  <User className="w-5 h-5" />
                </div>
                <span className="flex-1 font-medium text-foreground">Tài khoản</span>
                <ChevronRight className="w-4 h-4 text-muted-foreground group-hover:text-foreground transition-colors" />
              </Link>
            </>
          )}
        </nav>

        {/* Auth Actions */}
        <div className="p-4 border-t mt-4">
          {user ? (
            <Button
              variant="destructive"
              onClick={() => {
                onLogout();
                onClose?.();
              }}
              className="w-full gap-2"
            >
              <LogOut className="w-4 h-4" />
              Đăng xuất
            </Button>
          ) : (
            <div className="space-y-2">
              <Link href={ROUTES.LOGIN} onClick={onClose}>
                <Button className="w-full" size="lg">
                  Đăng nhập
                </Button>
              </Link>
              <Link href={ROUTES.REGISTER} onClick={onClose}>
                <Button variant="outline" className="w-full" size="lg">
                  Đăng ký tài khoản
                </Button>
              </Link>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
