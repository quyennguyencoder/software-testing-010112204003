/**
 * PaymentMethodSelector component - Select payment method
 */

'use client';

import { Wallet, CreditCard, Building2, Check } from 'lucide-react';
import { cn } from '@/lib/utils';
import type { PaymentMethod } from '@/types';

interface PaymentMethodOption {
  value: PaymentMethod;
  label: string;
  description: string;
  icon: React.ElementType;
}

const PAYMENT_METHODS: PaymentMethodOption[] = [
  {
    value: 'COD',
    label: 'Thanh toán khi nhận hàng',
    description: 'Thanh toán bằng tiền mặt khi nhận hàng',
    icon: Wallet,
  },
  {
    value: 'VNPAY',
    label: 'Ví điện tử VNPay',
    description: 'Thanh toán qua cổng VNPay',
    icon: CreditCard,
  },
  {
    value: 'BANK_TRANSFER',
    label: 'Chuyển khoản ngân hàng',
    description: 'Chuyển khoản trực tiếp qua ngân hàng',
    icon: Building2,
  },
];

interface PaymentMethodSelectorProps {
  value: PaymentMethod;
  onChange: (method: PaymentMethod) => void;
  disabled?: boolean;
}

export function PaymentMethodSelector({
  value,
  onChange,
  disabled = false,
}: PaymentMethodSelectorProps) {
  return (
    <div className="space-y-3">
      <h3 className="font-semibold text-foreground text-sm md:text-base">
        Phương thức thanh toán
      </h3>
      <div className="grid gap-3">
        {PAYMENT_METHODS.map((method) => {
          const Icon = method.icon;
          const isSelected = value === method.value;

          return (
            <button
              key={method.value}
              type="button"
              onClick={() => !disabled && onChange(method.value)}
              disabled={disabled}
              className={cn(
                'relative w-full rounded-xl border-2 p-4 text-left transition-all',
                'hover:border-primary/50 hover:shadow-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2',
                isSelected
                  ? 'border-primary bg-primary/5 shadow-sm'
                  : 'border-border bg-card',
                disabled && 'cursor-not-allowed opacity-50'
              )}
            >
              <div className="flex items-start gap-3">
                <div
                  className={cn(
                    'flex h-10 w-10 shrink-0 items-center justify-center rounded-lg transition-colors',
                    isSelected ? 'bg-primary text-primary-foreground' : 'bg-secondary text-muted-foreground'
                  )}
                >
                  <Icon className="h-5 w-5" />
                </div>
                <div className="flex-1 min-w-0">
                  <div className="flex items-center justify-between gap-2">
                    <h4 className="font-semibold text-foreground text-sm md:text-base">
                      {method.label}
                    </h4>
                    {isSelected && (
                      <div className="flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-primary">
                        <Check className="h-3 w-3 text-primary-foreground" />
                      </div>
                    )}
                  </div>
                  <p className="mt-1 text-xs md:text-sm text-muted-foreground">
                    {method.description}
                  </p>
                </div>
              </div>
            </button>
          );
        })}
      </div>
    </div>
  );
}
