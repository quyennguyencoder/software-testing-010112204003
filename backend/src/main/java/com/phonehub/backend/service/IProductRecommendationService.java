package com.phonehub.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phonehub.backend.dto.response.ChatbotAssistantUserResponse;
import com.phonehub.backend.dto.response.productview.ProductCardResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service gọi ProductView API để lấy sản phẩm cho Chatbot
 * 
 * OPTIMIZATION v2.0:
 * - Gọi trực tiếp IProductViewService thay vì HTTP API (giảm overhead)
 * - Sử dụng @Cacheable với Redis (thay vì in-memory cache)
 * - Lazy conversion từ ProductCardResponse -> RecommendedProductDTO
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IProductRecommendationService {
    
    private final RestTemplate restTemplate;
    private final IGeminiEmbeddingService embeddingService;
    private final ObjectMapper objectMapper;
    private final IProductViewService productViewService;
    
    @Value("${api.product.base-url:http://localhost:8081/api/v1/products}")
    private String productApiBaseUrl;
    
    // Fallback in-memory cache (khi Redis không available)
    private final Map<String, CachedProducts> productCache = new HashMap<>();
    private static final long CACHE_EXPIRY_MS = 3600000; // 1 giờ
    
    /**
     * Lấy sản phẩm nổi bật (GỌI TRỰC TIẾP SERVICE - không qua HTTP)
     */
    @Cacheable(value = "chatbotFeaturedProducts", unless = "#result == null || #result.isEmpty()")
    public List<ChatbotAssistantUserResponse.RecommendedProductDTO> getFeaturedProducts() {
        log.info("⭐ Chatbot: Lấy sản phẩm nổi bật (CACHE MISS)");
        try {
            List<ProductCardResponse> products = productViewService.getFeaturedProducts(10);
            return convertToRecommendedProducts(products);
        } catch (Exception e) {
            log.error("❌ Lỗi lấy featured products: {}", e.getMessage());
            return fallbackToHttpApi("featured");
        }
    }
    
    /**
     * Lấy sản phẩm bán chạy (GỌI TRỰC TIẾP SERVICE)
     */
    @Cacheable(value = "chatbotBestSellingProducts", unless = "#result == null || #result.isEmpty()")
    public List<ChatbotAssistantUserResponse.RecommendedProductDTO> getBestSellingProducts() {
        log.info("🔥 Chatbot: Lấy sản phẩm bán chạy (CACHE MISS)");
        try {
            List<ProductCardResponse> products = productViewService.getBestSellingProducts(10);
            return convertToRecommendedProducts(products);
        } catch (Exception e) {
            log.error("❌ Lỗi lấy best selling products: {}", e.getMessage());
            return fallbackToHttpApi("best-selling");
        }
    }
    
    /**
     * Lấy sản phẩm mới nhất (GỌI TRỰC TIẾP SERVICE)
     */
    @Cacheable(value = "chatbotNewArrivals", unless = "#result == null || #result.isEmpty()")
    public List<ChatbotAssistantUserResponse.RecommendedProductDTO> getNewArrivalsProducts() {
        log.info("🆕 Chatbot: Lấy sản phẩm mới (CACHE MISS)");
        try {
            List<ProductCardResponse> products = productViewService.getNewArrivals(10);
            return convertToRecommendedProducts(products);
        } catch (Exception e) {
            log.error("❌ Lỗi lấy new arrivals: {}", e.getMessage());
            return fallbackToHttpApi("new-arrivals");
        }
    }
    
    /**
     * Convert ProductCardResponse -> RecommendedProductDTO
     * Tối ưu: batch conversion với stream
     */
    private List<ChatbotAssistantUserResponse.RecommendedProductDTO> convertToRecommendedProducts(
            List<ProductCardResponse> cards) {
        if (cards == null || cards.isEmpty()) {
            return Collections.emptyList();
        }
        return cards.stream()
            .map(this::convertCard)
            .collect(Collectors.toList());
    }
    
    /**
     * Convert single ProductCardResponse -> RecommendedProductDTO
     */
    private ChatbotAssistantUserResponse.RecommendedProductDTO convertCard(ProductCardResponse card) {
        double price = card.getDiscountedPrice() != null 
            ? card.getDiscountedPrice().doubleValue()
            : (card.getMinPrice() != null ? card.getMinPrice().doubleValue() : 0.0);
        
        Double originalPrice = card.getOriginalPrice() != null 
            ? card.getOriginalPrice().doubleValue() : null;
        
        Integer discountPercent = null;
        if (originalPrice != null && price < originalPrice && originalPrice > 0) {
            discountPercent = (int) Math.round((1 - price / originalPrice) * 100);
        }
        
        // Build description từ specs
        StringBuilder desc = new StringBuilder();
        if (card.getRam() != null) desc.append("RAM ").append(card.getRam());
        if (card.getStorage() != null) {
            if (desc.length() > 0) desc.append(", ");
            desc.append(card.getStorage());
        }
        if (card.getBatteryCapacity() != null) {
            if (desc.length() > 0) desc.append(", ");
            desc.append(card.getBatteryCapacity()).append("mAh");
        }
        
        return ChatbotAssistantUserResponse.RecommendedProductDTO.builder()
            .id(card.getId())
            .name(card.getName())
            .description(desc.length() > 0 ? desc.toString() : card.getName())
            .price(price)
            .originalPrice(originalPrice)
            .rating(card.getAverageRating())
            .reviewCount(card.getTotalReviews())
            .imageUrl(card.getThumbnailUrl())
            .categoryName(card.getCategoryName())
            .productUrl("/products/" + card.getId())
            .ram(card.getRam())
            .storage(card.getStorage())
            .batteryCapacity(card.getBatteryCapacity())
            .operatingSystem(card.getOperatingSystem())
            .brandName(card.getBrandName())
            .discountPercent(discountPercent)
            .hasDiscount(card.getHasDiscount())
            .inStock(card.getInStock() != null ? card.getInStock() : true)
            .build();
    }
    
    /**
     * Fallback: Gọi HTTP API khi service call fail
     */
    private List<ChatbotAssistantUserResponse.RecommendedProductDTO> fallbackToHttpApi(String endpoint) {
        log.warn("⚠️ Fallback to HTTP API for: {}", endpoint);
        String url = productApiBaseUrl + "/" + endpoint;
        return fetchProductsFromApi(url);
    }
    
    /**
     * Tìm kiếm sản phẩm theo từ khóa + lọc
     */
    public List<ChatbotAssistantUserResponse.RecommendedProductDTO> searchProducts(
            String keyword, Double minPrice, Double maxPrice, Long categoryId, String sortBy) {
        
        log.info("🔍 Tìm kiếm sản phẩm: keyword={}, categoryId={}, minPrice={}, maxPrice={}, sortBy={}",
                keyword, categoryId, minPrice, maxPrice, sortBy);
        
        String url = UriComponentsBuilder.fromHttpUrl(productApiBaseUrl + "/search")
                .queryParam("keyword", keyword)
                .queryParamIfPresent("minPrice", Optional.ofNullable(minPrice))
                .queryParamIfPresent("maxPrice", Optional.ofNullable(maxPrice))
                .queryParamIfPresent("categoryId", Optional.ofNullable(categoryId))
                .queryParamIfPresent("sortBy", Optional.ofNullable(sortBy))
                .queryParam("limit", 10) // Giới hạn để tối ưu
                .build()
                .toUriString();
        
        return fetchProductsFromApi(url);
    }
    
    /**
     * Lấy sản phẩm theo danh mục
     */
    public List<ChatbotAssistantUserResponse.RecommendedProductDTO> getProductsByCategory(Long categoryId) {
        String cacheKey = "category_" + categoryId;
        return getProductsFromCache(cacheKey, () -> {
            log.info("📁 Gọi API /category/{} để lấy sản phẩm", categoryId);
            String url = productApiBaseUrl + "/category/" + categoryId;
            return fetchProductsFromApi(url);
        });
    }
    
    /**
     * Lấy sản phẩm liên quan (recommend tương tự)
     */
    public List<ChatbotAssistantUserResponse.RecommendedProductDTO> getRelatedProducts(Long productId) {
        String cacheKey = "related_" + productId;
        return getProductsFromCache(cacheKey, () -> {
            log.info("🔗 Gọi API /{}/related để lấy sản phẩm liên quan", productId);
            String url = productApiBaseUrl + "/" + productId + "/related";
            return fetchProductsFromApi(url);
        });
    }
    
    /**
     * Lọc sản phẩm theo RAM
     * Chi phí: 0 token (API trực tiếp)
     */
    public List<ChatbotAssistantUserResponse.RecommendedProductDTO> filterByRam(String ramValue) {
        String cacheKey = "filter_ram_" + ramValue;
        return getProductsFromCache(cacheKey, () -> {
            log.info("💾 Gọi API /filter/ram?ramOptions={}", ramValue);
            String url = UriComponentsBuilder.fromHttpUrl(productApiBaseUrl + "/filter/ram")
                    .queryParam("ramOptions", ramValue)
                    .queryParam("page", 0)
                    .queryParam("size", 10)
                    .build()
                    .toUriString();
            return fetchProductsFromApi(url);
        });
    }
    
    /**
     * Lọc sản phẩm theo Storage
     * Chi phí: 0 token (API trực tiếp)
     */
    public List<ChatbotAssistantUserResponse.RecommendedProductDTO> filterByStorage(String storageValue) {
        String cacheKey = "filter_storage_" + storageValue;
        return getProductsFromCache(cacheKey, () -> {
            log.info("💿 Gọi API /filter/storage?storageOptions={}", storageValue);
            String url = UriComponentsBuilder.fromHttpUrl(productApiBaseUrl + "/filter/storage")
                    .queryParam("storageOptions", storageValue)
                    .queryParam("page", 0)
                    .queryParam("size", 10)
                    .build()
                    .toUriString();
            return fetchProductsFromApi(url);
        });
    }
    
    /**
     * Lọc sản phẩm theo Pin (Battery)
     * Chi phí: 0 token (API trực tiếp)
     */
    public List<ChatbotAssistantUserResponse.RecommendedProductDTO> filterByBattery(String batteryRange) {
        String cacheKey = "filter_battery_" + batteryRange;
        return getProductsFromCache(cacheKey, () -> {
            log.info("🔋 Gọi API /filter/battery?minBattery={}", batteryRange);
            String url = UriComponentsBuilder.fromHttpUrl(productApiBaseUrl + "/filter/battery")
                    .queryParam("minBattery", batteryRange)
                    .queryParam("page", 0)
                    .queryParam("size", 10)
                    .build()
                    .toUriString();
            return fetchProductsFromApi(url);
        });
    }
    
    /**
     * Lọc sản phẩm theo Kích thước Màn hình
     * Chi phí: 0 token (API trực tiếp)
     */
    public List<ChatbotAssistantUserResponse.RecommendedProductDTO> filterByScreen(String screenSize) {
        String cacheKey = "filter_screen_" + screenSize;
        return getProductsFromCache(cacheKey, () -> {
            log.info("📱 Gọi API /filter/screen?screenSizeOptions={}", screenSize);
            String url = UriComponentsBuilder.fromHttpUrl(productApiBaseUrl + "/filter/screen")
                    .queryParam("screenSizeOptions", screenSize)
                    .queryParam("page", 0)
                    .queryParam("size", 10)
                    .build()
                    .toUriString();
            return fetchProductsFromApi(url);
        });
    }
    
    /**
     * Lọc sản phẩm theo Hệ Điều Hành
     * Chi phí: 0 token (API trực tiếp)
     */
    public List<ChatbotAssistantUserResponse.RecommendedProductDTO> filterByOS(String osValue) {
        String cacheKey = "filter_os_" + osValue;
        return getProductsFromCache(cacheKey, () -> {
            log.info("🖥️ Gọi API /filter/os?osOptions={}", osValue);
            String url = UriComponentsBuilder.fromHttpUrl(productApiBaseUrl + "/filter/os")
                    .queryParam("osOptions", osValue)
                    .queryParam("page", 0)
                    .queryParam("size", 10)
                    .build()
                    .toUriString();
            return fetchProductsFromApi(url);
        });
    }
    
    /**
     * Lọc sản phẩm theo Đánh Giá (Rating)
     * Chi phí: 0 token (API trực tiếp)
     */
    public List<ChatbotAssistantUserResponse.RecommendedProductDTO> filterByRating(Double minRating) {
        String cacheKey = "filter_rating_" + minRating;
        return getProductsFromCache(cacheKey, () -> {
            log.info("⭐ Gọi API /filter/rating?minRating={}", minRating);
            String url = UriComponentsBuilder.fromHttpUrl(productApiBaseUrl + "/filter/rating")
                    .queryParam("minRating", minRating)
                    .queryParam("page", 0)
                    .queryParam("size", 10)
                    .build()
                    .toUriString();
            return fetchProductsFromApi(url);
        });
    }
    
    /**
     * Lọc sản phẩm dựa trên embedding similarity
     * (Chỉ gọi khi cần, để tối ưu chi phí embedding)
     */
    public List<ChatbotAssistantUserResponse.RecommendedProductDTO> filterByEmbeddingSimilarity(
            List<ChatbotAssistantUserResponse.RecommendedProductDTO> products,
            String userQuery,
            double threshold) {
        
        log.info("🧠 Lọc sản phẩm dùng embedding similarity, threshold={}", threshold);
        
        try {
            List<Double> queryEmbedding = embeddingService.getEmbedding(userQuery);
            
            List<ChatbotAssistantUserResponse.RecommendedProductDTO> filtered = new ArrayList<>();
            
            for (ChatbotAssistantUserResponse.RecommendedProductDTO product : products) {
                String productText = product.getName() + " " + product.getDescription();
                List<Double> productEmbedding = embeddingService.getEmbedding(productText);
                
                double similarity = embeddingService.cosineSimilarity(queryEmbedding, productEmbedding);
                
                if (similarity >= threshold) {
                    product.setMatchScore(similarity);
                    filtered.add(product);
                }
            }
            
            // Sắp xếp theo độ tương tự giảm dần
            filtered.sort((a, b) -> Double.compare(b.getMatchScore(), a.getMatchScore()));
            
            log.info("✅ Lọc xong: {} sản phẩm phù hợp (threshold={})", filtered.size(), threshold);
            return filtered;
        } catch (Exception e) {
            log.error("❌ Lỗi lọc embedding: {}", e.getMessage());
            return products;
        }
    }
    
    /**
     * Gọi API internal để lấy sản phẩm
     */
    private List<ChatbotAssistantUserResponse.RecommendedProductDTO> fetchProductsFromApi(String url) {
        try {
            log.debug("🌐 Gọi API: {}", url);
            String responseJson = restTemplate.getForObject(url, String.class);
            
            JsonNode rootNode = objectMapper.readTree(responseJson);
            List<ChatbotAssistantUserResponse.RecommendedProductDTO> products = new ArrayList<>();
            
            // Xử lý response (có thể là mảng, object.data, hoặc object.data.content nếu là Page)
            JsonNode dataNode;
            if (rootNode.isArray()) {
                dataNode = rootNode;
            } else if (rootNode.has("data")) {
                JsonNode data = rootNode.path("data");
                // Nếu data có "content" thì là Page object
                if (data.has("content") && data.path("content").isArray()) {
                    dataNode = data.path("content");
                } else if (data.isArray()) {
                    dataNode = data;
                } else {
                    dataNode = data;
                }
            } else {
                dataNode = rootNode;
            }
            
            dataNode.forEach(productNode -> {
                // Lấy giá: ưu tiên discountedPrice, nếu không có thì dùng minPrice, cuối cùng là originalPrice
                double price = 0.0;
                if (productNode.has("discountedPrice") && !productNode.path("discountedPrice").isNull()) {
                    price = productNode.path("discountedPrice").asDouble();
                } else if (productNode.has("minPrice") && !productNode.path("minPrice").isNull()) {
                    price = productNode.path("minPrice").asDouble();
                } else if (productNode.has("originalPrice") && !productNode.path("originalPrice").isNull()) {
                    price = productNode.path("originalPrice").asDouble();
                }
                
                // Lấy rating và review count
                double rating = productNode.has("averageRating") && !productNode.path("averageRating").isNull()
                    ? productNode.path("averageRating").asDouble(0.0)
                    : 0.0;
                int reviewCount = productNode.has("totalReviews") && !productNode.path("totalReviews").isNull()
                    ? productNode.path("totalReviews").asInt(0)
                    : 0;
                
                // Lấy image URL
                String imageUrl = productNode.has("thumbnailUrl") && !productNode.path("thumbnailUrl").isNull()
                    ? productNode.path("thumbnailUrl").asText()
                    : "";
                
                // Lấy category name
                String categoryName = productNode.has("categoryName") && !productNode.path("categoryName").isNull()
                    ? productNode.path("categoryName").asText()
                    : (productNode.has("category") && productNode.path("category").has("name")
                        ? productNode.path("category").path("name").asText()
                        : "");
                
                ChatbotAssistantUserResponse.RecommendedProductDTO product = 
                    ChatbotAssistantUserResponse.RecommendedProductDTO.builder()
                        .id(productNode.path("id").asLong())
                        .name(productNode.path("name").asText())
                        .description(productNode.has("description") ? productNode.path("description").asText() : "")
                        .price(price)
                        .rating(rating)
                        .reviewCount(reviewCount)
                        .imageUrl(imageUrl)
                        .categoryName(categoryName)
                        .productUrl("/products/" + productNode.path("id").asLong())
                        .build();
                products.add(product);
            });
            
            log.debug("✅ Lấy được {} sản phẩm từ API", products.size());
            return products;
        } catch (Exception e) {
            log.error("❌ Lỗi gọi API sản phẩm: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
    
    /**
     * Helper: Lấy từ cache hoặc gọi API
     */
    private List<ChatbotAssistantUserResponse.RecommendedProductDTO> getProductsFromCache(
            String key, java.util.function.Supplier<List<ChatbotAssistantUserResponse.RecommendedProductDTO>> fetcher) {
        
        if (productCache.containsKey(key)) {
            CachedProducts cached = productCache.get(key);
            if (!cached.isExpired()) {
                log.debug("💾 Sử dụng cache cho key: {}", key);
                return cached.products;
            }
        }
        
        List<ChatbotAssistantUserResponse.RecommendedProductDTO> products = fetcher.get();
        productCache.put(key, new CachedProducts(products));
        return products;
    }
    
    /**
     * Xóa cache sản phẩm
     */
    public void clearCache() {
        productCache.clear();
        log.info("🧹 Product cache đã được xóa");
    }
    
    /**
     * Class helper để cache sản phẩm
     */
    private static class CachedProducts {
        private final List<ChatbotAssistantUserResponse.RecommendedProductDTO> products;
        private final long timestamp;
        
        CachedProducts(List<ChatbotAssistantUserResponse.RecommendedProductDTO> products) {
            this.products = products;
            this.timestamp = System.currentTimeMillis();
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_EXPIRY_MS;
        }
    }
}
