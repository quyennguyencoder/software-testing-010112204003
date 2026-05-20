/**
 * PromotionTemplate types - matching backend DTO
 */

export interface PromotionTemplateResponse {
  id: string;
  code: string;
  type: "DISCOUNT" | "FREESHIP" | "VOUCHER";
  createdAt: string;
}

export interface CreateTemplateRequest {
  code: string;
  type: "DISCOUNT" | "FREESHIP" | "VOUCHER";
}

export interface UpdateTemplateRequest extends CreateTemplateRequest {}
