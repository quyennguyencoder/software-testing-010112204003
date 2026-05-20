/**
 * Promotion (Voucher) related types
 * Synced with backend DTO responses
 */

export interface PromotionTarget {
  id: string;
  type: "CATEGORY" | "BRAND" | "PRODUCT";
  applicableObjectId: string;
}

export interface PromotionResponse {
  id: string;
  title: string;
  description: string;
  effectiveDate: string;
  expirationDate: string;
  percentDiscount: number;
  minValueToBeApplied: number | null;
  status: "ACTIVE" | "INACTIVE" | "EXPIRED";
  templateId: string;
  templateCode: string;
  templateType: "DISCOUNT_PERCENTAGE" | "DISCOUNT_FIXED" | "FREE_SHIPPING";
  targets: PromotionTarget[];
}

export interface CreatePromotionRequest {
  title: string;
  description: string;
  effectiveDate: string;
  expirationDate: string;
  percentDiscount: number;
  minValueToBeApplied: number | null;
  status: "ACTIVE" | "INACTIVE";
  templateId: string;
  targets: {
    type: "CATEGORY" | "BRAND" | "PRODUCT";
    applicableObjectId: string;
  }[];
}

export interface UpdatePromotionRequest extends CreatePromotionRequest {}

export interface AvailablePromotionParams {
  orderTotal: number;
}

export interface CalculateDiscountParams {
  promotionId: string;
  orderTotal: number;
}
