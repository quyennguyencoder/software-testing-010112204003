// Promotions extracted from backend/init.sql (seed data)
import type { Promotion } from '@/types/api-cart';

const initPromotions: Promotion[] = [
  {
    id: 'promo-001',
    title: 'Giảm 10% cho khách hàng mới',
    description: 'Áp dụng cho đơn hàng đầu tiên',
    percent_discount: 10.0,
    min_value_to_be_applied: 5000000.0,
    status: 'ACTIVE',
    template_id: 'template-001',
  },
  {
    id: 'promo-002',
    title: 'Miễn phí vận chuyển',
    description: 'Miễn phí ship cho đơn từ 500K',
    min_value_to_be_applied: 500000.0,
    status: 'ACTIVE',
    template_id: 'template-002',
  },
  {
    id: 'promo-003',
    title: 'Voucher 500K',
    description: 'Giảm 500K cho đơn từ 10 triệu',
    min_value_to_be_applied: 10000000.0,
    // include a helper amount for fixed voucher display
    fixed_amount: 500000,
    status: 'ACTIVE',
    template_id: 'template-001',
  },
];

export default initPromotions;
