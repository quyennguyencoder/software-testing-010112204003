package com.phonehub.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phonehub.backend.dto.request.ChatbotAssistantUserRequest;
import com.phonehub.backend.dto.request.productview.ProductFilterRequest;
import com.phonehub.backend.dto.response.ChatbotAssistantUserResponse;
import com.phonehub.backend.dto.response.productview.ProductCardResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service chính cho Chatbot Tư Vấn Sản Phẩm
 * 
 * Luồng hoạt động:
 * 1. Phân loại intent từ câu hỏi của khách hàng
 * 2. Gọi API ProductView phù hợp (tối ưu chi phí)
 * 3. Nếu cần, dùng embedding để lọc sản phẩm phù hợp
 * 4. Tạo phản hồi từ Gemini AI
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IChatbotAssistantUserService {
    
    @SuppressWarnings("unused") // Reserved for future semantic search feature
    private final IGeminiEmbeddingService embeddingService;
    private final IProductRecommendationService productService;
    private final IGeminiFallbackService fallbackService;
    private final IProductViewService productViewService;
    private final ObjectMapper objectMapper;
    private final IChatbotConfigService chatbotConfigService;
    
    // Brand name to ID mapping - CÁC HÃNG ĐANG KINH DOANH
    private static final Map<String, Long> BRAND_NAME_TO_ID = Map.ofEntries(
        Map.entry("apple", 1L), Map.entry("iphone", 1L),
        Map.entry("samsung", 2L), Map.entry("galaxy", 2L),
        Map.entry("xiaomi", 3L), Map.entry("redmi", 3L), Map.entry("poco", 3L),
        Map.entry("oppo", 4L), Map.entry("reno", 4L),
        Map.entry("vivo", 5L),
        Map.entry("realme", 6L),
        Map.entry("huawei", 7L), Map.entry("honor", 7L)
    );
    
    // CÁC HÃNG KHÔNG KINH DOANH - Cần thông báo rõ cho khách
    private static final Map<String, String> UNSUPPORTED_BRANDS = Map.ofEntries(
        Map.entry("nokia", "Nokia"),
        Map.entry("sony", "Sony"),
        Map.entry("lg", "LG"),
        Map.entry("motorola", "Motorola"),
        Map.entry("moto", "Motorola"),
        Map.entry("asus", "ASUS"),
        Map.entry("rog", "ASUS ROG"),
        Map.entry("lenovo", "Lenovo"),
        Map.entry("htc", "HTC"),
        Map.entry("blackberry", "BlackBerry"),
        Map.entry("oneplus", "OnePlus"),
        Map.entry("google", "Google Pixel"),
        Map.entry("pixel", "Google Pixel"),
        Map.entry("nothing", "Nothing Phone"),
        Map.entry("zte", "ZTE"),
        Map.entry("tcl", "TCL"),
        Map.entry("infinix", "Infinix"),
        Map.entry("tecno", "Tecno")
    );
    
    // Danh sách tên hãng đang kinh doanh (cho prompt AI)
    private static final String SUPPORTED_BRANDS_TEXT = "Apple/iPhone, Samsung/Galaxy, Xiaomi/Redmi/POCO, OPPO/Reno, Vivo, Realme, Huawei/Honor";
    
    // Các hãng lớn để fallback khi không có tiêu chí cụ thể
    private static final List<Long> TOP_BRAND_IDS = List.of(1L, 2L); // Apple, Samsung
    
    /**
     * Xử lý câu hỏi từ khách hàng
     * Logic cải tiến:
     * 1. KIỂM TRA TRẠNG THÁI CHATBOT - Nếu tắt, trả về fallback response
     * 2. Phân loại intent từ câu hỏi
     * 3. Tạo ProductFilterRequest đa tiêu chí từ message
     * 4. Gọi IProductViewService.filterProducts() trực tiếp (không qua HTTP)
     * 5. Tạo phản hồi AI với context sản phẩm
     */
    public ChatbotAssistantUserResponse chat(ChatbotAssistantUserRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("🤖 Chatbot nhận câu hỏi: {}", request.getMessage());
            
            // 0. KIỂM TRA TRẠNG THÁI CHATBOT
            if (!chatbotConfigService.isChatbotEnabled()) {
                log.info("⚠️ Chatbot đang TẮT - Trả về fallback response với sản phẩm nổi bật/mới/bán chạy");
                return chatbotConfigService.createFallbackResponse();
            }
            
            // 1. Phân loại intent
            String intent = detectIntent(request.getMessage());
            log.info("🎯 Intent phát hiện: {}", intent);
            
            // 2. Tạo filter đa tiêu chí từ message
            ProductFilterRequest filter = buildFilterFromMessage(request.getMessage(), request);
            log.info("🔧 Filter tạo thành công: categoryIds={}, brandIds={}, price=[{}-{}], ram={}, storage={}, battery={}, os={}",
                filter.getCategoryIds(), filter.getBrandIds(), 
                filter.getMinPrice(), filter.getMaxPrice(),
                filter.getRamOptions(), filter.getStorageOptions(),
                filter.getMinBattery(), filter.getOsOptions());
            
            // 3. Lấy sản phẩm dựa trên intent và filter
            List<ChatbotAssistantUserResponse.RecommendedProductDTO> products = 
                getProductsByIntentWithFilter(intent, filter, request);
            log.info("📦 Lấy được {} sản phẩm", products.size());
            
            // 4. Tính điểm relevance
            double relevanceScore = calculateRelevanceScore(products, request.getMessage());
            
            // 5. Giới hạn kết quả (max 5 sản phẩm)
            products = products.stream()
                .limit(5)
                .collect(Collectors.toList());
            
            // 6. Tạo phản hồi từ Gemini
            String aiResponse = generateAiResponse(request.getMessage(), products, intent);
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            return ChatbotAssistantUserResponse.builder()
                .aiResponse(aiResponse)
                .recommendedProducts(products)
                .detectedIntent(intent)
                .relevanceScore(relevanceScore)
                .processingTimeMs(processingTime)
                .build();
                
        } catch (Exception e) {
            log.error("❌ Lỗi xử lý chatbot: {}", e.getMessage(), e);
            return ChatbotAssistantUserResponse.builder()
                .aiResponse("Xin lỗi, tôi gặp sự cố khi xử lý yêu cầu của bạn. Vui lòng thử lại.")
                .recommendedProducts(Collections.emptyList())
                .detectedIntent("ERROR")
                .processingTimeMs(System.currentTimeMillis() - startTime)
                .build();
        }
    }
    
    /**
     * Tạo ProductFilterRequest từ message người dùng
     * Phân tích tất cả tiêu chí: brand, RAM, storage, battery, screen, OS, price
     */
    private ProductFilterRequest buildFilterFromMessage(String message, ChatbotAssistantUserRequest request) {
        String lower = message.toLowerCase();
        ProductFilterRequest.ProductFilterRequestBuilder builder = ProductFilterRequest.builder();
        
        // 1. Extract Brand IDs
        List<Long> brandIds = extractBrandIds(lower);
        if (!brandIds.isEmpty()) {
            builder.brandIds(brandIds);
            log.debug("📱 Phát hiện brands: {}", brandIds);
        }
        
        // 2. Extract RAM options
        List<String> ramOptions = extractRamOptions(lower);
        if (!ramOptions.isEmpty()) {
            builder.ramOptions(ramOptions);
            log.debug("💾 Phát hiện RAM: {}", ramOptions);
        }
        
        // 3. Extract Storage options
        List<String> storageOptions = extractStorageOptions(lower);
        if (!storageOptions.isEmpty()) {
            builder.storageOptions(storageOptions);
            log.debug("💿 Phát hiện Storage: {}", storageOptions);
        }
        
        // 4. Extract Battery
        Integer minBattery = extractMinBattery(lower);
        if (minBattery != null) {
            builder.minBattery(minBattery);
            log.debug("🔋 Phát hiện Battery: {}mAh", minBattery);
        }
        
        // 5. Extract OS
        List<String> osOptions = extractOsOptions(lower);
        if (!osOptions.isEmpty()) {
            builder.osOptions(osOptions);
            log.debug("🖥️ Phát hiện OS: {}", osOptions);
        }
        
        // 6. Extract Price Range
        extractPriceRange(lower, builder, request);
        
        // 7. Extract Rating
        Double minRating = extractMinRating(lower);
        if (minRating != null) {
            builder.minRating(minRating);
            log.debug("⭐ Phát hiện Rating: {}", minRating);
        }
        
        // 8. Category từ request
        if (request.getCategoryId() != null) {
            builder.categoryIds(List.of(request.getCategoryId()));
        }
        
        // 9. Discount
        if (lower.contains("giảm giá") || lower.contains("khuyến mãi") || 
            lower.contains("sale") || lower.contains("discount")) {
            builder.hasDiscountOnly(true);
        }
        
        // Pagination
        builder.page(0).size(10);
        
        return builder.build();
    }
    
    /**
     * Extract brand IDs từ message
     */
    private List<Long> extractBrandIds(String message) {
        List<Long> brandIds = new ArrayList<>();
        for (Map.Entry<String, Long> entry : BRAND_NAME_TO_ID.entrySet()) {
            if (message.contains(entry.getKey())) {
                if (!brandIds.contains(entry.getValue())) {
                    brandIds.add(entry.getValue());
                }
            }
        }
        return brandIds;
    }
    
    /**
     * Extract RAM options từ message
     * Hỗ trợ: "ram 8gb", "8gb ram", "ram từ 8gb", "ram 8 hoặc 12gb"
     */
    private List<String> extractRamOptions(String message) {
        List<String> options = new ArrayList<>();
        Pattern pattern = Pattern.compile("(\\d+)\\s*gb\\s*(ram)?|(ram)\\s*(\\d+)\\s*gb");
        Matcher matcher = pattern.matcher(message);
        
        while (matcher.find()) {
            String value = matcher.group(1) != null ? matcher.group(1) : matcher.group(4);
            if (value != null) {
                String normalized = normalizeRamOption(value + "GB");
                if (normalized != null && !options.contains(normalized)) {
                    options.add(normalized);
                }
            }
        }
        return options;
    }
    
    /**
     * Extract Storage options từ message
     */
    private List<String> extractStorageOptions(String message) {
        List<String> options = new ArrayList<>();
        Pattern pattern = Pattern.compile("(\\d+)\\s*(gb|tb)\\s*(storage|lưu trữ|bộ nhớ)?");
        Matcher matcher = pattern.matcher(message);
        
        while (matcher.find()) {
            String value = matcher.group(1);
            String unit = matcher.group(2).toUpperCase();
            if (value != null) {
                String normalized = normalizeStorageOption(value + unit);
                if (normalized != null && !options.contains(normalized)) {
                    options.add(normalized);
                }
            }
        }
        return options;
    }
    
    /**
     * Extract minimum battery từ message
     */
    private Integer extractMinBattery(String message) {
        // Pattern: "pin 5000mah", "5000 mah", "pin trâu", "pin lâu"
        if (message.contains("pin trâu") || message.contains("pin lâu") || message.contains("battery life")) {
            return 5000; // Default for "good battery"
        }
        
        Pattern pattern = Pattern.compile("(\\d{4,5})\\s*mah");
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return null;
    }
    
    /**
     * Extract OS options từ message
     */
    private List<String> extractOsOptions(String message) {
        List<String> options = new ArrayList<>();
        if (message.contains("iphone") || message.contains("ios") || message.contains("apple")) {
            options.add("iOS");
        }
        if (message.contains("android") || message.contains("samsung") || 
            message.contains("xiaomi") || message.contains("oppo") ||
            message.contains("vivo") || message.contains("realme")) {
            options.add("Android");
        }
        return options;
    }
    
    /**
     * Extract price range từ message
     */
    private void extractPriceRange(String message, ProductFilterRequest.ProductFilterRequestBuilder builder, 
                                   ChatbotAssistantUserRequest request) {
        // Use request values if provided
        if (request.getMinPrice() != null) {
            builder.minPrice(BigDecimal.valueOf(request.getMinPrice()));
        }
        if (request.getMaxPrice() != null) {
            builder.maxPrice(BigDecimal.valueOf(request.getMaxPrice()));
        }
        
        // Pattern: "dưới 10 triệu", "từ 5-10 triệu", "tầm 15 triệu", "8-12tr"
        Pattern rangePattern = Pattern.compile("(\\d+)\\s*[-–]\\s*(\\d+)\\s*(triệu|tr|m)");
        Matcher rangeMatcher = rangePattern.matcher(message);
        if (rangeMatcher.find()) {
            double min = Double.parseDouble(rangeMatcher.group(1)) * 1_000_000;
            double max = Double.parseDouble(rangeMatcher.group(2)) * 1_000_000;
            builder.minPrice(BigDecimal.valueOf(min));
            builder.maxPrice(BigDecimal.valueOf(max));
            return;
        }
        
        // Pattern: "dưới 10 triệu", "under 15tr"
        Pattern underPattern = Pattern.compile("(dưới|under|tối đa|max)\\s*(\\d+)\\s*(triệu|tr|m)");
        Matcher underMatcher = underPattern.matcher(message);
        if (underMatcher.find()) {
            double max = Double.parseDouble(underMatcher.group(2)) * 1_000_000;
            builder.maxPrice(BigDecimal.valueOf(max));
            return;
        }
        
        // Pattern: "trên 10 triệu", "từ 15tr"
        Pattern overPattern = Pattern.compile("(trên|từ|over|tối thiểu|min)\\s*(\\d+)\\s*(triệu|tr|m)");
        Matcher overMatcher = overPattern.matcher(message);
        if (overMatcher.find()) {
            double min = Double.parseDouble(overMatcher.group(2)) * 1_000_000;
            builder.minPrice(BigDecimal.valueOf(min));
            return;
        }
        
        // Pattern: "tầm 10 triệu" -> ±20%
        Pattern aroundPattern = Pattern.compile("(tầm|khoảng|around)\\s*(\\d+)\\s*(triệu|tr|m)");
        Matcher aroundMatcher = aroundPattern.matcher(message);
        if (aroundMatcher.find()) {
            double price = Double.parseDouble(aroundMatcher.group(2)) * 1_000_000;
            builder.minPrice(BigDecimal.valueOf(price * 0.8));
            builder.maxPrice(BigDecimal.valueOf(price * 1.2));
        }
    }
    
    /**
     * Extract minimum rating từ message
     */
    private Double extractMinRating(String message) {
        Pattern pattern = Pattern.compile("(\\d)\\s*sao|rating\\s*(\\d)|(\\d)\\s*⭐");
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            String value = matcher.group(1) != null ? matcher.group(1) : 
                          (matcher.group(2) != null ? matcher.group(2) : matcher.group(3));
            if (value != null) {
                return Double.parseDouble(value);
            }
        }
        return null;
    }
    
    /**
     * Normalize RAM option to valid values: 4GB, 6GB, 8GB, 12GB, 16GB
     */
    private String normalizeRamOption(String ram) {
        String normalized = ram.toUpperCase().replaceAll("\\s+", "");
        return switch (normalized) {
            case "2GB", "3GB", "4GB" -> "4GB";
            case "5GB", "6GB" -> "6GB";
            case "7GB", "8GB" -> "8GB";
            case "10GB", "11GB", "12GB" -> "12GB";
            case "14GB", "16GB", "18GB" -> "16GB";
            default -> null;
        };
    }
    
    /**
     * Normalize Storage option to valid values: 64GB, 128GB, 256GB, 512GB, 1TB
     */
    private String normalizeStorageOption(String storage) {
        String normalized = storage.toUpperCase().replaceAll("\\s+", "");
        if (normalized.contains("TB") || normalized.contains("1024")) {
            return "1TB";
        }
        return switch (normalized) {
            case "32GB", "64GB" -> "64GB";
            case "128GB" -> "128GB";
            case "256GB" -> "256GB";
            case "512GB" -> "512GB";
            default -> null;
        };
    }
    
    /**
     * Lấy sản phẩm dựa trên intent và filter
     * 
     * LOGIC:
     * - Nếu là intent đặc biệt (FEATURED, BEST_SELLING, NEW_ARRIVALS) → Gọi API tương ứng
     * - Nếu là SEARCH hoặc FILTER_* → Sử dụng filter đa tiêu chí với fallback strategy
     */
    private List<ChatbotAssistantUserResponse.RecommendedProductDTO> getProductsByIntentWithFilter(
            String intent, ProductFilterRequest filter, ChatbotAssistantUserRequest request) {
        
        // Kiểm tra xem filter có tiêu chí cụ thể không
        boolean hasFilter = hasSpecificFilterCriteria(filter);
        
        return switch (intent) {
            case "FEATURED" -> {
                log.info("⭐ Intent: Sản phẩm nổi bật");
                if (hasFilter) {
                    log.info("   → Có filter, kết hợp với tiêu chí nổi bật");
                    filter.setMinRating(4.0);
                    filter.setSortBy("rating");
                    filter.setSortDirection("desc");
                    yield searchWithMultiFilter(filter);
                }
                yield productService.getFeaturedProducts();
            }
            case "BEST_SELLING" -> {
                log.info("🔥 Intent: Sản phẩm bán chạy");
                if (hasFilter) {
                    log.info("   → Có filter, kết hợp với tiêu chí bán chạy");
                    filter.setSortBy("sold_count");
                    filter.setSortDirection("desc");
                    yield searchWithMultiFilter(filter);
                }
                yield productService.getBestSellingProducts();
            }
            case "NEW_ARRIVALS" -> {
                log.info("🆕 Intent: Sản phẩm mới");
                if (hasFilter) {
                    log.info("   → Có filter, kết hợp với tiêu chí mới nhất");
                    filter.setSortBy("created_date");
                    filter.setSortDirection("desc");
                    yield searchWithMultiFilter(filter);
                }
                yield productService.getNewArrivalsProducts();
            }
            case "FILTER_CAMERA" -> {
                log.info("📸 Intent: Điện thoại camera tốt");
                // Camera tốt thường đi với flagship hoặc mid-high range
                filter.setMinRating(4.0);
                filter.setSortBy("rating");
                filter.setSortDirection("desc");
                yield searchWithMultiFilter(filter);
            }
            case "FILTER_GAMING" -> {
                log.info("🎮 Intent: Điện thoại gaming/hiệu năng cao");
                // Gaming cần RAM cao, pin tốt
                if (filter.getRamOptions() == null || filter.getRamOptions().isEmpty()) {
                    filter.setRamOptions(List.of("8GB", "12GB", "16GB"));
                }
                filter.setMinBattery(4500);
                filter.setSortBy("rating");
                filter.setSortDirection("desc");
                yield searchWithMultiFilter(filter);
            }
            case "FILTER_BUDGET" -> {
                log.info("💰 Intent: Điện thoại giá rẻ/tiết kiệm");
                // Budget: dưới 8 triệu
                if (filter.getMaxPrice() == null) {
                    filter.setMaxPrice(java.math.BigDecimal.valueOf(8_000_000));
                }
                filter.setSortBy("rating");
                filter.setSortDirection("desc");
                yield searchWithMultiFilter(filter);
            }
            case "FILTER_FLAGSHIP" -> {
                log.info("👑 Intent: Điện thoại cao cấp/flagship");
                // Flagship: trên 20 triệu
                if (filter.getMinPrice() == null) {
                    filter.setMinPrice(java.math.BigDecimal.valueOf(20_000_000));
                }
                filter.setMinRating(4.5);
                filter.setSortBy("price");
                filter.setSortDirection("desc");
                yield searchWithMultiFilter(filter);
            }
            case "CATEGORY" -> {
                if (request.getCategoryId() != null) {
                    log.info("📁 Intent: Sản phẩm theo danh mục: {}", request.getCategoryId());
                    filter.setCategoryIds(List.of(request.getCategoryId()));
                    yield searchWithMultiFilter(filter);
                }
                yield searchWithMultiFilter(filter);
            }
            case "RELATED" -> {
                if (request.getProductId() != null) {
                    log.info("🔗 Intent: Sản phẩm liên quan: {}", request.getProductId());
                    yield productService.getRelatedProducts(request.getProductId());
                }
                yield searchWithMultiFilter(filter);
            }
            case "COMPARE" -> {
                log.info("⚖️ Intent: So sánh sản phẩm");
                if (hasFilter) {
                    filter.setSortBy("rating");
                    filter.setSortDirection("desc");
                    yield searchWithMultiFilter(filter);
                }
                yield productService.getBestSellingProducts();
            }
            default -> {
                // SEARCH, FILTER_* intents: sử dụng filter đa tiêu chí với fallback
                log.info("🔍 Intent: {} - Sử dụng filter đa tiêu chí", intent);
                yield searchWithMultiFilter(filter);
            }
        };
    }
    
    /**
     * Tìm kiếm sản phẩm với filter đa tiêu chí
     * 
     * CHIẾN LƯỢC FALLBACK:
     * 1. Tìm kiếm theo filter người dùng yêu cầu
     * 2. Nếu không có kết quả → Tìm theo các hãng lớn (Samsung, Apple) với cùng tiêu chí giá
     * 3. Nếu vẫn không có → Lấy sản phẩm mới nhất (New Arrivals)
     * 4. Cuối cùng → Lấy sản phẩm nổi bật (Featured)
     */
    private List<ChatbotAssistantUserResponse.RecommendedProductDTO> searchWithMultiFilter(
            ProductFilterRequest filter) {
        
        try {
            // BƯỚC 1: Tìm kiếm theo filter người dùng yêu cầu
            log.info("🔍 BƯỚC 1: Tìm kiếm theo filter người dùng");
            Page<ProductCardResponse> page = productViewService.filterProducts(filter);
            
            List<ChatbotAssistantUserResponse.RecommendedProductDTO> results = 
                page.getContent().stream()
                    .map(this::convertCardToRecommendedProduct)
                    .collect(Collectors.toList());
            
            if (!results.isEmpty()) {
                log.info("✅ Tìm thấy {} sản phẩm theo filter người dùng", results.size());
                return results;
            }
            
            // BƯỚC 2: Kiểm tra xem filter có rỗng không
            boolean hasSpecificCriteria = hasSpecificFilterCriteria(filter);
            
            if (!hasSpecificCriteria) {
                // Nếu không có tiêu chí cụ thể → Tìm theo hãng lớn (Samsung, Apple)
                log.info("🔍 BƯỚC 2: Không có tiêu chí cụ thể, tìm theo hãng lớn (Samsung, Apple)");
                results = searchByTopBrands(filter);
                
                if (!results.isEmpty()) {
                    log.info("✅ Tìm thấy {} sản phẩm từ hãng lớn", results.size());
                    return results;
                }
            } else {
                // Nếu có tiêu chí nhưng không tìm thấy → Nới lỏng filter
                log.info("🔍 BƯỚC 2: Có tiêu chí nhưng không tìm thấy, nới lỏng filter");
                results = searchWithRelaxedFilter(filter);
                
                if (!results.isEmpty()) {
                    log.info("✅ Tìm thấy {} sản phẩm với filter nới lỏng", results.size());
                    return results;
                }
            }
            
            // BƯỚC 3: Fallback về sản phẩm mới nhất
            log.info("🔍 BƯỚC 3: Fallback về sản phẩm mới nhất");
            results = productService.getNewArrivalsProducts();
            
            if (!results.isEmpty()) {
                log.info("✅ Lấy {} sản phẩm mới nhất", results.size());
                return results;
            }
            
            // BƯỚC 4: Cuối cùng - sản phẩm nổi bật
            log.info("🔍 BƯỚC 4: Fallback cuối cùng - sản phẩm nổi bật");
            results = productService.getFeaturedProducts();
            log.info("✅ Lấy {} sản phẩm nổi bật", results.size());
            
            return results;
            
        } catch (Exception e) {
            log.error("❌ Lỗi filterProducts: {}", e.getMessage());
            return productService.getFeaturedProducts();
        }
    }
    
    /**
     * Kiểm tra xem filter có tiêu chí cụ thể không
     */
    private boolean hasSpecificFilterCriteria(ProductFilterRequest filter) {
        return (filter.getBrandIds() != null && !filter.getBrandIds().isEmpty()) ||
               (filter.getCategoryIds() != null && !filter.getCategoryIds().isEmpty()) ||
               (filter.getRamOptions() != null && !filter.getRamOptions().isEmpty()) ||
               (filter.getStorageOptions() != null && !filter.getStorageOptions().isEmpty()) ||
               (filter.getOsOptions() != null && !filter.getOsOptions().isEmpty()) ||
               filter.getMinBattery() != null ||
               filter.getMinPrice() != null ||
               filter.getMaxPrice() != null ||
               filter.getMinRating() != null;
    }
    
    /**
     * Tìm kiếm theo các hãng lớn (Samsung, Apple) với cùng khoảng giá
     */
    private List<ChatbotAssistantUserResponse.RecommendedProductDTO> searchByTopBrands(
            ProductFilterRequest originalFilter) {
        
        ProductFilterRequest topBrandFilter = ProductFilterRequest.builder()
            .brandIds(TOP_BRAND_IDS)
            .minPrice(originalFilter.getMinPrice())
            .maxPrice(originalFilter.getMaxPrice())
            .sortBy("rating")
            .sortDirection("desc")
            .page(0)
            .size(10)
            .build();
        
        try {
            Page<ProductCardResponse> page = productViewService.filterProducts(topBrandFilter);
            return page.getContent().stream()
                .map(this::convertCardToRecommendedProduct)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("❌ Lỗi tìm theo hãng lớn: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
    
    /**
     * Nới lỏng filter để tìm sản phẩm tương tự
     * - Giữ lại: brand, price range (mở rộng 20%)
     * - Bỏ: các tiêu chí kỹ thuật chi tiết
     */
    private List<ChatbotAssistantUserResponse.RecommendedProductDTO> searchWithRelaxedFilter(
            ProductFilterRequest originalFilter) {
        
        // Mở rộng khoảng giá ±30%
        BigDecimal relaxedMinPrice = null;
        BigDecimal relaxedMaxPrice = null;
        
        if (originalFilter.getMinPrice() != null) {
            relaxedMinPrice = originalFilter.getMinPrice().multiply(BigDecimal.valueOf(0.7));
        }
        if (originalFilter.getMaxPrice() != null) {
            relaxedMaxPrice = originalFilter.getMaxPrice().multiply(BigDecimal.valueOf(1.3));
        }
        
        ProductFilterRequest relaxedFilter = ProductFilterRequest.builder()
            .brandIds(originalFilter.getBrandIds()) // Giữ brand
            .categoryIds(originalFilter.getCategoryIds()) // Giữ category
            .minPrice(relaxedMinPrice)
            .maxPrice(relaxedMaxPrice)
            // Bỏ các tiêu chí chi tiết: RAM, Storage, Battery, OS
            .sortBy("rating")
            .sortDirection("desc")
            .page(0)
            .size(10)
            .build();
        
        try {
            Page<ProductCardResponse> page = productViewService.filterProducts(relaxedFilter);
            return page.getContent().stream()
                .map(this::convertCardToRecommendedProduct)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("❌ Lỗi tìm với filter nới lỏng: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
    
    /**
     * Convert ProductCardResponse -> RecommendedProductDTO
     * Map đầy đủ thông tin cho Product Card
     */
    private ChatbotAssistantUserResponse.RecommendedProductDTO convertCardToRecommendedProduct(
            ProductCardResponse card) {
        // Build description từ specs
        String description = buildProductDescription(card);
        
        // Get price (ưu tiên discounted price)
        double price = card.getDiscountedPrice() != null 
            ? card.getDiscountedPrice().doubleValue() 
            : (card.getMinPrice() != null ? card.getMinPrice().doubleValue() : 0.0);
        
        // Get original price
        Double originalPrice = card.getOriginalPrice() != null 
            ? card.getOriginalPrice().doubleValue() : null;
        
        // Calculate discount percent
        Integer discountPercent = null;
        Boolean hasDiscount = false;
        if (originalPrice != null && price < originalPrice && originalPrice > 0) {
            discountPercent = (int) Math.round((1 - price / originalPrice) * 100);
            hasDiscount = discountPercent > 0;
        }
        
        return ChatbotAssistantUserResponse.RecommendedProductDTO.builder()
            .id(card.getId())
            .name(card.getName())
            .description(description)
            .price(price)
            .originalPrice(originalPrice)
            .rating(card.getAverageRating())
            .reviewCount(card.getTotalReviews())
            .imageUrl(card.getThumbnailUrl())
            .categoryName(card.getCategoryName())
            .productUrl("/products/" + card.getId())
            // Technical specs
            .ram(card.getRam())
            .storage(card.getStorage())
            .batteryCapacity(card.getBatteryCapacity())
            .operatingSystem(card.getOperatingSystem())
            .brandName(card.getBrandName())
            // Discount info
            .discountPercent(discountPercent)
            .hasDiscount(hasDiscount)
            // Sales info
            .soldCount(card.getSoldCount())
            .inStock(card.getInStock() != null ? card.getInStock() : true)
            .build();
    }
    
    /**
     * Build mô tả ngắn từ ProductCardResponse
     */
    private String buildProductDescription(ProductCardResponse card) {
        StringBuilder sb = new StringBuilder();
        if (card.getRam() != null) sb.append("RAM ").append(card.getRam());
        if (card.getStorage() != null) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(card.getStorage());
        }
        if (card.getBatteryCapacity() != null) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(card.getBatteryCapacity()).append("mAh");
        }
        if (card.getOperatingSystem() != null) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(card.getOperatingSystem());
        }
        return sb.length() > 0 ? sb.toString() : card.getName();
    }
    
    /**
     * Tính điểm relevance từ products
     */
    private double calculateRelevanceScore(
            List<ChatbotAssistantUserResponse.RecommendedProductDTO> products,
            String message) {
        if (products.isEmpty()) return 0.0;
        
        // Nếu có matchScore từ embedding, sử dụng nó
        OptionalDouble avgScore = products.stream()
            .filter(p -> p.getMatchScore() != null)
            .mapToDouble(ChatbotAssistantUserResponse.RecommendedProductDTO::getMatchScore)
            .average();
        
        if (avgScore.isPresent()) {
            return avgScore.getAsDouble();
        }
        
        // Default score dựa trên số lượng kết quả
        return Math.min(1.0, products.size() / 5.0);
    }
    
    /**
     * Phân loại intent từ câu hỏi
     * Ưu tiên API trực tiếp (không dùng embedding) để tối ưu chi phí
     * 
     * Các intent hỗ trợ:
     * - FEATURED: Sản phẩm nổi bật
     * - BEST_SELLING: Sản phẩm bán chạy
     * - NEW_ARRIVALS: Sản phẩm mới
     * - FILTER_RAM: Lọc theo RAM
     * - FILTER_STORAGE: Lọc theo dung lượng lưu trữ
     * - FILTER_BATTERY: Lọc theo pin
     * - FILTER_SCREEN: Lọc theo kích thước màn hình
     * - FILTER_OS: Lọc theo hệ điều hành
     * - FILTER_RATING: Lọc theo đánh giá sao
     * - CATEGORY: Xem sản phẩm theo danh mục
     * - COMPARE: So sánh sản phẩm
     * - SEARCH: Tìm kiếm (sử dụng embedding)
     */
    private String detectIntent(String message) {
        String lowerMessage = message.toLowerCase();
        
        // ========== CAMERA/CHỤP HÌNH ==========
        if (lowerMessage.matches(".*\\b(camera|chụp hình|chụp ảnh|chụp đêm|chụp đẹp|selfie|quay video|zoom|ống kính)\\b.*")) {
            return "FILTER_CAMERA";
        }
        
        // ========== GAMING/HIỆU NĂNG ==========
        if (lowerMessage.matches(".*\\b(game|gaming|chơi game|liên quân|pubg|hiệu năng|mạnh|cấu hình cao|chip|snapdragon|a17|a18)\\b.*")) {
            return "FILTER_GAMING";
        }
        
        // ========== GIÁ RẺ/TIẾT KIỆM ==========
        if (lowerMessage.matches(".*\\b(rẻ|giá rẻ|tiết kiệm|sinh viên|học sinh|ngân sách thấp|dưới 5 triệu|phân khúc thấp|giá tốt|khuyến mãi|giảm giá|sale)\\b.*")) {
            return "FILTER_BUDGET";
        }
        
        // ========== CAO CẤP/FLAGSHIP ==========
        if (lowerMessage.matches(".*\\b(cao cấp|flagship|pro max|ultra|premium|sang trọng|đắt tiền|hàng đầu|tốt nhất)\\b.*")) {
            return "FILTER_FLAGSHIP";
        }
        
        // RAM filters
        if (lowerMessage.matches(".*\\b(ram|bộ nhớ|memory)\\b.*") && 
            (lowerMessage.contains("4gb") || lowerMessage.contains("6gb") || 
             lowerMessage.contains("8gb") || lowerMessage.contains("12gb") || 
             lowerMessage.contains("16gb") || lowerMessage.contains("lọc theo ram"))) {
            return "FILTER_RAM";
        }
        
        // Storage filters
        if (lowerMessage.matches(".*\\b(storage|lưu trữ|dung lượng|bộ nhớ trong)\\b.*") && 
            (lowerMessage.contains("128gb") || lowerMessage.contains("256gb") || 
             lowerMessage.contains("512gb") || lowerMessage.contains("1tb") ||
             lowerMessage.contains("lọc theo storage"))) {
            return "FILTER_STORAGE";
        }
        
        // Battery filters - MỞ RỘNG
        if (lowerMessage.matches(".*\\b(pin|battery|mah|pin trâu|pin lâu|pin khỏe|dung lượng pin|sạc nhanh)\\b.*")) {
            return "FILTER_BATTERY";
        }
        
        // Screen size filters
        if (lowerMessage.matches(".*\\b(màn hình|screen|inch|màn lớn|màn nhỏ|màn đẹp|amoled|oled|lcd)\\b.*")) {
            return "FILTER_SCREEN";
        }
        
        // OS filters
        if (lowerMessage.matches(".*\\b(hệ điều hành|os|android|ios)\\b.*") && 
            (lowerMessage.contains("android") || lowerMessage.contains("ios") ||
             lowerMessage.contains("iphone") || lowerMessage.contains("samsung"))) {
            return "FILTER_OS";
        }
        
        // Rating filters
        if (lowerMessage.matches(".*\\b(đánh giá|rating|sao|⭐|review|nhận xét)\\b.*")) {
            return "FILTER_RATING";
        }
        
        // ========== BRAND SPECIFIC ==========
        if (lowerMessage.contains("iphone") || lowerMessage.contains("apple")) {
            return "FILTER_BRAND";
        }
        if (lowerMessage.contains("samsung") || lowerMessage.contains("galaxy")) {
            return "FILTER_BRAND";
        }
        if (lowerMessage.contains("xiaomi") || lowerMessage.contains("redmi") || lowerMessage.contains("poco")) {
            return "FILTER_BRAND";
        }
        if (lowerMessage.contains("oppo") || lowerMessage.contains("vivo") || lowerMessage.contains("realme")) {
            return "FILTER_BRAND";
        }
        
        // Featured products - MỞ RỘNG
        if (lowerMessage.contains("nổi bật") || lowerMessage.contains("best") || 
            lowerMessage.contains("recommended") || lowerMessage.contains("hàng đầu") ||
            lowerMessage.contains("top") || lowerMessage.contains("sản phẩm nổi bật") ||
            lowerMessage.contains("gợi ý") || lowerMessage.contains("đề xuất") ||
            lowerMessage.contains("tư vấn")) {
            return "FEATURED";
        }
        
        // Best selling products - MỞ RỘNG
        if (lowerMessage.contains("bán chạy") || lowerMessage.contains("best selling") || 
            lowerMessage.contains("hot") || lowerMessage.contains("popular") ||
            lowerMessage.contains("chạy nhất") || lowerMessage.contains("được yêu thích") ||
            lowerMessage.contains("nhiều người mua") || lowerMessage.contains("xu hướng")) {
            return "BEST_SELLING";
        }
        
        // New arrivals - MỞ RỘNG
        if (lowerMessage.contains("mới") || lowerMessage.contains("mới nhất") || 
            lowerMessage.contains("new") || lowerMessage.contains("latest") ||
            lowerMessage.contains("vừa về") || lowerMessage.contains("sản phẩm mới") ||
            lowerMessage.contains("ra mắt") || lowerMessage.contains("2024") || lowerMessage.contains("2025")) {
            return "NEW_ARRIVALS";
        }
        
        // Compare products
        if (lowerMessage.contains("so sánh") || lowerMessage.contains("compare") || 
            lowerMessage.contains("khác nhau") || lowerMessage.contains("difference") ||
            lowerMessage.contains("so với") || lowerMessage.contains("hay hơn") ||
            lowerMessage.contains("nên mua") || lowerMessage.contains("chọn cái nào")) {
            return "COMPARE";
        }
        
        // Category products
        if (lowerMessage.contains("danh mục") || lowerMessage.contains("category") || 
            lowerMessage.contains("loại") || lowerMessage.contains("dòng") ||
            lowerMessage.contains("theo danh mục") || lowerMessage.contains("loại điện thoại")) {
            return "CATEGORY";
        }
        
        // Related products
        if (lowerMessage.contains("liên quan") || lowerMessage.contains("related") ||
            lowerMessage.contains("giống") || lowerMessage.contains("tương tự") ||
            lowerMessage.contains("thay thế") || lowerMessage.contains("alternative")) {
            return "RELATED";
        }
        
        // Default: search (sử dụng keyword matching để tối ưu chi phí)
        return "SEARCH";
    }
    
    /**
     * Format giá theo định dạng tiền Việt chuẩn
     * Ví dụ: 8990000 -> "8.990.000₫"
     */
    private String formatVNDPrice(double price) {
        long priceInt = Math.round(price);
        return String.format("%,d₫", priceInt).replace(",", ".");
    }
    
    /**
     * Phát hiện loại sản phẩm/thiết bị user đang hỏi
     * KHÔNG block - chỉ trả về context để AI xử lý linh hoạt
     */
    private String detectProductContext(String message) {
        String lower = message.toLowerCase();
        
        // iPad/Tablet
        if (lower.contains("ipad") || lower.contains("tablet") || lower.contains("máy tính bảng")) {
            return "USER_ASKING_TABLET";
        }
        
        // Laptop/Máy tính
        if (lower.contains("laptop") || lower.contains("máy tính") || lower.contains("pc") || lower.contains("macbook")) {
            return "USER_ASKING_LAPTOP";
        }
        
        // Phụ kiện
        if (lower.contains("tai nghe") || lower.contains("headphone") || lower.contains("airpods") || 
            lower.contains("sạc") || lower.contains("charger") || lower.contains("ốp lưng") || lower.contains("case")) {
            return "USER_ASKING_ACCESSORY";
        }
        
        // Smart watch
        if (lower.contains("đồng hồ") || lower.contains("watch") || lower.contains("smartwatch") || lower.contains("apple watch")) {
            return "USER_ASKING_SMARTWATCH";
        }
        
        return null; // User đang hỏi về smartphone - bình thường
    }
    
    /**
     * Kiểm tra xem user có hỏi về brand không kinh doanh không
     * Trả về tên brand nếu tìm thấy, null nếu không
     */
    private String detectUnsupportedBrand(String message) {
        String lower = message.toLowerCase();
        for (Map.Entry<String, String> entry : UNSUPPORTED_BRANDS.entrySet()) {
            if (lower.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }
    
    /**
     * Detect brand mà user yêu cầu (cả supported và unsupported)
     * Trả về tên brand đập (display name)
     */
    private String detectRequestedBrand(String message) {
        String lower = message.toLowerCase();
        
        // Kiểm tra unsupported brands trước
        String unsupported = detectUnsupportedBrand(message);
        if (unsupported != null) return unsupported;
        
        // Kiểm tra supported brands
        if (lower.contains("iphone") || lower.contains("apple")) return "Apple/iPhone";
        if (lower.contains("samsung") || lower.contains("galaxy")) return "Samsung";
        if (lower.contains("xiaomi") || lower.contains("redmi") || lower.contains("poco")) return "Xiaomi";
        if (lower.contains("oppo") || lower.contains("reno")) return "OPPO";
        if (lower.contains("vivo")) return "Vivo";
        if (lower.contains("realme")) return "Realme";
        if (lower.contains("huawei") || lower.contains("honor")) return "Huawei";
        
        return null;
    }
    
    /**
     * Kiểm tra xem kết quả có chứa brand mà user yêu cầu không
     */
    private boolean hasRequestedBrandInResults(
            String requestedBrand, 
            List<ChatbotAssistantUserResponse.RecommendedProductDTO> products) {
        if (requestedBrand == null || products.isEmpty()) return true; // Không yêu cầu brand cụ thể
        
        String lowerBrand = requestedBrand.toLowerCase();
        return products.stream().anyMatch(p -> {
            String productName = p.getName() != null ? p.getName().toLowerCase() : "";
            String brandName = p.getBrandName() != null ? p.getBrandName().toLowerCase() : "";
            
            // Kiểm tra các biến thể của brand
            if (lowerBrand.contains("apple") || lowerBrand.contains("iphone")) {
                return productName.contains("iphone") || brandName.contains("apple");
            }
            if (lowerBrand.contains("samsung")) {
                return productName.contains("samsung") || productName.contains("galaxy") || brandName.contains("samsung");
            }
            if (lowerBrand.contains("xiaomi")) {
                return productName.contains("xiaomi") || productName.contains("redmi") || productName.contains("poco") || brandName.contains("xiaomi");
            }
            if (lowerBrand.contains("oppo")) {
                return productName.contains("oppo") || productName.contains("reno") || brandName.contains("oppo");
            }
            if (lowerBrand.contains("vivo")) {
                return productName.contains("vivo") || brandName.contains("vivo");
            }
            if (lowerBrand.contains("realme")) {
                return productName.contains("realme") || brandName.contains("realme");
            }
            if (lowerBrand.contains("huawei")) {
                return productName.contains("huawei") || productName.contains("honor") || brandName.contains("huawei");
            }
            
            return false;
        });
    }
    
    /**
     * Tạo phản hồi từ AI (với fallback API keys)
     * LOGIC LINH HOẠT - KHÔNG BLOCK, LUÔN TƯ VẤN:
     * 1. Detect context (brand, product type) → Thêm vào prompt
     * 2. AI sẽ xử lý linh hoạt, không cứng nhắc từ chối
     * 3. Nếu không tìm thấy sản phẩm → Tư vấn kỹ thuật chung
     */
    private String generateAiResponse(String userMessage, 
            List<ChatbotAssistantUserResponse.RecommendedProductDTO> products,
            String intent) {
        
        try {
            // Detect context - KHÔNG block, chỉ thêm context cho AI
            String productContext = detectProductContext(userMessage);
            String requestedBrand = detectRequestedBrand(userMessage);
            String unsupportedBrand = detectUnsupportedBrand(userMessage);
            
            // Xây dựng context đặc biệt nếu có
            StringBuilder specialContext = new StringBuilder();
            
            // Context cho sản phẩm khác loại (iPad, laptop, v.v.)
            if (productContext != null) {
                String productType = switch (productContext) {
                    case "USER_ASKING_TABLET" -> "iPad/Tablet/Máy tính bảng";
                    case "USER_ASKING_LAPTOP" -> "Laptop/Máy tính/MacBook";
                    case "USER_ASKING_ACCESSORY" -> "Phụ kiện (tai nghe, sạc, ốp lưng)";
                    case "USER_ASKING_SMARTWATCH" -> "Đồng hồ thông minh/Smartwatch";
                    default -> null;
                };
                if (productType != null) {
                    specialContext.append(String.format("""
                        
                        📝 CONTEXT ĐẶC BIỆT - USER HỎI VỀ: %s
                        → Cửa hàng KHÔNG kinh doanh sản phẩm này
                        → CÁCH XỬ LÝ: Nhẹ nhàng giải thích cửa hàng chuyên về smartphone,
                           sau đó TƯ VẤN KỸ THUẬT về nhu cầu thực sự của khách (cần màn hình lớn? cần di động? cần giải trí?)
                           và GỢI Ý điện thoại phù hợp từ danh sách (nếu có).
                           Nếu không có sản phẩm phù hợp, hãy tư vấn những tiêu chí nên tìm kiếm.
                        
                        """, productType));
                }
            }
            
            // Context cho brand không kinh doanh
            if (unsupportedBrand != null) {
                specialContext.append(String.format("""
                    
                    📝 CONTEXT ĐẶC BIỆT - BRAND KHÔNG KINH DOANH: %s
                    → Cửa hàng KHÔNG bán hãng này
                    → CÁCH XỬ LÝ: Nhẹ nhàng thông báo không có hãng %s,
                       nhưng HIỂU NHU CẦU của khách (pin trâu? camera tốt? giá rẻ? cao cấp?)
                       và GỢI Ý sản phẩm THAY THẾ với tính năng tương tự từ danh sách.
                    
                    """, unsupportedBrand, unsupportedBrand));
            }
            
            // Context cho brand mismatch (yêu cầu brand có bán nhưng không có trong kết quả)
            if (requestedBrand != null && unsupportedBrand == null) {
                boolean hasBrandInResults = hasRequestedBrandInResults(requestedBrand, products);
                if (!hasBrandInResults && !products.isEmpty()) {
                    specialContext.append(String.format("""
                        
                        📝 CONTEXT - BRAND MISMATCH: %s
                        → Khách yêu cầu hãng %s nhưng kết quả không có hãng này
                        → CÁCH XỬ LÝ: Giải thích hiện tại không có sản phẩm %s phù hợp tiêu chí,
                           nhưng có những sản phẩm thay thế tốt từ các hãng khác với tính năng tương tự.
                        
                        """, requestedBrand, requestedBrand, requestedBrand));
                }
            }
            
            // Tạo danh sách sản phẩm hoặc context tư vấn kỹ thuật
            String productListStr;
            String noProductAdvice = "";
            
            if (products.isEmpty()) {
                productListStr = "(Không tìm thấy sản phẩm phù hợp với tiêu chí)";
                noProductAdvice = """
                    
                    📝 KHÔNG TÌM THẤY SẢN PHẨM PHÙ HỢP:
                    → KHÔNG từ chối khách, hãy TƯ VẤN KỸ THUẬT:
                       - Giải thích tiêu chí nào có thể điều chỉnh
                       - Gợi ý các tính năng quan trọng nên tìm kiếm
                       - Đề xuất thử lại với tiêu chí khác (khoảng giá, RAM, v.v.)
                       - Thái độ thân thiện, sẵn sàng hỗ trợ
                    
                    """;
            } else {
                StringBuilder productList = new StringBuilder();
                for (int i = 0; i < Math.min(products.size(), 5); i++) {
                    var p = products.get(i);
                    StringBuilder specs = new StringBuilder();
                    if (p.getRam() != null) specs.append("RAM ").append(p.getRam());
                    if (p.getStorage() != null) {
                        if (specs.length() > 0) specs.append(", ");
                        specs.append(p.getStorage());
                    }
                    if (p.getBatteryCapacity() != null) {
                        if (specs.length() > 0) specs.append(", ");
                        specs.append("Pin ").append(p.getBatteryCapacity()).append("mAh");
                    }
                    
                    String priceStr = formatVNDPrice(p.getPrice());
                    String originalPriceStr = p.getOriginalPrice() != null && p.getOriginalPrice() > p.getPrice() 
                        ? " (giá gốc " + formatVNDPrice(p.getOriginalPrice()) + ")" : "";
                    
                    productList.append(String.format("%d. [%s] %s - %s%s (⭐%.1f, %d đánh giá) - %s\n",
                        i + 1, 
                        p.getBrandName() != null ? p.getBrandName() : "N/A",
                        p.getName(), priceStr, originalPriceStr,
                        p.getRating() != null ? p.getRating() : 0.0, 
                        p.getReviewCount() != null ? p.getReviewCount() : 0,
                        specs.length() > 0 ? specs.toString() : ""));
                }
                productListStr = productList.toString();
            }
            
            // Prompt thông minh - LINH HOẠT, KHÔNG CỨNG NHẮC
            String prompt = String.format("""
                🎯 BẠN LÀ: Chuyên viên tư vấn điện thoại **nhiệt tình, am hiểu** tại **UTE Phone Hub**.
                
                ═══════════════════════════════════════════════════════════════
                🏪 THÔNG TIN CỬA HÀNG:
                ═══════════════════════════════════════════════════════════════
                - Tên: UTE Phone Hub - Cửa hàng điện thoại chính hãng
                - Chuyên bán: Thiết bị di động, phụ kiện chính hãng, máy tính, đồng hồ, đồ công nghệ,..
                - Các hãng đang kinh doanh: %s
                %s%s
                ═══════════════════════════════════════════════════════════════
                💡 NGUYÊN TẮC TƯ VẤN - LINH HOẠT & THÂN THIỆN:
                ═══════════════════════════════════════════════════════════════
                1. ✅ LUÔN TƯ VẤN - Không bao giờ từ chối khách
                2. ✅ HIỂU NHU CẦU THỰC SỰ - Khách cần gì? (pin trâu, camera, giá rẻ, cao cấp?)
                3. ✅ NẾU không có sản phẩm phù hợp → TƯ VẤN KỸ THUẬT (tiêu chí nên tìm, xu hướng thị trường)
                4. ✅ NẾU khách hỏi sản phẩm không bán → Nhẹ nhàng giải thích và gợi ý thay thế
                5. ✅ Giữ thái độ THÂN THIỆN, CHUYÊN NGHIỆP, không cứng nhắc
                6. ❌ KHÔNG bịa đặt sản phẩm hoặc giá (nếu có danh sách, chỉ gợi ý từ đó)
                
                ═══════════════════════════════════════════════════════════════
                🎨 FORMAT (Markdown):
                ═══════════════════════════════════════════════════════════════
                - **In đậm** cho điểm quan trọng
                - *In nghiêng* cho lời khuyên
                - `highlight` cho thông số kỹ thuật
                - Emoji phù hợp: 👋 🔥 💡 📱 ⭐ 🏆 💰 🙏
                
                ═══════════════════════════════════════════════════════════════
                📌 YÊU CẦU KHÁCH HÀNG: "%s"
                🎯 INTENT: %s
                
                📦 DANH SÁCH SẢN PHẨM CÓ SẴN:
                %s
                
                🚀 Hãy tư vấn TỰ NHIÊN, THÂN THIỆN như đang trò chuyện với khách hàng thực!
                """, SUPPORTED_BRANDS_TEXT, specialContext.toString(), noProductAdvice, 
                     userMessage, intent, productListStr);
            
            Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                    Map.of(
                        "parts", List.of(
                            Map.of("text", prompt)
                        )
                    )
                ),
                "generationConfig", Map.of(
                    "temperature", 0.75,
                    "maxOutputTokens", 1000,
                    "topP", 0.9,
                    "topK", 40
                )
            );
            
            String requestJson = objectMapper.writeValueAsString(requestBody);
            
            log.debug("📤 Gửi request đến AI (fallback enabled)");
            
            // Sử dụng fallback service với xoay vòng API keys
            String responseJson = fallbackService.executeWithFallback(requestJson, false);
            
            JsonNode responseNode = objectMapper.readTree(responseJson);
            String aiText = responseNode
                .path("candidates")
                .get(0)
                .path("content")
                .path("parts")
                .get(0)
                .path("text")
                .asText();
            
            log.debug("✅ Nhận phản hồi từ AI");
            return aiText;
            
        } catch (Exception e) {
            log.error("❌ Lỗi tạo phản hồi AI: {}", e.getMessage());
            return formatDefaultResponse(products, intent, userMessage);
        }
    }
    
    /**
     * Phản hồi mặc định khi AI không khả dụng
     * LINH HOẠT - luôn tư vấn, không từ chối
     */
    private String formatDefaultResponse(
            List<ChatbotAssistantUserResponse.RecommendedProductDTO> products,
            String intent,
            String userMessage) {
        
        StringBuilder response = new StringBuilder();
        
        // Detect context để điều chỉnh response
        String productContext = detectProductContext(userMessage);
        String unsupportedBrand = detectUnsupportedBrand(userMessage);
        
        // Nếu user hỏi về sản phẩm không bán hoặc brand không kinh doanh
        if (productContext != null || unsupportedBrand != null) {
            response.append("👋 Xin chào! Cảm ơn bạn đã quan tâm đến **UTE Phone Hub**!\n\n");
            
            if (productContext != null) {
                String productType = switch (productContext) {
                    case "USER_ASKING_TABLET" -> "iPad/Tablet";
                    case "USER_ASKING_LAPTOP" -> "Laptop/Máy tính";
                    case "USER_ASKING_ACCESSORY" -> "Phụ kiện";
                    case "USER_ASKING_SMARTWATCH" -> "Đồng hồ thông minh";
                    default -> "sản phẩm này";
                };
                response.append(String.format("📱 Hiện tại cửa hàng chúng tôi **chuyên kinh doanh điện thoại di động**. " +
                    "Tuy %s chưa có trong danh mục, nhưng tôi có thể giúp bạn tìm một chiếc smartphone phù hợp!\n\n", productType));
            }
            
            if (unsupportedBrand != null) {
                response.append(String.format("📱 Hãng **%s** hiện chưa có trong kho của chúng tôi. " +
                    "Tuy nhiên, tôi có thể gợi ý những sản phẩm tương đương!\n\n", unsupportedBrand));
            }
        }
        
        // Nếu không có sản phẩm
        if (products.isEmpty()) {
            response.append("🔍 Hiện tại chưa tìm thấy sản phẩm phù hợp với tiêu chí của bạn.\n\n");
            response.append("💡 **Gợi ý cho bạn:**\n");
            response.append("• Thử điều chỉnh khoảng giá rộng hơn\n");
            response.append("• Xem xét các hãng khác với tính năng tương tự\n");
            response.append("• Mô tả lại nhu cầu (pin trâu, camera tốt, chơi game...)\n\n");
            response.append("📱 **Các hãng chúng tôi đang kinh doanh:** " + SUPPORTED_BRANDS_TEXT + "\n\n");
            response.append("💬 Hãy cho tôi biết thêm về nhu cầu của bạn, tôi sẵn lòng hỗ trợ!");
            return response.toString();
        }
        
        // Lời chào dựa trên intent - FORMAT ĐẸP với emoji
        if (response.length() == 0) {
            switch (intent) {
                case "FEATURED" -> response.append("🌟 **Sản phẩm nổi bật** được khách hàng yêu thích nhất!\n\n");
                case "BEST_SELLING" -> response.append("🔥 **Top sản phẩm bán chạy** tại UTE Phone Hub!\n\n");
                case "NEW_ARRIVALS" -> response.append("✨ **Hàng mới về** - Công nghệ mới nhất dành cho bạn!\n\n");
                case "FILTER_BATTERY" -> response.append("🔋 *Đây là những điện thoại pin trâu cho bạn!*\n\n");
                case "FILTER_CAMERA" -> response.append("📸 *Điện thoại camera tốt cho bạn!*\n\n");
                case "FILTER_GAMING" -> response.append("🎮 *Điện thoại gaming hiệu năng cao!*\n\n");
                default -> response.append("📱 *Dựa trên yêu cầu, tôi đề xuất cho bạn:*\n\n");
            }
        } else {
            response.append("💡 *Tôi xin gợi ý những sản phẩm phù hợp với nhu cầu của bạn:*\n\n");
        }
        
        // Hiển thị TOP 3 sản phẩm với format đẹp
        int displayCount = Math.min(products.size(), 3);
        String[] medals = {"🏆", "🥈", "🥉"};
        
        for (int i = 0; i < displayCount; i++) {
            var product = products.get(i);
            response.append(String.format("%s **%s** - **%s**\n", 
                medals[i], product.getName(), formatVNDPrice(product.getPrice())));
            
            // Thông số kỹ thuật dạng highlight
            StringBuilder specs = new StringBuilder();
            specs.append("   • *Cấu hình:* ");
            if (product.getRam() != null) {
                specs.append("`").append(product.getRam()).append("` ");
            }
            if (product.getStorage() != null) {
                specs.append("`").append(product.getStorage()).append("` ");
            }
            if (product.getBatteryCapacity() != null) {
                specs.append("`Pin ").append(product.getBatteryCapacity()).append("mAh`");
            }
            response.append(specs).append("\n");
            
            // Rating và discount
            if (product.getRating() != null && product.getRating() > 0) {
                response.append(String.format("   • *Đánh giá:* ⭐ %.1f (%d reviews)\n", 
                    product.getRating(), product.getReviewCount() != null ? product.getReviewCount() : 0));
            }
            
            if (product.getOriginalPrice() != null && product.getOriginalPrice() > product.getPrice()) {
                double saved = product.getOriginalPrice() - product.getPrice();
                response.append(String.format("   • 💰 *Tiết kiệm:* ~~%s~~ → Giảm **%s**!\n", 
                    formatVNDPrice(product.getOriginalPrice()), formatVNDPrice(saved)));
            }
            response.append("\n");
        }
        
        response.append("💡 *Mẹo:* Hãy so sánh cấu hình để chọn máy phù hợp nhất với nhu cầu!\n\n");
        response.append("👆 **Nhấn vào sản phẩm bên dưới để xem chi tiết và đặt hàng nhé!**");
        
        return response.toString();
    }
}
