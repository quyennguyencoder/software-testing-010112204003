/**
 * UserDashboardClient - Dashboard for customers
 */

"use client";

import { useEffect, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import {
    ShoppingCart,
    Menu,
    User,
    Heart,
    MapPin,
    Bell,
    CreditCard,
} from "lucide-react";
import { useAuth } from "@/lib/auth-context";
import { cn } from "@/lib/utils";
import {
    CustomerProfile,
    CustomerAddresses,
    CustomerWishlist,
    OrdersTable,
    PaymentHistory,
} from "@/components/features/dashboard";
import { Sidebar } from "@/components/features/layout/Sidebar";
import { useOrders } from "@/hooks";

type TabType =
    | "profile"
    | "orders"
    | "addresses"
    | "wishlist"
    | "payments";

export default function UserDashboardClient() {
    const router = useRouter();
    const searchParams = useSearchParams();
    const { user, logout, isLoading } = useAuth();
    const [sidebarOpen, setSidebarOpen] = useState(true);
    const [activeTab, setActiveTab] = useState<TabType>("profile");
    const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

    const isAdmin = user?.role === "ADMIN";
    const { orders, loading: ordersLoading } = useOrders(false); // false for customer

    // Customer menu items
    const menuItems = [
        { id: "profile" as TabType, label: "Thông tin cá nhân", icon: User },
        { id: "orders" as TabType, label: "Đơn hàng của tôi", icon: ShoppingCart },
        { id: "payments" as TabType, label: "Lịch sử thanh toán", icon: CreditCard },
        { id: "addresses" as TabType, label: "Địa chỉ", icon: MapPin },
        { id: "wishlist" as TabType, label: "Yêu thích", icon: Heart },
    ];

    useEffect(() => {
        // Redirect to login if not authenticated
        if (!isLoading && !user) {
            router.push("/login?redirect=/user");
        }

        // Set active tab from URL or default
        if (user) {
            const tabParam = searchParams.get("tab") as TabType | null;
            if (tabParam && menuItems.some((item) => item.id === tabParam)) {
                setActiveTab(tabParam);
            }
        }
    }, [user, isLoading, router, searchParams]);

    const handleLogout = () => {
        logout();
        router.push("/");
    };

    if (isLoading || !user) {
        return (
            <div className="min-h-screen bg-background flex items-center justify-center">
                <div className="text-center">
                    <div className="w-12 h-12 border-4 border-primary border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
                    <p className="text-muted-foreground">Đang tải...</p>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-secondary flex">
            {/* Mobile Menu Overlay */}
            {mobileMenuOpen && (
                <div
                    className="fixed inset-0 bg-black/50 z-40 lg:hidden"
                    onClick={() => setMobileMenuOpen(false)}
                />
            )}

            {/* Sidebar */}
            <Sidebar
                user={user}
                isAdmin={isAdmin}
                menuItems={menuItems}
                activeTab={activeTab}
                sidebarOpen={sidebarOpen}
                mobileMenuOpen={mobileMenuOpen}
                onTabChange={(tab) => {
                    setActiveTab(tab as TabType);
                    // Optional: update URL
                    // router.replace(`/user?tab=${tab}`);
                }}
                onToggleSidebar={() => setSidebarOpen(!sidebarOpen)}
                onCloseMobileMenu={() => setMobileMenuOpen(false)}
                onLogout={handleLogout}
            />

            {/* Main Content */}
            <div
                className={cn(
                    "flex-1 transition-all duration-300",
                    sidebarOpen ? "lg:ml-64" : "lg:ml-20"
                )}
            >
                {/* Top Bar */}
                <div className="bg-card border-b border-border p-4 flex items-center justify-between sticky top-0 z-30">
                    <div className="flex items-center gap-4">
                        <button
                            onClick={() => setMobileMenuOpen(true)}
                            className="p-2 hover:bg-secondary rounded-lg lg:hidden"
                        >
                            <Menu className="w-5 h-5" />
                        </button>
                        <h2 className="text-xl font-bold text-foreground">
                            {activeTab === "profile" && "Thông tin cá nhân"}
                            {activeTab === "orders" && "Đơn hàng của tôi"}
                            {activeTab === "payments" && "Lịch sử thanh toán"}
                            {activeTab === "addresses" && "Địa chỉ của tôi"}
                            {activeTab === "wishlist" && "Sản phẩm yêu thích"}
                        </h2>
                    </div>
                    <div className="flex items-center gap-3">
                        <button className="p-2 hover:bg-secondary rounded-lg relative">
                            <Bell className="w-5 h-5 text-muted-foreground" />
                            <span className="absolute top-1 right-1 w-2 h-2 bg-destructive rounded-full"></span>
                        </button>
                        <div className="hidden sm:flex items-center gap-2 text-sm">
                            <span className="text-muted-foreground">Xin chào,</span>
                            <span className="font-medium text-foreground">
                                {user.fullName}
                            </span>
                        </div>
                    </div>
                </div>

                {/* Content */}
                <div className="p-4 md:p-6">
                    {/* Customer Profile */}
                    {activeTab === "profile" && <CustomerProfile user={user} />}

                    {/* Customer Addresses */}
                    {activeTab === "addresses" && <CustomerAddresses />}

                    {/* Customer Wishlist */}
                    {activeTab === "wishlist" && <CustomerWishlist />}

                    {/* Orders */}
                    {activeTab === "orders" && (
                        ordersLoading ? (
                            <div className="bg-card rounded-xl border border-border p-6 animate-pulse h-64" />
                        ) : (
                            <OrdersTable orders={orders} isAdmin={false} />
                        )
                    )}

                    {/* Payment History */}
                    {activeTab === "payments" && <PaymentHistory />}
                </div>
            </div>
        </div>
    );
}
