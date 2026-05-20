import type { CartItem } from '@/types';

/**
 * Map backend cart item response to frontend CartItem type
 * Handles discount, appliedPrice, and various field name variations from backend
 */
export function mapBackendCartItem(item: unknown): CartItem {
  const obj = (item && typeof item === 'object') ? item as Record<string, unknown> : {};
  const productObj = (typeof obj.product === 'object' && obj.product) ? obj.product as Record<string, unknown> : {};
  
  // Extract price from various possible fields
  // Backend returns unitPrice as BigDecimal (serialized as number in JSON)
  const rawPrice = typeof obj.price === 'number' 
    ? obj.price 
    : (typeof obj.unitPrice === 'number' 
      ? obj.unitPrice 
      : (typeof obj.unitPrice === 'string'
        ? parseFloat(obj.unitPrice) || 0
        : (typeof productObj.salePrice === 'number' 
          ? productObj.salePrice 
          : (typeof productObj.price === 'number' 
            ? productObj.price 
            : 0))));
  
  const productOriginal = typeof productObj.originalPrice === 'number' 
    ? productObj.originalPrice 
    : (typeof productObj.price === 'number' 
      ? productObj.price 
      : 0);

  // Determine discount percent: prefer explicit fields from backend
  let discountPercent: number | undefined = undefined;
  if (typeof obj.discountPercent === 'number') {
    discountPercent = obj.discountPercent as number;
  } else if (typeof obj.discount === 'number') {
    discountPercent = obj.discount as number;
  } else if (typeof productObj.discountPercent === 'number') {
    discountPercent = productObj.discountPercent as number;
  }

  // If no explicit discount percent but we have original vs raw price, infer percent
  if (discountPercent === undefined && productOriginal > 0 && rawPrice < productOriginal) {
    discountPercent = Math.round((1 - rawPrice / productOriginal) * 100);
  }

  // Compute appliedPrice: if explicit discountPercent provided, apply it to a sensible base price
  let appliedPrice = rawPrice;
  if (typeof discountPercent === 'number' && discountPercent > 0) {
    const baseForDiscount = typeof productObj.price === 'number' && productObj.price > 0 
      ? Number(productObj.price) 
      : rawPrice;
    appliedPrice = Math.round(baseForDiscount * (100 - discountPercent) / 100);
  }

  return {
    id: Number(obj.id ?? 0),
    productId: Number(obj.productId ?? 0),
    productName: typeof obj.productName === 'string' 
      ? obj.productName 
      : (typeof productObj.name === 'string' 
        ? String(productObj.name) 
        : 'Unknown Product'),
    productImage: typeof obj.productImage === 'string' && obj.productImage.trim() !== ''
      ? obj.productImage 
      : (typeof obj.productThumbnailUrl === 'string' && obj.productThumbnailUrl.trim() !== ''
        ? obj.productThumbnailUrl 
        : (typeof productObj.thumbnailUrl === 'string' && String(productObj.thumbnailUrl).trim() !== ''
          ? String(productObj.thumbnailUrl) 
          : '')),
    price: rawPrice,
    discountPercent: discountPercent,
    appliedPrice: appliedPrice,
    quantity: Number(obj.quantity ?? 0),
    color: typeof obj.color === 'string' ? obj.color : undefined,
    storage: typeof obj.storage === 'string' ? obj.storage : undefined,
  };
}

/**
 * Map array of backend cart items
 */
export function mapBackendCartItems(items: unknown[]): CartItem[] {
  if (!Array.isArray(items)) return [];
  return items.map(mapBackendCartItem);
}

/**
 * Calculate item subtotal considering discount
 */
export function getItemSubtotal(item: CartItem): number {
  const effectivePrice = item.appliedPrice ?? item.price;
  return effectivePrice * item.quantity;
}

/**
 * Calculate cart totals
 */
export function calculateCartTotals(items: CartItem[]) {
  const totalItems = items.reduce((sum, item) => sum + item.quantity, 0);
  const totalPrice = items.reduce((sum, item) => sum + getItemSubtotal(item), 0);
  
  return { totalItems, totalPrice };
}
