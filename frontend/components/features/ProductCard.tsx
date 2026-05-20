"use client";

import { Heart, Star, ShoppingCart } from "lucide-react";
import { formatPrice } from "@/lib/utils";
import { useAuth } from "@/hooks";
import { useCartStore } from "@/store";
import { cartAPI } from "@/lib/api";
import { toast } from "sonner";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { cn } from "@/lib/utils";

interface ProductCardProps {
  id: number;
  name: string;
  image: string;
  // Support both mock data format and backend format
  originalPrice?: number; // Mock data
  salePrice?: number; // Mock data
  price?: number; // Backend: original price
  discountPercent?: number; // Backend: discount % (0-100)
  discountedPrice?: number; // Backend: price after discount
  rating: number;
  reviews: number;
  discount?: number; // Mock data discount
  isNew?: boolean;
}

export function ProductCard({
  id,
  name,
  image,
  originalPrice,
  salePrice,
  price,
  discountPercent,
  discountedPrice,
  rating,
  reviews,
  discount,
  isNew = false,
}: ProductCardProps) {
  const router = useRouter();
  const { user } = useAuth();
  const isAuthenticated = !!user;
  const { addItem, setItems } = useCartStore();

  // Determine actual prices based on available data
  // Priority: Backend data (price/discountedPrice) > Mock data (originalPrice/salePrice)
  const actualOriginalPrice = price ?? originalPrice ?? 0;
  const actualFinalPrice = discountedPrice ?? salePrice ?? actualOriginalPrice;
  
  // Calculate discount percentage safely
  const actualDiscountPercent = (() => {
    // If discountPercent is provided and valid, use it
    if (discountPercent && discountPercent > 0 && discountPercent <= 100) {
      return Math.round(discountPercent);
    }
    
    // If discount is provided (old format), use it
    if (discount && discount > 0 && discount <= 100) {
      return Math.round(discount);
    }
    
    // Otherwise calculate from prices
    if (actualOriginalPrice > 0 && actualFinalPrice < actualOriginalPrice) {
      const calculated = Math.round((1 - actualFinalPrice / actualOriginalPrice) * 100);
      return Math.max(0, Math.min(100, calculated));
    }
    
    return 0;
  })();
  
  const hasDiscount = actualDiscountPercent > 0;

  const isValidImage = (src: unknown) => {
    if (!src || typeof src !== "string") return false;
    return /^(https?:\/\/|\/|data:|blob:)/i.test(src);
  };

  const handleAddToCart = async (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    
    console.log('[ProductCard.handleAddToCart] Starting add to cart', {
      productId: id,
      isAuthenticated,
      user: user ? { id: user.id, email: user.email } : null,
    });
    
    if (!isAuthenticated) {
      console.log('[ProductCard.handleAddToCart] User not authenticated, adding to local cart');
      // Add to local cart store for guest
      addItem({
        productId: id,
        productName: name,
        productImage: isValidImage(image) ? image : "",
        price: actualOriginalPrice,
        discountPercent: hasDiscount ? actualDiscountPercent : undefined,
        appliedPrice: actualFinalPrice,
        quantity: 1,
      } as any);
      toast.success("Đã thêm vào giỏ (khách) — đăng nhập để đồng bộ");
      return;
    }

    console.log('[ProductCard.handleAddToCart] User authenticated, calling API');
    try {
      const resp = await cartAPI.addToCart({ productId: id, quantity: 1 });
      console.log('[ProductCard.handleAddToCart] API response received:', resp);
      console.log('[ProductCard.handleAddToCart] Response success:', resp?.success);
      console.log('[ProductCard.handleAddToCart] Response data:', resp?.data);
      
      if (!resp) {
        throw new Error("Không nhận được phản hồi từ server");
      }
      
      if (resp.success === false) {
        throw new Error(resp.message || "Không thể thêm vào giỏ hàng");
      }
      
      // If success is true or undefined (some APIs might not have success field)
      // Try to refresh cart from backend
      try {
        console.log('[ProductCard.handleAddToCart] Refreshing cart from backend...');
        const cartResp = await cartAPI.getCurrentCart();
        console.log('[ProductCard.handleAddToCart] Cart refresh response:', cartResp);
        
        if (cartResp && cartResp.data) {
          const backendItems = Array.isArray(cartResp.data.items)
            ? cartResp.data.items
            : [];
          console.log('[ProductCard.handleAddToCart] Backend items:', backendItems);
          
          if (backendItems.length > 0) {
            const mappedItems = backendItems.map((obj: any) => ({
              id: Number(obj.id ?? 0),
              productId: Number(obj.productId ?? 0),
              productName:
                typeof obj.productName === "string"
                  ? obj.productName
                  : obj.product?.name ?? "Unknown Product",
              productImage:
                typeof obj.productImage === "string"
                  ? obj.productImage
                  : obj.productThumbnailUrl ?? obj.product?.thumbnailUrl ?? "",
              price:
                typeof obj.price === "number"
                  ? obj.price
                  : obj.unitPrice ?? obj.product?.salePrice ?? 0,
              quantity: Number(obj.quantity ?? 0),
              color: obj.color,
              storage: obj.storage,
            }));

            console.log('[ProductCard.handleAddToCart] Mapped items:', mappedItems);
            setItems(mappedItems as any);
            console.log('[ProductCard.handleAddToCart] Cart updated in store');
          } else {
            console.warn('[ProductCard.handleAddToCart] Backend cart is empty after adding item');
          }
        } else {
          console.warn('[ProductCard.handleAddToCart] Cart refresh response has no data');
        }
      } catch (syncErr) {
        console.error(
          "[ProductCard.handleAddToCart] Failed to refresh cart after addToCart:",
          syncErr
        );
        // Don't throw - item was added, just couldn't refresh
      }

      toast.success("Đã thêm vào giỏ hàng");
    } catch (e: any) {
      console.error("[ProductCard.handleAddToCart] Add to cart failed:", e);
      console.error("[ProductCard.handleAddToCart] Error details:", {
        message: e?.message,
        stack: e?.stack,
        response: (e as any)?.response,
      });
      toast.error(e?.message || "Lỗi khi thêm vào giỏ");
    }
  };

  return (
    <Link href={`/products/${id}`} className="block h-full group">
      <div className={cn(
        "bg-card rounded-2xl border border-border/50 overflow-hidden h-full flex flex-col",
        "shadow-sm hover:shadow-2xl transition-all duration-500 ease-out",
        "hover:-translate-y-3 hover:border-primary/50",
        "relative before:absolute before:inset-0 before:bg-gradient-to-br before:from-primary/0 before:to-primary/0 hover:before:from-primary/5 hover:before:to-transparent before:transition-all before:duration-500",
        "cursor-pointer"
      )}>
        {/* Image Container với overlay effect */}
        <div className="relative overflow-hidden">
          <div className="h-48 md:h-64 bg-gradient-to-br from-gray-100 via-gray-50 to-gray-100 dark:from-gray-800 dark:via-gray-900 dark:to-gray-800 flex items-center justify-center group-hover:scale-110 transition-transform duration-700 ease-out">
            {isValidImage(image) ? (
              <img 
                src={image} 
                alt={name}
                className="w-full h-full object-contain p-4"
              />
            ) : (
              <div className="text-6xl md:text-7xl">{image}</div>
            )}
          </div>
          
          {/* Gradient Overlay on Hover */}
          <div className="absolute inset-0 bg-gradient-to-t from-black/20 via-transparent to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-500" />
          
          {/* Badges */}
          {isNew && (
            <span className="absolute top-3 left-3 bg-gradient-to-r from-emerald-500 to-green-500 text-white text-xs font-bold px-3 py-1.5 rounded-lg shadow-lg backdrop-blur-sm animate-in fade-in slide-in-from-top-2">
              Mới
            </span>
          )}
          {hasDiscount && (
            <span className="absolute top-3 right-3 bg-gradient-to-r from-red-500 to-rose-500 text-white text-xs font-bold px-3 py-1.5 rounded-lg shadow-lg backdrop-blur-sm">
              -{Math.round(actualDiscountPercent)}%
            </span>
          )}

          {/* Wishlist Button - Improved */}
          <button
            onClick={(e) => {
              e.preventDefault();
              e.stopPropagation();
            }}
            className="absolute bottom-3 right-3 rounded-full bg-white/95 backdrop-blur-sm p-2.5 shadow-lg opacity-0 transition-all duration-300 group-hover:opacity-100 hover:shadow-xl hover:scale-110 hover:bg-white focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring z-10"
            aria-label="Thêm vào yêu thích"
          >
            <Heart className="w-5 h-5 text-gray-600 hover:text-red-500 transition-colors" />
          </button>
        </div>
        
        {/* Content */}
        <div className="p-4 md:p-5 flex-1 flex flex-col bg-gradient-to-b from-card to-card/95">
          <h3 className="font-semibold text-foreground mb-2 line-clamp-2 min-h-[3rem] text-base md:text-lg group-hover:text-primary transition-colors duration-300">
            {name}
          </h3>
          
          {/* Rating */}
          <div className="flex items-center gap-1 mb-3">
            {[...Array(5)].map((_, i) => (
              <Star
                key={i}
                className={cn(
                  "w-4 h-4 transition-all",
                  i < Math.floor(rating)
                    ? "fill-amber-400 text-amber-400"
                    : "fill-gray-200 text-gray-200 dark:fill-gray-700 dark:text-gray-700"
                )}
              />
            ))}
            {reviews > 0 && (
              <span className="text-xs text-muted-foreground ml-1">
                ({reviews})
              </span>
            )}
          </div>
          
          {/* Price & CTA */}
          <div className="mt-auto pt-3 border-t border-border/50">
            <div className="flex flex-col mb-3">
              <span className="text-xl md:text-2xl font-bold bg-gradient-to-r from-primary to-primary/80 bg-clip-text text-transparent">
                {formatPrice(actualFinalPrice)}
              </span>
              {hasDiscount && (
                <span className="text-sm text-muted-foreground line-through">
                  {formatPrice(actualOriginalPrice)}
                </span>
              )}
            </div>
            
            <button
              onClick={handleAddToCart}
              className="w-full flex items-center justify-center gap-2 rounded-xl bg-gradient-to-r from-primary to-primary/90 px-4 py-3 text-sm font-semibold text-primary-foreground hover:from-primary/90 hover:to-primary/80 transition-all duration-300 hover:shadow-lg hover:scale-[1.02] active:scale-[0.98]"
            >
              <ShoppingCart className="w-4 h-4" />
              Thêm giỏ
            </button>
          </div>
        </div>
      </div>
    </Link>
  );
}
