/**
 * AdminOrderDetailModal - Modal for viewing and updating order details
 */

'use client';

import { useState, useEffect, useCallback } from 'react';
import { X, Package, MapPin, User, Phone, Mail, Calendar, CreditCard, Truck, FileText, CheckCircle } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { adminOrderAPI } from '@/lib/api';
import { formatPrice } from '@/lib/utils';
import { getOrderStatus } from '@/lib/constants';
import { cn } from '@/lib/utils';
import type { AdminOrderDetailResponse, OrderStatus } from '@/types';

interface AdminOrderDetailModalProps {
    orderId: number;
    onClose: () => void;
    onStatusUpdate?: () => void;
}

const STATUS_LABELS: Record<OrderStatus, string> = {
    PENDING: "Chờ xác nhận",
    CONFIRMED: "Đã xác nhận",
    SHIPPING: "Đang giao hàng",
    DELIVERED: "Đã giao hàng",
    CANCELLED: "Đã hủy",
};

export function AdminOrderDetailModal({ orderId, onClose, onStatusUpdate }: AdminOrderDetailModalProps) {
    const [order, setOrder] = useState<AdminOrderDetailResponse | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [selectedStatus, setSelectedStatus] = useState<OrderStatus | "">("");
    const [adminNote, setAdminNote] = useState("");
    const [updating, setUpdating] = useState(false);
    const [updateSuccess, setUpdateSuccess] = useState(false);

    const fetchOrderDetail = useCallback(async () => {
        try {
            setLoading(true);
            setError(null);
            const response = await adminOrderAPI.getOrderDetail(orderId);
            if (response.success && response.data) {
                setOrder(response.data);
                setSelectedStatus("");
            } else {
                setError(response.message || "Không thể tải thông tin đơn hàng");
            }
        } catch (err) {
            console.error("Error fetching order detail:", err);
            setError("Đã xảy ra lỗi khi tải thông tin đơn hàng");
        } finally {
            setLoading(false);
        }
    }, [orderId]);

    useEffect(() => {
        fetchOrderDetail();
    }, [fetchOrderDetail]);

    const handleStatusUpdate = async () => {
        if (!selectedStatus || !order) return;

        try {
            setUpdating(true);
            const response = await adminOrderAPI.updateOrderStatus(orderId, selectedStatus, adminNote || undefined);

            if (response.success && response.data) {
                setOrder(response.data);
                setSelectedStatus("");
                setAdminNote("");
                setUpdateSuccess(true);
                setTimeout(() => setUpdateSuccess(false), 3000);
                onStatusUpdate?.();
            } else {
                setError(response.message || "Không thể cập nhật trạng thái");
            }
        } catch (err) {
            console.error("Error updating order status:", err);
            setError("Đã xảy ra lỗi khi cập nhật trạng thái");
        } finally {
            setUpdating(false);
        }
    };

    return (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50 overflow-y-auto">
            <div className="bg-card rounded-xl border border-border max-w-4xl w-full max-h-[90vh] overflow-y-auto my-8">
                {/* Header */}
                <div className="sticky top-0 bg-card border-b border-border px-6 py-4 flex items-center justify-between z-10">
                    <div>
                        <h2 className="text-xl font-bold text-foreground">Chi tiết đơn hàng</h2>
                        {order && (
                            <p className="text-sm text-muted-foreground mt-1">
                                Mã đơn: <span className="font-medium text-foreground">{order.orderCode}</span>
                            </p>
                        )}
                    </div>
                    <button
                        onClick={onClose}
                        className="p-2 hover:bg-secondary rounded-lg transition-colors"
                    >
                        <X className="w-5 h-5" />
                    </button>
                </div>

                {/* Content */}
                <div className="p-6">
                    {/* Loading state */}
                    {loading && (
                        <div className="flex flex-col items-center justify-center py-12">
                            <div className="w-12 h-12 border-4 border-primary border-t-transparent rounded-full animate-spin mb-4" />
                            <p className="text-muted-foreground">Đang tải thông tin đơn hàng...</p>
                        </div>
                    )}

                    {/* Error state */}
                    {error && !loading && (
                        <div className="p-4 bg-red-50 border border-red-200 rounded-lg">
                            <p className="text-sm text-red-700">{error}</p>
                            <Button onClick={fetchOrderDetail} variant="outline" size="sm" className="mt-2">
                                Thử lại
                            </Button>
                        </div>
                    )}

                    {/* Success notification */}
                    {updateSuccess && (
                        <div className="mb-4 p-3 bg-green-50 border border-green-200 rounded-lg flex items-center gap-2">
                            <CheckCircle className="w-5 h-5 text-green-600" />
                            <p className="text-sm text-green-700 font-medium">Cập nhật trạng thái đơn hàng thành công!</p>
                        </div>
                    )}

                    {/* Order detail */}
                    {order && !loading && (
                        <div className="space-y-6">
                            {/* Status Badge */}
                            <div className="flex items-center justify-between">
                                <div className="flex items-center gap-3">
                                    <span className="text-sm text-muted-foreground">Trạng thái:</span>
                                    <span className={cn(
                                        "px-3 py-1 rounded-full text-sm font-semibold",
                                        getOrderStatus(order.status).class
                                    )}>
                                        {order.statusDisplay}
                                    </span>
                                </div>
                                <div className="text-sm text-muted-foreground">
                                    <Calendar className="w-4 h-4 inline-block mr-1" />
                                    {new Date(order.createdAt).toLocaleString('vi-VN')}
                                </div>
                            </div>

                            {/* Two columns layout */}
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                                {/* Customer Information */}
                                <div className="bg-secondary/30 rounded-lg p-4">
                                    <h3 className="font-semibold text-foreground mb-3 flex items-center gap-2">
                                        <User className="w-4 h-4 text-primary" />
                                        Thông tin khách hàng
                                    </h3>
                                    <div className="space-y-2 text-sm">
                                        <div className="flex items-center gap-2">
                                            <User className="w-4 h-4 text-muted-foreground" />
                                            <span className="text-muted-foreground">Tên:</span>
                                            <span className="font-medium">{order.customerName}</span>
                                        </div>
                                        <div className="flex items-center gap-2">
                                            <Mail className="w-4 h-4 text-muted-foreground" />
                                            <span className="text-muted-foreground">Email:</span>
                                            <span className="font-medium">{order.customerEmail}</span>
                                        </div>
                                        {order.customerPhone && (
                                            <div className="flex items-center gap-2">
                                                <Phone className="w-4 h-4 text-muted-foreground" />
                                                <span className="text-muted-foreground">Điện thoại:</span>
                                                <span className="font-medium">{order.customerPhone}</span>
                                            </div>
                                        )}
                                    </div>
                                </div>

                                {/* Shipping Information */}
                                <div className="bg-secondary/30 rounded-lg p-4">
                                    <h3 className="font-semibold text-foreground mb-3 flex items-center gap-2">
                                        <MapPin className="w-4 h-4 text-primary" />
                                        Thông tin giao hàng
                                    </h3>
                                    <div className="space-y-2 text-sm">
                                        <div className="flex items-start gap-2">
                                            <User className="w-4 h-4 text-muted-foreground mt-0.5" />
                                            <span className="text-muted-foreground">Người nhận:</span>
                                            <span className="font-medium">{order.recipientName}</span>
                                        </div>
                                        <div className="flex items-center gap-2">
                                            <Phone className="w-4 h-4 text-muted-foreground" />
                                            <span className="text-muted-foreground">SĐT:</span>
                                            <span className="font-medium">{order.recipientPhone}</span>
                                        </div>
                                        <div className="flex items-start gap-2">
                                            <MapPin className="w-4 h-4 text-muted-foreground mt-0.5" />
                                            <span className="text-muted-foreground">Địa chỉ:</span>
                                            <span className="font-medium">{order.shippingAddress}</span>
                                        </div>
                                        {order.shippingUnit && (
                                            <div className="flex items-center gap-2">
                                                <Truck className="w-4 h-4 text-muted-foreground" />
                                                <span className="text-muted-foreground">Đơn vị vận chuyển:</span>
                                                <span className="font-medium">{order.shippingUnit}</span>
                                            </div>
                                        )}
                                    </div>
                                </div>
                            </div>

                            {/* Order Items */}
                            <div className="border border-border rounded-lg overflow-hidden">
                                <div className="bg-secondary/50 px-4 py-3 flex items-center gap-2">
                                    <Package className="w-4 h-4 text-primary" />
                                    <h3 className="font-semibold text-foreground">Sản phẩm đặt hàng ({order.items.length})</h3>
                                </div>
                                <div className="divide-y divide-border">
                                    {order.items.map((item) => (
                                        <div key={item.id} className="p-4 flex items-center gap-4">
                                            {item.productThumbnail ? (
                                                <img
                                                    src={item.productThumbnail}
                                                    alt={item.productName}
                                                    className="w-16 h-16 object-cover rounded-lg border border-border"
                                                />
                                            ) : (
                                                <div className="w-16 h-16 bg-secondary rounded-lg flex items-center justify-center">
                                                    <Package className="w-8 h-8 text-muted-foreground" />
                                                </div>
                                            )}
                                            <div className="flex-1 min-w-0">
                                                <h4 className="font-medium text-foreground truncate">{item.productName}</h4>
                                                <p className="text-sm text-muted-foreground">
                                                    {formatPrice(item.price)} x {item.quantity}
                                                </p>
                                            </div>
                                            <div className="text-right">
                                                <p className="font-semibold text-primary">{formatPrice(item.totalPrice)}</p>
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            </div>

                            {/* Order Summary */}
                            <div className="bg-secondary/30 rounded-lg p-4">
                                <h3 className="font-semibold text-foreground mb-3 flex items-center gap-2">
                                    <CreditCard className="w-4 h-4 text-primary" />
                                    Tổng kết đơn hàng
                                </h3>
                                <div className="space-y-2 text-sm">
                                    <div className="flex justify-between">
                                        <span className="text-muted-foreground">Phương thức thanh toán:</span>
                                        <span className="font-medium">{order.paymentMethod}</span>
                                    </div>
                                    <div className="flex justify-between">
                                        <span className="text-muted-foreground">Phí vận chuyển:</span>
                                        <span className="font-medium">{formatPrice(order.shippingFee)}</span>
                                    </div>
                                    <div className="flex justify-between pt-2 border-t border-border">
                                        <span className="font-semibold text-foreground">Tổng cộng:</span>
                                        <span className="font-bold text-lg text-primary">{formatPrice(order.totalAmount)}</span>
                                    </div>
                                </div>
                            </div>

                            {/* Note */}
                            {order.note && (
                                <div className="bg-amber-50 border border-amber-200 rounded-lg p-4">
                                    <h3 className="font-semibold text-amber-800 mb-2 flex items-center gap-2">
                                        <FileText className="w-4 h-4" />
                                        Ghi chú đơn hàng
                                    </h3>
                                    <p className="text-sm text-amber-700">{order.note}</p>
                                </div>
                            )}

                            {/* Status Update Section */}
                            {order.availableStatusTransitions.length > 0 && (
                                <div className="border-t border-border pt-6 mt-6">
                                    <h3 className="font-semibold text-foreground mb-4">Cập nhật trạng thái đơn hàng</h3>
                                    <div className="flex flex-col sm:flex-row gap-4">
                                        <div className="flex-1">
                                            <label className="block text-sm text-muted-foreground mb-1">Trạng thái mới</label>
                                            <select
                                                value={selectedStatus}
                                                onChange={(e) => setSelectedStatus(e.target.value as OrderStatus)}
                                                className="w-full px-3 py-2 border border-border rounded-lg bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
                                            >
                                                <option value="">-- Chọn trạng thái --</option>
                                                {order.availableStatusTransitions.map((status) => (
                                                    <option key={status} value={status}>
                                                        {STATUS_LABELS[status]}
                                                    </option>
                                                ))}
                                            </select>
                                        </div>
                                        <div className="flex-1">
                                            <label className="block text-sm text-muted-foreground mb-1">Ghi chú (không bắt buộc)</label>
                                            <input
                                                type="text"
                                                value={adminNote}
                                                onChange={(e) => setAdminNote(e.target.value)}
                                                placeholder="Lý do cập nhật..."
                                                className="w-full px-3 py-2 border border-border rounded-lg bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
                                            />
                                        </div>
                                    </div>
                                    <div className="mt-4 flex justify-end">
                                        <Button
                                            onClick={handleStatusUpdate}
                                            disabled={!selectedStatus || updating}
                                            className="gap-2"
                                        >
                                            {updating ? (
                                                <>
                                                    <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                                                    Đang cập nhật...
                                                </>
                                            ) : (
                                                <>
                                                    <CheckCircle className="w-4 h-4" />
                                                    Cập nhật trạng thái
                                                </>
                                            )}
                                        </Button>
                                    </div>
                                </div>
                            )}

                            {/* No transitions available */}
                            {order.availableStatusTransitions.length === 0 && (
                                <div className="border-t border-border pt-6 mt-6">
                                    <div className="bg-secondary/30 rounded-lg p-4 text-center">
                                        <p className="text-muted-foreground text-sm">
                                            Đơn hàng đã ở trạng thái cuối cùng, không thể cập nhật thêm.
                                        </p>
                                    </div>
                                </div>
                            )}
                        </div>
                    )}
                </div>

                {/* Footer */}
                <div className="sticky bottom-0 bg-card border-t border-border px-6 py-4 flex justify-end gap-3">
                    <Button variant="outline" onClick={onClose}>
                        Đóng
                    </Button>
                </div>
            </div>
        </div>
    );
}
