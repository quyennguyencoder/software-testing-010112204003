/**
 * Central export for all TypeScript types
 * Import types like: import { User, LoginRequest } from '@/types'
 */

// API types
export type { ApiResponse, ApiError } from "./api";

// User types
export type {
  User,
  UpdateProfileRequest,
  ChangePasswordRequest,
  UsersPageResponse,
  CreateUserRequest,
  UserFilters,
} from "./user";

// Auth types
export type {
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  ForgotPasswordRequest,
  VerifyOtpRequest,
  RefreshTokenRequest,
} from "./auth";

// Cart types
export type { CartItem, CartState } from "./cart";

// Wishlist types
export type { WishlistItem, WishlistState } from "./wishlist";

// Category types
export type {
  Category,
  CategoryResponse,
  CreateCategoryRequest,
  UpdateCategoryRequest,
} from "./category";

// Brand types
export type {
  Brand,
  BrandResponse,
  CreateBrandRequest,
  UpdateBrandRequest,
} from "./brand";

// Product types
export type {
  Product,
  ProductResponse,
  ProductMetadata,
  CreateProductRequest,
  UpdateProductRequest,
} from "./product";

// Order types
export type {
  Order,
  OrderResponse,
  OrderStatus,
  OrderItem,
  OrderItemRequest,
  CreateOrderRequest,
  CreateOrderResponse,
  PaymentMethod,
  RecentOrderResponse,
  AdminOrderListResponse,
  AdminOrderDetailResponse,
  AdminOrderItemDto,
} from "./order";

// Dashboard types
export type {
  DashboardOverview,
  DashboardOverviewResponse,
  DashboardStats,
  RevenueChartData,
  OrderStatusChartData,
  UserRegistrationChartData,
  TopProduct,
  TopProductResponse,
  RecentOrder,
  LowStockProduct,
  DashboardPeriod,
  RegistrationPeriod,
} from "./dashboard";

// Promotion types
export type {
  PromotionResponse,
  PromotionTarget,
  CreatePromotionRequest,
  UpdatePromotionRequest,
  AvailablePromotionParams,
  CalculateDiscountParams,
} from "./promotion";
export type {
  PromotionTemplateResponse,
  CreateTemplateRequest,
  UpdateTemplateRequest,
} from "./template";

// Payment types
export type {
  PaymentStatus,
  PaymentResponse,
  VNPayPaymentResponse,
  CreatePaymentRequest,
  PaymentHistoryResponse,
  VNPayCallbackParams,
  PaymentMethodOption,
} from "./payment";
