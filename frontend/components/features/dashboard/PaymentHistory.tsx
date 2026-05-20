/**
 * PaymentHistory component - Display user's payment history
 */

'use client';

import { useState, useEffect, useCallback } from 'react';
import { CreditCard, CheckCircle, XCircle, Clock, AlertCircle, RefreshCw } from 'lucide-react';
import { paymentAPI } from '@/lib/api';
import { formatPrice } from '@/lib/utils';
import { cn } from '@/lib/utils';
import type { PaymentHistoryResponse, PaymentResponse, PaymentStatus } from '@/types/payment';

const STATUS_CONFIG: Record<PaymentStatus, { label: string; class: string; icon: typeof CheckCircle }> = {
    SUCCESS: { label: "Thành công", class: "bg-green-100 text-green-800", icon: CheckCircle },
    PENDING: { label: "Đang xử lý", class: "bg-yellow-100 text-yellow-800", icon: Clock },
    FAILED: { label: "Thất bại", class: "bg-red-100 text-red-800", icon: XCircle },
    CANCELLED: { label: "Đã hủy", class: "bg-gray-100 text-gray-800", icon: AlertCircle },
};

const PAYMENT_METHOD_LABELS: Record<string, string> = {
    COD: "Thanh toán khi nhận hàng",
    BANK_TRANSFER: "Chuyển khoản ngân hàng",
    VNPAY: "VNPay",
};

export function PaymentHistory() {
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [payments, setPayments] = useState<PaymentResponse[]>([]);
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);

    const fetchPaymentHistory = useCallback(async (page: number = 0) => {
        try {
            setLoading(true);
            setError(null);
            const response = await paymentAPI.getPaymentHistory(page, 10);

            if (response.success && response.data) {
                setPayments(response.data.payments || []);
                setCurrentPage(response.data.currentPage);
                setTotalPages(response.data.totalPages);
                setTotalElements(response.data.totalElements);
            } else {
                setError(response.message || "Không thể tải lịch sử thanh toán");
            }
        } catch (err) {
            console.error("Error fetching payment history:", err);
            setError("Đã xảy ra lỗi khi tải lịch sử thanh toán");
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchPaymentHistory(0);
    }, [fetchPaymentHistory]);

    // Loading state
    if (loading && payments.length === 0) {
        return (
            <div className="bg-card rounded-xl border border-border p-12">
                <div className="flex flex-col items-center justify-center">
                    <div className="w-12 h-12 border-4 border-primary border-t-transparent rounded-full animate-spin mb-4" />
                    <p className="text-muted-foreground">Đang tải lịch sử thanh toán...</p>
                </div>
            </div>
        );
    }

    // Error state
    if (error && payments.length === 0) {
        return (
            <div className="bg-card rounded-xl border border-border p-6">
                <div className="p-4 bg-red-50 border border-red-200 rounded-lg">
                    <p className="text-sm text-red-700">{error}</p>
                    <button
                        onClick={() => fetchPaymentHistory(0)}
                        className="mt-2 px-4 py-2 bg-red-100 hover:bg-red-200 text-red-800 rounded-lg text-sm font-medium transition-colors"
                    >
                        Thử lại
                    </button>
                </div>
            </div>
        );
    }

    // Empty state
    if (payments.length === 0) {
        return (
            <div className="bg-card rounded-xl border border-border p-12 text-center">
                <CreditCard className="w-16 h-16 mx-auto text-muted-foreground mb-4" />
                <h3 className="text-lg font-semibold text-foreground mb-2">Chưa có lịch sử thanh toán</h3>
                <p className="text-muted-foreground">Các giao dịch thanh toán của bạn sẽ xuất hiện tại đây.</p>
            </div>
        );
    }

    return (
        <div className="space-y-4">
            {/* Header */}
            <div className="flex items-center justify-between">
                <div className="flex items-center gap-3">
                    <div className="p-2 bg-primary/10 rounded-lg">
                        <CreditCard className="w-5 h-5 text-primary" />
                    </div>
                    <div>
                        <h3 className="font-semibold text-foreground">Lịch sử thanh toán</h3>
                        <p className="text-sm text-muted-foreground">{totalElements} giao dịch</p>
                    </div>
                </div>
                <button
                    onClick={() => fetchPaymentHistory(currentPage)}
                    disabled={loading}
                    className="p-2 hover:bg-secondary rounded-lg transition-colors disabled:opacity-50"
                    title="Làm mới"
                >
                    <RefreshCw className={cn("w-5 h-5 text-muted-foreground", loading && "animate-spin")} />
                </button>
            </div>

            {/* Payment List */}
            <div className="bg-card rounded-xl border border-border overflow-hidden">
                <div className="overflow-x-auto">
                    <table className="w-full text-sm">
                        <thead>
                            <tr className="border-b border-border bg-secondary/50">
                                <th className="text-left py-3 px-4 font-semibold text-muted-foreground">Mã đơn hàng</th>
                                <th className="text-left py-3 px-4 font-semibold text-muted-foreground">Phương thức</th>
                                <th className="text-left py-3 px-4 font-semibold text-muted-foreground">Số tiền</th>
                                <th className="text-left py-3 px-4 font-semibold text-muted-foreground">Trạng thái</th>
                                <th className="text-left py-3 px-4 font-semibold text-muted-foreground hidden md:table-cell">Thời gian</th>
                            </tr>
                        </thead>
                        <tbody>
                            {payments.map((payment) => {
                                const statusConfig = STATUS_CONFIG[payment.status] || STATUS_CONFIG.PENDING;
                                const StatusIcon = statusConfig.icon;
                                return (
                                    <tr key={payment.id} className="border-b border-border hover:bg-secondary/50">
                                        <td className="py-3 px-4 font-medium">#{payment.orderId}</td>
                                        <td className="py-3 px-4">
                                            <div className="flex items-center gap-2">
                                                <span>{PAYMENT_METHOD_LABELS[payment.paymentMethod] || payment.paymentMethod}</span>
                                                {payment.provider && (
                                                    <span className="text-xs text-muted-foreground">({payment.provider})</span>
                                                )}
                                            </div>
                                        </td>
                                        <td className="py-3 px-4 font-semibold text-primary">
                                            {formatPrice(payment.amount)}
                                        </td>
                                        <td className="py-3 px-4">
                                            <span className={cn("inline-flex items-center gap-1 px-2 py-1 rounded-full text-xs font-semibold", statusConfig.class)}>
                                                <StatusIcon className="w-3 h-3" />
                                                {statusConfig.label}
                                            </span>
                                        </td>
                                        <td className="py-3 px-4 text-muted-foreground hidden md:table-cell">
                                            {new Date(payment.createdAt).toLocaleString('vi-VN')}
                                        </td>
                                    </tr>
                                );
                            })}
                        </tbody>
                    </table>
                </div>

                {/* Pagination */}
                {totalPages > 1 && (
                    <div className="flex items-center justify-between px-4 py-3 border-t border-border bg-secondary/30">
                        <p className="text-sm text-muted-foreground">
                            Trang {currentPage + 1} / {totalPages}
                        </p>
                        <div className="flex gap-2">
                            <button
                                onClick={() => fetchPaymentHistory(currentPage - 1)}
                                disabled={currentPage === 0 || loading}
                                className="px-3 py-1 text-sm font-medium rounded-lg border border-border hover:bg-secondary disabled:opacity-50 disabled:cursor-not-allowed"
                            >
                                Trước
                            </button>
                            <button
                                onClick={() => fetchPaymentHistory(currentPage + 1)}
                                disabled={currentPage >= totalPages - 1 || loading}
                                className="px-3 py-1 text-sm font-medium rounded-lg border border-border hover:bg-secondary disabled:opacity-50 disabled:cursor-not-allowed"
                            >
                                Sau
                            </button>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
}
