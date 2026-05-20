/**
 * MainHeader component - Main navigation header with logo, search, and user actions
 */

"use client";

import { useState, useEffect, useRef, useCallback } from "react";
import Link from "next/link";
import Image from "next/image";
import { useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";
import {
  Smartphone,
  ShoppingCart,
  Search,
  User,
  Bot,
  Menu,
  Heart,
  X,
  Loader2,
} from "lucide-react";
import { MobileMenu } from "./MobileMenu";
import { ROUTES } from "@/lib/constants";
import { useCartStore, useWishlistStore } from "@/store";
import { cn } from "@/lib/utils";
import { searchProducts, type ProductCardResponse } from "@/services/new-product.service";

interface MainHeaderProps {
  user: any | null;
  onLogout: () => void;
}

export function MainHeader({ user, onLogout }: MainHeaderProps) {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [mobileSearchOpen, setMobileSearchOpen] = useState(false);
  const [searchKeyword, setSearchKeyword] = useState("");
  const [suggestions, setSuggestions] = useState<ProductCardResponse[]>([]);
  const [showSuggestions, setShowSuggestions] = useState(false);
  const [isSearching, setIsSearching] = useState(false);
  const { totalItems: cartItems } = useCartStore();
  const { totalItems: wishlistItems } = useWishlistStore();
  const router = useRouter();
  const searchRef = useRef<HTMLDivElement>(null);
  const mobileSearchRef = useRef<HTMLDivElement>(null);
  const debounceRef = useRef<NodeJS.Timeout | null>(null);

  // Debounced search for suggestions
  const fetchSuggestions = useCallback(async (keyword: string) => {
    if (!keyword.trim() || keyword.trim().length < 2) {
      setSuggestions([]);
      setShowSuggestions(false);
      return;
    }

    setIsSearching(true);
    try {
      const result = await searchProducts({
        keyword: keyword.trim(),
        page: 0,
        size: 5, // Only show top 5 suggestions
      });
      setSuggestions(result.content || []);
      setShowSuggestions(true);
    } catch (error) {
      console.error('Search suggestions error:', error);
      setSuggestions([]);
    } finally {
      setIsSearching(false);
    }
  }, []);

  // Handle input change with debounce
  const handleSearchChange = (value: string) => {
    setSearchKeyword(value);

    // Clear previous debounce
    if (debounceRef.current) {
      clearTimeout(debounceRef.current);
    }

    // Debounce 300ms
    debounceRef.current = setTimeout(() => {
      fetchSuggestions(value);
    }, 300);
  };

  // Close suggestions when clicking outside
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        searchRef.current &&
        !searchRef.current.contains(event.target as Node) &&
        mobileSearchRef.current &&
        !mobileSearchRef.current.contains(event.target as Node)
      ) {
        setShowSuggestions(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  // Cleanup debounce on unmount
  useEffect(() => {
    return () => {
      if (debounceRef.current) {
        clearTimeout(debounceRef.current);
      }
    };
  }, []);

  const handleSearchSubmit = () => {
    const keyword = searchKeyword.trim();
    if (!keyword) return;

    setShowSuggestions(false);
    const params = new URLSearchParams();
    params.set("keyword", keyword);
    router.push(`/products?${params.toString()}`);
    setMobileSearchOpen(false);
  };

  const handleSearchKeyDown = (event: React.KeyboardEvent<HTMLInputElement>) => {
    if (event.key === "Enter") {
      handleSearchSubmit();
    } else if (event.key === "Escape") {
      setShowSuggestions(false);
    }
  };

  const handleSuggestionClick = (productId: number) => {
    setShowSuggestions(false);
    setSearchKeyword("");
    router.push(`/products/${productId}`);
  };

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
    }).format(price);
  };

  // Suggestion dropdown component
  const SuggestionDropdown = () => {
    if (!showSuggestions) return null;

    return (
      <div className="absolute top-full left-0 right-0 mt-1 bg-white rounded-lg shadow-xl border border-gray-200 overflow-hidden z-50">
        {isSearching ? (
          <div className="flex items-center justify-center py-4">
            <Loader2 className="w-5 h-5 animate-spin text-primary" />
            <span className="ml-2 text-sm text-muted-foreground">Đang tìm kiếm...</span>
          </div>
        ) : suggestions.length > 0 ? (
          <>
            {suggestions.map((product) => (
              <button
                key={product.id}
                onClick={() => handleSuggestionClick(product.id)}
                className="w-full flex items-center gap-3 px-4 py-2.5 hover:bg-gray-50 transition-colors text-left"
              >
                <div className="w-12 h-12 relative flex-shrink-0 bg-gray-100 rounded-md overflow-hidden">
                  <Image
                    src={product.thumbnailUrl || '/placeholder-product.png'}
                    alt={product.name}
                    fill
                    className="object-cover"
                    sizes="48px"
                  />
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium text-gray-900 truncate">
                    {product.name}
                  </p>
                  <p className="text-sm text-primary font-semibold">
                    {formatPrice(product.discountedPrice || product.originalPrice)}
                  </p>
                </div>
              </button>
            ))}
            <button
              onClick={handleSearchSubmit}
              className="w-full py-2.5 text-center text-sm font-medium text-primary hover:bg-primary/5 border-t border-gray-100 transition-colors"
            >
              Xem tất cả kết quả cho "{searchKeyword}"
            </button>
          </>
        ) : searchKeyword.trim().length >= 2 ? (
          <div className="py-4 text-center text-sm text-muted-foreground">
            Không tìm thấy sản phẩm phù hợp
          </div>
        ) : null}
      </div>
    );
  };

  return (
    <header className="bg-primary sticky top-0 z-50 shadow-md">
      <div className="max-w-7xl mx-auto px-4 py-3">
        <div className="flex items-center justify-between gap-3">
          {/* Logo */}
          <Link
            href={ROUTES.HOME}
            className="flex items-center gap-2 flex-shrink-0"
          >
            <Smartphone className="w-7 h-7 sm:w-8 sm:h-8 text-primary-foreground" />
            <span className="text-lg sm:text-xl font-bold text-primary-foreground hidden sm:block">
              UTE Phone Hub
            </span>
          </Link>

          {/* Search Bar - Desktop */}
          <div className="flex-1 max-w-2xl hidden md:block" ref={searchRef}>
            <div className="relative">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground" />
              <input
                type="text"
                placeholder="Tìm kiếm điện thoại, phụ kiện..."
                value={searchKeyword}
                onChange={(event) => handleSearchChange(event.target.value)}
                onKeyDown={handleSearchKeyDown}
                onFocus={() => {
                  if (searchKeyword.trim().length >= 2 && suggestions.length > 0) {
                    setShowSuggestions(true);
                  }
                }}
                className="w-full px-4 py-2.5 pl-10 pr-20 rounded-full bg-white text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring/50 transition-all"
              />
              <button
                type="button"
                onClick={handleSearchSubmit}
                className="absolute right-2 top-1/2 -translate-y-1/2 px-4 py-1.5 rounded-full bg-primary text-primary-foreground text-sm font-medium hover:bg-primary/90 transition-colors"
              >
                Tìm
              </button>
              <SuggestionDropdown />
            </div>
          </div>

          {/* Actions */}
          <div className="flex items-center gap-1 sm:gap-2">
            {/* Mobile Search Toggle */}
            <button
              className="md:hidden p-2 text-primary-foreground hover:bg-primary-foreground/10 rounded-full transition-colors"
              onClick={() => setMobileSearchOpen(!mobileSearchOpen)}
            >
              {mobileSearchOpen ? <X className="w-5 h-5" /> : <Search className="w-5 h-5" />}
            </button>

            {/* Chatbot - Desktop */}
            <Link
              href="/chatbot"
              className="hidden sm:flex items-center gap-1.5 px-3 py-2 rounded-full text-primary-foreground hover:bg-primary-foreground/10 transition-colors"
            >
              <Bot className="w-5 h-5" />
              <span className="hidden lg:inline text-sm font-medium">AI Chatbot</span>
            </Link>

            {/* Wishlist */}
            <Link
              href="/wishlist"
              className="relative p-2 rounded-full text-primary-foreground hover:bg-primary-foreground/10 transition-colors"
            >
              <Heart className="w-5 h-5" />
              {wishlistItems > 0 && (
                <span className="absolute -top-0.5 -right-0.5 bg-red-500 text-white text-[10px] min-w-[16px] h-[16px] rounded-full flex items-center justify-center font-semibold">
                  {wishlistItems > 99 ? '99+' : wishlistItems}
                </span>
              )}
            </Link>

            {/* Cart */}
            <Link
              href="/cart"
              className="relative p-2 rounded-full text-primary-foreground hover:bg-primary-foreground/10 transition-colors"
            >
              <ShoppingCart className="w-5 h-5" />
              {cartItems > 0 && (
                <span className="absolute -top-0.5 -right-0.5 bg-orange-500 text-white text-[10px] min-w-[16px] h-[16px] rounded-full flex items-center justify-center font-semibold">
                  {cartItems > 99 ? '99+' : cartItems}
                </span>
              )}
            </Link>

            {/* User Menu - Desktop */}
            {user ? (
              <div className="hidden sm:flex items-center gap-2">
                <Link
                  href={user.role === 'ADMIN' ? ROUTES.ADMIN : ROUTES.USER}
                  className="flex items-center gap-1.5 px-3 py-2 rounded-full text-primary-foreground hover:bg-primary-foreground/10 transition-colors"
                >
                  <User className="w-5 h-5" />
                  <span className="hidden lg:inline text-sm font-medium truncate max-w-[100px]">
                    {user.fullName}
                  </span>
                </Link>
                <Button
                  variant="secondary"
                  size="sm"
                  onClick={onLogout}
                  className="rounded-full"
                >
                  Đăng xuất
                </Button>
              </div>
            ) : (
              <div className="hidden sm:flex items-center gap-2">
                <Link href={ROUTES.LOGIN}>
                  <Button variant="secondary" size="sm" className="rounded-full">
                    Đăng nhập
                  </Button>
                </Link>
                <Link href={ROUTES.REGISTER}>
                  <Button
                    variant="outline"
                    size="sm"
                    className="rounded-full border-primary-foreground/50 text-primary-foreground hover:bg-primary-foreground hover:text-primary"
                  >
                    Đăng ký
                  </Button>
                </Link>
              </div>
            )}

            {/* Mobile Menu Toggle */}
            <button
              className="md:hidden p-2 rounded-full text-primary-foreground hover:bg-primary-foreground/10 transition-colors"
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            >
              <Menu className="w-5 h-5" />
            </button>
          </div>
        </div>

        {/* Mobile Search Bar */}
        {mobileSearchOpen && (
          <div className="md:hidden pt-3 pb-1 animate-in slide-in-from-top duration-200" ref={mobileSearchRef}>
            <div className="relative">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground" />
              <input
                type="text"
                placeholder="Tìm kiếm sản phẩm..."
                value={searchKeyword}
                onChange={(event) => handleSearchChange(event.target.value)}
                onKeyDown={handleSearchKeyDown}
                onFocus={() => {
                  if (searchKeyword.trim().length >= 2 && suggestions.length > 0) {
                    setShowSuggestions(true);
                  }
                }}
                autoFocus
                className="w-full px-4 py-2.5 pl-10 pr-16 rounded-full bg-white text-foreground placeholder:text-muted-foreground focus:outline-none"
              />
              <button
                type="button"
                onClick={handleSearchSubmit}
                className="absolute right-2 top-1/2 -translate-y-1/2 px-4 py-1.5 rounded-full bg-primary text-primary-foreground text-sm font-medium"
              >
                Tìm
              </button>
              <SuggestionDropdown />
            </div>
          </div>
        )}
      </div>

      {/* Mobile Menu */}
      <MobileMenu
        isOpen={mobileMenuOpen}
        user={user}
        onLogout={onLogout}
        onClose={() => setMobileMenuOpen(false)}
      />
    </header>
  );
}
