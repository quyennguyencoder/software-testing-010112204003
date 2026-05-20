/**
 * Application route constants
 */

export const ROUTES = {
  // Public routes
  HOME: '/',

  // Auth routes
  LOGIN: '/login',
  REGISTER: '/register',
  FORGOT_PASSWORD: '/forgot-password',
  ACCOUNT_LOCKED: '/account-locked',

  // User routes
  // User routes
  USER: '/user',
  PROFILE: '/user?tab=profile',
  ORDERS: '/user?tab=orders',
  ADDRESSES: '/user?tab=addresses',
  WISHLIST: '/user?tab=wishlist',
  CART: '/cart',

  // Admin routes
  ADMIN: '/admin',
  ADMIN_DASHBOARD: '/admin?tab=dashboard',
  ADMIN_PRODUCTS: '/admin?tab=products',
  ADMIN_ORDERS: '/admin?tab=orders',
  ADMIN_USERS: '/admin?tab=users',
} as const;

export type RouteKey = keyof typeof ROUTES;
export type RouteValue = typeof ROUTES[RouteKey];
