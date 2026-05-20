package com.utephonehub.backend.dto.response.productview;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Response DTO cho danh sách sản phẩm theo danh mục
 * Bao gồm thông tin danh mục và breadcrumb
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryProductsResponse {
    
    private CategoryInfo category;
    private Page<ProductCardResponse> products; // Thêm field products
    private List<BreadcrumbItem> breadcrumbs;
    private List<CategoryInfo> subCategories;
    private FilterOptions filterOptions;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryInfo {
        private Long id;
        private String name;
        private String description;
        private Integer productCount;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BreadcrumbItem {
        private Long id;
        private String name;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FilterOptions {
        private List<BrandOption> availableBrands;
        private PriceRange priceRange;
        private List<RatingOption> ratingOptions;
        private List<StorageOption> storageOptions;
        private List<RamOption> ramOptions;
        private List<BatteryOption> batteryOptions;
        private List<ScreenSizeOption> screenSizeOptions;
        private List<OsOption> osOptions;
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class BrandOption {
            private Long id;
            private String name;
            private Integer productCount;
        }
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class PriceRange {
            private java.math.BigDecimal min;
            private java.math.BigDecimal max;
        }
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class RatingOption {
            private Integer stars;
            private Integer count;
        }
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class StorageOption {
            private String value;
            private String displayValue;
            private Integer count;
        }
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class RamOption {
            private String value;
            private String displayValue;
            private Integer count;
        }
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class BatteryOption {
            private String value;
            private String displayValue;
            private Integer count;
        }
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ScreenSizeOption {
            private String value;
            private String displayValue;
            private Integer count;
        }
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class OsOption {
            private String value;
            private Integer count;
        }
    }
}
