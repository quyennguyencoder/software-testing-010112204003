package com.utephonehub.backend.enums;

/**
 * Payment type enumeration
 * Maps to payment_method in orders table
 */
public enum EPaymentType {
    COD,      // Cash on Delivery
    VNPAY,    // VNPay e-wallet
    MOMO      // MoMo e-wallet
}
