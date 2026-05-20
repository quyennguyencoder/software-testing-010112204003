/**
 * Sidebar component - Collapsible sidebar for admin/customer dashboards
 */

'use client';

import Link from 'next/link';
import { Smartphone, Menu, X, Home, LogOut, LucideIcon } from 'lucide-react';
import { cn } from '@/lib/utils';

interface MenuItem {
    id: string;
    label: string;
    icon: LucideIcon;
}

interface SidebarProps {
    user: any;
    isAdmin: boolean;
    menuItems: MenuItem[];
    activeTab: string;
    sidebarOpen: boolean;
    mobileMenuOpen: boolean;
    onTabChange: (tab: string) => void;
    onToggleSidebar: () => void;
    onCloseMobileMenu: () => void;
    onLogout: () => void;
}

export function Sidebar({
    user,
    isAdmin,
    menuItems,
    activeTab,
    sidebarOpen,
    mobileMenuOpen,
    onTabChange,
    onToggleSidebar,
    onCloseMobileMenu,
    onLogout,
}: SidebarProps) {
    return (
        <div
            className={cn(
                "fixed h-screen bg-sidebar text-sidebar-foreground z-50 transition-all duration-300 flex flex-col",
                sidebarOpen ? "w-64" : "w-20",
                mobileMenuOpen ? "left-0" : "-left-64 lg:left-0"
            )}
        >
            {/* Logo */}
            <div className="p-4 border-b border-sidebar-border flex items-center justify-between flex-shrink-0">
                <Link href="/" className="flex items-center gap-2">
                    <Smartphone className="w-8 h-8 text-sidebar-primary flex-shrink-0" />
                    {sidebarOpen && <span className="text-lg font-bold">UTE Phone Hub</span>}
                </Link>
                <button
                    onClick={onToggleSidebar}
                    className="p-1.5 hover:bg-sidebar-accent rounded-lg hidden lg:block"
                >
                    {sidebarOpen ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
                </button>
                <button
                    onClick={onCloseMobileMenu}
                    className="p-1.5 hover:bg-sidebar-accent rounded-lg lg:hidden"
                >
                    <X className="w-5 h-5" />
                </button>
            </div>

            {/* User Info */}
            {sidebarOpen && (
                <div className="p-4 border-b border-sidebar-border flex-shrink-0">
                    <div className="flex items-center gap-3">
                        <div className="w-10 h-10 rounded-full bg-sidebar-primary text-sidebar-primary-foreground flex items-center justify-center font-bold">
                            {user.fullName?.charAt(0) || 'U'}
                        </div>
                        <div className="flex-1 min-w-0">
                            <p className="font-medium truncate">{user.fullName}</p>
                            <p className="text-xs text-sidebar-foreground/60 truncate">{user.email}</p>
                        </div>
                    </div>
                    <div className="mt-2">
                        <span className={cn(
                            "text-xs px-2 py-1 rounded-full",
                            isAdmin ? "bg-purple-500/20 text-purple-300" : "bg-blue-500/20 text-blue-300"
                        )}>
                            {isAdmin ? '👑 Admin' : '👤 Khách hàng'}
                        </span>
                    </div>
                </div>
            )}

            {/* Navigation */}
            <nav className="flex-1 p-4 space-y-1 overflow-y-auto">
                {menuItems.map((item) => {
                    const Icon = item.icon;
                    return (
                        <button
                            key={item.id}
                            onClick={() => {
                                onTabChange(item.id);
                                onCloseMobileMenu();
                            }}
                            className={cn(
                                "w-full flex items-center gap-3 px-4 py-3 rounded-lg transition-colors",
                                activeTab === item.id
                                    ? "bg-sidebar-primary text-sidebar-primary-foreground"
                                    : "text-sidebar-foreground hover:bg-sidebar-accent"
                            )}
                        >
                            <Icon className="w-5 h-5 flex-shrink-0" />
                            {sidebarOpen && <span>{item.label}</span>}
                        </button>
                    );
                })}
            </nav>

            {/* Bottom Actions */}
            <div className="p-4 border-t border-sidebar-border flex-shrink-0 space-y-1">
                <Link
                    href="/"
                    className="w-full flex items-center gap-3 px-4 py-3 rounded-lg text-sidebar-foreground hover:bg-sidebar-accent transition-colors"
                >
                    <Home className="w-5 h-5 flex-shrink-0" />
                    {sidebarOpen && <span>Trang chủ</span>}
                </Link>
                <button
                    onClick={onLogout}
                    className="w-full flex items-center gap-3 px-4 py-3 rounded-lg text-sidebar-foreground hover:bg-sidebar-accent transition-colors"
                >
                    <LogOut className="w-5 h-5 flex-shrink-0" />
                    {sidebarOpen && <span>Đăng xuất</span>}
                </button>
            </div>
        </div>
    );
}
