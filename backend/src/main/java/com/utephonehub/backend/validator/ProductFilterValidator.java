package com.utephonehub.backend.validator;

import com.utephonehub.backend.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Validator for Product filter/search/sort parameters
 * Extracted from ProductController to follow Single Responsibility Principle
 */
@Component
@Slf4j
public class ProductFilterValidator {

    private static final String VALID_SORT_FIELDS_REGEX = "^(name|price|stockQuantity|createdAt)$";
    private static final String VALID_SORT_DIRECTION_REGEX = "^(asc|desc)$";
    private static final int MIN_KEYWORD_LENGTH = 2;

    /**
     * Validate price range parameters
     */
    public void validatePriceRange(Double minPrice, Double maxPrice) {
        // minPrice <= maxPrice
        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            log.error("Invalid price range: minPrice ({}) > maxPrice ({})", minPrice, maxPrice);
            throw new BadRequestException("Giá tối thiểu không thể lớn hơn giá tối đa");
        }

        // Prices must be non-negative
        if (minPrice != null && minPrice < 0) {
            throw new BadRequestException("Giá tối thiểu phải lớn hơn hoặc bằng 0");
        }
        if (maxPrice != null && maxPrice < 0) {
            throw new BadRequestException("Giá tối đa phải lớn hơn hoặc bằng 0");
        }
    }

    /**
     * Validate sortBy field
     */
    public void validateSortBy(String sortBy) {
        if (!sortBy.matches(VALID_SORT_FIELDS_REGEX)) {
            log.error("Invalid sortBy parameter: {}", sortBy);
            throw new BadRequestException(
                    "Tham số sortBy không hợp lệ. Chỉ chấp nhận: name, price, stockQuantity, createdAt");
        }
    }

    /**
     * Validate sortDirection
     */
    public void validateSortDirection(String sortDirection) {
        if (!sortDirection.matches(VALID_SORT_DIRECTION_REGEX)) {
            log.error("Invalid sortDirection parameter: {}", sortDirection);
            throw new BadRequestException(
                    "Tham số sortDirection không hợp lệ. Chỉ chấp nhận: asc, desc");
        }
    }

    /**
     * Validate search keyword min length
     */
    public void validateKeyword(String keyword) {
        if (keyword != null && !keyword.trim().isEmpty() && keyword.trim().length() < MIN_KEYWORD_LENGTH) {
            log.error("Search keyword too short: {}", keyword);
            throw new BadRequestException(
                    "Từ khóa tìm kiếm phải có ít nhất " + MIN_KEYWORD_LENGTH + " ký tự");
        }
    }

    /**
     * Validate all product filter parameters at once
     */
    public void validateAll(String keyword, Double minPrice, Double maxPrice, 
                           String sortBy, String sortDirection) {
        validatePriceRange(minPrice, maxPrice);
        validateSortBy(sortBy);
        validateSortDirection(sortDirection);
        validateKeyword(keyword);
    }
}
