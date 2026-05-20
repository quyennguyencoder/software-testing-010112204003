/**
 * Centralized mock data for development
 * TODO: Replace with real API calls
 */

export interface Product {
  id: number;
  name: string;
  image: string;
  originalPrice: number; // For mock data compatibility
  salePrice: number; // For mock data compatibility
  // Backend fields
  price?: number; // Original price from backend
  discountPercent?: number; // Discount % from active DISCOUNT promotions (0-100)
  discountedPrice?: number; // Price after discount
  rating: number;
  reviews: number;
  discount: number; // For mock data compatibility
  isNew?: boolean;
  stock?: number;
  category?: string;
  sales?: number;
}

export interface Order {
  id: number;
  customer: string;
  total: number;
  status: "pending" | "processing" | "shipped" | "delivered" | "cancelled";
  date: string;
  items: number;
}

export interface UserData {
  id: number;
  name: string;
  email: string;
  role: string;
  status: "ACTIVE" | "INACTIVE" | "BANNED";
  joinDate: string;
}

/**
 * Mock featured products
 */
export const MOCK_FEATURED_PRODUCTS: Product[] = [
  {
    id: 1,
    name: "iPhone 15 Pro Max",
    image: "📱",
    originalPrice: 34990000,
    salePrice: 32990000,
    rating: 4.9,
    reviews: 256,
    discount: 6,
    isNew: true,
  },
  {
    id: 2,
    name: "Samsung Galaxy S24 Ultra",
    image: "📱",
    originalPrice: 33990000,
    salePrice: 29990000,
    rating: 4.8,
    reviews: 189,
    discount: 12,
    isNew: true,
  },
  {
    id: 3,
    name: "OPPO Find X7 Ultra",
    image: "📱",
    originalPrice: 24990000,
    salePrice: 22990000,
    rating: 4.7,
    reviews: 124,
    discount: 8,
    isNew: false,
  },
  {
    id: 4,
    name: "Xiaomi 14 Pro",
    image: "📱",
    originalPrice: 19990000,
    salePrice: 17990000,
    rating: 4.6,
    reviews: 98,
    discount: 10,
    isNew: false,
  },
];

/**
 * Mock flash sale products
 */
export const MOCK_FLASH_SALE_PRODUCTS: Product[] = [
  {
    id: 5,
    name: "iPhone 14",
    image: "📱",
    originalPrice: 22990000,
    salePrice: 17990000,
    rating: 4.8,
    reviews: 512,
    discount: 22,
  },
  {
    id: 6,
    name: "Samsung Galaxy A54",
    image: "📱",
    originalPrice: 10990000,
    salePrice: 8490000,
    rating: 4.5,
    reviews: 324,
    discount: 23,
  },
  {
    id: 7,
    name: "Realme GT Neo 5",
    image: "📱",
    originalPrice: 12990000,
    salePrice: 9990000,
    rating: 4.4,
    reviews: 156,
    discount: 23,
  },
  {
    id: 8,
    name: "vivo V29e",
    image: "📱",
    originalPrice: 9990000,
    salePrice: 7990000,
    rating: 4.3,
    reviews: 87,
    discount: 20,
  },
];

/**
 * Mock products for admin management
 */
export const MOCK_PRODUCTS: Product[] = [
  {
    id: 1,
    name: "iPhone 15 Pro Max",
    image: "📱",
    originalPrice: 34990000,
    salePrice: 32990000,
    rating: 4.9,
    reviews: 234,
    discount: 6,
    stock: 45,
    category: "Smartphone",
    sales: 234,
  },
  {
    id: 2,
    name: "Samsung Galaxy S24 Ultra",
    image: "📱",
    originalPrice: 33990000,
    salePrice: 29990000,
    rating: 4.8,
    reviews: 189,
    discount: 12,
    stock: 32,
    category: "Smartphone",
    sales: 189,
  },
  {
    id: 3,
    name: "OPPO Find X7 Ultra",
    image: "📱",
    originalPrice: 24990000,
    salePrice: 22990000,
    rating: 4.7,
    reviews: 156,
    discount: 8,
    stock: 28,
    category: "Smartphone",
    sales: 156,
  },
  {
    id: 4,
    name: "Xiaomi 14 Pro",
    image: "📱",
    originalPrice: 19990000,
    salePrice: 17990000,
    rating: 4.6,
    reviews: 142,
    discount: 10,
    stock: 50,
    category: "Smartphone",
    sales: 142,
  },
  {
    id: 5,
    name: "Google Pixel 8 Pro",
    image: "📱",
    originalPrice: 26990000,
    salePrice: 24990000,
    rating: 4.5,
    reviews: 87,
    discount: 7,
    stock: 15,
    category: "Smartphone",
    sales: 87,
  },
];

/**
 * Mock orders
 */
export const MOCK_ORDERS: Order[] = [
  {
    id: 1001,
    customer: "Nguyễn Văn A",
    total: 32990000,
    status: "delivered",
    date: "2024-01-15",
    items: 1,
  },
  {
    id: 1002,
    customer: "Trần Thị B",
    total: 29990000,
    status: "shipped",
    date: "2024-01-14",
    items: 1,
  },
  {
    id: 1003,
    customer: "Lê Văn C",
    total: 52980000,
    status: "processing",
    date: "2024-01-13",
    items: 2,
  },
  {
    id: 1004,
    customer: "Phạm Thị D",
    total: 17990000,
    status: "pending",
    date: "2024-01-12",
    items: 1,
  },
  {
    id: 1005,
    customer: "Hoàng Văn E",
    total: 62980000,
    status: "delivered",
    date: "2024-01-11",
    items: 2,
  },
];

/**
 * Mock users
 */
export const MOCK_USERS: UserData[] = [
  {
    id: 1,
    name: "Nguyễn Văn A",
    email: "nguyenvana@example.com",
    role: "CUSTOMER",
    status: "ACTIVE",
    joinDate: "2024-01-01",
  },
  {
    id: 2,
    name: "Trần Thị B",
    email: "tranthib@example.com",
    role: "CUSTOMER",
    status: "ACTIVE",
    joinDate: "2024-01-02",
  },
  {
    id: 3,
    name: "Lê Văn C",
    email: "levanc@example.com",
    role: "CUSTOMER",
    status: "INACTIVE",
    joinDate: "2024-01-03",
  },
  {
    id: 4,
    name: "Phạm Thị D",
    email: "phamthid@example.com",
    role: "CUSTOMER",
    status: "ACTIVE",
    joinDate: "2024-01-04",
  },
  {
    id: 5,
    name: "Admin User",
    email: "admin@utephonehub.com",
    role: "ADMIN",
    status: "ACTIVE",
    joinDate: "2024-01-05",
  },
];

/**
 * Mock dashboard stats
 */
export const MOCK_STATS = [
  {
    label: "Doanh thu",
    value: "1.234.567.890₫",
    change: "+20.1%",
    colorClass: "text-green-500",
  },
  {
    label: "Đơn hàng",
    value: "1,234",
    change: "+15.3%",
    colorClass: "text-blue-500",
  },
  {
    label: "Người dùng",
    value: "5,678",
    change: "+8.2%",
    colorClass: "text-purple-500",
  },
  {
    label: "Sản phẩm",
    value: "256",
    change: "+4.3%",
    colorClass: "text-orange-500",
  },
];
