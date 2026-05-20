/**
 * AdminDashboardClient - Dashboard for administrators
 */

"use client";

import { useEffect, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import {
    BarChart3,
    Users,
    ShoppingCart,
    Package,
    Menu,
    Bell,
    FolderTree,
    Tag,
    Ticket,
    FileText,
} from "lucide-react";
import { useAuth } from "@/lib/auth-context";
import { cn } from "@/lib/utils";
import {
    AdminDashboard,
    OrdersTable,
    UsersManagement,
    CategoryManagement,
    BrandManagement,
    AdminOrderDetailModal,
} from "@/components/features/dashboard";
import {
    PromotionsTable,
    TemplatesTable,
} from "@/components/features/promotion";
import { ProductsManagement } from "@/components/features/admin/ProductsManagement";
import { Sidebar } from "@/components/features/layout/Sidebar";
import { useOrders } from "@/hooks";

type TabType =
    | "dashboard"
    | "orders"
    | "products"
    | "categories"
    | "brands"
    | "promotions"
    | "templates"
    | "users";

export default function AdminDashboardClient() {
    const router = useRouter();
    const searchParams = useSearchParams();
    const { user, logout, isLoading } = useAuth();
    const [sidebarOpen, setSidebarOpen] = useState(true);
    const [activeTab, setActiveTab] = useState<TabType>("dashboard");
    const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
    const [selectedOrderId, setSelectedOrderId] = useState<number | null>(null);

    const isAdmin = user?.role === "ADMIN";
    const { orders, loading: ordersLoading, refetch: refetchOrders } = useOrders(true); // true for isAdmin

    // Admin menu items
    const menuItems = [
        { id: "dashboard" as TabType, label: "Dashboard", icon: BarChart3 },
        { id: "orders" as TabType, label: "Đơn hàng", icon: ShoppingCart },
        { id: "products" as TabType, label: "Sản phẩm", icon: Package },
        { id: "categories" as TabType, label: "Danh mục", icon: FolderTree },
        { id: "brands" as TabType, label: "Thương hiệu", icon: Tag },
        { id: "promotions" as TabType, label: "Khuyến mãi", icon: Ticket },
        { id: "templates" as TabType, label: "Templates", icon: FileText },
        { id: "users" as TabType, label: "Người dùng", icon: Users },
    ];

    useEffect(() => {
        // Redirect logic
        if (!isLoading) {
            if (!user) {
                router.push("/login");
            } else if (!isAdmin) {
                router.push("/user"); // Redirect non-admins to user dashboard
            }
        }

        // Set active tab from URL or default
        if (user && isAdmin) {
            const tabParam = searchParams.get("tab") as TabType | null;
            if (tabParam && menuItems.some((item) => item.id === tabParam)) {
                setActiveTab(tabParam);
            }
        }
    }, [user, isLoading, router, isAdmin, searchParams]);

    const handleLogout = () => {
        logout();
        router.push("/");
    };

    if (isLoading || !user || !isAdmin) {
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
                isAdmin={true}
                menuItems={menuItems}
                activeTab={activeTab}
                sidebarOpen={sidebarOpen}
                mobileMenuOpen={mobileMenuOpen}
                onTabChange={(tab) => {
                    setActiveTab(tab as TabType);
                    // Optional: update URL query param
                    // router.replace(`/admin?tab=${tab}`);
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
                            {activeTab === "dashboard" && "Dashboard"}
                            {activeTab === "products" && "Quản lý sản phẩm"}
                            {activeTab === "orders" && "Quản lý đơn hàng"}
                            {activeTab === "categories" && "Quản lý danh mục"}
                            {activeTab === "brands" && "Quản lý thương hiệu"}
                            {activeTab === "promotions" && "Quản lý khuyến mãi"}
                            {activeTab === "templates" && "Quản lý Templates"}
                            {activeTab === "users" && "Quản lý người dùng"}
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
                    {/* Admin Dashboard */}
                    {activeTab === "dashboard" && <AdminDashboard />}

                    {/* Products Management */}
                    {activeTab === "products" && <ProductsManagement />}

                    {/* Categories Management */}
                    {activeTab === "categories" && <CategoryManagement />}

                    {/* Brands Management */}
                    {activeTab === "brands" && <BrandManagement />}

                    {/* Promotions Management */}
                    {activeTab === "promotions" && <PromotionsTable />}

                    {/* Templates Management */}
                    {activeTab === "templates" && <TemplatesTable />}

                    {/* Orders */}
                    {activeTab === "orders" && (
                        ordersLoading ? (
                            <div className="bg-card rounded-xl border border-border p-6 animate-pulse h-64" />
                        ) : (
                            <OrdersTable
                                orders={orders}
                                isAdmin={true}
                                onViewDetail={(orderId) => setSelectedOrderId(orderId)}
                            />
                        )
                    )}

                    {/* Order Detail Modal */}
                    {selectedOrderId && (
                        <AdminOrderDetailModal
                            orderId={selectedOrderId}
                            onClose={() => setSelectedOrderId(null)}
                            onStatusUpdate={() => refetchOrders()}
                        />
                    )}

                    {/* Users Management */}
                    {activeTab === "users" && <UsersManagement />}
                </div>
            </div>
        </div>
    );
}
