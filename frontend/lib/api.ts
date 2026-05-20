// API Configuration and utility functions
import type {
  ApiResponse,
  User,
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  ForgotPasswordRequest,
  VerifyOtpRequest,
  Product,
  BrandResponse,
  CreateBrandRequest,
  UpdateBrandRequest,
  CategoryResponse,
  CreateCategoryRequest,
  UpdateCategoryRequest,
  CreateProductRequest,
  ProductResponse,
  Order,
  OrderResponse,
  RecentOrderResponse,
  DashboardOverviewResponse,
  TopProductResponse,
  PromotionResponse,
  CreatePromotionRequest,
  UpdatePromotionRequest,
  AvailablePromotionParams,
  CalculateDiscountParams,
  PromotionTemplateResponse,
  CreateTemplateRequest,
  UpdateTemplateRequest,
  CreateOrderRequest,
  CreateOrderResponse,
  PaymentMethod,
  PaymentResponse,
  VNPayPaymentResponse,
  CreatePaymentRequest,
  PaymentHistoryResponse,
  DashboardOverview,
  RevenueChartData,
  OrderStatusChartData,
  UserRegistrationChartData,
  TopProduct,
  RecentOrder,
  LowStockProduct,
  DashboardPeriod,
  RegistrationPeriod,
} from "@/types";

// Cart & Promotion API response types
import type {
  CartResponseData,
  CartItemResponse,
  Promotion,
} from "@/types/api-cart";

// Ensure API_BASE_URL is always absolute
const getApiBaseUrl = (): string => {
  const envUrl = process.env.NEXT_PUBLIC_API_URL;
  if (envUrl) return envUrl;

  // Default to backend server URL
  return "http://localhost:8081/api/v1";
};

const API_BASE_URL = getApiBaseUrl();

// Helper function to get auth token from localStorage
export const getAuthToken = (): string | null => {
  if (typeof window !== "undefined") {
    return localStorage.getItem("accessToken");
  }
  return null;
};

// Helper function to set auth tokens
export const setAuthTokens = (
  accessToken: string,
  refreshToken: string
): void => {
  if (typeof window !== "undefined") {
    localStorage.setItem("accessToken", accessToken);
    localStorage.setItem("refreshToken", refreshToken);
  }
};

// Helper function to clear auth tokens
export const clearAuthTokens = (): void => {
  if (typeof window !== "undefined") {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    localStorage.removeItem("user");
  }
};

// Helper function to get stored user
export const getStoredUser = (): User | null => {
  if (typeof window !== "undefined") {
    const user = localStorage.getItem("user");
    return user ? JSON.parse(user) : null;
  }
  return null;
};

// Helper function to set stored user
export const setStoredUser = (user: User): void => {
  if (typeof window !== "undefined") {
    localStorage.setItem("user", JSON.stringify(user));
  }
};

// Generic fetch wrapper with error handling
async function fetchAPI<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<ApiResponse<T>> {
  // Disallow absolute URLs to avoid malformed URLs like
  // `${API_BASE_URL}https://external.com/endpoint`
  if (/^https?:\/\//i.test(endpoint) || endpoint.startsWith("//")) {
    throw new Error(
      `fetchAPI endpoint must be a relative path starting with '/', received: '${endpoint}'`
    );
  }

  // Ensure endpoint starts with / for proper URL construction
  const normalizedEndpoint = endpoint.startsWith("/")
    ? endpoint
    : `/${endpoint}`;
  const url = `${API_BASE_URL}${normalizedEndpoint}`;
  const token = getAuthToken();

  if (process.env.NODE_ENV === 'development') {
    console.log(`[fetchAPI] Token check:`, {
      hasToken: !!token,
      tokenLength: token?.length,
      tokenPreview: token ? `${token.substring(0, 20)}...` : null,
    });
  }

  const headers = new Headers(options.headers);

  if (!headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }

  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
    if (process.env.NODE_ENV === 'development') {
      console.log(`[fetchAPI] Authorization header set`);
    }
  } else {
    if (process.env.NODE_ENV === 'development') {
      console.warn(`[fetchAPI] No token found, request will be unauthenticated`);
    }
  }

  try {
    // Log request for debugging
    if (process.env.NODE_ENV === 'development') {
      console.log(`[fetchAPI] ${options.method || 'GET'} ${url}`, {
        hasToken: !!token,
        body: options.body ? JSON.parse(options.body as string) : undefined,
      });
    }

    const response = await fetch(url, {
      ...options,
      headers,
    });

    // Log response status
    if (process.env.NODE_ENV === 'development') {
      console.log(`[fetchAPI] Response ${response.status} ${response.statusText} for ${options.method || 'GET'} ${url}`);
    }

    // Try to parse JSON, but handle cases where response might not be JSON
    let data: any;
    const contentType = response.headers.get("content-type");
    const isJson = contentType?.includes("application/json");

    if (isJson) {
      try {
        data = await response.json();
      } catch (parseError) {
        // If JSON parsing fails, use empty object
        data = {};
      }
    } else {
      // If not JSON, try to get text
      try {
        const text = await response.text();
        data = text ? { message: text } : {};
      } catch {
        data = {};
      }
    }

    if (!response.ok) {
      const errorMessage =
        data?.message ||
        data?.error ||
        (typeof data === "string" ? data : null) ||
        `API request failed with status ${response.status} ${response.statusText}`;

      // Log error for debugging
      console.error(`[fetchAPI] API Error [${response.status}]:`, errorMessage, data);

      // Create error object with response data
      const error = new Error(errorMessage) as any;
      error.status = response.status;
      error.data = data;
      throw error;
    }

    // Log successful response
    if (process.env.NODE_ENV === 'development') {
      console.log(`[fetchAPI] Success for ${options.method || 'GET'} ${url}:`, data);
      // Check if response has success field
      if (data && typeof data === 'object' && 'success' in data) {
        console.log(`[fetchAPI] Response success field:`, data.success);
      } else {
        console.warn(`[fetchAPI] Response does not have success field`);
      }
    }

    // Ensure response has success field if it's an ApiResponse
    if (data && typeof data === 'object' && !('success' in data)) {
      // If response doesn't have success field, assume it's successful (status 200)
      data.success = true;
    }

    return data;
  } catch (error) {
    console.error("API Error:", error);
    throw error;
  }
}

// Auth API endpoints
export const authAPI = {
  login: async (
    credentials: LoginRequest
  ): Promise<ApiResponse<LoginResponse>> => {
    return fetchAPI<LoginResponse>("/auth/login", {
      method: "POST",
      body: JSON.stringify(credentials),
    });
  },

  register: async (data: RegisterRequest): Promise<ApiResponse<User>> => {
    return fetchAPI<User>("/auth/register", {
      method: "POST",
      body: JSON.stringify(data),
    });
  },

  forgotPassword: async (
    data: ForgotPasswordRequest
  ): Promise<ApiResponse<null>> => {
    return fetchAPI<null>("/auth/forgot-password/request", {
      method: "POST",
      body: JSON.stringify(data),
    });
  },

  verifyOtp: async (data: VerifyOtpRequest): Promise<ApiResponse<null>> => {
    return fetchAPI<null>("/auth/forgot-password/verify", {
      method: "POST",
      body: JSON.stringify(data),
    });
  },

  logout: async (): Promise<ApiResponse<null>> => {
    return fetchAPI<null>("/auth/logout", {
      method: "POST",
    });
  },

  refresh: async (
    refreshToken: string
  ): Promise<ApiResponse<LoginResponse>> => {
    return fetchAPI<LoginResponse>("/auth/refresh", {
      method: "POST",
      body: JSON.stringify({ refreshToken }),
    });
  },
};

// User API endpoints
export const userAPI = {
  getMe: async (): Promise<ApiResponse<User>> => {
    return fetchAPI<User>("/user/me", {
      method: "GET",
    });
  },

  updateProfile: async (data: Partial<User>): Promise<ApiResponse<User>> => {
    return fetchAPI<User>("/user/profile", {
      method: "POST",
      body: JSON.stringify(data),
    });
  },

  changePassword: async (data: {
    currentPassword: string;
    newPassword: string;
    confirmPassword: string;
  }): Promise<ApiResponse<null>> => {
    return fetchAPI<null>("/user/password", {
      method: "POST",
      body: JSON.stringify(data),
    });
  },
};

// Address API endpoints
export const addressAPI = {
  /**
   * GET /api/v1/user/addresses
   * Lấy danh sách địa chỉ của user hiện tại
   */
  getAll: async (): Promise<ApiResponse<import("@/types/address").AddressResponse[]>> => {
    return fetchAPI<import("@/types/address").AddressResponse[]>("/user/addresses", {
      method: "GET",
    });
  },

  /**
   * POST /api/v1/user/addresses
   * Thêm địa chỉ mới
   */
  create: async (
    data: import("@/types/address").AddressRequest
  ): Promise<ApiResponse<import("@/types/address").AddressResponse>> => {
    return fetchAPI<import("@/types/address").AddressResponse>("/user/addresses", {
      method: "POST",
      body: JSON.stringify(data),
    });
  },

  /**
   * PUT /api/v1/user/addresses/{id}
   * Cập nhật địa chỉ
   */
  update: async (
    id: number,
    data: import("@/types/address").AddressRequest
  ): Promise<ApiResponse<import("@/types/address").AddressResponse>> => {
    return fetchAPI<import("@/types/address").AddressResponse>(`/user/addresses/${id}`, {
      method: "PUT",
      body: JSON.stringify(data),
    });
  },

  /**
   * DELETE /api/v1/user/addresses/{id}
   * Xóa địa chỉ
   */
  delete: async (id: number): Promise<ApiResponse<null>> => {
    return fetchAPI<null>(`/user/addresses/${id}`, {
      method: "DELETE",
    });
  },

  /**
   * PUT /api/v1/user/addresses/{id}/set-default
   * Đặt địa chỉ làm mặc định
   */
  setDefault: async (
    id: number
  ): Promise<ApiResponse<import("@/types/address").AddressResponse>> => {
    return fetchAPI<import("@/types/address").AddressResponse>(
      `/user/addresses/${id}/set-default`,
      {
        method: "PUT",
      }
    );
  },
};

// Health check
export const healthCheck = async (): Promise<ApiResponse<any>> => {
  return fetchAPI<any>("/health", {
    method: "GET",
  });
};

// Product API endpoints
// Note: Public product endpoints don't exist yet, so using mock data in components
// Only keeping admin endpoints that exist
export const productAPI = {
  // Get product by ID (Admin)
  // GET /api/v1/admin/products/{id}
  getById: async (id: number): Promise<ApiResponse<Product>> => {
    return fetchAPI<Product>(`/admin/products/${id}`, {
      method: "GET",
    });
  },

  // Update product (Admin)
  // PUT /api/v1/admin/products/{id}
  update: async (
    id: number,
    data: Partial<Product>
  ): Promise<ApiResponse<Product>> => {
    return fetchAPI<Product>(`/admin/products/${id}`, {
      method: "PUT",
      body: JSON.stringify(data),
    });
  },

  // Create product (Admin)
  // POST /api/v1/admin/products
  create: async (data: CreateProductRequest): Promise<ApiResponse<Product>> => {
    return fetchAPI<Product>("/admin/products", {
      method: "POST",
      body: JSON.stringify(data),
    });
  },

  // Get all products including deleted (Admin only)
  // GET /api/v1/admin/products?page={page}&size={size}
  getAllProducts: async (params?: {
    page?: number;
    size?: number;
    keyword?: string;
    categoryId?: number;
    brandId?: number;
  }): Promise<ApiResponse<any>> => {
    const queryParams = new URLSearchParams();
    if (params?.page !== undefined)
      queryParams.append("page", String(params.page));
    if (params?.size !== undefined)
      queryParams.append("size", String(params.size));
    if (params?.keyword) queryParams.append("keyword", params.keyword);
    if (params?.categoryId !== undefined)
      queryParams.append("categoryId", String(params.categoryId));
    if (params?.brandId !== undefined)
      queryParams.append("brandId", String(params.brandId));

    return fetchAPI<any>(
      `/admin/products${queryParams.toString() ? `?${queryParams.toString()}` : ""
      }`,
      {
        method: "GET",
      }
    );
  },

  // Get low stock products (for admin dashboard)
  // This endpoint exists: GET /api/v1/admin/dashboard/low-stock-products
  getLowStockProducts: async (
    threshold: number = 20
  ): Promise<ApiResponse<any[]>> => {
    return fetchAPI<any[]>(
      `/admin/dashboard/low-stock-products?threshold=${threshold}`,
      {
        method: "GET",
      }
    );
  },

  // Manage product images (replace all) (Admin)
  // POST /api/v1/admin/products/{id}/images
  uploadImage: async (
    productId: number,
    requestBody: {
      images: Array<{
        imageUrl: string;
        altText?: string;
        imageOrder: number;
        isPrimary: boolean;
      }>;
    }
  ): Promise<ApiResponse<null>> => {
    return fetchAPI<null>(`/admin/products/${productId}/images`, {
      method: "POST",
      body: JSON.stringify(requestBody),
    });
  },

  // Delete a specific product image (Admin)
  // DELETE /api/v1/admin/products/{id}/images/{imageId}
  deleteImage: async (
    productId: number,
    imageId: number
  ): Promise<ApiResponse<null>> => {
    return fetchAPI<null>(`/admin/products/${productId}/images/${imageId}`, {
      method: "DELETE",
    });
  },

  // Get all images for a product (Admin)
  // GET /api/v1/admin/products/{id}/images
  getImages: async (
    productId: number
  ): Promise<ApiResponse<any[]>> => {
    return fetchAPI<any[]>(`/admin/products/${productId}/images`, {
      method: "GET",
    });
  },
};

// Category API endpoints
export const categoryAPI = {
  // Get all categories (or by parentId)
  // GET /api/v1/categories?parentId={parentId}
  getCategories: async (parentId?: string): Promise<ApiResponse<any[]>> => {
    const queryParams = parentId ? `?parentId=${parentId}` : "";
    return fetchAPI<any[]>(`/categories${queryParams}`, {
      method: "GET",
    });
  },

  // Get all root categories (parentId = null)
  getRootCategories: async (): Promise<ApiResponse<any[]>> => {
    return fetchAPI<any[]>("/categories", {
      method: "GET",
    });
  },
};

// Brand API endpoints
export const brandAPI = {
  // Get all brands
  // GET /api/v1/brands
  getAll: async (): Promise<ApiResponse<any[]>> => {
    return fetchAPI<any[]>("/brands", {
      method: "GET",
    });
  },
};

// Order API endpoints
export const orderAPI = {
  // Create new order
  createOrder: async (
    orderData: CreateOrderRequest
  ): Promise<ApiResponse<CreateOrderResponse>> => {
    return fetchAPI<CreateOrderResponse>("/orders", {
      method: "POST",
      body: JSON.stringify(orderData),
    });
  },

  // Get order by ID
  getById: async (orderId: number): Promise<ApiResponse<OrderResponse>> => {
    return fetchAPI<OrderResponse>(`/orders/${orderId}`, {
      method: "GET",
    });
  },
  // Get current user's orders (simple list, no pagination)
  // GET /api/v1/orders/my-orders
  getMyOrders: async (): Promise<ApiResponse<OrderResponse[]>> => {
    return fetchAPI<OrderResponse[]>("/orders/my-orders", {
      method: "GET",
    });
  },

  // Get recent orders (for admin dashboard)
  // This endpoint exists: GET /api/v1/admin/dashboard/recent-orders?limit={limit}
  getRecentOrders: async (
    limit: number = 10
  ): Promise<ApiResponse<RecentOrderResponse[]>> => {
    return fetchAPI<RecentOrderResponse[]>(
      `/admin/dashboard/recent-orders?limit=${limit}`,
      {
        method: "GET",
      }
    );
  },
};

// Admin API endpoints
export const adminAPI = {
  // Users
  getAllUsers: async (params?: {
    page?: number;
    size?: number;
    role?: string;
    status?: string;
    search?: string;
  }): Promise<ApiResponse<any>> => {
    const queryParams = new URLSearchParams();
    if (params?.page !== undefined)
      queryParams.append("page", params.page.toString());
    if (params?.size !== undefined)
      queryParams.append("size", params.size.toString());
    if (params?.role) queryParams.append("role", params.role);
    if (params?.status) queryParams.append("status", params.status);
    if (params?.search) queryParams.append("search", params.search);

    const query = queryParams.toString();
    return fetchAPI<any>(`/admin/users${query ? `?${query}` : ""}`, {
      method: "GET",
    });
  },

  // Orders (Admin management)
  // GET /api/v1/admin/orders
  getOrders: async (params?: {
    search?: string;
    status?: string;
    paymentMethod?: string;
    customerId?: number;
    customerEmail?: string;
    fromDate?: string;
    toDate?: string;
    minAmount?: string;
    maxAmount?: string;
    page?: number;
    size?: number;
    sortBy?: string;
    sortDirection?: "asc" | "desc";
  }): Promise<ApiResponse<{
    content: import("@/types").AdminOrderListResponse[];
    totalPages: number;
    totalElements: number;
    number: number;
    size: number;
  }>> => {
    const queryParams = new URLSearchParams();
    if (params?.search) queryParams.append("search", params.search);
    if (params?.status) queryParams.append("status", params.status);
    if (params?.paymentMethod)
      queryParams.append("paymentMethod", params.paymentMethod);
    if (params?.customerId !== undefined)
      queryParams.append("customerId", String(params.customerId));
    if (params?.customerEmail)
      queryParams.append("customerEmail", params.customerEmail);
    if (params?.fromDate) queryParams.append("fromDate", params.fromDate);
    if (params?.toDate) queryParams.append("toDate", params.toDate);
    if (params?.minAmount) queryParams.append("minAmount", params.minAmount);
    if (params?.maxAmount) queryParams.append("maxAmount", params.maxAmount);
    if (params?.page !== undefined)
      queryParams.append("page", String(params.page));
    if (params?.size !== undefined)
      queryParams.append("size", String(params.size));
    if (params?.sortBy) queryParams.append("sortBy", params.sortBy);
    if (params?.sortDirection)
      queryParams.append("sortDirection", params.sortDirection);

    const query = queryParams.toString();
    return fetchAPI<{
      content: import("@/types").AdminOrderListResponse[];
      totalPages: number;
      totalElements: number;
      number: number;
      size: number;
    }>(`/admin/orders${query ? `?${query}` : ""}`, {
      method: "GET",
    });
  },

  // Dashboard
  getDashboardOverview: async (): Promise<
    ApiResponse<DashboardOverviewResponse>
  > => {
    return fetchAPI<DashboardOverviewResponse>("/admin/dashboard/overview", {
      method: "GET",
    });
  },

  // Categories
  // Public list endpoint
  // GET /api/v1/categories?parentId=
  getAllCategories: async (
    parentId?: number | null
  ): Promise<ApiResponse<CategoryResponse[]>> => {
    const query =
      parentId === undefined || parentId === null
        ? ""
        : `?parentId=${encodeURIComponent(String(parentId))}`;

    return fetchAPI<CategoryResponse[]>(`/categories${query}`, {
      method: "GET",
    });
  },

  // Admin CRUD endpoints
  // POST /api/v1/admin/categories
  createCategory: async (
    data: CreateCategoryRequest
  ): Promise<ApiResponse<CategoryResponse>> => {
    return fetchAPI<CategoryResponse>("/admin/categories", {
      method: "POST",
      body: JSON.stringify(data),
    });
  },

  // PUT /api/v1/admin/categories/{id}
  updateCategory: async (
    id: number,
    data: UpdateCategoryRequest
  ): Promise<ApiResponse<CategoryResponse>> => {
    return fetchAPI<CategoryResponse>(`/admin/categories/${id}`, {
      method: "PUT",
      body: JSON.stringify(data),
    });
  },

  // DELETE /api/v1/admin/categories/{id}
  deleteCategory: async (id: number): Promise<ApiResponse<null>> => {
    return fetchAPI<null>(`/admin/categories/${id}`, {
      method: "DELETE",
    });
  },

  // Brands
  // Public list endpoint
  getAllBrands: async (): Promise<ApiResponse<BrandResponse[]>> => {
    return fetchAPI<BrandResponse[]>("/brands", {
      method: "GET",
    });
  },

  // Admin CRUD endpoints
  // POST /api/v1/admin/brands
  createBrand: async (
    data: CreateBrandRequest
  ): Promise<ApiResponse<BrandResponse>> => {
    return fetchAPI<BrandResponse>("/admin/brands", {
      method: "POST",
      body: JSON.stringify(data),
    });
  },

  // PUT /api/v1/admin/brands/{id}
  updateBrand: async (
    id: number,
    data: UpdateBrandRequest
  ): Promise<ApiResponse<BrandResponse>> => {
    return fetchAPI<BrandResponse>(`/admin/brands/${id}`, {
      method: "PUT",
      body: JSON.stringify(data),
    });
  },

  // DELETE /api/v1/admin/brands/{id}
  deleteBrand: async (id: number): Promise<ApiResponse<null>> => {
    return fetchAPI<null>(`/admin/brands/${id}`, {
      method: "DELETE",
    });
  },

  // Products (admin management)
  getAllProducts: async (params?: {
    keyword?: string;
    page?: number;
    size?: number;
    categoryId?: number;
    brandId?: number;
    minPrice?: number;
    maxPrice?: number;
    sortBy?: string;
    sortDirection?: "asc" | "desc";
  }): Promise<ApiResponse<any>> => {
    const queryParams = new URLSearchParams();
    if (params?.keyword) queryParams.append("keyword", params.keyword);
    if (params?.categoryId !== undefined)
      queryParams.append("categoryId", String(params.categoryId));
    if (params?.brandId !== undefined)
      queryParams.append("brandId", String(params.brandId));
    if (params?.minPrice !== undefined)
      queryParams.append("minPrice", String(params.minPrice));
    if (params?.maxPrice !== undefined)
      queryParams.append("maxPrice", String(params.maxPrice));
    if (params?.sortBy) queryParams.append("sortBy", params.sortBy);
    if (params?.sortDirection)
      queryParams.append("sortDirection", params.sortDirection);
    if (params?.page !== undefined)
      queryParams.append("page", String(params.page));
    if (params?.size !== undefined)
      queryParams.append("size", String(params.size));

    const query = queryParams.toString();
    return fetchAPI<any>(`/admin/products${query ? `?${query}` : ""}`, {
      method: "GET",
    });
  },

  // Products (admin)
  // DELETE /api/v1/admin/products/{id}
  deleteProduct: async (id: number): Promise<ApiResponse<null>> => {
    return fetchAPI<null>(`/admin/products/${id}`, {
      method: "DELETE",
    });
  },
};

// Promotion API
export const promotionAPI = {
  // Customer: Get ALL active promotions (for /promotions page)
  // GET /api/v1/promotions
  getAllActivePromotions: async (): Promise<
    ApiResponse<PromotionResponse[]>
  > => {
    return fetchAPI<PromotionResponse[]>("/promotions", {
      method: "GET",
    });
  },

  // Customer: Get available promotions based on order total
  getAvailablePromotions: async (
    orderTotal: number
  ): Promise<ApiResponse<PromotionResponse[]>> => {
    return fetchAPI<PromotionResponse[]>(
      `/promotions/available?orderTotal=${orderTotal}`,
      {
        method: "GET",
      }
    );
  },

  // Customer: Calculate discount for a specific promotion
  calculateDiscount: async (
    promotionId: string,
    orderTotal: number
  ): Promise<ApiResponse<number>> => {
    return fetchAPI<number>(
      `/promotions/calculate?promotionId=${promotionId}&orderTotal=${orderTotal}`,
      {
        method: "GET",
      }
    );
  },

  // Admin: Get all promotions
  getAllPromotions: async (): Promise<ApiResponse<PromotionResponse[]>> => {
    return fetchAPI<PromotionResponse[]>("/admin/promotions", {
      method: "GET",
    });
  },

  // Admin: Get promotion details
  getPromotionDetails: async (
    id: string
  ): Promise<ApiResponse<PromotionResponse>> => {
    return fetchAPI<PromotionResponse>(`/admin/promotions/${id}`, {
      method: "GET",
    });
  },

  // Admin: Create new promotion
  createPromotion: async (
    data: CreatePromotionRequest
  ): Promise<ApiResponse<PromotionResponse>> => {
    return fetchAPI<PromotionResponse>("/admin/promotions", {
      method: "POST",
      body: JSON.stringify(data),
    });
  },

  // Admin: Update promotion
  updatePromotion: async (
    id: string,
    data: UpdatePromotionRequest
  ): Promise<ApiResponse<PromotionResponse>> => {
    return fetchAPI<PromotionResponse>(`/admin/promotions/${id}`, {
      method: "PUT",
      body: JSON.stringify(data),
    });
  },

  // Admin: Disable promotion
  disablePromotion: async (id: string): Promise<ApiResponse<void>> => {
    return fetchAPI<void>(`/admin/promotions/${id}/disable`, {
      method: "PATCH",
    });
  },
};

// Promotion Template API (Admin only)
export const templateAPI = {
  // Get all templates
  getAllTemplates: async (): Promise<
    ApiResponse<PromotionTemplateResponse[]>
  > => {
    return fetchAPI<PromotionTemplateResponse[]>("/admin/promotion-templates", {
      method: "GET",
    });
  },

  // Get template by ID
  getTemplateById: async (
    id: string
  ): Promise<ApiResponse<PromotionTemplateResponse>> => {
    return fetchAPI<PromotionTemplateResponse>(
      `/admin/promotion-templates/${id}`,
      {
        method: "GET",
      }
    );
  },

  // Create template
  createTemplate: async (
    data: CreateTemplateRequest
  ): Promise<ApiResponse<PromotionTemplateResponse>> => {
    return fetchAPI<PromotionTemplateResponse>("/admin/promotion-templates", {
      method: "POST",
      body: JSON.stringify(data),
    });
  },

  // Update template
  updateTemplate: async (
    id: string,
    data: UpdateTemplateRequest
  ): Promise<ApiResponse<PromotionTemplateResponse>> => {
    return fetchAPI<PromotionTemplateResponse>(
      `/admin/promotion-templates/${id}`,
      {
        method: "PUT",
        body: JSON.stringify(data),
      }
    );
  },

  // Delete template
  deleteTemplate: async (id: string): Promise<ApiResponse<void>> => {
    return fetchAPI<void>(`/admin/promotion-templates/${id}`, {
      method: "DELETE",
    });
  },
};

// Cart API
export const cartAPI = {
  /**
   * GET /api/v1/cart/me
   */
  getCurrentCart: async (): Promise<ApiResponse<CartResponseData>> => {
    try {
      if (process.env.NODE_ENV === 'development') {
        console.log('[cartAPI] Fetching cart from /cart/me');
      }

      const response = await fetchAPI<CartResponseData>("/cart/me", { method: "GET" });

      if (process.env.NODE_ENV === 'development') {
        console.log('[cartAPI] Raw response:', response);
      }

      // Ensure response has expected structure
      if (response) {
        // Handle case where response.data might be the cart data directly
        if (!response.data && (response as any).items) {
          // Backend might return cart data directly in response
          response.data = {
            items: Array.isArray((response as any).items) ? (response as any).items : [],
          } as CartResponseData;
        }

        if (response.data) {
          // Normalize items array if missing
          if (!Array.isArray(response.data.items)) {
            if (process.env.NODE_ENV === 'development') {
              console.warn('[cartAPI] items is not an array:', response.data.items);
            }
            response.data.items = [];
          }
        } else {
          // No data field, create empty structure
          response.data = {
            items: [],
          } as CartResponseData;
        }
      }

      if (process.env.NODE_ENV === 'development') {
        console.log('[cartAPI] Normalized response:', response);
      }

      return response;
    } catch (error: any) {
      console.error("[cartAPI] Cart API Error:", error);

      if (process.env.NODE_ENV === 'development') {
        console.error("[cartAPI] Error details:", {
          message: error?.message,
          stack: error?.stack,
        });
      }

      // Return a valid response structure even on error
      return {
        success: false,
        status: 500,
        message: error?.message || "Không thể tải giỏ hàng",
        data: {
          items: [],
        } as CartResponseData,
      };
    }
  },

  /**
   * POST /api/v1/cart/items
   */
  addToCart: async (data: {
    productId: number;
    quantity: number;
    color?: string;
    storage?: string;
  }): Promise<ApiResponse<CartItemResponse>> => {
    if (process.env.NODE_ENV === 'development') {
      console.log('[cartAPI.addToCart] Calling API with data:', data);
    }

    try {
      const response = await fetchAPI<CartItemResponse>("/cart/items", {
        method: "POST",
        body: JSON.stringify(data),
      });

      if (process.env.NODE_ENV === 'development') {
        console.log('[cartAPI.addToCart] API response:', response);
      }

      return response;
    } catch (error) {
      console.error('[cartAPI.addToCart] Error:', error);
      throw error;
    }
  },

  /**
   * PUT /api/v1/cart/items/{itemId}
   */
  updateCartItem: async (
    itemId: number,
    data: number | { quantity?: number }
  ): Promise<ApiResponse<CartItemResponse>> => {
    const payload = typeof data === "number" ? { quantity: data } : data;
    return fetchAPI<CartItemResponse>(`/cart/items/${itemId}`, {
      method: "PUT",
      body: JSON.stringify(payload),
    });
  },

  /**
   * DELETE /api/v1/cart/items/{itemId}
   */
  removeCartItem: async (itemId: number): Promise<ApiResponse<null>> => {
    return fetchAPI<null>(`/cart/items/${itemId}`, {
      method: "DELETE",
    });
  },

  /**
   * Remove multiple cart items by ids.
   * Backend does not expose a bulk delete, so perform parallel deletes and aggregate.
   */
  removeCartItems: async (itemIds: number[]): Promise<ApiResponse<null[]>> => {
    try {
      const results = await Promise.all(
        itemIds.map((id) =>
          fetchAPI<null>(`/cart/items/${id}`, { method: "DELETE" })
        )
      );
      return {
        success: true,
        data: results.map((r) => r.data ?? null),
      } as ApiResponse<null[]>;
    } catch (error) {
      console.error("Failed to remove multiple cart items:", error);
      throw error;
    }
  },

  /**
   * DELETE /api/v1/cart/clear
   */
  clearCart: async (): Promise<ApiResponse<null>> => {
    return fetchAPI<null>("/cart/clear", { method: "DELETE" });
  },

  /**
   * POST /api/v1/cart/merge
   */
  mergeGuestCart: async (data: {
    guestCartItems?: { productId: number; quantity: number }[];
    guestCartId?: string;
  }): Promise<ApiResponse<CartResponseData>> => {
    return fetchAPI<CartResponseData>("/cart/merge", {
      method: "POST",
      body: JSON.stringify(data),
    });
  },
};

// Guest Cart API (Redis-backed)
export const guestCartAPI = {
  /**
   * POST /api/v1/guest-cart
   */
  createGuestCart: async (): Promise<ApiResponse<{ guestCartId: string }>> => {
    return fetchAPI<{ guestCartId: string }>("/guest-cart", { method: "POST" });
  },

  /**
   * PUT /api/v1/guest-cart/{guestCartId}
   */
  replaceGuestCart: async (
    guestCartId: string,
    data: { items: { productId: number; quantity: number }[] }
  ): Promise<ApiResponse<null>> => {
    return fetchAPI<null>(`/guest-cart/${encodeURIComponent(guestCartId)}`, {
      method: "PUT",
      body: JSON.stringify(data),
    });
  },

  /**
   * DELETE /api/v1/guest-cart/{guestCartId}
   */
  deleteGuestCart: async (guestCartId: string): Promise<ApiResponse<null>> => {
    return fetchAPI<null>(`/guest-cart/${encodeURIComponent(guestCartId)}`, {
      method: "DELETE",
    });
  },
};

// Payment API
export const paymentAPI = {
  // GET /api/v1/payments/history?page={page}&size={size}
  getPaymentHistory: async (
    page: number = 0,
    size: number = 10
  ): Promise<ApiResponse<PaymentHistoryResponse>> => {
    return fetchAPI<PaymentHistoryResponse>(
      `/payments/history?page=${page}&size=${size}`,
      {
        method: "GET",
      }
    );
  },

  // GET /api/v1/payments/vnpay/callback?{params}
  // Used by the frontend return page to verify/process VNPay result
  handleVNPayCallback: async (
    params: Record<string, string>
  ): Promise<ApiResponse<PaymentResponse>> => {
    const qs = new URLSearchParams(params).toString();
    return fetchAPI<PaymentResponse>(`/payments/vnpay/callback?${qs}`, {
      method: "GET",
    });
  },

  // POST /api/v1/payments/vnpay/create
  createVNPayPayment: async (
    data: CreatePaymentRequest
  ): Promise<ApiResponse<VNPayPaymentResponse>> => {
    return fetchAPI<VNPayPaymentResponse>("/payments/vnpay/create", {
      method: "POST",
      body: JSON.stringify(data),
    });
  },
};

// ==================== DASHBOARD API ====================
// Module M10.2 - View Dashboard
// Comprehensive Dashboard endpoints for admin analytics
export const dashboardAPI = {
  /**
   * GET /api/v1/admin/dashboard/overview
   * Lấy 4 chỉ số tổng quan: Tổng doanh thu, Tổng đơn hàng, Tổng sản phẩm, Tổng người dùng
   */
  getOverview: async (): Promise<ApiResponse<DashboardOverview>> => {
    return fetchAPI<DashboardOverview>("/admin/dashboard/overview", {
      method: "GET",
    });
  },

  /**
   * GET /api/v1/admin/dashboard/revenue-chart?period=THIRTY_DAYS
   * Lấy dữ liệu biểu đồ doanh thu theo khoảng thời gian
   * @param period - 'SEVEN_DAYS' | 'THIRTY_DAYS' | 'THREE_MONTHS'
   */
  getRevenueChart: async (
    period: DashboardPeriod = "THIRTY_DAYS"
  ): Promise<ApiResponse<RevenueChartData>> => {
    return fetchAPI<RevenueChartData>(
      `/admin/dashboard/revenue-chart?period=${period}`,
      {
        method: "GET",
      }
    );
  },

  /**
   * GET /api/v1/admin/dashboard/order-status-chart
   * Lấy dữ liệu biểu đồ tròn về trạng thái đơn hàng
   * Trả về labels, values, percentages, totalOrders
   */
  getOrderStatusChart: async (): Promise<ApiResponse<OrderStatusChartData>> => {
    return fetchAPI<OrderStatusChartData>(
      "/admin/dashboard/order-status-chart",
      {
        method: "GET",
      }
    );
  },

  /**
   * GET /api/v1/admin/dashboard/user-registration-chart?period=MONTHLY
   * Lấy dữ liệu biểu đồ cột về người dùng đăng ký mới
   * @param period - 'WEEKLY' | 'MONTHLY'
   */
  getUserRegistrationChart: async (
    period: RegistrationPeriod = "WEEKLY"
  ): Promise<ApiResponse<UserRegistrationChartData>> => {
    return fetchAPI<UserRegistrationChartData>(
      `/admin/dashboard/user-registration-chart?period=${period}`,
      {
        method: "GET",
      }
    );
  },

  /**
   * GET /api/v1/admin/dashboard/top-products?limit=5
   * Lấy danh sách Top sản phẩm bán chạy nhất
   * @param limit - Số lượng sản phẩm cần lấy (mặc định: 5)
   */
  getTopProducts: async (
    limit: number = 5
  ): Promise<ApiResponse<TopProduct[]>> => {
    return fetchAPI<TopProduct[]>(
      `/admin/dashboard/top-products?limit=${limit}`,
      {
        method: "GET",
      }
    );
  },

  /**
   * GET /api/v1/admin/dashboard/recent-orders?limit=10
   * Lấy danh sách đơn hàng gần đây
   * @param limit - Số lượng đơn hàng cần lấy (mặc định: 10)
   */
  getRecentOrders: async (
    limit: number = 10
  ): Promise<ApiResponse<RecentOrder[]>> => {
    return fetchAPI<RecentOrder[]>(
      `/admin/dashboard/recent-orders?limit=${limit}`,
      {
        method: "GET",
      }
    );
  },

  /**
   * GET /api/v1/admin/dashboard/low-stock-products?threshold=10
   * Lấy danh sách sản phẩm sắp hết hàng
   * @param threshold - Ngưỡng tồn kho cảnh báo (mặc định: 10)
   */
  getLowStockProducts: async (
    threshold: number = 10
  ): Promise<ApiResponse<LowStockProduct[]>> => {
    return fetchAPI<LowStockProduct[]>(
      `/admin/dashboard/low-stock-products?threshold=${threshold}`,
      {
        method: "GET",
      }
    );
  },
};

// ============================================
// ADMIN - USER MANAGEMENT API
// ============================================
export const adminUserAPI = {
  /**
   * GET /api/v1/admin/users
   * Lấy danh sách users với pagination và filters
   */
  getUsers: async (params: {
    page?: number;
    size?: number;
    role?: string;
    status?: string;
  }): Promise<ApiResponse<import("@/types").UsersPageResponse>> => {
    const queryParams = new URLSearchParams();
    if (params.page !== undefined)
      queryParams.append("page", params.page.toString());
    if (params.size !== undefined)
      queryParams.append("size", params.size.toString());
    if (params.role && params.role !== "ALL")
      queryParams.append("role", params.role);
    if (params.status && params.status !== "ALL")
      queryParams.append("status", params.status);

    const response = await fetchAPI<any>(
      `/admin/users?${queryParams.toString()}`
    );

    // Transform backend response to match frontend type
    if (response.success && response.data) {
      return {
        ...response,
        data: {
          users: response.data.content || [],
          totalPages: response.data.totalPages || 0,
          totalElements: response.data.totalElements || 0,
          currentPage: response.data.number || 0,
          pageSize: response.data.size || 10,
        },
      };
    }

    return response;
  },

  /**
   * PUT /api/v1/admin/users/{userId}/lock
   * Khóa tài khoản user
   */
  lockUser: async (userId: number): Promise<ApiResponse<User>> => {
    return fetchAPI<User>(`/admin/users/${userId}/lock`, {
      method: "PUT",
    });
  },

  /**
   * PUT /api/v1/admin/users/{userId}/unlock
   * Mở khóa tài khoản user
   */
  unlockUser: async (userId: number): Promise<ApiResponse<User>> => {
    return fetchAPI<User>(`/admin/users/${userId}/unlock`, {
      method: "PUT",
    });
  },

  /**
   * POST /api/v1/admin/users
   * Tạo tài khoản mới
   */
  createUser: async (
    data: import("@/types").CreateUserRequest
  ): Promise<ApiResponse<User>> => {
    return fetchAPI<User>("/admin/users", {
      method: "POST",
      body: JSON.stringify(data),
    });
  },

  /**
   * GET /api/v1/admin/users/{userId}
   * Lấy chi tiết user
   */
  getUserById: async (userId: number): Promise<ApiResponse<User>> => {
    return fetchAPI<User>(`/admin/users/${userId}`);
  },
};

// ============================================
// ADMIN - ORDER MANAGEMENT API
// ============================================
export const adminOrderAPI = {
  /**
   * GET /api/v1/admin/orders
   * Lấy danh sách đơn hàng với pagination và filters
   */
  getOrders: async (params: {
    page?: number;
    size?: number;
    sortBy?: string;
    sortDirection?: string;
    search?: string;
    status?: string;
  }): Promise<ApiResponse<any>> => {
    const queryParams = new URLSearchParams();
    if (params.page !== undefined) queryParams.append("page", params.page.toString());
    if (params.size !== undefined) queryParams.append("size", params.size.toString());
    if (params.sortBy) queryParams.append("sortBy", params.sortBy);
    if (params.sortDirection) queryParams.append("sortDirection", params.sortDirection);
    if (params.search) queryParams.append("search", params.search);
    if (params.status) queryParams.append("status", params.status);

    return fetchAPI<any>(`/admin/orders?${queryParams.toString()}`);
  },

  /**
   * GET /api/v1/admin/orders/{orderId}
   * Lấy chi tiết đơn hàng
   */
  getOrderDetail: async (orderId: number): Promise<ApiResponse<import("@/types").AdminOrderDetailResponse>> => {
    return fetchAPI<import("@/types").AdminOrderDetailResponse>(`/admin/orders/${orderId}`);
  },

  /**
   * PUT /api/v1/admin/orders/{orderId}/status
   * Cập nhật trạng thái đơn hàng
   */
  updateOrderStatus: async (
    orderId: number,
    newStatus: string,
    adminNote?: string
  ): Promise<ApiResponse<import("@/types").AdminOrderDetailResponse>> => {
    const params = new URLSearchParams();
    params.append("newStatus", newStatus);
    if (adminNote) params.append("adminNote", adminNote);

    return fetchAPI<import("@/types").AdminOrderDetailResponse>(
      `/admin/orders/${orderId}/status?${params.toString()}`,
      { method: "PUT" }
    );
  },

  /**
   * GET /api/v1/admin/orders/{orderId}/available-transitions
   * Lấy danh sách trạng thái có thể chuyển đổi
   */
  getAvailableTransitions: async (orderId: number): Promise<ApiResponse<string[]>> => {
    return fetchAPI<string[]>(`/admin/orders/${orderId}/available-transitions`);
  },
};

// Export default fetchAPI for use in services
export default fetchAPI;

