package com.utephonehub.backend.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.utephonehub.backend.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    // Soft delete queries (default behavior with @Where annotation)
    Optional<Product> findByIdAndIsDeletedFalse(Long id);
    
    List<Product> findByIsDeletedFalse();
    
    @EntityGraph(attributePaths = {"images", "category", "brand"})
    Page<Product> findByIsDeletedFalse(Pageable pageable);
    
    List<Product> findByStatusTrue();
    
    Page<Product> findByStatusTrueAndIsDeletedFalse(Pageable pageable);
    
    List<Product> findByStatusTrueAndIsDeletedFalse();
    
    // Category & Brand queries
    List<Product> findByCategoryIdAndIsDeletedFalse(Long categoryId);
    
    Page<Product> findByCategoryIdAndIsDeletedFalse(Long categoryId, Pageable pageable);
    
    List<Product> findByBrandIdAndIsDeletedFalse(Long brandId);
    
    Page<Product> findByBrandIdAndIsDeletedFalse(Long brandId, Pageable pageable);
    
    // Search queries
    @EntityGraph(attributePaths = {"images", "category", "brand"})
    @Query("SELECT p FROM Product p WHERE p.isDeleted = false AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> searchProducts(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.isDeleted = false AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Product> searchProducts(@Param("keyword") String keyword);
    
    // Advanced filtering with price range - only returns products with templates when price filter active
    @EntityGraph(attributePaths = {"images", "category", "brand"})
    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN p.templates t " +
           "WHERE p.isDeleted = false " +
           "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
           "AND (:brandId IS NULL OR p.brand.id = :brandId) " +
           "AND p.status = true " +
           "AND (" +
           "  (:minPrice IS NULL AND :maxPrice IS NULL) " + 
           "  OR (" +
           "    t.id IS NOT NULL " + // Ensures product has templates when price filter is used
           "    AND t.status = true " +
           "    AND (:minPrice IS NULL OR t.price >= :minPrice) " +
           "    AND (:maxPrice IS NULL OR t.price <= :maxPrice)" +
           "  )" +
           ")")
    Page<Product> filterProducts(@Param("categoryId") Long categoryId,
                                  @Param("brandId") Long brandId,
                                  @Param("minPrice") BigDecimal minPrice,
                                  @Param("maxPrice") BigDecimal maxPrice,
                                  Pageable pageable);
    
    // Admin queries (include deleted products) - Use native query to bypass @Where annotation
    @Query(value = "SELECT * FROM products WHERE id = :id", nativeQuery = true)
    Optional<Product> findByIdIncludingDeleted(@Param("id") Long id);
    
    @Query(value = """
           SELECT p FROM Product p 
           LEFT JOIN FETCH p.category 
           LEFT JOIN FETCH p.brand 
           WHERE 1=1
           ORDER BY p.createdAt DESC
           """,
           countQuery = "SELECT COUNT(p) FROM Product p")
    Page<Product> findAllIncludingDeleted(Pageable pageable);
    
    // Query for deleted products only (isDeleted=true)
    @EntityGraph(attributePaths = {"images"})
    @Query("""
           SELECT p FROM Product p 
           LEFT JOIN FETCH p.category 
           LEFT JOIN FETCH p.brand 
           WHERE p.isDeleted = true
           AND (:keyword IS NULL OR :keyword = '' OR 
                LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR 
                LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
           AND (:categoryId IS NULL OR p.category.id = :categoryId)
           AND (:brandId IS NULL OR p.brand.id = :brandId)
           """)
    Page<Product> findDeletedProducts(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("brandId") Long brandId,
            Pageable pageable);
    
    // Count queries
    long countByIsDeletedFalse();
    
    long countByCategoryIdAndIsDeletedFalse(Long categoryId);
    
    long countByBrandIdAndIsDeletedFalse(Long brandId);
    
    // Check existence
    boolean existsByIdAndIsDeletedFalse(Long id);
    
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Product p " +
           "WHERE p.name = :name AND p.isDeleted = false AND (:id IS NULL OR p.id != :id)")
    boolean existsByNameAndNotDeleted(@Param("name") String name, @Param("id") Long id);
    
    // Bulk operations (for OrderService)
    List<Product> findAllByIdIn(List<Long> ids);
    
    // Check if products exist in category/brand (for admin deletion validation)
    boolean existsByCategoryId(Long categoryId);
    
    boolean existsByBrandId(Long brandId);
    
    // Low stock query (for Dashboard) - calculates total stock from templates
    @Query("SELECT p FROM Product p " +
           "LEFT JOIN p.templates t " +
           "WHERE p.status = true " +
           "GROUP BY p " +
           "HAVING COALESCE(SUM(CASE WHEN t.status = true THEN t.stockQuantity ELSE 0 END), 0) <= :threshold " +
           "ORDER BY COALESCE(SUM(CASE WHEN t.status = true THEN t.stockQuantity ELSE 0 END), 0) ASC")
    List<Product> findByStockQuantityLessThanEqualAndStatusTrueOrderByStockQuantityAsc(@Param("threshold") Integer threshold);
    
    // ==================== PRODUCTVIEW OPTIMIZED QUERIES ====================
    
    /**
     * Query tối ưu cho ProductView - JOIN FETCH để tránh N+1 problem
     * Load sẵn category, brand, templates, images, metadata trong 1 query
     */
    @Query(value = "SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.brand " +
           "WHERE p.status = true AND p.isDeleted = false " +
           "ORDER BY p.createdAt DESC",
           countQuery = "SELECT COUNT(DISTINCT p) FROM Product p WHERE p.status = true AND p.isDeleted = false")
    Page<Product> findAllForProductView(Pageable pageable);
    
    /**
     * Search ProductView bằng đối sánh chuỗi con cơ bản để tránh lỗi HQL phức tạp
     */
    @Query(value = """
               SELECT DISTINCT p FROM Product p
               LEFT JOIN FETCH p.category c
               LEFT JOIN FETCH p.brand b
               WHERE p.status = true
               AND p.isDeleted = false
               AND (
                      :keyword IS NULL OR :keyword = ''
                      OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                      OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
                      OR LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                      OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
               )
               """,
               countQuery = """
               SELECT COUNT(DISTINCT p) FROM Product p
               LEFT JOIN p.category c
               LEFT JOIN p.brand b
               WHERE p.status = true
               AND p.isDeleted = false
               AND (
                      :keyword IS NULL OR :keyword = ''
                      OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                      OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
                      OR LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                      OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
               )
               """)
    Page<Product> searchProductsOptimized(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * Filter by category với JOIN FETCH
     */
    @Query(value = "SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.brand " +
           "WHERE p.status = true AND p.isDeleted = false " +
           "AND p.category.id = :categoryId",
           countQuery = "SELECT COUNT(DISTINCT p) FROM Product p WHERE p.status = true AND p.isDeleted = false AND p.category.id = :categoryId")
    Page<Product> findByCategoryIdOptimized(@Param("categoryId") Long categoryId, Pageable pageable);
    
    /**
     * Advanced filter với JOIN FETCH và price range
     * Hỗ trợ lọc đa category và đa brand với logic OR
     */
    @Query(value = "SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.brand " +
           "LEFT JOIN p.templates t " +
           "WHERE p.isDeleted = false " +
           "AND p.status = true " +
           "AND (:categoryIds IS NULL OR p.category.id IN :categoryIds) " +
           "AND (:brandIds IS NULL OR p.brand.id IN :brandIds) " +
           "AND ((:minPrice IS NULL AND :maxPrice IS NULL) " +
           "OR (t.status = true " +
           "AND (:minPrice IS NULL OR t.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR t.price <= :maxPrice)))",
           countQuery = "SELECT COUNT(DISTINCT p) FROM Product p " +
                       "LEFT JOIN p.templates t " +
                       "WHERE p.isDeleted = false AND p.status = true " +
                       "AND (:categoryIds IS NULL OR p.category.id IN :categoryIds) " +
                       "AND (:brandIds IS NULL OR p.brand.id IN :brandIds) " +
                       "AND ((:minPrice IS NULL AND :maxPrice IS NULL) " +
                       "OR (t.status = true " +
                       "AND (:minPrice IS NULL OR t.price >= :minPrice) " +
                       "AND (:maxPrice IS NULL OR t.price <= :maxPrice)))")
    Page<Product> filterProductsOptimized(
            @Param("categoryIds") List<Long> categoryIds,
            @Param("brandIds") List<Long> brandIds,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

    // ==================== OPTIMIZED QUERIES FOR HOMEPAGE SECTIONS ====================
    
    /**
     * Lấy sản phẩm mới nhất với LIMIT tại DB level (tối ưu hiệu suất)
     * Sắp xếp theo createdAt DESC trong query thay vì in-memory
     */
    @Query(value = "SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.brand " +
           "WHERE p.status = true AND p.isDeleted = false " +
           "ORDER BY p.createdAt DESC",
           countQuery = "SELECT COUNT(p) FROM Product p WHERE p.status = true AND p.isDeleted = false")
    Page<Product> findNewArrivalsOptimized(Pageable pageable);
    
    /**
     * Lấy sản phẩm đang sale (có promotion active)
     * JOIN với templates để lấy thông tin giá
     */
    @Query(value = "SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.brand " +
           "WHERE p.status = true AND p.isDeleted = false " +
           "ORDER BY p.createdAt DESC",
           countQuery = "SELECT COUNT(p) FROM Product p WHERE p.status = true AND p.isDeleted = false")
    Page<Product> findProductsForSaleCheck(Pageable pageable);
    
    /**
     * Lấy sản phẩm nổi bật - dùng cho Featured Products section
     * Sẽ được filter thêm bởi service layer theo rating và sold count
     */
    @Query(value = "SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.brand " +
           "WHERE p.status = true AND p.isDeleted = false " +
           "ORDER BY p.createdAt DESC",
           countQuery = "SELECT COUNT(p) FROM Product p WHERE p.status = true AND p.isDeleted = false")
    Page<Product> findForFeaturedProducts(Pageable pageable);
    
    /**
     * Lấy sản phẩm theo danh sách IDs với JOIN FETCH
     * Dùng cho Featured Products sau khi đã có list IDs từ ReviewRepository
     */
    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.brand " +
           "WHERE p.id IN :ids AND p.status = true AND p.isDeleted = false")
    List<Product> findByIdsWithDetails(List<Long> ids);
    
    /**
     * Đếm tổng sản phẩm active - dùng cho cache validation
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.status = true AND p.isDeleted = false")
    long countActiveProducts();
    
    /**
     * Lấy TẤT CẢ sản phẩm matching filters (không pagination)
     * Dùng cho sorting theo computed fields (price, rating, soldCount)
     * Cần sort trong service layer sau khi tính toán
     */
    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.brand " +
           "LEFT JOIN p.templates t " +
           "WHERE p.isDeleted = false AND p.status = true " +
           "AND (:categoryIds IS NULL OR p.category.id IN :categoryIds) " +
           "AND (:brandIds IS NULL OR p.brand.id IN :brandIds) " +
           "AND ((:minPrice IS NULL AND :maxPrice IS NULL) " +
           "OR (t.status = true " +
           "AND (:minPrice IS NULL OR t.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR t.price <= :maxPrice)))")
    List<Product> filterProductsAll(
            @Param("categoryIds") List<Long> categoryIds,
            @Param("brandIds") List<Long> brandIds,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice);
}

